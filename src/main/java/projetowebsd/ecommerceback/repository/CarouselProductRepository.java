package projetowebsd.ecommerceback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projetowebsd.ecommerceback.model.CarouselProduct;
import projetowebsd.ecommerceback.model.CarouselProductId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarouselProductRepository extends JpaRepository<CarouselProduct, CarouselProductId> {
    Optional<CarouselProduct> findByCarouselIdAndProductId(UUID carouselId, UUID productId);
    List<CarouselProduct> findAllByCarouselIdOrderByPositionAsc(UUID carouselId);
    long countByCarouselId(UUID carouselId);
    void deleteByCarouselId(UUID carouselId);
}
