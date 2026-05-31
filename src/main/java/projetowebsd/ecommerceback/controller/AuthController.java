package projetowebsd.ecommerceback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projetowebsd.ecommerceback.dto.auth.AuthResponseDTO;
import projetowebsd.ecommerceback.dto.auth.LoginRequestDTO;
import projetowebsd.ecommerceback.dto.auth.RefreshRequestDTO;
import projetowebsd.ecommerceback.dto.auth.RegisterRequestDTO;
import projetowebsd.ecommerceback.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Registro, login e renovação de tokens")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Cadastrar novo usuário")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário e obter tokens JWT")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token usando refresh token")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidar refresh token (logout)")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshRequestDTO request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso"));
    }
}
