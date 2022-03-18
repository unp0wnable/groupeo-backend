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
    Friendship addFriend(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException,
                                                                         TargetUserIsCurrentUserException,
                                                                         TargetUserIsAlreadyFriendException,
                                                                         BlockedUserException;
    
    void removeFriend(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException,
                                                                      TargetUserIsCurrentUserException,
                                                                      TargetUserIsNotFriendException,
                                                                      BlockedUserException;
    
    Friendship acceptFriendRequest(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, TargetUserIsCurrentUserException, NonExistentFriendshipRequestException;
    
    Friendship declineFriendRequest(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, TargetUserIsCurrentUserException, NonExistentFriendshipRequestException;
    
    Friendship blockFriend(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, TargetUserIsCurrentUserException;
    
    void unblockFriend(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, TargetUserIsCurrentUserException;
    
    Block<User> getBlockedUsers(UUID userID, int page, int pageSize) throws InstanceNotFoundException;
    
    Block<User> getUserFriends(UUID userID, int page, int pageSize) throws InstanceNotFoundException;
    
    Friendship getFriendshipInfoWithUser(UUID requestorUserID, UUID targetUserID) throws InstanceNotFoundException, TargetUserIsCurrentUserException;
    
    Group createGroup(UUID ownerID, String name) throws InstanceAlreadyExistsException, InstanceNotFoundException;
    
    void deleteGroup(UUID ownerID, UUID groupID) throws InstanceNotFoundException;
    
    Group updateGroupData(UUID ownerID, UUID groupId, Group groupData) throws InstanceNotFoundException;
    
    Block<User> getFriendsFromGroup(UUID groupID, int page, int pageSize) throws InstanceNotFoundException;
    
    Friendship addFriendToGroup(UUID requestorUserID, UUID targetUserID, UUID groupID) throws InstanceNotFoundException, TargetUserIsCurrentUserException, InstanceAlreadyExistsException;
    
    Friendship removeFriendFromGroup(UUID requestorUserID, UUID targetUserID, UUID groupID) throws InstanceNotFoundException, TargetUserIsCurrentUserException;
    
}
