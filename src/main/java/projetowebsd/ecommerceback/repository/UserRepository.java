package projetowebsd.ecommerceback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projetowebsd.ecommerceback.model.User;
import projetowebsd.ecommerceback.model.enums.UserRole;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByRole(UserRole role);
}
