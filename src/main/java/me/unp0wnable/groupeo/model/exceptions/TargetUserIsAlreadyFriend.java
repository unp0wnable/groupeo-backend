package me.unp0wnable.groupeo.model.exceptions;

import java.util.UUID;

public class TargetUserIsAlreadyFriend extends Exception {
    private UUID user;
    
    public TargetUserIsAlreadyFriend(UUID user) {
        super(user + " is already friend");
        this.user = user;
    }
}
