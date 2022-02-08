package me.unp0wnable.groupeo.model.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
public abstract class AbstractInstanceException extends Exception {
    @NotBlank
    private String name;
    
    @NotNull
    private Object key;
    
    protected AbstractInstanceException(String message) {
        super(message);
    }
}
