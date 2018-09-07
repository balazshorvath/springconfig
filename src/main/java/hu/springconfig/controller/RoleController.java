package hu.springconfig.controller;

import hu.springconfig.config.message.entity.RoleMessages;
import hu.springconfig.data.dto.authentication.role.RoleCreate;
import hu.springconfig.data.dto.authentication.role.RoleUpdate;
import hu.springconfig.data.dto.simple.OKResponse;
import hu.springconfig.data.entity.authentication.Privilege;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.query.model.Condition;
import hu.springconfig.data.repository.authentication.IPrivilegeRepository;
import hu.springconfig.exception.NotFoundException;
import hu.springconfig.service.authentication.RoleService;
import hu.springconfig.util.SpecificationsUtils;
import hu.springconfig.validator.request.ConditionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

@RestController
@Transactional
public class RoleController {
    @Autowired
    private RoleService roleService;
    @Autowired
    private IPrivilegeRepository privilegeRepository;
    @Autowired
    private ConditionValidator conditionValidator;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/role")
    public Role create(@RequestBody RoleCreate create) {
        Set<Privilege> privileges = new HashSet<>();
        privilegeRepository.findAll(create.getPrivileges()).forEach(privileges::add);
        return roleService.create(create.getId(), create.getRole(), privileges);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/role/{id}")
    public Role get(@PathVariable Integer id) {
        return roleService.get(id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/role/{id}")
    public Role put(@PathVariable Integer id, @RequestBody RoleUpdate update) {
        Set<Privilege> privileges = new HashSet<>();
        for (Integer privilegeId : update.getPrivileges()) {
            Privilege privilege = privilegeRepository.findOne(privilegeId);
            if (privilege == null) {
                throw new NotFoundException(RoleMessages.PRIVILEGE_NOT_FOUND);
            }
            privileges.add(privilege);
        }
        return roleService.update(id, update.getId(), update.getRole(), privileges, update.getVersion());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/role/{id}")
    public OKResponse delete(@PathVariable Integer id) {
        roleService.delete(id);
        return new OKResponse();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/role/list")
    public Page<Role> list(@RequestBody(required = false) Condition condition, Pageable pageable) {
        conditionValidator.validate(condition);
        return roleService.list(condition, pageable);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/privilege/list")
    public Page<Privilege> listPrivileges(@RequestBody(required = false) Condition condition, Pageable pageable) {
        conditionValidator.validate(condition);
        return privilegeRepository.findAll(SpecificationsUtils.withQuery(condition), pageable);
    }
}
