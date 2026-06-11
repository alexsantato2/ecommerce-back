package projetowebsd.ecommerceback.dto.carousel;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Dados para mover de posição um carrossel ou produto")
public record MoveDTO(
        @Schema(description = "ID do item que está sendo movido")
        UUID id,

        @Schema(description = "Nova posição desejada para o item (baseada em índice 0)", example = "2")
        Integer targetPosition
) {}