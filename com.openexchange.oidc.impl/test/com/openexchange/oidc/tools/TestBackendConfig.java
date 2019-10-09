/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.oidc.tools;

import java.util.List;
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

    private final Properties properties;

    public TestBackendConfig() {
        this(new Properties());
    }

    public TestBackendConfig(Properties properties) {
        this.properties = properties;
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
        return getOrDefault(OIDCBackendProperty.rpRedirectURIPostSSOLogout);
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
        return getOrDefault(OIDCBackendProperty.oauthRefreshTime);
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
        return getOrDefault(OIDCBackendProperty.tokenLockTimeoutSeconds);
    }

    @Override
    public boolean tryRecoverStoredTokens() {
        return getOrDefault(OIDCBackendProperty.tryRecoverStoredTokens);
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