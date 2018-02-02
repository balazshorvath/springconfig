package hu.springconfig.service.authentication;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.service.base.LoggingComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class IdentityService extends LoggingComponent {
    @Autowired
    private IIdentityRepository identityRepository;
    @Autowired
    private BCryptPasswordEncoder encoder;

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
