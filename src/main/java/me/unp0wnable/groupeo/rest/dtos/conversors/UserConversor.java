package me.unp0wnable.groupeo.rest.dtos.conversors;

import lombok.experimental.UtilityClass;
import me.unp0wnable.groupeo.model.constants.UserRoles;
import me.unp0wnable.groupeo.model.entities.*;
import me.unp0wnable.groupeo.rest.dtos.users.*;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class UserConversor {
    /* ******************** Convertir a DTO ******************** */
    public static UserDto toUserDto(User user) {
        UserDto dto = new UserDto(
                user.getUserID(),
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
    
    public static FriendshipDto toFriendshipDTO(Friendship friendship) {
        FriendshipDto dto = new FriendshipDto();
        dto.setRequesterID(friendship.getId().getRequesterID());
        dto.setTargetID(friendship.getId().getTargetID());
        dto.setSpecifierID(friendship.getSpecifier().getUserID());
        dto.setGroupID(friendship.getGroup().getGroupID());
        dto.setStatus(friendship.getStatus().name());
        dto.setLastUpdate(friendship.getLastUpdate());
        
        return dto;
    }
    
    public static GroupDto toGroupDTO(Group group) {
        GroupDto dto = new GroupDto();
        dto.setGroupID(group.getGroupID());
        dto.setCreatorID(group.getCreator().getUserID());
        dto.setName(group.getName());
        
        return dto;
    }
    
    /* ******************** Convertir a conjunto de DTO ******************** */
    public static List<UserDto> toUserDtoList(List<User> userList) {
        return userList.stream()
                .map(UserConversor::toUserDto)
                .collect(Collectors.toList());
    }
    
    /* ******************** Convertir a Entidad ******************** */
    public static User fromUserDTO(UserDto dto) {
        User user = new User();
        user.setUserID(dto.getUserID());
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
    
    public static Group fromGroupDTO(GroupDto dto) {
        Group group = new Group();
        group.setName(dto.getName());
        
        return group;
    }

}
