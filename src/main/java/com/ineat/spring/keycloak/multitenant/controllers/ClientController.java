package com.ineat.spring.keycloak.multitenant.controllers;

import com.ineat.spring.keycloak.multitenant.dto.ClientRequest;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ineat.spring.keycloak.multitenant.utils.*;

import javax.ws.rs.core.Response;

@RestController
@RequestMapping("/keycloak")
public class ClientController {

    @PostMapping("/create-public-client")
    public ResponseEntity<String> createPublicClient(@RequestBody ClientRequest clientRequest){
        Keycloak keycloakInstance = AdminClientUtils.getKeycloakInstance();
        RealmResource createdRealmResource = keycloakInstance.realms().realm(clientRequest.getRealmName());

        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId(clientRequest.getClientId());
        clientRepresentation.setProtocol(clientRequest.getProtocol());//openid-connect
        clientRepresentation.setEnabled(clientRequest.getEnabled());
//      clientRepresentation.setPublicClient(true);
        clientRepresentation.setRootUrl(clientRequest.getRootUrl());
        Response response = createdRealmResource.clients().create(clientRepresentation);

        return new ResponseEntity<>(HttpStatus.valueOf(response.getStatus()));
    }

    @PostMapping("/create-bearer-client")//bearerOnly
    public ResponseEntity<String> createBearerClient(@RequestBody ClientRequest clientRequest){
        Keycloak keycloakInstance = AdminClientUtils.getKeycloakInstance();
        RealmResource createdRealmResource = keycloakInstance.realms().realm(clientRequest.getRealmName());

        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId(clientRequest.getClientId());
        clientRepresentation.setProtocol(clientRequest.getProtocol());
        clientRepresentation.setEnabled(clientRequest.getEnabled());
        clientRepresentation.setBearerOnly(true);
        clientRepresentation.setRootUrl(clientRequest.getRootUrl());
        Response response = createdRealmResource.clients().create(clientRepresentation);

        return new ResponseEntity<>(HttpStatus.valueOf(response.getStatus()));
    }
}





//        client.setClientId("test-client");
//        client.setProtocol("openid-connect");
//        client.setPublicClient(false);
//        client.setSecret("secret");
//        client.setServiceAccountsEnabled(true);
//        client.setEnabled(true);
//        client.setRedirectUris(Arrays.asList("http://url"));




//    public void updateClientWithAuthzServices() throws Exception {
//        // create a new client
//        ClientRepresentation client = new ClientRepresentation();
//        client.setClientId("my-app");
//        client.setPublicClient(false);
//        client.setBearerOnly(false);
//        client.setStandardFlowEnabled(true);
//        client.setImplicitFlowEnabled(false);
//        client.setDirectAccessGrantsEnabled(true);
//        client.setServiceAccountsEnabled(true);
//        client.setAuthorizationServicesEnabled(true); // TRIGGER!
//        client.setRedirectUris(Collections.singletonList("/"));
//        assertEquals(201, realm.clients().create(client).getStatus());
//
//        // find the ID of the client we just created from its name
//        List<ClientRepresentation> possibleClients =
//                realm.clients().findByClientId(client.getClientId());
//        assertEquals(1, possibleClients.size());
//        client.setId(possibleClients.get(0).getId());
//
//        // verify the client that we created matches what we asked for
//        ClientRepresentation storedClient =
//                realm.clients().get(client.getId()).toRepresentation();
//        client.setId(storedClient.getId());
//        assertClient(client, storedClient);
//
//        // attempt to update the client
//        ClientRepresentation newClient = storedClient;
//        newClient.setClientId("my-app-newname");
//        realm.clients().get(newClient.getId()).update(newClient); // HTTP 409 here!
//        ClientRepresentation newStoredClient =
//                realm.clients().get(newClient.getId()).toRepresentation();
//        assertClient(newClient, newStoredClient);
//    }
