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

import static com.openexchange.saml.utils.SAMLTestUtils.parseURIQuery;
import static com.openexchange.saml.utils.SAMLTestUtils.prepareHTTPRequest;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.sim.SimHttpServletRequest;
import javax.servlet.http.sim.SimHttpServletResponse;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPRedirectDeflateDecoder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.StatusResponseType;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.saml.saml2.encryption.Encrypter.KeyPlacement;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.encryption.support.DataEncryptionParameters;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.encryption.support.KeyEncryptionParameters;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.KeyInfoSupport;
import org.opensaml.xmlsec.keyinfo.NamedKeyInfoGeneratorManager;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.Signer;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.ldap.SimUser;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.impl.WebSSOProviderImpl;
import com.openexchange.saml.oauth.service.OAuthAccessTokenService;
import com.openexchange.saml.oauth.service.SimOAuthAccessTokenService;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.spi.SAMLBackend;
import com.openexchange.saml.state.SimStateManagement;
import com.openexchange.saml.tools.SAMLLoginTools;
import com.openexchange.saml.tools.SignatureHelper;
import com.openexchange.saml.utils.SecurityHelperUtils;
import com.openexchange.server.SimpleServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.session.reservation.SimSessionReservationService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SimSessiondService;
import com.openexchange.user.SimUserService;
import com.openexchange.user.UserService;
import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

