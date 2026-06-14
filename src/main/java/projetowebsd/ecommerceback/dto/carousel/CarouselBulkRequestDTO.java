package projetowebsd.ecommerceback.dto.carousel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CarouselBulkRequestDTO(
        String id, // Aceita String para permitir os "temp-uuid" do front

        @NotBlank(message = "O nome do carrossel é obrigatório")
        String name,

        @NotNull(message = "A posição é obrigatória")
        Integer position
) {}