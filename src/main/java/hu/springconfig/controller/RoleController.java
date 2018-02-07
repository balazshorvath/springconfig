package hu.springconfig.controller;

import hu.springconfig.data.entity.authentication.Role;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class RoleController {

    @PreAuthorize("hasAuthority('ROLE_GET')")
    @GetMapping("/role/{id}")
    public Role get(@PathVariable Long id){
        throw new UnsupportedOperationException();
    }

    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @PutMapping("/role/{id}")
    public Role put(@PathVariable Long id){
        throw new UnsupportedOperationException();
    }

    @PreAuthorize("hasAuthority('ROLE_LIST')")
    @PostMapping("/role/list")
    public Role list(){
        throw new UnsupportedOperationException();
    }
}
