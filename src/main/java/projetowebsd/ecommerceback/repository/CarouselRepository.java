package projetowebsd.ecommerceback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projetowebsd.ecommerceback.model.Carousel;
import java.util.List;
import java.util.UUID;

public interface CarouselRepository extends JpaRepository<Carousel, UUID> {
    List<Carousel> findAllByOrderByPositionAsc();

    @Modifying
    @Query(value = "INSERT INTO carousels (id, name, position) VALUES (:id, :name, :position)", nativeQuery = true)
    void insertCustom(@Param("id") UUID id, @Param("name") String name, @Param("position") Integer position);
}