package me.unp0wnable.groupeo.rest.dtos.users;

import lombok.Data;

@Data
public class ChangePasswordParamsDto {
    private String oldPassword;
    private String newPassword;
}
