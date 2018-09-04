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

package com.openexchange.oidc.spi;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public String getRpRedirectURIInit() {
        return this.loadStringProperty(OIDCBackendProperty.rpRedirectURIInit);
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
        boolean result = false;
        String autologinCookieMode = this.autologinCookieMode();
        AutologinMode autologinMode = OIDCBackendConfig.AutologinMode.get(autologinCookieMode);

        if (autologinMode == null) {
            LOG.debug("Unknown value for parameter com.openexchange.oidc.autologinCookieMode. Value is: {}", autologinCookieMode);
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
        return Integer.parseInt(this.loadStringProperty(backendProperty));
    }

    /**
     * Load the given {@link boolean} property. If a custom backendName is stored, this name
     * is added to the prefix, to load the correct property.
     *
     * @param backendProperty - The {@link OIDCBackendProperty} to load
     * @return The {@link boolean} value
     */
    protected boolean loadBooleanProperty(final OIDCBackendProperty backendProperty) {
        return Boolean.parseBoolean(this.loadStringProperty(backendProperty));
    }

    protected Property getCustomProperty(final OIDCBackendProperty backendProperty) {
        return new Property() {

            @Override
            public String getFQPropertyName() {
                return OIDCProperty.PREFIX + customPropertyPrefix + backendProperty.name();
            }

            @Override
            public <T extends Object> T getDefaultValue(Class<T> clazz) throws IllegalArgumentException {
                Object defaultValue = backendProperty.getDefaultValue();
                if (null == defaultValue) {
                    return null;
                }
                if (clazz.isAssignableFrom(defaultValue.getClass())) {
                    return clazz.cast(defaultValue);
                }
                throw new IllegalArgumentException("The object cannot be converted to the specified type '" + clazz.getCanonicalName() + "'");
            }
        };
    }
}
