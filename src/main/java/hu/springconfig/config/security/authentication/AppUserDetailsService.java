package hu.springconfig.config.security.authentication;

import hu.springconfig.data.entity.Authentication;
import hu.springconfig.data.repository.IAuthenticationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    @Autowired
    private IAuthenticationRepository authenticationRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Authentication authentication = authenticationRepository.findByUsername(username);
        if(authentication == null){
            throw new UsernameNotFoundException("User not found.");
        }
        return new User(authentication.getUsername(), authentication.getPassword(), authentication.createGrantedAuthorities());
    }
}
