package projetowebsd.ecommerceback.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CartItemRequestDTO(
        @NotNull(message = "O ID do produto é obrigatório") UUID productId,
        @Min(value = 1, message = "A quantidade deve ser de no mínimo 1") int quantity
) {}