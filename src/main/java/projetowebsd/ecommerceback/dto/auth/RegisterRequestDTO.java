package projetowebsd.ecommerceback.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criação de conta")
public record RegisterRequestDTO(

        @Schema(description = "Nome completo", example = "João Silva")
        @NotBlank(message = "Nome é obrigatório")
        String name,

        @Schema(description = "E-mail", example = "joao@email.com")
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @Schema(description = "Senha (mínimo 6 caracteres)", example = "senha123")
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres")
        String password
) {}
