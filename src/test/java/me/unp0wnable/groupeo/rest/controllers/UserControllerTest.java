package me.unp0wnable.groupeo.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.unp0wnable.groupeo.model.entities.User;
import me.unp0wnable.groupeo.model.entities.UserAddress;
import me.unp0wnable.groupeo.model.exceptions.IncorrectLoginException;
import me.unp0wnable.groupeo.model.repositories.UserAddressRepository;
import me.unp0wnable.groupeo.model.repositories.UserRepository;
import me.unp0wnable.groupeo.rest.dtos.conversors.UserConversor;
import me.unp0wnable.groupeo.rest.dtos.users.*;
import me.unp0wnable.groupeo.rest.http.jwt.JwtData;
import me.unp0wnable.groupeo.rest.http.jwt.JwtGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
    private final String API_ENDPOINT = "/api/users";
    private final ObjectMapper mapper = new ObjectMapper();
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private JwtGenerator jwtGenerator;
    
    @Autowired
    private UserController userController;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserAddressRepository userAddressRepository;

    
    /* ****************************** AUX FUNCTIONS ****************************** */
    /** Registra un usuario válido en el sistema */
    private AuthenticatedUserDto createAuthenticatedUser(String nickName)
            throws IncorrectLoginException {
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
        String endpointAddress = API_ENDPOINT + "/" + NON_EXISTENT_USER_ID + "/changePassword";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                put(endpointAddress)
                        // Valores anotados como @RequestAttribute
                        .requestAttr("userID", NON_EXISTENT_USER_ID)
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
        String endpointAddress = API_ENDPOINT + "/" + userID + "/update";
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
        String endpointAddress = API_ENDPOINT + "/" + targetUserID + "/update";
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
        String endpointAddress = API_ENDPOINT + "/" + NON_EXISTENT_USER_ID + "/update";
        String encodedBodyContent = this.mapper.writeValueAsString(paramsDto);
        
        // Ejecutar funcionalidades
        var performAction = mockMvc.perform(
                put(endpointAddress)
                        // Valores anotados como @RequestAttribute
                        .requestAttr("userID", NON_EXISTENT_USER_ID)
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
}
