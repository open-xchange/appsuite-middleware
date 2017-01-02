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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
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
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Charsets;
import com.openexchange.java.util.UUIDs;
import com.openexchange.saml.OpenSAML;
import com.openexchange.saml.SAMLConfig;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.SAMLExceptionCode;
import com.openexchange.saml.SAMLSessionParameters;
import com.openexchange.saml.SAMLWebSSOProvider;
import com.openexchange.saml.spi.AuthenticationInfo;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.spi.LogoutInfo;
import com.openexchange.saml.spi.SAMLBackend;
import com.openexchange.saml.spi.WebSSOCustomizer;
import com.openexchange.saml.spi.WebSSOCustomizer.RequestContext;
import com.openexchange.saml.state.AuthnRequestInfo;
import com.openexchange.saml.state.DefaultAuthnRequestInfo;
import com.openexchange.saml.state.DefaultLogoutRequestInfo;
import com.openexchange.saml.state.LogoutRequestInfo;
import com.openexchange.saml.state.StateManagement;
import com.openexchange.saml.tools.CryptoHelper;
import com.openexchange.saml.tools.SAMLLoginTools;
import com.openexchange.saml.validation.AuthnResponseValidationResult;
import com.openexchange.saml.validation.ValidationException;
import com.openexchange.saml.validation.ValidationStrategy;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.servlet.http.Tools;

