package hu.springconfig.service.authentication;

import hu.springconfig.config.message.entity.RoleMessages;
import hu.springconfig.data.entity.authentication.Privilege;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.repository.authentication.IRoleRepository;
import hu.springconfig.exception.ForbiddenException;
import hu.springconfig.service.base.EntityService;
import hu.springconfig.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService extends EntityService<Role, Integer> {
    public static final Integer USER_ROLE_ID = 1;
    public static final Integer ADMIN_ROLE_ID = 1000;

    @Autowired
    private IRoleRepository roleRepository;


    public Set<Role> getRoles(Set<Integer> roleIds) {
        return roleIds.stream().map(this::get).collect(Collectors.toSet());
    }

    public Role create(Integer id, String roleName, Set<Privilege> privileges) {
        Role role = new Role();
        role.setId(id);
        role.setRole(roleName);
        role.setPrivileges(privileges);
        return save(role);
    }

    public Role deleteRole(Integer roleId) {
        Role role = get(roleId);
        if (USER_ROLE_ID.equals(roleId) || ADMIN_ROLE_ID.equals(roleId)) {
            throw new ForbiddenException(RoleMessages.ROLE_STATIC_DELETE);
        }
        roleRepository.delete(roleId);
        return role;
    }

    public Role update(Integer id, Integer newId, String roleName, Set<Privilege> privileges, long version) {
        Role role = get(id);
        if (newId != null && !id.equals(newId)) {
            if (USER_ROLE_ID.equals(id) || ADMIN_ROLE_ID.equals(id)) {
                throw new ForbiddenException(RoleMessages.ROLE_ID_STATIC);
            }
            deleteRole(id);
            role = new Role(role);
            role.setId(newId);
            role.setVersion(0);
        } else {
            role.setVersion(version);
        }
        if (Util.notNullAndNotEmpty(roleName) && !role.getRole().equals(roleName)) {
            if (USER_ROLE_ID.equals(id) || ADMIN_ROLE_ID.equals(id)) {
                throw new ForbiddenException(RoleMessages.ROLE_ROLE_NAME_STATIC);
            }
            role.setRole(roleName);
        }
        role.setPrivileges(privileges);
        return save(role);
    }

    @Override
    protected CrudRepository<Role, Integer> getRepository() {
        return roleRepository;
    }

    @Override
    protected String getEntityName() {
        return RoleMessages.ENTITY_NAME;
    }
}
