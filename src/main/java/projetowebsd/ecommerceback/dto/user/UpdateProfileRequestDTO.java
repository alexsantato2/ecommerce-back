package projetowebsd.ecommerceback.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para atualização do perfil")
public record UpdateProfileRequestDTO(

        @Schema(description = "Novo nome", example = "Maria Silva")
        @NotBlank(message = "Nome é obrigatório")
        String name
) {}
