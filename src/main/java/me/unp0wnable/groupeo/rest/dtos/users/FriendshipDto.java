package me.unp0wnable.groupeo.rest.dtos.users;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
    
    @JsonInclude(Include.NON_NULL)
    private UUID groupID;
    
    private Date lastUpdate;
    
    private String status;
}
