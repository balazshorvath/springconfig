package hu.springconfig.service.authentication;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.repository.authentication.IRoleRepository;
import hu.springconfig.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

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

    public Role create() {
        throw new UnsupportedOperationException();
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
}
