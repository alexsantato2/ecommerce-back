package projetowebsd.ecommerceback.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projetowebsd.ecommerceback.dto.product.ProductFilterDTO;
import projetowebsd.ecommerceback.dto.product.ProductRequestDTO;
import projetowebsd.ecommerceback.dto.product.ProductResponseDTO;
import projetowebsd.ecommerceback.dto.product.StockUpdateDTO;
import projetowebsd.ecommerceback.exception.BusinessException;
import projetowebsd.ecommerceback.exception.ResourceNotFoundException;
import projetowebsd.ecommerceback.model.Product;
import projetowebsd.ecommerceback.repository.ProductRepository;
import projetowebsd.ecommerceback.repository.ReviewRepository;
import org.springframework.data.domain.Sort;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    // Atualize a assinatura para aceitar as strings de ordenação
    @Cacheable(value = "products", key = "#filter.toString() + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #sortProperty + '-' + #sortDirection")
    public Page<ProductResponseDTO> listWithFilters(ProductFilterDTO filter, Pageable pageable, String sortProperty, Sort.Direction sortDirection) {
        Specification<Product> spec = buildSpec(filter, sortProperty, sortDirection);
        return productRepository.findAll(spec, pageable)
                .map(p -> ProductResponseDTO.from(p, reviewRepository));
    }

    @Cacheable(value = "products", key = "'id-' + #id")
    public ProductResponseDTO findById(UUID id) {
        return ProductResponseDTO.from(getProduct(id), reviewRepository);
    }

    private Specification<Product> buildSpec(ProductFilterDTO filter, String sortProperty, Sort.Direction sortDirection) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("active"), true));

            // --- Filtros convencionais ---
            if (filter.name() != null && !filter.name().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.name().toLowerCase() + "%"));
            }
            if (filter.category() != null && !filter.category().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), filter.category().toLowerCase()));
            }
            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
            }
            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }

            // --- Lógica de Ordenação Avançada ---
            if (sortProperty.equals("salesCount")) {
                configureSalesSort(root, query, cb, sortDirection);
            }
            else if (sortProperty.equals("rating")) {
                configureRatingSort(root, query, cb, sortDirection);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void configureSalesSort(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb, Sort.Direction direction) {
        var subquery = query.subquery(Long.class);
        var orderItemRoot = subquery.from(projetowebsd.ecommerceback.model.OrderItem.class);

        subquery.select(cb.coalesce(cb.sum(orderItemRoot.get("quantity")), 0L));
        subquery.where(cb.equal(orderItemRoot.get("product"), root));

        if (direction == Sort.Direction.DESC) {
            query.orderBy(cb.desc(subquery));
        } else {
            query.orderBy(cb.asc(subquery));
        }
    }

    // Método auxiliar para ordenar pela média das avaliações recebidas
    private void configureRatingSort(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb, Sort.Direction direction) {
        // Assume-se que a classe mapeada para a tabela reviews chama-se Review
        var subquery = query.subquery(Double.class);
        var reviewRoot = subquery.from(projetowebsd.ecommerceback.model.Review.class); // Altere para o seu pacote correto da classe

        subquery.select(cb.coalesce(cb.avg(reviewRoot.get("rating")), 0.0));
        subquery.where(cb.equal(reviewRoot.get("product"), root)); // Considerando relacionamento @ManyToOne no Review

        if (direction == Sort.Direction.DESC) {
            query.orderBy(cb.desc(subquery));
        } else {
            query.orderBy(cb.asc(subquery));
        }
    }
    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductResponseDTO create(ProductRequestDTO request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .active(true)
                .build();
        return ProductResponseDTO.from(productRepository.save(product), reviewRepository);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductResponseDTO update(UUID id, ProductRequestDTO request) {
        Product product = getProduct(id);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        return ProductResponseDTO.from(productRepository.save(product), reviewRepository);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductResponseDTO deactivate(UUID id) {
        Product product = getProduct(id);
        product.setActive(false);
        return ProductResponseDTO.from(productRepository.save(product), reviewRepository);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductResponseDTO updateStock(UUID id, StockUpdateDTO request) {
        Product product = getProduct(id);
        int newStock = product.getStockQuantity() + request.delta();
        if (newStock < 0) {
            throw new BusinessException("Estoque insuficiente para este ajuste");
        }
        product.setStockQuantity(newStock);
        return ProductResponseDTO.from(productRepository.save(product), reviewRepository);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void evictCache() {
    }

    public Product getProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));
    }

    private Specification<Product> buildSpec(ProductFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("active"), true));

            if (filter.name() != null && !filter.name().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + filter.name().toLowerCase() + "%"));
            }
            if (filter.category() != null && !filter.category().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")),
                        filter.category().toLowerCase()));
            }
            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
            }
            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }

            // Exemplo para a regra de Promoções / Descontos (se aplicável ao seu banco)
            if (filter.specialOffers() != null && filter.specialOffers()) {
                // Filtra produtos onde o preço promocional não é nulo ou há desconto ativo
                predicates.add(cb.isNotNull(root.get("promoPrice")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