/**
 * {@link SAMLWebSSOProviderOAuthTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class SAMLWebSSOProviderOAuthTest {

    private SAMLWebSSOProvider provider;
    private TestConfig config;
    private OpenSAML openSAML;
    private TestCredentials testCredentials;
    private CredentialProvider credentialProvider;
    private SimStateManagement stateManagement;
    private SimSessionReservationService sessionReservationService;
    private SimSessiondService sessiondService;
    private SimpleServiceLookup services;
    private SimUserService userService;
    private static OAuthAccessTokenService oAuthAccessTokenService;
    private static ParserPool parserPool;

    @BeforeClass
    public static void beforeClass() throws Exception {
        InitializationService.initialize();
        oAuthAccessTokenService = new SimOAuthAccessTokenService();

        // static dependency of c.o.ajax.SessionUtility
        SimConfigurationService simConfigurationService = new SimConfigurationService();
        ServerServiceRegistry.getInstance().addService(ConfigurationService.class, simConfigurationService);
    }

    @Before
    public void setUp() throws Exception {
        testCredentials = new TestCredentials();
        credentialProvider = testCredentials.getSPCredentialProvider();

        parserPool = XMLObjectProviderRegistrySupport.getParserPool();

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
        services.add(OAuthAccessTokenService.class, oAuthAccessTokenService);
        provider = new WebSSOProviderImpl(config, openSAML, stateManagement, services, samlBackend);
    }

    @Test
    public void testLoginRoundtrip() throws Exception {
        /*
         * Trigger AuthnRequest
         */
        String requestHost = "webmail.example.com";
        String requestedLoginPath = "/fancyclient/login.html";
        SimHttpServletRequest loginHTTPRequest = prepareHTTPRequest("GET", new URIBuilder().setScheme("https").setHost(requestHost).setPath("/appsuite/api/saml/init").setParameter("flow", "login").setParameter("loginPath", requestedLoginPath).setParameter("client", "test-client").build());
        URI authnRequestURI = new URI(provider.buildAuthnRequest(loginHTTPRequest, new SimHttpServletResponse()));

        /*
         * Validate redirect location
         */
        String relayState = parseURIQuery(authnRequestURI).get("RelayState");
        Assert.assertNotNull(relayState);
        SimHttpServletRequest authnHTTPRequest = prepareHTTPRequest("GET", authnRequestURI);
        Assert.assertNull(SignatureHelper.validateURISignature(authnHTTPRequest, Collections.singletonList(testCredentials.getSPSigningCredential())));
        AuthnRequest authnRequest = parseAuthnRequest(authnHTTPRequest);

        /*
         * Build response and process it
         */
        Response response = buildResponse(authnRequest);
        SimHttpServletRequest samlResponseRequest = prepareHTTPRequest("POST", new URIBuilder(authnRequest.getAssertionConsumerServiceURL()).setParameter("SAMLResponse", Base64Support.encode(marshall(response).getBytes(), false)).setParameter("RelayState", relayState).build());

        SimHttpServletResponse httpResponse = new SimHttpServletResponse();
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
        SimHttpServletRequest samlResponseRequest = prepareHTTPRequest("POST", new URIBuilder(authnRequest.getAssertionConsumerServiceURL()).setParameter("SAMLResponse", Base64Support.encode(marshall(response).getBytes(), false)).build());

        SimHttpServletResponse httpResponse = new SimHttpServletResponse();

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
        String encodedRelayState = Base64Support.encode(relayStateBuilder.toString().getBytes(), false);

        /*
         * Build response and process it
         */
        Response response = buildResponseWithoutInResponseTo();
        SimHttpServletRequest samlResponseRequest = prepareHTTPRequest("POST", new URIBuilder(authnRequest.getAssertionConsumerServiceURL()).setParameter("SAMLResponse", Base64Support.encode(marshall(response).getBytes(), false)).setParameter("RelayState", encodedRelayState).build());

        SimHttpServletResponse httpResponse = new SimHttpServletResponse();

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
        String encodedRelayState = Base64Support.encode(relayStateBuilder.toString().getBytes(), false);

        /*
         * Build response and process it
         */
        Response response = buildResponseWithoutInResponseTo();
        SimHttpServletRequest samlResponseRequest = prepareHTTPRequest("POST", new URIBuilder(authnRequest.getAssertionConsumerServiceURL()).setParameter("SAMLResponse", Base64Support.encode(marshall(response).getBytes(), false)).setParameter("RelayState", encodedRelayState).build());

        SimHttpServletResponse httpResponse = new SimHttpServletResponse();
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
        HTTPRedirectDeflateDecoder decoder = new HTTPRedirectDeflateDecoder();
        decoder.setParserPool(parserPool);
        decoder.setHttpServletRequest(httpRequest);
        decoder.initialize();
        decoder.decode();

        return (AuthnRequest) decoder.getMessageContext().getMessage();
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
        statusCode.setValue(StatusCode.SUCCESS);
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
        subjectConfirmationData.setNotOnOrAfter(new DateTime(System.currentTimeMillis() + 60 * 60 * 1000));
        subjectConfirmationData.setRecipient(config.getAssertionConsumerServiceURL());
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subject.getSubjectConfirmations().add(subjectConfirmation);
        assertion.setSubject(subject);

        Conditions conditions = openSAML.buildSAMLObject(Conditions.class);
        conditions.setNotBefore(new DateTime(System.currentTimeMillis() - 60 * 1000));
        conditions.setNotOnOrAfter(new DateTime(System.currentTimeMillis() + 60 * 60 * 1000));
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
        SecurityHelperUtils.prepareSignatureParams(assertionSignature, signingCredential);
        assertion.setSignature(assertionSignature);
        openSAML.marshall(assertion); // marshalling is necessary for subsequent signing
        Signer.signObject(assertionSignature);

        EncryptedAssertion encryptedAssertion = getEncrypter().encrypt(assertion);
        response.getEncryptedAssertions().add(encryptedAssertion);

        Signature responseSignature = openSAML.buildSAMLObject(Signature.class);
        responseSignature.setSigningCredential(signingCredential);
        SecurityHelperUtils.prepareSignatureParams(responseSignature, signingCredential);
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
        statusCode.setValue(StatusCode.SUCCESS);
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
        subjectConfirmationData.setNotOnOrAfter(new DateTime(System.currentTimeMillis() + 60 * 60 * 1000));
        subjectConfirmationData.setRecipient(config.getAssertionConsumerServiceURL());
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subject.getSubjectConfirmations().add(subjectConfirmation);
        assertion.setSubject(subject);

        Conditions conditions = openSAML.buildSAMLObject(Conditions.class);
        conditions.setNotBefore(new DateTime(System.currentTimeMillis() - 60 * 1000));
        conditions.setNotOnOrAfter(new DateTime(System.currentTimeMillis() + 60 * 60 * 1000));
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
        SecurityHelperUtils.prepareSignatureParams(assertionSignature, signingCredential);
        assertion.setSignature(assertionSignature);
        openSAML.marshall(assertion); // marshalling is necessary for subsequent signing
        Signer.signObject(assertionSignature);

        EncryptedAssertion encryptedAssertion = getEncrypter().encrypt(assertion);
        response.getEncryptedAssertions().add(encryptedAssertion);

        Signature responseSignature = openSAML.buildSAMLObject(Signature.class);
        responseSignature.setSigningCredential(signingCredential);
        SecurityHelperUtils.prepareSignatureParams(responseSignature, signingCredential);
        response.setSignature(responseSignature);
        openSAML.marshall(response); // marshalling is necessary for subsequent signing
        Signer.signObject(responseSignature);

        return response;
    }

    private String marshall(StatusResponseType response) throws MarshallingException {
        // Never ever use the prettyPrint method! The resulting XML will differ slightly and signature validation will fail!
        return SerializeSupport.nodeToString(openSAML.getMarshallerFactory().getMarshaller(response).marshall(response));
    }

    private Encrypter getEncrypter() throws Exception {
        // https://wiki.shibboleth.net/confluence/display/OpenSAML/OSTwoUserManJavaXMLEncryption
        Credential keyEncryptionCredential = testCredentials.getEncryptionCredential();

        DataEncryptionParameters encParams = new DataEncryptionParameters();
        encParams.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);

        KeyEncryptionParameters kekParams = new KeyEncryptionParameters();
        kekParams.setEncryptionCredential(keyEncryptionCredential);
        kekParams.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
        KeyInfoGenerator keyInfoGenerator = KeyInfoSupport.getKeyInfoGenerator(keyEncryptionCredential, new NamedKeyInfoGeneratorManager(), null);
        kekParams.setKeyInfoGenerator(keyInfoGenerator);

        Encrypter samlEncrypter = new Encrypter(encParams, kekParams);
        samlEncrypter.setKeyPlacement(KeyPlacement.PEER);
        return samlEncrypter;
    }
}
