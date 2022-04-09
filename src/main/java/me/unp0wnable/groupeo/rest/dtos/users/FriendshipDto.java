package me.unp0wnable.groupeo.rest.dtos.users;

import lombok.*;

import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FriendshipDto {
    private UUID requesterID;
    
    private UUID targetID;
    
    private UUID specifierID;
    
    private UUID groupID;
    
    private Date lastUpdate;
    
    private String status;
}
