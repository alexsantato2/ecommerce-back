package projetowebsd.ecommerceback.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import projetowebsd.ecommerceback.model.Review;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados de uma avaliação")
public record ReviewResponseDTO(

        @Schema(description = "ID da avaliação")
        UUID id,

        @Schema(description = "ID do usuário")
        UUID userId,

        @Schema(description = "Nome do usuário")
        String userName,

        @Schema(description = "Nota de 1 a 5")
        Integer rating,

        @Schema(description = "Comentário")
        String comment,

        @Schema(description = "Data de criação")
        LocalDateTime createdAt
) {
    public static ReviewResponseDTO from(Review review) {
        return new ReviewResponseDTO(
                review.getId(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
