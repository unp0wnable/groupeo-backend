package me.unp0wnable.groupeo.model.exceptions;

public class TargetUserIsCurrentUser extends Exception {
    public TargetUserIsCurrentUser() {
        super("Target user is current user");
    }
    
    public TargetUserIsCurrentUser(String message) {
        super(message);
    }
}
