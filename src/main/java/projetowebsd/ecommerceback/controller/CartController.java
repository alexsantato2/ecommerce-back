package projetowebsd.ecommerceback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projetowebsd.ecommerceback.dto.cart.CartItemRequestDTO;
import projetowebsd.ecommerceback.dto.cart.CartResponseDTO;
import projetowebsd.ecommerceback.service.CartService;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Carrinho", description = "Gestão de itens no carrinho de compras")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Obter o carrinho do usuário autenticado")
    public ResponseEntity<CartResponseDTO> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    @PostMapping("/items")
    @Operation(summary = "Adicionar ou atualizar quantidade de um item no carrinho")
    public ResponseEntity<CartResponseDTO> addItemToCart(@Valid @RequestBody CartItemRequestDTO request) {
        return ResponseEntity.ok(cartService.addItem(request));
    }

    @PatchMapping("/items/{productId}")
    @Operation(summary = "Atualizar diretamente a quantidade de um produto específico")
    public ResponseEntity<CartResponseDTO> updateItemQuantity(
            @PathVariable UUID productId,
            @RequestParam int quantity
    ) {
        return ResponseEntity.ok(cartService.updateQuantity(productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remover um produto do carrinho")
    public ResponseEntity<CartResponseDTO> removeItemFromCart(@PathVariable UUID productId) {
        return ResponseEntity.ok(cartService.removeItem(productId));
    }

    @DeleteMapping
    @Operation(summary = "Esvaziar o carrinho por completo")
    public ResponseEntity<Void> clearCart() {
        cartService.clearMyCart();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obter o carrinho de qualquer usuário (Apenas Admin)")
    public ResponseEntity<CartResponseDTO> getCartByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(cartService.getCartByUserIdForAdmin(userId));
    }
}