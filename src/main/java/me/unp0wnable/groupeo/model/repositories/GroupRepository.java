package me.unp0wnable.groupeo.model.repositories;

import me.unp0wnable.groupeo.model.entities.Group;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface GroupRepository extends PagingAndSortingRepository<Group, UUID> {

}
