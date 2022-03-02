package me.unp0wnable.groupeo.model.services;

import me.unp0wnable.groupeo.model.entities.*;
import me.unp0wnable.groupeo.model.exceptions.*;

import java.util.UUID;

public interface UserService {
    /* *********************************** User profile *********************************** */
    User signUp(User profile) throws InstanceAlreadyExistsException;
    
    User login(String nickName, String rawPassword) throws IncorrectLoginException;
    
    User loginFromServiceToken(UUID userID) throws InstanceNotFoundException;
    
    void changePassword(UUID id, String oldPassword, String newPassword) throws InstanceNotFoundException, IncorrectPasswordExcepion;
    
    User updateUserProfile(UUID userID, User profile) throws InstanceNotFoundException;
    
    UserAddress assignAddressToUser(UUID userID, UserAddress address) throws InstanceNotFoundException;
    
    void deleteUser(UUID userID) throws InstanceNotFoundException;
    
    
    /* *********************************** User relationships *********************************** */
    FriendshipStatus addFriend(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException,
                                                                               TargetUserIsCurrentUser,
                                                                               TargetUserIsAlreadyFriend,
                                                                               BlockedUserException;
    
    /*void removeFriend(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, InvalidUserException;
    
    FriendshipStatus acceptFriendRequest(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, InvalidUserException;
    
    FriendshipStatus declineFriendRequest(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, InvalidUserException;
    
    FriendshipStatus blockFriend(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, InvalidUserException;
    
    void unblockFriend(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, InvalidUserException;
    
    Block<User> getBlockedUsers(UUID userID) throws InstanceNotFoundException;
    
    Block<User> getUserFriends(UUID userID) throws InstanceNotFoundException;
    
    FriendshipStatus getCurrentFriendshipStatus(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, InvalidUserException;
    
    Group createGroup(UUID ownerID, String name) throws InstanceAlreadyExistsException;
    
    void deleteGroup(UUID ownerID, UUID groupId) throws InstanceNotFoundException;
    
    void updateGroupData(UUID ownerID, UUID groupId) throws InstanceNotFoundException;
    
    Block<User> getFriendsFromGroup(UUID ownerID, UUID groupID) throws InstanceNotFoundException;
    
    Friendship addFriendToGroup(UUID requestorUserID, UUID targetUserID, UUID groupID) throws InstanceNotFoundException, InvalidUserException, InstanceAlreadyExistsException;
    
    Friendship removeFriendFromGroup(UUID requestorUserID, UUID targetUserID, UUID groupID) throws InstanceNotFoundException, InvalidUserException;
    */
}
