package projetowebsd.ecommerceback.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para criar uma avaliação")
public record ReviewRequestDTO(

        @Schema(description = "Nota de 1 a 5", example = "4")
        @NotNull(message = "Nota é obrigatória")
        @Min(value = 1, message = "Nota mínima: 1")
        @Max(value = 5, message = "Nota máxima: 5")
        Integer rating,

        @Schema(description = "Comentário sobre o produto")
        @NotBlank(message = "Comentário é obrigatório")
        String comment
) {}
