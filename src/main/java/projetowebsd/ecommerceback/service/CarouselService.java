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

    private static final int GAP = 1000;

    @Transactional(readOnly = true)
    public List<CarouselResponseDTO> listAll() {
        return carouselRepository.findAllByOrderByPositionAsc().stream()
                .map(c -> CarouselResponseDTO.from(c, reviewRepository))
                .toList();
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public CarouselResponseDTO create(CarouselRequestDTO request) {
        // Pega o último carrossel para colocar o novo no fim da fila
        List<Carousel> existing = carouselRepository.findAllByOrderByPositionAsc();
        int nextPos = existing.isEmpty() ? GAP : existing.get(existing.size() - 1).getPosition() + GAP;

        Carousel carousel = Carousel.builder()
                .name(request.name())
                .position(nextPos)
                .build();

        return CarouselResponseDTO.from(carouselRepository.save(carousel), reviewRepository);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void addProduct(UUID carouselId, UUID productId) {
        Carousel carousel = carouselRepository.findById(carouselId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrossel não encontrado"));
        Product product = productService.getProduct(productId);

        List<CarouselProduct> currentItems = carouselProductRepository.findAllByCarouselIdOrderByPositionAsc(carouselId);
        int nextPos = currentItems.isEmpty() ? GAP : currentItems.get(currentItems.size() - 1).getPosition() + GAP;

        CarouselProduct relation = CarouselProduct.builder()
                .carousel(carousel)
                .product(product)
                .position(nextPos)
                .build();

        carouselProductRepository.save(relation);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void moveCarousel(MoveDTO move) {
        Carousel carousel = carouselRepository.findById(move.id())
                .orElseThrow(() -> new ResourceNotFoundException("Carrossel não encontrado"));

        int newPos = calculateNewPosition(move.positionBefore(), move.positionAfter());

        if (newPos == -1) { // Deu colisão absoluta de inteiros
            rebalanceCarousels();
            moveCarousel(move); // Tenta novamente após reordenar tudo
            return;
        }

        carousel.setPosition(newPos);
        carouselRepository.save(carousel);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void moveProductInCarousel(UUID carouselId, MoveDTO move) {
        CarouselProduct relation = carouselProductRepository.findByCarouselIdAndProductId(carouselId, move.id())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não está no carrossel"));

        int newPos = calculateNewPosition(move.positionBefore(), move.positionAfter());

        if (newPos == -1) {
            rebalanceProducts(carouselId);
            moveProductInCarousel(carouselId, move);
            return;
        }

        relation.setPosition(newPos);
        carouselProductRepository.save(relation);
    }

    private int calculateNewPosition(Integer before, Integer after) {
        if (before == null && after == null) return GAP;
        if (before == null) return after / 2;
        if (after == null) return before + GAP;

        int middle = (before + after) / 2;
        if (middle == before || middle == after) {
            return -1; // Sinaliza colisão
        }
        return middle;
    }

    private void rebalanceCarousels() {
        List<Carousel> carousels = carouselRepository.findAllByOrderByPositionAsc();
        int pos = GAP;
        for (Carousel c : carousels) {
            c.setPosition(pos);
            pos += GAP;
        }
        carouselRepository.saveAll(carousels);
    }

    private void rebalanceProducts(UUID carouselId) {
        List<CarouselProduct> relations = carouselProductRepository.findAllByCarouselIdOrderByPositionAsc(carouselId);
        int pos = GAP;
        for (CarouselProduct cp : relations) {
            cp.setPosition(pos);
            pos += GAP;
        }
        carouselProductRepository.saveAll(relations);
    }
}