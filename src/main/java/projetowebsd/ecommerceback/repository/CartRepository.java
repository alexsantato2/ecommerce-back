package projetowebsd.ecommerceback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projetowebsd.ecommerceback.model.Cart;
import projetowebsd.ecommerceback.model.User;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    // Busca o carrinho atrelado ao usuário logado
    Optional<Cart> findByUser(User user);
    Optional<Cart> findByUserId(UUID userId);
}