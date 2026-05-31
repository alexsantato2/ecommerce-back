package projetowebsd.ecommerceback.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Ajuste de estoque. Positivo incrementa, negativo decrementa.")
public record StockUpdateDTO(

        @Schema(description = "Quantidade a ajustar (positivo ou negativo)", example = "5")
        @NotNull(message = "Delta é obrigatório")
        Integer delta
) {}
