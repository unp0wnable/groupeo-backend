package me.unp0wnable.groupeo.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import me.unp0wnable.groupeo.model.entities.*;
import me.unp0wnable.groupeo.model.entities.identities.FriendshipPK;
import me.unp0wnable.groupeo.model.exceptions.IncorrectLoginException;
import me.unp0wnable.groupeo.model.repositories.*;
import me.unp0wnable.groupeo.model.services.UserService;
import me.unp0wnable.groupeo.rest.dtos.common.BlockDto;
import me.unp0wnable.groupeo.rest.dtos.conversors.CommonConversor;
import me.unp0wnable.groupeo.rest.dtos.conversors.UserConversor;
import me.unp0wnable.groupeo.rest.dtos.errors.ErrorsDto;
import me.unp0wnable.groupeo.rest.dtos.users.*;
import me.unp0wnable.groupeo.rest.http.jwt.JwtData;
import me.unp0wnable.groupeo.rest.http.jwt.JwtGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static me.unp0wnable.groupeo.rest.security.JwtFilter.AUTH_TOKEN_PREFIX;
import static me.unp0wnable.groupeo.utils.TestDataGenerator.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class UserControllerTest {
    private static final  String API_ENDPOINT = "/api/users";
    private final ObjectMapper mapper = new ObjectMapper()
                                                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final Locale locale = Locale.getDefault();
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private MessageSource messageSource;
    
    @Autowired
    private JwtGenerator jwtGenerator;
    
    @Autowired
    private UserController userController;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FriendshipRepository friendshipRepository;
    
    @Autowired
    private UserAddressRepository userAddressRepository;

    
    /* ****************************** AUX FUNCTIONS ****************************** */
    /** Registra un usuario válido en el sistema */
    private AuthenticatedUserDto createAuthenticatedUser(String nickName) throws IncorrectLoginException {
        // Crea un usuario válido
        User user = generateValidUser(nickName);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        
        // Guarda el usuario en la base de datos
        userRepository.save(user);
        
        // Genera el DTO con los datos del usuario recién creado
        LoginParamsDto loginParamsDto = new LoginParamsDto();
        loginParamsDto.setNickName(nickName);
        loginParamsDto.setPassword(DEFAULT_PASSWORD);
        
        return userController.login(loginParamsDto);
    }
    
    private AddressDto createAddressForUser(User user) {
        // Crea una dirección válida
        UserAddress address = generateValidAddressForUser(user);
        //address.setUserProfile(user);
        
        // Guarda la dirección en la base de datos
        userAddressRepository.save(address);
        
        // Genera el DTO con los datos de la dirección recién creada
        return UserConversor.toAddressDto(address);
    }
    
    /** Crea un DTO con los datos necesarios para registrar un usuario válido */
    private SignUpParamsDto generateSignUpParamsDto( ) {
        User user = generateValidUser(DEFAULT_NICKNAME);
        SignUpParamsDto signUpParams = new SignUpParamsDto();
        signUpParams.setFirstName(user.getFirstName());
        signUpParams.setSurname1(user.getSurname1());
        signUpParams.setSurname2(user.getSurname2());
        signUpParams.setNickName(user.getNickName());
        signUpParams.setRawPassword(user.getPassword());
        signUpParams.setEmail(user.getEmail());
        signUpParams.setBirthDate(user.getBirthDate());
        signUpParams.setImageB64(user.getImageB64());
        return signUpParams;
    }
    
    /** Recupera datos de una amistad entre dos usuarios y los devuelve como un DTO */
    private FriendshipDto fetchFriendshipAsDTO(String requestorID, String targetID) {
        UUID requestorUUID = UUID.fromString(requestorID);
        UUID targetUUID = UUID.fromString(targetID);
        FriendshipPK friendshipPK = new FriendshipPK(requestorUUID, targetUUID);
        Friendship friendship = friendshipRepository.findById(friendshipPK).get();
        
        return UserConversor.toFriendshipDTO(friendship);
    }
    
    /** Recupera el texto asociado a la propiedad recibida a partir del fichero de I18N en el idioma indicado. */
    private String getI18NExceptionMessage(String propertyName, Locale locale) {
        String globalErrorMessage = messageSource.getMessage(
                propertyName,
                null,
                propertyName,
                locale
        );
        
        return globalErrorMessage;
    }
    
    
    
    /* ****************************** TEST CASES ****************************** */
    @Test
    public void testSignUp_POST() throws Exception {
        // Crear datos de prueba
        SignUpParamsDto paramsDTo = generateSignUpParamsDto();
        String endpointAddress = API_ENDPOINT + "/signUp";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDTo);
        
        // Ejecutar funcionalidades
        ResultActions performAction = mockMvc.perform(
            post(endpointAddress)
                .contentType(MediaType.APPLICATION_JSON)
                .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testSignUpTwice_POST() throws Exception {
        // Crear datos de prueba
        SignUpParamsDto paramsDto = generateSignUpParamsDto();
        String endpointAddress = API_ENDPOINT + "/signUp";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        ResultActions successfulSignUp = mockMvc.perform(
                post(endpointAddress)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        ResultActions exceptionalSignUp = mockMvc.perform(
                post(endpointAddress)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        successfulSignUp
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        exceptionalSignUp
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    
    @Test
    public void testLoginUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto authUserDto = createAuthenticatedUser(DEFAULT_NICKNAME);
        LoginParamsDto paramsDto = new LoginParamsDto();
        paramsDto.setNickName(authUserDto.getUserDTO().getNickName());
        paramsDto.setPassword(DEFAULT_PASSWORD);
        String endpointAddress = API_ENDPOINT + "/login";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
            post(endpointAddress)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN_PREFIX + authUserDto.getServiceToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testLoginUserWithIncorrectPassword_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto authUserDto = createAuthenticatedUser(DEFAULT_NICKNAME);
        LoginParamsDto paramsDto = new LoginParamsDto();
        paramsDto.setNickName(authUserDto.getUserDTO().getNickName());
        paramsDto.setPassword(DEFAULT_PASSWORD + "XXX");
        String endpointAddress = API_ENDPOINT + "/login";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                post(endpointAddress)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN_PREFIX + authUserDto.getServiceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testLoginNonExistentUser_POST() throws Exception {
        // Crear datos de prueba
        LoginParamsDto paramsDto = new LoginParamsDto();
        paramsDto.setNickName(DEFAULT_NICKNAME);
        paramsDto.setPassword(DEFAULT_PASSWORD );
        String endpointAddress = API_ENDPOINT + "/login";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                post(endpointAddress)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testLoginWithServiceToken_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto authUserDto = createAuthenticatedUser(DEFAULT_NICKNAME);
        String endpointAddress = API_ENDPOINT + "/tokenLogin";
        JwtData jwtData = jwtGenerator.extractInfo(authUserDto.getServiceToken());
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                post(endpointAddress)
                        // Valores anotados como @RequestAttribute
                        .requestAttr("userID", jwtData.getUserID())
                        .requestAttr("token", jwtData.toString())
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN_PREFIX + authUserDto.getServiceToken())
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testLoginNonExistentUserWithServiceToken_POST() throws Exception {
        // Crear datos de prueba
        LoginParamsDto paramsDto = new LoginParamsDto();
        paramsDto.setNickName(DEFAULT_NICKNAME);
        paramsDto.setPassword(DEFAULT_PASSWORD );
        String endpointAddress = API_ENDPOINT + "/login";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                post(endpointAddress)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testChangePassword_PUT() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto authUserDto = createAuthenticatedUser(DEFAULT_NICKNAME);
        String userID = authUserDto.getUserDTO().getUserID().toString();
        ChangePasswordParamsDto paramsDto = new ChangePasswordParamsDto();
        paramsDto.setOldPassword(DEFAULT_PASSWORD);
        paramsDto.setNewPassword(DEFAULT_PASSWORD + "XXX");
        String endpointAddress = API_ENDPOINT + "/" + userID + "/changePassword";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                put(endpointAddress)
                        // Valores anotados como @RequestAttribute
                        .requestAttr("userID", UUID.fromString(userID))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN_PREFIX + authUserDto.getServiceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction.andExpect(status().isNoContent());
    }
    
    @Test
    public void testChangePasswordToOtherUser_PUT() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto currentUserDto = createAuthenticatedUser("currentUser");
        AuthenticatedUserDto targetUserDto = createAuthenticatedUser("targetUser");
        String currentUserID = currentUserDto.getUserDTO().getUserID().toString();
        String targetUserID = targetUserDto.getUserDTO().getUserID().toString();
        ChangePasswordParamsDto paramsDto = new ChangePasswordParamsDto();
        paramsDto.setOldPassword(DEFAULT_PASSWORD);
        paramsDto.setNewPassword(DEFAULT_PASSWORD + "XXX");
        String endpointAddress = API_ENDPOINT + "/" + targetUserID + "/changePassword";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                put(endpointAddress)
                        // Valores anotados como @RequestAttribute
                        .requestAttr("userID", UUID.fromString(currentUserID))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN_PREFIX + currentUserDto.getServiceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testChangePasswordToNonExistingUser_PUT() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto authUserDto = createAuthenticatedUser(DEFAULT_NICKNAME);
        //Elimina al usuario recién creado para que no exista en el servicio
        userRepository.deleteById(authUserDto.getUserDTO().getUserID());
        ChangePasswordParamsDto paramsDto = new ChangePasswordParamsDto();
        paramsDto.setOldPassword(DEFAULT_PASSWORD);
        paramsDto.setNewPassword(DEFAULT_PASSWORD);
        String endpointAddress = API_ENDPOINT + "/" + NON_EXISTENT_UUID + "/changePassword";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                put(endpointAddress)
                        // Valores anotados como @RequestAttribute
                        .requestAttr("userID", NON_EXISTENT_UUID)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN_PREFIX + authUserDto.getServiceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction.andExpect(status().isNotFound());
    }
    
    @Test
    public void testChangePasswordWithIncorrectPassword_PUT() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto authUserDto = createAuthenticatedUser(DEFAULT_NICKNAME);
        String userID = authUserDto.getUserDTO().getUserID().toString();
        ChangePasswordParamsDto paramsDto = new ChangePasswordParamsDto();
        paramsDto.setOldPassword(DEFAULT_PASSWORD + "OOO");
        paramsDto.setNewPassword(DEFAULT_PASSWORD + "XXX");
        String endpointAddress = API_ENDPOINT + "/" + userID + "/changePassword";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                put(endpointAddress)
                        // Valores anotados como @RequestAttribute
                        .requestAttr("userID", UUID.fromString(userID))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN_PREFIX + authUserDto.getServiceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testUpdateProfile_PUT() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto authUserDto = createAuthenticatedUser(DEFAULT_NICKNAME);
        String userID = authUserDto.getUserDTO().getUserID().toString();
        UpdateProfileParamsDto paramsDto = new UpdateProfileParamsDto();
        paramsDto.setFirstName(authUserDto.getUserDTO().getFirstName() + "XXX");
        paramsDto.setSurname1(authUserDto.getUserDTO().getSurname1() + "XXX");
        paramsDto.setSurname2(authUserDto.getUserDTO().getSurname2() + "XXX");
        paramsDto.setEmail(authUserDto.getUserDTO().getEmail() + "XXX");
        paramsDto.setDescription(authUserDto.getUserDTO().getDescription() + "XXX");
        String endpointAddress = API_ENDPOINT + "/" + userID;
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                put(endpointAddress)
                        // Valores anotados como @RequestAttribute
                        .requestAttr("userID", UUID.fromString(userID))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN_PREFIX + authUserDto.getServiceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testUpdateProfileToOtherUser_PUT() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto currentUserDto = createAuthenticatedUser("currentUser");
        AuthenticatedUserDto targetUserDto = createAuthenticatedUser("targetUser");
        String currentUserID = currentUserDto.getUserDTO().getUserID().toString();
        String targetUserID = targetUserDto.getUserDTO().getUserID().toString();
        UpdateProfileParamsDto paramsDto = new UpdateProfileParamsDto();
        paramsDto.setFirstName(currentUserDto.getUserDTO().getFirstName() + "XXX");
        paramsDto.setSurname1(currentUserDto.getUserDTO().getSurname1() + "XXX");
        paramsDto.setSurname2(currentUserDto.getUserDTO().getSurname2() + "XXX");
        paramsDto.setEmail(currentUserDto.getUserDTO().getEmail() + "XXX");
        paramsDto.setDescription(currentUserDto.getUserDTO().getDescription() + "XXX");
        String endpointAddress = API_ENDPOINT + "/" + targetUserID;
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                put(endpointAddress)
                        // Valores anotados como @RequestAttribute
                        .requestAttr("userID", UUID.fromString(currentUserID))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN_PREFIX + currentUserDto.getServiceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testUpdateProfileToNonExistingUser_PUT() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto authUserDto = createAuthenticatedUser(DEFAULT_NICKNAME);
        //Elimina al usuario recién creado para que no exista en el servicio
        userRepository.deleteById(authUserDto.getUserDTO().getUserID());
        UpdateProfileParamsDto paramsDto = new UpdateProfileParamsDto();
        paramsDto.setFirstName(authUserDto.getUserDTO().getFirstName() + "XXX");
        paramsDto.setSurname1(authUserDto.getUserDTO().getSurname1() + "XXX");
        paramsDto.setSurname2(authUserDto.getUserDTO().getSurname2() + "XXX");
        paramsDto.setEmail(authUserDto.getUserDTO().getEmail() + "XXX");
        paramsDto.setDescription(authUserDto.getUserDTO().getDescription() + "XXX");
        String endpointAddress = API_ENDPOINT + "/" + NON_EXISTENT_UUID;
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                put(endpointAddress)
                        // Valores anotados como @RequestAttribute
                        .requestAttr("userID", NON_EXISTENT_UUID)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN_PREFIX + authUserDto.getServiceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testCreateNewAddressForUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto authUserDto = createAuthenticatedUser(DEFAULT_NICKNAME);
        AddressDto paramsDto = createAddressForUser(UserConversor.fromUserDTO(authUserDto.getUserDTO()));
        String userID = authUserDto.getUserDTO().getUserID().toString();
        String endpointAddress = API_ENDPOINT + "/" + userID + "/address";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                post(endpointAddress)
                        // Valores anotados como @RequestAttribute
                        .requestAttr("userID", UUID.fromString(userID))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN_PREFIX + authUserDto.getServiceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testCreateNewAddressForOtherUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto currentUserDto = createAuthenticatedUser("currentUser");
        AuthenticatedUserDto targetUserDto = createAuthenticatedUser("targetUser");
        AddressDto paramsDto = createAddressForUser(UserConversor.fromUserDTO(currentUserDto.getUserDTO()));
        String currentUserID = currentUserDto.getUserDTO().getUserID().toString();
        String targetUserID = targetUserDto.getUserDTO().getUserID().toString();
        String endpointAddress = API_ENDPOINT + "/" + targetUserID + "/address";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                post(endpointAddress)
                        // Valores anotados como @RequestAttribute
                        .requestAttr("userID", UUID.fromString(currentUserID))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN_PREFIX + currentUserDto.getServiceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testUpdateAddress_PUT() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto authUserDto = createAuthenticatedUser(DEFAULT_NICKNAME);
        User user = UserConversor.fromUserDTO(authUserDto.getUserDTO());
        AddressDto paramsDto = createAddressForUser(user);
        String userID = authUserDto.getUserDTO().getUserID().toString();
        paramsDto.setAddressID(paramsDto.getAddressID());
        paramsDto.setCity(paramsDto.getCity() + "XXX");
        paramsDto.setRegion(paramsDto.getRegion() + "XXX");
        paramsDto.setCountry(paramsDto.getCountry() + "XXX");
        paramsDto.setPostalCode(paramsDto.getPostalCode() + "0");
        String endpointAddress = API_ENDPOINT + "/" + userID + "/address";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                post(endpointAddress)
                        // Valores anotados como @RequestAttribute
                        .requestAttr("userID", UUID.fromString(userID))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN_PREFIX + authUserDto.getServiceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testUpdateAddressToOtherUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto currentUserDto = createAuthenticatedUser("currentUser");
        AuthenticatedUserDto targetUserDto = createAuthenticatedUser("targetUser");
        User currentUser = UserConversor.fromUserDTO(currentUserDto.getUserDTO());
        AddressDto paramsDto = createAddressForUser(currentUser);
        String currentUserID = currentUserDto.getUserDTO().getUserID().toString();
        String targetUserID = targetUserDto.getUserDTO().getUserID().toString();
        paramsDto.setAddressID(paramsDto.getAddressID());
        paramsDto.setCity(paramsDto.getCity() + "XXX");
        paramsDto.setRegion(paramsDto.getRegion() + "XXX");
        paramsDto.setCountry(paramsDto.getCountry() + "XXX");
        paramsDto.setPostalCode(paramsDto.getPostalCode() + "0");
        String endpointAddress = API_ENDPOINT + "/" + targetUserID + "/address";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                post(endpointAddress)
                        // Valores anotados como @RequestAttribute
                        .requestAttr("userID", UUID.fromString(currentUserID))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN_PREFIX + currentUserDto.getServiceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testDeleteUser_DELETE() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto authUserDto = createAuthenticatedUser(DEFAULT_NICKNAME);
        String userID = authUserDto.getUserDTO().getUserID().toString();
        String endpointAddress = API_ENDPOINT + "/" + userID;
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                delete(endpointAddress)
                        .requestAttr("userID", UUID.fromString(userID))
        );
        
        // Comprobar resultados
        performAction.andExpect(status().isNoContent());
    }
    
    @Test
    public void testDeleteNonExistentUser_DELETE() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto authUserDto = createAuthenticatedUser(DEFAULT_NICKNAME);
        //Elimina al usuario recién creado para que no exista en el servicio
        userRepository.deleteById(authUserDto.getUserDTO().getUserID());
        String userID = authUserDto.getUserDTO().getUserID().toString();
        String endpointAddress = API_ENDPOINT + "/" + userID;
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                delete(endpointAddress)
                        .requestAttr("userID", UUID.fromString(userID))
        );
        
        // Comprobar resultados
        performAction.andExpect(status().isNotFound()) ;
    }
    
    @Test
    public void testDeleteOtherUserAccount_DELETE() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto currentUserDto = createAuthenticatedUser("currentUser");
        AuthenticatedUserDto targetUserDto = createAuthenticatedUser("targetUser");
        String currentUserID = currentUserDto.getUserDTO().getUserID().toString();
        String targetUserID = targetUserDto.getUserDTO().getUserID().toString();
        String endpointAddress = API_ENDPOINT + "/" + targetUserID;
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                delete(endpointAddress)
                        .requestAttr("userID", UUID.fromString(currentUserID))
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    /* *********************************** User relationships *********************************** */
    
    @Test
    public void testRequestFriendship_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        
        
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/add/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        FriendshipDto expectedResponseBody = fetchFriendshipAsDTO(requestorUserID, targetUserID);
        
        
        // Comprobar resultados
        String encodedExpectedResponse = this.mapper.writeValueAsString(expectedResponseBody);
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(encodedExpectedResponse));
    }
    
    @Test
    public void testRequestFriendshipAsOtherUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/add/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(targetUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.PERMISSION_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testRequestFriendshipToBlockedUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Boquear al usuario
        userService.blockFriend(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/add/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.BLOCKED_USER_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testRequestFriendshipToCurrentUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/add/%s", API_ENDPOINT, requestorUserID, requestorUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.TARGET_USER_IS_CURRENT_USER_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testRequestFriendshipToAlreadyFriendUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Añadir como amigo al usuario
        userService.requestFriendship(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        userService.acceptFriendshipRequest(UUID.fromString(targetUserID), UUID.fromString(requestorUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/add/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(UserController.TARGET_USER_IS_ALREADY_FRIEND_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testRequestFriendshipToNonExistentUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/add/%s", API_ENDPOINT, requestorUserID, NON_EXISTENT_UUID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.INSTANCE_NOT_FOUND_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testRequestFriendshipTwice_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Enviar petición de amistad al usuario
        userService.requestFriendship(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/add/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.INSTANCE_ALREADY_EXISTS_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testRemoveFriend_DELETE() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Añadir como amigo al usuario
        userService.requestFriendship(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        userService.acceptFriendshipRequest(UUID.fromString(targetUserID), UUID.fromString(requestorUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/remove/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                delete(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNoContent());
    }
    
    @Test
    public void testRemoveFriendAsOtherUser_DELETE() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Añadir como amigo al usuario
        userService.requestFriendship(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        userService.acceptFriendshipRequest(UUID.fromString(targetUserID), UUID.fromString(requestorUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/remove/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                delete(endpointAddress)
                        .requestAttr("userID", UUID.fromString(targetUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.PERMISSION_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testRemoveFriendshipToBlockedUser_DELETE() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Boquear al usuario
        userService.blockFriend(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/remove/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                delete(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.BLOCKED_USER_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testRemoveFriendshipToCurrentUser_DELETE() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/remove/%s", API_ENDPOINT, requestorUserID, requestorUserID);
        ResultActions performAction = mockMvc.perform(
                delete(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.TARGET_USER_IS_CURRENT_USER_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testRemoveFriendshipWithNonFriendUser_DELETE() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/remove/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                delete(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.TARGET_USER_IS_NOT_FRIEND_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testRemoveFriendshipToNonExistentUser_DELETE() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/remove/%s", API_ENDPOINT, requestorUserID, NON_EXISTENT_UUID);
        ResultActions performAction = mockMvc.perform(
                delete(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.INSTANCE_NOT_FOUND_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testAcceptFriendshipRequest_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Añadir como amigo al usuario
        userService.requestFriendship(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/accept/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        FriendshipDto expectedResponseBody = fetchFriendshipAsDTO(requestorUserID, targetUserID);
        
        
        // Comprobar resultados
        String encodedExpectedResponse = this.mapper.writeValueAsString(expectedResponseBody);
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(encodedExpectedResponse));
    }
    
    @Test
    public void testAcceptFriendshipRequestAsOtherUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Añadir como amigo al usuario
        userService.requestFriendship(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/accept/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(targetUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.PERMISSION_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testAcceptFriendshipToCurrentUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/accept/%s", API_ENDPOINT, requestorUserID, requestorUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.TARGET_USER_IS_CURRENT_USER_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testAcceptFriendshipToAlreadyFriendUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Añadir como amigo al usuario
        userService.requestFriendship(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        userService.acceptFriendshipRequest(UUID.fromString(targetUserID), UUID.fromString(requestorUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/accept/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(UserController.TARGET_USER_IS_ALREADY_FRIEND_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testAcceptFriendshipToNonExistentUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/accept/%s", API_ENDPOINT, requestorUserID, NON_EXISTENT_UUID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.INSTANCE_NOT_FOUND_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testAcceptNonExistentFriendshipRequest_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/accept/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(UserController.NON_EXISTENT_FRIENDSHIP_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testAcceptFriendshipTwice_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Enviar petición de amistad al usuario
        userService.requestFriendship(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        userService.acceptFriendshipRequest(UUID.fromString(targetUserID), UUID.fromString(requestorUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/accept/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.INSTANCE_ALREADY_EXISTS_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testDeclineFriendshipRequest_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Añadir como amigo al usuario
        userService.requestFriendship(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/decline/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        FriendshipDto expectedResponseBody = fetchFriendshipAsDTO(requestorUserID, targetUserID);
        
        // Comprobar resultados
        String encodedExpectedResponse = this.mapper.writeValueAsString(expectedResponseBody);
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(encodedExpectedResponse));
    }
    
    @Test
    public void testDeclineFriendshipRequestAsOtherUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Añadir como amigo al usuario
        userService.requestFriendship(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/decline/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(targetUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.PERMISSION_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testDeclineFriendshipToCurrentUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/decline/%s", API_ENDPOINT, requestorUserID, requestorUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.TARGET_USER_IS_CURRENT_USER_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testDeclineFriendshipToNonExistentUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/decline/%s", API_ENDPOINT, requestorUserID, NON_EXISTENT_UUID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.INSTANCE_NOT_FOUND_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testDeclineNonExistentFriendshipRequest_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/decline/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(UserController.NON_EXISTENT_FRIENDSHIP_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testDeclineFriendshipTwice_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Enviar petición de amistad al usuario
        userService.requestFriendship(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        userService.declineFriendshipRequest(UUID.fromString(targetUserID), UUID.fromString(requestorUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/decline/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        FriendshipDto expectedResponseBody = fetchFriendshipAsDTO(requestorUserID, targetUserID);
        
        // Comprobar resultados
        String encodedExpectedResponse = this.mapper.writeValueAsString(expectedResponseBody);
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(encodedExpectedResponse));
    }
    
    @Test
    public void testBlockUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/block/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        FriendshipDto expectedResponseBody = fetchFriendshipAsDTO(requestorUserID, targetUserID);
        
        // Comprobar resultados
        String encodedExpectedResponse = this.mapper.writeValueAsString(expectedResponseBody);
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(encodedExpectedResponse));
    }
    
    @Test
    public void testBlockUserAsOtherUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/block/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(targetUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.PERMISSION_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testBlockCurrentUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/block/%s", API_ENDPOINT, requestorUserID, requestorUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.TARGET_USER_IS_CURRENT_USER_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testBlockNonExistentUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/block/%s", API_ENDPOINT, requestorUserID, NON_EXISTENT_UUID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.INSTANCE_NOT_FOUND_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testBlockUserTwice_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Enviar petición de amistad al usuario
        userService.blockFriend(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/decline/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        FriendshipDto expectedResponseBody = fetchFriendshipAsDTO(requestorUserID, targetUserID);
        
        // Comprobar resultados
        String encodedExpectedResponse = this.mapper.writeValueAsString(expectedResponseBody);
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(encodedExpectedResponse));
    }
    
    @Test
    public void testUnblockUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Bloquea al usuario
        userService.blockFriend(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/unblock/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNoContent());
    }
    
    @Test
    public void testUnblockUserAsOtherUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Bloquea al usuario
        userService.blockFriend(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/unblock/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(targetUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.PERMISSION_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testUnblockCurrentUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Bloquea al usuario
        userService.blockFriend(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/unblock/%s", API_ENDPOINT, requestorUserID, requestorUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.TARGET_USER_IS_CURRENT_USER_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testUnblockNonExistentUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Bloquea al usuario
        userService.blockFriend(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/unblock/%s", API_ENDPOINT, requestorUserID, NON_EXISTENT_UUID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.INSTANCE_NOT_FOUND_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testUnblockNonBlockedUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/unblock/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(UserController.NON_EXISTENT_FRIENDSHIP_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testUnblockUserTwice_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Bloquea al usuario
        userService.blockFriend(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/unblock/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNoContent());
    }
    
    @Test
    public void testGetBlockedUsers_GET() throws Exception {
        // Crear datos de prueba
        final int AMMOUNT_OF_USERS = 2;
        final int PAGE = 0;
        final int PAGE_SIZE = 5;
        AuthenticatedUserDto currentUser = createAuthenticatedUser("User");
        String currentUserID = currentUser.getUserDTO().getUserID().toString();
        List<User> targetUsersList = registerMultipleUsers(AMMOUNT_OF_USERS, this.userService);
        // Bloquear a los usuarios
        for (User target : targetUsersList ) {
            userService.blockFriend(UUID.fromString(currentUserID), target.getUserID());
        }
        Block<User> blockedUsers = userService.getBlockedUsers(UUID.fromString(currentUserID), PAGE, PAGE_SIZE);
    
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/blocked", API_ENDPOINT, currentUserID);
        ResultActions performAction = mockMvc.perform(
                get(endpointAddress)
                        .requestAttr("userID", UUID.fromString(currentUserID))
        );
        BlockDto<UserDto> expectedResponseBody = CommonConversor.toBlockDTO(blockedUsers, UserConversor::toUserDto);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testGetBlockedUsersWithPagination_GET() throws Exception {
        // Crear datos de prueba
        final int AMMOUNT_OF_USERS = 5;
        final int PAGE = 0;
        final int PAGE_SIZE = 3;
        AuthenticatedUserDto currentUser = createAuthenticatedUser("User");
        String currentUserID = currentUser.getUserDTO().getUserID().toString();
        List<User> targetUsersList = registerMultipleUsers(AMMOUNT_OF_USERS, this.userService);
        // Bloquear a los usuarios
        for (User target : targetUsersList ) {
            userService.blockFriend(UUID.fromString(currentUserID), target.getUserID());
        }
        Block<User> blockedUsers = userService.getBlockedUsers(UUID.fromString(currentUserID), PAGE, PAGE_SIZE);
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/blocked", API_ENDPOINT, currentUserID);
        ResultActions performAction = mockMvc.perform(
                get(endpointAddress)
                        .requestAttr("userID", UUID.fromString(currentUserID))
                        .queryParam("page", String.valueOf(PAGE))
                        .queryParam("pageSize", String.valueOf(PAGE_SIZE))
        );
        BlockDto<UserDto> expectedResponseBody = CommonConversor.toBlockDTO(blockedUsers, UserConversor::toUserDto);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testGetBlockedUsersWithouthHavingUsersBlocked_GET() throws Exception {
        // Crear datos de prueba
        final int AMMOUNT_OF_USERS = 0;
        final int PAGE = 0;
        final int PAGE_SIZE = 5;
        AuthenticatedUserDto currentUser = createAuthenticatedUser("User");
        String currentUserID = currentUser.getUserDTO().getUserID().toString();
        List<User> targetUsersList = registerMultipleUsers(AMMOUNT_OF_USERS, this.userService);
        // Bloquear a los usuarios
        for (User target : targetUsersList ) {
            userService.blockFriend(UUID.fromString(currentUserID), target.getUserID());
        }
        Block<User> blockedUsers = userService.getBlockedUsers(UUID.fromString(currentUserID), PAGE, PAGE_SIZE);
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/blocked", API_ENDPOINT, currentUserID);
        ResultActions performAction = mockMvc.perform(
                get(endpointAddress)
                        .requestAttr("userID", UUID.fromString(currentUserID))
        );
        BlockDto<UserDto> expectedResponseBody = CommonConversor.toBlockDTO(blockedUsers, UserConversor::toUserDto);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testGetBlockedUsersAsOtherUser_GET() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto currentUser = createAuthenticatedUser("User");
        String currentUserID = currentUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/blocked", API_ENDPOINT, currentUserID);
        ResultActions performAction = mockMvc.perform(
                get(endpointAddress)
                        .requestAttr("userID", UUID.randomUUID())
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.PERMISSION_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testGetBlockedUsersFronNonExistentUser_GET() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto currentUser = createAuthenticatedUser("User");
        String currentUserID = currentUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/blocked", API_ENDPOINT, NON_EXISTENT_UUID);
        ResultActions performAction = mockMvc.perform(
                get(endpointAddress)
                        .requestAttr("userID", UUID.fromString(currentUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.INSTANCE_NOT_FOUND_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testGetUserFriends_GET() throws Exception {
        // Crear datos de prueba
        final int AMMOUNT_OF_USERS = 4;
        final int PAGE = 0;
        final int PAGE_SIZE = 5;
        AuthenticatedUserDto currentUser = createAuthenticatedUser("User");
        String currentUserID = currentUser.getUserDTO().getUserID().toString();
        List<User> targetUsersList = registerMultipleUsers(AMMOUNT_OF_USERS, this.userService);
        // Agregar a los usuarios como amigos
        for (User target : targetUsersList ) {
            userService.requestFriendship(UUID.fromString(currentUserID), target.getUserID());
            userService.acceptFriendshipRequest(target.getUserID(), UUID.fromString(currentUserID));
        }
        Block<User> userFriends = userService.getUserFriends(UUID.fromString(currentUserID), PAGE, PAGE_SIZE);
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s", API_ENDPOINT, currentUserID);
        ResultActions performAction = mockMvc.perform(
                get(endpointAddress)
                        .requestAttr("userID", UUID.fromString(currentUserID))
        );
        BlockDto<UserDto> expectedResponseBody = CommonConversor.toBlockDTO(userFriends, UserConversor::toUserDto);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testGetUserFriendsWithPagination_GET() throws Exception {
        // Crear datos de prueba
        final int AMMOUNT_OF_USERS = 5;
        final int PAGE = 0;
        final int PAGE_SIZE = 3;
        AuthenticatedUserDto currentUser = createAuthenticatedUser("User");
        String currentUserID = currentUser.getUserDTO().getUserID().toString();
        List<User> targetUsersList = registerMultipleUsers(AMMOUNT_OF_USERS, this.userService);
        // Agregar a los usuarios como amigos
        for (User target : targetUsersList ) {
            userService.requestFriendship(UUID.fromString(currentUserID), target.getUserID());
            userService.acceptFriendshipRequest(target.getUserID(), UUID.fromString(currentUserID));
        }
        Block<User> userFriends = userService.getUserFriends(UUID.fromString(currentUserID), PAGE, PAGE_SIZE);
    
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s", API_ENDPOINT, currentUserID);
        ResultActions performAction = mockMvc.perform(
                get(endpointAddress)
                        .requestAttr("userID", UUID.fromString(currentUserID))
                        .queryParam("page", String.valueOf(PAGE))
                        .queryParam("pageSize", String.valueOf(PAGE_SIZE))
        );
        BlockDto<UserDto> expectedResponseBody = CommonConversor.toBlockDTO(userFriends, UserConversor::toUserDto);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testGetUserFriendsWithouthHavingFriends_GET() throws Exception {
        // Crear datos de prueba
        final int AMMOUNT_OF_USERS = 0;
        final int PAGE = 0;
        final int PAGE_SIZE = 5;
        AuthenticatedUserDto currentUser = createAuthenticatedUser("User");
        String currentUserID = currentUser.getUserDTO().getUserID().toString();
        List<User> targetUsersList = registerMultipleUsers(AMMOUNT_OF_USERS, this.userService);
        // Agregar a los usuarios como amigos
        for (User target : targetUsersList ) {
            userService.requestFriendship(UUID.fromString(currentUserID), target.getUserID());
            userService.acceptFriendshipRequest(target.getUserID(), UUID.fromString(currentUserID));
        }
        Block<User> userFriends = userService.getUserFriends(UUID.fromString(currentUserID), PAGE, PAGE_SIZE);
    
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s", API_ENDPOINT, currentUserID);
        ResultActions performAction = mockMvc.perform(
                get(endpointAddress)
                        .requestAttr("userID", UUID.fromString(currentUserID))
        );
        BlockDto<UserDto> expectedResponseBody = CommonConversor.toBlockDTO(userFriends, UserConversor::toUserDto);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testGetUserFriendsAsOtherUser_GET() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto currentUser = createAuthenticatedUser("User");
        String currentUserID = currentUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s", API_ENDPOINT, currentUserID);
        ResultActions performAction = mockMvc.perform(
                get(endpointAddress)
                        .requestAttr("userID", UUID.randomUUID())
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.PERMISSION_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testGetUserFriendsFronNonExistentUser_GET() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto currentUser = createAuthenticatedUser("User");
        String currentUserID = currentUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s", API_ENDPOINT, NON_EXISTENT_UUID);
        ResultActions performAction = mockMvc.perform(
                get(endpointAddress)
                        .requestAttr("userID", UUID.fromString(currentUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.INSTANCE_NOT_FOUND_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testGetFriendshipDataAsNonFriends_GET() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("User");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
    
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/friendship/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                get(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(UserController.NON_EXISTENT_FRIENDSHIP_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
    
    
        // Comprobar resultados
        performAction
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testGetFriendshipDataFromNonExistentUser_GET() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("User");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/friendship/%s", API_ENDPOINT, requestorUserID, NON_EXISTENT_UUID);
        ResultActions performAction = mockMvc.perform(
                get(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.INSTANCE_NOT_FOUND_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testGetFriendshipDataHavingPendingRequest_GET() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("User");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Enviar petición de amistad
        userService.requestFriendship(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/friendship/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                get(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        FriendshipDto expectedResponseBody = fetchFriendshipAsDTO(requestorUserID, targetUserID);
        
        
        // Comprobar resultados
        String encodedExpectedResponse = this.mapper.writeValueAsString(expectedResponseBody);
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(encodedExpectedResponse));
    }
    
    @Test
    public void testGetFriendshipDataWithFriendUser_GET() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("User");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Enviar petición de amistad
        userService.requestFriendship(UUID.fromString(requestorUserID), UUID.fromString(targetUserID));
        userService.acceptFriendshipRequest(UUID.fromString(targetUserID), UUID.fromString(requestorUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/friendship/%s", API_ENDPOINT, requestorUserID, targetUserID);
        ResultActions performAction = mockMvc.perform(
                get(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        FriendshipDto expectedResponseBody = fetchFriendshipAsDTO(requestorUserID, targetUserID);
        
        
        // Comprobar resultados
        String encodedExpectedResponse = this.mapper.writeValueAsString(expectedResponseBody);
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(encodedExpectedResponse));
    }
    
    @Test
    public void testCreateGroup_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        CreateGroupParamsDto paramsDto = new CreateGroupParamsDto();
        paramsDto.setName("Test group");
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/groups", API_ENDPOINT, requestorUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(encodedBodyContent));
    }
    @Test
    public void testCreateGroupTwice_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        CreateGroupParamsDto paramsDto = new CreateGroupParamsDto();
        paramsDto.setName("Test group");
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        // Crear el grupo
        userService.createGroup(UUID.fromString(requestorUserID), paramsDto.getName());
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/groups", API_ENDPOINT, requestorUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.INSTANCE_ALREADY_EXISTS_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
    
        // Comprobar resultados
        performAction
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testCreateGroupAsOtherUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        CreateGroupParamsDto paramsDto = new CreateGroupParamsDto();
        paramsDto.setName("Test group");
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/groups", API_ENDPOINT, requestorUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.INSTANCE_NOT_FOUND_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testCreateGroupAsNonExistentUser_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        CreateGroupParamsDto paramsDto = new CreateGroupParamsDto();
        paramsDto.setName("Test group");
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/groups", API_ENDPOINT, NON_EXISTENT_UUID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", NON_EXISTENT_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        String errorMessage = getI18NExceptionMessage(CommonControllerAdvice.INSTANCE_NOT_FOUND_EXCEPTION_KEY, locale);
        ErrorsDto expectedResponseBody = new ErrorsDto(errorMessage);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testDeleteGroup_DELETE() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        // Crear grupo a borrar
        Group group = userService.createGroup(UUID.fromString(requestorUserID), "Test group");
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/groups/%s", API_ENDPOINT, requestorUserID, group.getGroupID());
        ResultActions performAction = mockMvc.perform(
                delete(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isNoContent());
    }
    
    @Test
    public void testUpdateGroup_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto requestorUser = createAuthenticatedUser("Requestor");
        String requestorUserID = requestorUser.getUserDTO().getUserID().toString();
        // Crear grupo a actualizar
        Group group = userService.createGroup(UUID.fromString(requestorUserID), "Test group");
        // Datos a actualizar
        group.setName(group.getName() + "X");
        GroupDto paramsDto = UserConversor.toGroupDTO(group);
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/groups/%s", API_ENDPOINT, requestorUserID, group.getGroupID());
        ResultActions performAction = mockMvc.perform(
                put(endpointAddress)
                        .requestAttr("userID", UUID.fromString(requestorUserID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(encodedBodyContent)
        );
        
        // Comprobar resultados
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(encodedBodyContent));
    }
    
    @Test
    public void testGetFriendsFromGroup_GET() throws Exception {
        // Crear datos de prueba
        final int AMMOUNT_OF_USERS = 2;
        final int PAGE = 0;
        final int PAGE_SIZE = 5;
        AuthenticatedUserDto currentUser = createAuthenticatedUser("User");
        String currentUserID = currentUser.getUserDTO().getUserID().toString();
        List<User> targetUsersList = registerMultipleUsers(AMMOUNT_OF_USERS, this.userService);
        // Crear el grupo, agregar como amigos a los usuarios y meterlos en el grupo
        Group group = userService.createGroup(UUID.fromString(currentUserID), "Test Group");
        for (User target : targetUsersList ) {
            userService.requestFriendship(UUID.fromString(currentUserID), target.getUserID());
            userService.acceptFriendshipRequest(target.getUserID(), UUID.fromString(currentUserID));
            userService.addFriendToGroup(UUID.fromString(currentUserID), target.getUserID(), group.getGroupID());
        }
        Block<User> friendsInGroup = userService.getFriendsFromGroup(group.getGroupID(), PAGE, PAGE_SIZE);
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/groups/%s/people", API_ENDPOINT, currentUserID, group.getGroupID());
        ResultActions performAction = mockMvc.perform(
                get(endpointAddress)
                        .requestAttr("userID", UUID.fromString(currentUserID))
        );
        BlockDto<UserDto> expectedResponseBody = CommonConversor.toBlockDTO(friendsInGroup, UserConversor::toUserDto);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testAddUserToGroup_POST() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto currentUser = createAuthenticatedUser("User");
        String currentUserID = currentUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Crear el grupo, agregar como amigo al usuario y meterlo en el grupo
        Group group = userService.createGroup(UUID.fromString(currentUserID), "Test Group");
        userService.requestFriendship(UUID.fromString(currentUserID), UUID.fromString(targetUserID));
        userService.acceptFriendshipRequest(UUID.fromString(targetUserID), UUID.fromString(currentUserID));
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/groups/%s/add/%s", API_ENDPOINT, currentUserID, group.getGroupID(), targetUserID);
        ResultActions performAction = mockMvc.perform(
                post(endpointAddress)
                        .requestAttr("userID", UUID.fromString(currentUserID))
        );
        FriendshipDto expectedResponseBody = fetchFriendshipAsDTO(currentUserID, targetUserID);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
    @Test
    public void testRemoveUserFromGroup_DELETE() throws Exception {
        // Crear datos de prueba
        AuthenticatedUserDto currentUser = createAuthenticatedUser("User");
        String currentUserID = currentUser.getUserDTO().getUserID().toString();
        AuthenticatedUserDto targetUser = createAuthenticatedUser("Target");
        String targetUserID = targetUser.getUserDTO().getUserID().toString();
        // Crear el grupo, agregar como amigo al usuario y meterlo en el grupo
        Group group = userService.createGroup(UUID.fromString(currentUserID), "Test Group");
        userService.requestFriendship(UUID.fromString(currentUserID), UUID.fromString(targetUserID));
        userService.acceptFriendshipRequest(UUID.fromString(targetUserID), UUID.fromString(currentUserID));
        userService.addFriendToGroup(UUID.fromString(currentUserID), UUID.fromString(targetUserID), group.getGroupID());
        
        // Ejecutar funcionalidades
        String endpointAddress = String.format("%s/friends/%s/groups/%s/remove/%s", API_ENDPOINT, currentUserID, group.getGroupID(), targetUserID);
        ResultActions performAction = mockMvc.perform(
                delete(endpointAddress)
                        .requestAttr("userID", UUID.fromString(currentUserID))
        );
        FriendshipDto expectedResponseBody = fetchFriendshipAsDTO(currentUserID, targetUserID);
        String expectedResponseBodyAsJSON = this.mapper.writeValueAsString(expectedResponseBody);
        
        // Comprobar resultados
        performAction
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseBodyAsJSON));
    }
    
}
