package me.unp0wnable.groupeo.rest.dtos.users;

import lombok.*;

import javax.validation.constraints.Size;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpParamsDto {
    @Size(min = 1, max = 50)
    private String firstName;
    
    @Size(min = 1, max = 50)
    private String surname1;
    
    @Size(min = 1, max = 50)
    private String surname2;
    
    @Size(min = 1, max = 50)
    private String nickName;
    
    private String rawPassword;
    
    private String email;
    
    private Date birthDate;
    
    private String imageB64;
}
