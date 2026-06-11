package projetowebsd.ecommerceback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projetowebsd.ecommerceback.dto.carousel.*;
import projetowebsd.ecommerceback.exception.ResourceNotFoundException;
import projetowebsd.ecommerceback.model.*;
import projetowebsd.ecommerceback.repository.*;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarouselService {

    private final CarouselRepository carouselRepository;
    private final CarouselProductRepository carouselProductRepository;
    private final ProductService productService;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<CarouselResponseDTO> listAll() {
        return carouselRepository.findAllByOrderByPositionAsc().stream()
                .map(c -> CarouselResponseDTO.from(c, reviewRepository))
                .toList();
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public CarouselResponseDTO create(CarouselRequestDTO request) {
        long count = carouselRepository.count();

        Carousel carousel = Carousel.builder()
                .name(request.name())
                .position((int) count)
                .build();

        return CarouselResponseDTO.from(carouselRepository.save(carousel), reviewRepository);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void addProduct(UUID carouselId, UUID productId) {
        Carousel carousel = carouselRepository.findById(carouselId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrossel não encontrado"));
        Product product = productService.getProduct(productId);

        long count = carouselProductRepository.countByCarouselId(carouselId);

        CarouselProduct relation = CarouselProduct.builder()
                .carousel(carousel)
                .product(product)
                .position((int) count)
                .build();

        carouselProductRepository.save(relation);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void moveCarousel(UUID carouselId, int targetPosition) {
        Carousel carouselToMove = carouselRepository.findById(carouselId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrossel não encontrado"));

        int currentPosition = carouselToMove.getPosition();
        if (currentPosition == targetPosition) return;

        List<Carousel> carousels = carouselRepository.findAllByOrderByPositionAsc();

        // Garante que o targetPosition não estoure os limites da lista
        targetPosition = Math.max(0, Math.min(targetPosition, carousels.size() - 1));

        // Permuta e desloca os carrosséis no caminho
        if (currentPosition < targetPosition) {
            // Movendo para baixo/frente: empurra quem está no caminho para trás (subtrai 1)
            for (Carousel c : carousels) {
                if (c.getPosition() > currentPosition && c.getPosition() <= targetPosition) {
                    c.setPosition(c.getPosition() - 1);
                }
            }
        } else {
            // Movendo para cima/trás: empurra quem está no caminho para frente (soma 1)
            for (Carousel c : carousels) {
                if (c.getPosition() >= targetPosition && c.getPosition() < currentPosition) {
                    c.setPosition(c.getPosition() + 1);
                }
            }
        }

        carouselToMove.setPosition(targetPosition);
        carouselRepository.saveAll(carousels); // Salva todo mundo atualizado
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void moveProductInCarousel(UUID carouselId, UUID productId, int targetPosition) {
        CarouselProduct relationToMove = carouselProductRepository.findByCarouselIdAndProductId(carouselId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não está no carrossel"));

        int currentPosition = relationToMove.getPosition();
        if (currentPosition == targetPosition) return;

        List<CarouselProduct> currentItems = carouselProductRepository.findAllByCarouselIdOrderByPositionAsc(carouselId);

        // Garante que o targetPosition respeite o tamanho real de itens no carrossel
        targetPosition = Math.max(0, Math.min(targetPosition, currentItems.size() - 1));

        // Permuta e desloca as relações no caminho
        if (currentPosition < targetPosition) {
            // Movendo para baixo/frente: reduz a posição dos intermediários
            for (CarouselProduct cp : currentItems) {
                if (cp.getPosition() > currentPosition && cp.getPosition() <= targetPosition) {
                    cp.setPosition(cp.getPosition() - 1);
                }
            }
        } else {
            // Movendo para cima/trás: aumenta a posição dos intermediários
            for (CarouselProduct cp : currentItems) {
                if (cp.getPosition() >= targetPosition && cp.getPosition() < currentPosition) {
                    cp.setPosition(cp.getPosition() + 1);
                }
            }
        }

        relationToMove.setPosition(targetPosition);
        carouselProductRepository.saveAll(currentItems);
    }
}