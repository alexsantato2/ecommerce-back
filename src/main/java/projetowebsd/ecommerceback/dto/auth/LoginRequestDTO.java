package projetowebsd.ecommerceback.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de acesso")
public record LoginRequestDTO(

        @Schema(description = "E-mail cadastrado", example = "joao@email.com")
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @Schema(description = "Senha", example = "senha123")
        @NotBlank(message = "Senha é obrigatória")
        String password
) {}
