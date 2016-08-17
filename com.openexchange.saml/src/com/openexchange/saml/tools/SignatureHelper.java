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

package com.openexchange.saml.tools;

import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.ws.transport.http.HTTPTransportUtils;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SigningUtil;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.SignableXMLObject;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import com.openexchange.saml.validation.ValidationError;
import com.openexchange.saml.validation.ValidationFailedReason;


/**
 * {@link SignatureHelper}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SignatureHelper {

    /**
     * Validates the signature of a {@link SignableXMLObject} with the passed {@link Credential}.
     *
     * @param object The signed object
     * @param credential The credential
     * @return A {@link ValidationError} if signature validation fails or <code>null</code> if the signature is valid
     */
    public static ValidationError validateSignature(SignableXMLObject object, Credential credential) {
        try {
            Signature signature = object.getSignature();
            SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
            profileValidator.validate(signature);
            SignatureValidator signatureValidator = new SignatureValidator(credential);
            signatureValidator.validate(signature);
        } catch (org.opensaml.xml.validation.ValidationException e) {
            return new ValidationError(ValidationFailedReason.INVALID_SIGNATURE, e.getMessage(), e);
        }

        return null;
    }

    /**
     * Validates the signature of a HTTP-Redirect request.
     *
     * @param httpRequest The HTTP request
     * @param credential The credential
     * @return A {@link ValidationError} if signature validation fails or <code>null</code> if the signature is valid
     */
    public static ValidationError validateURISignature(HttpServletRequest httpRequest, Credential credential) {
        String sigAlg = httpRequest.getParameter("SigAlg");
        if (sigAlg == null) {
            return new ValidationError(ValidationFailedReason.INVALID_REQUEST, "Parameter 'SigAlg' is not set");
        }

        String signature = httpRequest.getParameter("Signature");
        if (signature == null) {
            return new ValidationError(ValidationFailedReason.INVALID_REQUEST, "Parameter 'Signature' is not set");
        }

        String queryString = httpRequest.getQueryString();
        StringBuilder signedContent = new StringBuilder();
        if (httpRequest.getParameter("SAMLRequest") != null) {
            signedContent.append(HTTPTransportUtils.getRawQueryStringParameter(queryString, "SAMLRequest"));
        } else {
            signedContent.append(HTTPTransportUtils.getRawQueryStringParameter(queryString, "SAMLResponse"));
        }
        String rawRelayState = HTTPTransportUtils.getRawQueryStringParameter(queryString, "RelayState");
        if (rawRelayState != null) {
            signedContent.append('&').append(rawRelayState);
        }
        signedContent.append('&').append(HTTPTransportUtils.getRawQueryStringParameter(queryString, "SigAlg"));

        try {
            if (!SigningUtil.verifyWithURI(credential, sigAlg, Base64.decodeBase64(signature), signedContent.toString().getBytes("UTF-8"))) {
                return new ValidationError(ValidationFailedReason.INVALID_SIGNATURE, "The signature does not match the signed content");
            }
        } catch (UnsupportedEncodingException e) {
            return new ValidationError(ValidationFailedReason.INVALID_SIGNATURE, e.getMessage(), e);
        } catch (SecurityException e) {
            return new ValidationError(ValidationFailedReason.INVALID_SIGNATURE, e.getMessage(), e);
        }

        return null;
    }

}
