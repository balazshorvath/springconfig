package hu.springconfig.service.authentication;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.query.model.Condition;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.exception.ForbiddenException;
import hu.springconfig.exception.NotFoundException;
import hu.springconfig.service.base.EntityService;
import hu.springconfig.service.mail.MailingService;
import hu.springconfig.util.SpecificationsUtils;
import hu.springconfig.util.Util;
import hu.springconfig.validator.entity.IdentityValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Set;

import static hu.springconfig.config.message.IdentityMessages.*;

@Service
@Transactional
public class IdentityService extends EntityService<Identity, Long> {

    @Autowired
    private IIdentityRepository identityRepository;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private MailingService mailingService;
    @Autowired
    private IdentityValidator validator;


    public Identity grantRoles(Identity current, Long id, Set<Role> roles) {
        Identity grantTo = get(id);
        // If the identity has higher ranks, than the current
        if (grantTo.isSuperiorTo(current)) {
            /* Current user is not allowed to update user, because the target has a higher rank. */
            throw new ForbiddenException(IDENTITY_LOW_RANK);
        }
        Role highest = current.getHighestRole();
        if (roles.stream().anyMatch(role -> role.getId() > highest.getId())) {
            /* Cannot grant roles higher, than own rank. */
            throw new ForbiddenException(IDENTITY_LOW_RANK);
        }
        grantTo.getRoles().addAll(roles);
        return save(grantTo);
    }

    public Identity denyRoles(Identity current, Long id, Set<Role> roles) {
        Identity denyFrom = get(id);
        // If the identity has higher ranks, than the current
        if (denyFrom.isSuperiorTo(current)) {
            /* Current user is not allowed to update user, because the target has a higher rank. */
            throw new ForbiddenException(IDENTITY_LOW_RANK);
        }
        Role highest = current.getHighestRole();
        if (roles.stream().anyMatch(role -> role.getId() > highest.getId())) {
            /* Cannot deny roles higher, than own rank. */
            throw new ForbiddenException(IDENTITY_LOW_RANK);
        }
        denyFrom.getRoles().removeAll(roles);
        return save(denyFrom);
    }

    public Identity changePassword(Identity current, String oldPassword, String newPassword, String newConfirm) {
        checkPassword(current, oldPassword);
        current.setPassword(encoder.encode(newPassword));
        current.setTokenExpiration(System.currentTimeMillis());
        return saveWithPasswords(current, newPassword, newConfirm);
    }

    public Identity changeEmailSelf(Identity current, String password, String newEmail) {
        checkPassword(current, password);
        current.setEmail(newEmail);
        return save(current);
    }

    public Identity changeUsernameSelf(Identity current, String password, String newUsername) {
        checkPassword(current, password);
        current.setUsername(newUsername);
        current.setTokenExpiration(System.currentTimeMillis());
        return save(current);
    }

    public Identity resetPassword(String email, String username) {
        Identity identity = findByUsername(username);
        if (!Util.notNullAndNotEmpty(email) || !email.equals(identity.getEmail())) {
            throw new ForbiddenException(IDENTITY_RESET_PASSWORD_FAILED);
        }

        String newPassword = Util.randomString(Util.CHAR_AND_NUMBER_POOL, 8);
        identity.setPassword(encoder.encode(newPassword));
        identity.setTokenExpiration(System.currentTimeMillis());
        mailingService.sendPasswordReset(identity.getEmail(), newPassword);
        return save(identity);
    }

    public Identity updateIdentity(Identity current, Long id, String username, String email, long version) {
        Identity identity = get(id);
        if (identity.isSuperiorTo(current)) {
            throw new ForbiddenException(IDENTITY_LOW_RANK);
        }
        if (Util.notNullAndNotEmpty(username)) {
            current.setTokenExpiration(System.currentTimeMillis());
        }
        identity.setUsername(username);
        identity.setEmail(email);
        identity.setVersion(version);
        return save(identity);
    }

    public Identity createIdentity(String username, String email, String password, String passwordConfirm) {
        Identity identity = new Identity();
        identity.setUsername(username);
        identity.setEmail(email);
        identity.setRoles(Collections.singleton(roleService.get(RoleService.USER_ROLE_ID)));
        identity.setPassword(encoder.encode(password));

        return saveWithPasswords(identity, password, passwordConfirm);
    }

    public void delete(Identity current, Long id) {
        Identity identity = get(id);
        if (identity.isSuperiorTo(current)) {
            throw new ForbiddenException(IDENTITY_LOW_RANK);
        }
        identityRepository.delete(id);
    }

    public Identity findByUsername(String username) {
        Identity identity = identityRepository.findByUsername(username);
        if (identity == null) {
            throw new NotFoundException(IDENTITY_NOT_FOUND);
        }
        return identity;
    }

    private void checkPassword(Identity identity, String password) {
        if (!Util.notNullAndNotEmpty(password) || !encoder.matches(password, identity.getPassword())) {
            throw new ForbiddenException(IDENTITY_CHECK_PASSWORD_FAILED);
        }
    }

    public Page<Identity> list(Condition condition, Pageable pageable) {
        return identityRepository.findAll(
                SpecificationsUtils.withQuery(condition),
                pageable
        );
    }

    private Identity saveWithPasswords(Identity identity, String password, String passwordConfirm) {
        validator.validateWithPasswords(identity, password, passwordConfirm);
        return super.save(identity);
    }

    @Override
    protected CrudRepository<Identity, Long> getRepository() {
        return identityRepository;
    }

    @Override
    protected String getEntityName() {
        return ENTITY_NAME;
    }
}
