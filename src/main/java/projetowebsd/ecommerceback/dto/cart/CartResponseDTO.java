package projetowebsd.ecommerceback.dto.cart;

public record CartResponseDTO(
        java.util.UUID id,
        java.util.List<CartItemResponseDTO> items,
        java.math.BigDecimal totalAmount
) {}