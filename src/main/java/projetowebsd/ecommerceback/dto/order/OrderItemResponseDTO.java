package projetowebsd.ecommerceback.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import projetowebsd.ecommerceback.model.OrderItem;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Item de um pedido")
public record OrderItemResponseDTO(

        @Schema(description = "ID do item")
        UUID id,

        @Schema(description = "ID do produto")
        UUID productId,

        @Schema(description = "Nome do produto")
        String productName,

        @Schema(description = "URL da imagem do produto")
        String productImageUrl,

        @Schema(description = "Quantidade")
        Integer quantity,

        @Schema(description = "Preço unitário no momento da compra")
        BigDecimal unitPrice
) {
    public static OrderItemResponseDTO from(OrderItem item) {
        return new OrderItemResponseDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getImageData() != null ? "/api/images/" + item.getProduct().getId() : null,
                item.getQuantity(),
                item.getUnitPrice()
        );
    }
}
