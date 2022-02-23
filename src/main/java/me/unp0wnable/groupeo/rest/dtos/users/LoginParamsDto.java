package me.unp0wnable.groupeo.rest.dtos.users;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginParamsDto {
    @NotNull
    @Size(min = 1, max = 50)
    private String nickName;
    
    @NotNull
    private String password;
}
