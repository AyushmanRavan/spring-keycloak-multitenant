package com.ineat.spring.keycloak.multitenant.controllers;

import com.ineat.spring.keycloak.multitenant.dto.UserRequest;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.ineat.spring.keycloak.multitenant.utils.*;

import javax.ws.rs.core.Response;
import java.util.*;

@RestController
@RequestMapping("/keycloak")
public class RealmUserController {

    @PostMapping("/create-user")
    public ResponseEntity<String> createUser(@RequestBody UserRequest userRequest) {
        if (StringUtils.isEmpty(userRequest.getUsername()) || StringUtils.isEmpty(userRequest.getPassword())) {
            return ResponseEntity.badRequest().body("Username and password can not be empty.");
        }
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(userRequest.getUsername());
        userRepresentation.setEmail(userRequest.getEmailId());
        userRepresentation.setFirstName(userRequest.getFirstName());
        userRepresentation.setLastName(userRequest.getLastName());
        userRepresentation.setCredentials(Arrays.asList(CredentialsUtils.createPasswordCredentials(userRequest.getPassword())));
        userRepresentation.setEnabled(true);

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("description", Arrays.asList("A test user"));
        userRepresentation.setAttributes(attributes);

        Response result = AdminClientUtils.getUsersResourceInstance(userRequest.getRealmName()).create(userRepresentation);
        return new ResponseEntity<>(HttpStatus.valueOf(result.getStatus()));
    }

    @PutMapping("/update-user")
    public ResponseEntity<UserRepresentation> updateUser(@RequestBody UserRequest userRequest) {
        Optional<UserRepresentation> user = AdminClientUtils.getUsersResourceInstance(userRequest.getRealmName()).search(userRequest.getUsername()).stream()
                .filter(u -> u.getUsername().equals(userRequest.getUsername())).findFirst();
        if (user.isPresent()) {
            UserRepresentation userRepresentation = user.get();
            UserResource userResource = AdminClientUtils.getUsersResourceInstance(userRequest.getRealmName()).get(userRepresentation.getId());
            userRepresentation.setFirstName(userRequest.getFirstName());
            userRepresentation.setLastName(userRequest.getLastName());
            Map<String, List<String>> attributes = new HashMap<>();
            attributes.put("description", Arrays.asList("put"));
            userRepresentation.setAttributes(attributes);
            userResource.update(userRepresentation);

            return ResponseEntity.ok().body(userRepresentation);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users-list")
    public ResponseEntity<List<UserRepresentation>> getUsers(@RequestParam String realmName) {
        List<UserRepresentation> userRepresentations = AdminClientUtils.getUsersResourceInstance(realmName).list();
        return new ResponseEntity<>(userRepresentations, HttpStatus.OK);
    }

    @GetMapping("/user-by-name")
    public ResponseEntity<UserRepresentation> getUserByName(@RequestParam String username,@RequestParam String realmName) {
        Optional<UserRepresentation> user = AdminClientUtils.getUsersResourceInstance(realmName).search(username).stream()
                .filter(u -> u.getUsername().equals(username)).findFirst();
        if (user.isPresent()) {
            return ResponseEntity.ok().body(user.get());
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/delete-user")
    public void deleteUser(@RequestParam String username,@RequestParam String realmName) {
        UsersResource users = AdminClientUtils.getUsersResourceInstance(realmName);
        users.search(username).stream()
                .forEach(user -> AdminClientUtils.getUsersResourceInstance(realmName).delete(user.getId()));
    }

    @GetMapping("/roles-by-user")
    public ResponseEntity<List<RoleRepresentation>> getRolesByUser(@RequestParam String username, @RequestParam String clientId,@RequestParam String realmName) {
        Optional<UserRepresentation> user = AdminClientUtils.getUsersResourceInstance(realmName).search(username).stream()
                .filter(u -> u.getUsername().equals(username)).findFirst();
        if (user.isPresent()) {
            UserRepresentation userRepresentation = user.get();
            UserResource userResource = AdminClientUtils.getUsersResourceInstance(realmName).get(userRepresentation.getId());
            ClientRepresentation clientRepresentation = AdminClientUtils.getClientsResourceInstance(realmName).findByClientId(clientId).get(0);
            List<RoleRepresentation> roles = userResource.roles().clientLevel(clientRepresentation.getId()).listAll();
            return ResponseEntity.ok(roles);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
