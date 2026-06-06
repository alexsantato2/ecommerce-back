package projetowebsd.ecommerceback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projetowebsd.ecommerceback.model.Order;
import projetowebsd.ecommerceback.model.User;
import projetowebsd.ecommerceback.model.enums.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    long countByStatus(OrderStatus status);

    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
            "WHERE oi.order.user.id = :userId " +
            "AND oi.product.id = :productId " +
            "AND oi.order.status = projetowebsd.ecommerceback.model.enums.OrderStatus.APPROVED")
    boolean existsApprovedOrderWithProduct(@Param("userId") UUID userId, @Param("productId") UUID productId);
}