package hu.springconfig.service.authentication;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.exception.BadRequestException;
import hu.springconfig.exception.ForbiddenException;
import hu.springconfig.exception.NotFoundException;
import hu.springconfig.service.base.LoggingComponent;
import hu.springconfig.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IdentityService extends LoggingComponent {
    @Autowired
    private IIdentityRepository identityRepository;
    @Autowired
    private PasswordEncoder encoder;


    public Identity grantRoles(Identity current, Long id, Set<Role.Roles> roles) {
        Identity grantTo = get(id);
        // If the identity has higher ranks, than the current
        if (grantTo.isSuperiorTo(current)) {
            /* Current user is not allowed to update user, because the target has a higher rank. */
            throw new AccessDeniedException("identity.low_rank");
        }
        Role.Roles highest = current.getHighestRole().getRole();
        if (roles.stream().anyMatch(role -> role.getValue() > highest.getValue())) {
            /* Cannot grant roles higher, than own rank. */
            throw new AccessDeniedException("identity.low_rank");
        }
        grantTo.setRoles(roles.stream().map(Role::new).collect(Collectors.toSet()));
        return identityRepository.save(grantTo);
    }

    public Identity changePassword(Identity current, String oldPassword, String newPassword, String newConfirm) {
        current = get(current.getId());
        checkPassword(current, oldPassword);
        checkPasswordValidity(newPassword, newConfirm);
        current.setPassword(encoder.encode(newPassword));
        return identityRepository.save(current);
    }

    public Identity changeEmailSelf(Identity current, String password, String newEmail) {
        current = get(current.getId());
        checkPassword(current, password);
        // You just cannot validate emails.
        if (!Util.notNullAndNotEmpty(newEmail)) {
            throw new BadRequestException("validation.email.empty");
        }
        current.setEmail(newEmail);
        return identityRepository.save(current);
    }

    public Identity changeUsernameSelf(Identity current, String password, String newUsername) {
        current = get(current.getId());
        checkPassword(current, password);
        // You just cannot validate emails.
        if (!Util.notNullAndNotEmpty(newUsername)) {
            throw new BadRequestException("validation.username.empty");
        }
        current.setUsername(newUsername);
        return identityRepository.save(current);
    }

    public Identity resetPassword(Identity current) {
        throw new UnsupportedOperationException("resetPassword is not yet implemented");
    }

    public Identity updateIdentity(Identity current, Long id, String username, String email) {
        Identity identity = get(id);
        if (identity.isSuperiorTo(current)) {
            throw new AccessDeniedException("identity.low_rank");
        }
        if (Util.notNullAndNotEmpty(username)) {
            identity.setUsername(username);
        }
        if (Util.notNullAndNotEmpty(email)) {
            identity.setUsername(email);
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

    public Identity createIdentity(Identity identity, String password, String passwordConfirm) {
        checkPasswordValidity(password, passwordConfirm);
        identity.setPassword(encoder.encode(password));
        return identityRepository.save(identity);
    }

    public Identity findByUsername(String username) {
        return identityRepository.findByUsername(username);
    }

    public void checkPassword(Identity identity, String password) {
        if (!Util.notNullAndNotEmpty(password) || !encoder.matches(password, identity.getPassword())) {
            throw new ForbiddenException("identity.check_password_failed");
        }
    }

    public void checkPasswordValidity(String password, String passwordConfirm) {
        if (!Util.notNullAndNotEmpty(password) || !Util.notNullAndNotEmpty(passwordConfirm)
                || !password.equals(passwordConfirm)) {
            throw new BadRequestException("validation.password.confirm.mismatch");
        }
    }
}
