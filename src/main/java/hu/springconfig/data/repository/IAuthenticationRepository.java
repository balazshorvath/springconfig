package hu.springconfig.data.repository;

import hu.springconfig.data.entity.Authentication;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAuthenticationRepository extends CrudRepository<Authentication, Long> {
    Authentication findByUsername(String username);
}
