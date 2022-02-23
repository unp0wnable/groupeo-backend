package me.unp0wnable.groupeo.model.exceptions;

public class InstanceNotFoundException extends AbstractInstanceException {
    public InstanceNotFoundException(String name, Object key) {
        super(name, key);
    }
}
