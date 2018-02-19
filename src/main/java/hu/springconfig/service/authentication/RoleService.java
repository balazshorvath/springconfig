package hu.springconfig.service.authentication;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Privilege;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.query.model.Condition;
import hu.springconfig.data.repository.authentication.IRoleRepository;
import hu.springconfig.exception.NotFoundException;
import hu.springconfig.util.SpecificationsUtils;
import hu.springconfig.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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

    public Set<Role> getRoles(Set<Integer> roleIds) {
        return roleIds.stream().map(this::get).collect(Collectors.toSet());
    }

    public Role get(Integer roleId) {
        Role role = roleRepository.findOne(roleId);
        if (role == null) {
            throw new NotFoundException("role.not_found");
        }
        return role;
    }

    public Role create(Integer id, String roleName, Set<Privilege> privileges) {
        Role role = new Role();
        role.setId(id);
        role.setRole(roleName);
        role.setPrivileges(privileges);
        return roleRepository.save(role);
    }

    public Role delete(Identity current, Integer roleId) {
        Role role = get(roleId);
        if (USER_ROLE_ID.equals(roleId) || ADMIN_ROLE_ID.equals(roleId)) {
            throw new AccessDeniedException("role.static_delete");
        }
        if (current.getHighestRole().getId() < roleId) {
            throw new AccessDeniedException("identity.low_rank");
        }
        return role;
    }

    @Transactional
    public Role update(Identity current, Integer id, Integer newId, String roleName, Set<Privilege> privileges, long version) {
        Integer highest = current.getHighestRole().getId();
        if (highest <= id && newId <= highest) {
            throw new AccessDeniedException("identity.low_rank");
        }
        Role role = get(id);
        role.setVersion(version);
        if (newId != null) {
            role = delete(current, id);
            role.setId(id);
        }
        if (Util.notNullAndNotEmpty(roleName)) {
            role.setRole(roleName);
        }
        if (Util.notNullAndNotEmpty(privileges)) {
            role.setPrivileges(privileges);
        }
        return roleRepository.save(role);
    }

    public Page<Role> list(Condition condition, Pageable pageable) {
        return roleRepository.findAll(SpecificationsUtils.withQuery(condition), pageable);
    }
}
