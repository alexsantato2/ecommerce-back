package projetowebsd.ecommerceback.dto.carousel;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Dados para mover de posição um carrossel ou produto")
public record MoveDTO(
        @Schema(description = "ID do item que está sendo movido")
        UUID id,
        @Schema(description = "Posição do elemento que ficou ANTES dele (null se virou o primeiro)")
        Integer positionBefore,
        @Schema(description = "Posição do elemento que ficou DEPOIS dele (null se virou o último)")
        Integer positionAfter
) {}