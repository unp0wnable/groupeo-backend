package me.unp0wnable.groupeo.rest.dtos.users;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUserDto {
    @JsonProperty("serviceToken")
    private String serviceToken;
    
    @JsonProperty("user")
    private UserDTO userDTO;
}
