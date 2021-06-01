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

package com.openexchange.oidc.tools;

import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.l;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import com.openexchange.authentication.NamePart;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCBackendProperty;

/**
 * An {@link OIDCBackendConfig} that returns default values, but can be adjusted
 * to return certain values. Of course it can also be mocked to let certain methods
 * return mock values.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class TestBackendConfig implements OIDCBackendConfig {

    static String OP_BASE_URI = "https://sso.example.com/oauth";

    static String RP_BASE_URI = "https://mail.example.com/appsuite/api/oidc";

    private final Map<String, Object> properties;

    public TestBackendConfig() {
        properties = new HashMap<>();
    }

    public TestBackendConfig(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Deprecated
    public TestBackendConfig(Properties properties) {
        this.properties = new HashMap<>();
        for (Entry<Object, Object> entry : properties.entrySet()) {
            this.properties.put(entry.getKey().toString(), entry.getValue());
        }
    }

    @Override
    public String getClientID() {
        return getOrDefault(OIDCBackendProperty.clientId);
    }

    @Override
    public String getRpRedirectURIAuth() {
        return getOrDefault(OIDCBackendProperty.rpRedirectURIAuth);
    }

    @Override
    public String getOpAuthorizationEndpoint() {
        return getOrDefault(OIDCBackendProperty.opAuthorizationEndpoint);
    }

    @Override
    public String getOpTokenEndpoint() {
        return getOrDefault(OIDCBackendProperty.opTokenEndpoint);
    }

    @Override
    public String getClientSecret() {
        return getOrDefault(OIDCBackendProperty.clientSecret);
    }

    @Override
    public String getOpJwkSetEndpoint() {
        return getOrDefault(OIDCBackendProperty.opJwkSetEndpoint);
    }

    @Override
    public String getJWSAlgortihm() {
        return getOrDefault(OIDCBackendProperty.jwsAlgorithm);
    }

    @Override
    public String getScope() {
        return getOrDefault(OIDCBackendProperty.scope);
    }

    @Override
    public String getOpIssuer() {
        return getOrDefault(OIDCBackendProperty.opIssuer);
    }

    @Override
    public String getResponseType() {
        return getOrDefault(OIDCBackendProperty.responseType);
    }

    @Override
    public String getOpLogoutEndpoint() {
        return getOrDefault(OIDCBackendProperty.opLogoutEndpoint);
    }

    @Override
    public String getRpRedirectURIPostSSOLogout() {
        return getOrDefault(OIDCBackendProperty.rpRedirectURIPostSSOLogout);
    }

    @Override
    public boolean isSSOLogout() {
        return b(Boolean.class.cast(getOrDefault(OIDCBackendProperty.ssoLogout)));
    }

    @Override
    public String getRpRedirectURILogout() {
        return getOrDefault(OIDCBackendProperty.rpRedirectURILogout);
    }

    @Override
    public String autologinCookieMode() {
        return getOrDefault(OIDCBackendProperty.autologinCookieMode);
    }

    @Override
    public boolean isAutologinEnabled() {
        return !"off".equals(autologinCookieMode());
    }

    @Override
    public int getOauthRefreshTime() {
        return i(getOrDefault(OIDCBackendProperty.oauthRefreshTime));
    }

    @Override
    public String getUIWebpath() {
        return getOrDefault(OIDCBackendProperty.uiWebPath);
    }

    @Override
    public String getBackendPath() {
        return getOrDefault(OIDCBackendProperty.backendPath);
    }

    @Override
    public List<String> getHosts() {
        return getOrDefault(OIDCBackendProperty.hosts);
    }

    @Override
    public String getFailureRedirect() {
        return null;
    }

    @Override
    public String getContextLookupClaim() {
        return "sub";
    }

    @Override
    public NamePart getContextLookupNamePart() {
        return NamePart.DOMAIN;
    }

    @Override
    public String getUserLookupClaim() {
        return "sub";
    }

    @Override
    public NamePart getUserLookupNamePart() {
        return NamePart.LOCAL_PART;
    }

    @Override
    public NamePart getPasswordGrantUserNamePart() {
        return NamePart.FULL;
    }

    @Override
    public long getTokenLockTimeoutSeconds() {
        return l(getOrDefault(OIDCBackendProperty.tokenLockTimeoutSeconds));
    }

    @Override
    public boolean tryRecoverStoredTokens() {
        return b(Boolean.class.cast(getOrDefault(OIDCBackendProperty.tryRecoverStoredTokens)));
    }

    public void setProperty(OIDCBackendProperty property, Object value) {
        properties.put(property.getFQPropertyName(), value);
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrDefault(OIDCBackendProperty property) {
        String name = property.getFQPropertyName();
        Object value;
        if (properties.containsKey(name)) {
            value = properties.get(name);
        } else {
            value = property.getDefaultValue();
        }

        if (value == null) {
            return null;
        }

        return (T) value;
    }

}