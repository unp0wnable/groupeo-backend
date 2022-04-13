package me.unp0wnable.groupeo.model.exceptions;

import java.util.UUID;

public class TargetUserIsAlreadyFriendException extends Exception {
    private final UUID user;
    
    public TargetUserIsAlreadyFriendException(UUID user) {
        super(user + " is already friend");
        this.user = user;
    }
}
