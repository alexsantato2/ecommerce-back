package projetowebsd.ecommerceback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projetowebsd.ecommerceback.dto.order.OrderFilterDTO;
import projetowebsd.ecommerceback.dto.order.OrderRequestDTO;
import projetowebsd.ecommerceback.dto.order.OrderResponseDTO;
import projetowebsd.ecommerceback.model.enums.OrderStatus;
import projetowebsd.ecommerceback.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Criação e gestão de pedidos")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Finalizar compra")
    public ResponseEntity<OrderResponseDTO> placeOrder(@Valid @RequestBody OrderRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(request));
    }

    @GetMapping("/my")
    @Operation(summary = "Histórico de pedidos do usuário autenticado")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    @GetMapping
    @Operation(summary = "Listar todos os pedidos com filtros (admin)")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) OrderStatus status
    ) {
        OrderFilterDTO filter = new OrderFilterDTO(userId, startDate, endDate, minAmount, maxAmount, status);
        return ResponseEntity.ok(orderService.getAllOrders(filter));
    }

    @GetMapping("/pending-count")
    @Operation(summary = "Contar pedidos pendentes (admin)")
    public ResponseEntity<java.util.Map<String, Long>> pendingCount() {
        return ResponseEntity.ok(java.util.Map.of("count", orderService.countPending()));
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Aprovar pedido pendente (admin)")
    public ResponseEntity<OrderResponseDTO> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.approve(id));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Rejeitar pedido pendente e restaurar estoque (admin)")
    public ResponseEntity<OrderResponseDTO> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.reject(id));
    }
}
