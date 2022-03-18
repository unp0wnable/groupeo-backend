package me.unp0wnable.groupeo.model.exceptions;

import java.util.UUID;

public class UserNotInGroupException extends AbstractInstanceException {
    public UserNotInGroupException(UUID userID, Object key) {
        super(userID.toString(), key);
    }
}
