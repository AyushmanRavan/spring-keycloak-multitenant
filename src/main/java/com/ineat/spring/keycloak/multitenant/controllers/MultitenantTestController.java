package com.ineat.spring.keycloak.multitenant.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MultitenantTestController {

    @GetMapping("/admin")
    public String adminSecuredEndpoint(){
        return "Documents admin";
    }

    @GetMapping("/user")
    public String userSecuredEndpoint(){
        return "Documents user";
    }

    @GetMapping("/all-user")
    public String allUserEndpoint(){
        return "Documents all-user";
    }

    @GetMapping("/unsecured")
    public String unsecuredEndpoint(){
        return "Documents public";
    }
}