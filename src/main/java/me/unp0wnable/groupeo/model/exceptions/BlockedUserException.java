package me.unp0wnable.groupeo.model.exceptions;

import java.util.UUID;

public class BlockedUserException extends Exception {
    private UUID blockedUserID;
    
    public BlockedUserException(UUID blockedUserID) {
        super(String.format(
                "User with id %s is blocked",
                blockedUserID
              )
        );
    }
}
