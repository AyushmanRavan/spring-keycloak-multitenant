package com.ineat.spring.keycloak.multitenant.utils;

import org.keycloak.representations.idm.CredentialRepresentation;

public class CredentialsUtils {
    public static CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation passwordCredentials = new CredentialRepresentation();
        passwordCredentials.setTemporary(false);
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(password);
        return passwordCredentials;
    }
}
