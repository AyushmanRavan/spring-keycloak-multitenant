package com.ineat.spring.keycloak.multitenant.controllers;

import com.ineat.spring.keycloak.multitenant.dto.RoleRequest;
import com.ineat.spring.keycloak.multitenant.utils.AdminClientUtils;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/keycloak")
public class RealmRoleController {
    @GetMapping("/realm-roles-list")
    public ResponseEntity<List<RoleRepresentation>> getRealmRoles(@RequestParam String realmName) {
        List<RoleRepresentation> availableRoles = AdminClientUtils.getRolesResourceInstance(realmName).list();
//                .stream().map(role -> role.getName()).collect(Collectors.toList());
        return ResponseEntity.ok(availableRoles);
    }

    @PostMapping("/create-realm-role")
    public ResponseEntity<String> createRealmRole(@RequestBody RoleRequest roleRequest) {
        if (StringUtils.isEmpty(roleRequest.getName()) ) {
            return ResponseEntity.badRequest().body("Empty role name.");
        }
        RoleRepresentation roleRepresentation = new  RoleRepresentation();
        roleRepresentation.setName(roleRequest.getName());
        roleRepresentation.setDescription("new role " + roleRequest.getDescription());
        AdminClientUtils.getRolesResourceInstance(roleRequest.getRealmName()).create(roleRepresentation);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/composite-realm-role")
    public ResponseEntity<String> makeRoleComposite(@RequestParam String roleName,@RequestParam String realmName){
        RoleRepresentation role =AdminClientUtils.getRolesResourceInstance(realmName).get(roleName).toRepresentation();
        List<RoleRepresentation> composites = new LinkedList<>();
        composites.add(AdminClientUtils.getRolesResourceInstance(realmName).get("offline_access").toRepresentation());
        AdminClientUtils.getRealmResourceInstance(realmName).rolesById().addComposites(role.getId(), composites);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/assign-realm-role")
    public ResponseEntity<String> addRealmRoleToUser(@RequestParam String username, @RequestParam String roleName,@RequestParam String realmName){
        String userId = AdminClientUtils.getUsersResourceInstance(realmName).search(username).get(0).getId();
        UserResource user = AdminClientUtils.getUsersResourceInstance(realmName).get(userId);
        List<RoleRepresentation> roleToAdd = new LinkedList<>();
        roleToAdd.add(AdminClientUtils.getRolesResourceInstance(realmName).get(roleName).toRepresentation());
        user.roles().realmLevel().add(roleToAdd);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
