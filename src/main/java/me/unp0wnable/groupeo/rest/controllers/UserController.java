package me.unp0wnable.groupeo.rest.controllers;

import me.unp0wnable.groupeo.model.entities.UserAddress;
import me.unp0wnable.groupeo.model.entities.UserProfile;
import me.unp0wnable.groupeo.model.exceptions.*;
import me.unp0wnable.groupeo.model.services.UserService;
import me.unp0wnable.groupeo.rest.dtos.conversors.UserConversor;
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
    
    
    /* ************************************************* ENDPOINTS ************************************************* */
    @PostMapping("/signUp")
    public ResponseEntity<AuthenticatedUserDto> signUp(@Validated @RequestBody SignUpParamsDto params)
            throws InstanceAlreadyExistsException {
        // Parsear datos del usuario recibidos en el DTO y registrar al usuario en el servicio
        UserProfile user = UserConversor.fromSignUpParamsDTO(params);
        userService.signUp(user);
        
        // Genera los datos que contendrá la respuesta
        URI resourceLocation = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{userId}")
                .buildAndExpand(user.getUserProfileID())
                .toUri();
        String token = generateServiceTokenForUser(user);
        AuthenticatedUserDto authUserDto = UserConversor.toAuthenticatedUserDTO(user, token);
        
        // Crea la respuesta HTTP y la envía
        ResponseEntity<AuthenticatedUserDto> response = ResponseEntity
                .created(resourceLocation)
                .contentType(MediaType.APPLICATION_JSON)
                .body(authUserDto);
        
        return response;
    }
    
    
    @PostMapping("/login")
    public AuthenticatedUserDto login(@Validated @RequestBody LoginParamsDto params) throws IncorrectLoginException {
        // Inicia sesión en el servicio
        UserProfile user = userService.login(params.getNickName(), params.getPassword());
        
        // Genera el token para el usuario
        String token = generateServiceTokenForUser(user);
        
        // Crea la respuesta y la envía
        AuthenticatedUserDto authUserDto = UserConversor.toAuthenticatedUserDTO(user, token);
        
        return authUserDto;
    }
    
    
    @PostMapping("/tokenLogin")
    public AuthenticatedUserDto loginUsingServiceToken(@RequestAttribute UUID userID, @RequestAttribute String token)
            throws InstanceNotFoundException {
        // Inicia sesión en el servicio
        UserProfile user = userService.loginFromServiceToken(userID);
        
        // Devuelve los datos del usuario junto al token recibido
        return UserConversor.toAuthenticatedUserDTO(user, token);
    }
    
    
    @PutMapping("/{userID}/changePassword")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestAttribute UUID userID, @PathVariable("userID") UUID pathUserID,
                                @Validated @RequestBody ChangePasswordParamsDto params) throws PermissionException, InstanceNotFoundException,
                                                                                               IncorrectPasswordExcepion {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, pathUserID)) {
            throw new PermissionException();
        }
        
        // Actualizar contraseña en el servicio
        userService.changePassword(userID, params.getOldPassword(), params.getNewPassword());
    }
    
    
    @PutMapping("/{userID}/update")
    public UserDTO updateProfile(@RequestAttribute UUID userID, @PathVariable("userID") UUID pathUserID,
                                @Validated @RequestBody UpdateProfileParamsDto params)
            throws PermissionException, InstanceNotFoundException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, pathUserID)) {
            throw new PermissionException();
        }
        
        // Actualizar perfil en el servicio
        UserProfile userData = UserConversor.fromUpdateProfileParamsDTO(params);
        UserProfile updatedUser = userService.updateUserProfile(userID, userData);
        
        // Generar respuesta
        UserDTO dto = UserConversor.toUserDto(updatedUser);
        
        return dto;
    }
    
    
    @PutMapping("/{userID}/updateAddress")
    public AddressDTO updateUserAddress(@RequestAttribute UUID userID, @PathVariable("userID") UUID pathUserID,
                                        @Validated @RequestBody AddressDTO params)
            throws PermissionException, InstanceNotFoundException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, pathUserID)) {
            throw new PermissionException();
        }
    
        // Actualizar dirección en el servicio
        UserAddress address = UserConversor.fromAddressDTO(params);
        UserAddress updatedAddress = userService.updateUserAddress(userID, address);
    
        // Generar respuesta
        AddressDTO dto = UserConversor.toAddressDto(updatedAddress);
    
        return dto;
    }
    
    
    @DeleteMapping("/{userID}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    ResponseEntity<Void> deleteUser(@RequestAttribute UUID userID, @PathVariable("userID") UUID pathUserID)
            throws PermissionException, InstanceNotFoundException {
        // Comprobar que el usuario actual es quién dice ser
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
    
    /** Comprueba si un usuario está autorizado para realizar la operación */
    private boolean doUsersMatch(UUID requestUserID, UUID pathUserID) {
        return requestUserID.equals(pathUserID);
    }

}
