package projetowebsd.ecommerceback.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Item do pedido")
public record OrderItemDTO(

        @Schema(description = "ID do produto")
        @NotNull(message = "ID do produto é obrigatório")
        UUID productId,

        @Schema(description = "Quantidade", example = "2")
        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
        Integer quantity
) {}
