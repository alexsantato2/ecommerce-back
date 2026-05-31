package projetowebsd.ecommerceback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projetowebsd.ecommerceback.exception.BusinessException;
import projetowebsd.ecommerceback.model.RefreshToken;
import projetowebsd.ecommerceback.model.User;
import projetowebsd.ecommerceback.repository.RefreshTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${refresh.token.expiration}")
    private long expirationMs;

    @Transactional
    public RefreshToken generate(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusSeconds(expirationMs / 1000))
                .build();

        return refreshTokenRepository.save(token);
    }

    public RefreshToken validate(String tokenString) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new BusinessException("Refresh token inválido"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new BusinessException("Refresh token expirado. Faça login novamente.");
        }

        return token;
    }

    @Transactional
    public void delete(String tokenString) {
        refreshTokenRepository.findByToken(tokenString)
                .ifPresent(refreshTokenRepository::delete);
    }
}
