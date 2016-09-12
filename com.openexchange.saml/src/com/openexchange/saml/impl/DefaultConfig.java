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

package com.openexchange.saml.impl;

import static com.openexchange.saml.SAMLProperties.ACS_URL;
import static com.openexchange.saml.SAMLProperties.ALLOW_UNSOLICITED_RESPONSES;
import static com.openexchange.saml.SAMLProperties.ENABLE_AUTO_LOGIN;
import static com.openexchange.saml.SAMLProperties.ENABLE_METADATA_SERVICE;
import static com.openexchange.saml.SAMLProperties.ENABLE_SINGLE_LOGOUT;
import static com.openexchange.saml.SAMLProperties.ENTITY_ID;
import static com.openexchange.saml.SAMLProperties.IDP_ENTITY_ID;
import static com.openexchange.saml.SAMLProperties.IDP_LOGIN_URL;
import static com.openexchange.saml.SAMLProperties.IDP_LOGOUT_URL;
import static com.openexchange.saml.SAMLProperties.LOGOUT_RESPONSE_BINDING;
import static com.openexchange.saml.SAMLProperties.LOGOUT_RESPONSE_POST_TEMPLATE;
import static com.openexchange.saml.SAMLProperties.PROVIDER_NAME;
import static com.openexchange.saml.SAMLProperties.SLS_URL;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.saml.SAMLConfig;

/**
 * Default implementation of {@link SAMLConfig} based on {@link ConfigurationService} and
 * <code>saml.properties</code>.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class DefaultConfig implements SAMLConfig {

    private String providerName;

    private String entityID;

    private String acsURL;

    private Binding logoutResponseBinding;

    private String idpAuthnURL;

    private String idpEntityID;

    private String idpLogoutURL;

    private String slsURL;

    private boolean supportSingleLogout;

    private boolean enableMetadataService;

    private String logoutResponseTemplate;

    private boolean autoLoginEnabled;

    private boolean allowUnsolicitedResponses;

    private DefaultConfig() {
        super();
    }

    public static DefaultConfig init(ConfigurationService configService) throws OXException {
        DefaultConfig config = new DefaultConfig();
        config.setProviderName(checkProperty(configService, PROVIDER_NAME));
        config.setEntityID(checkProperty(configService, ENTITY_ID));
        config.setAcsURL(checkProperty(configService, ACS_URL));
        config.setIdpEntityID(checkProperty(configService, IDP_ENTITY_ID));
        config.setIdpURL(checkProperty(configService, IDP_LOGIN_URL));
        boolean supportSingleLogout = configService.getBoolProperty(ENABLE_SINGLE_LOGOUT, false);
        if (supportSingleLogout) {
            config.setSupportSingleLogout(supportSingleLogout);
            config.setSingleLogoutServiceURL(checkProperty(configService, SLS_URL));
            config.setIdentityProviderLogoutURL(checkProperty(configService, IDP_LOGOUT_URL));
            Binding logoutResponseBinding = checkBinding(configService, LOGOUT_RESPONSE_BINDING);
            config.setLogoutResponseBinding(logoutResponseBinding);
            if (logoutResponseBinding == Binding.HTTP_POST) {
                config.setLogoutResponseTemplate(checkProperty(configService, LOGOUT_RESPONSE_POST_TEMPLATE));
            }
        }

        config.setEnableMetadataService(configService.getBoolProperty(ENABLE_METADATA_SERVICE, false));
        config.setAutoLoginEnabled(configService.getBoolProperty(ENABLE_AUTO_LOGIN, false));
        config.setAllowUnsolicitedResponses(configService.getBoolProperty(ALLOW_UNSOLICITED_RESPONSES, true));
        return config;
    }

    private static Binding checkBinding(ConfigurationService configService, String property) throws OXException {
        String bindingName = checkProperty(configService, property);
        if ("http-redirect".equals(bindingName)) {
            return Binding.HTTP_REDIRECT;
        } else if ("http-post".equals(bindingName)) {
            return Binding.HTTP_POST;
        }

        throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(property + " = " + bindingName);
    }

    private static String checkProperty(ConfigurationService configService, String name) throws OXException {
        String property = configService.getProperty(name);
        if (property == null) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(name);
        }

        return property;
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

    private void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    private void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    private void setAcsURL(String acsURL) {
        this.acsURL = acsURL;
    }

    private void setSingleLogoutServiceURL(String slsURL) {
        this.slsURL = slsURL;
    }

    private void setLogoutResponseBinding(Binding logoutResponseBinding) {
        this.logoutResponseBinding = logoutResponseBinding;
    }

    private void setIdpEntityID(String idpEntityID) {
        this.idpEntityID = idpEntityID;
    }

    private void setIdpURL(String idpURL) {
        this.idpAuthnURL = idpURL;
    }

    private void setIdentityProviderLogoutURL(String idpLogoutURL) {
        this.idpLogoutURL = idpLogoutURL;
    }

    private void setSupportSingleLogout(boolean supportSingleLogout) {
        this.supportSingleLogout = supportSingleLogout;
    }

    private void setEnableMetadataService(boolean enableMetadataService) {
        this.enableMetadataService = enableMetadataService;
    }

    private void setLogoutResponseTemplate(String logoutResponseTemplate) {
        this.logoutResponseTemplate = logoutResponseTemplate;
    }

    private void setAutoLoginEnabled(boolean autoLoginEnabled) {
        this.autoLoginEnabled = autoLoginEnabled;;
    }

    public boolean isAllowUnsolicitedResponses() {
        return allowUnsolicitedResponses;
    }

    public void setAllowUnsolicitedResponses(boolean allowUnsolicitedResponses) {
        this.allowUnsolicitedResponses = allowUnsolicitedResponses;
    }

}
