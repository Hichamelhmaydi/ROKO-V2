package com.example.roko.service;

import com.example.roko.dto.CreateUserRequest;
import com.example.roko.dto.UserDTO;
import com.example.roko.entity.Admin;
import com.example.roko.entity.User;
import com.example.roko.entity.Voyageurs;
import com.example.roko.enums.CompteStatus;
import com.example.roko.mapper.UserMapper;
import com.example.roko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));
        return userMapper.toDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email: " + email));
        return userMapper.toDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByStatus(CompteStatus status) {
        List<User> users = userRepository.findByStatus(status);
        return users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> searchUsers(String search) {
        List<User> users = userRepository.searchByNomOrPrenom(search);
        return users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getActiveUsers() {
        List<User> users = userRepository.findAllByStatusOrderByNomAsc(CompteStatus.ACTIVER);
        return users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        if (!existingUser.getEmail().equals(userDTO.getEmail())
                && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        existingUser.setNom(userDTO.getNom());
        existingUser.setPrenom(userDTO.getPrenom());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setTelephone(userDTO.getTelephone());

        if (userDTO.getStatus() != null) {
            existingUser.setStatus(CompteStatus.valueOf(userDTO.getStatus()));
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDTO(updatedUser);
    }

    public UserDTO toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        if (user.getStatus() == CompteStatus.ACTIVER) {
            user.setStatus(CompteStatus.DESACTIVER);
        } else {
            user.setStatus(CompteStatus.ACTIVER);
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    public UserDTO updatePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        user.setPassword(newPassword);

        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.countByStatus(CompteStatus.ACTIVER);
    }

    @Transactional(readOnly = true)
    public long countAllUsers() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public long countInactiveUsers() {
        return userRepository.countByStatus(CompteStatus.DESACTIVER);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAdmins() {
        List<User> admins = userRepository.findByUserType(Admin.class);
        return admins.stream().map(userMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getVoyageurs() {
        List<User> voyageurs = userRepository.findByUserType(Voyageurs.class);
        return voyageurs.stream().map(userMapper::toDTO).collect(Collectors.toList());
    }


    public UserDTO createUser(CreateUserRequest userDTO) {

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        User user = userMapper.toEntity(userDTO);

        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setStatus(CompteStatus.ACTIVER);

        User savedUser = userRepository.save(user);

        return userMapper.toDTO(savedUser);
    }
}