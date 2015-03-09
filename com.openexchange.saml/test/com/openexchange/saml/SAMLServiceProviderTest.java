package com.openexchange.saml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.sim.SimHttpServletRequest;
import javax.servlet.http.sim.SimHttpServletResponse;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDSAContentSignerBuilder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.binding.security.SAML2HTTPRedirectDeflateSignatureRule;
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
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.DOMMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.security.MetadataCredentialResolver;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.ws.security.SecurityPolicy;
import org.opensaml.ws.security.provider.BasicSecurityPolicy;
import org.opensaml.ws.security.provider.StaticSecurityPolicyResolver;
import org.opensaml.ws.transport.OutputStreamOutTransportAdapter;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.encryption.InlineEncryptedKeyResolver;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.ChainingCredentialResolver;
import org.opensaml.xml.security.credential.StaticCredentialResolver;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.BasicProviderKeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.KeyInfoCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoProvider;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.provider.DEREncodedKeyValueProvider;
import org.opensaml.xml.security.keyinfo.provider.DSAKeyValueProvider;
import org.opensaml.xml.security.keyinfo.provider.InlineX509DataProvider;
import org.opensaml.xml.security.keyinfo.provider.KeyInfoReferenceProvider;
import org.opensaml.xml.security.keyinfo.provider.RSAKeyValueProvider;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.openexchange.java.util.UUIDs;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.session.reservation.SimSessionReservationService;

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

