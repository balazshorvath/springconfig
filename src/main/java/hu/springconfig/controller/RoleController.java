package hu.springconfig.controller;

import hu.springconfig.data.dto.authentication.RoleCreate;
import hu.springconfig.data.dto.authentication.RoleUpdate;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Privilege;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.query.model.Condition;
import hu.springconfig.data.repository.authentication.IPrivilegeRepository;
import hu.springconfig.service.authentication.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@RestController
public class RoleController {
    @Autowired
    private RoleService roleService;
    @Autowired
    private IPrivilegeRepository privilegeRepository;

    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    @PostMapping("/role")
    public Role create(@RequestBody @Valid RoleCreate create) {
        Set<Privilege> privileges = new HashSet<>();
        privilegeRepository.findAll(create.getPrivileges()).forEach(privileges::add);
        return roleService.create(create.getId(), create.getRole(), privileges);
    }

    @PreAuthorize("hasAuthority('ROLE_GET')")
    @GetMapping("/role/{id}")
    public Role get(@PathVariable Integer id) {
        return roleService.get(id);
    }

    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @PutMapping("/role/{id}")
    public Role put(@PathVariable Integer id, @RequestBody RoleUpdate update, Authentication authentication) {
        Set<Privilege> privileges = new HashSet<>();
        privilegeRepository.findAll(update.getPrivileges()).forEach(privileges::add);
        return roleService.update((Identity) authentication.getPrincipal(), id, update.getId(), update.getRole(), privileges, update.getVersion());
    }

    @PreAuthorize("hasAuthority('ROLE_LIST')")
    @PostMapping("/role/list")
    public Page<Role> list(@RequestBody Condition condition, Pageable pageable) {
        return roleService.list(condition, pageable);
    }
}
