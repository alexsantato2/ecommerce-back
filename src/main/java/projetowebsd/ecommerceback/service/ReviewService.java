package projetowebsd.ecommerceback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projetowebsd.ecommerceback.dto.review.ReviewRequestDTO;
import projetowebsd.ecommerceback.dto.review.ReviewResponseDTO;
import projetowebsd.ecommerceback.exception.BusinessException;
import projetowebsd.ecommerceback.model.Review;
import projetowebsd.ecommerceback.model.Product;
import projetowebsd.ecommerceback.model.User;
import projetowebsd.ecommerceback.repository.OrderRepository;
import projetowebsd.ecommerceback.repository.ProductRepository;
import projetowebsd.ecommerceback.repository.ReviewRepository;
import projetowebsd.ecommerceback.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;

    public List<ReviewResponseDTO> listByProduct(UUID productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(ReviewResponseDTO::from)
                .toList();
    }

    @Transactional
    public ReviewResponseDTO create(UUID productId, ReviewRequestDTO request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        boolean temPedidoAprovado = orderRepository.existsApprovedOrderWithProduct(user.getId(), productId);

        if (!temPedidoAprovado) {
            throw new BusinessException("Só é possível avaliar produtos de pedidos aprovados");
        }

        boolean jaAvaliou = reviewRepository.existsByUserIdAndProductId(user.getId(), productId);

        Review review;

        if (jaAvaliou) {
            review = reviewRepository.findByUserIdAndProductId(user.getId(), productId)
                    .orElseThrow(() -> new BusinessException("Erro ao recuperar a avaliação existente"));

            review.setRating(request.rating());
            review.setComment(request.comment());
        } else {
            review = Review.builder()
                    // Aqui você pode usar o productRepository.findById caso não queira depender do productService
                    .product(productRepository.findById(productId).orElseThrow(() -> new BusinessException("Produto não encontrado")))
                    .user(user)
                    .rating(request.rating())
                    .comment(request.comment())
                    .build();
        }

        // Salva a review (seja nova ou update)
        Review reviewSalva = reviewRepository.save(review);

        // 🚀 RECALCULA E ATUALIZA A MÉDIA NO PRODUTO:
        atualizarMediaDoProduto(productId);

        return ReviewResponseDTO.from(reviewSalva);
    }

    // 💡 Adicione este método na sua classe ReviewService
    private void atualizarMediaDoProduto(UUID productId) {
        // Usa as queries prontas que você já tem no seu ReviewRepository!
        Double novaMedia = reviewRepository.findAverageRatingByProductId(productId);
        long novaQuantidade = reviewRepository.countByProductId(productId);

        // Evita salvar nulo se não houver notas
        if (novaMedia == null) {
            novaMedia = 0.0;
        }

        // Busca o produto correspondente na tabela de produtos
        projetowebsd.ecommerceback.model.Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Produto não encontrado"));

        // Atualiza os campos de cache de nota do produto
        product.setAverageRating(novaMedia);
        product.setReviewCount((int) novaQuantidade);

        // Salva o produto atualizado no banco
        productRepository.save(product);
    }

    public boolean hasUserReviewedProduct(UUID productId, String email) {
        // Aqui você verifica se existe alguma avaliação com o productId E o email/username do usuário
        return reviewRepository.existsByProductIdAndUserEmail(productId, email);
    }
}
