package me.unp0wnable.groupeo.model.services;

import me.unp0wnable.groupeo.model.constants.FriendshipStatusCodes;
import me.unp0wnable.groupeo.model.entities.*;
import me.unp0wnable.groupeo.model.exceptions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
            () -> userService.loginFromServiceToken(NON_EXISTENT_USER_ID)
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
            () -> userService.changePassword(NON_EXISTENT_USER_ID, oldPassword, newPassword)
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
            () -> userService.updateUserProfile(NON_EXISTENT_USER_ID, user)
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
                     () -> userService.assignAddressToUser(NON_EXISTENT_USER_ID, address)
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
            () -> userService.deleteUser(NON_EXISTENT_USER_ID)
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
            () -> userService.addFriend(requestorID, NON_EXISTENT_USER_ID)
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
                () -> assertEquals(secondRequestedFriendship.getStatus(), FriendshipStatusCodes.REQUESTED)
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
                   TargetUserIsNotFriendException, BlockedUserException, TargetUserIsAlreadyFriendException {
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
                     () -> userService.removeFriend(requestorID, NON_EXISTENT_USER_ID)
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
                () -> assertEquals(postRequestFriendship.getStatus(), FriendshipStatusCodes.ACCEPTED),
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
                   BlockedUserException, TargetUserIsAlreadyFriendException, NonExistentFriendshipException {
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
                    () -> userService.acceptFriendshipRequest(targetID, NON_EXISTENT_USER_ID)
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
                () -> assertEquals(postRequestFriendship.getStatus(), FriendshipStatusCodes.DECLINED),
                () -> assertEquals(postRequestFriendship.getSpecifier(), targetUser),
                () -> assertNull(postRequestFriendship.getGroup())
        );
    }
    
    @Test
    public void testDeclineNonExistentFriendshipRequest()
            throws InstanceAlreadyExistsException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   BlockedUserException, TargetUserIsAlreadyFriendException, NonExistentFriendshipException {
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
                () -> assertEquals(friendship.getStatus(), FriendshipStatusCodes.BLOCKED)
        );
    }
    
    
    @Test
    public void testBlockCurrentUser()
            throws TargetUserIsCurrentUserException, InstanceNotFoundException, InstanceAlreadyExistsException {
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
                () -> assertEquals(firstBlockFriendship.getStatus(), FriendshipStatusCodes.BLOCKED),
                () -> assertEquals(secondBlockFriendship.getStatus(), FriendshipStatusCodes.BLOCKED)
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
                () -> assertEquals(blockedFrienship.getStatus(), FriendshipStatusCodes.BLOCKED),
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
        List<Friendship> blockedFriendshipsList = new ArrayList<>(AMMOUNT_OF_BLOCKED_USERS);
        
        // Ejecutar funcionalidades
        for (User targetUser: targetUsersList) {            // Bloquea a todos los usuarios generados
            Friendship blockedFriendship = userService.blockFriend(requestorID, targetUser.getUserID());
            blockedFriendshipsList.add(blockedFriendship);
        }
        Block<User> blockedUsersBlock = userService.getBlockedUsers(requestorID, BLOCK_PAGE, BLOCK_PAGE_SIZE);
        
        // Comprobar resultados
        assertAll(
                () -> assertEquals(AMMOUNT_OF_BLOCKED_USERS, blockedUsersBlock.getItemsCount()),
                () -> assertFalse(blockedUsersBlock.hasMoreItems())
        );
    }
    
    @Test
    public void testGetBlockedUsersFromNonExistentUser() {
        // Crear datos de prueba
        final int BLOCK_PAGE = 0;
        final int BLOCK_PAGE_SIZE = 5;
    
        // Comprobar resultados
        assertThrows(InstanceNotFoundException.class,
                     () -> userService.getBlockedUsers(NON_EXISTENT_USER_ID, BLOCK_PAGE, BLOCK_PAGE_SIZE)
        );
    }
}
