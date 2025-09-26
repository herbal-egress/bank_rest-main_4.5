package com.example.bankcards.service.impl;
import com.example.bankcards.dto.user.UserRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.UserUtils; 
import com.example.bankcards.mapper.UserMapper; 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserUtils userUtils; 
    private final UserMapper userMapper; 
    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Запрос на создание пользователя: {}", userRequest.getUsername());
        if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            log.error("Попытка создать пользователя с уже существующим именем: {}", userRequest.getUsername());
            throw new UsernameAlreadyExistsException("Пользователь с именем '" + userRequest.getUsername() + "' уже существует");
        }
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        Set<Role> roles = userUtils.resolveRoles(userRequest.getRoles()); 
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        log.info("Пользователь успешно создан с ID: {}", savedUser.getId());
        return userMapper.mapToUserResponse(savedUser); 
    }
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Запрос пользователя по ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + id + " не найден"));
        return userMapper.mapToUserResponse(user); 
    }
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Запрос всех пользователей");
        List<User> users = userRepository.findAll();
        return users.stream().map(userMapper::mapToUserResponse).collect(Collectors.toList()); 
    }
    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        log.info("Запрос на обновление пользователя с ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + id + " не найден"));
        if (!user.getUsername().equals(userRequest.getUsername()) &&
                userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            log.error("Попытка обновить имя пользователя на уже существующее: {}", userRequest.getUsername());
            throw new UsernameAlreadyExistsException("Пользователь с именем '" + userRequest.getUsername() + "' уже существует");
        }
        user.setUsername(userRequest.getUsername());
        if (userRequest.getPassword() != null && !userRequest.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
        if (userRequest.getRoles() != null) {
            user.setRoles(userUtils.resolveRoles(userRequest.getRoles())); 
        }
        User updatedUser = userRepository.save(user);
        log.info("Пользователь с ID {} успешно обновлен", id);
        return userMapper.mapToUserResponse(updatedUser); 
    }
    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Запрос на удаление пользователя с ID: {}", id);
        if (!userRepository.existsById(id)) {
            log.error("Попытка удалить несуществующего пользователя с ID: {}", id);
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден");
        }
        userRepository.deleteById(id);
        log.info("Пользователь с ID {} успешно удален", id);
    }
}