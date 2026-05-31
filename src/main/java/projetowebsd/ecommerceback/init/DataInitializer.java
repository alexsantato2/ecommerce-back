package projetowebsd.ecommerceback.init;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import projetowebsd.ecommerceback.model.User;
import projetowebsd.ecommerceback.model.enums.UserRole;
import projetowebsd.ecommerceback.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByRole(UserRole.ADMIN)) {
            User admin = User.builder()
                    .name("Administrador")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
        }
    }
}
