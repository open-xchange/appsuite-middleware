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

package com.openexchange.saml.ucs.impl;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSAnyImpl;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.ucs.common.UCSLookup;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.saml.SAMLConfig;
import com.openexchange.saml.spi.AbstractSAMLBackend;
import com.openexchange.saml.spi.AuthenticationInfo;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.spi.ExceptionHandler;
import com.openexchange.saml.spi.LogoutInfo;
import com.openexchange.saml.state.StateManagement;
import com.openexchange.saml.ucs.config.UCSSamlConfiguration;
import com.openexchange.saml.ucs.config.UCSSamlProperty;
import com.openexchange.saml.validation.StrictValidationStrategy;
import com.openexchange.saml.validation.ValidationStrategy;
import com.openexchange.user.UserService;

/**
 *
 * Authentication Plugin for the UCS Server Product.
 * This Class implements the needed Authentication against an UCS LDAP Server:
 * 1. User enters following information on Loginscreen: username and password (NO CONTEXT, will be resolved by the LDAP Attribute)
 * 1a. Search for given "username" (NOT with context) given by OX Loginmask with configured pattern and with configured LDAP BASE.
 * 2. If user is found, bind to LDAP Server with the found DN
 * 3. If BIND successfull, fetch the configured "context" Attribute and parse out the context name.
 * 4. Return context name and username to OX API!
 * 5. User is logged in!
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @since v7.10.1
 *
 */
public class UCSSamlBackend extends AbstractSAMLBackend {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UCSSamlBackend.class);
    private final UCSLookup                     ucsLookup;
    private final LeanConfigurationService      leanConfig;
    private final UCSSamlConfiguration uCSSamlConfiguration;
    private final UserService userService;
    private final ContextService contextService;

    /**
     * Default constructor.
     * 
     * @param leanConfig
     * @param uCSSamlConfiguration 
     *
     * @param configService The service to use
     * @throws OXException If initialization fails
     */
    public UCSSamlBackend(UCSLookup ucsLookup, LeanConfigurationService leanConfig, UserService userService, ContextService contextService, UCSSamlConfiguration uCSSamlConfiguration) throws OXException {
        super();
        this.ucsLookup = ucsLookup;
        this.leanConfig = leanConfig;
        this.userService = userService;
        this.contextService = contextService;
        this.uCSSamlConfiguration = uCSSamlConfiguration;
    }

    private UCSSamlExceptionHandler getInternalExceptionHandler() {
        String failureRedirectUrl = leanConfig.getProperty(UCSSamlProperty.failureRedirect);
        String failureLogoutRedirectUrl = leanConfig.getProperty(UCSSamlProperty.logoutFailureRedirect);
        failureLogoutRedirectUrl = Strings.isEmpty(failureLogoutRedirectUrl) ? failureRedirectUrl : failureLogoutRedirectUrl;
        UCSSamlExceptionHandler exceptionHandler = Strings.isEmpty(failureRedirectUrl) ? null : new UCSSamlExceptionHandler(failureRedirectUrl, failureLogoutRedirectUrl);
        return exceptionHandler;
    }

    @Override
    public ValidationStrategy getValidationStrategy(SAMLConfig config, StateManagement stateManagement) {
        return new StrictValidationStrategy(config, uCSSamlConfiguration.getCredentialProvider(), stateManagement);
    }

    @Override
    protected ExceptionHandler doGetExceptionHandler() {
        UCSSamlExceptionHandler exceptionHandler = getInternalExceptionHandler();
        return exceptionHandler == null ? super.doGetExceptionHandler() : exceptionHandler;
    }

    @Override
    protected CredentialProvider doGetCredentialProvider() {
        return uCSSamlConfiguration.getCredentialProvider();
    }

    @Override
    protected AuthenticationInfo doResolveAuthnResponse(Response response, Assertion assertion) throws OXException {
        String samlId = leanConfig.getProperty(UCSSamlProperty.id);
        String identifier = null;
        outer: for (AttributeStatement statement : assertion.getAttributeStatements()) {
            for (org.opensaml.saml2.core.Attribute attribute : statement.getAttributes()) {
                if (samlId.equals(attribute.getName())) {
                    identifier = getAttributeValue(attribute.getAttributeValues().get(0));
                    break outer;
                }
            }
        }
        if (identifier == null) {
            LOG.error("failed to find an SAML attribute with name \"{}\"", samlId);
            throw new OXException();
        }
        Authenticated userlookup = ucsLookup.handleLoginInfo(identifier);

        int contextId = contextService.getContextId(userlookup.getContextInfo());
        if (contextId < 0) {
            throw LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_CONTEXT_MAPPING.create(userlookup.getContextInfo());
        }
        int userId = userService.getUserId(userlookup.getUserInfo(), contextService.getContext(contextId));

        return new AuthenticationInfo(contextId, userId);
    }

    private String getAttributeValue(XMLObject attributeValue) {
        if (attributeValue == null) {
            return null;
        } else if (attributeValue instanceof XSString) {
            return getStringAttributeValue((XSString) attributeValue);
        } else if (attributeValue instanceof XSAnyImpl) {
            return getAnyAttributeValue((XSAnyImpl) attributeValue);
        } else {
            return attributeValue.toString();
        }
    }

    private String getStringAttributeValue(XSString attributeValue) {
        return attributeValue.getValue();
    }

    private String getAnyAttributeValue(XSAnyImpl attributeValue) {
        return attributeValue.getTextContent();
    }

    @Override
    protected LogoutInfo doResolveLogoutRequest(LogoutRequest request) throws OXException {
        LogoutInfo logoutInfo = new LogoutInfo();
        return logoutInfo;
    }

    @Override
    protected void doFinishLogout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        httpResponse.sendRedirect(leanConfig.getProperty(UCSSamlProperty.logoutRedirectUrl));
    }

}
