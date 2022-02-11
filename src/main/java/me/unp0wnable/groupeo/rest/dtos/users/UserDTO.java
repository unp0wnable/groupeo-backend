package me.unp0wnable.groupeo.rest.dtos.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDTO {
    private UUID userID;
    
    @Size(min = 1, max = 50)
    private String firstName;
    
    @Size(min = 1, max = 50)
    private String surname1;
    
    @Size(min = 1, max = 50)
    private String surname2;
    
    @Size(min = 1, max = 100)
    private String email;
    
    private Date birthDate;
    
    private Date joinDate;
    
    private String description;
    
    @Size(min = 1, max = 50)
    private String nickName;
    
    private String password;
    
    private String imageB64;
    
    private Float score;
    
    /** Constuctor sin <c>password</c> */
    public UserDTO(UUID userID, String firstName, String surname1, String surname2, String email, Date birthDate,
            Date joinDate, String description, String nickName, String imageB64, Float score) {
        this.userID = userID;
        this.firstName = firstName;
        this.surname1 = surname1;
        this.surname2 = surname2;
        this.email = email;
        this.birthDate = birthDate;
        this.joinDate = joinDate;
        this.description = description;
        this.nickName = nickName;
        this.imageB64 = imageB64;
        this.score = score;
    }
    
    /** Constuctor sin <c>password, imageB64</c> */
    public UserDTO(UUID userID, String firstName, String surname1, String surname2, String email, Date birthDate,
            Date joinDate, String description, String nickName, Float score) {
        this.userID = userID;
        this.firstName = firstName;
        this.surname1 = surname1;
        this.surname2 = surname2;
        this.email = email;
        this.birthDate = birthDate;
        this.joinDate = joinDate;
        this.description = description;
        this.nickName = nickName;
        this.score = score;
    }
}
