package animals.services;

import animals.domain.Group;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends CrudRepository<Group, Long>
{
    List<Group> findAll();
    Group findByName(String name);
    Group getOne(long id);

    @Override
    void delete(Group group);
}
