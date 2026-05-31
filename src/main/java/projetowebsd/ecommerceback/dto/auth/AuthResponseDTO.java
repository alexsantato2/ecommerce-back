package projetowebsd.ecommerceback.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resposta de autenticação com tokens JWT")
public record AuthResponseDTO(

        @Schema(description = "Access token JWT (curta duração)")
        String token,

        @Schema(description = "Refresh token (longa duração)")
        String refreshToken,

        @Schema(description = "ID do usuário")
        UUID userId,

        @Schema(description = "Nome do usuário")
        String name,

        @Schema(description = "Papel do usuário", example = "CUSTOMER")
        String role
) {}
