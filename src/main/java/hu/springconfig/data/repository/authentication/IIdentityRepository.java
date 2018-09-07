package hu.springconfig.data.repository.authentication;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IIdentityRepository extends CrudRepository<Identity, Long>, JpaSpecificationExecutor<Identity> {
    Identity findByEmail(String email);

    Identity findByVerificationCode(String verificationCode);

    List<Identity> findByRoles(Role role);
}
