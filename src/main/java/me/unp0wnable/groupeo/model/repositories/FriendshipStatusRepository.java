package me.unp0wnable.groupeo.model.repositories;

import me.unp0wnable.groupeo.model.entities.FriendshipStatus;
import me.unp0wnable.groupeo.model.entities.FriendshipStatusPK;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FriendshipStatusRepository extends PagingAndSortingRepository<FriendshipStatus, FriendshipStatusPK> {

}
