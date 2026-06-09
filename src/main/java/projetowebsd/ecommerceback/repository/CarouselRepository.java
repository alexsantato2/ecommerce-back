package projetowebsd.ecommerceback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projetowebsd.ecommerceback.model.Carousel;
import java.util.List;
import java.util.UUID;

public interface CarouselRepository extends JpaRepository<Carousel, UUID> {
    List<Carousel> findAllByOrderByPositionAsc();
}