/**
 * Provides the functionality for supported SAML profiles.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class WebSSOProviderImpl implements SAMLWebSSOProvider {

    private static final Logger LOG = LoggerFactory.getLogger(WebSSOProviderImpl.class);

    /**
     * The number of milliseconds for which a LogoutRequest sent by us is considered valid (5 minutes).
     */
    private static final long LOGOUT_REQUEST_TIMEOUT = 5 * 60 * 1000l;

    /**
     * The number of milliseconds for which an AuthnRequestInfo is remembered (5 minutes).
     */
    private static final long AUTHN_REQUEST_TIMEOUT = 5 * 60 * 1000l;

    /**
     * The number of milliseconds for which an authentication response ID is remembered (2 hours).
     */
    private static final long AUTHN_RESPONSE_TIMEOUT = 120 * 60 *1000l;

    private final SAMLConfig config;

    private final OpenSAML openSAML;

    private final SAMLBackend backend;

    private final StateManagement stateManagement;

    private final SessionReservationService sessionReservationService;

    private final ServiceLookup services;

    public WebSSOProviderImpl(SAMLConfig config, OpenSAML openSAML, StateManagement stateManagement, ServiceLookup services) {
        super();
        this.config = config;
        this.openSAML = openSAML;
        this.stateManagement = stateManagement;
        this.backend = services.getService(SAMLBackend.class);
        this.sessionReservationService = services.getService(SessionReservationService.class);
        this.services = services;
    }

    @Override
    public String buildAuthnRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        final AuthnRequest authnRequest = customizeAuthnRequest(prepareAuthnRequest(), httpRequest, httpResponse);
        String domainName = getDomainName(httpRequest);
        DefaultAuthnRequestInfo requestInfo = new DefaultAuthnRequestInfo();
        requestInfo.setRequestId(authnRequest.getID());
        requestInfo.setDomainName(domainName);
        requestInfo.setLoginPath(httpRequest.getParameter("loginPath"));
        requestInfo.setClientID(httpRequest.getParameter("client"));

        String relayState = stateManagement.addAuthnRequestInfo(requestInfo, AUTHN_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        try {
            String authnRequestXML = openSAML.marshall(authnRequest);
            LOG.debug("Prepared AuthnRequest:\n{}", new Object() {
                @Override
                public String toString() {
                    return XMLHelper.prettyPrintXML(authnRequest.getDOM());
                }
            });
            return compileAuthnRequestRedirectURI(authnRequestXML, relayState);
        } catch (MarshallingException e) {
            throw SAMLExceptionCode.MARSHALLING_PROBLEM.create(e, e.getMessage());
        }
    }

    @Override
    public void handleAuthnResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Binding binding) throws OXException, IOException {
        if (binding != Binding.HTTP_POST) {
            /*
             * The HTTP Redirect binding MUST NOT be used, as the response will typically exceed the URL length permitted by most user
             * agents.
             * [profiles 06 - 4.1.2p17]
             *
             * We don't support artifact binding yet. If you need it, feel free to implement it ;-)
             */
            throw SAMLExceptionCode.UNSUPPORTED_BINDING.create(binding.name());
        }

        Response response = extractAuthnResponse(httpRequest);

        AuthnRequestInfo requestInfo;
        if (response.getInResponseTo() == null) {
            /*
             * An unsolicited <Response> MUST NOT contain an InResponseTo attribute, nor should any bearer
             * <SubjectConfirmationData> elements contain one. If metadata as specified in [SAMLMeta] is used,
             * the <Response> or artifact SHOULD be delivered to the <md:AssertionConsumerService> endpoint
             * of the service provider designated as the default.
             * [profiles 06 - 4.1.5 p20]
             */
            if (config.isAllowUnsolicitedResponses()) {
                String relayState = httpRequest.getParameter("RelayState");
                if (relayState == null) {
                    // use DefaultAuthnRequestInfo if no RelayState is set
                    String domainName = getDomainName(httpRequest);
                    requestInfo = new DefaultAuthnRequestInfo();
                    ((DefaultAuthnRequestInfo)requestInfo).setDomainName(domainName);
                } else {
                    requestInfo = backend.parseRelayState(response, relayState);
                }
            } else {
                throw SAMLExceptionCode.INVALID_REQUEST.create("Unsolicited responses are not enabled");
            }
        } else {
            String relayState = httpRequest.getParameter("RelayState");
            if (relayState == null) {
                throw SAMLExceptionCode.INVALID_REQUEST.create("The 'RelayState' parameter was not set");
            }

            requestInfo = stateManagement.removeAuthnRequestInfo(relayState);
            if (requestInfo == null) {
                throw SAMLExceptionCode.INVALID_REQUEST.create("The 'RelayState' parameter was invalid");
            }
        }

        try {
            ValidationStrategy validationStrategy = backend.getValidationStrategy(config, stateManagement);
            AuthnResponseValidationResult validationResult = validationStrategy.validateAuthnResponse(response, requestInfo, binding);

            // Response is valid, remember its ID to detect replay attacks
            String responseId = response.getID();
            if (responseId != null) {
                stateManagement.addAuthnResponseID(responseId, AUTHN_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
            }

            Assertion bearerAssertion = validationResult.getBearerAssertion();
            AuthenticationInfo authInfo = backend.resolveAuthnResponse(response, bearerAssertion, requestInfo);
            LOG.debug("User {} in context {} is considered authenticated", authInfo.getUserId(), authInfo.getContextId());

            enhanceAuthInfo(authInfo, bearerAssertion);
            Map<String, String> properties = authInfo.getProperties();
            String sessionToken = sessionReservationService.reserveSessionFor(
                authInfo.getUserId(),
                authInfo.getContextId(),
                60l,
                TimeUnit.SECONDS,
                properties.isEmpty() ? null : properties);

            URIBuilder redirectLocation = new URIBuilder()
                .setScheme(getRedirectScheme(httpRequest))
                .setHost(requestInfo.getDomainName())
                .setPath(getRedirectPathPrefix() + "login")
                .setParameter(LoginServlet.PARAMETER_ACTION, SAMLLoginTools.ACTION_SAML_LOGIN)
                .setParameter(SAMLLoginTools.PARAM_TOKEN, sessionToken);

            String loginPath = requestInfo.getLoginPath();
            if (loginPath != null) {
                redirectLocation.setParameter(SAMLLoginTools.PARAM_LOGIN_PATH, loginPath);
            }

            String clientID = requestInfo.getClientID();
            if (clientID != null) {
                redirectLocation.setParameter(LoginFields.CLIENT_PARAM, clientID);
            }

            Tools.disableCaching(httpResponse);
            httpResponse.sendRedirect(redirectLocation.build().toString());
        } catch (URISyntaxException e) {
            throw SAMLExceptionCode.INTERNAL_ERROR.create(e.getMessage());
        } catch (ValidationException e) {
            throw SAMLExceptionCode.VALIDATION_FAILED.create(e.getReason().getMessage(), e.getMessage());
        }
    }

    @Override
    public String buildLogoutRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Session session) throws OXException {
        final LogoutRequest logoutRequest = customizeLogoutRequest(prepareLogoutRequest(session), session, httpRequest, httpResponse);
        String domainName = getDomainName(httpRequest);
        DefaultLogoutRequestInfo requestInfo = new DefaultLogoutRequestInfo();
        requestInfo.setRequestId(logoutRequest.getID());
        requestInfo.setSessionId(session.getSessionID());
        requestInfo.setDomainName(domainName);
        String relayState = stateManagement.addLogoutRequestInfo(requestInfo, LOGOUT_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);

        try {
            String logoutRequestXML = openSAML.marshall(logoutRequest);
            LOG.debug("Prepared LogoutRequest:\n{}", new Object() {
                @Override
                public String toString() {
                    return XMLHelper.prettyPrintXML(logoutRequest.getDOM());
                }
            });
            return compileLogoutRequestRedirectURI(logoutRequestXML, relayState);
        } catch (MarshallingException e) {
            throw SAMLExceptionCode.MARSHALLING_PROBLEM.create(e, e.getMessage());
        }
    }

    @Override
    public void handleLogoutResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Binding binding) throws IOException, OXException {
        String relayState = httpRequest.getParameter("RelayState");
        if (relayState == null) {
            throw SAMLExceptionCode.INVALID_REQUEST.create("The 'RelayState' parameter was not set");
        }

        LogoutRequestInfo requestInfo = stateManagement.removeLogoutRequestInfo(relayState);
        if (requestInfo == null) {
            throw SAMLExceptionCode.INVALID_REQUEST.create("LogoutResponse contains no valid 'InResponseTo' attribute");
        }

        LogoutResponse response = extractLogoutResponse(httpRequest, binding);
        try {
            ValidationStrategy validationStrategy = backend.getValidationStrategy(config, stateManagement);
            validationStrategy.validateLogoutResponse(response, httpRequest, requestInfo, binding);
            URI redirectLocationBuilder = new URIBuilder()
                .setScheme(getRedirectScheme(httpRequest))
                .setHost(requestInfo.getDomainName())
                .setPath(getRedirectPathPrefix() + "login")
                .setParameter(LoginServlet.PARAMETER_ACTION, SAMLLoginTools.ACTION_SAML_LOGOUT)
                .setParameter(LoginServlet.PARAMETER_SESSION, requestInfo.getSessionId())
                .build();

            Tools.disableCaching(httpResponse);
            String redirectLocation = redirectLocationBuilder.toString();
            LOG.debug("LogoutResponse '{}' is considered valid. Redirecting to logout action: {}", response.getID(), redirectLocation);
            httpResponse.sendRedirect(redirectLocation);
        } catch (URISyntaxException e) {
            throw SAMLExceptionCode.INTERNAL_ERROR.create(e.getMessage());
        } catch (ValidationException e) {
            throw SAMLExceptionCode.VALIDATION_FAILED.create(e.getReason().getMessage(), e.getMessage());
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
            final LogoutResponse logoutResponse = customizeLogoutResponse(prepareLogoutResponse(status, logoutRequest == null ? null : logoutRequest.getID()), httpRequest, httpResponse);
            String responseXML = openSAML.marshall(logoutResponse);
            LOG.debug("Marshalled LogoutResponse:\n{}", new Object() {
                @Override
                public String toString() {
                    return XMLHelper.prettyPrintXML(logoutResponse.getDOM());
                }
            });
            Binding responseBinding = config.getLogoutResponseBinding();
            switch (responseBinding) {
                case HTTP_REDIRECT:
                    sendLogoutResponseViaRedirect(responseXML, httpRequest, httpResponse);
                    break;

                case HTTP_POST:
                    sendLogoutResponseViaPOST(responseXML, httpRequest, httpResponse);
                    break;

                default:
                    throw SAMLExceptionCode.UNSUPPORTED_BINDING.create(responseBinding.name());
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

        if (config.singleLogoutEnabled()) {
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

    private String getDomainName(HttpServletRequest httpRequest) {
        return SAMLLoginTools.getHostName(services.getOptionalService(HostnameService.class), httpRequest);
    }

    private String compileLogoutRequestRedirectURI(String logoutRequestXML, String relayState) throws OXException {
        String encoded = deflateAndEncode(logoutRequestXML);
        try {
            URIBuilder redirectLocationBuilder = new URIBuilder(config.getIdentityProviderLogoutURL()).setParameter("SAMLRequest", encoded).setParameter("RelayState", relayState);
            trySignRedirectHeader(redirectLocationBuilder);
            String redirectLocation = redirectLocationBuilder.build().toString();
            LOG.debug("Redirect URI for LogoutRequest: {}", redirectLocation );
            return redirectLocation;
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
        logoutRequest.setID(generateID());
        logoutRequest.setIssueInstant(new DateTime());
        logoutRequest.setVersion(SAMLVersion.VERSION_20);
        logoutRequest.setReason(LogoutRequest.USER_REASON);
        logoutRequest.setNotOnOrAfter(new DateTime(System.currentTimeMillis() + LOGOUT_REQUEST_TIMEOUT));
        String sessionIndex = (String) session.getParameter(SAMLSessionParameters.SESSION_INDEX);
        if (sessionIndex != null) {
            SessionIndex indexElement = openSAML.buildSAMLObject(SessionIndex.class);
            indexElement.setSessionIndex(sessionIndex);
            logoutRequest.getSessionIndexes().add(indexElement);
        }

        String subjectID = (String) session.getParameter(SAMLSessionParameters.SUBJECT_ID);
        if (subjectID != null) {
            try {
                SAMLObject subjectIDElement = openSAML.unmarshall(SAMLObject.class, subjectID);
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

    private LogoutRequest customizeLogoutRequest(LogoutRequest logoutRequest, Session session, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        WebSSOCustomizer customizer = backend.getWebSSOCustomizer();
        if (customizer != null) {
            RequestContext requestContext = new RequestContext();
            requestContext.config = config;
            requestContext.openSAML = openSAML;
            requestContext.httpRequest = httpRequest;
            requestContext.httpResponse = httpResponse;
            return customizer.customizeLogoutRequest(logoutRequest, session, requestContext);
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
        logoutResponse.setID(generateID());
        logoutResponse.setIssueInstant(new DateTime());
        logoutResponse.setVersion(SAMLVersion.VERSION_20);
        if (inResponseTo != null) {
            logoutResponse.setInResponseTo(inResponseTo);
        }

        return logoutResponse;
    }

    private LogoutResponse customizeLogoutResponse(LogoutResponse logoutResponse, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        WebSSOCustomizer customizer = backend.getWebSSOCustomizer();
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

    private void sendLogoutResponseViaPOST(String responseXML, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        try {
            String encoded = Base64.encodeBase64String(responseXML.getBytes());
            TemplateService templateService = services.getService(TemplateService.class);
            OXTemplate template = templateService.loadTemplate(config.getLogoutResponseTemplate());
            Map<String, String> vars = new HashMap<String, String>(5);
            vars.put("action", config.getIdentityProviderLogoutURL());
            vars.put("SAMLResponse", encoded);
            String relayState = httpRequest.getParameter("RelayState");
            if (relayState != null) {
                vars.put("RelayState", relayState);
            }

            httpResponse.setStatus(HttpServletResponse.SC_OK);
            httpResponse.setContentType("text/html");
            httpResponse.setCharacterEncoding(Charsets.UTF_8_NAME);
            StringWriter writer = new StringWriter();
            template.process(vars, writer);
            String html = writer.toString();
            LOG.debug("Sending LogoutResponse via POST:\n{}", html);
            httpResponse.getWriter().write(html);
        } catch (OXException e) {
            LOG.error("Could not send LogoutResponse via POST", e);
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void terminateSessions(LogoutRequest logoutRequest, LogoutInfo logoutInfo, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        SessiondService sessiondService = services.getService(SessiondService.class);
        List<SessionIndex> sessionIndexes = logoutRequest.getSessionIndexes();
        boolean removedAnySession = false;
        int numIndexes = sessionIndexes.size();
        if (numIndexes > 0) {
            String filterString;
            if (numIndexes == 1) {
                filterString = "(" + SAMLSessionParameters.SESSION_INDEX + "=" + sessionIndexes.get(0).getSessionIndex() + ")";
            } else {
                StringBuilder fsBuilder = new StringBuilder(128).append("(|");
                for (SessionIndex sessionIndex : sessionIndexes) {
                    fsBuilder.append("(" + SAMLSessionParameters.SESSION_INDEX + "=" + sessionIndex.getSessionIndex() + ")");
                }
                fsBuilder.append(')');
                filterString = fsBuilder.toString();
            }

            Collection<String> sessionIds = sessiondService.removeSessionsGlobally(SessionFilter.create(filterString));
            LOG.debug("Removed {} sessions for {} indexes", sessionIds.size(), numIndexes);
            removedAnySession = sessionIds.size() > 0;
        } else {
            int contextId = logoutInfo.getContextId();
            int userId = logoutInfo.getUserId();
            if (contextId > 0 && userId > 0) {
                sessiondService.removeUserSessionsGlobally(userId, contextId);
                removedAnySession = true;
                LOG.debug("Removed sessions globally for user {} in context {}", userId, contextId);
            } else {
                LOG.warn("LogoutRequest contained no session indexes but no valid user and context were determined. Cannot invalidate any session...");
            }
        }

        if (!removedAnySession) {
            LOG.info("Received LogoutRequest but no session was removed");
        }
    }

    private String getRedirectPathPrefix() {
        DispatcherPrefixService prefixService = services.getService(DispatcherPrefixService.class);
        return prefixService.getPrefix();
    }

    private String getRedirectScheme(HttpServletRequest httpRequest) {
        boolean secure = Tools.considerSecure(httpRequest);
        return secure ? "https" : "http";
    }

    private void enhanceAuthInfo(AuthenticationInfo authInfo, Assertion bearerAssertion) throws OXException {
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
            properties.put(SAMLSessionParameters.SESSION_INDEX, sessionIndex);
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
            properties.put(SAMLSessionParameters.SESSION_NOT_ON_OR_AFTER, sessionNotOnOrAfter);
        }

        /*
         * The bearer assertions subject must contain a <code>NameID</code>, <code>BaseID</code> or <code>EncryptedID</code>
         * element. We need to remember the elements XML representation, as it will be later on used to compile logout requests.
         * However if the IDP does not comply to the specification, the attribute might be missing.
         */
        String subjectID = extractSubjectID(bearerAssertion);
        if (subjectID != null) {
            properties.put(SAMLSessionParameters.SUBJECT_ID, subjectID);
        }

        /*
         * The bearer assertion might include an Attribute statement, with an attribute conforming to the xXMLml format of the
         * OAuth 2 specification.
         */
        String accessToken = extractAccessToken(bearerAssertion);
        if (accessToken != null) {
            properties.put(SAMLSessionParameters.ACCESS_TOKEN, accessToken);
        }

        /*
         * This parameter must be checked within the AuthenticationService implementation. If not set, the login request is triggered
         * by any other authentication mechanism (e.g. HTTP Basic Auth) and its credentials must be checked.
         */
        properties.put(SAMLSessionParameters.AUTHENTICATED, Boolean.TRUE.toString());
    }

    private String compileAuthnRequestRedirectURI(String authnRequestXML, String relayState) throws OXException {
        String encoded = deflateAndEncode(authnRequestXML);
        try {
            URIBuilder redirectLocationBuilder = new URIBuilder(config.getIdentityProviderAuthnURL()).setParameter("SAMLRequest", encoded).setParameter("RelayState", relayState);
            trySignRedirectHeader(redirectLocationBuilder);
            String redirectLocation = redirectLocationBuilder.build().toString();
            LOG.debug("Redirect URI for AuthnRequest: {}", redirectLocation );
            return redirectLocation;
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

    private AuthnRequest customizeAuthnRequest(AuthnRequest authnRequest, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        WebSSOCustomizer customizer = backend.getWebSSOCustomizer();
        if (customizer != null) {
            RequestContext requestContext = new WebSSOCustomizer.RequestContext();
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
        issuer.setValue(config.getEntityID());
        authnRequest.setIssuer(issuer);

        authnRequest.setProviderName(config.getProviderName());
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        authnRequest.setAssertionConsumerServiceURL(config.getAssertionConsumerServiceURL());
        authnRequest.setDestination(config.getIdentityProviderAuthnURL());
        authnRequest.setIsPassive(Boolean.FALSE);
        authnRequest.setForceAuthn(Boolean.FALSE);
        authnRequest.setID(generateID());
        authnRequest.setIssueInstant(new DateTime());
        return authnRequest;
    }

    private SPSSODescriptor customizeDescriptor(SPSSODescriptor spssoDescriptor) throws OXException {
        WebSSOCustomizer customizer = backend.getWebSSOCustomizer();
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
                return openSAML.getParserPool().parse(new InflaterInputStream(new ByteArrayInputStream(requestBytes), new Inflater(true)));
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
                return openSAML.getParserPool().parse(new InflaterInputStream(new ByteArrayInputStream(requestBytes), new Inflater(true)));
            } else {
                return openSAML.getParserPool().parse(new ByteArrayInputStream(requestBytes));
            }
        } catch (XMLParserException e) {
            throw SAMLExceptionCode.DECODING_ERROR.create(e, e.getMessage());
        }
    }

    private String customDecodeLogoutRequest(HttpServletRequest httpRequest, Binding binding) throws OXException {
        WebSSOCustomizer customizer = backend.getWebSSOCustomizer();
        if (customizer != null) {
            return customizer.decodeLogoutRequest(httpRequest, binding);
        }

        return null;
    }

    private String customDecodeLogoutResponse(HttpServletRequest httpRequest, Binding binding) throws OXException {
        WebSSOCustomizer customizer = backend.getWebSSOCustomizer();
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
        WebSSOCustomizer customizer = backend.getWebSSOCustomizer();
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

    private String extractAccessToken(Assertion assertion) throws OXException {
        return backend.getAccessToken(assertion);
    }

    private static String generateID() {
        return UUIDs.getUnformattedString(UUID.randomUUID());
    }

}
