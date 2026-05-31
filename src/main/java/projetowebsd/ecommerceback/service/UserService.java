package projetowebsd.ecommerceback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projetowebsd.ecommerceback.dto.user.UpdateProfileRequestDTO;
import projetowebsd.ecommerceback.dto.user.UserResponseDTO;
import projetowebsd.ecommerceback.exception.ResourceNotFoundException;
import projetowebsd.ecommerceback.model.User;
import projetowebsd.ecommerceback.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponseDTO> listAll() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::from)
                .toList();
    }

    public UserResponseDTO findById(UUID id) {
        return UserResponseDTO.from(
                userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id))
        );
    }

    public UserResponseDTO getProfile(String email) {
        return UserResponseDTO.from(getByEmail(email));
    }

    @Transactional
    public UserResponseDTO updateProfile(String email, UpdateProfileRequestDTO request) {
        User user = getByEmail(email);
        user.setName(request.name());
        return UserResponseDTO.from(userRepository.save(user));
    }

    private User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }
}
