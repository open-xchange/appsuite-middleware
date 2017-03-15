package com.openexchange.saml;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.sim.SimHttpServletRequest;
import javax.servlet.http.sim.SimHttpServletResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusResponseType;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.saml2.encryption.Encrypter.KeyPlacement;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.ws.transport.OutputStreamOutTransportAdapter;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.ldap.SimUser;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.impl.WebSSOProviderImpl;
import com.openexchange.saml.oauth.service.OAuthAccessToken;
import com.openexchange.saml.oauth.service.OAuthAccessTokenService;
import com.openexchange.saml.oauth.service.OAuthAccessTokenService.OAuthGrantType;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.spi.SAMLBackend;
import com.openexchange.saml.state.SimStateManagement;
import com.openexchange.saml.tools.SAMLLoginTools;
import com.openexchange.saml.tools.SignatureHelper;
import com.openexchange.server.SimpleServiceLookup;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.session.reservation.SimSessionReservationService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SimSessiondService;
import com.openexchange.user.SimUserService;
import com.openexchange.user.UserService;

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

/**
 * {@link SAMLWebSSOProviderOAuthTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(OAuthAccessTokenService.class)
@PowerMockIgnore({"javax.net.*","javax.security.*","javax.crypto.*"})
public class SAMLWebSSOProviderOAuthTest {

    private static SAMLWebSSOProvider provider;
    private static TestConfig config;
    private static OpenSAML openSAML;
    private static TestCredentials testCredentials;
    private static CredentialProvider credentialProvider;
    private static SimStateManagement stateManagement;
    private static SimSessionReservationService sessionReservationService;
    private static SimSessiondService sessiondService;
    private static SimpleServiceLookup services;
    private static SimUserService userService;

    private static OAuthAccessTokenService mock;

    @BeforeClass
    public static void beforeClass() throws Exception {
        DefaultBootstrap.bootstrap();
        testCredentials = new TestCredentials();
        credentialProvider = testCredentials.getSPCredentialProvider();

        /*
         * Init service provider
         */
        config = new TestConfig();
        openSAML = new OpenSAML();

        services = new SimpleServiceLookup();
        sessionReservationService = new SimSessionReservationService();
        services.add(SessionReservationService.class, sessionReservationService);
        services.add(DispatcherPrefixService.class, new DispatcherPrefixService() {
            @Override
            public String getPrefix() {
                return "/appsuite/api/";
            }
        });
        TestSAMLBackend samlBackend = new TestSAMLBackend(credentialProvider, config);
        services.add(SAMLBackend.class, samlBackend);
        sessiondService = new SimSessiondService();
        services.add(SessiondService.class, sessiondService);
        userService = new SimUserService();
        services.add(UserService.class, userService);
        userService.addUser(new SimUser(1), 1);
        stateManagement = new SimStateManagement();

        mock = PowerMockito.mock(OAuthAccessTokenService.class);
        PowerMockito.when(mock, "isConfigured", 1,1).thenReturn(true);
        services.add(OAuthAccessTokenService.class,mock);

        provider = new WebSSOProviderImpl(config, openSAML, stateManagement, services, samlBackend);
    }

     @Test
     public void testLoginRoundtrip() throws Exception {
        /*
         * Trigger AuthnRequest
         */
        String requestHost = "webmail.example.com";
        String requestedLoginPath = "/fancyclient/login.html";
        SimHttpServletRequest loginHTTPRequest = prepareHTTPRequest("GET", new URIBuilder()
            .setScheme("https")
            .setHost(requestHost)
            .setPath("/appsuite/api/saml/init")
            .setParameter("flow", "login")
            .setParameter("loginPath", requestedLoginPath)
            .setParameter("client", "test-client")
            .build());
        URI authnRequestURI = new URI(provider.buildAuthnRequest(loginHTTPRequest, new SimHttpServletResponse()));

        /*
         * Validate redirect location
         */
        String relayState = parseURIQuery(authnRequestURI).get("RelayState");
        Assert.assertNotNull(relayState);
        SimHttpServletRequest authnHTTPRequest = prepareHTTPRequest("GET", authnRequestURI);
        Assert.assertNull(SignatureHelper.validateURISignature(authnHTTPRequest, testCredentials.getSPSigningCredential()));
        AuthnRequest authnRequest = parseAuthnRequest(authnHTTPRequest);

        /*
         * Build response and process it
         */
        Response response = buildResponse(authnRequest);
        SimHttpServletRequest samlResponseRequest = prepareHTTPRequest("POST", new URIBuilder(authnRequest.getAssertionConsumerServiceURL())
            .setParameter("SAMLResponse", Base64.encodeBytes(marshall(response).getBytes()))
            .setParameter("RelayState", relayState)
            .build());

        SimHttpServletResponse httpResponse = new SimHttpServletResponse();
        mockSAMLConfig();
        provider.handleAuthnResponse(samlResponseRequest, httpResponse, Binding.HTTP_POST);
        assertCachingDisabledHeaders(httpResponse);

        /*
         * Assert final login redirect
         */
        Assert.assertEquals(HttpServletResponse.SC_FOUND, httpResponse.getStatus());
        String location = httpResponse.getHeader("Location");
        Assert.assertNotNull(location);
        URI locationURI = new URIBuilder(location).build();
        Assert.assertEquals(requestHost, locationURI.getHost());
        Map<String, String> redirectParams = parseURIQuery(locationURI);
        Assert.assertEquals(requestedLoginPath, redirectParams.get(SAMLLoginTools.PARAM_LOGIN_PATH));
        Assert.assertEquals("test-client", redirectParams.get(LoginFields.CLIENT_PARAM));
        Assert.assertEquals(SAMLLoginTools.ACTION_SAML_LOGIN, redirectParams.get("action"));
        String reservationToken = redirectParams.get(SAMLLoginTools.PARAM_TOKEN);
        Assert.assertNotNull(reservationToken);
        Assert.assertNotNull(sessionReservationService.removeReservation(reservationToken));
    }

     @Test
     public void testIdPInitiatedLogin() throws Exception {
        AuthnRequest authnRequest = prepareAuthnRequest();

        /*
         * Build response and process it
         */
        Response response = buildResponseWithoutInResponseTo();
        SimHttpServletRequest samlResponseRequest = prepareHTTPRequest("POST", new URIBuilder(authnRequest.getAssertionConsumerServiceURL())
            .setParameter("SAMLResponse", Base64.encodeBytes(marshall(response).getBytes()))
            .build());

        SimHttpServletResponse httpResponse = new SimHttpServletResponse();
        mockSAMLConfig();


        provider.handleAuthnResponse(samlResponseRequest, httpResponse, Binding.HTTP_POST);
        assertCachingDisabledHeaders(httpResponse);

        /*
         * Assert final login redirect
         */
        Assert.assertEquals(HttpServletResponse.SC_FOUND, httpResponse.getStatus());
        String location = httpResponse.getHeader("Location");
        Assert.assertNotNull(location);
        URI locationURI = new URIBuilder(location).build();
        Assert.assertEquals("webmail.example.com", locationURI.getHost());
        Map<String, String> redirectParams = parseURIQuery(locationURI);
        Assert.assertEquals(SAMLLoginTools.ACTION_SAML_LOGIN, redirectParams.get("action"));
        String reservationToken = redirectParams.get(SAMLLoginTools.PARAM_TOKEN);
        Assert.assertNotNull(reservationToken);
        Assert.assertNotNull(sessionReservationService.removeReservation(reservationToken));
    }

    private void mockSAMLConfig() throws Exception {

        PowerMockito.when(mock.getAccessToken((OAuthGrantType) Mockito.any(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).thenReturn(new OAuthAccessToken("access", "refresh", "oauth_bearer", 10));

     }

     @Test
     public void testIdPInitiatedLoginWithRelayState() throws Exception {
        AuthnRequest authnRequest = prepareAuthnRequest();

        String requestHost = "webmail2.example.com";
        String requestedLoginPath = "/fancyclient/login.html";
        String requestClient = "test-client";
        String split = ":";

        StringBuilder relayStateBuilder = new StringBuilder();
        relayStateBuilder.append("domain=").append(requestHost).append(split);
        relayStateBuilder.append("loginpath=").append(requestedLoginPath).append(split);
        relayStateBuilder.append("client=").append(requestClient);
        String encodedRelayState = Base64.encodeBytes(relayStateBuilder.toString().getBytes());

        /*
         * Build response and process it
         */
        Response response = buildResponseWithoutInResponseTo();
        SimHttpServletRequest samlResponseRequest = prepareHTTPRequest("POST", new URIBuilder(authnRequest.getAssertionConsumerServiceURL())
            .setParameter("SAMLResponse", Base64.encodeBytes(marshall(response).getBytes()))
            .setParameter("RelayState", encodedRelayState)
            .build());

        SimHttpServletResponse httpResponse = new SimHttpServletResponse();

        mockSAMLConfig();

        provider.handleAuthnResponse(samlResponseRequest, httpResponse, Binding.HTTP_POST);
        assertCachingDisabledHeaders(httpResponse);

        /*
         * Assert final login redirect
         */
        Assert.assertEquals(HttpServletResponse.SC_FOUND, httpResponse.getStatus());
        String location = httpResponse.getHeader("Location");
        Assert.assertNotNull(location);
        URI locationURI = new URIBuilder(location).build();
        Assert.assertEquals(requestHost, locationURI.getHost());
        Map<String, String> redirectParams = parseURIQuery(locationURI);
        Assert.assertEquals(requestedLoginPath, redirectParams.get(SAMLLoginTools.PARAM_LOGIN_PATH));
        Assert.assertEquals(requestClient, redirectParams.get(LoginFields.CLIENT_PARAM));
        Assert.assertEquals(SAMLLoginTools.ACTION_SAML_LOGIN, redirectParams.get("action"));
        String reservationToken = redirectParams.get(SAMLLoginTools.PARAM_TOKEN);
        Assert.assertNotNull(reservationToken);
        Assert.assertNotNull(sessionReservationService.removeReservation(reservationToken));
    }

     @Test
     public void testIdPInitiatedLoginWithPartlyRelayState() throws Exception {
        AuthnRequest authnRequest = prepareAuthnRequest();

        String requestHost = "webmail2.example.com";
        String split = ":";

        StringBuilder relayStateBuilder = new StringBuilder();
        relayStateBuilder.append("domain=").append(requestHost).append(split);
        String encodedRelayState = Base64.encodeBytes(relayStateBuilder.toString().getBytes());

        /*
         * Build response and process it
         */
        Response response = buildResponseWithoutInResponseTo();
        SimHttpServletRequest samlResponseRequest = prepareHTTPRequest("POST", new URIBuilder(authnRequest.getAssertionConsumerServiceURL())
            .setParameter("SAMLResponse", Base64.encodeBytes(marshall(response).getBytes()))
            .setParameter("RelayState", encodedRelayState)
            .build());

        SimHttpServletResponse httpResponse = new SimHttpServletResponse();
        mockSAMLConfig();
        provider.handleAuthnResponse(samlResponseRequest, httpResponse, Binding.HTTP_POST);
        assertCachingDisabledHeaders(httpResponse);

        /*
         * Assert final login redirect
         */
        Assert.assertEquals(HttpServletResponse.SC_FOUND, httpResponse.getStatus());
        String location = httpResponse.getHeader("Location");
        Assert.assertNotNull(location);
        URI locationURI = new URIBuilder(location).build();
        Assert.assertEquals(requestHost, locationURI.getHost());
        Map<String, String> redirectParams = parseURIQuery(locationURI);
        Assert.assertEquals(SAMLLoginTools.ACTION_SAML_LOGIN, redirectParams.get("action"));
        String reservationToken = redirectParams.get(SAMLLoginTools.PARAM_TOKEN);
        Assert.assertNotNull(reservationToken);
        Assert.assertNotNull(sessionReservationService.removeReservation(reservationToken));
    }

    private void assertCachingDisabledHeaders(SimHttpServletResponse response) {
        // Pragma: no-cache
        Assert.assertEquals("no-cache", response.getHeader("Pragma"));

        // Cache-Control: no-store, no-cache, must-revalidate, post-check=0, pre-check=0
        String cacheControl = response.getHeader("Cache-Control");
        Assert.assertNotNull(cacheControl);
        List<String> cacheControls = Strings.splitAndTrim(cacheControl, ",");
        Assert.assertTrue(cacheControls.contains("no-store"));
        Assert.assertTrue(cacheControls.contains("no-cache"));
        Assert.assertTrue(cacheControls.contains("must-revalidate"));
        Assert.assertTrue(cacheControls.contains("post-check=0"));
        Assert.assertTrue(cacheControls.contains("pre-check=0"));

        // Expires: Tue, 03 May 1988 12:00:00 GMT
        String expires = response.getHeader("Expires");
        Assert.assertNotNull(expires);
        try {
            Date expiryDate = new SimpleDateFormat("EEE',' dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).parse(expires);
            Assert.assertTrue(expiryDate.before(new Date()));
        } catch (ParseException e) {
            Assert.fail("Invalid date format for expires header: " + expires);
        }
    }

    private AuthnRequest parseAuthnRequest(HttpServletRequest httpRequest) throws Exception {
        HttpServletRequestAdapter inTransport = new HttpServletRequestAdapter(httpRequest);
        BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject> context = new BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject>();
        context.setCommunicationProfileId("urn:oasis:names:tc:SAML:2.0:profiles:SSO:browser");
        context.setInboundMessageTransport(inTransport);
        context.setInboundSAMLProtocol(SAMLConstants.SAML20P_NS);
        context.setPeerEntityRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        context.setOutboundMessageTransport(new OutputStreamOutTransportAdapter(output));
        context.setOutboundSAMLProtocol(SAMLConstants.SAML20P_NS);

        HTTPRedirectDeflateDecoder decoder = new HTTPRedirectDeflateDecoder();
        decoder.decode(context);
        return (AuthnRequest) context.getInboundSAMLMessage();
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
        authnRequest.setID(UUIDs.getUnformattedString(UUID.randomUUID()));
        authnRequest.setIssueInstant(new DateTime());
        return authnRequest;
    }

    private static SimHttpServletRequest prepareHTTPRequest(String method, URI location) {
        SimHttpServletRequest request = new SimHttpServletRequest();
        request.setRequestURI(location.getRawPath());
        request.setRequestURL(location.getScheme() + "://" + location.getHost() + location.getPath());
        request.setMethod(method);
        request.setScheme(location.getScheme());
        request.setSecure("https".equals(location.getScheme()));
        request.setServerName(location.getHost());
        request.setQueryString(location.getRawQuery());
        request.setCookies(Collections.<Cookie>emptyList());
        request.setRemoteAddr("127.0.0.1");
        Map<String, String> params = parseURIQuery(location);
        for (String name : params.keySet()) {
            request.setParameter(name, params.get(name));
        }
        return request;
    }

    private static Map<String, String> parseURIQuery(URI uri) {
        Map<String, String> map = new HashMap<String, String>();
        List<NameValuePair> pairs = URLEncodedUtils.parse(uri, "UTF-8");
        for (NameValuePair pair : pairs) {
            map.put(pair.getName(), pair.getValue());
        }
        return map;
    }

    private Response buildResponse(AuthnRequest request) throws Exception {
        String requestID = request.getID();
        Response response = openSAML.buildSAMLObject(Response.class);
        response.setDestination(config.getAssertionConsumerServiceURL());
        response.setID(UUIDs.getUnformattedString(UUID.randomUUID()));
        response.setInResponseTo(requestID);
        response.setIssueInstant(new DateTime());
        response.setVersion(SAMLVersion.VERSION_20);

        Issuer responseIssuer = openSAML.buildSAMLObject(Issuer.class);
        responseIssuer.setValue(config.getIdentityProviderEntityID());
        response.setIssuer(responseIssuer);

        Status status = openSAML.buildSAMLObject(Status.class);
        StatusCode statusCode = openSAML.buildSAMLObject(StatusCode.class);
        statusCode.setValue(StatusCode.SUCCESS_URI);
        status.setStatusCode(statusCode);
        response.setStatus(status);

        Assertion assertion = openSAML.buildSAMLObject(Assertion.class);
        assertion.setID(UUIDs.getUnformattedString(UUID.randomUUID()));
        assertion.setIssueInstant(new DateTime());

        Issuer assertionIssuer = openSAML.buildSAMLObject(Issuer.class);
        assertionIssuer.setValue(config.getIdentityProviderEntityID());
        assertion.setIssuer(assertionIssuer);

        Subject subject = openSAML.buildSAMLObject(Subject.class);
        NameID nameID = openSAML.buildSAMLObject(NameID.class);
        nameID.setFormat(NameIDType.TRANSIENT);
        nameID.setValue(UUIDs.getUnformattedString(UUID.randomUUID()));
        subject.setNameID(nameID);

        SubjectConfirmation subjectConfirmation = openSAML.buildSAMLObject(SubjectConfirmation.class);
        subjectConfirmation.setMethod("urn:oasis:names:tc:SAML:2.0:cm:bearer");
        SubjectConfirmationData subjectConfirmationData = openSAML.buildSAMLObject(SubjectConfirmationData.class);
        subjectConfirmationData.setAddress("10.20.30.1");
        subjectConfirmationData.setInResponseTo(requestID);
        subjectConfirmationData.setNotOnOrAfter(new DateTime(System.currentTimeMillis() + 60 *60 * 1000));
        subjectConfirmationData.setRecipient(config.getAssertionConsumerServiceURL());
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subject.getSubjectConfirmations().add(subjectConfirmation);
        assertion.setSubject(subject);

        Conditions conditions = openSAML.buildSAMLObject(Conditions.class);
        conditions.setNotBefore(new DateTime(System.currentTimeMillis() - 60 * 1000));
        conditions.setNotOnOrAfter(new DateTime(System.currentTimeMillis() + 60 *60 * 1000));
        AudienceRestriction audienceRestriction = openSAML.buildSAMLObject(AudienceRestriction.class);
        Audience audience = openSAML.buildSAMLObject(Audience.class);
        audience.setAudienceURI(config.getEntityID());
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        assertion.setConditions(conditions);

        AuthnStatement authnStatement = openSAML.buildSAMLObject(AuthnStatement.class);
        authnStatement.setAuthnInstant(new DateTime(System.currentTimeMillis() - 60 * 1000));
        authnStatement.setSessionIndex(UUIDs.getUnformattedString(UUID.randomUUID()));
        AuthnContext authnContext = openSAML.buildSAMLObject(AuthnContext.class);
        AuthnContextClassRef authnContextClassRef = openSAML.buildSAMLObject(AuthnContextClassRef.class);
        authnContextClassRef.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);
        assertion.getAuthnStatements().add(authnStatement);

        AttributeStatement attributeStatement = openSAML.buildSAMLObject(AttributeStatement.class);
        Attribute attribute = openSAML.buildSAMLObject(Attribute.class);
        attribute.setFriendlyName("userID");
        attribute.setName("urn:open-xchange:saml:userID");
        XSString attributeValue = (XSString) openSAML.getBuilderFactory().getBuilder(XSString.TYPE_NAME).buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        attributeValue.setValue("oxuser1");
        attribute.getAttributeValues().add(attributeValue);
        attributeStatement.getAttributes().add(attribute);
        assertion.getAttributeStatements().add(attributeStatement);

        Credential signingCredential = testCredentials.getIDPSigningCredential();
        Signature assertionSignature = openSAML.buildSAMLObject(Signature.class);
        assertionSignature.setSigningCredential(signingCredential);
        SecurityHelper.prepareSignatureParams(assertionSignature, signingCredential, null, null);
        assertion.setSignature(assertionSignature);
        openSAML.marshall(assertion); // marshalling is necessary for subsequent signing
        Signer.signObject(assertionSignature);

        EncryptedAssertion encryptedAssertion = getEncrypter().encrypt(assertion);
        response.getEncryptedAssertions().add(encryptedAssertion);

        Signature responseSignature = openSAML.buildSAMLObject(Signature.class);
        responseSignature.setSigningCredential(signingCredential);
        SecurityHelper.prepareSignatureParams(responseSignature, signingCredential, null, null);
        response.setSignature(responseSignature);
        openSAML.marshall(response); // marshalling is necessary for subsequent signing
        Signer.signObject(responseSignature);

        return response;
    }

    private Response buildResponseWithoutInResponseTo() throws Exception {
        Response response = openSAML.buildSAMLObject(Response.class);
        response.setDestination(config.getAssertionConsumerServiceURL());
        response.setID(UUIDs.getUnformattedString(UUID.randomUUID()));
        response.setIssueInstant(new DateTime());
        response.setVersion(SAMLVersion.VERSION_20);

        Issuer responseIssuer = openSAML.buildSAMLObject(Issuer.class);
        responseIssuer.setValue(config.getIdentityProviderEntityID());
        response.setIssuer(responseIssuer);

        Status status = openSAML.buildSAMLObject(Status.class);
        StatusCode statusCode = openSAML.buildSAMLObject(StatusCode.class);
        statusCode.setValue(StatusCode.SUCCESS_URI);
        status.setStatusCode(statusCode);
        response.setStatus(status);

        Assertion assertion = openSAML.buildSAMLObject(Assertion.class);
        assertion.setID(UUIDs.getUnformattedString(UUID.randomUUID()));
        assertion.setIssueInstant(new DateTime());

        Issuer assertionIssuer = openSAML.buildSAMLObject(Issuer.class);
        assertionIssuer.setValue(config.getIdentityProviderEntityID());
        assertion.setIssuer(assertionIssuer);

        Subject subject = openSAML.buildSAMLObject(Subject.class);
        NameID nameID = openSAML.buildSAMLObject(NameID.class);
        nameID.setFormat(NameIDType.TRANSIENT);
        nameID.setValue(UUIDs.getUnformattedString(UUID.randomUUID()));
        subject.setNameID(nameID);

        SubjectConfirmation subjectConfirmation = openSAML.buildSAMLObject(SubjectConfirmation.class);
        subjectConfirmation.setMethod("urn:oasis:names:tc:SAML:2.0:cm:bearer");
        SubjectConfirmationData subjectConfirmationData = openSAML.buildSAMLObject(SubjectConfirmationData.class);
        subjectConfirmationData.setAddress("10.20.30.1");
        subjectConfirmationData.setNotOnOrAfter(new DateTime(System.currentTimeMillis() + 60 *60 * 1000));
        subjectConfirmationData.setRecipient(config.getAssertionConsumerServiceURL());
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subject.getSubjectConfirmations().add(subjectConfirmation);
        assertion.setSubject(subject);

        Conditions conditions = openSAML.buildSAMLObject(Conditions.class);
        conditions.setNotBefore(new DateTime(System.currentTimeMillis() - 60 * 1000));
        conditions.setNotOnOrAfter(new DateTime(System.currentTimeMillis() + 60 *60 * 1000));
        AudienceRestriction audienceRestriction = openSAML.buildSAMLObject(AudienceRestriction.class);
        Audience audience = openSAML.buildSAMLObject(Audience.class);
        audience.setAudienceURI(config.getEntityID());
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        assertion.setConditions(conditions);

        AuthnStatement authnStatement = openSAML.buildSAMLObject(AuthnStatement.class);
        authnStatement.setAuthnInstant(new DateTime(System.currentTimeMillis() - 60 * 1000));
        authnStatement.setSessionIndex(UUIDs.getUnformattedString(UUID.randomUUID()));
        AuthnContext authnContext = openSAML.buildSAMLObject(AuthnContext.class);
        AuthnContextClassRef authnContextClassRef = openSAML.buildSAMLObject(AuthnContextClassRef.class);
        authnContextClassRef.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);
        assertion.getAuthnStatements().add(authnStatement);

        AttributeStatement attributeStatement = openSAML.buildSAMLObject(AttributeStatement.class);
        Attribute attribute = openSAML.buildSAMLObject(Attribute.class);
        attribute.setFriendlyName("userID");
        attribute.setName("urn:open-xchange:saml:userID");
        XSString attributeValue = (XSString) openSAML.getBuilderFactory().getBuilder(XSString.TYPE_NAME).buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        attributeValue.setValue("oxuser1");
        attribute.getAttributeValues().add(attributeValue);
        attributeStatement.getAttributes().add(attribute);
        assertion.getAttributeStatements().add(attributeStatement);

        Credential signingCredential = testCredentials.getIDPSigningCredential();
        Signature assertionSignature = openSAML.buildSAMLObject(Signature.class);
        assertionSignature.setSigningCredential(signingCredential);
        SecurityHelper.prepareSignatureParams(assertionSignature, signingCredential, null, null);
        assertion.setSignature(assertionSignature);
        openSAML.marshall(assertion); // marshalling is necessary for subsequent signing
        Signer.signObject(assertionSignature);

        EncryptedAssertion encryptedAssertion = getEncrypter().encrypt(assertion);
        response.getEncryptedAssertions().add(encryptedAssertion);

        Signature responseSignature = openSAML.buildSAMLObject(Signature.class);
        responseSignature.setSigningCredential(signingCredential);
        SecurityHelper.prepareSignatureParams(responseSignature, signingCredential, null, null);
        response.setSignature(responseSignature);
        openSAML.marshall(response); // marshalling is necessary for subsequent signing
        Signer.signObject(responseSignature);

        return response;
    }

    private String marshall(StatusResponseType response) throws MarshallingException {
        // Never ever use the prettyPrint method! The resulting XML will differ slightly and signature validation will fail!
        return XMLHelper.nodeToString(openSAML.getMarshallerFactory().getMarshaller(response).marshall(response));
    }

    private Encrypter getEncrypter() throws Exception {
        // https://wiki.shibboleth.net/confluence/display/OpenSAML/OSTwoUserManJavaXMLEncryption
        Credential keyEncryptionCredential = testCredentials.getEncryptionCredential();

        EncryptionParameters encParams = new EncryptionParameters();
        encParams.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);

        KeyEncryptionParameters kekParams = new KeyEncryptionParameters();
        kekParams.setEncryptionCredential(keyEncryptionCredential);
        kekParams.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
        KeyInfoGeneratorFactory kigf = openSAML.getGlobalSecurityConfiguration().getKeyInfoGeneratorManager().getDefaultManager().getFactory(keyEncryptionCredential);
        kekParams.setKeyInfoGenerator(kigf.newInstance());

        Encrypter samlEncrypter = new Encrypter(encParams, kekParams);
        samlEncrypter.setKeyPlacement(KeyPlacement.PEER);
        return samlEncrypter;

    }
}
