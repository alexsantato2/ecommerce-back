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
//import projetowebsd.ecommerceback.model.Carousel;
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover um carrossel permanentemente", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        System.out.println("[DELETE] Removendo o carrossel ID: " + id);
        carouselService.delete(id);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content (padrão para deleção bem-sucedida sem corpo)
    }

    @PostMapping("/{id}/products/{productId}")
    @Operation(summary = "Adicionar produto ao carrossel", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> addProduct(@PathVariable UUID id, @PathVariable UUID productId) {
        carouselService.addProduct(id, productId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PatchMapping("/{id}/products/move")
    @Operation(summary = "Mudar a posição de um produto dentro do carrossel", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> moveProductInCarousel(@PathVariable UUID id, @Valid @RequestBody MoveDTO request) {
        carouselService.moveProductInCarousel(id, request.id(), request.targetPosition());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/products/bulk")
    @Operation(
            summary = "Atualizar lista completa de produtos de um carrossel em lote",
            description = "Substitui todos os produtos de um carrossel pela lista fornecida, respeitando a ordem do array.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> bulkUpdateCarouselProducts(
            @PathVariable UUID id,
            @Valid @RequestBody List<UUID> productIds) {

        System.out.println("[BULK] Atualizando produtos do carrossel " + id + ". Total de itens: " + productIds.size());

        carouselService.updateCarouselProducts(id, productIds);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // Você pode remover o antigo /batch-positions se quiser limpar o código morto

    @PutMapping("/bulk")
    @Operation(summary = "Salvar e reordenar carrosséis em lote (Bulk)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<CarouselResponseDTO>> bulkSaveCarousels(
            @Valid @RequestBody List<CarouselBulkRequestDTO> request) {

        System.out.println("[BULK] Recebida carga total de carrosséis para salvar. Itens: " + request.size());

        // O service processa os DTOs e devolve a lista atualizada mapeada para ResponseDTO
        List<CarouselResponseDTO> saved = carouselService.bulkSave(request);

        System.out.println("[BULK] Estado em lote persistido com sucesso!");
        return ResponseEntity.ok(saved);
    }


}