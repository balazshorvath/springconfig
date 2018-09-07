package hu.springconfig.service.authentication;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.exception.ForbiddenException;
import hu.springconfig.exception.NotFoundException;
import hu.springconfig.service.base.EntityService;
import hu.springconfig.service.mail.MailingService;
import hu.springconfig.util.Util;
import hu.springconfig.validator.entity.IdentityValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

import static hu.springconfig.config.message.entity.IdentityMessages.*;

@Service
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
        current.setTokenExpiration(System.currentTimeMillis());
        return save(current);
    }

//    public Identity changeUsernameSelf(Identity current, String password, String newUsername) {
//        checkPassword(current, password);
//        current.setTokenExpiration(System.currentTimeMillis());
//        return save(current);
//    }

    public Identity resetPassword(String email) {
        Identity identity = findByEmail(email);

        String newPassword = Util.randomString(Util.CHAR_AND_NUMBER_POOL, 8);
        identity.setPassword(encoder.encode(newPassword));
        identity.setTokenExpiration(System.currentTimeMillis());
        mailingService.sendPasswordReset(identity.getEmail(), newPassword);
        return save(identity);
    }

    public Identity updateIdentity(Identity current, Long id, String email, long version) {
        Identity identity = get(id);
        if (identity.isSuperiorTo(current)) {
            throw new ForbiddenException(IDENTITY_LOW_RANK);
        }
        if (Util.notNullAndNotEmpty(email)) {
            current.setTokenExpiration(System.currentTimeMillis());
        }
        identity.setEmail(email);
        identity.setVersion(version);
        return save(identity);
    }

    public Identity createIdentity(String email, String password, String passwordConfirm) {
        return createIdentity(email, password, passwordConfirm, null);
    }

    public Identity createIdentity(String email, String password, String passwordConfirm, String
            verification) {
        Identity identity = new Identity();
        identity.setEmail(email);
        identity.setRoles(Collections.singleton(roleService.get(RoleService.USER_ROLE_ID)));
        identity.setPassword(encoder.encode(password));
        identity.setVerificationCode(verification);

        return saveWithPasswords(identity, password, passwordConfirm);
    }

    public void delete(Identity current, Long id) {
        Identity identity = get(id);
        if (identity.isSuperiorTo(current)) {
            throw new ForbiddenException(IDENTITY_LOW_RANK);
        }
        identityRepository.delete(id);
    }

    public void verify(String verificationCode) {
        Identity identity = findByVerificationCode(verificationCode);
        identity.setVerificationCode(null);
        save(identity);
    }

    public void unlock(Long id) {
        Identity identity = get(id);
        identity.setLoginFails(0);
        save(identity);
    }

    public Identity findByEmail(String email) {
        Identity identity = identityRepository.findByEmail(email);
        if (identity == null) {
            throw new NotFoundException(IDENTITY_NOT_FOUND);
        }
        return identity;
    }

    public Identity findByVerificationCode(String verificationCode) {
        Identity identity = identityRepository.findByVerificationCode(verificationCode);
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

    public void loginFailed(Long id) {
        Identity identity = get(id);
        identity.incrementLoginFails();
        save(identity);
    }
}
