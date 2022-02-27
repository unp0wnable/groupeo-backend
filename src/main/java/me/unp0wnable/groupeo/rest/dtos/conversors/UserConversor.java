package me.unp0wnable.groupeo.rest.dtos.conversors;

import lombok.experimental.UtilityClass;
import me.unp0wnable.groupeo.model.entities.User;
import me.unp0wnable.groupeo.model.entities.User.UserRoles;
import me.unp0wnable.groupeo.model.entities.UserAddress;
import me.unp0wnable.groupeo.rest.dtos.users.*;

@UtilityClass
public class UserConversor {
    /* ******************** Convertir a DTO ******************** */
    public static UserDto toUserDto(User user) {
        UserDto dto = new UserDto(
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
        dto.setPassword(user.getPassword());
        // Agrega valores optativos si están disponibles
        if (user.getImageB64() != null) dto.setImageB64(user.getImageB64());
        
        
        return dto;
    }
    
    public static AddressDto toAddressDto(UserAddress address) {
        AddressDto dto = new AddressDto();
        dto.setCity(address.getCity());
        dto.setCountry(address.getCountry());
        dto.setRegion(address.getRegion());
        dto.setPostalCode(address.getPostalCode());
        dto.setAddressID(address.getUserAddressID());
        
        return dto;
    }
    
    public static AuthenticatedUserDto toAuthenticatedUserDTO(User user, String token) {
        return new AuthenticatedUserDto(token, toUserDto(user));
    }
    

    /* ******************** Convertir a Entidades ******************** */
    public static User fromUserDTO(UserDto dto) {
        User user = new User();
        user.setUserProfileID(dto.getUserID());
        user.setFirstName(dto.getFirstName());
        user.setSurname1(dto.getSurname1());
        user.setSurname2(dto.getSurname2());
        user.setEmail(dto.getEmail());
        user.setBirthDate(dto.getBirthDate());
        user.setJoinDate(dto.getJoinDate());
        user.setDescription(dto.getDescription());
        user.setNickName(dto.getNickName());
        user.setScore(dto.getScore());
        user.setRole(UserRoles.valueOf(dto.getRole()));
    
        // Agrega valores optativos si están disponibles
        if (dto.getImageB64() != null) user.setImageB64(user.getImageB64());
        return user;
    }
    
    public static User fromSignUpParamsDTO(SignUpParamsDto dto) {
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setSurname1(dto.getSurname1());
        user.setSurname2(dto.getSurname2());
        user.setNickName(dto.getNickName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getRawPassword());
        user.setEmail(dto.getEmail());
        user.setBirthDate(dto.getBirthDate());
        user.setImageB64(dto.getImageB64());
        
        return user;
    }
    
    public static User fromUpdateProfileParamsDTO(UpdateProfileParamsDto dto) {
        User user = new User();
        user.setNickName(dto.getFirstName());
        user.setSurname1(dto.getSurname1());
        user.setSurname2(dto.getSurname2());
        user.setEmail(dto.getEmail());
        user.setDescription(dto.getDescription());
     
        return user;
    }
    
    public static UserAddress fromAddressDTO(AddressDto dto) {
        UserAddress address = new UserAddress();
        address.setCity(dto.getCity());
        address.setCountry(dto.getCountry());
        address.setPostalCode(dto.getPostalCode());
        address.setRegion(dto.getRegion());
        
        return address;
    }
}
