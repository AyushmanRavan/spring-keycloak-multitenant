package com.ineat.spring.keycloak.multitenant.utils;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;

public class AdminClientUtils {
    private final static String masterRealm="master";
    private final static String clientId="admin-cli";
    private final static String userName="admin";
    private final static String password="admin";
    private final static String serverUrl="http://localhost:8080/auth/";

//    private final static String applicationRealm="ineat-realm";
//    private final static String clientSecret="49be0029-d928-43ae-883e-6b32d60e7f12";

    public AdminClientUtils() {
    }
    public static Keycloak getKeycloakInstance(){
        return KeycloakBuilder.builder()
                .realm(masterRealm)
                .clientId(clientId)
                .username(userName)
                .password(password)
                .serverUrl(serverUrl)
                .grantType(OAuth2Constants.PASSWORD)
//              .clientSecret(clientSecret)
//              .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();
    }

    public static UsersResource getUsersResourceInstance(String realmName){
        return getKeycloakInstance().realm(realmName).users();
    }

    public static ClientsResource getClientsResourceInstance(String realmName){
        return getKeycloakInstance().realm(realmName).clients();
    }

    public static RolesResource getRolesResourceInstance(String realmName){
        return getKeycloakInstance().realm(realmName).roles();
    }

    public static RealmResource getRealmResourceInstance(String realmName){
        return getKeycloakInstance().realm(realmName);
    }
}
