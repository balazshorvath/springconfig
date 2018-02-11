package hu.springconfig.controller;

import hu.springconfig.data.dto.authentication.ChangePassword;
import hu.springconfig.data.dto.authentication.IdentityCreate;
import hu.springconfig.data.dto.authentication.IdentityUpdate;
import hu.springconfig.data.dto.authentication.SecuredUpdate;
import hu.springconfig.data.dto.simple.OKResponse;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.query.model.Condition;
import hu.springconfig.service.authentication.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Set;

@RestController
public class IdentityController {
    @Autowired
    private IdentityService identityService;

    /* Standard user features */

    @PostMapping("/auth/register")
    public OKResponse register(@RequestBody IdentityCreate identity) {
        Identity newIdentity = new Identity();
        newIdentity.setUsername(identity.getUsername());
        newIdentity.setEmail(identity.getEmail());
        newIdentity.setRoles(Collections.singleton(new Role(Role.Roles.USER)));

        identityService.createIdentity(newIdentity, identity.getPassword(), identity.getPasswordConfirm());
        return new OKResponse();
    }

    @PostMapping("/auth/resetPassword")
    public OKResponse resetPassword(Authentication authentication) {
        identityService.resetPassword((Identity) authentication.getPrincipal());
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
    public Identity grant(@PathVariable Long id, @RequestBody Set<Role.Roles> roles, Authentication authentication) {
        return identityService.grantRoles((Identity) authentication.getPrincipal(), id, roles);
    }

    @PreAuthorize("hasAuthority('IDENTITY_GET') || @identityAuthorization.isSelf(authentication, #id)")
    @GetMapping("/auth/{id}")
    public Identity get(@RequestParam Long id) {
        return identityService.get(id);
    }

    @PreAuthorize("hasAuthority('IDENTITY_UPDATE')")
    @PutMapping("/auth/{id}")
    public Identity put(@RequestParam Long id, @RequestBody IdentityUpdate update, Authentication authentication) {
        return identityService.updateIdentity((Identity) authentication.getPrincipal(), id, update.getUsername(), update.getEmail());
    }

    @PreAuthorize("hasAuthority('IDENTITY_DELETE')")
    @DeleteMapping("/auth/{id}")
    public OKResponse delete(@RequestParam Long id, Authentication authentication) {
        identityService.delete((Identity) authentication.getPrincipal(), id);
        return new OKResponse();
    }

    @PreAuthorize("hasAuthority('IDENTITY_LIST')")
    @PostMapping("/auth/list")
    public Page<Identity> list(@RequestBody Condition condition, Pageable pageable) {
        return identityService.list(condition, pageable);
    }

}
