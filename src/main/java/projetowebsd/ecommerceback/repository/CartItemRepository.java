package projetowebsd.ecommerceback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projetowebsd.ecommerceback.model.CartItem;

import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    // Caso precise deletar um item específico diretamente pelo produto e carrinho
    void deleteByCartIdAndProductId(UUID cartId, UUID productId);
}