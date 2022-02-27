package me.unp0wnable.groupeo.model.entities;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class FriendshipPK implements Serializable {
    private static final long serialVersionUID = 2750663615925248479L;
    /** ID del usuario que realiza la acción */
    private UUID requesterID;

    /** ID del usuario que recibe la acción */
    private UUID targetID;

}
