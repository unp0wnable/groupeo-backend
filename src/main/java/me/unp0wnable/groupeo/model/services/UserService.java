package me.unp0wnable.groupeo.model.services;

import me.unp0wnable.groupeo.model.entities.User;
import me.unp0wnable.groupeo.model.entities.UserAddress;
import me.unp0wnable.groupeo.model.exceptions.*;

import java.util.UUID;

public interface UserService {

    User signUp(User profile) throws InstanceAlreadyExistsException;
    
    User login(String nickName, String rawPassword) throws IncorrectLoginException;
    
    User loginFromServiceToken(UUID userID) throws InstanceNotFoundException;
    
    void changePassword(UUID id, String oldPassword, String newPassword) throws InstanceNotFoundException,
                                                                                IncorrectPasswordExcepion;
    
    User updateUserProfile(UUID userID, User profile) throws InstanceNotFoundException;
    
    UserAddress assignAddressToUser(UUID userID, UserAddress address) throws InstanceNotFoundException;
    
    void deleteUser(UUID userID) throws InstanceNotFoundException;
}
