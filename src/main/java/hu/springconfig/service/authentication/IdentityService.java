package hu.springconfig.service.authentication;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.service.base.LoggingComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IdentityService extends LoggingComponent {
    @Autowired
    private IIdentityRepository identityRepository;
    @Autowired
    private BCryptPasswordEncoder encoder;


    public Identity grantRoles(Identity current, Long id, Set<Role.Roles> roles) {
        Identity grantTo = identityRepository.findOne(id);
        // If the identity has higher ranks, than the current
        if(grantTo.isSuperiorTo(current)){
            throw new AccessDeniedException("Current user is not allowed to update user "
                    + id + ", because the target has a higher rank.");
        }
        Role.Roles highest = current.getHighestRole().getRole();
        if(roles.stream().anyMatch(role -> role.getValue() > highest.getValue())){
            throw new AccessDeniedException("Cannot grant roles higher, than own rank.");
        }
        grantTo.setRoles(roles.stream().map(Role::new).collect(Collectors.toSet()));
        return identityRepository.save(grantTo);
    }

    public Identity updatePassword(Identity current, String oldPassword, String newPassword, String newConfirm){
        Identity updateUser = identityRepository.findOne(id);
        if(!current.getId().equals(id)){
            throw new AccessDeniedException(".");
        }
    }
    /**
     * Encodes the identity's password and saves the object.
     *
     * @param identity identity to save
     * @return new identity
     */
    public Identity createIdentity(Identity identity){
        identity.setPassword(encoder.encode(identity.getPassword()));
        return identityRepository.save(identity);
    }

    public Identity findByUsername(String username){
        return identityRepository.findByUsername(username);
    }
}
