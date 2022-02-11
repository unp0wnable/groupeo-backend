package me.unp0wnable.groupeo.rest.dtos.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateProfileParamsDTO {
    @Size(min = 1, max = 50)
    private String firstName;
    
    @Size(min = 1, max = 50)
    private String surname1;
    
    @Size(min = 1, max = 50)
    private String surname2;
}
