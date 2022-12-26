package com.ineat.spring.keycloak.multitenant.controllers;

import com.ineat.spring.keycloak.multitenant.dto.RoleRequest;
import com.ineat.spring.keycloak.multitenant.utils.AdminClientUtils;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/keycloak")
public class ClientRoleController {

    @GetMapping("/client-roles-list")
    public ResponseEntity<List<RoleRepresentation>> getClientRoles(@RequestParam String clientId,@RequestParam String realmName) {
        ClientRepresentation clientRepresentation = AdminClientUtils.getClientsResourceInstance(realmName).findByClientId(clientId).get(0);
        List<RoleRepresentation> roles = AdminClientUtils.getClientsResourceInstance(realmName).get(clientRepresentation.getId()).roles().list();
//          .stream().map(role -> role.getName()).collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/create-client-role")
    public ResponseEntity<String> createClientRole(@RequestBody RoleRequest roleRequest) {
        if (StringUtils.isEmpty(roleRequest.getName()) ) {
            return ResponseEntity.badRequest().body("Empty role name.");
        }
        ClientRepresentation clientRepresentation = AdminClientUtils.getClientsResourceInstance(roleRequest.getRealmName()).findByClientId(roleRequest.getClientId()).get(0);
        RoleRepresentation roleRepresentation = new  RoleRepresentation();
        roleRepresentation.setName(roleRequest.getName());
        roleRepresentation.setDescription("new role " + roleRequest.getDescription());

        AdminClientUtils.getClientsResourceInstance(roleRequest.getRealmName())
                .get(clientRepresentation.getId())
                .roles()
                .create(roleRepresentation);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @GetMapping("/composite-client-role")
    public ResponseEntity<String> makeRoleComposite(@RequestParam String roleName,@RequestParam String clientId,@RequestParam String realmName){
        ClientRepresentation clientRep = AdminClientUtils.getClientsResourceInstance(realmName).findByClientId(clientId).get(0);
        RoleRepresentation role = AdminClientUtils.getClientsResourceInstance(realmName).get(clientRep.getId()).roles().get(roleName).toRepresentation();
        List<RoleRepresentation> composites = new LinkedList<>();
        composites.add(AdminClientUtils.getRolesResourceInstance(realmName).get("offline_access").toRepresentation());
        AdminClientUtils.getRealmResourceInstance(realmName).rolesById().addComposites(role.getId(),composites);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/assign-client-role")
    public ResponseEntity<String> addClientRoleToUser(@RequestParam String username, @RequestParam String roleName,@RequestParam String clientId,@RequestParam String realmName){
        String client_id = AdminClientUtils.getClientsResourceInstance(realmName).findByClientId(clientId).get(0).getId();
        String user_id = AdminClientUtils.getUsersResourceInstance(realmName).search(username).get(0).getId();

        UserResource user = AdminClientUtils.getUsersResourceInstance(realmName).get(user_id);

        List<RoleRepresentation> roleToAdd = new LinkedList<>();
        roleToAdd.add(AdminClientUtils.getClientsResourceInstance(realmName).get(client_id).roles().get(roleName).toRepresentation());
        user.roles().clientLevel(client_id).add(roleToAdd);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
