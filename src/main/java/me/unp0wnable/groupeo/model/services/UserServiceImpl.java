package me.unp0wnable.groupeo.model.services;

import me.unp0wnable.groupeo.model.constants.FriendshipStatusCodes;
import me.unp0wnable.groupeo.model.constants.UserRoles;
import me.unp0wnable.groupeo.model.entities.*;
import me.unp0wnable.groupeo.model.entities.identities.FriendshipPK;
import me.unp0wnable.groupeo.model.exceptions.*;
import me.unp0wnable.groupeo.model.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class
UserServiceImpl implements UserService {
    /* ***************************** DEPENDENCIES INJECTION ***************************** */
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FriendshipRepository friendshipRepository;
    @Autowired
    private GroupRepository groupRepository;
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
    @Override
    public Friendship addFriend(UUID requestorUserID, UUID targetUserID)
            throws InstanceNotFoundException, TargetUserIsCurrentUserException, TargetUserIsAlreadyFriendException,
                   BlockedUserException, InstanceAlreadyExistsException {
        // Comprobar que el usuario actual y el objetivo no sean el mismo
        if (requestorUserID.equals(targetUserID)) throw new TargetUserIsCurrentUserException();
        
        // Obtener a los usuarios
        User requestorUser = fetchUser(requestorUserID);
        User targetUser = fetchUser(targetUserID);
        Friendship usersFriendship = getFriendshipBetweenUsers(requestorUser, targetUser);
        
        // Si ya existe algún tipo de relación entre los usuarios
        if ( usersFriendship != null) {
            switch ( usersFriendship.getStatus() ) {
                // Comprobar que los usuarios no estén bloqueados
                case BLOQUED:
                    throw new BlockedUserException(targetUserID);
                // Comprobar que no exista alguna petición de amistad sin responder
                case REQUESTED:
                    throw new InstanceAlreadyExistsException(Friendship.class.getName(), targetUserID);
                // Comprobar que no se pueda añadir como amigo a un usuario que ya es amigo actualmente
                case ACCEPTED:
                    throw new TargetUserIsAlreadyFriendException(targetUserID);
                // Si usuario ha rechazado la peticion, se puede volver a solicitar
                case DECLINED:
                    usersFriendship = updateFriendhipStatus(usersFriendship, requestorUser, FriendshipStatusCodes.REQUESTED);
            }
        }
    
        // Si no hay amistad entre los usuarios, se crea la petición de amistad
        if ( usersFriendship == null) {
            usersFriendship = createNewFriendship(requestorUser, targetUser);
            usersFriendship.setStatus(FriendshipStatusCodes.REQUESTED);
        }
        
        return usersFriendship;
    }
    
    @Override
    public void removeFriend(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, TargetUserIsCurrentUserException,
                                                                             TargetUserIsNotFriendException, BlockedUserException {
        // Comprobar que el usuario actual y el objetivo no sean el mismo
        if (requestorUserID.equals(targetUserID)) throw new TargetUserIsCurrentUserException();
    
        // Obtener a los usuarios
        User requestorUser = fetchUser(requestorUserID);
        User targetUser = fetchUser(targetUserID);
        Friendship friendship = getFriendshipBetweenUsers(requestorUser, targetUser);
    
        // Si no hay amistad, no se puede eliminar como amigo
        if (friendship == null)
            throw new TargetUserIsNotFriendException(targetUserID);
            
        // Si el usuario objetivo está bloqueado, no se puede modifiar la amistad
        if (friendship.getStatus().equals(FriendshipStatusCodes.BLOQUED))
            throw new BlockedUserException(targetUserID);
        
        // Elimina la entidad de la BD
        friendshipRepository.delete(friendship);
    }
    
    @Override
    public Friendship acceptFriendshipRequest(UUID requestorUserID, UUID targetUserID)
            throws InstanceNotFoundException, TargetUserIsCurrentUserException, NonExistentFriendshipRequestException,
                   TargetUserIsAlreadyFriendException {
        // Comprobar que el usuario actual y el objetivo no sean el mismo
        if (requestorUserID.equals(targetUserID)) throw new TargetUserIsCurrentUserException();
    
        // Obtener a los usuarios
        User requestorUser = fetchUser(requestorUserID);
        User targetUser = fetchUser(targetUserID);
    
        // Comprobar si hay una petición de amistad pendiente
        Friendship friendship = getFriendshipBetweenUsers(requestorUser, targetUser);
        
        // Si no hay petición de amistad, no se puede aceptar
        if (friendship == null) throw new NonExistentFriendshipRequestException(requestorUserID, targetUserID);
        
        // Solo se acepta la petición si no está aceptada previamente
        if (friendship.getStatus().equals(FriendshipStatusCodes.ACCEPTED))
            throw new TargetUserIsAlreadyFriendException(targetUserID);
        // Cambiar el estado de la amistad a ACEPTADA por parte del usuario actual
        Friendship updatedFriendship = updateFriendhipStatus(friendship, targetUser, FriendshipStatusCodes.ACCEPTED);
        
        return friendshipRepository.save(updatedFriendship);
    }
    
    @Override
    public Friendship declineFriendshipRequest(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, TargetUserIsCurrentUserException, NonExistentFriendshipRequestException {
        // Comprobar que el usuario actual y el objetivo no sean el mismo
        if (requestorUserID.equals(targetUserID)) throw new TargetUserIsCurrentUserException();
    
        // Obtener a los usuarios
        User requestorUser = fetchUser(requestorUserID);
        User targetUser = fetchUser(targetUserID);
    
        // Comprobar si hay una petición de amistad pendiente
        Friendship friendship = getFriendshipBetweenUsers(requestorUser, targetUser);
    
        // Si no hay petición de amistad, ńo se puede denegar
        if (friendship == null) throw new NonExistentFriendshipRequestException(requestorUserID, targetUserID);
    
        // Cambiar el estado de la amistad a ACEPTADA por parte del usuario actual
        Friendship updatedFriendship = updateFriendhipStatus(friendship, requestorUser, FriendshipStatusCodes.DECLINED);
    
        return friendshipRepository.save(updatedFriendship);
    }
    
    @Override
    public Friendship blockFriend(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, TargetUserIsCurrentUserException {
        // Comprobar que el usuario actual y el objetivo no sean el mismo
        if (requestorUserID.equals(targetUserID)) throw new TargetUserIsCurrentUserException();
    
        // Obtener a los usuarios
        User requestorUser = fetchUser(requestorUserID);
        User targetUser = fetchUser(targetUserID);
    
        // Comprobar si hay amistad actualmente
        Friendship friendship = getFriendshipBetweenUsers(requestorUser, targetUser);
        
        // Si no hay ningun tipo de relación previa, se crea una nueva
        if (friendship == null) {
            friendship = new Friendship();
            FriendshipPK friendshipPK = new FriendshipPK(requestorUserID, targetUserID);
            friendship.setId(friendshipPK);
        }
        
        // Asigna el estado de la relación a BLOQUEADO
        Friendship updatedFriendship = updateFriendhipStatus(friendship, targetUser, FriendshipStatusCodes.BLOQUED);
        
        return friendshipRepository.save(updatedFriendship);
    }
    
    @Override
    public void unblockFriend(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, TargetUserIsCurrentUserException {
        // Comprobar que el usuario actual y el objetivo no sean el mismo
        if (requestorUserID.equals(targetUserID)) throw new TargetUserIsCurrentUserException();
    
        // Obtener a los usuarios
        User requestorUser = fetchUser(requestorUserID);
        User targetUser = fetchUser(targetUserID);
    
        // Obtener la amistad actual y eliminarla
        Friendship friendship = getFriendshipBetweenUsers(requestorUser, targetUser);
        friendshipRepository.delete(friendship);
    }
    
    @Override
    public Block<User> getBlockedUsers(UUID userID, int page, int pageSize) throws InstanceNotFoundException {
        // Obtener al usuario actual
        User currentUser = fetchUser(userID);
        
        // Obtener usuarios bloqueados
        Pageable pagination = PageRequest.of(page, pageSize);
        Slice<User> usersSlice = friendshipRepository.getBlockedUsersByUserID(userID, pagination);
        
        // Crear respuesta paginada
        Block<User> block = createBlockFromSlice(usersSlice);
        
        return block;
    }
    
    @Override
    public Block<User> getUserFriends(UUID userID, int page, int pageSize) throws InstanceNotFoundException {
        // Obtener al usuario actual
        User currentUser = fetchUser(userID);
    
        // Obtener amigos del usuario
        Pageable pagination = PageRequest.of(page, pageSize);
        Slice<User> usersSlice = friendshipRepository.getFriendsByUser(userID, pagination);
        
        // Crear respuesta paginada
        Block<User> block = createBlockFromSlice(usersSlice);
        
        return block;
    }
    
    @Override
    public Friendship getFriendshipInfoWithUser(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, TargetUserIsCurrentUserException {
        // Comprobar que el usuario actual y el objetivo no sean el mismo
        if (requestorUserID.equals(targetUserID)) throw new TargetUserIsCurrentUserException();
    
        // Obtener a los usuarios
        User requestorUser = fetchUser(requestorUserID);
        User targetUser = fetchUser(targetUserID);
        Friendship friendship = getFriendshipBetweenUsers(requestorUser, targetUser);
        
        return friendship;
    }
    
    @Override
    public Group createGroup(UUID ownerID, String name) throws InstanceAlreadyExistsException,
                                                               InstanceNotFoundException {
        // Obtener al creador del grupo
        User groupOwner = fetchUser(ownerID);
        
        // Comprobar que el usuario actual no haya creado otro grupo con el mismo nombre
        if (groupRepository.existsByNameAndCreatorAllIgnoreCase(name, groupOwner))
            throw new InstanceAlreadyExistsException(Group.class.getName(), name);
        
        // Crear el grupo
        Group group = new Group();
        group.setCreator(groupOwner);
        group.setName(name);
        
        return groupRepository.save(group);
    }
    
    @Override
    public void deleteGroup(UUID ownerID, UUID groupID) throws InstanceNotFoundException {
        // Comprobar si existe el propietario del grupo
        User groupOwner = fetchUser(ownerID);
        
        // Comprobar si existe el grupo
        Group group = fetchGroup(groupID);
        
        // Eliminar el grupo
        groupRepository.deleteById(groupID);
    }
    
    @Override
    public Group updateGroupData(UUID ownerID, UUID groupId, Group groupData) throws InstanceNotFoundException {
        // Comprobar si existe el propietario del grupo
        User groupOwner = fetchUser(ownerID);
    
        // Comprobar si existe el grupo
        Group group = fetchGroup(groupId);
    
        // Comprobar qué datos se han modificado y actualizarlos
        if (groupData.getName() != null && !group.getName().equals(groupData.getName()))
            group.setName(groupData.getName());
        
        return groupRepository.save(group);
    }
    
    @Override
    public Block<User> getFriendsFromGroup(UUID groupID, int page, int pageSize) throws InstanceNotFoundException {
        // Comprobar si existe el grupo
        Group group = fetchGroup(groupID);
    
        // Obtener amigos del grupo
        Pageable pagination = PageRequest.of(page, pageSize);
        Slice<User> usersSlice = friendshipRepository.getFriendsByGroup(groupID, pagination);
    
        // Crear respuesta paginada
        Block<User> block = createBlockFromSlice(usersSlice);
    
        return block;
    }
    
    @Override
    public Friendship addFriendToGroup(UUID requestorUserID, UUID targetUserID, UUID groupID) throws InstanceNotFoundException, TargetUserIsCurrentUserException, InstanceAlreadyExistsException {
        // Comprobar que el usuario actual y el objetivo no sean el mismo
        if (requestorUserID.equals(targetUserID)) throw new TargetUserIsCurrentUserException();
        
        // Comprobar si existen los usuarios
        User groupOwner = fetchUser(requestorUserID);
        User targetUser = fetchUser(targetUserID);
    
        // Comprobar si existe el grupo
        Group group = fetchGroup(groupID);
        
        // Asignar grupo a la amistad entre ambos usuarios
        Friendship usersFriendship = getFriendshipBetweenUsers(groupOwner, targetUser);
        if (usersFriendship.getGroup() != null)
            throw new InstanceAlreadyExistsException(Group.class.getName(), targetUserID);
        usersFriendship.setGroup(group);
        
        return friendshipRepository.save(usersFriendship);
    }
    
    @Override
    public Friendship removeFriendFromGroup(UUID requestorUserID, UUID targetUserID, UUID groupID) throws InstanceNotFoundException, TargetUserIsCurrentUserException {
        // Comprobar que el usuario actual y el objetivo no sean el mismo
        if (requestorUserID.equals(targetUserID)) throw new TargetUserIsCurrentUserException();
    
        // Comprobar si existen los usuarios
        User groupOwner = fetchUser(requestorUserID);
        User targetUser = fetchUser(targetUserID);
    
        // Comprobar si existe el grupo
        Group group = fetchGroup(groupID);
    
        // Asignar grupo a la amistad entre ambos usuarios
        Friendship usersFriendship = getFriendshipBetweenUsers(groupOwner, targetUser);
        if (usersFriendship.getGroup() != null)
            throw new InstanceNotFoundException(targetUserID.toString(), group);
        usersFriendship.setGroup(null);
    
        return friendshipRepository.save(usersFriendship);
    }
    
    
    
    /* ****************************** AUX FUNCTIONS ****************************** */
    /** Busca un usuario en la base de datos. Lanza InstanceNotFound si no existe */
    private User fetchUser(UUID userID) throws InstanceNotFoundException {
        // Comprobar si existe el usuario con el ID recibido
        Optional<User> optionalUser = userRepository.findById(userID);
        if ( optionalUser.isEmpty() )
            throw new InstanceNotFoundException(User.class.getName(), userID);
        
        return optionalUser.get();
    }
    
    /** Busca un grupo en la base de datos. Lanza InstanceNotFound si no existe */
    private Group fetchGroup(UUID groupID) throws InstanceNotFoundException {
        // Comprobar si existe el grupo con el ID recibido
        Optional<Group> optionalGroup = groupRepository.findById(groupID);
        if ( optionalGroup.isEmpty() )
            throw new InstanceNotFoundException(Group.class.getName(), groupID);
        
        return optionalGroup.get();
    }
    
    private String capitalize(String string) {
        if (string == null) return null;
        if (string.isEmpty()) return string;
        
        String firstChar = string.substring(0, 1);
        String rest = string.substring(1);
        
        return firstChar.toUpperCase() + rest;
    }

    private Date getCurrentTime() {
        return Calendar.getInstance().getTime();
    }
    
    
    /** Obtiene una instancia de la amistad entre dos usuarios */
    private Friendship getFriendshipBetweenUsers(User requestorUser, User targetUser) {
        // Extraer los ID de los usuarios antes de realizar las operaciones
        UUID requestorID = requestorUser.getUserID();
        UUID targetID = targetUser.getUserID();
        
        // Como la clave primaria es compuesta, se instancia un objeto con los ID y se le pasa al repositorio
        FriendshipPK friendshipID = new FriendshipPK(requestorID, targetID);
        Optional<Friendship> optionalFriendship = friendshipRepository.findById(friendshipID);
        if ( optionalFriendship.isEmpty() ) return null;
        
        return optionalFriendship.get();
    }
    
    /** Crea una nueva relación entre dos usuarios */
    private Friendship createNewFriendship(User requestorUser, User targetUser) {
        Friendship friendship = new Friendship();
        FriendshipPK friendshipPK = new FriendshipPK(requestorUser.getUserID(), targetUser.getUserID());
        friendship.setId(friendshipPK);
        friendship.setSpecifier(requestorUser);
        friendship.setLastUpdate(getCurrentTime());
        
        return friendshipRepository.save(friendship);
    }
    
    /** Actualiza el estado de la amistad entre dos usuarios */
    private Friendship updateFriendhipStatus(Friendship friendship, User specifierUser, FriendshipStatusCodes status) {
        friendship.setStatus(status);
        friendship.setSpecifier(specifierUser);
        friendship.setLastUpdate(getCurrentTime());
        
        return friendship;
    }
    
    /** Crea un objeto Block a partir de un Slice */
    private <T> Block<T> createBlockFromSlice(Slice<T> slice) {
        Block<T> block = new Block<>();
        block.setItems(slice.getContent());
        block.setItemsCount(slice.getSize());
        block.setHasMoreItems(slice.hasNext());
        
        return block;
    }
}
