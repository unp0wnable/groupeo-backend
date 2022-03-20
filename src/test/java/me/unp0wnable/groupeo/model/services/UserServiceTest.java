package me.unp0wnable.groupeo.model.services;

import me.unp0wnable.groupeo.model.entities.*;
import me.unp0wnable.groupeo.model.exceptions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static me.unp0wnable.groupeo.utils.TestGenerator.*;
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
    public void testChangePasswordToNonExistingUser() throws InstanceAlreadyExistsException {
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
    public void testChangePasswordWithIncorrectPassword() {
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
        UserAddress generatedAddress = generateValidAddressForUser(user);
        
        // Ejecutar funcionalidades
        UUID userID = user.getUserID();
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
        UserAddress generatedAddress = generateValidAddressForUser(user);
        UUID userID = user.getUserID();
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
        User targetUser = registerValidUser("Target", this.userService);
    
        // Ejecutar funcionalidades
        UUID requestorID = requestorUser.getUserID();
        UUID targetID = targetUser.getUserID();
        Friendship friendship = userService.addFriend(requestorID, targetID);
    
        // Comprobar resultados
        assertAll(
                () -> assertNull(friendship.getGroup()),
                () -> assertEquals(friendship.getSpecifier(), requestorUser)
        );
    }
    
}
