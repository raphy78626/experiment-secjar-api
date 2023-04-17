package com.secjar.secjarapi.services;

import CryptoServerCXI.CryptoServerCXI;
import com.secjar.secjarapi.dtos.requests.UserPatchRequestDTO;
import com.secjar.secjarapi.enums.MFATypeEnum;
import com.secjar.secjarapi.enums.UserRolesEnum;
import com.secjar.secjarapi.exceptions.BadNewPasswordException;
import com.secjar.secjarapi.exceptions.ResourceNotFoundException;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.models.UserRole;
import com.secjar.secjarapi.repositories.UserRepository;
import com.secjar.secjarapi.utils.PasswordValidatorUtil;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final HsmService hsmService;
    private final PasswordValidatorUtil passwordValidator;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService, HsmService hsmService, PasswordValidatorUtil passwordValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.hsmService = hsmService;
        this.passwordValidator = passwordValidator;
    }

    public User getUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(() -> new ResourceNotFoundException(String.format("User with uuid: %s does not exist", uuid)));
    }

    public User getUserById(long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format("User with id: %s does not exist", id)));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(String.format("User with username: %s does not exist", username)));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException(String.format("User with email: %s does not exist", email)));
    }

    public boolean checkIfUserWithEmailExist(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean checkIfUserWithUsernameExist(String username) {
        return userRepository.findByEmail(username).isPresent();
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void enableUser(User user) {
        user.setVerified(true);
        saveUser(user);
    }

    public void addCryptoKeyToUser(long id) {
        User user = getUserById(id);

        CryptoServerCXI.Key key = hsmService.generateKey(String.format("%s's key", user.getUuid()));
        byte[] keyIndex = hsmService.insertKeyToStore(key);

        user.setCryptographicKeyIndex(keyIndex);

        saveUser(user);
    }

    public void changeUserPasswordByUuid(String userUuid, String newPassword) {
        User user = getUserByUuid(userUuid);

        if (!passwordValidator.validate(newPassword)) {
            throw new BadNewPasswordException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        saveUser(user);
    }

    public void changeUserPasswordByEmail(String userEmail, String newPassword) {
        User user = getUserByEmail(userEmail);

        if (!passwordValidator.validate(newPassword)) {
            throw new BadNewPasswordException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        saveUser(user);
    }

    public void pathUser(String userUuid, UserPatchRequestDTO userPatchRequestDTO) {
        User user = getUserByUuid(userUuid);

        if (userPatchRequestDTO.fileDeletionDelay() != null) {
            user.setFileDeletionDelay(userPatchRequestDTO.fileDeletionDelay());
        }

        if (userPatchRequestDTO.allowedDiskSpace() != null) {
            changeAllowedDiskSpace(user, userPatchRequestDTO.allowedDiskSpace());
        }

        saveUser(user);
    }

    private void changeAllowedDiskSpace(User user, long allowedDiskSpace) {
        if (allowedDiskSpace < user.getCurrentDiskSpace()) {
            throw new IllegalArgumentException("New allowed disk space is less than currently occupied space");
        }

        user.setAllowedDiskSpace(allowedDiskSpace);

        saveUser(user);
    }

    public boolean verifyUserPassword(String userUuid, String password) {
        User user = getUserByUuid(userUuid);

        return passwordEncoder.matches(password, user.getPassword());
    }

    public void updateUserMFA(String userUuid, MFATypeEnum mfaTypeEnum) {
        User user = getUserByUuid(userUuid);

        user.setMfaType(mfaTypeEnum);

        saveUser(user);
    }

    public void increaseTakenDiskSpace(String userUuid, long newFileSize) {
        User user = getUserByUuid(userUuid);

        user.setCurrentDiskSpace(user.getCurrentDiskSpace() + newFileSize);

        saveUser(user);
    }

    public void decreaseTakenDiskSpace(String userUuid, long deletedFileSize) {
        User user = getUserByUuid(userUuid);

        user.setCurrentDiskSpace(user.getCurrentDiskSpace() - deletedFileSize);

        saveUser(user);
    }

    public boolean isUserAdmin(String userUuid) {
        User user = getUserByUuid(userUuid);
        UserRole adminRole = roleService.getRole(UserRolesEnum.ROLE_ADMIN);

        return user.getRoles().contains(adminRole);
    }

    public void deleteUserByUuid(String userToDeleteUuid) {
        User user = getUserByUuid(userToDeleteUuid);

        userRepository.delete(user);
    }

    public void updateUserAdminRole(String userUuid, boolean updateIsUserAdmin) {
        UserRole adminRole = roleService.getRole(UserRolesEnum.ROLE_ADMIN);
        User user = getUserByUuid(userUuid);

        if (updateIsUserAdmin) {
            user.getRoles().add(adminRole);
        } else {
            user.getRoles().remove(adminRole);
        }

        saveUser(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
