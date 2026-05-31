package projetowebsd.ecommerceback.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projetowebsd.ecommerceback.dto.order.OrderFilterDTO;
import projetowebsd.ecommerceback.dto.order.OrderItemDTO;
import projetowebsd.ecommerceback.dto.order.OrderRequestDTO;
import projetowebsd.ecommerceback.dto.order.OrderResponseDTO;
import projetowebsd.ecommerceback.exception.BusinessException;
import projetowebsd.ecommerceback.exception.ResourceNotFoundException;
import projetowebsd.ecommerceback.model.Order;
import projetowebsd.ecommerceback.model.OrderItem;
import projetowebsd.ecommerceback.model.Product;
import projetowebsd.ecommerceback.model.User;
import projetowebsd.ecommerceback.model.enums.OrderStatus;
import projetowebsd.ecommerceback.repository.OrderRepository;
import projetowebsd.ecommerceback.repository.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final NotificationService notificationService;

    @Transactional
    public OrderResponseDTO placeOrder(OrderRequestDTO request) {
        User user = getCurrentUser();

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemDTO itemDTO : request.items()) {
            Product product = productService.getProduct(itemDTO.productId());

            if (!product.getActive()) {
                throw new BusinessException("Produto indisponível: " + product.getName());
            }
            if (product.getStockQuantity() < itemDTO.quantity()) {
                throw new BusinessException("Estoque insuficiente para: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - itemDTO.quantity());

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemDTO.quantity())
                    .unitPrice(product.getPrice())
                    .build();

            items.add(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.quantity())));

            notificationService.notifyStockChange(product);
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(total)
                .shippingCep(request.shippingCep())
                .shippingStreet(request.shippingStreet())
                .shippingNeighborhood(request.shippingNeighborhood())
                .shippingCity(request.shippingCity())
                .shippingState(request.shippingState())
                .shippingNumber(request.shippingNumber())
                .shippingComplement(request.shippingComplement())
                .items(items)
                .build();

        items.forEach(item -> item.setOrder(order));

        Order saved = orderRepository.save(order);

        notificationService.notifyOrderStatusChange(saved);
        notificationService.notifyAdminNewOrder(saved);

        return OrderResponseDTO.from(saved);
    }

    public List<OrderResponseDTO> getMyOrders() {
        User user = getCurrentUser();
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(OrderResponseDTO::from)
                .toList();
    }

    public List<OrderResponseDTO> getAllOrders(OrderFilterDTO filter) {
        Specification<Order> spec = buildSpec(filter);
        return orderRepository.findAll(spec).stream()
                .map(OrderResponseDTO::from)
                .toList();
    }

    @Transactional
    public OrderResponseDTO approve(UUID id) {
        Order order = getPendingOrder(id);
        order.setStatus(OrderStatus.APPROVED);
        Order saved = orderRepository.save(order);
        notificationService.notifyOrderStatusChange(saved);
        return OrderResponseDTO.from(saved);
    }

    @Transactional
    public OrderResponseDTO reject(UUID id) {
        Order order = getPendingOrder(id);
        order.setStatus(OrderStatus.REJECTED);

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            notificationService.notifyStockChange(product);
        }

        Order saved = orderRepository.save(order);
        notificationService.notifyOrderStatusChange(saved);
        return OrderResponseDTO.from(saved);
    }

    public long countPending() {
        return orderRepository.countByStatus(OrderStatus.PENDING);
    }

    private Order getPendingOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + id));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Apenas pedidos pendentes podem ser aprovados ou rejeitados");
        }
        return order;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private Specification<Order> buildSpec(OrderFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.userId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), filter.userId()));
            }
            if (filter.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"), filter.startDate().atStartOfDay()
                ));
            }
            if (filter.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"), filter.endDate().atTime(23, 59, 59)
                ));
            }
            if (filter.minAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalAmount"), filter.minAmount()));
            }
            if (filter.maxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalAmount"), filter.maxAmount()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
