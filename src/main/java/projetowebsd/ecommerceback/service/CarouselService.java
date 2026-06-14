package projetowebsd.ecommerceback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projetowebsd.ecommerceback.dto.carousel.*;
import projetowebsd.ecommerceback.exception.ResourceNotFoundException;
import projetowebsd.ecommerceback.model.*;
import projetowebsd.ecommerceback.repository.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Transactional
    public void updateCarouselProducts(UUID carouselId, List<UUID> productIds) {
        Carousel carousel = carouselRepository.findById(carouselId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrossel não encontrado"));

        // 1. Pega tudo que já existe no banco
        List<CarouselProduct> currentItems = carouselProductRepository.findAllByCarouselIdOrderByPositionAsc(carouselId);

        // 2. Transforma em um mapa para busca rápida (ID do produto -> Objeto existente)
        Map<UUID, CarouselProduct> existingMap = currentItems.stream()
                .collect(Collectors.toMap(cp -> cp.getProduct().getId(), cp -> cp));

        // 3. Identifica o que deve ser mantido ou atualizado
        // Aqui comparamos a lista nova (productIds) com o que temos no banco (existingMap)
        for (int i = 0; i < productIds.size(); i++) {
            UUID productId = productIds.get(i);

            if (existingMap.containsKey(productId)) {
                // O produto JÁ EXISTE no carrossel.
                CarouselProduct existing = existingMap.get(productId);
                // Só atualiza a posição se ela mudou
                if (existing.getPosition() != i) {
                    existing.setPosition(i);
                    carouselProductRepository.save(existing);
                }
                // Remove do mapa para saber que ele foi processado
                existingMap.remove(productId);
            } else {
                // É um produto NOVO, precisa inserir
                Product product = productService.getProduct(productId);
                CarouselProduct newRelation = new CarouselProduct();
                newRelation.setCarousel(carousel);
                newRelation.setProduct(product);
                newRelation.setPosition(i);
                carouselProductRepository.save(newRelation);
            }
        }

        // 4. O que sobrou no existingMap são os produtos que NÃO vieram na lista nova.
        // Ou seja, precisam ser DELETADOS.
        if (!existingMap.isEmpty()) {
            carouselProductRepository.deleteAll(existingMap.values());
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public List<CarouselResponseDTO> bulkSave(List<CarouselBulkRequestDTO> dtos) {
        // 1. Opcional: Se o seu objetivo for fazer um "Replace" total (deletar o que não foi enviado no JSON):
        // List<UUID> idsEnviados = dtos.stream()
        //     .filter(d -> d.id() != null && !d.id().startsWith("temp-"))
        //     .map(d -> UUID.fromString(d.id()))
        //     .toList();
        // carouselRepository.deleteAllByIdNotIn(idsEnviados);

        // 2. Mapeia os DTOs recebidos para Entidades do Banco
        List<Carousel> carouselsToSave = dtos.stream().map(dto -> {
            Carousel carousel;

            // Se for um carrossel existente (UUID válido)
            if (dto.id() != null && !dto.id().startsWith("temp-")) {
                UUID uuid = UUID.fromString(dto.id());
                // Busca o existente para não perder os produtos já vinculados a ele
                carousel = carouselRepository.findById(uuid)
                        .orElse(new Carousel());
                carousel.setId(uuid);
            } else {
                // Se começar com "temp-", é um carrossel totalmente novo criado no front
                carousel = new Carousel();
                carousel.setId(null); // O Hibernate vai gerar o UUID real
            }

            carousel.setName(dto.name());
            carousel.setPosition(dto.position());
            return carousel;
        }).toList();

        // 3. Salva todo o lote de uma única vez de forma atômica
        List<Carousel> savedCarousels = carouselRepository.saveAll(carouselsToSave);

        // 4. Converte de volta para CarouselResponseDTO para responder ao Front-end
        return savedCarousels.stream()
                .map(c -> new CarouselResponseDTO(c.getId(), c.getName(), c.getPosition(), /* seus produtos aqui se houver */ null))
                .sorted(java.util.Comparator.comparing(CarouselResponseDTO::position))
                .toList();
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public CarouselResponseDTO create(CarouselRequestDTO request) {
        long count = carouselRepository.count();

        UUID novoId = UUID.randomUUID();
        carouselRepository.insertCustom(novoId, request.name(), (int) count);

        Carousel carousel = Carousel.builder()
                .id(novoId)
                .name(request.name())
                .position((int) count)
                .carouselProducts(new java.util.ArrayList<>()) // Evita NullPointerException no DTO
                .build();

        System.out.println("Salvo com Sucesso via SQL Nativo:");
        System.out.println("Nome: " + carousel.getName());
        System.out.println("Posição: " + carousel.getPosition());
        System.out.println("ID: " + carousel.getId());

        Carousel carouselSalvoOficial = carouselRepository.findById(novoId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Erro ao recuperar o carrossel salvo"));

        return CarouselResponseDTO.from(carouselSalvoOficial, reviewRepository);
    }

    @Transactional
    public void delete(UUID id) {
        Carousel carousel = carouselRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Carrossel não encontrado"));

        carouselRepository.delete(carousel);
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