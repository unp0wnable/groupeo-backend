package me.unp0wnable.groupeo.model.exceptions;

public class TargetUserIsCurrentUserException extends Exception {
    public TargetUserIsCurrentUserException() {
        super("Target user is current user");
    }
    
    public TargetUserIsCurrentUserException(String message) {
        super(message);
    }
}
