package projetowebsd.ecommerceback.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import projetowebsd.ecommerceback.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Filtros para listagem de pedidos (admin)")
public record OrderFilterDTO(

        @Schema(description = "Filtrar por ID do usuário")
        UUID userId,

        @Schema(description = "Data inicial (yyyy-MM-dd)", example = "2024-01-01")
        LocalDate startDate,

        @Schema(description = "Data final (yyyy-MM-dd)", example = "2024-12-31")
        LocalDate endDate,

        @Schema(description = "Valor mínimo", example = "100.00")
        BigDecimal minAmount,

        @Schema(description = "Valor máximo", example = "5000.00")
        BigDecimal maxAmount,

        @Schema(description = "Status do pedido")
        OrderStatus status
) {}
