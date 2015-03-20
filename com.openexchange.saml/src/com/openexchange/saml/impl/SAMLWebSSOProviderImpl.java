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

package com.openexchange.saml.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.BaseID;
import org.opensaml.saml2.core.EncryptedID;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SigningUtil;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.saml.CryptoHelper;
import com.openexchange.saml.OpenSAML;
import com.openexchange.saml.SAMLConfig;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.SAMLExceptionCode;
import com.openexchange.saml.SessionProperties;
import com.openexchange.saml.WebSSOProvider;
import com.openexchange.saml.spi.AuthenticationInfo;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.spi.LogoutInfo;
import com.openexchange.saml.spi.SAMLBackend;
import com.openexchange.saml.spi.SAMLWebSSOCustomizer;
import com.openexchange.saml.spi.SAMLWebSSOCustomizer.RequestContext;
import com.openexchange.saml.state.AuthnRequestInfo;
import com.openexchange.saml.state.DefaultAuthnRequestInfo;
import com.openexchange.saml.state.DefaultLogoutRequestInfo;
import com.openexchange.saml.state.LogoutRequestInfo;
import com.openexchange.saml.state.StateManagement;
import com.openexchange.saml.validation.AuthnResponseValidationResult;
import com.openexchange.saml.validation.ValidationException;
import com.openexchange.saml.validation.ValidationStrategy;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.http.Tools;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SAMLWebSSOProviderImpl implements WebSSOProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SAMLWebSSOProviderImpl.class);

    /**
     * The number of milliseconds for which a LogoutRequest sent by us is considered valid.
     */
    private static final long LOGOUT_REQUEST_TIMEOUT = 5 * 60 * 1000l;

    private final SAMLConfig config;

    private final OpenSAML openSAML;

    private final SAMLBackend backend;

    private final StateManagement stateManagement;

    private final SessionReservationService sessionReservationService;

    private final ServiceLookup services;

    public SAMLWebSSOProviderImpl(SAMLConfig config, OpenSAML openSAML, StateManagement stateManagement, ServiceLookup services) {
        super();
        this.config = config;
        this.openSAML = openSAML;
        this.stateManagement = stateManagement;
        this.backend = services.getService(SAMLBackend.class);
        this.sessionReservationService = services.getService(SessionReservationService.class);
        this.services = services;
    }

    @Override
    public void respondWithAuthnRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        AuthnRequest authnRequest = customizeAuthnRequest(prepareAuthnRequest(), httpRequest, httpResponse);
        String relayState = getFrontendDomain(httpRequest);
        DefaultAuthnRequestInfo requestInfo = new DefaultAuthnRequestInfo();
        requestInfo.setRequestId(authnRequest.getID());
        requestInfo.setRelayState(relayState);
        stateManagement.addAuthnRequest(requestInfo, 5, TimeUnit.MINUTES);
        try {
            String authnRequestXML = openSAML.marshall(authnRequest);
            LOG.debug("Responding with AuthnRequest:\n{}", authnRequestXML);
            sendAuthnRequestRedirect(authnRequestXML, relayState, httpRequest, httpResponse);
        } catch (MarshallingException e) {
            throw SAMLExceptionCode.MARSHALLING_PROBLEM.create(e, e.getMessage());
        }
    }

    @Override
    public void handleAuthnResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Binding binding) throws OXException, IOException {
        if (binding != Binding.HTTP_POST) {
            /*
             * The HTTP Redirect binding MUST NOT be used, as the response will typically exceed the URL length permitted by most user
             * agents. [profiles 06 - 4.1.2p17] We don't support artifact binding yet. If you need it, feel free to implement it ;-)
             */
            throw SAMLExceptionCode.UNSUPPORTED_BINDING.create(binding.name());
        }

        String redirectHost = httpRequest.getParameter("RelayState");
        if (redirectHost == null) {
            throw SAMLExceptionCode.INVALID_REQUEST.create("The 'RelayState' parameter was not set");
        }

        Response response = extractAuthnResponse(httpRequest);
        try {
            ValidationStrategy validationStrategy = backend.getValidationStrategy(config, stateManagement);
            AuthnResponseValidationResult validationResult = validationStrategy.validateAuthnResponse(response, binding);
            AuthnRequestInfo requestInfo = validationResult.getRequestInfo();
            if (requestInfo != null) {
                stateManagement.removeAuthnRequestInfo(requestInfo.getRequestID());
            }

            Assertion bearerAssertion = validationResult.getBearerAssertion();
            AuthenticationInfo authInfo = backend.resolveAuthnResponse(response, bearerAssertion);
            LOG.debug("User {} in context {} is considered authenticated", authInfo.getUserId(), authInfo.getContextId());

            enhanceAuthInfo(authInfo, bearerAssertion);
            Map<String, String> properties = authInfo.getProperties();
            String sessionToken = sessionReservationService.reserveSessionFor(
                authInfo.getUserId(),
                authInfo.getContextId(),
                60l,
                TimeUnit.SECONDS,
                properties.isEmpty() ? null : properties);

            URI redirectLocation = new URIBuilder()
                .setScheme(getRedirectScheme(httpRequest))
                .setHost(redirectHost)
                .setPath(getRedirectPathPrefix() + "login")
                .setParameter("action", "redeemReservation")
                .setParameter("token", sessionToken)
                .build();

            Tools.disableCaching(httpResponse);
            httpResponse.sendRedirect(redirectLocation.toString());
        } catch (URISyntaxException e) {
            throw SAMLExceptionCode.INTERNAL_ERROR.create(e.getMessage());
        } catch (ValidationException e) {
            throw SAMLExceptionCode.VALIDATION_FAILED.create(e.getReason().getMessage(), e.getMessage());
        }
    }

    @Override
    public void respondWithLogoutRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Session session) throws OXException, IOException {
        LogoutRequest logoutRequest = customizeLogoutRequest(prepareLogoutRequest(session), httpRequest, httpResponse);
        String relayState = getFrontendDomain(httpRequest);
        DefaultLogoutRequestInfo requestInfo = new DefaultLogoutRequestInfo();
        requestInfo.setRequestId(logoutRequest.getID());
        requestInfo.setSessionId(session.getSessionID());
        requestInfo.setRelayState(relayState);
        stateManagement.addLogoutRequest(requestInfo, LOGOUT_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);

        try {
            String logoutRequestXML = openSAML.marshall(logoutRequest);
            LOG.debug("Responding with LogoutRequest:\n{}", logoutRequestXML);
            sendLogoutRequestRedirect(logoutRequestXML, relayState, httpRequest, httpResponse);
        } catch (MarshallingException e) {
            throw SAMLExceptionCode.MARSHALLING_PROBLEM.create(e, e.getMessage());
        }
    }

    @Override
    public void handleLogoutResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Binding binding) throws OXException, IOException {
        LogoutResponse response = extractLogoutResponse(httpRequest, binding);
        String redirectHost = httpRequest.getParameter("RelayState");
        if (redirectHost == null) {
            throw SAMLExceptionCode.INVALID_REQUEST.create("The 'RelayState' parameter was not set");
        }

        try {
            String inResponseTo = response.getInResponseTo();
            if (inResponseTo == null) {
                throw SAMLExceptionCode.INVALID_REQUEST.create("LogoutResponse contains no valid 'InResponseTo' attribute");
            }

            LogoutRequestInfo requestInfo = stateManagement.removeLogoutRequest(inResponseTo);
            if (requestInfo == null) {
                throw SAMLExceptionCode.INVALID_REQUEST.create("LogoutResponse contains no valid 'InResponseTo' attribute");
            }

            // TODO: add full validation

            String sessionId = requestInfo.getSessionId();
            URI redirectLocationBuilder = new URIBuilder()
                .setScheme(getRedirectScheme(httpRequest))
                .setHost(redirectHost)
                .setPath(getRedirectPathPrefix() + "login")
                .setParameter("action", "samlLogout")
                .setParameter("session", sessionId)
                .build();

            Tools.disableCaching(httpResponse);
            String redirectLocation = redirectLocationBuilder.toString();
            LOG.debug("LogoutResponse '{}' is considered valid. Redirecting to logout action: {}", response.getID(), redirectLocation);
            httpResponse.sendRedirect(redirectLocation);
        } catch (URISyntaxException e) {
            throw SAMLExceptionCode.INTERNAL_ERROR.create(e.getMessage());
        }
    }

    @Override
    public void handleLogoutRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Binding binding) throws IOException {
        Status status;
        LogoutRequest logoutRequest = null;
        try {
            logoutRequest = extractLogoutRequest(httpRequest, binding);
            ValidationStrategy validationStrategy = backend.getValidationStrategy(config, stateManagement);
            validationStrategy.validateLogoutRequest(logoutRequest, httpRequest, binding);
            LogoutInfo logoutInfo = backend.resolveLogoutRequest(logoutRequest);
            /*
             * TODO:
             * - look for session indexes and use them
             * - if none are contained, delete all sessions for the determined user
             */
            LOG.debug("LogoutRequest is considered valid, starting to terminate sessions based on {}", logoutInfo);
            terminateSessions(logoutRequest, logoutInfo, httpRequest, httpResponse);
            StatusCode statusCode = openSAML.buildSAMLObject(StatusCode.class);
            statusCode.setValue(StatusCode.SUCCESS_URI);
            status = openSAML.buildSAMLObject(Status.class);
            status.setStatusCode(statusCode);
        } catch (ValidationException e) {
            StatusCode statusCode = openSAML.buildSAMLObject(StatusCode.class);
            statusCode.setValue(StatusCode.REQUESTER_URI);
            StatusMessage statusMessage = openSAML.buildSAMLObject(StatusMessage.class);
            statusMessage.setMessage(e.getReason().getMessage() + " (" + e.getMessage() + ")");
            status = openSAML.buildSAMLObject(Status.class);
            status.setStatusCode(statusCode);
            status.setStatusMessage(statusMessage);
        } catch (OXException e) {
            StatusCode statusCode = openSAML.buildSAMLObject(StatusCode.class);
            statusCode.setValue(StatusCode.REQUESTER_URI);
            StatusMessage statusMessage = openSAML.buildSAMLObject(StatusMessage.class);
            statusMessage.setMessage(e.getMessage());
            status = openSAML.buildSAMLObject(Status.class);
            status.setStatusCode(statusCode);
            status.setStatusMessage(statusMessage);
        }

        Tools.disableCaching(httpResponse);
        try {
            LogoutResponse logoutResponse = customizeLogoutResponse(prepareLogoutResponse(status, logoutRequest == null ? null : logoutRequest.getID()), httpRequest, httpResponse);
            String responseXML = openSAML.marshall(logoutResponse);
            LOG.debug("Marshalled LogoutResponse: {}", responseXML);
            switch (config.getLogoutResponseBinding()) {
                case HTTP_REDIRECT:
                    sendLogoutResponseViaRedirect(responseXML, httpRequest, httpResponse);
                    break;

                case HTTP_POST:
                    // sendLogoutResponseViaPOST(responseXML, httpRequest, httpResponse); // TODO
                    break;
            }
        } catch (MarshallingException e) {
            LOG.error("Could not marshall LogoutResponse", e);
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (OXException e) {
            LOG.error("Could not compile LogoutResponse", e);
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String getMetadataXML() throws OXException {
        SPSSODescriptor spssoDescriptor = openSAML.buildSAMLObject(SPSSODescriptor.class);
        spssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);

        AssertionConsumerService acs = openSAML.buildSAMLObject(AssertionConsumerService.class);
        acs.setIndex(1);
        acs.setIsDefault(Boolean.TRUE);
        acs.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        acs.setLocation(config.getAssertionConsumerServiceURL());
        spssoDescriptor.getAssertionConsumerServices().add(acs);

        if (config.supportSingleLogout()) {
            SingleLogoutService slService = openSAML.buildSAMLObject(SingleLogoutService.class);
            slService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
            slService.setLocation(config.getSingleLogoutServiceURL());
            spssoDescriptor.getSingleLogoutServices().add(slService);
        }

        try {
            X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
            keyInfoGeneratorFactory.setEmitEntityCertificate(true);
            KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
            CredentialProvider credentialProvider = backend.getCredentialProvider();
            if (credentialProvider.hasSigningCredential()) {
                KeyDescriptor signKeyDescriptor = openSAML.buildSAMLObject(KeyDescriptor.class);
                signKeyDescriptor.setUse(UsageType.SIGNING);
                KeyInfo keyInfo = keyInfoGenerator.generate(credentialProvider.getSigningCredential());
                signKeyDescriptor.setKeyInfo(keyInfo);
                spssoDescriptor.getKeyDescriptors().add(signKeyDescriptor);
                spssoDescriptor.setAuthnRequestsSigned(Boolean.TRUE);
            } else {
                spssoDescriptor.setAuthnRequestsSigned(Boolean.FALSE);
            }

            if (credentialProvider.hasDecryptionCredential()) {
                KeyDescriptor encryptionKeyDescriptor = openSAML.buildSAMLObject(KeyDescriptor.class);
                encryptionKeyDescriptor.setUse(UsageType.ENCRYPTION);
                encryptionKeyDescriptor.setKeyInfo(keyInfoGenerator.generate(credentialProvider.getDecryptionCredential()));
                spssoDescriptor.getKeyDescriptors().add(encryptionKeyDescriptor);
                spssoDescriptor.setWantAssertionsSigned(Boolean.TRUE);
            } else {
                spssoDescriptor.setWantAssertionsSigned(Boolean.FALSE);
            }
        } catch (SecurityException e) {
            throw SAMLExceptionCode.CREDENTIAL_PROBLEM.create(e, e.getMessage());
        }

        spssoDescriptor = customizeDescriptor(spssoDescriptor);
        try {
            EntityDescriptor spDescriptor = openSAML.buildSAMLObject(EntityDescriptor.class);
            spDescriptor.setEntityID(config.getEntityID());
            spDescriptor.getRoleDescriptors().add(spssoDescriptor);
            return openSAML.marshall(spDescriptor);
        } catch (MarshallingException e) {
            throw SAMLExceptionCode.MARSHALLING_PROBLEM.create(e, e.getMessage());
        }
    }

    private void sendLogoutRequestRedirect(String logoutRequestXML, String relayState, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        String encoded = deflateAndEncode(logoutRequestXML);
        try {
            URIBuilder redirectLocationBuilder = new URIBuilder(config.getIdentityProviderLogoutURL()).setParameter("SAMLRequest", encoded).setParameter("RelayState", relayState);
            trySignRedirectHeader(redirectLocationBuilder);
            String redirectLocation = redirectLocationBuilder.build().toString();
            LOG.debug("Sending LogoutRequest via redirect: {}", redirectLocation);
            throw LoginExceptionCodes.REDIRECT.create(redirectLocation);
        } catch (URISyntaxException e) {
            throw SAMLExceptionCode.ENCODING_ERROR.create(e, "Could not construct redirect location");
        }
    }

    private LogoutRequest prepareLogoutRequest(Session session) throws OXException {
        Issuer issuer = openSAML.buildSAMLObject(Issuer.class);
        issuer.setValue(config.getEntityID());

        LogoutRequest logoutRequest = openSAML.buildSAMLObject(LogoutRequest.class);
        logoutRequest.setIssuer(issuer);
        logoutRequest.setDestination(config.getIdentityProviderLogoutURL());
        logoutRequest.setID(UUIDs.getUnformattedString(UUID.randomUUID()));
        logoutRequest.setIssueInstant(new DateTime());
        logoutRequest.setVersion(SAMLVersion.VERSION_20);
        logoutRequest.setReason(LogoutRequest.USER_REASON);
        logoutRequest.setNotOnOrAfter(new DateTime(System.currentTimeMillis() + LOGOUT_REQUEST_TIMEOUT));
        String sessionIndex = (String) session.getParameter(SessionProperties.SESSION_INDEX);
        if (sessionIndex != null) {
            SessionIndex indexElement = openSAML.buildSAMLObject(SessionIndex.class);
            indexElement.setSessionIndex(sessionIndex);
            logoutRequest.getSessionIndexes().add(indexElement);
        }

        String subjectID = (String) session.getParameter(SessionProperties.SUBJECT_ID);
        if (subjectID != null) {
            try {
                SAMLObject subjectIDElement = openSAML.unmarshall(SAMLObject.class, subjectID);
                /*
                 * TODO: implement subject ID encryption. Needs the CredentialProvider to be extended by an
                 * encryption credential first.
                 */
                if (subjectIDElement instanceof BaseID) {
                    logoutRequest.setBaseID((BaseID) subjectIDElement);
                } else if (subjectIDElement instanceof NameID) {
                    logoutRequest.setNameID((NameID) subjectIDElement);
                } else {
                    throw SAMLExceptionCode.UNMARSHALLING_ERROR.create("Not a valid BaseID or NameID: " + subjectID);
                }
            } catch (ClassCastException e) {
                throw SAMLExceptionCode.UNMARSHALLING_ERROR.create(subjectID);
            } catch (XMLParserException e) {
                throw SAMLExceptionCode.UNMARSHALLING_ERROR.create(subjectID);
            } catch (UnmarshallingException e) {
                throw SAMLExceptionCode.UNMARSHALLING_ERROR.create(subjectID);
            }
        }

        return logoutRequest;
    }

    private LogoutRequest customizeLogoutRequest(LogoutRequest logoutRequest, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        SAMLWebSSOCustomizer customizer = backend.getWebSSOCustomizer();
        if (customizer != null) {
            RequestContext requestContext = new RequestContext();
            requestContext.config = config;
            requestContext.openSAML = openSAML;
            requestContext.httpRequest = httpRequest;
            requestContext.httpResponse = httpResponse;
            return customizer.customizeLogoutRequest(logoutRequest, requestContext);
        }

        return logoutRequest;
    }

    private LogoutResponse prepareLogoutResponse(Status status, String inResponseTo) {
        Issuer issuer = openSAML.buildSAMLObject(Issuer.class);
        issuer.setValue(config.getEntityID());

        LogoutResponse logoutResponse = openSAML.buildSAMLObject(LogoutResponse.class);
        logoutResponse.setStatus(status);
        logoutResponse.setIssuer(issuer);
        logoutResponse.setDestination(config.getIdentityProviderLogoutURL());
        logoutResponse.setID(UUIDs.getUnformattedString(UUID.randomUUID()));
        logoutResponse.setIssueInstant(new DateTime());
        logoutResponse.setVersion(SAMLVersion.VERSION_20);
        if (inResponseTo != null) {
            logoutResponse.setInResponseTo(inResponseTo);
        }
        return logoutResponse;
    }

    private LogoutResponse customizeLogoutResponse(LogoutResponse logoutResponse, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        SAMLWebSSOCustomizer customizer = backend.getWebSSOCustomizer();
        if (customizer != null) {
            RequestContext requestContext = new RequestContext();
            requestContext.config = config;
            requestContext.openSAML = openSAML;
            requestContext.httpRequest = httpRequest;
            requestContext.httpResponse = httpResponse;
            return customizer.customizeLogoutResponse(logoutResponse, requestContext);
        }

        return logoutResponse;
    }

    private void sendLogoutResponseViaRedirect(String responseXML, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        try {
            String encoded = deflateAndEncode(responseXML);
            URIBuilder redirectLocationBuilder = new URIBuilder(config.getIdentityProviderLogoutURL()).setParameter("SAMLResponse", encoded);
            String relayState = httpRequest.getParameter("RelayState");
            if (relayState != null) {
                redirectLocationBuilder.setParameter("RelayState", relayState);
            }
            trySignRedirectHeader(redirectLocationBuilder);
            String redirectLocation = redirectLocationBuilder.build().toString();
            LOG.debug("Sending LogoutResponse via redirect: {}", redirectLocation);
            httpResponse.sendRedirect(redirectLocation);
        } catch (URISyntaxException e) {
            LOG.error("Could not send LogoutResponse via redirect", e);
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (OXException e) {
            LOG.error("Could not send LogoutResponse via redirect", e);
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void terminateSessions(LogoutRequest logoutRequest, LogoutInfo logoutInfo, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        SessiondService sessiondService = services.getService(SessiondService.class);
        int contextId = logoutInfo.getContextId();
        int userId = logoutInfo.getUserId();
        if (logoutInfo.isTerminateAll()) {
            if (contextId > 0 && userId > 0) {
                sessiondService.removeUserSessionsGlobal(userId, contextId);
                LOG.debug("Removed sessions globally for user {} in context {}", userId, contextId);
            } else {
                LOG.warn("LogoutInfo says terminateAll=true but no valid user and context were set");
            }
        }

        if (logoutInfo.isHeedSessionIndexes()) {
            List<SessionIndex> sessionIndexes = logoutRequest.getSessionIndexes();
            if (sessionIndexes.size() > 0) {
                List<String> keys = new ArrayList<String>(sessionIndexes.size());
                for (SessionIndex sessionIndex : sessionIndexes) {
                    keys.add(sessionIndex.getSessionIndex());
                }

                List<String> sessionIds = stateManagement.removeSessionIds(keys);
                LOG.debug("Found {} session IDs for {} indexes", sessionIds.size(), keys.size());

                for (String sessionId : sessionIds) {
                    if (sessiondService.removeSession(sessionId)) {
                        LOG.debug("Session {} was terminated", sessionId);
                    } else {
                        LOG.debug("Session {} did not exist anymore", sessionId);
                    }
                }
            } else {
                LOG.debug("No session indexes contained in LogoutRequest");
            }
        }

        if (logoutInfo.isRemoveSessionCookies()) {
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies == null) {
                LOG.debug("No cookies will be removed, as none are contained in the HTTP request");
            } else {
                /*
                 * TODO: brute-force approach, maybe we should match 'open-xchange-' prefixes here and provide an extension point to remove custom cookies.
                 * Unfortunately we have no reliable way to get the original cookie hash here...
                 */
                LOG.debug("{} cookies will be removed", cookies.length);
                for (Cookie cookie : cookies) {
                    Cookie toDelete = (Cookie) cookie.clone();
                    toDelete.setMaxAge(0);
                    httpResponse.addCookie(toDelete);
                }
            }
        }
    }

    private String getRedirectPathPrefix() {
        DispatcherPrefixService prefixService = services.getService(DispatcherPrefixService.class);
        return prefixService.getPrefix();
    }

    private String getRedirectScheme(HttpServletRequest httpRequest) {
        ConfigurationService configService = services.getService(ConfigurationService.class);
        boolean secure = Tools.considerSecure(
            httpRequest,
            Boolean.parseBoolean(configService.getProperty(Property.FORCE_HTTPS.getPropertyName(), Property.FORCE_HTTPS.getDefaultValue())));

        return secure ? "https" : "http";
    }

    private void enhanceAuthInfo(AuthenticationInfo authInfo, Assertion bearerAssertion) {
        Map<String, String> properties = authInfo.getProperties();
        String sessionIndex = extractSessionIndex(bearerAssertion);
        if (sessionIndex != null) {
            /*
             * The SAML specification states that the bearer assertion must contain a <code>SessionIndex</code> attribute in its
             * <code>AuthnStatement</code> element:
             * If the identity provider supports the Single Logout profile, defined in Section 4.4, any authentication statements
             * MUST include a SessionIndex attribute to enable per-session logout requests by the service provider.
             * [profiles 06 - 4.1.4.2p20]
             *
             * We need this for subsequent logout requests. However if the IDP does not comply to the specification,
             * the attribute might be missing.
             */
            properties.put(SessionProperties.SESSION_INDEX, sessionIndex);
        }

        String sessionNotOnOrAfter = extractSessionNotOnOrAfter(bearerAssertion);
        if (sessionNotOnOrAfter != null) {
            /*
             * If an <AuthnStatement> used to establish a security context for the principal contains a SessionNotOnOrAfter attribute,
             * the security context SHOULD be discarded once this time is reached, unless the service provider reestablishes the principal's
             * identity by repeating the use of this profile. Note that if multiple <AuthnStatement> elements are present, the
             * SessionNotOnOrAfter value closest to the present time SHOULD be honored.
             * [profiles 06 - 4.1.4.3p21]
             *
             * The SAMLSessionInspector takes care of that parameter and logs out expired sessions if necessary.
             */
            properties.put(SessionProperties.SESSION_NOT_ON_OR_AFTER, sessionNotOnOrAfter);
        }

        /*
         * The bearer assertions subject must contain a <code>NameID</code>, <code>BaseID</code> or <code>EncryptedID</code>
         * element. We need to remember the elements XML representation, as it will be later on used to compile logout requests.
         * However if the IDP does not comply to the specification, the attribute might be missing.
         */
        String subjectID = extractSubjectID(bearerAssertion);
        if (subjectID != null) {
            properties.put(SessionProperties.SUBJECT_ID, subjectID);
        }
    }

    private void sendAuthnRequestRedirect(String authnRequestXML, String relayState, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        String encoded = deflateAndEncode(authnRequestXML);
        try {
            URIBuilder redirectLocationBuilder = new URIBuilder(config.getIdentityProviderAuthnURL()).setParameter("SAMLRequest", encoded).setParameter("RelayState", relayState);
            trySignRedirectHeader(redirectLocationBuilder);
            String redirectLocation = redirectLocationBuilder.build().toString();
            LOG.debug("Sending AuthnRequest via redirect: {}", redirectLocation );
            throw LoginExceptionCodes.REDIRECT.create(redirectLocation);
        } catch (URISyntaxException e) {
            throw SAMLExceptionCode.ENCODING_ERROR.create(e, "Could not construct redirect location");
        }
    }

    private static String deflateAndEncode(String xml) throws OXException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFLATED, true);
        DeflaterOutputStream deflaterStream = new DeflaterOutputStream(bytesOut, deflater);
        try {
            deflaterStream.write(xml.getBytes(Charsets.UTF_8));
            deflaterStream.finish();
            return Base64.encodeBase64String(bytesOut.toByteArray());
        } catch (IOException e) {
            throw SAMLExceptionCode.ENCODING_ERROR.create(e, "Could not deflate XML");
        }
    }

    private void trySignRedirectHeader(URIBuilder redirectLocationBuilder) throws OXException {
        CredentialProvider credentialProvider = backend.getCredentialProvider();
        if (credentialProvider.hasSigningCredential()) {
            Credential signingCredential = credentialProvider.getSigningCredential();
            String sigAlg = openSAML.getGlobalSecurityConfiguration().getSignatureAlgorithmURI(signingCredential.getPrivateKey().getAlgorithm());
            redirectLocationBuilder.setParameter("SigAlg", sigAlg);
            String rawQuery = null;
            try {
                rawQuery = redirectLocationBuilder.build().getRawQuery();
                byte[] rawSignature = SigningUtil.signWithURI(signingCredential, sigAlg, rawQuery.getBytes(Charsets.UTF_8));
                String signature = Base64.encodeBase64String(rawSignature);
                redirectLocationBuilder.setParameter("Signature", signature);
                LOG.debug("Redirect header was signed with algorithm {}", sigAlg);
            } catch (URISyntaxException e) {
                throw SAMLExceptionCode.ENCODING_ERROR.create(e, "Could not construct redirect location");
            } catch (SecurityException e) {
                throw SAMLExceptionCode.ENCODING_ERROR.create(e, "Could not compute URI signature for location " + rawQuery);
            }
        }
    }

    private String getFrontendDomain(HttpServletRequest httpRequest) {
        HostnameService hostnameService = services.getOptionalService(HostnameService.class);
        if (hostnameService == null) {
            return httpRequest.getServerName();
        }

        String hostname = hostnameService.getHostname(-1, -1);
        if (hostname == null) {
            return httpRequest.getServerName();
        }

        return hostname;
    }

    private AuthnRequest customizeAuthnRequest(AuthnRequest authnRequest, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        SAMLWebSSOCustomizer customizer = backend.getWebSSOCustomizer();
        if (customizer != null) {
            RequestContext requestContext = new SAMLWebSSOCustomizer.RequestContext();
            requestContext.config = config;
            requestContext.openSAML = openSAML;
            requestContext.httpRequest = httpRequest;
            requestContext.httpResponse = httpResponse;
            return customizer.customizeAuthnRequest(authnRequest, requestContext);
        }
        return authnRequest;
    }

    private AuthnRequest prepareAuthnRequest() {
        AuthnRequest authnRequest = openSAML.buildSAMLObject(AuthnRequest.class);

        /*
         * The <Issuer> element MUST be present and MUST contain the unique identifier of the requesting service provider; the Format
         * attribute MUST be omitted or have a value of urn:oasis:names:tc:SAML:2.0:nameid-format:entity. [profiles 06 - 4.1.4.1p19]
         */
        Issuer issuer = openSAML.buildSAMLObject(Issuer.class);
        issuer.setFormat(NameIDType.ENTITY);
        issuer.setValue(config.getEntityID());
        authnRequest.setIssuer(issuer);

        authnRequest.setVersion(SAMLVersion.VERSION_20);
        String providerName = config.getProviderName();
        if (!Strings.isEmpty(providerName)) {
            authnRequest.setProviderName(providerName);
        }
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        authnRequest.setAssertionConsumerServiceURL(config.getAssertionConsumerServiceURL());
        authnRequest.setDestination(config.getIdentityProviderAuthnURL());
        authnRequest.setIsPassive(Boolean.FALSE);
        authnRequest.setForceAuthn(Boolean.FALSE);
        authnRequest.setID(UUIDs.getUnformattedString(UUID.randomUUID()));
        authnRequest.setIssueInstant(new DateTime());
        return authnRequest;
    }

    private SPSSODescriptor customizeDescriptor(SPSSODescriptor spssoDescriptor) throws OXException {
        SAMLWebSSOCustomizer customizer = backend.getWebSSOCustomizer();
        if (customizer != null) {
            return customizer.customizeSPSSODescriptor(spssoDescriptor);
        }

        return spssoDescriptor;
    }

    private LogoutResponse extractLogoutResponse(HttpServletRequest httpRequest, Binding binding) throws OXException {
        Document responseDoc;
        try {
            String responseXML = customDecodeLogoutResponse(httpRequest, binding);
            if (responseXML == null) {
                responseDoc = decodeLogoutResponse(httpRequest, binding);
            } else {
                responseDoc = openSAML.getParserPool().parse(new StringReader(responseXML));
            }
        } catch (XMLParserException e) {
            throw SAMLExceptionCode.DECODING_ERROR.create(e, e.getMessage());
        }

        try {
            Element documentElement = responseDoc.getDocumentElement();
            XMLObject unmarshalled = openSAML.getUnmarshallerFactory().getUnmarshaller(documentElement).unmarshall(documentElement);
            if (!(unmarshalled instanceof LogoutResponse)) {
                throw SAMLExceptionCode.UNMARSHALLING_ERROR.create("XML was not a valid LogoutResponse element");
            }

            final LogoutResponse logoutResponse = (LogoutResponse) unmarshalled;
            LOG.debug("Received SAMLResponse:\n{}", new Object() {
                @Override
                public String toString() {
                    return XMLHelper.prettyPrintXML(logoutResponse.getDOM());
                }
            });
            return logoutResponse;
        } catch (UnmarshallingException e) {
            throw SAMLExceptionCode.UNMARSHALLING_ERROR.create(e, e.getMessage());
        }
    }

    private Document decodeLogoutResponse(HttpServletRequest httpRequest, Binding binding) throws OXException {
        String b64Request = httpRequest.getParameter("SAMLResponse");
        if (b64Request == null) {
            throw SAMLExceptionCode.DECODING_ERROR.create("Request parameter 'SAMLResponse' is missing");
        }

        byte[] requestBytes = Base64.decodeBase64(b64Request);
        try {
            if (binding == Binding.HTTP_REDIRECT) {
                // bytes are deflated in redirect binding
                return openSAML.getParserPool().parse(new InflaterInputStream(new ByteArrayInputStream(requestBytes)));
            } else {
                return openSAML.getParserPool().parse(new ByteArrayInputStream(requestBytes));
            }
        } catch (XMLParserException e) {
            throw SAMLExceptionCode.DECODING_ERROR.create(e, e.getMessage());
        }
    }

    private LogoutRequest extractLogoutRequest(HttpServletRequest httpRequest, Binding binding) throws OXException {
        Document requestDoc;
        try {
            String requestXML = customDecodeLogoutRequest(httpRequest, binding);
            if (requestXML == null) {
                requestDoc = decodeLogoutRequest(httpRequest, binding);
            } else {
                requestDoc = openSAML.getParserPool().parse(new StringReader(requestXML));
            }
        } catch (XMLParserException e) {
            throw SAMLExceptionCode.DECODING_ERROR.create(e, e.getMessage());
        }

        try {
            Element documentElement = requestDoc.getDocumentElement();
            XMLObject unmarshalled = openSAML.getUnmarshallerFactory().getUnmarshaller(documentElement).unmarshall(documentElement);
            if (!(unmarshalled instanceof LogoutRequest)) {
                throw SAMLExceptionCode.UNMARSHALLING_ERROR.create("XML was not a valid LogoutRequest element");
            }

            final LogoutRequest logoutRequest = (LogoutRequest) unmarshalled;
            LOG.debug("Received SAMLRequest:\n{}", new Object() {
                @Override
                public String toString() {
                    return XMLHelper.prettyPrintXML(logoutRequest.getDOM());
                }
            });
            return logoutRequest;
        } catch (UnmarshallingException e) {
            throw SAMLExceptionCode.UNMARSHALLING_ERROR.create(e, e.getMessage());
        }
    }

    private Document decodeLogoutRequest(HttpServletRequest httpRequest, Binding binding) throws OXException {
        String b64Request = httpRequest.getParameter("SAMLRequest");
        if (b64Request == null) {
            throw SAMLExceptionCode.DECODING_ERROR.create("Request parameter 'SAMLRequest' is missing");
        }

        byte[] requestBytes = Base64.decodeBase64(b64Request);
        try {
            if (binding == Binding.HTTP_REDIRECT) {
                // bytes are deflated in redirect binding
                return openSAML.getParserPool().parse(new InflaterInputStream(new ByteArrayInputStream(requestBytes)));
            } else {
                return openSAML.getParserPool().parse(new ByteArrayInputStream(requestBytes));
            }
        } catch (XMLParserException e) {
            throw SAMLExceptionCode.DECODING_ERROR.create(e, e.getMessage());
        }
    }

    private String customDecodeLogoutRequest(HttpServletRequest httpRequest, Binding binding) throws OXException {
        SAMLWebSSOCustomizer customizer = backend.getWebSSOCustomizer();
        if (customizer != null) {
            return customizer.decodeLogoutRequest(httpRequest, binding);
        }

        return null;
    }

    private String customDecodeLogoutResponse(HttpServletRequest httpRequest, Binding binding) throws OXException {
        SAMLWebSSOCustomizer customizer = backend.getWebSSOCustomizer();
        if (customizer != null) {
            return customizer.decodeLogoutResponse(httpRequest, binding);
        }

        return null;
    }

    private Response extractAuthnResponse(HttpServletRequest httpRequest) throws OXException {
        String responseXML = customDecodeAuthnResponse(httpRequest);
        if (responseXML == null) {
            String b64Response = httpRequest.getParameter("SAMLResponse");
            if (b64Response == null) {
                throw SAMLExceptionCode.INVALID_REQUEST.create("The 'SAMLResponse' parameter was not set");
            }

            responseXML = new String(Base64.decodeBase64(b64Response));
        }

        try {
            Element responseElement = openSAML.getParserPool().parse(new ByteArrayInputStream(responseXML.getBytes())).getDocumentElement();
            XMLObject unmarshalledResponse = openSAML.getUnmarshallerFactory().getUnmarshaller(responseElement).unmarshall(responseElement);
            if (!(unmarshalledResponse instanceof Response)) {
                throw SAMLExceptionCode.UNMARSHALLING_ERROR.create("XML was not a valid Response element");
            }

            final Response response = (Response) unmarshalledResponse;
            LOG.debug("Received SAMLResponse:\n{}", new Object() {
                @Override
                public String toString() {
                    return XMLHelper.prettyPrintXML(response.getDOM());
                }
            });
            return response;
        } catch (XMLParserException e) {
            throw SAMLExceptionCode.DECODING_ERROR.create(e, e.getMessage());
        } catch (UnmarshallingException e) {
            throw SAMLExceptionCode.UNMARSHALLING_ERROR.create(e, e.getMessage());
        }
    }

    private String customDecodeAuthnResponse(HttpServletRequest httpRequest) throws OXException {
        SAMLWebSSOCustomizer customizer = backend.getWebSSOCustomizer();
        if (customizer != null) {
            return customizer.decodeAuthnResponse(httpRequest);
        }

        return null;
    }

    private static String extractSessionIndex(Assertion assertion) {
        List<AuthnStatement> authnStatements = assertion.getAuthnStatements();
        for (AuthnStatement statement : authnStatements) {
            String sessionIndex = statement.getSessionIndex();
            if (sessionIndex != null) {
                return sessionIndex;
            }
        }

        return null;
    }

    private static String extractSessionNotOnOrAfter(Assertion assertion) {
        List<AuthnStatement> authnStatements = assertion.getAuthnStatements();
        for (AuthnStatement statement : authnStatements) {
            DateTime sessionNotOnOrAfter = statement.getSessionNotOnOrAfter();
            if (sessionNotOnOrAfter != null) {
                return Long.toString(sessionNotOnOrAfter.getMillis());
            }
        }

        return null;
    }

    private String extractSubjectID(Assertion assertion) {
        Subject subject = assertion.getSubject();
        if (subject == null) {
            return null;
        }

        BaseID baseID = subject.getBaseID();
        if (baseID != null) {
            try {
                return openSAML.marshall(baseID);
            } catch (MarshallingException e) {
                LOG.warn("Could not marshall BaseID of assertions '{}' subject. Single logout for this session will probably fail.", assertion.getID(), e);
            }
        }

        NameID nameID = subject.getNameID();
        if (nameID != null) {
            try {
                return openSAML.marshall(nameID);
            } catch (MarshallingException e) {
                LOG.warn("Could not marshall NameID of assertions '{}' subject. Single logout for this session will probably fail.", assertion.getID(), e);
            }
        }

        EncryptedID encryptedID = subject.getEncryptedID();
        if (encryptedID != null) {
            CredentialProvider credentialProvider = backend.getCredentialProvider();
            if (credentialProvider.hasDecryptionCredential()) {
                Decrypter decrypter = CryptoHelper.getDecrypter(credentialProvider);
                try {
                    SAMLObject decrypted = decrypter.decrypt(encryptedID);
                    return openSAML.marshall(decrypted);
                } catch (DecryptionException e) {
                    LOG.warn("Could not decrypt EncryptedID of assertions '{}' subject. Single logout for this session will probably fail.", assertion.getID(), e);
                } catch (MarshallingException e) {
                    LOG.warn("Could not marshall decrypted EncryptedID of assertions '{}' subject. Single logout for this session will probably fail.", assertion.getID(), e);
                }
            } else {
                LOG.warn("Could not decrypt EncryptedID of assertions '{}' subject as no credential is available. Single logout for this session will probably fail.", assertion.getID());
            }
        }

        return null;
    }

    private static String getBindingURI(Binding binding) {
        switch (binding) {
        case HTTP_POST:
            return SAMLConstants.SAML2_POST_BINDING_URI;
        case HTTP_REDIRECT:
            return SAMLConstants.SAML2_REDIRECT_BINDING_URI;
        default:
            return null;
        }
    }

}
