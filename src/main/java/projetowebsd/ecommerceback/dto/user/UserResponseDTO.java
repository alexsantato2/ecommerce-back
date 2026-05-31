package projetowebsd.ecommerceback.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import projetowebsd.ecommerceback.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados públicos de um usuário")
public record UserResponseDTO(

        @Schema(description = "ID do usuário")
        UUID id,

        @Schema(description = "Nome")
        String name,

        @Schema(description = "E-mail")
        String email,

        @Schema(description = "Papel", example = "CUSTOMER")
        String role,

        @Schema(description = "Data de cadastro")
        LocalDateTime createdAt
) {
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
