package projetowebsd.ecommerceback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projetowebsd.ecommerceback.dto.review.ReviewRequestDTO;
import projetowebsd.ecommerceback.dto.review.ReviewResponseDTO;
import projetowebsd.ecommerceback.exception.BusinessException;
import projetowebsd.ecommerceback.model.Review;
import projetowebsd.ecommerceback.model.User;
import projetowebsd.ecommerceback.repository.OrderRepository;
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

        if (!orderRepository.existsApprovedOrderWithProduct(user.getId(), productId)) {
            throw new BusinessException("Só é possível avaliar produtos de pedidos aprovados");
        }

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new BusinessException("Você já avaliou este produto");
        }

        Review review = Review.builder()
                .product(productService.getProduct(productId))
                .user(user)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        return ReviewResponseDTO.from(reviewRepository.save(review));
    }
}
