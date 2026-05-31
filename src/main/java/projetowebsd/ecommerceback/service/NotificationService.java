package projetowebsd.ecommerceback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import projetowebsd.ecommerceback.dto.notification.OrderNotificationDTO;
import projetowebsd.ecommerceback.dto.notification.StockNotificationDTO;
import projetowebsd.ecommerceback.model.Order;
import projetowebsd.ecommerceback.model.Product;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyOrderStatusChange(Order order) {
        String message = switch (order.getStatus()) {
            case PENDING -> "Pedido recebido e aguardando aprovação";
            case APPROVED -> "Seu pedido foi aprovado!";
            case REJECTED -> "Seu pedido foi rejeitado";
        };

        OrderNotificationDTO notification = new OrderNotificationDTO(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getName(),
                order.getStatus(),
                message,
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/topic/orders/" + order.getUser().getId(), notification);
    }

    public void notifyAdminNewOrder(Order order) {
        OrderNotificationDTO notification = new OrderNotificationDTO(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getName(),
                order.getStatus(),
                "Novo pedido de " + order.getUser().getName(),
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/topic/admin/orders", notification);
    }

    public void notifyStockChange(Product product) {
        StockNotificationDTO notification = new StockNotificationDTO(
                product.getId(),
                product.getName(),
                product.getStockQuantity(),
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/topic/stock", notification);
    }
}
