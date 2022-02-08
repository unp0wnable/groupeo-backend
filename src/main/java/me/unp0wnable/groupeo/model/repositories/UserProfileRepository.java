package me.unp0wnable.groupeo.model.repositories;

import me.unp0wnable.groupeo.model.entities.UserProfile;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface UserProfileRepository extends PagingAndSortingRepository<UserProfile, UUID> {

    boolean existsByNickName(String nickName);

}
