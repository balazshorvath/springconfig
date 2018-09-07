package hu.springconfig.service.account;

import hu.springconfig.data.entity.Account;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.repository.account.IAccountRepository;
import hu.springconfig.exception.ForbiddenException;
import hu.springconfig.service.base.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import static hu.springconfig.config.message.entity.IdentityMessages.IDENTITY_LOW_RANK;

@Service
public class AccountService extends EntityService<Account, Long> {
    @Autowired
    private IAccountRepository accountRepository;

    public Account create(Identity identity, String firstName, String lastName, Long dailyCalorieGoal) {
        Account account = new Account();
        account.setIdentity(identity);
        account.setFirstName(firstName);
        account.setLastName(lastName);
        account.setDailyCalorieGoal(dailyCalorieGoal);
        return save(account);
    }

    @Override
    protected CrudRepository<Account, Long> getRepository() {
        return accountRepository;
    }

    @Override
    protected String getEntityName() {
        return null;
    }

    public Account update(Long id, String firstName, String lastName, Long dailyCalorieGoal, long version,
                          Identity current) {
        Account account = get(id);
        if (account.getIdentity().isSuperiorTo(current)) {
            throw new ForbiddenException(IDENTITY_LOW_RANK);
        }
        account.setLastName(lastName);
        account.setFirstName(firstName);
        account.setDailyCalorieGoal(dailyCalorieGoal);
        account.setVersion(version);
        return save(account);
    }
}
