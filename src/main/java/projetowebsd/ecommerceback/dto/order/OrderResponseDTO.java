package projetowebsd.ecommerceback.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import projetowebsd.ecommerceback.model.Order;
import projetowebsd.ecommerceback.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Dados de um pedido")
public record OrderResponseDTO(

        @Schema(description = "ID do pedido")
        UUID id,

        @Schema(description = "ID do usuário")
        UUID userId,

        @Schema(description = "Nome do usuário")
        String userName,

        @Schema(description = "Status do pedido")
        OrderStatus status,

        @Schema(description = "Valor total")
        BigDecimal totalAmount,

        @Schema(description = "CEP de entrega")
        String shippingCep,

        @Schema(description = "Logradouro")
        String shippingStreet,

        @Schema(description = "Bairro")
        String shippingNeighborhood,

        @Schema(description = "Cidade")
        String shippingCity,

        @Schema(description = "Estado")
        String shippingState,

        @Schema(description = "Número")
        String shippingNumber,

        @Schema(description = "Complemento")
        String shippingComplement,

        @Schema(description = "Itens do pedido")
        List<OrderItemResponseDTO> items,

        @Schema(description = "Data de criação")
        LocalDateTime createdAt
) {
    public static OrderResponseDTO from(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getName(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingCep(),
                order.getShippingStreet(),
                order.getShippingNeighborhood(),
                order.getShippingCity(),
                order.getShippingState(),
                order.getShippingNumber(),
                order.getShippingComplement(),
                order.getItems().stream().map(OrderItemResponseDTO::from).toList(),
                order.getCreatedAt()
        );
    }
}
