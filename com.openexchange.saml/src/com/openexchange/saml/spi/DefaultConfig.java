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

package com.openexchange.saml.spi;

import static com.openexchange.saml.SAMLProperties.*;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.saml.SAMLConfig;

/**
 * Default implementation of {@link SAMLConfig} based on {@link ConfigurationService} and
 * <code>saml.properties</code>.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class DefaultConfig implements SAMLConfig {

    private static final ImmutableSet<String> ALL_HOSTS_SET = ImmutableSet.of("all");

    /**
     * Initializes the default configuration using given service.
     *
     * @param configService The lean configuration service to use
     * @return The initialized default configuration
     * @throws OXException If initialization fails
     */
    public static DefaultConfig init(LeanConfigurationService configService) throws OXException {
        return init(configService, false);
    }

    /**
     * Initializes the default configuration using given service.
     *
     * @param configService The lean configuration service to use
     * @param optional Whether the values are to be considered optional or not
     * @return The initialized default configuration
     * @throws OXException If initialization fails
     */
    public static DefaultConfig init(LeanConfigurationService configService, boolean optional) throws OXException {
        String providerName = checkProperty(configService, PROVIDER_NAME, optional);
        String entityID = checkProperty(configService, ENTITY_ID, optional);
        String acsURL = checkProperty(configService, ACS_URL, optional);
        String idpEntityID = checkProperty(configService, IDP_ENTITY_ID, optional);
        String idpAuthnURL = checkProperty(configService, IDP_LOGIN_URL, optional);
        boolean supportSingleLogout = configService.getBooleanProperty(ENABLE_SINGLE_LOGOUT);
        String slsURL = null;
        String idpLogoutURL = null;
        Binding logoutResponseBinding = null;
        String logoutResponseTemplate = null;
        if (supportSingleLogout) {
            slsURL = checkProperty(configService, SLS_URL, optional);
            idpLogoutURL = checkProperty(configService, IDP_LOGOUT_URL, optional);
            logoutResponseBinding = checkBinding(configService, LOGOUT_RESPONSE_BINDING, optional);
            if (logoutResponseBinding == Binding.HTTP_POST) {
                logoutResponseTemplate = checkProperty(configService, LOGOUT_RESPONSE_POST_TEMPLATE, optional);
            }
        }
        boolean enableMetadataService = configService.getBooleanProperty(ENABLE_METADATA_SERVICE);
        boolean autoLoginEnabled = configService.getBooleanProperty(ENABLE_AUTO_LOGIN);
        boolean allowUnsolicitedResponses = configService.getBooleanProperty(ALLOW_UNSOLICITED_RESPONSES);
        boolean sessionIndexAutoLoginEnabled = configService.getBooleanProperty(ENABLE_SESSION_INDEX_AUTO_LOGIN);

        DefaultConfig config = new DefaultConfig(providerName, entityID, acsURL, logoutResponseBinding, idpAuthnURL, idpEntityID,
            idpLogoutURL, slsURL, supportSingleLogout, enableMetadataService, logoutResponseTemplate, autoLoginEnabled,
            allowUnsolicitedResponses, sessionIndexAutoLoginEnabled);
        return config;
    }

    private static Binding checkBinding(LeanConfigurationService configService, Property property, boolean optional) throws OXException {
        String bindingName = checkProperty(configService, property, optional);
        if (bindingName == null) {
            if (optional) {
                return null;
            }
        } else if ("http-redirect".equals(bindingName)) {
            return Binding.HTTP_REDIRECT;
        } else if ("http-post".equals(bindingName)) {
            return Binding.HTTP_POST;
        }

        throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(property + " = " + bindingName);
    }

    private static String checkProperty(LeanConfigurationService configService, Property prop, boolean optional) throws OXException {
        String property = configService.getProperty(prop);
        if (Strings.isEmpty(property)) {
            if (optional) {
                return null;
            }
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(prop);
        }

        return property;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String providerName;
    private final String entityID;
    private final String acsURL;
    private final Binding logoutResponseBinding;
    private final String idpAuthnURL;
    private final String idpEntityID;
    private final String idpLogoutURL;
    private final String slsURL;
    private final boolean supportSingleLogout;
    private final boolean enableMetadataService;
    private final String logoutResponseTemplate;
    private final boolean autoLoginEnabled;
    private final boolean allowUnsolicitedResponses;
    private final boolean sessionIndexAutoLoginEnabled;

    /**
     * Initializes a new {@link DefaultConfig}.
     *
     * @param providerName
     * @param entityID
     * @param acsURL
     * @param logoutResponseBinding
     * @param idpAuthnURL
     * @param idpEntityID
     * @param idpLogoutURL
     * @param slsURL
     * @param supportSingleLogout
     * @param enableMetadataService
     * @param logoutResponseTemplate
     * @param autoLoginEnabled
     * @param allowUnsolicitedResponses
     * @param sessionIndexAutoLoginEnabled
     */
    private DefaultConfig(String providerName, String entityID, String acsURL, Binding logoutResponseBinding, String idpAuthnURL,
            String idpEntityID, String idpLogoutURL, String slsURL, boolean supportSingleLogout, boolean enableMetadataService,
            String logoutResponseTemplate, boolean autoLoginEnabled, boolean allowUnsolicitedResponses, boolean sessionIndexAutoLoginEnabled) {
        super();
        this.providerName = providerName;
        this.entityID = entityID;
        this.acsURL = acsURL;
        this.logoutResponseBinding = logoutResponseBinding;
        this.idpAuthnURL = idpAuthnURL;
        this.idpEntityID = idpEntityID;
        this.idpLogoutURL = idpLogoutURL;
        this.slsURL = slsURL;
        this.supportSingleLogout = supportSingleLogout;
        this.enableMetadataService = enableMetadataService;
        this.logoutResponseTemplate = logoutResponseTemplate;
        this.autoLoginEnabled = autoLoginEnabled;
        this.allowUnsolicitedResponses = allowUnsolicitedResponses;
        this.sessionIndexAutoLoginEnabled = sessionIndexAutoLoginEnabled;
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
    public String getEntityID() {
        return entityID;
    }

    @Override
    public String getAssertionConsumerServiceURL() {
        return acsURL;
    }

    @Override
    public String getSingleLogoutServiceURL() {
        return slsURL;
    }

    @Override
    public Binding getLogoutResponseBinding() {
        return logoutResponseBinding;
    }

    @Override
    public String getIdentityProviderEntityID() {
        return idpEntityID;
    }

    @Override
    public String getIdentityProviderAuthnURL() {
        return idpAuthnURL;
    }

    @Override
    public String getIdentityProviderLogoutURL() {
        return idpLogoutURL;
    }

    @Override
    public boolean singleLogoutEnabled() {
        return supportSingleLogout;
    }

    @Override
    public boolean enableMetadataService() {
        return enableMetadataService;
    }

    @Override
    public String getLogoutResponseTemplate() {
        return logoutResponseTemplate;
    }

    @Override
    public boolean isAutoLoginEnabled() {
        return autoLoginEnabled;
    }

    @Override
    public boolean isAllowUnsolicitedResponses() {
        return allowUnsolicitedResponses;
    }

    @Override
    public boolean isSessionIndexAutoLoginEnabled() {
        return sessionIndexAutoLoginEnabled;
    }

    @Override
    public Set<String> getHosts() {
        return ALL_HOSTS_SET;
    }

}
