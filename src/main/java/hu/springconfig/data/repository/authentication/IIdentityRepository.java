package hu.springconfig.data.repository.authentication;

import hu.springconfig.data.entity.authentication.Identity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IIdentityRepository extends CrudRepository<Identity, Long>, JpaSpecificationExecutor<Identity> {
    Identity findByUsername(String username);
}
