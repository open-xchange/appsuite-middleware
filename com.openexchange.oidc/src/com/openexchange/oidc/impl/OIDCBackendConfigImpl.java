package com.openexchange.oidc.impl;

import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCBackendProperty;

public class OIDCBackendConfigImpl implements OIDCBackendConfig{
    
    private LeanConfigurationService leanConfigurationService; 
    
    public OIDCBackendConfigImpl(LeanConfigurationService leanConfigurationService) {
        this.leanConfigurationService = leanConfigurationService;
    }

    @Override
    public String getClientID() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.clientId);
    }

    @Override
    public String getRedirectURIInit() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.redirectURIInit);
    }
    
    @Override
    public String getRedirectURIAuth() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.redirectURIAuth);
    }

    @Override
    public String getAuthorizationEndpoint() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.authorizationEndpoint);
    }

    @Override
    public String getTokenEndpoint() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.tokenEndpoint);
    }

    @Override
    public String getClientSecret() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.clientSecret);
    }

    @Override
    public String getJwkSet() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.jwkSetEndpoint);
    }

    @Override
    public String getJWSAlgortihm() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.jwsAlgorithm);
    }
    
    @Override
    public String getScope() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.scope);
    }
    
    @Override
    public String getIssuer() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.issuer);
    }

    @Override
    public String getResponseType() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.responseType);
    }
    
    @Override
    public String getUserInfoEndpoint() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.userInfoEndpoint);
    }

}
