package com.ineat.spring.keycloak.multitenant.controllers;

import com.ineat.spring.keycloak.multitenant.dto.RealmRequest;
import com.ineat.spring.keycloak.multitenant.utils.AdminClientUtils;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/keycloak")
public class RealmController {

    @PostMapping("/create-realm")
    public ResponseEntity<String> createRealm(@RequestBody RealmRequest realmRequest){
        RealmRepresentation realm = new RealmRepresentation();
        realm.setEnabled(true);
        realm.setId(realmRequest.getRealmId());
        realm.setRealm(realmRequest.getRealmName());
        AdminClientUtils.getKeycloakInstance().realms().create(realm);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}

