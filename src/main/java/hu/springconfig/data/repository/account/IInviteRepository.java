package hu.springconfig.data.repository.account;

import hu.springconfig.data.entity.Invite;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IInviteRepository extends CrudRepository<Invite, Long>, JpaSpecificationExecutor<Invite> {
    boolean existsByInviteKey(String inviteKey);

    Invite findByInviteKey(String inviteKey);
}
