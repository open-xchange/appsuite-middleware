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

package com.openexchange.saml;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.Response;
import com.openexchange.exception.OXException;
import com.openexchange.saml.spi.AbstractSAMLBackend;
import com.openexchange.saml.spi.AuthenticationInfo;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.spi.LogoutInfo;


/**
 * {@link TestSAMLBackend}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class TestSAMLBackend extends AbstractSAMLBackend {

    private final CredentialProvider credentialProvider;
    private SAMLConfig config;

    public TestSAMLBackend(CredentialProvider credentialProvider, SAMLConfig config) {
        this.credentialProvider = credentialProvider;
        this.config = config;
    }

    @Override
    protected CredentialProvider doGetCredentialProvider() {
        return credentialProvider;
    }

    @Override
    protected AuthenticationInfo doResolveAuthnResponse(Response response, Assertion assertion) throws OXException {
        String identifier = null;
        outer: for (AttributeStatement statement : assertion.getAttributeStatements()) {
            for (Attribute attribute : statement.getAttributes()) {
                if ("urn:open-xchange:saml:userID".equals(attribute.getName())) {
                    XSString stringValue = (XSString) attribute.getAttributeValues().get(0);
                    identifier = stringValue.getValue();
                    break outer;
                }
            }
        }
        AuthenticationInfo authInfo;
        if (null != identifier && "oxuser1".equals(identifier)) {
            authInfo = new AuthenticationInfo(1, 1);
        } else {
            authInfo = new AuthenticationInfo(-1, -1);
        }
        authInfo.setProperty("com.openexchange.saml.test.IsTest", Boolean.TRUE.toString());
        return authInfo;
    }

    @Override
    protected LogoutInfo doResolveLogoutRequest(LogoutRequest request) throws OXException {
        return new LogoutInfo();
    }

    @Override
    protected void doFinishLogout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

    }

    @Override
    public SAMLConfig getConfig() {
        return config;
    }
}
