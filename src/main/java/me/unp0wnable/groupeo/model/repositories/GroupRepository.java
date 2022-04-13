package me.unp0wnable.groupeo.model.repositories;

import me.unp0wnable.groupeo.model.entities.Group;
import me.unp0wnable.groupeo.model.entities.User;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface GroupRepository extends PagingAndSortingRepository<Group, UUID> {
    
    boolean existsByNameAndCreatorAllIgnoreCase(String name, User creator);
    
}
