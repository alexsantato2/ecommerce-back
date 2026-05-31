package projetowebsd.ecommerceback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projetowebsd.ecommerceback.model.OrderItem;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}
