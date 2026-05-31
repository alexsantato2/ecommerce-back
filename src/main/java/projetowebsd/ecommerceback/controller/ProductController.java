package projetowebsd.ecommerceback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projetowebsd.ecommerceback.dto.product.ProductFilterDTO;
import projetowebsd.ecommerceback.dto.product.ProductRequestDTO;
import projetowebsd.ecommerceback.dto.product.ProductResponseDTO;
import projetowebsd.ecommerceback.dto.product.StockUpdateDTO;
import projetowebsd.ecommerceback.service.ProductService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Catálogo e gestão de produtos")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Listar produtos ativos com filtros e paginação")
    public ResponseEntity<Page<ProductResponseDTO>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        ProductFilterDTO filter = new ProductFilterDTO(name, category, minPrice, maxPrice);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(productService.listWithFilters(filter, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<ProductResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar produto", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar produto", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequestDTO request
    ) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Inativar produto", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ProductResponseDTO> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.deactivate(id));
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Ajustar estoque (delta positivo ou negativo)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ProductResponseDTO> updateStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockUpdateDTO request
    ) {
        return ResponseEntity.ok(productService.updateStock(id, request));
    }
}
