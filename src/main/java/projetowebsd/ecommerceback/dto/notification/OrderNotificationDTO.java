package projetowebsd.ecommerceback.dto.notification;

import projetowebsd.ecommerceback.model.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderNotificationDTO(
        UUID orderId,
        UUID userId,
        String userName,
        OrderStatus status,
        String message,
        LocalDateTime timestamp
) {}
