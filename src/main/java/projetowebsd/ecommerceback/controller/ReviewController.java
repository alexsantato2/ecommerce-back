package projetowebsd.ecommerceback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import projetowebsd.ecommerceback.dto.review.ReviewRequestDTO;
import projetowebsd.ecommerceback.dto.review.ReviewResponseDTO;
import projetowebsd.ecommerceback.service.ReviewService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Avaliações", description = "Avaliações de produtos por clientes")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "Listar avaliações de um produto")
    public ResponseEntity<List<ReviewResponseDTO>> list(@PathVariable UUID productId) {
        return ResponseEntity.ok(reviewService.listByProduct(productId));
    }

    @PostMapping
    @Operation(summary = "Avaliar um produto (apenas clientes com pedido aprovado)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ReviewResponseDTO> create(
            @PathVariable UUID productId,
            @Valid @RequestBody ReviewRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.create(productId, request, userDetails.getUsername()));
    }
}
