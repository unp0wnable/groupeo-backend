package me.unp0wnable.groupeo.model.exceptions;

import java.util.UUID;

public class TargetUserIsNotFriendException extends Exception {
    private final UUID user;
    
    public TargetUserIsNotFriendException(UUID user) {
        super(user + " is already friend");
        this.user = user;
    }
}
