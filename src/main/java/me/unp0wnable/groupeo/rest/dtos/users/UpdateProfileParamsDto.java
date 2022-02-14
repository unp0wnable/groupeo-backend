package me.unp0wnable.groupeo.rest.dtos.users;

import lombok.*;

import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateProfileParamsDto {
    @Size(min = 1, max = 50)
    private String firstName;
    
    @Size(min = 1, max = 50)
    private String surname1;
    
    @Size(min = 1, max = 50)
    private String surname2;
    
    @Size(min = 1, max = 100)
    private String email;
    
    private String description;
}
