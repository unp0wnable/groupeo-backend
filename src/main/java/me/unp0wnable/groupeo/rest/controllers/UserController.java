package me.unp0wnable.groupeo.rest.controllers;

import me.unp0wnable.groupeo.model.entities.UserAddress;
import me.unp0wnable.groupeo.model.entities.UserProfile;
import me.unp0wnable.groupeo.model.exceptions.*;
import me.unp0wnable.groupeo.model.services.UserService;
import me.unp0wnable.groupeo.rest.dtos.conversors.UserConversor;
import me.unp0wnable.groupeo.rest.dtos.errors.ErrorsDto;
import me.unp0wnable.groupeo.rest.dtos.users.*;
import me.unp0wnable.groupeo.rest.exceptions.PermissionException;
import me.unp0wnable.groupeo.rest.http.jwt.JwtData;
import me.unp0wnable.groupeo.rest.http.jwt.JwtGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    JwtGenerator jwtGenerator;
    @Autowired
    MessageSource messageSource;
    
    /* ********************************************* EXCEPTION HANDLERS ********************************************* */
    private static final String INCORRECT_LOGIN_EXCEPTION_KEY = "project.exceptions.IncorrectLoginException";
    private static final String INCORRECT_PASSWORD_EXCEPTION_KEY = "project.exceptions.IncorrectPasswordException";
    
    
    @ExceptionHandler(IncorrectLoginException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorsDto handleIncorrectLoginException(IncorrectLoginException exception, Locale locale) {
        String errorMessage = messageSource.getMessage(
                INCORRECT_LOGIN_EXCEPTION_KEY, null, INCORRECT_LOGIN_EXCEPTION_KEY, locale
        );
    
        return new ErrorsDto(errorMessage);
    }
    
    @ExceptionHandler(IncorrectPasswordExcepion.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorsDto handleIncorrectPasswordException(IncorrectPasswordExcepion exception, Locale locale) {
        String errorMessage = messageSource.getMessage(
                INCORRECT_PASSWORD_EXCEPTION_KEY, null, INCORRECT_PASSWORD_EXCEPTION_KEY, locale
        );
        
        return new ErrorsDto(errorMessage);
    }
    
    
    /* ************************************************* ENDPOINTS ************************************************* */
    @PostMapping("/signUp")
    public ResponseEntity<AuthenticatedUserDto> signUp(@Validated @RequestBody SignUpParamsDto params)
            throws InstanceAlreadyExistsException {
        // Parsear datos del usuario recibidos en el DTO y registrar al usuario en el servicio
        UserProfile parsedUser = UserConversor.fromSignUpParamsDTO(params);
        UserProfile signedUpUser = userService.signUp(parsedUser);
        
        // Genera los datos que contendr?? la respuesta
        URI resourceLocation = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{userId}")
                .buildAndExpand(signedUpUser.getUserProfileID())
                .toUri();
        String token = generateServiceTokenForUser(signedUpUser);
        AuthenticatedUserDto authUserDto = UserConversor.toAuthenticatedUserDTO(signedUpUser, token);
        
        // Crea la respuesta HTTP y la env??a
        ResponseEntity<AuthenticatedUserDto> response = ResponseEntity
                .created(resourceLocation)
                .contentType(MediaType.APPLICATION_JSON)
                .body(authUserDto);
        
        return response;
    }
    
    
    @PostMapping("/login")
    public AuthenticatedUserDto login(@Validated @RequestBody LoginParamsDto params) throws IncorrectLoginException {
        // Inicia sesi??n en el servicio
        UserProfile user = userService.login(params.getNickName(), params.getPassword());
        
        // Genera el token para el usuario
        String serviceToken = generateServiceTokenForUser(user);
        
        // Crea la respuesta y la env??a
        return UserConversor.toAuthenticatedUserDTO(user, serviceToken);
    }
    
    
    @PostMapping("/tokenLogin")
    public AuthenticatedUserDto loginUsingServiceToken(@RequestAttribute UUID userID, @RequestAttribute String serviceToken)
            throws InstanceNotFoundException {
        // Inicia sesi??n en el servicio
        UserProfile user = userService.loginFromServiceToken(userID);
        
        // Devuelve los datos del usuario junto al token recibido
        return UserConversor.toAuthenticatedUserDTO(user, serviceToken);
    }
    
    
    @PutMapping("/{userID}/changePassword")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestAttribute UUID userID, @PathVariable("userID") UUID pathUserID,
                                @Validated @RequestBody ChangePasswordParamsDto params) throws PermissionException, InstanceNotFoundException,
                                                                                               IncorrectPasswordExcepion {
        // Comprobar que el usuario actual es qui??n dice ser
        if (!doUsersMatch(userID, pathUserID)) {
            throw new PermissionException();
        }
        
        // Actualizar contrase??a en el servicio
        userService.changePassword(userID, params.getOldPassword(), params.getNewPassword());
    }
    
    
    @PutMapping("/{userID}/update")
    public UserDto updateProfile(@RequestAttribute UUID userID, @PathVariable("userID") UUID pathUserID,
                                @Validated @RequestBody UpdateProfileParamsDto params)
            throws PermissionException, InstanceNotFoundException {
        // Comprobar que el usuario actual es qui??n dice ser
        if (!doUsersMatch(userID, pathUserID)) {
            throw new PermissionException();
        }
        
        // Actualizar perfil en el servicio
        UserProfile userData = UserConversor.fromUpdateProfileParamsDTO(params);
        UserProfile updatedUser = userService.updateUserProfile(userID, userData);
        
        // Generar respuesta
        return UserConversor.toUserDto(updatedUser);
    }
    
    @PostMapping("/{userID}/address")
    public AddressDto assignAddressToUser(@RequestAttribute UUID userID, @PathVariable("userID") UUID pathUserID,
                                        @Validated @RequestBody AddressDto params)
            throws PermissionException, InstanceNotFoundException {
        // Comprobar que el usuario actual es qui??n dice ser
        if (!doUsersMatch(userID, pathUserID)) {
            throw new PermissionException();
        }
        
        // Insertar direcci??n del usuario en el servicio
        UserAddress address = UserConversor.fromAddressDTO(params);
        UserAddress createdAddress = userService.assignAddressToUser(userID, address);
        
        // Generar respuesta
        return UserConversor.toAddressDto(createdAddress);
    }
    
    
    @DeleteMapping("/{userID}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    ResponseEntity<Void> deleteUser(@RequestAttribute UUID userID, @PathVariable("userID") UUID pathUserID)
            throws PermissionException, InstanceNotFoundException {
        // Comprobar que el usuario actual es qui??n dice ser
        if (!doUsersMatch(userID, pathUserID)) {
            throw new PermissionException();
        }
        
        // Eliminar usuario del servicio
        userService.deleteUser(userID);
        
        // Generar respuesta
        ResponseEntity<Void> response = ResponseEntity.noContent().build();
        
        return response;
    }
    
    
    /* ************************************************* AUX METHODS ************************************************* */
    /** Genera un JWT para el usuario actual */
    private String generateServiceTokenForUser(UserProfile user) {
        JwtData jwtData = new JwtData(user.getUserProfileID(), user.getNickName(), user.getRole().toString());
        
        return jwtGenerator.generateJWT(jwtData);
    }
    
    /** Comprueba si un usuario est?? autorizado para realizar la operaci??n */
    private boolean doUsersMatch(UUID requestUserID, UUID pathUserID) {
        return requestUserID.equals(pathUserID);
    }

}
