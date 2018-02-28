package hu.springconfig.controller;

import hu.springconfig.data.dto.authentication.SecuredUpdate;
import hu.springconfig.data.dto.authentication.identity.*;
import hu.springconfig.data.dto.simple.OKResponse;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.query.model.Condition;
import hu.springconfig.service.authentication.IdentityService;
import hu.springconfig.service.authentication.RoleService;
import hu.springconfig.validator.request.ConditionValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
public class IdentityController {
    @Autowired
    private IdentityService identityService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ConditionValidator conditionValidator;

    /* Standard user features */

    @PostMapping("/auth/register")
    public OKResponse register(@RequestBody IdentityCreate identity) {
        identityService.createIdentity(identity.getUsername(), identity.getEmail(), identity.getPassword(), identity.getPasswordConfirm());
        return new OKResponse();
    }

    @PostMapping("/auth/resetPassword")
    public OKResponse resetPassword(@RequestBody ResetPassword resetPassword) {
        identityService.resetPassword(resetPassword.getEmail(), resetPassword.getUsername());
        return new OKResponse();
    }

    @PostMapping("/auth/changeEmail")
    public OKResponse changeEmail(@RequestBody SecuredUpdate update, Authentication authentication) {
        identityService.changeEmailSelf((Identity) authentication.getPrincipal(), update.getPassword(), update.getValue());
        return new OKResponse();
    }

    @PostMapping("/auth/changeUsername")
    public OKResponse changeUsername(@RequestBody SecuredUpdate update, Authentication authentication) {
        identityService.changeUsernameSelf((Identity) authentication.getPrincipal(), update.getPassword(), update.getValue());
        return new OKResponse();
    }

    @PostMapping("/auth/changePassword")
    public OKResponse changePassword(@RequestBody ChangePassword password, Authentication authentication) {
        identityService.changePassword((Identity) authentication.getPrincipal(), password.getOldPassword(),
                password.getNewPassword(), password.getNewPasswordConfirm());
        return new OKResponse();
    }

    /* Admin features */

    @PreAuthorize("hasAuthority('IDENTITY_GRANT')")
    @PostMapping("/auth/{id}/grant")
    public IdentityDTO grant(@PathVariable Long id, @RequestBody Set<Integer> roleIds, Authentication authentication) {
        return modelMapper.map(
                identityService.grantRoles((Identity) authentication.getPrincipal(), id, roleService.getRoles(roleIds)),
                IdentityDTO.class
        );
    }

    @PreAuthorize("hasAuthority('IDENTITY_DENY')")
    @PostMapping("/auth/{id}/deny")
    public IdentityDTO deny(@PathVariable Long id, @RequestBody Set<Integer> roleIds, Authentication authentication) {
        return modelMapper.map(
                identityService.denyRoles((Identity) authentication.getPrincipal(), id, roleService.getRoles(roleIds)),
                IdentityDTO.class
        );
    }

    @PreAuthorize("hasAuthority('IDENTITY_GET') || @identityAuthorization.isSelf(authentication, #id)")
    @GetMapping("/auth/{id}")
    public IdentityDTO get(@PathVariable Long id) {
        return modelMapper.map(
                identityService.get(id),
                IdentityDTO.class
        );
    }

    @PreAuthorize("hasAuthority('IDENTITY_UPDATE')")
    @PutMapping("/auth/{id}")
    public IdentityDTO put(@PathVariable Long id, @RequestBody IdentityUpdate update, Authentication authentication) {
        return modelMapper.map(
                identityService.updateIdentity((Identity) authentication.getPrincipal(), id, update.getUsername(), update.getEmail(), update.getVersion()),
                IdentityDTO.class
        );
    }

    @PreAuthorize("hasAuthority('IDENTITY_DELETE')")
    @DeleteMapping("/auth/{id}")
    public OKResponse delete(@PathVariable Long id, Authentication authentication) {
        identityService.delete((Identity) authentication.getPrincipal(), id);
        return new OKResponse();
    }

    @PreAuthorize("hasAuthority('IDENTITY_LIST')")
    @PostMapping("/auth/list")
    public Page<IdentityDTO> list(@RequestBody Condition condition, Pageable pageable) {
        conditionValidator.validate(condition);
        return identityService.list(condition, pageable).map(source -> modelMapper.map(source, IdentityDTO.class));
    }

}
