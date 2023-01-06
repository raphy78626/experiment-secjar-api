package com.secjar.secjarapi.services;

import com.secjar.secjarapi.dtos.RegistrationRequestDTO;
import com.secjar.secjarapi.enums.UserRolesEnum;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    public boolean checkIfUserWithEmailExist(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean checkIfUserWithUsernameExist(String username) {
        return userRepository.findByEmail(username).isPresent();
    }

    public String addUserToDatabase(RegistrationRequestDTO registrationRequestDTO) {

        User user = new User(
                UUID.randomUUID().toString(),
                registrationRequestDTO.username(),
                passwordEncoder.encode(registrationRequestDTO.password()),
                registrationRequestDTO.email(),
                List.of(roleService.getRole(UserRolesEnum.ROLE_USER))
        );

        userRepository.save(user);

        return user.getUuid();
    }
}
