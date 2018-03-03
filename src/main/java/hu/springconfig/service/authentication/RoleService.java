package hu.springconfig.service.authentication;

import hu.springconfig.config.message.RoleMessages;
import hu.springconfig.data.entity.authentication.Privilege;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.query.model.Condition;
import hu.springconfig.data.repository.authentication.IRoleRepository;
import hu.springconfig.exception.ForbiddenException;
import hu.springconfig.exception.NotFoundException;
import hu.springconfig.util.SpecificationsUtils;
import hu.springconfig.util.Util;
import hu.springconfig.validator.entity.RoleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {
    public static final Integer USER_ROLE_ID = 1;
    public static final Integer ADMIN_ROLE_ID = 1000;

    @Autowired
    private IRoleRepository roleRepository;
    @Autowired
    private RoleValidator validator;


    public Set<Role> getRoles(Set<Integer> roleIds) {
        return roleIds.stream().map(this::get).collect(Collectors.toSet());
    }

    public Role get(Integer roleId) {
        Role role = roleRepository.findOne(roleId);
        if (role == null) {
            throw new NotFoundException(RoleMessages.ROLE_NOT_FOUND);
        }
        return role;
    }

    public Role create(Integer id, String roleName, Set<Privilege> privileges) {
        Role role = new Role();
        role.setId(id);
        role.setRole(roleName);
        role.setPrivileges(privileges);
        return save(role);
    }

    private Role save(Role role) {
        validator.validate(role);
        return roleRepository.save(role);
    }

    public Role delete(Integer roleId) {
        Role role = get(roleId);
        if (USER_ROLE_ID.equals(roleId) || ADMIN_ROLE_ID.equals(roleId)) {
            throw new ForbiddenException(RoleMessages.ROLE_STATIC_DELETE);
        }
        roleRepository.delete(roleId);
        return role;
    }

    @Transactional
    public Role update(Integer id, Integer newId, String roleName, Set<Privilege> privileges, long version) {
        Role role = get(id);
        role.setVersion(version);
        if (newId != null) {
            if (USER_ROLE_ID.equals(id) || ADMIN_ROLE_ID.equals(id)) {
                throw new ForbiddenException(RoleMessages.ROLE_ID_STATIC);
            }
            role = delete(id);
            role.setId(newId);
        }
        if (Util.notNullAndNotEmpty(roleName)) {
            if (USER_ROLE_ID.equals(id) || ADMIN_ROLE_ID.equals(id)) {
                throw new ForbiddenException(RoleMessages.ROLE_ROLE_NAME_STATIC);
            }
            role.setRole(roleName);
        }
        if (Util.notNullAndNotEmpty(privileges)) {
            role.setPrivileges(privileges);
        }
        return save(role);
    }

    public Page<Role> list(Condition condition, Pageable pageable) {
        return roleRepository.findAll(SpecificationsUtils.withQuery(condition), pageable);
    }
}
