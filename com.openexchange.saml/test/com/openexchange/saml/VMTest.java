package com.openexchange.saml;

import java.net.URI;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;
import javax.servlet.http.sim.SimHttpServletRequest;
import javax.servlet.http.sim.SimHttpServletResponse;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import com.openexchange.java.util.UUIDs;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.spi.KeySpecCredentialProvider;
import com.openexchange.saml.spi.KeySpecCredentialProvider.Algorithm;
import com.openexchange.session.reservation.SimSessionReservationService;


public class VMTest {
    
    private static final String VM_PUBLIC_KEY = 
        "MIHwMIGoBgcqhkjOOAQBMIGcAkEA/KaCzo4Syrom78z3EQ5SbbB4sF7ey80etKII864WF64B81uR" + 
        "pH5t9jQTxeEu0ImbzRMqzVDZkVG9xD7nN1kuFwIVAJYu3cw2nLqOuyYO5rahJtk0bjjFAkBnhHGy" + 
        "epz0TukaScUUfbGpqvJE8FpDTWSGkx0tFCcbnjUDC3H9c9oXkGmzLik1Yw4cIGI1TQ2iCmxBblC+" + 
        "eUykA0MAAkBUyhX/86HtXK0FtlXJI2ZVl69XVvDu+YnDHmEezN5Hblt+isEUDX2eT7d6GDpKBSNa" + 
        "JefsezJt6aNEWznHj5cW";
    
    private static final String RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<samlp:Response xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" xmlns=\"urn:oasis:names:tc:SAML:2.0:assertion\" xmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\" ID=\"kcnacddlekdnkfkbmmajpflmildebgmgaokeohof\" IssueInstant=\"2015-03-05T13:14:24Z\" Version=\"2.0\"><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments\" /><SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#dsa-sha1\" /><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\" /></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\" /><DigestValue>4gZOQiq+JqBdbCtEXr0kXMqcLeM=</DigestValue></Reference></SignedInfo><SignatureValue>ftNqJ7qETLi8Dy8gdiwA8sPsQIIMFMmcB8ReoCckr1rJO5fkQ6w/1A==</SignatureValue><KeyInfo><KeyValue><DSAKeyValue><P>/KaCzo4Syrom78z3EQ5SbbB4sF7ey80etKII864WF64B81uRpH5t9jQTxeEu0ImbzRMqzVDZkVG9xD7nN1kuFw==</P><Q>li7dzDacuo67Jg7mtqEm2TRuOMU=</Q><G>Z4Rxsnqc9E7pGknFFH2xqaryRPBaQ01khpMdLRQnG541Awtx/XPaF5Bpsy4pNWMOHCBiNU0NogpsQW5QvnlMpA==</G><Y>VMoV//Oh7VytBbZVySNmVZevV1bw7vmJwx5hHszeR25bforBFA19nk+3ehg6SgUjWiXn7HsybemjRFs5x4+XFg==</Y></DSAKeyValue></KeyValue></KeyInfo></Signature><samlp:Status><samlp:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\" /></samlp:Status><saml:Assertion xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\" ID=\"koeoppjjbfhlfplijmmojgigfafjfknanfgflijo\" IssueInstant=\"2003-04-17T00:46:02Z\" Version=\"2.0\"><saml:Issuer>virginmedia.com</saml:Issuer><saml:Subject><saml:NameID Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:emailAddress\">pradeepks@gmail.com</saml:NameID><saml:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\"><saml:SubjectConfirmationData Recipient=\"http://serviceprovidertestapp.virginmedia.com:9080/serviceprovidertest/serviceProvider/acs?issuerid=5\" /></saml:SubjectConfirmation></saml:Subject><saml:Conditions NotBefore=\"2015-03-05T14:13:24Z\" NotOnOrAfter=\"2015-03-05T16:15:24Z\" /><saml:AuthnStatement AuthnInstant=\"2015-03-05T15:14:24Z\"><saml:AuthnContext><saml:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:Password</saml:AuthnContextClassRef></saml:AuthnContext></saml:AuthnStatement><saml:AttributeStatement><saml:Attribute Name=\"vmLoginID\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:basic\"><saml:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">green.grass@virginmedia.com</saml:AttributeValue></saml:Attribute></saml:AttributeStatement></saml:Assertion></samlp:Response>";
    
    private static final SAMLConfig CONFIG = new SAMLConfig() {

        @Override
        public String getProviderName() {
            return "Ziggo VM Mail";
        }

        @Override
        public String getEntityID() {
            return "ziggo.com";
        }

        @Override
        public String getAssertionConsumerServiceURL() {
            return "http://serviceprovidertestapp.virginmedia.com:9080/serviceprovidertest/serviceProvider/acs?issuerid=5";
        }

        @Override
        public Binding getRequestBinding() {
            return Binding.HTTP_REDIRECT;
        }

        @Override
        public Binding getResponseBinding() {
            return Binding.HTTP_POST;
        }

        @Override
        public String getIdentityProviderURL() {
            return "https://identity.a.itp2.virginmedia.com/vm_sso/idp/requestAssertion.action";
        }

        @Override
        public boolean supportSingleLogout() {
            return true;
        }

        @Override
        public String getSingleLogoutServiceURL() {
            return "http://localhost/ajax/saml/sls";
        }

        @Override
        public String getIdentityProviderEntityID() {
            return "virginmedia.com";
        }
        
    };

