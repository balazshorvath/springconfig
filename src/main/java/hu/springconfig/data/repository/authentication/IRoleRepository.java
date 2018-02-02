package hu.springconfig.data.repository.authentication;

import hu.springconfig.data.entity.authentication.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRoleRepository extends CrudRepository<Role, Integer>{
}
