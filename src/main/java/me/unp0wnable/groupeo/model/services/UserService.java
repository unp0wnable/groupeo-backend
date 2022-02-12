package me.unp0wnable.groupeo.model.services;

import me.unp0wnable.groupeo.model.entities.UserAddress;
import me.unp0wnable.groupeo.model.entities.UserProfile;
import me.unp0wnable.groupeo.model.exceptions.*;

import java.util.UUID;

public interface UserService {

    void signUp(UserProfile profile) throws InstanceAlreadyExistsException;
    
    UserProfile login(String nickName, String rawPassword) throws IncorrectLoginException;
    
    UserProfile loginFromServiceToken(UUID id) throws InstanceNotFoundException;
    
    void changePassword(UUID id, String oldPassword, String newPassword) throws InstanceNotFoundException,
                                                                                IncorrectPasswordExcepion;
    
    UserProfile updateUserProfile(UUID id, UserProfile profile) throws InstanceNotFoundException;
    
    UserAddress updateUserAddress(UUID userID, UserAddress addressData) throws InstanceNotFoundException;
    
    void deleteUser(UUID id) throws InstanceNotFoundException;
}
