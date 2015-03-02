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
 * @since v7.6.0
 */
public class DefaultConfig implements SAMLConfig {

    private static final String PROP_PROTOCOL_BINDING = "com.openexchange.saml.sp.protocolBinding";

    private static final String PROP_ACS_URL = "com.openexchange.saml.sp.acsURL";

    private static final String PROP_ENTITY_ID = "com.openexchange.saml.sp.entityID";

    private static final String PROP_PROVIDER_NAME = "com.openexchange.saml.sp.providerName";

    private static final String PROP_IDP_URL = "com.openexchange.saml.sp.idpURL";

    private static final String PROP_SIGN_AUTHN_REQUEST = "com.openexchange.saml.sp.signAuthnRequest";

    private static final String PROP_WANT_ASSERTIONS_SIGNED = "com.openexchange.saml.sp.wantAssertionsSigned";

    private String providerName;

    private String entityID;

    private String acsURL;

    private Binding binding;

    private String idpURL;

    private boolean signAuthnRequest;

    private boolean wantAssertionsSigned;

    private DefaultConfig() {
        super();
    }

    public static DefaultConfig init(ConfigurationService configService) throws OXException {
        DefaultConfig config = new DefaultConfig();
        config.setProviderName(checkProperty(configService, PROP_PROVIDER_NAME));
        config.setEntityID(checkProperty(configService, PROP_ENTITY_ID));
        config.setAcsURL(checkProperty(configService, PROP_ACS_URL));
        config.setIdpURL(checkProperty(configService, PROP_IDP_URL));
        String bindingName = checkProperty(configService, PROP_PROTOCOL_BINDING);
        if ("http-redirect".equals(bindingName)) {
            config.setBinding(Binding.HTTP_REDIRECT);
        } else if ("http-post".equals(bindingName)) {
            config.setBinding(Binding.HTTP_POST);
        } else {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(PROP_PROTOCOL_BINDING + " = " + bindingName);
        }
        config.setSignAuthnRequest(configService.getBoolProperty(PROP_SIGN_AUTHN_REQUEST, false));
        config.setWantAssertionsSigned(configService.getBoolProperty(PROP_WANT_ASSERTIONS_SIGNED, false));

        return config;
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
    public Binding getProtocolBinding() {
        return binding;
    }

    @Override
    public String getIdentityProviderURL() {
        return idpURL;
    }

    @Override
    public boolean signAuthnRequest() {
        return signAuthnRequest;
    }

    @Override
    public boolean wantAssertionsSigned() {
        return wantAssertionsSigned;
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

    private void setBinding(Binding binding) {
        this.binding = binding;
    }

    private void setIdpURL(String idpURL) {
        this.idpURL = idpURL;
    }

    private void setSignAuthnRequest(boolean signAuthnRequest) {
        this.signAuthnRequest = signAuthnRequest;
    }

    public void setWantAssertionsSigned(boolean wantAssertionsSigned) {
        this.wantAssertionsSigned = wantAssertionsSigned;
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

    @Override
    public String getKeyStorePath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getKeyStorePassword() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSigningKeyAlias() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSigningKeyPassword() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getEncryptionKeyAlias() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getEncryptionKeyPassword() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIDPCertificateAlias() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIdentityProviderEntityID() {
        // TODO Auto-generated method stub
        return null;
    }

}
