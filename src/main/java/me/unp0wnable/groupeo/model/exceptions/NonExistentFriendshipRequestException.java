package me.unp0wnable.groupeo.model.exceptions;

import java.util.UUID;

public class NonExistentFriendshipRequestException extends Exception {
    private final UUID requestorID;
    private final UUID targetID;
    
    public NonExistentFriendshipRequestException(UUID requestorID, UUID targetID) {
        super(requestorID + " has not any friendship with " + targetID);
        this.requestorID = requestorID;
        this.targetID = targetID;
    }
}
