package projetowebsd.ecommerceback.dto.carousel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para criar ou editar um carrossel")
public record CarouselRequestDTO(
        @Schema(description = "Nome do carrossel", example = "Destaques da Semana")
        @NotBlank(message = "Nome é obrigatório")
        String name
) {}