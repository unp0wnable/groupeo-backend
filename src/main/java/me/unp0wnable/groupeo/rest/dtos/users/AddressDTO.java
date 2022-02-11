package me.unp0wnable.groupeo.rest.dtos.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private UUID addressID;
    
    @Size(min = 1, max = 50)
    private String city;
    
    @Size(min = 1, max = 50)
    private String region;
    
    @Size(min = 1, max = 10)
    private String postalCode;
    
    @Size(min = 1, max = 50)
    private String country;
    
    private UUID userProfileID;
}
