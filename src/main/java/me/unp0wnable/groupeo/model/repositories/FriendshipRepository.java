package me.unp0wnable.groupeo.model.repositories;

import me.unp0wnable.groupeo.model.entities.Friendship;
import me.unp0wnable.groupeo.model.entities.identities.FriendshipPK;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FriendshipRepository extends PagingAndSortingRepository<Friendship, FriendshipPK> {

}
