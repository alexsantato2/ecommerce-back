package projetowebsd.ecommerceback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projetowebsd.ecommerceback.exception.BusinessException;
import projetowebsd.ecommerceback.exception.ResourceNotFoundException;
import projetowebsd.ecommerceback.model.Product;
import projetowebsd.ecommerceback.repository.ProductRepository;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Imagens", description = "Upload e acesso a imagens de produtos")
public class ImageController {

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final ProductRepository productRepository;

    @CacheEvict(value = "products", allEntries = true)
    @PostMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Fazer upload de imagem para um produto (admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> upload(
            @PathVariable UUID productId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessException("Arquivo vazio");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException("Arquivo muito grande. Máximo permitido: 5 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("Tipo de arquivo não suportado. Use JPEG, PNG, WebP ou GIF");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + productId));

        product.setImageData(file.getBytes());
        product.setImageContentType(contentType);
        productRepository.save(product);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Obter imagem de um produto")
    public ResponseEntity<byte[]> getImage(@PathVariable UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + productId));

        if (product.getImageData() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(product.getImageContentType()));
        headers.setCacheControl("max-age=86400");

        return ResponseEntity.ok().headers(headers).body(product.getImageData());
    }
}
