package me.unp0wnable.groupeo.model.services;

import me.unp0wnable.groupeo.model.entities.UserAddress;
import me.unp0wnable.groupeo.model.entities.UserProfile;
import me.unp0wnable.groupeo.model.exceptions.IncorrectLoginException;
import me.unp0wnable.groupeo.model.exceptions.IncorrectPasswordExcepion;
import me.unp0wnable.groupeo.model.exceptions.InstanceAlreadyExistsException;
import me.unp0wnable.groupeo.model.exceptions.InstanceNotFoundException;
import me.unp0wnable.groupeo.model.repositories.UserAddressRepository;
import me.unp0wnable.groupeo.model.repositories.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    /* ***************************** DEPENDENCIES INJECTION ***************************** */
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private UserAddressRepository userAddressRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    /* *********************************** USE CASES *********************************** */
    @Override
    public void signUp(UserProfile profile, UserAddress address) throws InstanceAlreadyExistsException {
        // Comprobar si ya existe un usuario con el mismo nick
        if (userProfileRepository.existsByNickName(profile.getNickName())) {
            throw new InstanceAlreadyExistsException(UserProfile.class.getName(), profile.getNickName());
        }
        
        // Asignar datos por defecto del usuario
        profile.setPassword(passwordEncoder.encode(profile.getPassword()));
        profile.setFirstName(capitalize(profile.getFirstName()));
        profile.setSurname1(capitalize(profile.getSurname1()));
        profile.setSurname2(capitalize(profile.getSurname2()));
        profile.setJoinDate(( Date ) Calendar.getInstance().getTime());
        profile.setScore((float) 0);
        
        // Guardar datos de usuario recien creado
        UserProfile storedUser = userProfileRepository.save(profile);
        
        // Asignar datos de la dirección (si existen)
        if (address != null) {
            address.setUserProfile(storedUser);
            UserAddress storedAddress = userAddressRepository.save(address);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserProfile login(String nickName, String rawPassword) throws IncorrectLoginException {
        // Comprobar si existe el usuario recibido
        Optional<UserProfile> optionalUser = userProfileRepository.findByNickNameIgnoreCase(nickName);
        if ( optionalUser.isEmpty() ) {
            throw new IncorrectLoginException(nickName, rawPassword);
        }
        UserProfile user = optionalUser.get();
        
        // Comprobar si las contraseñas coinciden
        if ( !passwordEncoder.matches(rawPassword, user.getPassword()) ) {
            throw new IncorrectLoginException(user.getNickName(), rawPassword);
        }
        
        return user;
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserProfile loginFromServiceToken(UUID id) throws InstanceNotFoundException {
        // Comprobar si existe el usuario con el ID recibido
        Optional<UserProfile> optionalUser = userProfileRepository.findById(id);
        if ( optionalUser.isEmpty() ) {
            throw new InstanceNotFoundException(UserProfile.class.getName(), id);
        }
        return optionalUser.get();
    }
    
    @Override
    public void changePassword(UUID id, String oldPassword, String newPassword) throws InstanceNotFoundException, IncorrectPasswordExcepion {
        // Comprobar si existe el usuario con el ID recibido
        Optional<UserProfile> optionalUser = userProfileRepository.findById(id);
        if ( optionalUser.isEmpty() ) {
            throw new InstanceNotFoundException(UserProfile.class.getName(), id);
        }
        UserProfile user = optionalUser.get();
        
        // Comprobar que contraseñas coincidan
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IncorrectPasswordExcepion();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        
        userProfileRepository.save(user);
    }
    
    @Override
    public UserProfile updateUserProfile(UUID id, UserProfile profile) throws InstanceNotFoundException {
        // Comprobar si existe el usuario con el ID recibido
        Optional<UserProfile> optionalUser = userProfileRepository.findById(id);
        if ( optionalUser.isEmpty() ) {
            throw new InstanceNotFoundException(UserProfile.class.getName(), id);
        }
        UserProfile user = optionalUser.get();
        
        // Comprobar qué datos se han modificado y actualizarlos
        if (!profile.getFirstName().equals(user.getFirstName()))
            user.setFirstName(capitalize(profile.getFirstName()));
        if (!profile.getSurname1().equals(user.getSurname1()))
            user.setSurname1(capitalize(profile.getSurname1()));
        if (!profile.getSurname2().equals(user.getSurname2()))
            user.setSurname2(capitalize(profile.getSurname2()));
        if (!profile.getEmail().equals(user.getEmail()))
            user.setEmail(profile.getEmail());
        if (!profile.getDescription().equals(user.getDescription()))
            user.setDescription(profile.getDescription());

        return userProfileRepository.save(user);
    }
    
    public UserAddress updateUserAddress(UUID userID, UserAddress addressData) throws InstanceNotFoundException {
        // Comprobar si existe el usuario con el ID recibido
        Optional<UserProfile> optionalUser = userProfileRepository.findById(userID);
        if ( optionalUser.isEmpty() ) {
            throw new InstanceNotFoundException(UserProfile.class.getName(), userID);
        }
        UserProfile user = optionalUser.get();
        
        return userAddressRepository.save(addressData);
    }
    
    @Override
    public void deleteUser(UUID id) throws InstanceNotFoundException {
        // Comprobar si existe el usuario con el ID recibido
        Optional<UserProfile> optionalUser = userProfileRepository.findById(id);
        if ( optionalUser.isEmpty() ) {
            throw new InstanceNotFoundException(UserProfile.class.getName(), id);
        }
        UserProfile user = optionalUser.get();
        
        // ELiminar al usuario
        userProfileRepository.deleteById(id);
    }
    
    
    /* ****************************** AUX FUNCTIONS ****************************** */
    private String capitalize(String string) {
        if (string == null) return null;
        
        String firstChar = string.substring(0, 1);
        String rest = string.substring(1);
        
        return firstChar.toUpperCase() + rest;
    }
    
}