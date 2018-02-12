package hu.springconfig.data.repository.authentication;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface IRoleRepository extends CrudRepository<Role, Integer> {
    Set<Role> findAllByIdentities(Identity identity);

    Role findByRole(String role);
}
