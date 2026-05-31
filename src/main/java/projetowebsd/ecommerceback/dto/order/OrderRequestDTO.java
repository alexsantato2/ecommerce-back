package projetowebsd.ecommerceback.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Dados para finalizar uma compra")
public record OrderRequestDTO(

        @Schema(description = "Itens do carrinho")
        @NotEmpty(message = "O pedido deve conter ao menos um item")
        @Valid
        List<OrderItemDTO> items,

        @Schema(description = "CEP de entrega", example = "01310-100")
        @NotBlank(message = "CEP é obrigatório")
        String shippingCep,

        @Schema(description = "Logradouro", example = "Av. Paulista")
        @NotBlank(message = "Logradouro é obrigatório")
        String shippingStreet,

        @Schema(description = "Bairro", example = "Bela Vista")
        String shippingNeighborhood,

        @Schema(description = "Cidade", example = "São Paulo")
        @NotBlank(message = "Cidade é obrigatória")
        String shippingCity,

        @Schema(description = "Estado", example = "SP")
        @NotBlank(message = "Estado é obrigatório")
        String shippingState,

        @Schema(description = "Número", example = "1578")
        String shippingNumber,

        @Schema(description = "Complemento", example = "Apto 42")
        String shippingComplement
) {}
