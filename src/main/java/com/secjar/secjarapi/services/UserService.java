package com.secjar.secjarapi.services;

import CryptoServerCXI.CryptoServerCXI;
import com.secjar.secjarapi.dtos.requests.RegistrationRequestDTO;
import com.secjar.secjarapi.enums.UserRolesEnum;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final HsmService hsmService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService, HsmService hsmService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.hsmService = hsmService;
    }

    public User getUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(() -> new UsernameNotFoundException(String.format("User with uuid: %s does not exist", uuid)));
    }

    public User getUserById(long id) {
        return userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException(String.format("User with id: %s does not exist", id)));
    }

    public boolean checkIfUserWithEmailExist(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean checkIfUserWithUsernameExist(String username) {
        return userRepository.findByEmail(username).isPresent();
    }

    public User createUserFromRegistrationRequest(RegistrationRequestDTO registrationRequestDTO) {
        return new User(
                UUID.randomUUID().toString(),
                registrationRequestDTO.username(),
                passwordEncoder.encode(registrationRequestDTO.password()),
                registrationRequestDTO.email(),
                List.of(roleService.getRole(UserRolesEnum.ROLE_USER))
        );
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public int enableUser(String email) {
        return userRepository.enableAppUser(email);
    }

    public void addCryptoKeyToUser(long id) {
        User user = getUserById(id);

        CryptoServerCXI.Key key =  hsmService.generateKey(String.format("%s's key", user.getUuid()));
        byte[] keyIndex = hsmService.insertKeyToStore(key);

        user.setCryptographicKeyIndex(keyIndex);

        saveUser(user);
    }
}
