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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.saml;

import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * {@link DefaultConfig}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class DefaultConfig implements SAMLConfig {

    private static final String PROP_REQUEST_BINDING = "com.openexchange.saml.requestBinding";

    private static final String PROP_RESPONSE_BINDING = "com.openexchange.saml.responseBinding";

    private static final String PROP_ACS_URL = "com.openexchange.saml.acsURL";

    private static final String PROP_ENTITY_ID = "com.openexchange.saml.entityID";

    private static final String PROP_PROVIDER_NAME = "com.openexchange.saml.providerName";

    private static final String PROP_IDP_ENTITY_ID = "com.openexchange.saml.idpEntityID";

    private static final String PROP_IDP_URL = "com.openexchange.saml.idpURL";

    private String providerName;

    private String entityID;

    private String acsURL;

    private Binding requestBinding;

    private Binding responseBinding;

    private String idpURL;

    private String idpEntityID;


    private DefaultConfig() {
        super();
    }

    public static DefaultConfig init(ConfigurationService configService) throws OXException {
        DefaultConfig config = new DefaultConfig();
        config.setProviderName(checkProperty(configService, PROP_PROVIDER_NAME));
        config.setEntityID(checkProperty(configService, PROP_ENTITY_ID));
        config.setAcsURL(checkProperty(configService, PROP_ACS_URL));
        config.setIdpEntityID(checkProperty(configService, PROP_IDP_ENTITY_ID));
        config.setIdpURL(checkProperty(configService, PROP_IDP_URL));
        config.setResponseBinding(checkBinding(configService, PROP_RESPONSE_BINDING));
        config.setRequestBinding(checkBinding(configService, PROP_REQUEST_BINDING));

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
    public Binding getRequestBinding() {
        return requestBinding;
    }

    @Override
    public Binding getResponseBinding() {
        return responseBinding;
    }

    @Override
    public String getIdentityProviderEntityID() {
        return idpEntityID;
    }

    @Override
    public String getIdentityProviderURL() {
        return idpURL;
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

    private void setRequestBinding(Binding binding) {
        this.requestBinding = binding;
    }

    private void setResponseBinding(Binding binding) {
        this.responseBinding = binding;
    }

    private void setIdpEntityID(String idpEntityID) {
        this.idpEntityID = idpEntityID;
    }

    private void setIdpURL(String idpURL) {
        this.idpURL = idpURL;
    }

    @Override
    public boolean supportSingleLogout() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getSingleLogoutServiceURL() {
        // TODO Auto-generated method stub
        return null;
    }

}
