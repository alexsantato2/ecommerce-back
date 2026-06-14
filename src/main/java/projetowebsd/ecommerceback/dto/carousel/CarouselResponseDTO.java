package projetowebsd.ecommerceback.dto.carousel;

import io.swagger.v3.oas.annotations.media.Schema;
import projetowebsd.ecommerceback.dto.product.ProductResponseDTO;
import projetowebsd.ecommerceback.model.Carousel;
import projetowebsd.ecommerceback.repository.ReviewRepository;

import java.util.List;
import java.util.UUID;

@Schema(description = "Dados de retorno do Carrossel com seus produtos ordenados")
public record CarouselResponseDTO(
        UUID id,
        String name,
        Integer position,
        List<ProductResponseDTO> products
) {
    public static CarouselResponseDTO from(Carousel carousel, ReviewRepository reviewRepository) {
        // Proteção contra NullPointerException se o carrossel não tiver produtos ainda
        List<ProductResponseDTO> orderedProducts = List.of();

        if (carousel.getCarouselProducts() != null) {
            orderedProducts = carousel.getCarouselProducts().stream()
                    .map(cp -> ProductResponseDTO.from(cp.getProduct(), reviewRepository))
                    .toList();
        }

        return new CarouselResponseDTO(
                carousel.getId(),
                carousel.getName(),
                carousel.getPosition(),
                orderedProducts
        );
    }
}