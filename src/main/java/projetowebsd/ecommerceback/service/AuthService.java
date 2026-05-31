package projetowebsd.ecommerceback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projetowebsd.ecommerceback.dto.auth.AuthResponseDTO;
import projetowebsd.ecommerceback.dto.auth.LoginRequestDTO;
import projetowebsd.ecommerceback.dto.auth.RegisterRequestDTO;
import projetowebsd.ecommerceback.exception.BusinessException;
import projetowebsd.ecommerceback.model.RefreshToken;
import projetowebsd.ecommerceback.model.User;
import projetowebsd.ecommerceback.model.enums.UserRole;
import projetowebsd.ecommerceback.repository.UserRepository;
import projetowebsd.ecommerceback.security.JwtTokenProvider;
import projetowebsd.ecommerceback.security.UserDetailsServiceImpl;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("E-mail já cadastrado");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.CUSTOMER)
                .build();

        userRepository.save(user);
        return buildResponse(user);
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        return buildResponse(user);
    }

    @Transactional
    public AuthResponseDTO refresh(String tokenString) {
        RefreshToken refreshToken = refreshTokenService.validate(tokenString);
        User user = refreshToken.getUser();
        refreshTokenService.delete(tokenString);
        return buildResponse(user);
    }

    @Transactional
    public void logout(String tokenString) {
        refreshTokenService.delete(tokenString);
    }

    private AuthResponseDTO buildResponse(User user) {
        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenProvider.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.generate(user);

        return new AuthResponseDTO(
                accessToken,
                refreshToken.getToken(),
                user.getId(),
                user.getName(),
                user.getRole().name()
        );
    }
}
