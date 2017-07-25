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
    public String getRedirectURI() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.redirectURI);
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
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.publicRSAKeys);
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
    public String getResponseType() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.responseType);
    }

}
