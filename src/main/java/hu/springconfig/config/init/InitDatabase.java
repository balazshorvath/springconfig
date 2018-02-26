package hu.springconfig.config.init;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.data.repository.authentication.IPrivilegeRepository;
import hu.springconfig.exception.NotFoundException;
import hu.springconfig.service.authentication.IdentityService;
import hu.springconfig.service.authentication.RoleService;
import hu.springconfig.service.base.LoggingComponent;
import hu.springconfig.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class InitDatabase extends LoggingComponent implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private IdentityService identityService;
    @Autowired
    private IIdentityRepository identityRepository;
    @Autowired
    private RoleService roleService;
    @Autowired
    private BCryptPasswordEncoder encoder;

    /**
     * Creates an administrator user with ADMIN role.
     * The administrator will have a generated password, that can be found in the error log.
     * It should be changed ASAP.
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try{
            identityService.findByUsername("administrator");
        }catch (NotFoundException e){
            String password = Util.randomString(Util.CHAR_AND_NUMBER_POOL, 8);
            Identity adminIdentity = new Identity();
            adminIdentity.setUsername("administrator");
            adminIdentity.setPassword(encoder.encode(password));
            adminIdentity.setRoles(Collections.singleton(roleService.get(RoleService.ADMIN_ROLE_ID)));
            identityRepository.save(adminIdentity);
            log.error("Recognized, that this is a newly deployed version. Created a new user with admin role, you can now login with the password: {}. Please change is ASAP!", password);
        }
    }

}
