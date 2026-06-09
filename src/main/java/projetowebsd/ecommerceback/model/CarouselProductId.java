package projetowebsd.ecommerceback.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class CarouselProductId implements Serializable {
    private UUID carousel;
    private UUID product;

    public CarouselProductId() {}
    public CarouselProductId(UUID carousel, UUID product) {
        this.carousel = carousel;
        this.product = product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarouselProductId that = (CarouselProductId) o;
        return Objects.equals(carousel, that.carousel) && Objects.equals(product, that.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(carousel, product);
    }
}