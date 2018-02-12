package hu.springconfig.config.init;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.repository.authentication.IPrivilegeRepository;
import hu.springconfig.service.authentication.IdentityService;
import hu.springconfig.service.authentication.RoleService;
import hu.springconfig.service.base.LoggingComponent;
import hu.springconfig.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Profile("initial")
@Component
public class InitDatabase extends LoggingComponent implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private IdentityService identityService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private IPrivilegeRepository privilegeRepository;

    /**
     * Create roles and privileges.
     * By default we want to give the administrator all thr privileges.
     * After that, the administrator should change the password, and setup the roles
     * to have the appropriate privileges.
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Identity adminIdentity = identityService.findByUsername("administrator");
        // In this case I assume, that the database is in an uninitialized state and needs an admin.
        if (adminIdentity == null) {
            String password = Util.randomString(Util.CHAR_AND_NUMBER_POOL, 8);
            adminIdentity = new Identity();
            adminIdentity.setUsername("administrator");
            adminIdentity.setRoles(Collections.singleton(roleService.get(RoleService.ADMIN_ROLE_ID)));
            identityService.createIdentity(adminIdentity, password, password);
            log.warn("Recognized, that this is a newly deployed version. Created a new user with admin role, you can now login with the password: {}. Please change is ASAP!", password);
        }
    }

}
