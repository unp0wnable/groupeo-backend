package me.unp0wnable.groupeo.model.services;

import me.unp0wnable.groupeo.model.entities.User;
import me.unp0wnable.groupeo.model.entities.User.UserRoles;
import me.unp0wnable.groupeo.model.entities.UserAddress;
import me.unp0wnable.groupeo.model.exceptions.*;
import me.unp0wnable.groupeo.model.repositories.UserAddressRepository;
import me.unp0wnable.groupeo.model.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    /* ***************************** DEPENDENCIES INJECTION ***************************** */
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserAddressRepository userAddressRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    /* *********************************** USE CASES *********************************** */
    @Override
    public User signUp(User profile) throws InstanceAlreadyExistsException {
        // Comprobar si ya existe un usuario con el mismo nick
        if ( userRepository.existsByNickName(profile.getNickName())) {
            throw new InstanceAlreadyExistsException(User.class.getName(), profile.getNickName());
        }
        
        // Asignar datos por defecto del usuario
        profile.setPassword(passwordEncoder.encode(profile.getPassword()));
        profile.setFirstName(capitalize(profile.getFirstName()));
        profile.setSurname1(capitalize(profile.getSurname1()));
        profile.setSurname2(capitalize(profile.getSurname2()));
        profile.setJoinDate(Calendar.getInstance().getTime());
        profile.setScore((float) 0);
        profile.setRole(UserRoles.USER);
        
        // Guardar datos de usuario recién creado
        return userRepository.save(profile);
    }
    
    @Override
    @Transactional(readOnly = true)
    public User login(String nickName, String rawPassword) throws IncorrectLoginException {
        // Comprobar si existe el usuario recibido
        Optional<User> optionalUser = userRepository.findByNickNameIgnoreCase(nickName);
        if ( optionalUser.isEmpty() ) {
            throw new IncorrectLoginException(nickName, rawPassword);
        }
        User user = optionalUser.get();
        
        // Comprobar si las contraseñas coinciden
        if ( !passwordEncoder.matches(rawPassword, user.getPassword()) ) {
            throw new IncorrectLoginException(user.getNickName(), rawPassword);
        }
        
        return user;
    }
    
    @Override
    @Transactional(readOnly = true)
    public User loginFromServiceToken(UUID userID) throws InstanceNotFoundException {
        // Comprobar si existe el usuario con el ID recibido
        Optional<User> optionalUser = userRepository.findById(userID);
        if ( optionalUser.isEmpty() ) {
            throw new InstanceNotFoundException(User.class.getName(), userID);
        }
        return optionalUser.get();
    }
    
    @Override
    public void changePassword(UUID userID, String oldPassword, String newPassword) throws InstanceNotFoundException, IncorrectPasswordExcepion {
        // Comprobar si existe el usuario con el ID recibido
        Optional<User> optionalUser = userRepository.findById(userID);
        if ( optionalUser.isEmpty() ) {
            throw new InstanceNotFoundException(User.class.getName(), userID);
        }
        User user = optionalUser.get();
        
        // Comprobar que contraseñas coincidan
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IncorrectPasswordExcepion();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        
        userRepository.save(user);
    }
    
    @Override
    public User updateUserProfile(UUID userID, User profile) throws InstanceNotFoundException {
        // Comprobar si existe el usuario con el ID recibido
        Optional<User> optionalUser = userRepository.findById(userID);
        if ( optionalUser.isEmpty() ) {
            throw new InstanceNotFoundException(User.class.getName(), userID);
        }
        User user = optionalUser.get();
        
        // Comprobar qué datos se han modificado y actualizarlos
        if ((profile.getFirstName() != null) && !profile.getFirstName().equals(user.getFirstName()))
            user.setFirstName(capitalize(profile.getFirstName()));
        if ((profile.getSurname1() != null) && !profile.getSurname1().equals(user.getSurname1()))
            user.setSurname1(capitalize(profile.getSurname1()));
        if ((profile.getSurname2() != null) && !profile.getSurname2().equals(user.getSurname2()))
            user.setSurname2(capitalize(profile.getSurname2()));
        if ((profile.getEmail() != null) && !profile.getEmail().equals(user.getEmail()))
            user.setEmail(profile.getEmail());
        if ((profile.getDescription() != null) && !profile.getDescription().equals(user.getDescription()))
            user.setDescription(profile.getDescription());

        return userRepository.save(user);
    }
    
    @Override
    public UserAddress assignAddressToUser(UUID userID, UserAddress address) throws InstanceNotFoundException {
        // Comprobar si existe el usuario con el ID recibido
        Optional<User> optionalUser = userRepository.findById(userID);
        if ( optionalUser.isEmpty() ) {
            throw new InstanceNotFoundException(User.class.getName(), userID);
        }
        User user = optionalUser.get();
        
        // Asigna la dirección al usuario
        user.setAddress(address);
        User updatedUser = userRepository.save(user);
        
        return updatedUser.getAddress();
    }
    
    @Override
    public void deleteUser(UUID userID) throws InstanceNotFoundException {
        // Comprobar si existe el usuario con el ID recibido
        Optional<User> optionalUser = userRepository.findById(userID);
        if ( optionalUser.isEmpty() ) {
            throw new InstanceNotFoundException(User.class.getName(), userID);
        }
        
        // Eliminar al usuario
        userRepository.deleteById(userID);
    }
    
    
    /* ****************************** AUX FUNCTIONS ****************************** */
    private String capitalize(String string) {
        if (string == null) return null;
        if (string.isEmpty()) return string;
        
        String firstChar = string.substring(0, 1);
        String rest = string.substring(1);
        
        return firstChar.toUpperCase() + rest;
    }
    
}
