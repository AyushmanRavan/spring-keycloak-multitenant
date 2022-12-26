package com.ineat.spring.keycloak.multitenant.dto;

public class RealmRequest {
    private String realmId;
    private String realmName;
    private Boolean  realmEnabled;

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public Boolean getRealmEnabled() {
        return realmEnabled;
    }

    public void setRealmEnabled(Boolean realmEnabled) {
        this.realmEnabled = realmEnabled;
    }
}
