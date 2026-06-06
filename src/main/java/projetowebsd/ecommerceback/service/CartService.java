package projetowebsd.ecommerceback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projetowebsd.ecommerceback.dto.cart.CartItemRequestDTO;
import projetowebsd.ecommerceback.dto.cart.CartItemResponseDTO;
import projetowebsd.ecommerceback.dto.cart.CartResponseDTO;
import projetowebsd.ecommerceback.model.Cart;
import projetowebsd.ecommerceback.model.CartItem;
import projetowebsd.ecommerceback.model.Product;
import projetowebsd.ecommerceback.model.User;
import projetowebsd.ecommerceback.model.enums.UserRole;
import projetowebsd.ecommerceback.repository.CartItemRepository;
import projetowebsd.ecommerceback.repository.CartRepository;
import projetowebsd.ecommerceback.repository.ProductRepository;
import projetowebsd.ecommerceback.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // Helper privado para pegar o usuário logado via Token JWT
    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email));
    }

    // Helper privado para garantir que o carrinho exista (se não existir, cria um na hora)
    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    @Transactional(readOnly = true)
    public CartResponseDTO getMyCart() {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);
        return convertToDTO(cart);
    }

    @Transactional
    public CartResponseDTO addItem(CartItemRequestDTO request) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Se o produto já estiver no carrinho, apenas atualiza somando a quantidade
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.productId()))
                .findFirst()
                .orElse(null);

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + request.quantity());
        } else {
            cartItem = new CartItem(cart, product, request.quantity());
            cart.getItems().add(cartItem);
        }

        cartRepository.save(cart);
        return convertToDTO(cart);
    }

    @Transactional
    public CartResponseDTO updateQuantity(UUID productId, int quantity) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item não encontrado no carrinho"));

        if (quantity <= 0) {
            cart.getItems().remove(cartItem);
        } else {
            cartItem.setQuantity(quantity);
        }

        cartRepository.save(cart);
        return convertToDTO(cart);
    }

    @Transactional
    public CartResponseDTO removeItem(UUID productId) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));

        cartRepository.save(cart);
        return convertToDTO(cart);
    }

    @Transactional
    public void clearMyCart() {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // 🔒 REGRA DO ADMIN: Método extra para o Admin buscar qualquer carrinho por ID de usuário
    @Transactional(readOnly = true)
    public CartResponseDTO getCartByUserIdForAdmin(UUID userId) {
        User currentUser = getAuthenticatedUser();

        // Validação explícita de segurança (Mesmo se a rota travar, o service garante)
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Acesso negado: Apenas administradores podem ver carrinhos alheios.");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário alvo não encontrado"));

        Cart cart = getOrCreateCart(targetUser);
        return convertToDTO(cart);
    }

    // Método auxiliar para transformar a Entidade em DTO e calcular os totais
    private CartResponseDTO convertToDTO(Cart cart) {
        List<CartItemResponseDTO> itemDTOs = cart.getItems().stream().map(item -> {
            BigDecimal unitPrice = item.getProduct().getPrice(); // Se seu preço for Double/Float
            BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            return new CartItemResponseDTO(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    unitPrice,
                    item.getQuantity(),
                    subTotal
            );
        }).collect(Collectors.toList());

        BigDecimal totalAmount = itemDTOs.stream()
                .map(CartItemResponseDTO::subTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponseDTO(cart.getId(), itemDTOs, totalAmount);
    }
}