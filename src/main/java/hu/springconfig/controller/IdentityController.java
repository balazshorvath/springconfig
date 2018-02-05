package hu.springconfig.controller;

import hu.springconfig.data.dto.authentication.IdentityUpdate;
import hu.springconfig.data.dto.simple.OKResponse;
import hu.springconfig.service.authentication.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/auth")
public class IdentityController {
    @Autowired
    private IdentityService identityService;

    /**
     * Create a new {@link hu.springconfig.data.entity.authentication.Identity}.
     *
     * @param identity
     * @return
     */
    @PostMapping("/register")
    public OKResponse register(@RequestBody IdentityUpdate identity){

        return new OKResponse();
    }


}
