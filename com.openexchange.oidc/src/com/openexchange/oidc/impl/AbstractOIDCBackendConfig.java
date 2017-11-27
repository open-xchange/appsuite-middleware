package com.openexchange.oidc.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.java.Strings;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCBackendProperty;
import com.openexchange.oidc.OIDCProperty;


public abstract class AbstractOIDCBackendConfig implements OIDCBackendConfig {

    private final static Logger LOG = LoggerFactory.getLogger(AbstractOIDCBackendConfig.class);

    private LeanConfigurationService leanConfigurationService;
    
    private String backendName;

    public AbstractOIDCBackendConfig(LeanConfigurationService leanConfigurationService, String backendName) {
        this.leanConfigurationService = leanConfigurationService;
        this.backendName = backendName;
    }

    @Override
    public String getClientID() {
        return this.loadStringProperty(OIDCBackendProperty.clientId);
    }

    @Override
    public String getRedirectURIInit() {
        return this.loadStringProperty(OIDCBackendProperty.redirectURIInit);
    }

    @Override
    public String getRedirectURIAuth() {
        return this.loadStringProperty(OIDCBackendProperty.redirectURIAuth);
    }

    @Override
    public String getAuthorizationEndpoint() {
        return this.loadStringProperty(OIDCBackendProperty.authorizationEndpoint);
    }

    @Override
    public String getTokenEndpoint() {
        return this.loadStringProperty(OIDCBackendProperty.tokenEndpoint);
    }

    @Override
    public String getClientSecret() {
        return this.loadStringProperty(OIDCBackendProperty.clientSecret);
    }

    @Override
    public String getJwkSetEndpoint() {
        return this.loadStringProperty(OIDCBackendProperty.jwkSetEndpoint);
    }

    @Override
    public String getJWSAlgortihm() {
        return this.loadStringProperty(OIDCBackendProperty.jwsAlgorithm);
    }

    @Override
    public String getScope() {
        return this.loadStringProperty(OIDCBackendProperty.scope);
    }

    @Override
    public String getIssuer() {
        return this.loadStringProperty(OIDCBackendProperty.issuer);
    }

    @Override
    public String getResponseType() {
        return this.loadStringProperty(OIDCBackendProperty.responseType);
    }

    @Override
    public String getUserInfoEndpoint() {
        return this.loadStringProperty(OIDCBackendProperty.userInfoEndpoint);
    }

    @Override
    public String getLogoutEndpoint() {
        return this.loadStringProperty(OIDCBackendProperty.logoutEndpoint);
    }

    @Override
    public String getRedirectURIPostSSOLogout() {
        return this.loadStringProperty(OIDCBackendProperty.redirectURIPostSSOLogout);
    }

    @Override
    public boolean isSSOLogout() {
        return this.loadBooleanProperty(OIDCBackendProperty.ssoLogout);
    }

    @Override
    public String getRedirectURILogout() {
        return this.loadStringProperty(OIDCBackendProperty.redirectURILogout);
    }

    @Override
    public String autologinCookieMode() {
        return this.loadStringProperty(OIDCBackendProperty.autologinCookieMode);
    }

    @Override
    public boolean isStoreOAuthTokensEnabled() {
        return this.loadBooleanProperty(OIDCBackendProperty.storeOAuthTokens);
    }

    @Override
    public boolean isAutologinEnabled() {
        boolean result = false;
        AutologinMode autologinMode = OIDCBackendConfig.AutologinMode.get(this.autologinCookieMode());

        if (autologinMode == null) {
            LOG.debug("Unknown value for parameter com.openexchange.oidc.autologinCookieMode. Value is: {}", this.autologinCookieMode());
        } else {
            result = (autologinMode == AutologinMode.OX_DIRECT || autologinMode == AutologinMode.SSO_REDIRECT);
        }
        return result;
    }

    @Override
    public int getOauthRefreshTime() {
        return this.loadIntProperty(OIDCBackendProperty.oauthRefreshTime);
    }

    @Override
    public String getUIWebpath() {
        return this.loadStringProperty(OIDCBackendProperty.uiWebPath);
    }
    
    @Override
    public String getBackendPath() {
        return this.loadStringProperty(OIDCBackendProperty.backendPath);
    }
    
    protected String loadStringProperty(final OIDCBackendProperty backendProperty) {
        String result = "";
        if (Strings.isEmpty(this.backendName)) {
            result = this.leanConfigurationService.getProperty(backendProperty);
        } else {
            result = this.leanConfigurationService.getProperty(this.getCustomProperty(backendProperty));
        }
        return result;
    }
    
    protected int loadIntProperty(final OIDCBackendProperty backendProperty) {
        int result;
        if (Strings.isEmpty(this.backendName)) {
            result = this.leanConfigurationService.getIntProperty(backendProperty);
        } else {
            result = this.leanConfigurationService.getIntProperty(this.getCustomProperty(backendProperty));
        }
        return result;
    }
    
    protected boolean loadBooleanProperty(final OIDCBackendProperty backendProperty) {
        boolean result;
        if (Strings.isEmpty(this.backendName)) {
            result = this.leanConfigurationService.getBooleanProperty(backendProperty);
        } else {
            result = this.leanConfigurationService.getBooleanProperty(this.getCustomProperty(backendProperty));
        }
        return result;
    }
    
    private Property getCustomProperty(final OIDCBackendProperty backendProperty) {
        return new Property() {
            
            @Override
            public String getFQPropertyName() {
                return OIDCProperty.PREFIX + backendName + backendProperty.name();
            }
            
            @Override
            public <T> T getDefaultValue(Class<T> clazz) {
                Object defaultValue = backendProperty.getDefaultValue();
                if (defaultValue .getClass().isAssignableFrom(clazz)) {
                    return clazz.cast(defaultValue);
                }
                throw new IllegalArgumentException("The object cannot be converted to the specified type '" + clazz.getCanonicalName() + "'");
            }
        };
    }
}
