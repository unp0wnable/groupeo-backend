package me.unp0wnable.groupeo.model.repositories;

import me.unp0wnable.groupeo.model.entities.FriendshipStatus;
import me.unp0wnable.groupeo.model.entities.User;
import me.unp0wnable.groupeo.model.entities.identities.FriendshipStatusPK;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;
import java.util.UUID;

public interface FriendshipStatusRepository extends PagingAndSortingRepository<FriendshipStatus, FriendshipStatusPK> {
    
    /** Obtiene la última actualización en la amistad entre dos usuarios */
    Optional<FriendshipStatus> getMostRecentFriendshipStatusBetweenUsers(UUID requester, UUID target);

    

}
