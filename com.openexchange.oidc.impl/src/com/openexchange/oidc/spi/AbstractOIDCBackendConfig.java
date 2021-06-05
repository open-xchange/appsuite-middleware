/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.oidc.spi;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.authentication.NamePart;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.java.Strings;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCBackendProperty;
import com.openexchange.oidc.OIDCProperty;


/**
 * {@link AbstractOIDCBackendConfig} Abstract implementation of the {@link OIDCBackendConfig} interface.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public abstract class AbstractOIDCBackendConfig implements OIDCBackendConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractOIDCBackendConfig.class);

    /** The reference to tracked <code>LeanConfigurationService</code> */
    protected final LeanConfigurationService leanConfigurationService;

    /**
     * customPropertyPrefix - If set, it identifies this {@link OIDCBackendConfig}. The set value
     * is appended to OIDCProperty.PREFIX.
     * See <code>com.openexchange.oidc.spi.AbstractOIDCBackendConfig.getCustomProperty(OIDCBackendProperty)</code>
     * for more information
     */
    protected final String customPropertyPrefix;

    /**
     * Initializes a new {@link AbstractOIDCBackendConfig}. The {@link LeanConfigurationService} is
     * used to load all properties. The {@link backendName} is used to add a custom name to the prefix of
     * every property, before loading. Can be null, this way the prefix stored in {@link OIDCProperty}
     * will be used.
     *
     * @param leanConfigurationService - The {@link LeanConfigurationService} to use
     * @param customPropertyPrefix - The additional prefix like "custom.", the dot at the end is mandatory. Can be null.
     */
    protected AbstractOIDCBackendConfig(LeanConfigurationService leanConfigurationService, String customPropertyPrefix) {
        this.leanConfigurationService = leanConfigurationService;
        this.customPropertyPrefix = customPropertyPrefix;
    }

    @Override
    public String getClientID() {
        return this.loadStringProperty(OIDCBackendProperty.clientId);
    }

    @Override
    public String getRpRedirectURIAuth() {
        return this.loadStringProperty(OIDCBackendProperty.rpRedirectURIAuth);
    }

    @Override
    public String getOpAuthorizationEndpoint() {
        return this.loadStringProperty(OIDCBackendProperty.opAuthorizationEndpoint);
    }

    @Override
    public String getOpTokenEndpoint() {
        return this.loadStringProperty(OIDCBackendProperty.opTokenEndpoint);
    }

    @Override
    public String getClientSecret() {
        return this.loadStringProperty(OIDCBackendProperty.clientSecret);
    }

    @Override
    public String getOpJwkSetEndpoint() {
        return this.loadStringProperty(OIDCBackendProperty.opJwkSetEndpoint);
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
    public String getOpIssuer() {
        return this.loadStringProperty(OIDCBackendProperty.opIssuer);
    }

    @Override
    public String getResponseType() {
        return this.loadStringProperty(OIDCBackendProperty.responseType);
    }

    @Override
    public String getOpLogoutEndpoint() {
        return this.loadStringProperty(OIDCBackendProperty.opLogoutEndpoint);
    }

    @Override
    public String getRpRedirectURIPostSSOLogout() {
        return this.loadStringProperty(OIDCBackendProperty.rpRedirectURIPostSSOLogout);
    }

    @Override
    public boolean isSSOLogout() {
        return this.loadBooleanProperty(OIDCBackendProperty.ssoLogout);
    }

    @Override
    public String getRpRedirectURILogout() {
        return this.loadStringProperty(OIDCBackendProperty.rpRedirectURILogout);
    }

    @Override
    public String autologinCookieMode() {
        return this.loadStringProperty(OIDCBackendProperty.autologinCookieMode);
    }

    @Override
    public List<String> getHosts() {
        String[] hosts = this.loadStringProperty(OIDCBackendProperty.hosts).split(",");
        return Arrays.asList(hosts);
    }

    @Override
    public boolean isAutologinEnabled() {
        return true;
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

    @Override
    public String getFailureRedirect() {
        return this.loadStringProperty(OIDCBackendProperty.failureRedirect);
    }

    @Override
    public NamePart getPasswordGrantUserNamePart() {
        return this.loadNamePartProperty(OIDCBackendProperty.passwordGrantUserNamePart);
    }

    @Override
    public String getContextLookupClaim() {
        return this.loadStringProperty(OIDCBackendProperty.contextLookupClaim);
    }

    @Override
    public NamePart getContextLookupNamePart() {
        return this.loadNamePartProperty(OIDCBackendProperty.contextLookupNamePart);
    }

    @Override
    public String getUserLookupClaim() {
        return this.loadStringProperty(OIDCBackendProperty.userLookupClaim);
    }

    @Override
    public NamePart getUserLookupNamePart() {
        return this.loadNamePartProperty(OIDCBackendProperty.userLookupNamePart);
    }

    @Override
    public long getTokenLockTimeoutSeconds() {
        return this.loadIntProperty(OIDCBackendProperty.tokenLockTimeoutSeconds);
    }

    @Override
    public boolean tryRecoverStoredTokens() {
        return this.loadBooleanProperty(OIDCBackendProperty.tryRecoverStoredTokens);
    }

    /**
     * Load the given {@link String} property. If a custom backendName is stored, this name
     * is added to the prefix, to load the correct property.
     *
     * @param backendProperty - The {@link OIDCBackendProperty} to load
     * @return The {@link String} value
     */
    protected String loadStringProperty(final OIDCBackendProperty backendProperty) {
        String result = "";
        if (!Strings.isEmpty(this.customPropertyPrefix)) {
            result = this.leanConfigurationService.getProperty(this.getCustomProperty(backendProperty));
        }
        if (Strings.isEmpty(result)) {
            result = this.leanConfigurationService.getProperty(backendProperty);
        }
        return result;
    }

    /**
     * Load the given {@link int} property. If a custom backendName is stored, this name
     * is added to the prefix, to load the correct property.
     *
     * @param backendProperty - The {@link OIDCBackendProperty} to load
     * @return The {@link int} value
     */
    protected int loadIntProperty(final OIDCBackendProperty backendProperty) {
        if (Strings.isEmpty(this.customPropertyPrefix)) {
            return this.leanConfigurationService.getIntProperty(backendProperty);
        }

        return this.leanConfigurationService.getIntProperty(this.getCustomProperty(backendProperty));
    }

    /**
     * Load the given {@link boolean} property. If a custom backendName is stored, this name
     * is added to the prefix, to load the correct property.
     *
     * @param backendProperty - The {@link OIDCBackendProperty} to load
     * @return The {@link boolean} value
     */
    protected boolean loadBooleanProperty(final OIDCBackendProperty backendProperty) {
        if (Strings.isEmpty(this.customPropertyPrefix)) {
            return this.leanConfigurationService.getBooleanProperty(backendProperty);
        }

        return this.leanConfigurationService.getBooleanProperty(this.getCustomProperty(backendProperty));
    }

    protected NamePart loadNamePartProperty(OIDCBackendProperty backendProperty) {
        String value = this.loadStringProperty(backendProperty);
        NamePart namePart = NamePart.of(value);
        if (namePart == null) {
            namePart = backendProperty.getDefaultValue(NamePart.class);
            LOG.warn("Illegal value '{}' for property '{}'. Falling back to default: {}",
                value, backendProperty.getFQPropertyName(), namePart.getConfigName());
        }
        return namePart;
    }

    protected Property getCustomProperty(final OIDCBackendProperty backendProperty) {
        return new Property() {

            @Override
            public String getFQPropertyName() {
                return OIDCProperty.PREFIX + customPropertyPrefix + backendProperty.name();
            }

            @Override
            public Object getDefaultValue() {
                return backendProperty.getDefaultValue();
            }
        };
    }
}
