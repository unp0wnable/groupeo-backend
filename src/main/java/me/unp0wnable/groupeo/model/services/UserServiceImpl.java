package me.unp0wnable.groupeo.model.services;

import me.unp0wnable.groupeo.model.constants.FriendshipStatusCodes;
import me.unp0wnable.groupeo.model.constants.UserRoles;
import me.unp0wnable.groupeo.model.entities.*;
import me.unp0wnable.groupeo.model.entities.identities.FriendshipPK;
import me.unp0wnable.groupeo.model.exceptions.*;
import me.unp0wnable.groupeo.model.repositories.*;
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
    private FriendshipRepository friendshipRepository;
    @Autowired
    private FriendshipStatusRepository friendshipStatusRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    /* *********************************** USE CASES *********************************** */
    /* *********************************** User profile *********************************** */
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
        return fetchUser(userID);
    }
    
    @Override
    public void changePassword(UUID userID, String oldPassword, String newPassword) throws InstanceNotFoundException, IncorrectPasswordExcepion {
        // Obtener al usuario
        User user = fetchUser(userID);
        
        // Comprobar que contraseñas coincidan
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IncorrectPasswordExcepion();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        
        userRepository.save(user);
    }
    
    @Override
    public User updateUserProfile(UUID userID, User profile) throws InstanceNotFoundException {
        // Obtener al usuario
        User user = fetchUser(userID);
        
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
        // Obtener al usuario
        User user = fetchUser(userID);
        
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
    
    /* *********************************** User relationships *********************************** */
    public FriendshipStatus addFriend(UUID requestorUserID, UUID targetUserID)
            throws InstanceNotFoundException, TargetUserIsCurrentUser, TargetUserIsAlreadyFriend, BlockedUserException {
        // Comprobar que el usuario actual y el objetivo no sean el mismo
        if (requestorUserID.equals(targetUserID)) throw new TargetUserIsCurrentUser();
        
        // Obtener a los usuarios
        User requestorUser = fetchUser(requestorUserID);
        User targetUser = fetchUser(targetUserID);
        
        // Comprobar que no sean amigos actualmente
        Friendship friendship = getFriendshipBetweenUsers(requestorUser, targetUser);
        
        // Comprobar que usuario a agregar no esté bloqueado por el usuario que envía la petición
        FriendshipStatus friendshipStatus = getFriendshipStatusBetweenUsers(requestorUser, targetUser);
        if (friendshipStatus.getStatus().equals(FriendshipStatusCodes.BLOQUED))
            throw new BlockedUserException(targetUserID);

        // Establecer petición de amistad del usuario al objetivo
        createNewFriendshipBetweenUsers(requestorUser, targetUser);
        
        
    }
    
    
    /* ****************************** AUX FUNCTIONS ****************************** */
    private User fetchUser(UUID userID) throws InstanceNotFoundException {
        // Comprobar si existe el usuario con el ID recibido
        Optional<User> optionalUser = userRepository.findById(userID);
        if ( optionalUser.isEmpty() )
            throw new InstanceNotFoundException(User.class.getName(), userID);
        
        return optionalUser.get();
    }
    
    private String capitalize(String string) {
        if (string == null) return null;
        if (string.isEmpty()) return string;
        
        String firstChar = string.substring(0, 1);
        String rest = string.substring(1);
        
        return firstChar.toUpperCase() + rest;
    }
    
    /** Obtiene una instancia de la amistad entre dos usuarios */
    private Friendship getFriendshipBetweenUsers(User requestorUser, User targetUser) {
        // Extraer los ID de los usuarios antes de realizar las operaciones
        UUID requestorID = requestorUser.getUserID();
        UUID targetID = targetUser.getUserID();
        
        // Como la clave primaria es compuesta, se instancia un objeto con los ID y se le pasa al repositorio
        FriendshipPK id = new FriendshipPK(requestorID, targetID);
        
        Optional<Friendship> optionalFriendship = friendshipRepository.findById(id);
        if ( optionalFriendship.isEmpty() ) return null;
        
        return optionalFriendship.get();
    }
    
    /** Obtiene una instancia de la amistad entre dos usuarios */
    private FriendshipStatus getFriendshipStatusBetweenUsers(User requestorUser, User targetUser) throws InstanceNotFoundException {
        // Extraer los ID de los usuarios antes de realizar las operaciones
        UUID requestorID = requestorUser.getUserID();
        UUID targetID = targetUser.getUserID();
        
        // Como la clave primaria es compuesta, se instancia un objeto con los ID y se le pasa al repositorio
        FriendshipPK id = new FriendshipPK(requestorID, targetID);
        
        Optional<FriendshipStatus> optionalStatus = friendshipStatusRepository
                .getMostRecentFriendshipStatusBetweenUsers(requestorID, targetID);
        
        if ( optionalStatus.isEmpty() )
            throw new InstanceNotFoundException(FriendshipStatus.class.getName(), id);
        
        return optionalStatus.get();
    }
    
    private Friendship createNewFriendshipBetweenUsers(User requestorUser, User targetUser) {
        // Extraer los ID de los usuarios antes de realizar las operaciones
        UUID requestorID = requestorUser.getUserID();
        UUID targetID = targetUser.getUserID();
    
        // Crea la relación de amistad entre los usuarios
        Friendship createdFriendship = new Friendship();

        // Inserta un registro en la relación indicando que se ha enviado una petición de amistad
        FriendshipStatus createdFriendshipStatus = new FriendshipStatus();
        createdFriendshipStatus.setSpecifier(requestorUser);
        createdFriendshipStatus.setStatus(FriendshipStatusCodes.REQUESTED);
        
        // Asigna el registro con la relación de amistad recién creada
        createdFriendship.getFriendshipStatus().add(createdFriendshipStatus);
        
        // Guardar relación y petición de amistad
        Friendship storedFriendship = friendshipRepository.save(createdFriendship);
        
        return storedFriendship;
    }
    
    /** Comprueba si dos usuarios son amigos */
    private boolean areUsersFriends(User requestorUser, User targetUser) {
        try {
            // Si lanza InstanceNotFoundException es que no son amigos
            FriendshipStatus friendshipStatus = this.getFriendshipStatusBetweenUsers(requestorUser, targetUser);
            FriendshipStatusCodes currentStatus = friendshipStatus.getStatus();
            
            return (currentStatus.equals(FriendshipStatusCodes.ACCEPTED));
        } catch ( InstanceNotFoundException ex) {
            return false;
        }
    }
    
}
