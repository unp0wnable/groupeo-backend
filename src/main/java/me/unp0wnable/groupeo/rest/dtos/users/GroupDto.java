package me.unp0wnable.groupeo.rest.dtos.users;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GroupDto {
    private UUID groupID;
    
    private String name;
    
    private UUID creatorID;
}
