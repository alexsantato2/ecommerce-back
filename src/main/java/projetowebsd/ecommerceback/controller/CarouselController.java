package projetowebsd.ecommerceback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projetowebsd.ecommerceback.dto.carousel.*;
import projetowebsd.ecommerceback.service.CarouselService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/carousels")
@RequiredArgsConstructor
@Tag(name = "Carrosséis", description = "Gestão, vinculação e ordenação dos carrosséis da Home")
public class CarouselController {

    private final CarouselService carouselService;

    @GetMapping
    @Operation(summary = "Listar carrosséis com produtos ordenados")
    public ResponseEntity<List<CarouselResponseDTO>> list() {
        return ResponseEntity.ok(carouselService.listAll());
    }

    @PostMapping
    @Operation(summary = "Criar um novo carrossel", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CarouselResponseDTO> create(@Valid @RequestBody CarouselRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carouselService.create(request));
    }

    @PostMapping("/{id}/products/{productId}")
    @Operation(summary = "Adicionar produto ao carrossel", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> addProduct(@PathVariable UUID id, @PathVariable UUID productId) {
        carouselService.addProduct(id, productId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/move")
    @Operation(summary = "Mudar posição global do carrossel na Home", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> moveCarousel(@Valid @RequestBody MoveDTO request) {
        carouselService.moveCarousel(request.id(), request.targetPosition());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/products/move")
    @Operation(summary = "Mudar a posição de um produto dentro do carrossel", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> moveProductInCarousel(@PathVariable UUID id, @Valid @RequestBody MoveDTO request) {
        carouselService.moveProductInCarousel(id, request.id(), request.targetPosition());
        return ResponseEntity.noContent().build();
    }
}