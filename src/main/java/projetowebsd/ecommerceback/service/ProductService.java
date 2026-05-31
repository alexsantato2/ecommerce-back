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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    @Cacheable(value = "products", key = "#filter.toString() + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductResponseDTO> listWithFilters(ProductFilterDTO filter, Pageable pageable) {
        Specification<Product> spec = buildSpec(filter);
        return productRepository.findAll(spec, pageable)
                .map(p -> ProductResponseDTO.from(p, reviewRepository));
    }

    @Cacheable(value = "products", key = "'id-' + #id")
    public ProductResponseDTO findById(UUID id) {
        return ProductResponseDTO.from(getProduct(id), reviewRepository);
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
