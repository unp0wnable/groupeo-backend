package me.unp0wnable.groupeo.model.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IncorrectLoginException extends Exception {
    private final String nickName;
    private final String password;
}
