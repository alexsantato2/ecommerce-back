package projetowebsd.ecommerceback.dto.cart;

public record CartItemResponseDTO(
        java.util.UUID productId,
        String productName,
        java.math.BigDecimal unitPrice,
        int quantity,
        java.math.BigDecimal subTotal
) {}