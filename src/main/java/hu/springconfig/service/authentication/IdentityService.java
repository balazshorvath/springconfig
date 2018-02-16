package hu.springconfig.service.authentication;

import hu.springconfig.config.message.MessageProvider;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.query.model.Condition;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.data.validator.IdentityValidator;
import hu.springconfig.exception.ForbiddenException;
import hu.springconfig.exception.NotFoundException;
import hu.springconfig.service.base.LoggingComponent;
import hu.springconfig.service.mail.MailingService;
import hu.springconfig.util.SpecificationsUtils;
import hu.springconfig.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Service
public class IdentityService extends LoggingComponent {
    @Autowired
    private IIdentityRepository identityRepository;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private MailingService mailingService;
    @Autowired
    private MessageProvider messageProvider;
    @Autowired
    private IdentityValidator validator;


    public Identity grantRoles(Identity current, Long id, Set<Role> roles) {
        Identity grantTo = get(id);
        // If the identity has higher ranks, than the current
        if (grantTo.isSuperiorTo(current)) {
            /* Current user is not allowed to update user, because the target has a higher rank. */
            throw new AccessDeniedException("identity.low_rank");
        }
        Role highest = current.getHighestRole();
        if (roles.stream().anyMatch(role -> role.getId() > highest.getId())) {
            /* Cannot grant roles higher, than own rank. */
            throw new AccessDeniedException("identity.low_rank");
        }
        grantTo.getRoles().addAll(roles);
        return identityRepository.save(grantTo);
    }

    public Identity denyRoles(Identity current, Long id, Set<Role> roles) {
        Identity denyFrom = get(id);
        // If the identity has higher ranks, than the current
        if (denyFrom.isSuperiorTo(current)) {
            /* Current user is not allowed to update user, because the target has a higher rank. */
            throw new AccessDeniedException("identity.low_rank");
        }
        Role highest = current.getHighestRole();
        if (roles.stream().anyMatch(role -> role.getId() > highest.getId())) {
            /* Cannot deny roles higher, than own rank. */
            throw new AccessDeniedException("identity.low_rank");
        }
        denyFrom.getRoles().removeAll(roles);
        return identityRepository.save(denyFrom);
    }

    public void changePassword(Identity current, String oldPassword, String newPassword, String newConfirm) {
        checkPassword(current, oldPassword);
        validator.validatePasswordConfirm(newPassword, newConfirm);
        current.setPassword(encoder.encode(newPassword));
        identityRepository.save(current);
    }

    public void changeEmailSelf(Identity current, String password, String newEmail) {
        checkPassword(current, password);
        validator.validateEmail(newEmail);
        current.setEmail(newEmail);
        identityRepository.save(current);
    }

    public void changeUsernameSelf(Identity current, String password, String newUsername) {
        checkPassword(current, password);
        validator.validateUsername(newUsername);
        current.setUsername(newUsername);
        identityRepository.save(current);
    }

    public void resetPassword(Identity current) {
        String newPassword = Util.randomString(Util.CHAR_AND_NUMBER_POOL, 8);
        String msg = messageProvider.getMessage("mail.password_reset.text", newPassword);
        String subject = messageProvider.getMessage("mail.password_reset.subject");
        current.setPassword(encoder.encode(newPassword));
        current.setTokenExpiration(new Date());
        mailingService.sendMail(current.getEmail(), subject, msg);
        identityRepository.save(current);
    }

    public Identity updateIdentity(Identity current, Long id, String username, String email) {
        Identity identity = get(id);
        if (identity.isSuperiorTo(current)) {
            throw new AccessDeniedException("identity.low_rank");
        }
        if (Util.notNullAndNotEmpty(username)) {
            validator.validateUsername(username);
            identity.setUsername(username);
        }
        if (Util.notNullAndNotEmpty(email)) {
            validator.validateEmail(email);
            identity.setEmail(email);
        }
        return identityRepository.save(identity);
    }

    public Identity get(Long id) {
        Identity identity = identityRepository.findOne(id);
        if (identity == null) {
            throw new NotFoundException("identity.not_found");
        }
        return identity;
    }

    public Identity createIdentity(String username, String email, String password, String passwordConfirm) {
        validator.validatePasswordConfirm(password, passwordConfirm);
        validator.validateEmail(email);
        validator.validateUsername(username);
        Identity identity = new Identity();
        identity.setUsername(username);
        identity.setEmail(email);
        identity.setRoles(Collections.singleton(roleService.get(RoleService.USER_ROLE_ID)));
        identity.setPassword(encoder.encode(password));
        return identityRepository.save(identity);
    }

    public void delete(Identity current, Long id) {
        Identity identity = get(id);
        if (identity.isSuperiorTo(current)) {
            throw new AccessDeniedException("identity.low_rank");
        }
        identityRepository.delete(id);
    }

    public Identity findByUsername(String username) {
        return identityRepository.findByUsername(username);
    }

    private void checkPassword(Identity identity, String password) {
        if (!Util.notNullAndNotEmpty(password) || !encoder.matches(password, identity.getPassword())) {
            throw new ForbiddenException("identity.check_password_failed");
        }
    }

    public Page<Identity> list(Condition condition, Pageable pageable) {
        return identityRepository.findAll(
                SpecificationsUtils.withQuery(condition),
                pageable
        );
    }
}
