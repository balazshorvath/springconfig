package hu.springconfig.data.repository.authentication;

import hu.springconfig.data.entity.authentication.Privilege;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPrivilegeRepository extends CrudRepository<Privilege, Integer>, JpaSpecificationExecutor<Privilege> {
}
