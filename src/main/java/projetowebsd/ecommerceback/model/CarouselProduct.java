package projetowebsd.ecommerceback.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "carousel_products")
@IdClass(CarouselProductId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarouselProduct {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carousel_id", nullable = false)
    private Carousel carousel;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer position; // Posição do produto dentro DESTE carrossel
}