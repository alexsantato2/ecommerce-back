package projetowebsd.ecommerceback.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import projetowebsd.ecommerceback.model.Product;
import projetowebsd.ecommerceback.repository.ReviewRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados de um produto")
public record ProductResponseDTO(

        @Schema(description = "ID do produto")
        UUID id,

        @Schema(description = "Nome")
        String name,

        @Schema(description = "Descrição")
        String description,

        @Schema(description = "Categoria")
        String category,

        @Schema(description = "Preço")
        BigDecimal price,

        @Schema(description = "Quantidade em estoque")
        Integer stockQuantity,

        @Schema(description = "URL da imagem (endpoint /api/images/{id}), null se sem imagem")
        String imageUrl,

        @Schema(description = "Produto ativo")
        Boolean active,

        @Schema(description = "Média das avaliações (1–5)")
        Double averageRating,

        @Schema(description = "Número de avaliações")
        long reviewCount,

        @Schema(description = "Data de criação")
        LocalDateTime createdAt
) {
    public static ProductResponseDTO from(Product product, ReviewRepository reviewRepository) {
        String imageUrl = product.getImageData() != null
                ? "/api/images/" + product.getId()
                : null;

        Double avg = reviewRepository.findAverageRatingByProductId(product.getId());
        long count = reviewRepository.countByProductId(product.getId());

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getStockQuantity(),
                imageUrl,
                product.getActive(),
                avg != null ? Math.round(avg * 10.0) / 10.0 : null,
                count,
                product.getCreatedAt()
        );
    }
}
