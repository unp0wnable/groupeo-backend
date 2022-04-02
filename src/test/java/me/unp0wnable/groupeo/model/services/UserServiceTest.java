package me.unp0wnable.groupeo.model.services;

import me.unp0wnable.groupeo.model.constants.FriendshipStatusCodes;
import me.unp0wnable.groupeo.model.entities.*;
import me.unp0wnable.groupeo.model.exceptions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static me.unp0wnable.groupeo.utils.TestDataGenerator.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    /* ****************************** TEST CASES ****************************** */
    /* *********************************** User profile *********************************** */
    @Test
    public void testSignUpAndLoginUsingID()
            throws InstanceAlreadyExistsException, InstanceNotFoundException {
        // Crear datos de prueba
        User createdUser = generateValidUser(DEFAULT_NICKNAME);
        
        
        // Ejecutar funcionalidades
        userService.signUp(createdUser);
        User loggedInUser = userService.loginFromServiceToken(createdUser.getUserID());
        
        
        // Comprobar resultados
        assertEquals(createdUser, loggedInUser);
    }
    
    @Test
    public void testSignUpAndLoginUsingIDHavingSomeUserFieldsNullOrEmpty()
            throws InstanceAlreadyExistsException, InstanceNotFoundException {
        // Crear datos de prueba
        User createdUser = generateValidUser(DEFAULT_NICKNAME);
        createdUser.setSurname1(null);
        createdUser.setSurname2("");
        
        // Ejecutar funcionalidades
        userService.signUp(createdUser);
        User loggedInUser = userService.loginFromServiceToken(createdUser.getUserID());
        
        
        // Comprobar resultados
        assertEquals(createdUser, loggedInUser);
    }
    
    @Test
    public void testSignUpTwice() throws InstanceAlreadyExistsException {
        // Crear datos de prueba
        User user1 = generateValidUser(DEFAULT_NICKNAME);
        User user2 = generateValidUser(DEFAULT_NICKNAME);
        
        // Ejecutar funcionalidades
        userService.signUp(user1);
        assertThrows(InstanceAlreadyExistsException.class,
            () -> userService.signUp(user2)
        );
    }
    
    @Test
    public void testLogin() throws InstanceAlreadyExistsException, IncorrectLoginException {
        // Crear datos de prueba
        User user = generateValidUser(DEFAULT_NICKNAME);
        String clearPassword = user.getPassword();
        
        // Ejecutar funcionalidades
        userService.signUp(user);
        User loggedInUser = userService.login(DEFAULT_NICKNAME, clearPassword);
        
        // Comprobar resultados
        assertEquals(user.getNickName(), loggedInUser.getNickName());
    }
    
    @Test
    public void testLoginWithIncorrectPassword() throws InstanceAlreadyExistsException {
        // Crear datos de prueba
        User user = generateValidUser(DEFAULT_NICKNAME);
        String clearPassword = user.getPassword();
        
        // Ejecutar funcionalidades
        userService.signUp(user);
        
        // Comprobar resultados
        assertThrows(IncorrectLoginException.class,
            () -> userService.login(DEFAULT_NICKNAME, clearPassword + 'X')
        );
    }
    
    @Test
    public void testLoginWithNonExistenUser() {
        // Crear datos de prueba
        User nonExistentUser = generateValidUser(NON_EXISTENT_NICKNAME);
        String clearPassword = nonExistentUser.getPassword();
        
        // Comprobar resultados
        assertThrows(IncorrectLoginException.class,
            () -> userService.login(NON_EXISTENT_NICKNAME, clearPassword)
        );
    }
    
    @Test
    public void testLoginNonExistentUserWithToken() {
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
            () -> userService.loginFromServiceToken(NON_EXISTENT_UUID)
        );
    }
    
    @Test
    public void testChangePassword()
            throws InstanceAlreadyExistsException, IncorrectPasswordExcepion, InstanceNotFoundException {
        // Generar datos
        User user = generateValidUser(DEFAULT_NICKNAME);
        String oldPassword = user.getPassword();
        String newPassword = user.getPassword() + 'X';
        
        // Ejecutar funcionalidades
        userService.signUp(user);
        userService.changePassword(user.getUserID(), oldPassword, newPassword);
        
        // Comprobar resultados
        assertDoesNotThrow(
            () -> userService.login(DEFAULT_NICKNAME, newPassword)
        );
    }
    
    @Test
    public void testChangePasswordWithIncorrectPassword() throws InstanceAlreadyExistsException {
        // Generar datos
        User user = generateValidUser(DEFAULT_NICKNAME);
        String oldPassword = user.getPassword();
        String newPassword = oldPassword + "X";
    
        // Ejecutar funcionalidades
        userService.signUp(user);
        
        // Comprobar resultados
        assertThrows(IncorrectPasswordExcepion.class,
            () -> userService.changePassword(user.getUserID(), "Y" + oldPassword, newPassword)
        );
    }
    
    @Test
    public void testChangePasswordToNonExistingUser() {
        // Generar datos
        String oldPassword = "old";
        String newPassword = "new";
        
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
            () -> userService.changePassword(NON_EXISTENT_UUID, oldPassword, newPassword)
        );
    }
    
    @Test
    public void testUpdateProfile() throws InstanceAlreadyExistsException, InstanceNotFoundException {
        // Generar datos
        User originalUser = generateValidUser(DEFAULT_NICKNAME);
        userService.signUp(originalUser);
        UUID userID = originalUser.getUserID();
        
        // Ejecutar funcionalidades
        originalUser.setFirstName(originalUser.getFirstName() + "X");
        originalUser.setSurname1(originalUser.getSurname1() + "X");
        originalUser.setSurname2("");
        originalUser.setEmail(originalUser.getEmail() + "X");
        originalUser.setDescription("A new description");
        userService.updateUserProfile(userID, originalUser);
        
        // Comprobar resultados
        User updatedUser = userService.loginFromServiceToken(userID);
        assertEquals(originalUser, updatedUser);
    }
    
    @Test
    public void testUpdateProfileOfNonExistentUser() {
        // Generar datos
        User user = generateValidUser(DEFAULT_NICKNAME);
        
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
            () -> userService.updateUserProfile(NON_EXISTENT_UUID, user)
        );
    }
    
    @Test
    public void testAssignAddressToUser() throws InstanceAlreadyExistsException, InstanceNotFoundException {
        // Generar datos
        User user = generateValidUser(DEFAULT_NICKNAME);
        userService.signUp(user);                           // Registrar usuario antes para obtener su ID
        UUID userID = user.getUserID();
        UserAddress generatedAddress = generateValidAddressForUser(user);
        
        // Ejecutar funcionalidades
        UserAddress assignedAddress = userService.assignAddressToUser(userID, generatedAddress);
        
        // Comprobar resultados
        assertEquals(assignedAddress.getUser(), user);
        assertEquals(generatedAddress.getCity(), assignedAddress.getCity());
        assertEquals(generatedAddress.getRegion(), assignedAddress.getRegion());
        assertEquals(generatedAddress.getPostalCode(), assignedAddress.getPostalCode());
        assertEquals(generatedAddress.getCountry(), assignedAddress.getCountry());
    }
    
    @Test
    public void testUpdateUserAddress() throws InstanceAlreadyExistsException, InstanceNotFoundException {
        // Generar datos
        User user = generateValidUser(DEFAULT_NICKNAME);
        userService.signUp(user);                           // Registrar usuario antes para obtener su ID
        UUID userID = user.getUserID();
        UserAddress generatedAddress = generateValidAddressForUser(user);
        UserAddress updatingAddress = userService.assignAddressToUser(userID, generatedAddress);
        
        // Ejecutar funcionalidades
        updatingAddress.setCountry(generatedAddress.getCountry() + 'X');
        updatingAddress.setCity(generatedAddress.getCity() + 'X');
        updatingAddress.setRegion(generatedAddress.getRegion() + 'X');
        updatingAddress.setPostalCode(generatedAddress.getPostalCode() + 'X');
        UserAddress updatedAddress = userService.assignAddressToUser(userID, updatingAddress);
        
        // Comprobar resultados
        assertEquals(updatingAddress, updatedAddress);
    }
    
    @Test
    public void testAssignAddressOfNonExistingUser() {
        // Generar datos
        User user = generateValidUser(DEFAULT_NICKNAME);
        UserAddress address = generateValidAddressForUser(user);
        
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
                     () -> userService.assignAddressToUser(NON_EXISTENT_UUID, address)
        );
    }
    
    @Test
    public void testDeleteUser() throws InstanceAlreadyExistsException, InstanceNotFoundException {
        // Generar datos
        User user = generateValidUser(DEFAULT_NICKNAME);
        
        // Ejecutar funcionalidades
        userService.signUp(user);
        UUID userId = user.getUserID();
        userService.deleteUser(userId);
        
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
            () -> userService.loginFromServiceToken(userId)
        );
    }
    
    @Test
    public void testDeleteNonExistingUser() {
        assertThrows(InstanceNotFoundException.class,
            () -> userService.deleteUser(NON_EXISTENT_UUID)
        );
    }
    
    
    /* *********************************** User relationships *********************************** */
    @Test
    public void testAddFriend()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   TargetUserIsAlreadyFriendException, BlockedUserException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
    
        // Ejecutar funcionalidades
        Friendship friendship = userService.addFriend(requestorID, targetID);
    
        // Comprobar resultados
        assertAll(
                () -> assertNull(friendship.getGroup()),
                () -> assertEquals(friendship.getSpecifier(), requestorUser)
        );
    }
    
    @Test
    public void testAddFriendToNonExistentUser() throws InstanceAlreadyExistsException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        
        // Ejecutar funcionalidades
        UUID requestorID = requestorUser.getUserID();
        
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
            () -> userService.addFriend(requestorID, NON_EXISTENT_UUID)
        );
    }
    
    @Test
    public void testAddFriendTwice()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   TargetUserIsAlreadyFriendException, BlockedUserException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        Friendship friendship = userService.addFriend(requestorID, targetID);
        
        // Comprobar resultados
        assertThrows(InstanceAlreadyExistsException.class,
            () -> userService.addFriend(requestorID, targetID)
        );
    }
    
    @Test
    public void testAddFriendSwitchingUserIDsDoesntCreateNewFriendship()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   TargetUserIsAlreadyFriendException, BlockedUserException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        Friendship friendship = userService.addFriend(requestorID, targetID);
        
        // Comprobar resultados
        assertAll(
                // Se crea una amistad correctamente
                () -> assertNotNull(friendship),
                // No se crea una nueva amistad entre los usuarios al intercambiar los ID
                () -> assertThrows(InstanceAlreadyExistsException.class,
                    () -> userService.addFriend(targetID, requestorID)
                )
        );
    }
    
    @Test
    public void testAddFriendToCurrentUser() throws InstanceAlreadyExistsException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        
        
        // Ejecutar funcionalidades
        UUID requestorID = requestorUser.getUserID();
        
        // Comprobar resultados
        assertThrows(TargetUserIsCurrentUserException.class,
                     () -> userService.addFriend(requestorID, requestorID)
        );
    }
    
    @Test
    public void testAddFriendToAlreadyRequestedFriend()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   TargetUserIsAlreadyFriendException, BlockedUserException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        
        // Ejecutar funcionalidades
        Friendship friendship = userService.addFriend(requestorID, targetID);
        
        // Comprobar resultados
        assertAll(
            () -> assertThrows(
                TargetUserIsCurrentUserException.class,
                () -> userService.addFriend(requestorID, requestorID)
            ),
            () -> assertNotNull(friendship)
        );
    }
    
    @Test
    public void testAddFriendToAlreadyAcceptedFriend()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   TargetUserIsAlreadyFriendException, BlockedUserException, NonExistentFriendshipException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        
        // Ejecutar funcionalidades
        Friendship beforeRequestFriendship = userService.addFriend(requestorID, targetID);
        Friendship aceptedFriendship = userService.acceptFriendshipRequest(targetID, requestorID);
        
        
        // Comprobar resultados
        assertAll(
                // Comprobar que se ha creado la petición de amistad
                () -> assertNotNull(beforeRequestFriendship),
                // Comprobar que se ha aceptado la petición de amistad
                () -> assertNotNull(aceptedFriendship),
                // Comprobar que no se puede enviar petición a un usuario que ya es amigo
                () -> assertThrows(TargetUserIsAlreadyFriendException.class,
                        () -> userService.addFriend(requestorID, targetID)
                )
        );
    }
    
    @Test
    public void testAddFriendToUserThatDeclinedFriendshipEarlier()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   TargetUserIsAlreadyFriendException, BlockedUserException, NonExistentFriendshipException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        
        // Ejecutar funcionalidades
        Friendship firstRequestedFriendship = userService.addFriend(requestorID, targetID);
        // TargetUser es quien rechaza la amistad del usuario actual, por eso los ID están intercambiados
        Friendship firstDeclinedFriendship = userService.declineFriendshipRequest(requestorID, targetID);
        Friendship secondRequestedFriendship = userService.addFriend(requestorID, targetID);
        
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(firstRequestedFriendship),
                () -> assertNotNull(firstDeclinedFriendship),
                () -> assertNotNull(secondRequestedFriendship),
                () -> assertEquals(secondRequestedFriendship.getSpecifier(), requestorUser),
                () -> assertEquals(FriendshipStatusCodes.REQUESTED, secondRequestedFriendship.getStatus())
        );
    }
    
    @Test
    public void testAddFriendToBlockedUser()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        
        // Ejecutar funcionalidades
        Friendship friendship = userService.blockFriend(requestorID, targetID);
        
        // Comprobar resultados
        assertAll(
                () -> assertThrows(BlockedUserException.class,
                        () -> userService.addFriend(requestorID, targetID)
                ),
                () -> assertNotNull(friendship)
        );
    }
    
    @Test
    public void testRemoveFriend()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   BlockedUserException, TargetUserIsAlreadyFriendException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        Friendship friendship = userService.addFriend(requestorID, targetID);
        
        // Comprobar resultados
        assertAll(
                // Comprobar que se creó una relación de amistad entre usuarios antes de poder eliminarse como amigos
                () -> assertNotNull(friendship),
                // Comprobar que ya no existe esta relación
                () -> assertDoesNotThrow(() -> userService.removeFriend(requestorID, targetID))
        );
    }
    
    @Test
    public void testRemoveFriendTwice()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   TargetUserIsAlreadyFriendException, BlockedUserException, TargetUserIsNotFriendException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        Friendship friendship = userService.addFriend(requestorID, targetID);
        userService.removeFriend(requestorID, targetID);
        
        // Comprobar resultados
        assertThrows(TargetUserIsNotFriendException.class,
            () -> userService.removeFriend(requestorID, targetID)
        );
    }
    
    @Test
    public void testRemoveFriendSwitchingUserIDs()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   TargetUserIsAlreadyFriendException, BlockedUserException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        Friendship friendship = userService.addFriend(requestorID, targetID);
        
        // Comprobar resultados
        assertAll(
            // Se crea una amistad correctamente
            () -> assertNotNull(friendship),
            // No se crea una nueva amistad entre los usuarios al intercambiar los ID
            () -> assertDoesNotThrow(() -> userService.removeFriend(targetID, requestorID))
        );
    }
    
    @Test
    public void testRemoveFriendToNonExistentUser() throws InstanceAlreadyExistsException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        
        
        // Ejecutar funcionalidades
        UUID requestorID = requestorUser.getUserID();
        
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
                     () -> userService.removeFriend(requestorID, NON_EXISTENT_UUID)
        );
    }
    
    @Test
    public void testRemoveFriendToCurrentUser() throws InstanceAlreadyExistsException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        
        
        // Ejecutar funcionalidades
        UUID requestorID = requestorUser.getUserID();
        
        // Comprobar resultados
        assertThrows(TargetUserIsCurrentUserException.class,
                     () -> userService.removeFriend(requestorID, requestorID)
        );
    }
    
    @Test
    public void testRemoveFriendToNonFriendUser() throws InstanceAlreadyExistsException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        
        // Ejecutar funcionalidades
        
        // Comprobar resultados
        assertThrows(TargetUserIsNotFriendException.class,
                     () -> userService.removeFriend(requestorID, targetID)
        );
    }
    
    @Test
    public void testRemoveFriendToBlockedUser()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        
        // Ejecutar funcionalidades
        Friendship friendship = userService.blockFriend(requestorID, targetID);
        
        // Comprobar resultados
        assertAll(
                () -> assertThrows(
                        BlockedUserException.class,
                        () -> userService.removeFriend(requestorID, targetID)
                ),
                () -> assertNotNull(friendship)
        );
    }
    
    @Test
    public void testAcceptFriendshipRequest()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   BlockedUserException, TargetUserIsAlreadyFriendException, NonExistentFriendshipException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        Friendship preRequestFriendship = userService.addFriend(requestorID, targetID);
        Friendship postRequestFriendship = userService.acceptFriendshipRequest(targetID, requestorID);
        
        
        // Comprobar resultados
        assertAll(
                // Comprobar que se creó una relación de amistad entre usuarios
                () -> assertNotNull(preRequestFriendship),
                // Comprobar que se sigue existiendo dicha amistad entre usuarios,
                () -> assertNotNull(postRequestFriendship),
                // Comprobar que la amistad se ha aceptado por el usuario que recibio la peticion (Target)
                () -> assertEquals(FriendshipStatusCodes.ACCEPTED, postRequestFriendship.getStatus()),
                () -> assertEquals(postRequestFriendship.getSpecifier(), targetUser),
                () -> assertNull(postRequestFriendship.getGroup())
        );
    }
    
    @Test
    public void testAcceptFriendshipRequestTwice()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   BlockedUserException, TargetUserIsAlreadyFriendException, NonExistentFriendshipException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        Friendship preRequestFriendship = userService.addFriend(requestorID, targetID);
        Friendship postRequestFriendship = userService.acceptFriendshipRequest(targetID, requestorID);
        
        
        // Comprobar resultados
        assertAll(
                // Comprobar que se creó una relación de amistad entre usuarios
                () -> assertNotNull(preRequestFriendship),
                // Comprobar que no se puede aceptar la petición dos veces
                () -> assertThrows(TargetUserIsAlreadyFriendException.class,
                    () -> userService.acceptFriendshipRequest(targetID, requestorID)
                )
        );
    }
    
    @Test
    public void testAcceptFriendshipRequestFromNonExistentUser()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   BlockedUserException, TargetUserIsAlreadyFriendException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        Friendship preRequestFriendship = userService.addFriend(requestorID, targetID);
        
        // Comprobar resultados
        assertAll(
                // Comprobar que se creó una relación de amistad entre usuarios
                () -> assertNotNull(preRequestFriendship),
                () -> assertThrows(InstanceNotFoundException.class,
                    () -> userService.acceptFriendshipRequest(targetID, NON_EXISTENT_UUID)
                )
        );
    }
    
    
    @Test
    public void testAcceptNonExistentFriendshipRequest() throws InstanceAlreadyExistsException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Comprobar resultados
        assertThrows(NonExistentFriendshipException.class,
                     () -> userService.acceptFriendshipRequest(targetID, requestorID)
        );
    }
    
    @Test
    public void testDeclineFriendshipRequest()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   BlockedUserException, TargetUserIsAlreadyFriendException, NonExistentFriendshipException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        Friendship preRequestFriendship = userService.addFriend(requestorID, targetID);
        Friendship postRequestFriendship = userService.declineFriendshipRequest(targetID, requestorID);
        
        
        // Comprobar resultados
        assertAll(
                // Comprobar que se creó una relación de amistad entre usuarios
                () -> assertNotNull(preRequestFriendship),
                // Comprobar que se sigue existiendo dicha amistad entre usuarios,
                () -> assertNotNull(postRequestFriendship),
                // Comprobar que la amistad se ha aceptado por el usuario que recibio la peticion (Target)
                () -> assertEquals(FriendshipStatusCodes.DECLINED, postRequestFriendship.getStatus()),
                () -> assertEquals(postRequestFriendship.getSpecifier(), targetUser),
                () -> assertNull(postRequestFriendship.getGroup())
        );
    }
    
    @Test
    public void testDeclineNonExistentFriendshipRequest() throws InstanceAlreadyExistsException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Comprobar resultados
        assertThrows(NonExistentFriendshipException.class, () -> userService.declineFriendshipRequest(requestorID, targetID));
    }
    
    
    @Test
    public void testBlockFriend()
            throws TargetUserIsCurrentUserException, InstanceNotFoundException, InstanceAlreadyExistsException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        Friendship friendship = userService.blockFriend(requestorID, targetID);
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(friendship),
                () -> assertNull(friendship.getGroup()),
                () -> assertEquals(friendship.getSpecifier(), requestorUser),
                () -> assertEquals(FriendshipStatusCodes.BLOCKED, friendship.getStatus())
        );
    }
    
    
    @Test
    public void testBlockCurrentUser() throws InstanceAlreadyExistsException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        
        // Comprobar resultados
        assertThrows(TargetUserIsCurrentUserException.class,
                     () -> userService.blockFriend(requestorID, requestorID)
        );
    }
    
    @Test
    public void testBlockFriendTwice()
            throws TargetUserIsCurrentUserException, InstanceNotFoundException, InstanceAlreadyExistsException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        Friendship firstBlockFriendship = userService.blockFriend(requestorID, targetID);
        Friendship secondBlockFriendship = userService.blockFriend(requestorID, targetID);
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(firstBlockFriendship),
                () -> assertNotNull(secondBlockFriendship),
                () -> assertEquals(firstBlockFriendship.getSpecifier(), requestorUser),
                () -> assertEquals(secondBlockFriendship.getSpecifier(), requestorUser),
                () -> assertEquals(FriendshipStatusCodes.BLOCKED, firstBlockFriendship.getStatus()),
                () -> assertEquals(FriendshipStatusCodes.BLOCKED, secondBlockFriendship.getStatus())
        );
    }
    
    @Test
    public void testUnblockFriend()
            throws TargetUserIsCurrentUserException, InstanceNotFoundException, InstanceAlreadyExistsException,
                   NonExistentFriendshipException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        Friendship blockedFrienship = userService.blockFriend(requestorID, targetID);
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(blockedFrienship),
                () -> assertEquals(blockedFrienship.getSpecifier(), requestorUser),
                () -> assertEquals(FriendshipStatusCodes.BLOCKED, blockedFrienship.getStatus()),
                () -> assertDoesNotThrow(() -> userService.unblockFriend(requestorID, targetID))
        );
    }
    
    @Test
    public void testUnblockFriendTwice()
            throws TargetUserIsCurrentUserException, InstanceNotFoundException, InstanceAlreadyExistsException,
                   NonExistentFriendshipException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        Friendship blockedFrienship = userService.blockFriend(requestorID, targetID);
        userService.unblockFriend(requestorID, targetID);
        
        // Comprobar resultados
        assertThrows(NonExistentFriendshipException.class,
                     () -> userService.unblockFriend(requestorID, targetID)
        );
    }
    
    @Test
    public void testGetBlockedUsers()
            throws TargetUserIsCurrentUserException, InstanceNotFoundException, InstanceAlreadyExistsException {
        // Crear datos de prueba
        final int AMMOUNT_OF_BLOCKED_USERS = 5;
        final int BLOCK_PAGE = 0;
        final int BLOCK_PAGE_SIZE = 5;
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        List<User> targetUsersList = registerMultipleUsers(AMMOUNT_OF_BLOCKED_USERS, this.userService);
        
        // Ejecutar funcionalidades
        for (User targetUser: targetUsersList) {            // Bloquea a todos los usuarios generados
            userService.blockFriend(requestorID, targetUser.getUserID());
        }
        Block<User> blockedUsersBlock = userService.getBlockedUsers(requestorID, BLOCK_PAGE, BLOCK_PAGE_SIZE);
        
        // Comprobar resultados
        assertAll(
                () -> assertEquals(AMMOUNT_OF_BLOCKED_USERS, blockedUsersBlock.getItemsCount()),
                () -> assertFalse(blockedUsersBlock.hasMoreItems())
        );
    }
    
    @Test
    public void testGetBlockedUsersWithPagination()
            throws TargetUserIsCurrentUserException, InstanceNotFoundException, InstanceAlreadyExistsException {
        // Crear datos de prueba
        final int AMMOUNT_OF_BLOCKED_USERS = 6;
        final int BLOCK_PAGE = 0;
        final int BLOCK_PAGE_SIZE = 5;
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        List<User> targetUsersList = registerMultipleUsers(AMMOUNT_OF_BLOCKED_USERS, this.userService);
        
        // Ejecutar funcionalidades
        for (User targetUser: targetUsersList) {            // Bloquea a todos los usuarios generados
            userService.blockFriend(requestorID, targetUser.getUserID());
        }
        Block<User> blockedUsersBlock = userService.getBlockedUsers(requestorID, BLOCK_PAGE, BLOCK_PAGE_SIZE);
        Block<User> blockedUsersNextPageBlock = userService.getBlockedUsers(requestorID, BLOCK_PAGE + 1, BLOCK_PAGE_SIZE);
        
        // Comprobar resultados
        assertAll(
                // Comprobar que la primera página contiene resultados
                () -> assertEquals(BLOCK_PAGE_SIZE, blockedUsersBlock.getItemsCount()),
                () -> assertTrue(blockedUsersBlock.hasMoreItems()),
                // Comprobar que la segunda página contiene la cantidad de elementos correcta
                () -> assertEquals(AMMOUNT_OF_BLOCKED_USERS % BLOCK_PAGE_SIZE, blockedUsersNextPageBlock.getItemsCount()),
                () -> assertFalse(blockedUsersNextPageBlock.hasMoreItems())
        );
    }
    
    @Test
    public void testGetBlockedUsersReturnsEmptyBlock() throws InstanceNotFoundException, InstanceAlreadyExistsException {
        // Crear datos de prueba
        final int AMMOUNT_OF_BLOCKED_USERS = 0;
        final int BLOCK_PAGE = 0;
        final int BLOCK_PAGE_SIZE = 5;
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        
        Block<User> emptyBlockedUsersBlock = userService.getBlockedUsers(requestorID, BLOCK_PAGE, BLOCK_PAGE_SIZE);
        
        // Comprobar resultados
        assertAll(
                // Comprobar que la primera página contiene resultados
                () -> assertEquals(AMMOUNT_OF_BLOCKED_USERS, emptyBlockedUsersBlock.getItemsCount()),
                () -> assertFalse(emptyBlockedUsersBlock.hasMoreItems())
        );
    }
    
    @Test
    public void testGetBlockedUsersFromNonExistentUser() {
        // Crear datos de prueba
        final int BLOCK_PAGE = 0;
        final int BLOCK_PAGE_SIZE = 5;
    
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
                     () -> userService.getBlockedUsers(NON_EXISTENT_UUID, BLOCK_PAGE, BLOCK_PAGE_SIZE)
        );
    }
    
    @Test
    public void testGetUserFriends()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   TargetUserIsAlreadyFriendException, BlockedUserException, NonExistentFriendshipException {
        // Crear datos de prueba
        final int AMMOUNT_OF_FRIENDS = 5;
        final int BLOCK_PAGE = 0;
        final int BLOCK_PAGE_SIZE = 5;
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        List<User> targetUsersList = registerMultipleUsers(AMMOUNT_OF_FRIENDS, this.userService);
        
        // Ejecutar funcionalidades
        for (User targetUser: targetUsersList) {            // Bloquea a todos los usuarios generados
            userService.addFriend(requestorID, targetUser.getUserID());
            userService.acceptFriendshipRequest(targetUser.getUserID(), requestorID);
        }
        Block<User> userFriendsBlock = userService.getUserFriends(requestorID, BLOCK_PAGE, BLOCK_PAGE_SIZE);
        
        // Comprobar resultados
        assertAll(
                () -> assertEquals(AMMOUNT_OF_FRIENDS, userFriendsBlock.getItemsCount()),
                () -> assertFalse(userFriendsBlock.hasMoreItems())
        );
    }
    
    @Test
    public void testGetUserFriendsWithPagination()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   TargetUserIsAlreadyFriendException, BlockedUserException, NonExistentFriendshipException {
        // Crear datos de prueba
        final int AMMOUNT_OF_FRIENDS = 7;
        final int BLOCK_PAGE = 0;
        final int BLOCK_PAGE_SIZE = 5;
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        List<User> targetUsersList = registerMultipleUsers(AMMOUNT_OF_FRIENDS, this.userService);
        
        // Ejecutar funcionalidades
        for (User targetUser: targetUsersList) {            // Bloquea a todos los usuarios generados
            userService.addFriend(requestorID, targetUser.getUserID());
            userService.acceptFriendshipRequest(targetUser.getUserID(), requestorID);
        }
        Block<User> userFriendsBlock = userService.getUserFriends(requestorID, BLOCK_PAGE, BLOCK_PAGE_SIZE);
        Block<User> userFriendsNextBlock = userService.getUserFriends(requestorID, BLOCK_PAGE + 1, BLOCK_PAGE_SIZE);
        
        // Comprobar resultados
        assertAll(
                // Comprobar que la primera página contiene resultados
                () -> assertEquals(BLOCK_PAGE_SIZE, userFriendsBlock.getItemsCount()),
                () -> assertTrue(userFriendsBlock.hasMoreItems()),
                // Comprobar que la segunda página contiene la cantidad de elementos correcta
                () -> assertEquals(AMMOUNT_OF_FRIENDS % BLOCK_PAGE_SIZE, userFriendsNextBlock.getItemsCount()),
                () -> assertFalse(userFriendsNextBlock.hasMoreItems())
        );
    }
    
    @Test
    public void testGetFriendshipDataWithCurrentUser() throws InstanceAlreadyExistsException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        
        // Comprobar resultados
        assertThrows(TargetUserIsCurrentUserException.class,
                     () -> userService.getFriendshipInfoWithUser(requestorID, requestorID)
        );
    }
    
    @Test
    public void testGetFriendshipDataWithNonRelatedUser()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        Friendship friendshipData = userService.getFriendshipInfoWithUser(requestorID, targetID);
        
        // Comprobar resultados
        assertNull(friendshipData);
    }
    
    @Test
    public void testGetFriendshipDataWithRequestedFriendshipUser()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   TargetUserIsAlreadyFriendException, BlockedUserException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        userService.addFriend(requestorID, targetID);
        Friendship friendshipData = userService.getFriendshipInfoWithUser(requestorID, targetID);
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(friendshipData),
                () -> assertEquals(FriendshipStatusCodes.REQUESTED, friendshipData.getStatus()),
                () -> assertEquals(friendshipData.getSpecifier(), requestorUser),
                () -> assertNull(friendshipData.getGroup())
        );
    }
    
    @Test
    public void testGetFriendshipDataWithFriend()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   TargetUserIsAlreadyFriendException, BlockedUserException, NonExistentFriendshipException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        userService.addFriend(requestorID, targetID);
        userService.acceptFriendshipRequest(targetID, requestorID);
        Friendship friendshipData = userService.getFriendshipInfoWithUser(requestorID, targetID);
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(friendshipData),
                () -> assertEquals(FriendshipStatusCodes.ACCEPTED, friendshipData.getStatus()),
                () -> assertEquals(friendshipData.getSpecifier(), targetUser),
                () -> assertNull(friendshipData.getGroup())
        );
    }
    
    @Test
    public void testGetFriendshipDataAfterDeclineFriendshipRequest()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   TargetUserIsAlreadyFriendException, BlockedUserException, NonExistentFriendshipException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        userService.addFriend(requestorID, targetID);
        userService.declineFriendshipRequest(targetID, requestorID);
        Friendship friendshipData = userService.getFriendshipInfoWithUser(requestorID, targetID);
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(friendshipData),
                () -> assertEquals(FriendshipStatusCodes.DECLINED, friendshipData.getStatus()),
                () -> assertEquals(friendshipData.getSpecifier(), targetUser),
                () -> assertNull(friendshipData.getGroup())
        );
    }
    
    @Test
    public void testGetFriendshipDataWithBlockedUser()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        userService.blockFriend(requestorID, targetID);
        Friendship friendshipData = userService.getFriendshipInfoWithUser(requestorID, targetID);
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(friendshipData),
                () -> assertEquals(FriendshipStatusCodes.BLOCKED, friendshipData.getStatus()),
                () -> assertEquals(friendshipData.getSpecifier(), requestorUser),
                () -> assertNull(friendshipData.getGroup())
        );
    }
    
    @Test
    public void testGetFriendshipDataBlockingUserAfterRequestingFriendship()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   TargetUserIsAlreadyFriendException, BlockedUserException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        userService.addFriend(requestorID, targetID);
        userService.blockFriend(requestorID, targetID);
        Friendship friendshipData = userService.getFriendshipInfoWithUser(requestorID, targetID);
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(friendshipData),
                () -> assertEquals(FriendshipStatusCodes.BLOCKED, friendshipData.getStatus()),
                () -> assertEquals(friendshipData.getSpecifier(), requestorUser),
                () -> assertNull(friendshipData.getGroup())
        );
    }
    
    @Test
    public void testCreateGroup() throws InstanceAlreadyExistsException, InstanceNotFoundException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        String GROUP_NAME = "testGroup";
        
        // Ejecutar funcionalidades
        Group group = userService.createGroup(requestorID, GROUP_NAME);
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(group),
                () -> assertEquals(group.getName(), GROUP_NAME),
                () -> assertEquals(group.getCreator(), requestorUser)
        );
    }
    
    @Test
    public void testCreateGroupTwice() throws InstanceAlreadyExistsException, InstanceNotFoundException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        String GROUP_NAME = "testGroup";
        
        // Ejecutar funcionalidades
        Group group = userService.createGroup(requestorID, GROUP_NAME);
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(group),
                () -> assertThrows(InstanceAlreadyExistsException.class,
                    () -> userService.createGroup(requestorID, GROUP_NAME)
                )
        );
    }
    
    @Test
    public void testCreateGroupByNonExistentUser() {
        // Crear datos de prueba
        String GROUP_NAME = "testGroup";
        
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class, () -> userService.createGroup(NON_EXISTENT_UUID, GROUP_NAME));
    }
    
    @Test
    public void testDeleteGroup() throws InstanceAlreadyExistsException, InstanceNotFoundException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        String GROUP_NAME = "testGroup";
        
        // Ejecutar funcionalidades
        Group group = userService.createGroup(requestorID, GROUP_NAME);
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(group),
                () -> assertDoesNotThrow(() -> userService.deleteGroup(requestorID, group.getGroupID()))
        );
    }
    
    @Test
    public void testDeleteNonExistentGroup() throws InstanceAlreadyExistsException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        String GROUP_NAME = "testGroup";
    
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
            () -> userService.deleteGroup(requestorID, NON_EXISTENT_UUID)
        );
    }
    
    @Test
    public void testDeleteNonExistentGroupByNonExistentUser() throws InstanceAlreadyExistsException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        String GROUP_NAME = "testGroup";
        
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
                     () -> userService.deleteGroup(NON_EXISTENT_UUID, NON_EXISTENT_UUID)
        );
    }
    
    @Test
    public void testUpdateGroupData() throws InstanceAlreadyExistsException, InstanceNotFoundException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        String GROUP_NAME = "testGroup";
        
        // Ejecutar funcionalidades
        Group originalGroup = userService.createGroup(requestorID, GROUP_NAME);
        Group modifiedGroup = originalGroup;
        modifiedGroup.setName(GROUP_NAME + "X");
        Group updatedGroup = userService.updateGroupData(requestorID, originalGroup.getGroupID(), modifiedGroup);
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(originalGroup),
                () -> assertNotNull(updatedGroup),
                () -> assertEquals(updatedGroup.getCreator(), requestorUser),
                () -> assertEquals(updatedGroup.getGroupID(), originalGroup.getGroupID()),
                () -> assertEquals(updatedGroup.getName(), GROUP_NAME + "X")
        );
    }
    
    @Test
    public void testUpdateGroupDataWithoutChangingData() throws InstanceAlreadyExistsException, InstanceNotFoundException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        String GROUP_NAME = "testGroup";
        
        // Ejecutar funcionalidades
        Group originalGroup = userService.createGroup(requestorID, GROUP_NAME);
        Group updatedGroup = userService.updateGroupData(requestorID, originalGroup.getGroupID(), originalGroup);
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(originalGroup),
                () -> assertNotNull(updatedGroup),
                () -> assertEquals(originalGroup, updatedGroup)
        );
    }
    
    @Test
    public void testUpdateNonExistentGroupData() throws InstanceAlreadyExistsException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        String GROUP_NAME = "testGroup";
        
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
            () -> userService.updateGroupData(requestorID, NON_EXISTENT_UUID, new Group())
        );
    }
    
    @Test
    public void testUpdateGroupDataByNonExistentUser() throws InstanceAlreadyExistsException,
                                                              InstanceNotFoundException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        String GROUP_NAME = "testGroup";
    
        // Ejecutar funcionalidades
        Group group = userService.createGroup(requestorID, GROUP_NAME);
        
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
                     () -> userService.updateGroupData(NON_EXISTENT_UUID, group.getGroupID(), new Group())
        );
    }
    
    @Test
    public void testAddFriendToGroup()
            throws InstanceAlreadyExistsException, InstanceNotFoundException, TargetUserIsCurrentUserException,
                   TargetUserIsAlreadyFriendException, BlockedUserException, NonExistentFriendshipException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        String GROUP_NAME = "testGroup";
        
        // Ejecutar funcionalidades
        Group group = userService.createGroup(requestorID, GROUP_NAME);
        userService.addFriend(requestorID, targetID);
        Friendship friendshipBeforeGroup = userService.acceptFriendshipRequest(targetID, requestorID);
        Friendship friendshipAfterGroup = userService.addFriendToGroup(requestorID, targetID, group.getGroupID());
        
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(group),
                () -> assertNotNull(friendshipBeforeGroup),
                () -> assertNotNull(friendshipAfterGroup),
                () -> assertEquals(group.getCreator(), requestorUser),
                () -> assertEquals(friendshipAfterGroup.getGroup(), group)
        );
    }
    
    @Test
    public void testAddFriendToNonExistentGroup()
            throws InstanceAlreadyExistsException, InstanceNotFoundException, TargetUserIsCurrentUserException,
                   TargetUserIsAlreadyFriendException, BlockedUserException, NonExistentFriendshipException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        userService.addFriend(requestorID, targetID);
        userService.acceptFriendshipRequest(targetID, requestorID);
        
        
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
            () -> userService.addFriendToGroup(requestorID, targetID, NON_EXISTENT_UUID)
        );
    }
    
    @Test
    public void testAddNonExistentFriendToGroup()
            throws InstanceAlreadyExistsException, InstanceNotFoundException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        String GROUP_NAME = "testGroup";
        
        // Ejecutar funcionalidades
        Group group = userService.createGroup(requestorID, GROUP_NAME);
        
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
                     () -> userService.addFriendToGroup(requestorID, NON_EXISTENT_UUID, group.getGroupID())
        );
    }
    
    @Test
    public void testAddCurrentUserToGroup()
            throws InstanceAlreadyExistsException, InstanceNotFoundException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        String GROUP_NAME = "testGroup";
        
        // Ejecutar funcionalidades
        Group group = userService.createGroup(requestorID, GROUP_NAME);
        
        // Comprobar resultados
        assertThrows(TargetUserIsCurrentUserException.class,
                     () -> userService.addFriendToGroup(requestorID, requestorID, group.getGroupID())
        );
    }
    
    @Test
    public void testAddNonFriendToGroup()
            throws InstanceAlreadyExistsException, InstanceNotFoundException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        String GROUP_NAME = "testGroup";
        
        // Ejecutar funcionalidades
        Group group = userService.createGroup(requestorID, GROUP_NAME);
        
        // Comprobar resultados
        assertThrows(NonExistentFriendshipException.class,
                     () -> userService.addFriendToGroup(requestorID, targetID, group.getGroupID())
        );
    }
    
    @Test
    public void testAddBlockedUserToGroup()
            throws InstanceAlreadyExistsException, InstanceNotFoundException, TargetUserIsCurrentUserException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        String GROUP_NAME = "testGroup";
        
        // Ejecutar funcionalidades
        Group group = userService.createGroup(requestorID, GROUP_NAME);
        userService.blockFriend(targetID, requestorID);
        
        // Comprobar resultados
        assertThrows(BlockedUserException.class,
                     () -> userService.addFriendToGroup(requestorID, targetID, group.getGroupID())
        );
    }
    
    @Test
    public void testAddFriendToGroupTwice()
            throws InstanceAlreadyExistsException, InstanceNotFoundException, TargetUserIsCurrentUserException,
                   TargetUserIsAlreadyFriendException, BlockedUserException, NonExistentFriendshipException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        String GROUP_NAME = "testGroup";
        
        // Ejecutar funcionalidades
        Group group = userService.createGroup(requestorID, GROUP_NAME);
        userService.addFriend(requestorID, targetID);
        userService.acceptFriendshipRequest(targetID, requestorID);
        Friendship usersFriendship = userService.addFriendToGroup(requestorID, targetID, group.getGroupID());
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(usersFriendship),
                () -> assertEquals(usersFriendship.getGroup(), group),
                () -> assertThrows(InstanceAlreadyExistsException.class,
                    () -> userService.addFriendToGroup(requestorID, targetID, group.getGroupID())
                )
        );
    }
    
    @Test
    public void testAddMultipleFriendsToSameGroup()
            throws InstanceAlreadyExistsException, InstanceNotFoundException, TargetUserIsCurrentUserException,
                   TargetUserIsAlreadyFriendException, BlockedUserException, NonExistentFriendshipException {
        // Crear datos de prueba
        final int AMMOUNT_OF_FRIENDS = 4;
        final int BLOCK_PAGE = 0;
        final int BLOCK_PAGE_SIZE = 5;
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        List<User> friendsList = registerMultipleUsers(AMMOUNT_OF_FRIENDS, this.userService);
        String GROUP_NAME = "testGroup";
        
        // Ejecutar funcionalidades
        Group group = userService.createGroup(requestorID, GROUP_NAME);
        for (User targetUser: friendsList) {            // Bloquea a todos los usuarios generados
            userService.addFriend(requestorID, targetUser.getUserID());
            userService.acceptFriendshipRequest(targetUser.getUserID(), requestorID);
            userService.addFriendToGroup(requestorID, targetUser.getUserID(), group.getGroupID());
        }
        Block<User> friendsInGroupBlock = userService.getFriendsFromGroup(group.getGroupID(), BLOCK_PAGE, BLOCK_PAGE_SIZE);
        
        
        // Comprobar resultados
        assertAll(
                () -> assertFalse(friendsInGroupBlock.hasMoreItems()),
                () -> assertEquals(friendsInGroupBlock.getItemsCount(), AMMOUNT_OF_FRIENDS)
        );
    }
    
    @Test
    public void testRemoveFriendFromGroup()
            throws InstanceAlreadyExistsException, InstanceNotFoundException, TargetUserIsCurrentUserException,
                   TargetUserIsAlreadyFriendException, BlockedUserException, NonExistentFriendshipException,
                   UserNotInGroupException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        String GROUP_NAME = "testGroup";
        
        // Ejecutar funcionalidades
        Group group = userService.createGroup(requestorID, GROUP_NAME);
        userService.addFriend(requestorID, targetID);
        userService.acceptFriendshipRequest(targetID, requestorID);
        userService.addFriendToGroup(requestorID, targetID, group.getGroupID());
        Friendship friendshipOutsideGroup = userService.removeFriendFromGroup(requestorID, targetID, group.getGroupID());
        
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(group),
                () -> assertNotNull(friendshipOutsideGroup),
                () -> assertEquals(group.getCreator(), requestorUser),
                () -> assertNull(friendshipOutsideGroup.getGroup())
        );
    }
    @Test
    public void testRemoveFriendFromNonExistentGroup()
            throws InstanceAlreadyExistsException, InstanceNotFoundException, TargetUserIsCurrentUserException,
                   TargetUserIsAlreadyFriendException, BlockedUserException, NonExistentFriendshipException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        
        // Ejecutar funcionalidades
        userService.addFriend(requestorID, targetID);
        userService.acceptFriendshipRequest(targetID, requestorID);
        
        
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
                     () -> userService.removeFriendFromGroup(requestorID, targetID, NON_EXISTENT_UUID)
        );
    }
    
    @Test
    public void testRemoveFriendFromGroupTwice()
            throws InstanceAlreadyExistsException, InstanceNotFoundException, TargetUserIsCurrentUserException,
                   TargetUserIsAlreadyFriendException, BlockedUserException, NonExistentFriendshipException,
                   UserNotInGroupException {
        // Crear datos de prueba
        User requestorUser = registerValidUser("Requestor", this.userService);
        UUID requestorID = requestorUser.getUserID();
        User targetUser = registerValidUser("Target", this.userService);
        UUID targetID = targetUser.getUserID();
        String GROUP_NAME = "testGroup";
        
        // Ejecutar funcionalidades
        Group group = userService.createGroup(requestorID, GROUP_NAME);
        userService.addFriend(requestorID, targetID);
        userService.acceptFriendshipRequest(targetID, requestorID);
        userService.addFriendToGroup(requestorID, targetID, group.getGroupID());
        Friendship usersFriendship = userService.removeFriendFromGroup(requestorID, targetID, group.getGroupID());
        
        // Comprobar resultados
        assertAll(
                () -> assertNotNull(usersFriendship),
                () -> assertNull(usersFriendship.getGroup()),
                () -> assertThrows(UserNotInGroupException.class,
                                   () -> userService.removeFriendFromGroup(requestorID, targetID, group.getGroupID())
                )
        );
    }
    
}
