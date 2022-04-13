package me.unp0wnable.groupeo.rest.controllers;

import me.unp0wnable.groupeo.model.entities.*;
import me.unp0wnable.groupeo.model.exceptions.*;
import me.unp0wnable.groupeo.model.services.UserService;
import me.unp0wnable.groupeo.rest.dtos.common.BlockDto;
import me.unp0wnable.groupeo.rest.dtos.conversors.CommonConversor;
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
    private UserService userService;
    @Autowired
    private JwtGenerator jwtGenerator;
    @Autowired
    private MessageSource messageSource;
    
    
    /* ********************************************* EXCEPTION HANDLERS ********************************************* */
    public static final String INCORRECT_LOGIN_EXCEPTION_KEY                   = "project.exceptions.user.IncorrectLoginException";
    public static final String INCORRECT_PASSWORD_EXCEPTION_KEY                = "project.exceptions.user.IncorrectPasswordException";
    public static final String NON_EXISTENT_FRIENDSHIP_EXCEPTION_KEY           = "project.exceptions.user.NonExistentFriendshipException";
    public static final String TARGET_USER_IS_ALREADY_FRIEND_EXCEPTION_KEY     = "project.exceptions.user.TargetUserIsAlreadyFriendException";
    public static final String USER_NOT_IN_GROUP_EXCEPTION_KEY                 = "project.exceptions.user.UserNotInGroupException";
    
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
    
    @ExceptionHandler(NonExistentFriendshipException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorsDto handleNonExistentFriendshipException(NonExistentFriendshipException exception, Locale locale) {
        String errorMessage = messageSource.getMessage(
                NON_EXISTENT_FRIENDSHIP_EXCEPTION_KEY, null, NON_EXISTENT_FRIENDSHIP_EXCEPTION_KEY, locale
        );
        
        return new ErrorsDto(errorMessage);
    }
    
    @ExceptionHandler(TargetUserIsAlreadyFriendException.class)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ResponseBody
    public ErrorsDto handleTargetUserIsAlreadyFriendException(TargetUserIsAlreadyFriendException exception, Locale locale) {
        String errorMessage = messageSource.getMessage(
                TARGET_USER_IS_ALREADY_FRIEND_EXCEPTION_KEY, null, TARGET_USER_IS_ALREADY_FRIEND_EXCEPTION_KEY, locale
        );
        
        return new ErrorsDto(errorMessage);
    }
    
    @ExceptionHandler(UserNotInGroupException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorsDto handleUserNotInGroupException(UserNotInGroupException exception, Locale locale) {
        String errorMessage = messageSource.getMessage(
                USER_NOT_IN_GROUP_EXCEPTION_KEY, null, USER_NOT_IN_GROUP_EXCEPTION_KEY, locale
        );
        
        return new ErrorsDto(errorMessage);
    }
    
    /* ************************************************* ENDPOINTS ************************************************* */
    /* *********************************** User profile *********************************** */
    @PostMapping("/signUp")
    public ResponseEntity<AuthenticatedUserDto> signUp(@Validated @RequestBody SignUpParamsDto params)
            throws InstanceAlreadyExistsException {
        // Parsear datos del usuario recibidos en el DTO y registrar al usuario en el servicio
        User parsedUser = UserConversor.fromSignUpParamsDTO(params);
        User signedUpUser = userService.signUp(parsedUser);
        
        // Genera los datos que contendrá la respuesta
        URI resourceLocation = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{userId}")
                .buildAndExpand(signedUpUser.getUserID())
                .toUri();
        String token = generateServiceTokenForUser(signedUpUser);
        AuthenticatedUserDto authUserDto = UserConversor.toAuthenticatedUserDTO(signedUpUser, token);
        
        // Crea la respuesta HTTP y la envía
        return ResponseEntity
                .created(resourceLocation)
                .contentType(MediaType.APPLICATION_JSON)
                .body(authUserDto);
    }
    
    
    @PostMapping("/login")
    public AuthenticatedUserDto login(@Validated @RequestBody LoginParamsDto params) throws IncorrectLoginException {
        // Inicia sesión en el servicio
        User user = userService.login(params.getNickName(), params.getPassword());
        
        // Genera el token para el usuario
        String serviceToken = generateServiceTokenForUser(user);
        
        // Crea la respuesta y la envía
        return UserConversor.toAuthenticatedUserDTO(user, serviceToken);
    }
    
    
    @PostMapping("/tokenLogin")
    public AuthenticatedUserDto loginUsingServiceToken(@RequestAttribute UUID userID, @RequestAttribute String serviceToken)
            throws InstanceNotFoundException {
        // Inicia sesión en el servicio
        User user = userService.loginFromServiceToken(userID);
        
        // Devuelve los datos del usuario junto al token recibido
        return UserConversor.toAuthenticatedUserDTO(user, serviceToken);
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
    
    
    @PutMapping("/{userID}")
    public UserDto updateProfile(@RequestAttribute UUID userID, @PathVariable("userID") UUID pathUserID,
                                @Validated @RequestBody UpdateProfileParamsDto params)
            throws PermissionException, InstanceNotFoundException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, pathUserID)) {
            throw new PermissionException();
        }
        
        // Actualizar perfil en el servicio
        User userData = UserConversor.fromUpdateProfileParamsDTO(params);
        User updatedUser = userService.updateUserProfile(userID, userData);
        
        // Generar respuesta
        return UserConversor.toUserDto(updatedUser);
    }
    
    @PostMapping("/{userID}/address")
    public AddressDto assignAddressToUser(@RequestAttribute UUID userID, @PathVariable("userID") UUID pathUserID,
                                        @Validated @RequestBody AddressDto params)
            throws PermissionException, InstanceNotFoundException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, pathUserID)) {
            throw new PermissionException();
        }
        
        // Insertar dirección del usuario en el servicio
        UserAddress address = UserConversor.fromAddressDTO(params);
        UserAddress createdAddress = userService.assignAddressToUser(userID, address);
        
        // Generar respuesta
        return UserConversor.toAddressDto(createdAddress);
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
        return ResponseEntity.noContent()
                             .build();
    }
    
    /* *********************************** User relationships *********************************** */
    @PostMapping("/friends/{requestorID}/add/{targetID}")
    public FriendshipDto requestFriendship(@RequestAttribute UUID userID,
                                           @PathVariable("requestorID") UUID requestorID,
                                           @PathVariable("targetID") UUID targetID)
            throws PermissionException, BlockedUserException, TargetUserIsCurrentUserException,
                   TargetUserIsAlreadyFriendException, InstanceNotFoundException, InstanceAlreadyExistsException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, requestorID)) {
            throw new PermissionException();
        }
        
        // Enviar petición de amistad al usuario objetivo en el servicio
        Friendship friendshipRequest = userService.requestFriendship(userID, targetID);
        
        // Generar respuesta
        return UserConversor.toFriendshipDTO(friendshipRequest);
    }
    
    @DeleteMapping("/friends/{requestorID}/remove/{targetID}")
    public ResponseEntity<Void> removeFriend(@RequestAttribute UUID userID,
                                             @PathVariable("requestorID") UUID requestorID,
                                             @PathVariable("targetID") UUID targetID)
            throws PermissionException, BlockedUserException, TargetUserIsCurrentUserException, TargetUserIsNotFriendException,
                   InstanceNotFoundException{
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, requestorID)) {
            throw new PermissionException();
        }
        
        // Eliminar amigo en el servicio
        userService.removeFriend(userID, targetID);
        
        // Generar respuesta
        return ResponseEntity.noContent()
                             .build();
    }
    
    @PostMapping("/friends/{requestorID}/accept/{targetID}")
    public FriendshipDto acceptFriend(@RequestAttribute UUID userID,
                                      @PathVariable("requestorID") UUID requestorID,
                                      @PathVariable("targetID") UUID targetID)
            throws PermissionException, TargetUserIsCurrentUserException, TargetUserIsAlreadyFriendException,
                   InstanceNotFoundException, NonExistentFriendshipException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, requestorID)) {
            throw new PermissionException();
        }
        
        // Aceptar petición de amistad al usuario objetivo en el servicio
        Friendship friendship = userService.acceptFriendshipRequest(userID, targetID);
        
        // Generar respuesta
        return UserConversor.toFriendshipDTO(friendship);
    }
    
    @PostMapping("/friends/{requestorID}/decline/{targetID}")
    public FriendshipDto declineFriendship(@RequestAttribute UUID userID,
                                           @PathVariable("requestorID") UUID requestorID,
                                           @PathVariable("targetID") UUID targetID)
            throws PermissionException, TargetUserIsCurrentUserException, InstanceNotFoundException, NonExistentFriendshipException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, requestorID)) {
            throw new PermissionException();
        }
        
        // Aceptar petición de amistad al usuario objetivo en el servicio
        Friendship declinedFriendship = userService.declineFriendshipRequest(userID, targetID);
        
        // Generar respuesta
        return UserConversor.toFriendshipDTO(declinedFriendship);
    }
    
    @PostMapping("/friends/{requestorID}/block/{targetID}")
    public FriendshipDto blockFriend(@RequestAttribute UUID userID,
                                     @PathVariable("requestorID") UUID requestorID,
                                     @PathVariable("targetID") UUID targetID)
            throws PermissionException, TargetUserIsCurrentUserException, InstanceNotFoundException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, requestorID)) {
            throw new PermissionException();
        }
        
        // Bloquear amigo en el servicio
        Friendship blockedFriendship = userService.blockFriend(userID, targetID);
        
        // Generar respuesta
        return UserConversor.toFriendshipDTO(blockedFriendship);
    }
    
    @PostMapping("/friends/{requestorID}/unblock/{targetID}")
    public ResponseEntity<Void> unblockFriend(@RequestAttribute UUID userID,
                                              @PathVariable("requestorID") UUID requestorID,
                                              @PathVariable("targetID") UUID targetID)
            throws PermissionException, TargetUserIsCurrentUserException, InstanceNotFoundException, NonExistentFriendshipException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, requestorID)) {
            throw new PermissionException();
        }
        
        // Desbloquear amigo en el servicio
        userService.unblockFriend(userID, targetID);
        
        // Generar respuesta
        return ResponseEntity.noContent()
                             .build();
    }
    
    @GetMapping("/friends/{requestorID}/blocked")
    public BlockDto<UserDto> getBlockedUsers(@RequestAttribute UUID userID,
                                             @PathVariable("requestorID") UUID requestorID,
                                             @RequestParam(name = "page", defaultValue = "0") int page,
                                             @RequestParam(name = "pageSize", defaultValue = "10") int pageSize)
            throws PermissionException, InstanceNotFoundException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, requestorID)) {
            throw new PermissionException();
        }
        
        // Obtener usuarios bloqueados por el usuario desde el servicio
        Block<User> usersBlock = userService.getBlockedUsers(userID, page, pageSize);
        
        // Generar respuesta
        return CommonConversor.toBlockDTO(usersBlock, UserConversor::toUserDto);
    }
    
    @GetMapping("/friends/{requestorID}")
    public BlockDto<UserDto> getUserFriends(@RequestAttribute UUID userID,
                                            @PathVariable("requestorID") UUID requestorID,
                                            @RequestParam(name = "page", defaultValue = "0") int page,
                                            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize)
            throws PermissionException, InstanceNotFoundException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, requestorID)) {
            throw new PermissionException();
        }
        
        // Obtener usuarios bloqueados por el usuario desde el servicio
        Block<User> usersBlock = userService.getUserFriends(userID, page, pageSize);
        
        // Generar respuesta
        return CommonConversor.toBlockDTO(usersBlock, UserConversor::toUserDto);
    }
    
    @GetMapping("/friends/{requestorID}/friendship/{targetID}")
    public FriendshipDto getFriendshipDataBetweenUsers(@RequestAttribute UUID userID,
                                                       @PathVariable("requestorID") UUID requestorID,
                                                       @PathVariable("targetID") UUID targetID)
            throws PermissionException, TargetUserIsCurrentUserException, InstanceNotFoundException,
                   NonExistentFriendshipException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, requestorID)) {
            throw new PermissionException();
        }
        
        // Aceptar petición de amistad al usuario objetivo en el servicio
        Friendship friendshipData = userService.getFriendshipInfoWithUser(userID, targetID);
        
        // Generar respuesta
        return UserConversor.toFriendshipDTO(friendshipData);
    }
    
    @PostMapping("/friends/{userID}/groups")
    public GroupDto createGroup(@RequestAttribute UUID userID,
                                @PathVariable("userID") UUID pathUserID,
                                @RequestBody CreateGroupParamsDto paramsDto)
            throws PermissionException, InstanceAlreadyExistsException, InstanceNotFoundException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, pathUserID)) {
            throw new PermissionException();
        }
        
        // Crear grupo
        Group group = userService.createGroup(userID, paramsDto.getName());
        
        // Generar respuesta
        return UserConversor.toGroupDTO(group);
    }
    
    @DeleteMapping("/friends/{userID}/groups/{groupID}")
    public ResponseEntity<Void> deleteGroup(@RequestAttribute UUID userID,
                                            @PathVariable("userID") UUID pathUserID,
                                            @PathVariable("groupID") UUID groupID)
            throws PermissionException, InstanceNotFoundException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, pathUserID)) {
            throw new PermissionException();
        }
        
        // Borrar grupo
        userService.deleteGroup(userID, groupID);
        
        // Generar respuesta
        return ResponseEntity.noContent()
                             .build();
    }
    
    @PutMapping("/friends/{userID}/groups/{groupID}")
    public GroupDto updateGroup(@RequestAttribute UUID userID,
                                @PathVariable("userID") UUID pathUserID,
                                @PathVariable("groupID") UUID groupID,
                                @RequestBody GroupDto paramsDto)
            throws PermissionException, InstanceNotFoundException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, pathUserID)) {
            throw new PermissionException();
        }
        
        // Actualizar datos del grupo
        Group groupdData = UserConversor.fromGroupDTO(paramsDto);
        Group group = userService.updateGroupData(userID, groupID, groupdData);
        
        // Generar respuesta
        return UserConversor.toGroupDTO(group);
    }
    
    @GetMapping("/friends/{userID}/groups/{groupID}/people")
    public BlockDto<UserDto> getFriendsFromGroup(@RequestAttribute UUID userID,
                                                 @PathVariable("userID") UUID pathUserID,
                                                 @PathVariable("groupID") UUID groupID,
                                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") int pageSize)
            throws PermissionException, InstanceNotFoundException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, pathUserID)) {
            throw new PermissionException();
        }
        
        // Obtener miembros del grupo
        Block<User> usersBlock = userService.getFriendsFromGroup(groupID, page, pageSize);
        
        // Generar respuesta
        return CommonConversor.toBlockDTO(usersBlock, UserConversor::toUserDto);
    }
    
    @PostMapping("/friends/{userID}/groups/{groupID}/add/{targetID}")
    public FriendshipDto addUserToGroup(@RequestAttribute UUID userID,
                                        @PathVariable("userID") UUID pathUserID,
                                        @PathVariable("groupID") UUID groupID,
                                        @PathVariable("targetID") UUID targetUserID)
            throws PermissionException, InstanceNotFoundException, NonExistentFriendshipException,
                   TargetUserIsCurrentUserException, InstanceAlreadyExistsException, BlockedUserException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, pathUserID)) {
            throw new PermissionException();
        }
        
        // Añadir usuario al grupo
        Friendship updatedFriendship = userService.addFriendToGroup(userID, targetUserID, groupID);
        
        // Generar respuesta
        return UserConversor.toFriendshipDTO(updatedFriendship);
    }
    
    @DeleteMapping("/friends/{userID}/groups/{groupID}/remove/{targetID}")
    public FriendshipDto removeUserFromGroup(@RequestAttribute UUID userID,
                                           @PathVariable("userID") UUID pathUserID,
                                           @PathVariable("groupID") UUID groupID,
                                           @PathVariable("targetID") UUID targetUserID)
            throws PermissionException, InstanceNotFoundException, NonExistentFriendshipException,
                   TargetUserIsCurrentUserException, UserNotInGroupException {
        // Comprobar que el usuario actual es quién dice ser
        if (!doUsersMatch(userID, pathUserID)) {
            throw new PermissionException();
        }
        
        // Añadir usuario al grupo
        Friendship updatedFriendship = userService.removeFriendFromGroup(userID, targetUserID, groupID);
        
        // Generar respuesta
        return UserConversor.toFriendshipDTO(updatedFriendship);
    }
    
    /* ************************************************* AUX METHODS ************************************************* */
    /** Genera un JWT para el usuario actual */
    private String generateServiceTokenForUser(User user) {
        JwtData jwtData = new JwtData(user.getUserID(), user.getNickName(), user.getRole().toString());
        
        return jwtGenerator.generateJWT(jwtData);
    }
    
    /** Comprueba si un usuario está autorizado para realizar la operación */
    private boolean doUsersMatch(UUID requestUserID, UUID pathUserID) {
        return requestUserID.equals(pathUserID);
    }

}