/**
 * {@link SAMLServiceProviderTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SAMLServiceProviderTest {

    private static final String SP_KEY_STORE_PASSWORD = "lkd789013sd";

    private static final String SP_ENCRYPTION_CERT_ALIAS = "sp-encryption-cert";

    private static final String SP_ENCRYPTION_KEY_ALIAS = "sp-encryption-key";

    private static final String SP_ENCRYPTION_KEY_PASSWORD = "fsdafs78";

    private static final String SP_SIGNING_KEY_ALIAS = "sp-signing-key";

    private static final String SP_SIGNING_KEY_PASSWORD = "cpl#al56df";

    private static final String SP_SIGNING_CERT_ALIAS = "sp-signing-cert";

    private static final String IDP_KEY_STORE_PASSWORD = "dsjk546565";

    private static final String IDP_SIGNING_KEY_ALIAS = "idp-signing-key";

    private static final String IDP_SIGNING_KEY_PASSWORD = "jh!65gaasa";

    private static final String IDP_SIGNING_CERT_ALIAS = "idp-signing-cert";


    /**
     * KeyStore of the IDP. Contains:
     * <ul>
     * <li>A certificate of the SP for encrypting response data</li>
     * <li>A certificate of the SP to validate signed request data</li>
     * <li>A private key for signing response data</li>
     * </ul>
     */
    private KeyStore idpKeyStore;

    /**
     * KeyStore of the SP. Contains:
     * <ul>
     * <li>A private key for decrypting IDP response data</li>
     * <li>A private key for signing request data</li>
     * <li>A certificate of the IDP to validate signed response data</li>
     * </ul>
     */
    private KeyStore spKeyStore;

    private File spKeyStoreFile;

    @Before
    public void setup() throws Exception {
        KeyPairGenerator dsaGenerator = KeyPairGenerator.getInstance("DSA");
        dsaGenerator.initialize(1024);
        KeyPairGenerator rsaGenerator = KeyPairGenerator.getInstance("RSA");
        rsaGenerator.initialize(4096);

        idpKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        idpKeyStore.load(null, IDP_KEY_STORE_PASSWORD.toCharArray());

        spKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        spKeyStore.load(null, SP_KEY_STORE_PASSWORD.toCharArray());

        /*
         * Build SPs encryption key pair and certificate
         */
        KeyPair spEncryptionKeyPair = rsaGenerator.generateKeyPair();
        Certificate spEncryptionCert = getCertificateForKeyPair(spEncryptionKeyPair);
        spKeyStore.setKeyEntry(SP_ENCRYPTION_KEY_ALIAS, spEncryptionKeyPair.getPrivate(), SP_ENCRYPTION_KEY_PASSWORD.toCharArray(), new Certificate[] { spEncryptionCert });
        idpKeyStore.setCertificateEntry(SP_ENCRYPTION_CERT_ALIAS, spEncryptionCert);

        /*
         * Build SPs signing key pair and certificate
         */
        KeyPair spSigningKeyPair = dsaGenerator.generateKeyPair();
        Certificate spSigningCert = getCertificateForKeyPair(spSigningKeyPair);
        spKeyStore.setKeyEntry(SP_SIGNING_KEY_ALIAS, spSigningKeyPair.getPrivate(), SP_SIGNING_KEY_PASSWORD.toCharArray(), new Certificate[] { spSigningCert });
        idpKeyStore.setCertificateEntry(SP_SIGNING_CERT_ALIAS, spSigningCert);

        /*
         * Build IDPs signing key pair and certificate
         */
        KeyPair idpSigningKeyPair = dsaGenerator.generateKeyPair();
        Certificate idpSigningCert = getCertificateForKeyPair(idpSigningKeyPair);
        idpKeyStore.setKeyEntry(IDP_SIGNING_KEY_ALIAS, idpSigningKeyPair.getPrivate(), IDP_SIGNING_KEY_PASSWORD.toCharArray(), new Certificate[] { idpSigningCert });
        spKeyStore.setCertificateEntry(IDP_SIGNING_CERT_ALIAS, idpSigningCert);

        idpKeyStoreFile = File.createTempFile("idpKeyStore", ".jks");
        idpKeyStoreFile.deleteOnExit();
        idpKeyStore.store(new FileOutputStream(idpKeyStoreFile), IDP_KEY_STORE_PASSWORD.toCharArray());

        spKeyStoreFile = File.createTempFile("spKeyStore", ".jks");
        spKeyStoreFile.deleteOnExit();
        spKeyStore.store(new FileOutputStream(spKeyStoreFile), SP_KEY_STORE_PASSWORD.toCharArray());

        /*
         * Init service provider
         */
        config = new TestConfig();
        openSAML = new OpenSAML();
        serviceProvider = new SAMLServiceProvider(config, openSAML, new TestResponseHandler(), new SimSessionReservationService());
        serviceProvider.init();
    }

    @After
    public void cleanUp() throws Exception {
        if (idpKeyStoreFile != null) {
            idpKeyStoreFile.delete();
        }

        if (spKeyStoreFile != null) {
            spKeyStoreFile.delete();
        }
    }

    private SAMLServiceProvider serviceProvider;
    private TestConfig config;
    private OpenSAML openSAML;

    private static final class TestConfig implements SAMLConfig {


        private TestConfig() {
            super();
        }

        @Override
        public String getProviderName() {
            return "http://test.saml.open-xchange.com";
        }

        @Override
        public String getEntityID() {
            return "http://test.saml.open-xchange.com";
        }

        @Override
        public String getAssertionConsumerServiceURL() {
            return "http://test.saml.open-xchange.com/ajax/saml/acs";
        }

        @Override
        public Binding getResponseBinding() {
            return Binding.HTTP_POST;
        }

        @Override
        public String getIdentityProviderURL() {
            return "https://idp.testshib.org/idp/profile/SAML2/Redirect/SSO";
        }

        @Override
        public boolean supportSingleLogout() {
            return false;
        }

        @Override
        public String getSingleLogoutServiceURL() {
            return "http://test.saml.open-xchange.com/ajax/saml/logout";
        }

        @Override
        public String getIdentityProviderEntityID() {
            return "https://idp.testshib.org/idp/shibboleth";
        }

        @Override
        public Binding getRequestBinding() {
            return Binding.HTTP_REDIRECT;
        }

    }

    @BeforeClass
    public static void beforeClass() throws ConfigurationException {
        DefaultBootstrap.bootstrap();
    }

    private File idpKeyStoreFile;

    @Test
    public void testMetadata() throws Exception {
        String metadataXML = serviceProvider.getMetadataXML();
        System.out.println(metadataXML);
    }

    @Test
    public void testWSO2IDP() throws Exception {
        SimHttpServletRequest httpRequest = new SimHttpServletRequest();
        SimHttpServletResponse httpResponse = new SimHttpServletResponse();
        serviceProvider.respondWithAuthnRequest(httpRequest, httpResponse);
        Assert.assertEquals(HttpServletResponse.SC_FOUND, httpResponse.getStatus());
        String location = httpResponse.getHeader("location");
        System.out.println(location);
    }

    @Test
    public void testResponseValidation() throws Exception {
        SimHttpServletRequest samlResponseRequest = new SimHttpServletRequest();
        samlResponseRequest.setRequestURI(new URI(config.getAssertionConsumerServiceURL()).getPath());
        samlResponseRequest.setRequestURL(config.getAssertionConsumerServiceURL());
        samlResponseRequest.setMethod("POST");
        samlResponseRequest.setParameter("SAMLResponse", Base64.encodeBytes(buildResponse(false).getBytes()));

        HttpServletRequestAdapter inTransport = new HttpServletRequestAdapter(samlResponseRequest);
        BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject> context = new BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject>();
        context.setCommunicationProfileId("urn:oasis:names:tc:SAML:2.0:profiles:SSO:browser");
        context.setInboundMessageTransport(inTransport);
        context.setInboundSAMLProtocol(SAMLConstants.SAML20P_NS);
        context.setPeerEntityRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        context.setOutboundMessageTransport(new OutputStreamOutTransportAdapter(output));
        context.setOutboundSAMLProtocol(SAMLConstants.SAML20P_NS);

        HTTPPostDecoder decoder = new HTTPPostDecoder();
        decoder.decode(context);
        Response samlMessage = (Response) context.getInboundSAMLMessage();

        List<Assertion> assertions = new LinkedList<Assertion>();

        boolean testEncryption = false;
        if (testEncryption) {
            System.out.println("Encrypted Response:");
            System.out.println(XMLHelper.prettyPrintXML(samlMessage.getDOM()));
            List<EncryptedAssertion> encryptedAssertions = samlMessage.getEncryptedAssertions();
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(new FileInputStream(spKeyStoreFile.getAbsolutePath()), SP_KEY_STORE_PASSWORD.toCharArray());
            BasicCredential decryptCredential = new BasicCredential();
            decryptCredential.setUsageType(UsageType.ENCRYPTION);
            PrivateKeyEntry entry = (PrivateKeyEntry) keystore.getEntry(SP_ENCRYPTION_KEY_ALIAS, new PasswordProtection(SP_ENCRYPTION_KEY_ALIAS.toCharArray()));
            decryptCredential.setPrivateKey(entry.getPrivateKey());
            StaticKeyInfoCredentialResolver skicr = new StaticKeyInfoCredentialResolver(decryptCredential);

            Decrypter samlDecrypter = new Decrypter(null, skicr, new InlineEncryptedKeyResolver());
            samlDecrypter.setRootInNewDocument(true);
            Assert.assertEquals(1, encryptedAssertions.size());

            for (EncryptedAssertion encryptedAssertion : encryptedAssertions) {
                Assertion assertion = samlDecrypter.decrypt(encryptedAssertion);
                assertions.add(assertion);
            }
        }

        assertions.addAll(samlMessage.getAssertions());
        SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
        BasicCredential idpSigningCredential = new BasicCredential();
        idpSigningCredential.setEntityId("https://idp.testshib.org/idp/shibboleth");
        idpSigningCredential.setPublicKey(spKeyStore.getCertificate(IDP_SIGNING_CERT_ALIAS).getPublicKey());
        idpSigningCredential.setUsageType(UsageType.SIGNING);
        SignatureValidator signatureValidator = new SignatureValidator(idpSigningCredential);
        for (Assertion assertion : assertions) {
//            Element assertionElement = assertion.getDOM();
//            Document ownerDocument = assertionElement.getOwnerDocument();
//            System.out.println("Assertion:");
//            System.out.println(XMLHelper.prettyPrintXML(assertionElement));
//            System.out.println("Owner DOC:");
//            System.out.println(XMLHelper.prettyPrintXML(ownerDocument));

            Assert.assertTrue(assertion.isSigned());
            Signature signature = assertion.getSignature();
            profileValidator.validate(signature);
            signatureValidator.validate(signature);
        }
    }

    @Test
    public void testRedirectRequest() throws Exception {
        SimHttpServletRequest httpRequest = new SimHttpServletRequest();
        SimHttpServletResponse httpResponse = new SimHttpServletResponse();
        serviceProvider.respondWithAuthnRequest(httpRequest, httpResponse);
        Assert.assertEquals(HttpServletResponse.SC_FOUND, httpResponse.getStatus());
        String location = httpResponse.getHeader("location");
        System.out.println(location);

        XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
        AssertionConsumerService acs = (AssertionConsumerService) builderFactory.getBuilder(AssertionConsumerService.DEFAULT_ELEMENT_NAME).buildObject(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
        acs.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
        acs.setLocation(config.getAssertionConsumerServiceURL());
        acs.setResponseLocation(config.getIdentityProviderURL());

        SimHttpServletRequest redirectRequest = new SimHttpServletRequest();
        redirectRequest.setRequestURI(location.substring(0, location.indexOf('?')));
        redirectRequest.setRequestURL(redirectRequest.getRequestURI());
        redirectRequest.setMethod("GET");
        redirectRequest.setQueryString(location.substring(location.indexOf('?') + 1));
        for (String kv : new URI(location).getQuery().split("&")) {
            int i = kv.indexOf('=');
            redirectRequest.setParameter(kv.substring(0, i), kv.substring(i + 1));
        }

        DOMMetadataProvider metadataProvider = new DOMMetadataProvider(openSAML.getParserPool().parse(new StringReader(serviceProvider.getMetadataXML())).getDocumentElement());
        metadataProvider.initialize();

        HttpServletRequestAdapter inTransport = new HttpServletRequestAdapter(redirectRequest);
        BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject> context = new BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject>();
        context.setCommunicationProfileId("urn:oasis:names:tc:SAML:2.0:profiles:SSO:browser");
        context.setMetadataProvider(metadataProvider);
        context.setSecurityPolicyResolver(new StaticSecurityPolicyResolver(getSecurityPolicy(metadataProvider)));
        context.setInboundMessageTransport(inTransport);
        context.setInboundSAMLProtocol(SAMLConstants.SAML20P_NS);
        context.setPeerEntityRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        context.setOutboundMessageTransport(new OutputStreamOutTransportAdapter(output));
        context.setOutboundSAMLProtocol(SAMLConstants.SAML20P_NS);

        HTTPRedirectDeflateDecoder decoder = new HTTPRedirectDeflateDecoder();
        decoder.decode(context);
        System.out.println(new String(output.toByteArray()));


//        EntityDescriptor entityDescriptor = metadataProvider.getEntityDescriptor("https://localhost:9443/samlsso");
//        SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
//        profileValidator.validate(entityDescriptor.getSignature());

//        SignatureValidator sigValidator = new SignatureValidator(cred);

//        sigValidator.validate(entityDescriptor.getSignature());

//        BasicCredential credential = new BasicCredential();
//        credential.setPublicKey(SAMLAuthenticator.KEY_PAIR.getPublic());
//        StaticKeyInfoCredentialResolver keyInfoResolver = new StaticKeyInfoCredentialResolver(credential);
//        ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(keyInfoResolver, keyInfoResolver);
//        SAML2HTTPRedirectDeflateSignatureRule rule = new SAML2HTTPRedirectDeflateSignatureRule(trustEngine);
//        BasicSecurityPolicy policy = new BasicSecurityPolicy();
//        policy.getPolicyRules().add(rule);
//        context.setSecurityPolicyResolver(new StaticSecurityPolicyResolver(policy));
//        context.setRelayState(state); // TODO
    //      context.setOutboundSAMLMessageSigningCredential(getSigningCredential()); // TODO
//
//
//        XMLObject inboundMessage = context.getInboundMessage();
//        Element marshall = Configuration.getMarshallerFactory().getMarshaller(AuthnRequest.DEFAULT_ELEMENT_NAME).marshall(context.getInboundMessage());
//        System.out.println(XMLHelper.prettyPrintXML(marshall));





//        ByteArrayServletOutputStream output = new ByteArrayServletOutputStream();
//        httpResponse.setOutputStream(output);
//        output.close();
//        byte[] byteArray = output.toByteArray();
    }

    @Test
    public void testHandlePOSTAuthnResponse() throws Exception {
        String responseXML = buildResponse(true);
        SimHttpServletRequest samlResponseRequest = new SimHttpServletRequest();
        samlResponseRequest.setRequestURI(new URI(config.getAssertionConsumerServiceURL()).getPath());
        samlResponseRequest.setRequestURL(config.getAssertionConsumerServiceURL());
        samlResponseRequest.setMethod("POST");
        samlResponseRequest.setParameter("SAMLResponse", Base64.encodeBytes(responseXML.getBytes("UTF-8"), Base64.DONT_BREAK_LINES));
        samlResponseRequest.setParameter("RelayState", "example.com");
        serviceProvider.handleAuthnResponse(samlResponseRequest, new SimHttpServletResponse(), Binding.HTTP_POST);
    }

    @Test
    public void testSignatureValidationWithInlineCertificate() throws Exception {
        String responseXMLWithCert = buildResponse(true);
        Element responseXMLWithCertElement = openSAML.getParserPool().parse(new StringReader(responseXMLWithCert)).getDocumentElement();
        Response responseWithCert = (Response) openSAML.getUnmarshallerFactory().getUnmarshaller(responseXMLWithCertElement).unmarshall(responseXMLWithCertElement);

        List<KeyInfoProvider> keyInfoProviders = new ArrayList<KeyInfoProvider>(4);
        keyInfoProviders.add(new InlineX509DataProvider());
        keyInfoProviders.add(new KeyInfoReferenceProvider());
        keyInfoProviders.add(new DEREncodedKeyValueProvider());
        keyInfoProviders.add(new RSAKeyValueProvider());
        keyInfoProviders.add(new DSAKeyValueProvider());
        BasicProviderKeyInfoCredentialResolver keyInfoCredentialResolver = new BasicProviderKeyInfoCredentialResolver(keyInfoProviders);

        BasicCredential keyStoreCertificate = new BasicCredential();
        keyStoreCertificate.setUsageType(UsageType.SIGNING);
        keyStoreCertificate.setPublicKey(spKeyStore.getCertificate(IDP_SIGNING_CERT_ALIAS).getPublicKey());

        ChainingCredentialResolver credentialResolver = new ChainingCredentialResolver();
        credentialResolver.getResolverChain().add(keyInfoCredentialResolver);
        credentialResolver.getResolverChain().add(new StaticCredentialResolver(keyStoreCertificate));

        Assertion assertionWithCert = responseWithCert.getAssertions().get(0);
        Assert.assertTrue(assertionWithCert.isSigned());

        new SAMLSignatureProfileValidator().validate(assertionWithCert.getSignature());
        new SignatureValidator(credentialResolver.resolveSingle(new CriteriaSet(new KeyInfoCriteria(assertionWithCert.getSignature().getKeyInfo())))).validate(assertionWithCert.getSignature());
    }

    @Test
    public void testSignatureValidationWithoutInlineCertificate() throws Exception {
        String responseXML = buildResponse(false);
        Element responseElement = openSAML.getParserPool().parse(new StringReader(responseXML)).getDocumentElement();
        Response response = (Response) openSAML.getUnmarshallerFactory().getUnmarshaller(responseElement).unmarshall(responseElement);

        List<KeyInfoProvider> keyInfoProviders = new ArrayList<KeyInfoProvider>(4);
        keyInfoProviders.add(new InlineX509DataProvider());
        keyInfoProviders.add(new KeyInfoReferenceProvider());
        keyInfoProviders.add(new DEREncodedKeyValueProvider());
        keyInfoProviders.add(new RSAKeyValueProvider());
        keyInfoProviders.add(new DSAKeyValueProvider());
        BasicProviderKeyInfoCredentialResolver keyInfoCredentialResolver = new BasicProviderKeyInfoCredentialResolver(keyInfoProviders);

        BasicCredential keyStoreCertificate = new BasicCredential();
        keyStoreCertificate.setUsageType(UsageType.SIGNING);
        keyStoreCertificate.setPublicKey(spKeyStore.getCertificate(IDP_SIGNING_CERT_ALIAS).getPublicKey());

        ChainingCredentialResolver credentialResolver = new ChainingCredentialResolver();
        credentialResolver.getResolverChain().add(keyInfoCredentialResolver);
        credentialResolver.getResolverChain().add(new StaticCredentialResolver(keyStoreCertificate));

        Assertion assertion = response.getAssertions().get(0);
        Assert.assertTrue(assertion.isSigned());

        new SAMLSignatureProfileValidator().validate(assertion.getSignature());
        new SignatureValidator(credentialResolver.resolveSingle(new CriteriaSet(new KeyInfoCriteria(assertion.getSignature().getKeyInfo())))).validate(assertion.getSignature());
    }

    private String buildResponse(boolean includeCert) throws Exception {
        Response response = openSAML.buildSAMLObject(Response.class);
        response.setDestination(config.getAssertionConsumerServiceURL());
        response.setID("_" + UUIDs.getUnformattedString(UUID.randomUUID()));
        response.setInResponseTo("_" + UUIDs.getUnformattedString(UUID.randomUUID()));
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
        assertion.setID("_" + UUIDs.getUnformattedString(UUID.randomUUID()));
        assertion.setIssueInstant(new DateTime());

        Issuer assertionIssuer = openSAML.buildSAMLObject(Issuer.class);
        assertionIssuer.setValue(config.getIdentityProviderEntityID());
        assertion.setIssuer(assertionIssuer);

        Subject subject = openSAML.buildSAMLObject(Subject.class);
        NameID nameID = openSAML.buildSAMLObject(NameID.class);
        nameID.setFormat(NameIDType.TRANSIENT);
        nameID.setValue("_" + UUIDs.getUnformattedString(UUID.randomUUID()));
        subject.setNameID(nameID);

        SubjectConfirmation subjectConfirmation = openSAML.buildSAMLObject(SubjectConfirmation.class);
        subjectConfirmation.setMethod("urn:oasis:names:tc:SAML:2.0:cm:bearer");
        SubjectConfirmationData subjectConfirmationData = openSAML.buildSAMLObject(SubjectConfirmationData.class);
        subjectConfirmationData.setAddress("10.20.30.1");
        subjectConfirmationData.setInResponseTo("_" + UUIDs.getUnformattedString(UUID.randomUUID()));
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
        authnStatement.setSessionIndex("_" + UUIDs.getUnformattedString(UUID.randomUUID()));
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


        PrivateKeyEntry signingKeyEntry = (PrivateKeyEntry) idpKeyStore.getEntry(IDP_SIGNING_KEY_ALIAS, new PasswordProtection(IDP_SIGNING_KEY_PASSWORD.toCharArray()));
        BasicX509Credential signingCredential = new BasicX509Credential();
        signingCredential.setUsageType(UsageType.SIGNING);
        signingCredential.setPrivateKey(signingKeyEntry.getPrivateKey());
        if (includeCert) {
            Certificate certificate = signingKeyEntry.getCertificate();
            signingCredential.setEntityCertificate((java.security.cert.X509Certificate) certificate);
            signingCredential.setEntityCertificateChain(Arrays.asList((java.security.cert.X509Certificate[]) signingKeyEntry.getCertificateChain()));
        }

        Signature signature = openSAML.buildSAMLObject(Signature.class);
        signature.setSigningCredential(signingCredential);
        SecurityHelper.prepareSignatureParams(signature, signingCredential, null, null);

        assertion.setSignature(signature);
        openSAML.getMarshallerFactory().getMarshaller(assertion).marshall(assertion); // marshalling is necessary for subsequent signing
        Signer.signObject(signature);

        response.getAssertions().add(assertion);
        // Never ever use the prettyPrint method! The resulting XML will differ slightly and signature validation will fail!
        return XMLHelper.nodeToString(openSAML.getMarshallerFactory().getMarshaller(response).marshall(response));
    }

    private SecurityPolicy getSecurityPolicy(MetadataProvider metadataProvider) {
        List<KeyInfoProvider> keyInfoProviders = new ArrayList<KeyInfoProvider>(4);
        keyInfoProviders.add(new InlineX509DataProvider());
        keyInfoProviders.add(new KeyInfoReferenceProvider());
        BasicProviderKeyInfoCredentialResolver keyInfoCredentialResolver = new BasicProviderKeyInfoCredentialResolver(keyInfoProviders);
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolver(metadataProvider);
        ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(metadataCredentialResolver, keyInfoCredentialResolver);
        SAML2HTTPRedirectDeflateSignatureRule rule = new SAML2HTTPRedirectDeflateSignatureRule(trustEngine);
        BasicSecurityPolicy policy = new BasicSecurityPolicy();
        policy.getPolicyRules().add(rule);
        return policy;
    }

    private static String print(Element element, int indent) {
        StringBuilder sb = new StringBuilder();
        if (indent > 0) {
            sb.append('\n');
        }
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
        sb.append('<');
        String prefix = element.getPrefix();
        if (prefix != null) {
            sb.append(prefix).append(':');
        }
        sb.append(element.getNodeName());
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr item = (Attr) attributes.item(i);
            sb.append(' ').append(item.getName()).append("=\"").append(item.getValue()).append("\"");
        }
        sb.append('>').append('\n');

        if (element.hasChildNodes()) {
            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (item instanceof Element) {
                    sb.append(print((Element) item, indent + 2));
                }
            }
        } else {
            for (int i = 0; i < indent + 2; i++) {
                sb.append(' ');
            }
            sb.append(element.getTextContent());
        }

        sb.append('\n');
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
        sb.append("</");
        if (prefix != null) {
            sb.append(prefix).append(':');
        }
        sb.append(element.getNodeName()).append('>');
        return sb.toString();
    }

    private static Certificate getCertificateForKeyPair(KeyPair keyPair) throws OperatorCreationException, IOException, CertificateException {
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1with" + keyPair.getPrivate().getAlgorithm());
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        SubjectPublicKeyInfo keyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

        X500Name issuer = new X500Name("C=DE, ST=NRW, L=Olpe, O=Open-Xchange GmbH, OU=Engineering, CN=Steffen Templin");
        X509v3CertificateBuilder myCertificateGenerator = new X509v3CertificateBuilder(
            issuer,
            new BigInteger(Long.toString(System.nanoTime())),
            new Date(System.currentTimeMillis()),
            new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000),
            issuer,
            keyInfo);

        ContentSigner sigGen;
        AsymmetricKeyParameter privateKey = PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());
        if (keyPair.getPrivate().getAlgorithm().equals("RSA")) {
            sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(privateKey);
        } else {
            sigGen = new BcDSAContentSignerBuilder(sigAlgId, digAlgId).build(privateKey);
        }

        X509CertificateHolder holder = myCertificateGenerator.build(sigGen);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // Read Certificate
        InputStream is = new ByteArrayInputStream(holder.getEncoded());
        try {
            return cf.generateCertificate(is);
        } finally {
            is.close();
        }
    }

}
