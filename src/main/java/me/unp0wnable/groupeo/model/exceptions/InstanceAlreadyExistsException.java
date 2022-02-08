package me.unp0wnable.groupeo.model.exceptions;

public class InstanceAlreadyExistsException extends AbstractInstanceException {
    public InstanceAlreadyExistsException(String name, Object key) {
        super (name, key);
    }
}
