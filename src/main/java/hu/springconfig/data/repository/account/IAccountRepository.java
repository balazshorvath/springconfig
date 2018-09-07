package hu.springconfig.data.repository.account;

import hu.springconfig.data.entity.Account;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAccountRepository extends CrudRepository<Account, Long>, JpaSpecificationExecutor<Account> {
}
