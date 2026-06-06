package projetowebsd.ecommerceback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projetowebsd.ecommerceback.model.Review;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByProductIdOrderByCreatedAtDesc(UUID productId);

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    boolean existsByProductIdAndUserEmail(UUID productId, String userEmail);

    // 🆕 ADICIONE ESTA LINHA BEM AQUI:
    Optional<Review> findByUserIdAndProductId(UUID userId, UUID productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") UUID productId);

    long countByProductId(UUID productId);
}