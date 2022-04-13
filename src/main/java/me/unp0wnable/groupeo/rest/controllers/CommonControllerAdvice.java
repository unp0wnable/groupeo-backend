package me.unp0wnable.groupeo.rest.controllers;

import me.unp0wnable.groupeo.model.exceptions.*;
import me.unp0wnable.groupeo.rest.dtos.errors.ErrorsDto;
import me.unp0wnable.groupeo.rest.exceptions.PermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@ControllerAdvice
public class CommonControllerAdvice {
    @Autowired
    private MessageSource messageSource;
    
    /* ********************************************* EXCEPTION HANDLERS *********************************************  */
    // Referencias a los errores en los ficheros de i18n
    public static final String INSTANCE_ALREADY_EXISTS_EXCEPTION_KEY       = "project.exceptions.common.InstanceAlreadyExistsException";
    public static final String INSTANCE_NOT_FOUND_EXCEPTION_KEY            = "project.exceptions.common.InstanceNotFoundException";
    public static final String PERMISSION_EXCEPTION_KEY                    = "project.exceptions.common.PermissionException";
    public static final String BLOCKED_USER_EXCEPTION_KEY                  = "project.exceptions.common.BlockdUserException";
    public static final String TARGET_USER_IS_CURRENT_USER_EXCEPTION_KEY   = "project.exceptions.common.TargetUserIsCurrentUserException";
    public static final String TARGET_USER_IS_NOT_FRIEND_EXCEPTION_KEY     = "project.exceptions.common.TargetUserIsNotFriendException";

    
    @ExceptionHandler(InstanceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorsDto handleInstanceAlreadyExistsException(InstanceAlreadyExistsException exception, Locale locale) {
        String exceptionNameMessage = messageSource.getMessage(
                exception.getName(), null, exception.getName(), locale
        );
        String globalErrorMessage = messageSource.getMessage(
                INSTANCE_ALREADY_EXISTS_EXCEPTION_KEY,
                new Object[] {exceptionNameMessage, exception.getKey().toString()},
                INSTANCE_ALREADY_EXISTS_EXCEPTION_KEY,
                locale
        );
        
        return new ErrorsDto(globalErrorMessage);
    }
    
    @ExceptionHandler(InstanceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorsDto handleInstanceNotFoundException(InstanceNotFoundException exception, Locale locale) {
        String exceptionNameMessage = messageSource.getMessage(
                exception.getName(), null, exception.getName(), locale
        );
        String globalErrorMessage = messageSource.getMessage(
                INSTANCE_NOT_FOUND_EXCEPTION_KEY,
                new Object[] {exceptionNameMessage, exception.getKey().toString()},
                INSTANCE_NOT_FOUND_EXCEPTION_KEY,
                locale
        );
        
        return new ErrorsDto(globalErrorMessage);
    }
    
    @ExceptionHandler(PermissionException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorsDto handlePermissionException(PermissionException exception, Locale locale) {
        String globalErrorMessage = messageSource.getMessage(
                PERMISSION_EXCEPTION_KEY,
                null,
                PERMISSION_EXCEPTION_KEY,
                locale
        );
        
        return new ErrorsDto(globalErrorMessage);
    }
    
    @ExceptionHandler(BlockedUserException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorsDto handleBlockedUserException(BlockedUserException exception, Locale locale) {
        String globalErrorMessage = messageSource.getMessage(
                BLOCKED_USER_EXCEPTION_KEY,
                null,
                BLOCKED_USER_EXCEPTION_KEY,
                locale
        );
        
        return new ErrorsDto(globalErrorMessage);
    }
    
    @ExceptionHandler(TargetUserIsCurrentUserException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorsDto handleTargetUserIsCurrentUserException(TargetUserIsCurrentUserException exception, Locale locale) {
        String globalErrorMessage = messageSource.getMessage(
                TARGET_USER_IS_CURRENT_USER_EXCEPTION_KEY,
                null,
                TARGET_USER_IS_CURRENT_USER_EXCEPTION_KEY,
                locale
        );
        
        return new ErrorsDto(globalErrorMessage);
    }
    
    @ExceptionHandler(TargetUserIsNotFriendException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorsDto handleTargetUserIsNotFriendException(TargetUserIsNotFriendException exception, Locale locale) {
        String globalErrorMessage = messageSource.getMessage(
                TARGET_USER_IS_NOT_FRIEND_EXCEPTION_KEY,
                null,
                TARGET_USER_IS_NOT_FRIEND_EXCEPTION_KEY,
                locale
        );
        
        return new ErrorsDto(globalErrorMessage);
    }
}
