package hu.springconfig.config.security.authentication;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    @Autowired
    private IIdentityRepository identityRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Identity identity = identityRepository.findByUsername(username);
        if(identity == null){
            throw new UsernameNotFoundException("Identity not found.");
        }
        return new User(identity.getUsername(), identity.getPassword(), identity.createGrantedAuthorities());
    }
}
