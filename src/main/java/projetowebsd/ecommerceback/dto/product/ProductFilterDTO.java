package projetowebsd.ecommerceback.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Filtros para busca de produtos")
public record ProductFilterDTO(

        @Schema(description = "Busca por nome (parcial)", example = "cadeira")
        String name,

        @Schema(description = "Filtrar por categoria", example = "Móveis")
        String category,

        @Schema(description = "Preço mínimo", example = "100.00")
        BigDecimal minPrice,

        @Schema(description = "Preço máximo", example = "2000.00")
        BigDecimal maxPrice,

        @Schema(description = "Filtrar apenas produtos em promoção", example = "true")
        Boolean specialOffers
) {}