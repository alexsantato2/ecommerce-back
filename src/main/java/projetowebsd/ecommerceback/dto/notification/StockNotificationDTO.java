package projetowebsd.ecommerceback.dto.notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockNotificationDTO(
        UUID productId,
        String productName,
        Integer newStock,
        LocalDateTime timestamp
) {}
