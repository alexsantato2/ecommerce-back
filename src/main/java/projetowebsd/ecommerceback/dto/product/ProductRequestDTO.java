package projetowebsd.ecommerceback.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Dados para criar ou editar um produto")
public record ProductRequestDTO(

        @Schema(description = "Nome do produto", example = "Cadeira Escandinava")
        @NotBlank(message = "Nome é obrigatório")
        String name,

        @Schema(description = "Descrição detalhada", example = "Cadeira em madeira maciça...")
        String description,

        @Schema(description = "Categoria", example = "Móveis")
        String category,

        @Schema(description = "Preço", example = "1250.00")
        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
        BigDecimal price,

        @Schema(description = "Quantidade em estoque", example = "10")
        @NotNull(message = "Estoque é obrigatório")
        @Min(value = 0, message = "Estoque não pode ser negativo")
        Integer stockQuantity
) {}
