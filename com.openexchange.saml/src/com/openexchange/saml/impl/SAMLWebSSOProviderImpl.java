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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.xml.XMLObject;
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
import com.openexchange.saml.OpenSAML;
import com.openexchange.saml.SAMLConfig;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.SAMLExceptionCode;
import com.openexchange.saml.WebSSOProvider;
import com.openexchange.saml.spi.AuthenticationInfo;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.spi.SAMLBackend;
import com.openexchange.saml.spi.SAMLWebSSOCustomizer;
import com.openexchange.saml.spi.SAMLWebSSOCustomizer.RequestContext;
import com.openexchange.saml.state.AuthnRequestInfo;
import com.openexchange.saml.state.DefaultAuthnRequestInfo;
import com.openexchange.saml.state.StateManagement;
import com.openexchange.saml.validation.ValidationException;
import com.openexchange.saml.validation.ValidationResult;
import com.openexchange.saml.validation.ValidationStrategy;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.tools.servlet.http.Tools;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SAMLWebSSOProviderImpl implements WebSSOProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SAMLWebSSOProviderImpl.class);

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
            Binding requestBinding = config.getRequestBinding();
            LOG.debug("Responding with AuthnRequest via " + requestBinding.toString() + ":\n{}", authnRequestXML);
            switch (requestBinding) {
                case HTTP_POST:
                    // sendFormResponse(authnRequestXML, httpResponse);
                    break;
                case HTTP_REDIRECT:
                    sendRedirect(authnRequestXML, relayState, httpRequest, httpResponse);
                    break;
                default:
                    throw SAMLExceptionCode.UNSUPPORTED_BINDING.create(requestBinding);
            }
        } catch (MarshallingException e) {
            throw SAMLExceptionCode.MARSHALLING_PROBLEM.create(e, e.getMessage());
        }
    }

    @Override
    public void handleAuthnResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Binding binding) throws OXException, IOException {
        if (binding != Binding.HTTP_POST) {
            /*
             * The HTTP Redirect binding MUST NOT be used, as the response will
             * typically exceed the URL length permitted by most user agents.
             * [profiles 06 - 4.1.2p17]
             *
             * We don't support artifact binding yet. If you need it, feel free to implement it ;-)
             */
            throw SAMLExceptionCode.UNSUPPORTED_BINDING.create(binding.name());
        }

        Response response = decodeResponse(httpRequest);
        try {
            /*
             * TODO:
             * If the identity provider supports the Single Logout profile, defined in Section 4.4, any authentication
             * statements MUST include a SessionIndex attribute to enable per-session logout requests by the
             * service provider.
             * [profiles 06 - 4.1.4.2p20]
             */

            /*
             * TODO:
             * If an <AuthnStatement> used to establish a security context for the principal contains a
             * SessionNotOnOrAfter attribute, the security context SHOULD be discarded once this time is
             * reached, unless the service provider reestablishes the principal's identity by repeating the use of this
             * profile. Note that if multiple <AuthnStatement> elements are present, the SessionNotOnOrAfter
             * value closest to the present time SHOULD be honored.
             */

            ValidationStrategy validationStrategy = backend.getValidationStrategy(config, stateManagement);
            ValidationResult validationResult = validationStrategy.validate(response, binding);
            AuthnRequestInfo requestInfo = validationResult.getRequestInfo();
            if (requestInfo != null) {
                stateManagement.removeAuthnRequestInfo(requestInfo.getRequestID());
            }

            Assertion bearerAssertion = validationResult.getBearerAssertion();
            AuthenticationInfo authInfo = backend.resolveResponse(response, bearerAssertion);
            LOG.debug("User {} in context {} is considered authenticated", authInfo.getUserId(), authInfo.getContextId());

            String relayState = httpRequest.getParameter("RelayState");
            if (relayState == null) {
                throw SAMLExceptionCode.INVALID_REQUEST.create("The 'RelayState' parameter was not set");
            }

            String redirectHost = relayState; // TODO: integrity check and fail on null

            /*
             * TODO state:
             *  - session index for logout
             *  - SessionNotOnOrAfter
             */
            Map<String, String> properties = authInfo.getProperties();
            String sessionToken = sessionReservationService.reserveSessionFor(authInfo.getUserId(), authInfo.getContextId(), 60l, TimeUnit.SECONDS, properties.isEmpty() ? null : properties);

            ConfigurationService configService = services.getService(ConfigurationService.class);
            boolean secure = Tools.considerSecure(httpRequest, Boolean.parseBoolean(configService.getProperty(Property.FORCE_HTTPS.getPropertyName(), Property.FORCE_HTTPS.getDefaultValue())));

            DispatcherPrefixService prefixService = services.getService(DispatcherPrefixService.class);
            String prefix = prefixService.getPrefix();

            URI redirectLocation = new URIBuilder()
                .setScheme(secure ? "https" : "http")
                .setHost(redirectHost)
                .setPath(prefix + "login")
                .setParameter("action", "redeemReservation")
                .setParameter("token", sessionToken)
                .build();

            httpResponse.sendRedirect(redirectLocation.toString());
        } catch (URISyntaxException e) {
            throw SAMLExceptionCode.INTERNAL_ERROR.create(e.getMessage());
        } catch (ValidationException e) {
            throw SAMLExceptionCode.VALIDATION_FAILED.create(e.getReason().getMessage(), e.getMessage());
        }
    }

    @Override
    public String getMetadataXML() throws OXException {
        SPSSODescriptor spssoDescriptor = openSAML.buildSAMLObject(SPSSODescriptor.class);
        spssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);

        AssertionConsumerService acs = openSAML.buildSAMLObject(AssertionConsumerService.class);
        acs.setIndex(1);
        acs.setIsDefault(Boolean.TRUE);
        acs.setBinding(getBindingURI(config.getResponseBinding()));
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
            Element element = openSAML.getMarshallerFactory().getMarshaller(spDescriptor).marshall(spDescriptor);
            return XMLHelper.nodeToString(element);
        } catch (MarshallingException e) {
            throw SAMLExceptionCode.MARSHALLING_PROBLEM.create(e, e.getMessage());
        }
    }

    private void sendRedirect(String authnRequestXML, String relayState, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFLATED, true);
        DeflaterOutputStream deflaterStream = new DeflaterOutputStream(bytesOut, deflater);
        String encoded;
        try {
            deflaterStream.write(authnRequestXML.getBytes(Charsets.UTF_8));
            deflaterStream.finish();
            encoded = Base64.encodeBase64String(bytesOut.toByteArray());
        } catch (IOException e) {
            throw SAMLExceptionCode.ENCODING_ERROR.create(e, "Could not deflate XML");
        }

        String redirectLocation;
        try {
            // TODO: integrity protect RelayState
            CredentialProvider credentialProvider = backend.getCredentialProvider();
            URIBuilder redirectLocationBuilder = new URIBuilder(config.getIdentityProviderURL()).setParameter("SAMLRequest", encoded).setParameter("RelayState", relayState);
            if (credentialProvider.hasSigningCredential()) {
                /*
                 * The <AuthnRequest> message MAY be signed, if authentication of the request issuer is required.
                 * [profiles 06 - 4.1.3.3p18]
                 */
                Credential signingCredential = credentialProvider.getSigningCredential();
                String sigAlg = openSAML.getGlobalSecurityConfiguration().getSignatureAlgorithmURI(signingCredential.getPrivateKey().getAlgorithm());
                redirectLocationBuilder.setParameter("SigAlg", sigAlg);
                byte[] rawSignature = SigningUtil.signWithURI(
                    signingCredential,
                    sigAlg,
                    redirectLocationBuilder.build().getRawQuery().getBytes(Charsets.UTF_8));
                String signature = Base64.encodeBase64String(rawSignature);
                redirectLocationBuilder.setParameter("Signature", signature);
                LOG.debug("AuthnRequest was signed with algorithm {}", sigAlg);
            }

            redirectLocation = redirectLocationBuilder.build().toString();
            throw LoginExceptionCodes.REDIRECT.create(redirectLocation);
        } catch (URISyntaxException e) {
            throw SAMLExceptionCode.ENCODING_ERROR.create(e, "Could not construct redirect location");
        } catch (SecurityException e) {
            throw SAMLExceptionCode.ENCODING_ERROR.create(e, "Could not compute authentication request signature");
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
         * The <Issuer> element MUST be present and MUST contain the unique identifier of the requesting
         * service provider; the Format attribute MUST be omitted or have a value of urn:oasis:names:tc:SAML:2.0:nameid-format:entity.
         * [profiles 06 - 4.1.4.1p19]
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
        authnRequest.setProtocolBinding(getBindingURI(config.getResponseBinding()));
        authnRequest.setAssertionConsumerServiceURL(config.getAssertionConsumerServiceURL());
        authnRequest.setDestination(config.getIdentityProviderURL());
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

    private Response decodeResponse(HttpServletRequest httpRequest) throws OXException {
        String responseXML = customDecode(httpRequest);
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
                throw SAMLExceptionCode.DECODING_ERROR.create("XML was not a valid Response element");
            }

            final Response response = (Response) unmarshalledResponse;
            LOG.debug("Received SAMLResponse:\n{}", new Object() { @Override
                public String toString() {
                    return XMLHelper.prettyPrintXML(response.getDOM());
                }
            });
            return response;
        } catch (XMLParserException e) {
            throw SAMLExceptionCode.DECODING_ERROR.create(e, e.getMessage());
        } catch (UnmarshallingException e) {
            throw SAMLExceptionCode.DECODING_ERROR.create(e, e.getMessage());
        }
    }

    private String customDecode(HttpServletRequest httpRequest) throws OXException {
        SAMLWebSSOCustomizer customizer = backend.getWebSSOCustomizer();
        if (customizer != null) {
            return customizer.decodeResponse(httpRequest);
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
