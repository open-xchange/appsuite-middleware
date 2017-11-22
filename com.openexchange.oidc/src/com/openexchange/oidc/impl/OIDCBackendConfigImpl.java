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

package com.openexchange.oidc.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCBackendProperty;

/**
 * {@link OIDCBackendConfigImpl} Default implementation of {@link OIDCBackendConfig}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class OIDCBackendConfigImpl implements OIDCBackendConfig{

    private final static Logger LOG = LoggerFactory.getLogger(OIDCBackendConfigImpl.class);

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
    public String getJwkSetEndpoint() {
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

    @Override
    public String getLogoutEndpoint() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.logoutEndpoint);
    }

    @Override
    public String getRedirectURIPostSSOLogout() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.redirectURIPostSSOLogout);
    }

    @Override
    public boolean isSSOLogout() {
        return this.leanConfigurationService.getBooleanProperty(OIDCBackendProperty.ssoLogout);
    }

    @Override
    public String getRedirectURILogout() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.redirectURILogout);
    }

    @Override
    public String autologinCookieMode() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.autologinCookieMode);
    }

    @Override
    public boolean isStoreOAuthTokensEnabled() {
        return this.leanConfigurationService.getBooleanProperty(OIDCBackendProperty.storeOAuthTokens);
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
        return this.leanConfigurationService.getIntProperty(OIDCBackendProperty.oauthRefreshTime);
    }

    @Override
    public String getUIWebpath() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.uiWebPath);
    }
    
    @Override
    public String getBackendPath() {
        return this.leanConfigurationService.getProperty(OIDCBackendProperty.backendPath);
    }
}