    private static OpenSAML openSAML;
    
    private static CredentialProvider credentialProvider;
    
    @BeforeClass
    public static void setUp() throws Exception {
        DefaultBootstrap.bootstrap();
        openSAML = new OpenSAML();
        credentialProvider = KeySpecCredentialProvider.newInstance(new X509EncodedKeySpec(Base64.decode(VM_PUBLIC_KEY)), Algorithm.DSA, null, null, null, null);
    }
    
    @Test
    public void testVMResponse() throws Exception {
        SimHttpServletRequest samlResponseRequest = new SimHttpServletRequest();
        samlResponseRequest.setRequestURI(new URI(CONFIG.getAssertionConsumerServiceURL()).getPath());
        samlResponseRequest.setRequestURL(CONFIG.getAssertionConsumerServiceURL());
        samlResponseRequest.setMethod("POST");
        samlResponseRequest.setParameter("SAMLResponse", Base64.encodeBytes(RESPONSE.getBytes()));
        
        SAMLWebSSOProvider serviceProvider = new SAMLWebSSOProvider(CONFIG, openSAML, new TestResponseHandler(), new SimSessionReservationService());
        serviceProvider.setCredentialProvider(credentialProvider);
        serviceProvider.handleAuthnResponse(samlResponseRequest, new SimHttpServletResponse(), Binding.HTTP_POST);
    }
    
    private String buildResponse(boolean includeCert) throws Exception {
        Response response = openSAML.buildSAMLObject(Response.class);
        response.setDestination(CONFIG.getAssertionConsumerServiceURL());
        response.setID("_" + UUIDs.getUnformattedString(UUID.randomUUID()));
        response.setInResponseTo("_" + UUIDs.getUnformattedString(UUID.randomUUID()));
        response.setIssueInstant(new DateTime());
        response.setVersion(SAMLVersion.VERSION_20);

        Issuer responseIssuer = openSAML.buildSAMLObject(Issuer.class);
        responseIssuer.setValue(CONFIG.getIdentityProviderEntityID());
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
        assertionIssuer.setValue(CONFIG.getIdentityProviderEntityID());
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
        subjectConfirmationData.setRecipient(CONFIG.getAssertionConsumerServiceURL());
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subject.getSubjectConfirmations().add(subjectConfirmation);
        assertion.setSubject(subject);

        Conditions conditions = openSAML.buildSAMLObject(Conditions.class);
        conditions.setNotBefore(new DateTime(System.currentTimeMillis() - 60 * 1000));
        conditions.setNotOnOrAfter(new DateTime(System.currentTimeMillis() + 60 *60 * 1000));
        AudienceRestriction audienceRestriction = openSAML.buildSAMLObject(AudienceRestriction.class);
        Audience audience = openSAML.buildSAMLObject(Audience.class);
        audience.setAudienceURI(CONFIG.getEntityID());
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


//        PrivateKeyEntry signingKeyEntry = (PrivateKeyEntry) idpKeyStore.getEntry(IDP_SIGNING_KEY_ALIAS, new PasswordProtection(IDP_SIGNING_KEY_PASSWORD.toCharArray()));
//        BasicX509Credential signingCredential = new BasicX509Credential();
//        signingCredential.setUsageType(UsageType.SIGNING);
//        signingCredential.setPrivateKey(signingKeyEntry.getPrivateKey());
//        if (includeCert) {
//            Certificate certificate = signingKeyEntry.getCertificate();
//            signingCredential.setEntityCertificate((java.security.cert.X509Certificate) certificate);
//            signingCredential.setEntityCertificateChain(Arrays.asList((java.security.cert.X509Certificate[]) signingKeyEntry.getCertificateChain()));
//        }
//
//        Signature signature = openSAML.buildSAMLObject(Signature.class);
//        signature.setSigningCredential(signingCredential);
//        SecurityHelper.prepareSignatureParams(signature, signingCredential, null, null);
//
//        assertion.setSignature(signature);
//        openSAML.getMarshallerFactory().getMarshaller(assertion).marshall(assertion); // marshalling is necessary for subsequent signing
//        Signer.signObject(signature);

        response.getAssertions().add(assertion);
        // Never ever use the prettyPrint method! The resulting XML will differ slightly and signature validation will fail!
        return XMLHelper.nodeToString(openSAML.getMarshallerFactory().getMarshaller(response).marshall(response));
    }

}
