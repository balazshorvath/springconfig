package hu.springconfig.service.account;


import hu.springconfig.data.entity.Invite;
import hu.springconfig.data.repository.account.IInviteRepository;
import hu.springconfig.service.base.EntityService;
import hu.springconfig.service.mail.MailingService;
import hu.springconfig.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class InviteService extends EntityService<Invite, Long> {
    @Autowired
    private IInviteRepository inviteRepository;
    @Autowired
    private MailingService mailingService;

    public Invite create(String email) {
        String key;
        do {
            key = Util.randomString(Util.CHAR_AND_NUMBER_POOL, 32);
        } while (inviteRepository.existsByInviteKey(key));

        Invite invite = new Invite();
        invite.setEmail(email);
        invite.setInviteKey(key);
        invite.setUsed(false);
        invite.setDate(new Date());
        invite = save(invite);
        mailingService.sendInvite(email, key);
        return invite;
    }

    @Override
    public Invite save(Invite entity) {
        return super.save(entity);
    }

    @Override
    protected CrudRepository<Invite, Long> getRepository() {
        return inviteRepository;
    }

    @Override
    protected String getEntityName() {
        return "invite";
    }

    public Invite getByKey(String inviteKey) {
        return inviteRepository.findByInviteKey(inviteKey);
    }
}
