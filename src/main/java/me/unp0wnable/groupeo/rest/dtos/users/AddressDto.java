package me.unp0wnable.groupeo.rest.dtos.users;

import lombok.*;
import me.unp0wnable.groupeo.model.entities.UserProfile;

import javax.validation.constraints.Size;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private UUID addressID;
    
    @Size(min = 1, max = 50)
    private String city;
    
    @Size(min = 1, max = 50)
    private String region;
    
    @Size(min = 1, max = 10)
    private String postalCode;
    
    @Size(min = 1, max = 50)
    private String country;
    
    private UserProfile userProfile;
}
