package me.unp0wnable.groupeo.rest.dtos.conversors;

import lombok.NoArgsConstructor;
import me.unp0wnable.groupeo.model.entities.UserAddress;
import me.unp0wnable.groupeo.model.entities.UserProfile;
import me.unp0wnable.groupeo.model.entities.UserProfile.UserRoles;
import me.unp0wnable.groupeo.rest.dtos.users.*;

import java.sql.Date;

@NoArgsConstructor
public class UserConversor {
    /* ******************** Convertir a DTO ******************** */
    public static UserDTO toUserDto(UserProfile user) {
        UserDTO dto = new UserDTO(
                user.getUserProfileID(),
                user.getFirstName(),
                user.getSurname1(),
                user.getSurname2(),
                user.getEmail(),
                user.getBirthDate(),
                user.getJoinDate(),
                user.getDescription(),
                user.getNickName(),
                user.getScore(),
                user.getRole().toString()
        );
        
        // Agrega valores optativos si están disponibles
        if (user.getImageB64() != null) dto.setImageB64(user.getImageB64());
        
        
        return dto;
    }
    
    public static AddressDTO toAddressDto(UserAddress address) {
        AddressDTO dto = new AddressDTO();
        dto.setCity(address.getCity());
        dto.setCountry(address.getCountry());
        dto.setRegion(address.getRegion());
        dto.setPostalCode(address.getPostalCode());
        dto.setAddressID(address.getUserAddressID());
        
        return dto;
    }
    
    public static AuthenticatedUserDto toAuthenticatedUserDTO(UserProfile user, String token) {
        return new AuthenticatedUserDto(token, toUserDto(user));
    }
    

    /* ******************** Convertir a Entidades ******************** */
    public static UserProfile fromUserDTO(UserDTO dto) {
        UserProfile user = new UserProfile();
        user.setUserProfileID(dto.getUserID());
        user.setFirstName(dto.getFirstName());
        user.setSurname1(dto.getSurname1());
        user.setSurname2(dto.getSurname2());
        user.setEmail(dto.getEmail());
        user.setBirthDate(( Date ) dto.getBirthDate());
        user.setJoinDate(( Date ) dto.getJoinDate());
        user.setDescription(dto.getDescription());
        user.setNickName(dto.getNickName());
        user.setScore(dto.getScore());
        user.setRole(UserRoles.valueOf(dto.getRole()));
    
        // Agrega valores optativos si están disponibles
        if (dto.getImageB64() != null) user.setImageB64(user.getImageB64());
        return user;
    }
    
    public static UserProfile fromSignUpParamsDTO(SignUpParamsDto dto) {
        UserProfile user = new UserProfile();
        user.setFirstName(dto.getFirstName());
        user.setSurname1(dto.getSurname1());
        user.setSurname2(dto.getSurname2());
        user.setNickName(dto.getNickName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getRawPassword());
        user.setEmail(dto.getEmail());
        user.setBirthDate(( Date ) dto.getBirthDate());
        user.setImageB64(dto.getImageB64());
        
        return user;
    }
    
    public static UserProfile fromUpdateProfileParamsDTO(UpdateProfileParamsDto dto) {
        UserProfile user = new UserProfile();
        user.setNickName(dto.getFirstName());
        user.setSurname1(dto.getSurname1());
        user.setSurname2(dto.getSurname2());
     
        return user;
    }
    
    public static UserAddress fromAddressDTO(AddressDTO dto) {
        UserAddress address = new UserAddress();
        address.setCity(dto.getCity());
        address.setCountry(dto.getCountry());
        address.setPostalCode(dto.getPostalCode());
        address.setRegion(dto.getRegion());
        
        return address;
    }
}
