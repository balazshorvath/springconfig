package hu.springconfig.config.init;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Privilege;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.repository.authentication.IPrivilegeRepository;
import hu.springconfig.data.repository.authentication.IRoleRepository;
import hu.springconfig.service.authentication.IdentityService;
import hu.springconfig.service.base.LoggingComponent;
import hu.springconfig.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InitDatabase extends LoggingComponent implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private IdentityService identityService;
    @Autowired
    private IRoleRepository roleRepository;
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
        initializePrivileges();
        initializeRoles();
        Identity adminIdentity = identityService.findByUsername("administrator");
        // In this case I assume, that the database is in an uninitialized state and needs an admin.
        if(adminIdentity == null){
            String password = Util.randomString(Util.CHAR_AND_NUMBER_POOL, 8);
            adminIdentity = new Identity();
            adminIdentity.setPassword(password);
            adminIdentity.setUsername("administrator");
            adminIdentity.setRoles(Collections.singleton(new Role(Role.Roles.ADMIN)));
            identityService.createIdentity(adminIdentity);
            log.warn("Recognized, that this is a newly deployed version. Created a new user with admin role, you can now login with the password: {}. Please change is ASAP!", password);
        }
    }

    /**
     * Create Privileges, if they are not in the database.
     */
    private void initializePrivileges(){
        List<Privilege> privileges = Arrays.stream(Privilege.Privileges.values()).map(Privilege::new).collect(Collectors.toList());
        for (Privilege privilege : privileges){
            if(!privilegeRepository.exists(privilege.getId())){
                privilegeRepository.save(privilege);
            }
        }
    }

    /**
     * Create Roles, if they are not in the database.
     */
    private void initializeRoles(){
        List<Role> roles = Arrays.stream(Role.Roles.values()).map(Role::new).collect(Collectors.toList());
        for (Role role : roles){
            if(!roleRepository.exists(role.getId())){
                roleRepository.save(role);
            }
        }
    }
}
