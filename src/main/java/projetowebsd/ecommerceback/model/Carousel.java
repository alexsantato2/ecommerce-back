package projetowebsd.ecommerceback.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carousels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carousel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer position; // Posição do carrossel na Home

    @OneToMany(mappedBy = "carousel", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @ToString.Exclude
    private List<CarouselProduct> carouselProducts;
}