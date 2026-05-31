package projetowebsd.ecommerceback.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição para renovar o access token")
public record RefreshRequestDTO(

        @Schema(description = "Refresh token emitido no login")
        @NotBlank(message = "Refresh token é obrigatório")
        String refreshToken
) {}
