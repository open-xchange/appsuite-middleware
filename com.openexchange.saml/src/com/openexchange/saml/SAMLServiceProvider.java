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

package com.openexchange.saml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.decoding.SAMLMessageDecoder;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.saml2.encryption.EncryptedElementTypeEncryptedKeyResolver;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.NameIDFormat;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.encryption.ChainingEncryptedKeyResolver;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.encryption.InlineEncryptedKeyResolver;
import org.opensaml.xml.encryption.SimpleKeyInfoReferenceEncryptedKeyResolver;
import org.opensaml.xml.encryption.SimpleRetrievalMethodEncryptedKeyResolver;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.Criteria;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SigningUtil;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.KeyStoreCredentialResolver;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.keyinfo.KeyInfoProvider;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.provider.DEREncodedKeyValueProvider;
import org.opensaml.xml.security.keyinfo.provider.DSAKeyValueProvider;
import org.opensaml.xml.security.keyinfo.provider.InlineX509DataProvider;
import org.opensaml.xml.security.keyinfo.provider.KeyInfoReferenceProvider;
import org.opensaml.xml.security.keyinfo.provider.RSAKeyValueProvider;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Element;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.spi.ServiceProviderCustomizer;
import com.openexchange.saml.spi.ServiceProviderCustomizer.RequestContext;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link #init()} must be called before usage!
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class SAMLServiceProvider {

    private final SAMLConfig config;

    private final OpenSAML openSAML;

    private volatile ServiceProviderCustomizer customizer;

    private KeyStore keystore;

    private Credential signingCredential;

    private Credential encryptionCredential;

    private Credential idpCertificateCredential;

    private boolean initialized;

    private KeyStoreCredentialResolver keyStoreCredentialResolver;

    public SAMLServiceProvider(SAMLConfig config, OpenSAML openSAML) throws OXException {
        super();
        this.config = config;
        this.openSAML = openSAML;
        initialized = false;
    }

    public synchronized void init() throws OXException {
        initKeystore();
        initCredentials();
        initialized = true;
    }

    private void initKeystore() throws OXException {
        if (config.signAuthnRequest() || config.wantAssertionsSigned()) {
            String keyStorePath = config.getKeyStorePath();
            if (Strings.isEmpty(keyStorePath)) {
                throw SAMLExceptionCode.KEYSTORE_PROBLEM.create("No keystore path was set");
            }

            File keyStoreFile = new File(keyStorePath);
            if (!keyStoreFile.exists() || !keyStoreFile.isFile()) {
                throw SAMLExceptionCode.KEYSTORE_PROBLEM.create("The keystore path " + keyStorePath + " points to an invalid file");
            }

            if (!keyStoreFile.canRead()) {
                throw SAMLExceptionCode.KEYSTORE_PROBLEM.create("The keystore file " + keyStorePath + " is not readable");
            }

            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(keyStoreFile);
                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                String keyStorePassword = config.getKeyStorePassword();
                keystore.load(inputStream, keyStorePassword == null ? null : keyStorePassword.toCharArray());
                this.keystore = keystore;
            } catch (KeyStoreException e) {
                throw SAMLExceptionCode.KEYSTORE_PROBLEM.create(e, e.getMessage());
            } catch (FileNotFoundException e) {
                throw SAMLExceptionCode.KEYSTORE_PROBLEM.create(e, e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                throw SAMLExceptionCode.KEYSTORE_PROBLEM.create(e, e.getMessage());
            } catch (CertificateException e) {
                throw SAMLExceptionCode.KEYSTORE_PROBLEM.create(e, e.getMessage());
            } catch (IOException e) {
                throw SAMLExceptionCode.KEYSTORE_PROBLEM.create(e, e.getMessage());
            } finally {
                Streams.close(inputStream);
            }
        }
    }

    private void initCredentials() throws OXException {
        String signingKeyAlias = config.getSigningKeyAlias();
        String signingKeyPassword = config.getSigningKeyPassword();
        String encryptionKeyAlias = config.getEncryptionKeyAlias();
        String encryptionKeyPassword = config.getEncryptionKeyPassword();
        String idpCertificateAlias = config.getIDPCertificateAlias();
        Map<String, String> passwordMap = new HashMap<String, String>(4);
        if (!Strings.isEmpty(signingKeyAlias) && !Strings.isEmpty(signingKeyPassword)) {
            passwordMap.put(signingKeyAlias, signingKeyPassword);
        }
        if (!Strings.isEmpty(encryptionKeyAlias) && !Strings.isEmpty(encryptionKeyPassword)) {
            passwordMap.put(encryptionKeyAlias, encryptionKeyPassword);
        }

        keyStoreCredentialResolver = new KeyStoreCredentialResolver(keystore, passwordMap);
        if (config.signAuthnRequest()) {
            signingCredential = resolveCredential(keyStoreCredentialResolver, signingKeyAlias);
        }

        if (config.wantAssertionsSigned()) {
            idpCertificateCredential = resolveCredential(keyStoreCredentialResolver, idpCertificateAlias);
        }

        if (!Strings.isEmpty(encryptionKeyAlias)) {
            encryptionCredential = resolveCredential(keyStoreCredentialResolver, encryptionKeyAlias);
        }
    }

    private static Credential resolveCredential(KeyStoreCredentialResolver resolver, String alias) throws OXException {
        try {
            Criteria criteria = new EntityIDCriteria(alias);
            CriteriaSet criteriaSet = new CriteriaSet(criteria);
            Credential credential = resolver.resolveSingle(criteriaSet);
            if (credential == null) {
                throw SAMLExceptionCode.KEYSTORE_PROBLEM.create("Found no key store entry for alias '" + alias + "'");
            }

            return credential;
        } catch (SecurityException e) {
            throw SAMLExceptionCode.KEYSTORE_PROBLEM.create(e, e.getMessage());
        }
    }

    KeyStore getKeystore() {
        return keystore;
    }

    public String getMetadataXML() throws OXException {
        checkInitialized();

        // AssertionConsumerService redirectACS = openSAML.buildSAMLObject(AssertionConsumerService.class);
        // redirectACS.setIndex(1);
        // redirectACS.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
        // redirectACS.setLocation(config.getAssertionConsumerServiceURL());

        AssertionConsumerService postACS = openSAML.buildSAMLObject(AssertionConsumerService.class);
        postACS.setIndex(1);
        postACS.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        postACS.setLocation(config.getAssertionConsumerServiceURL());

        SPSSODescriptor spssoDescriptor = openSAML.buildSAMLObject(SPSSODescriptor.class);
        // spssoDescriptor.getAssertionConsumerServices().add(redirectACS);
        spssoDescriptor.getAssertionConsumerServices().add(postACS);
        spssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);

        if (config.supportSingleLogout()) {
            SingleLogoutService slService = openSAML.buildSAMLObject(SingleLogoutService.class);
            slService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
            slService.setLocation(config.getSingleLogoutServiceURL());
            spssoDescriptor.getSingleLogoutServices().add(slService);
        }

        NameIDFormat nameIDFormat = openSAML.buildSAMLObject(NameIDFormat.class);
        nameIDFormat.setFormat(NameIDType.TRANSIENT);
        spssoDescriptor.getNameIDFormats().add(nameIDFormat);

        try {
            X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
            keyInfoGeneratorFactory.setEmitEntityCertificate(true);
            KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
            if (config.signAuthnRequest()) {
                KeyDescriptor signKeyDescriptor = openSAML.buildSAMLObject(KeyDescriptor.class);
                signKeyDescriptor.setUse(UsageType.SIGNING);
                KeyInfo keyInfo = keyInfoGenerator.generate(signingCredential);
                signKeyDescriptor.setKeyInfo(keyInfo);
                spssoDescriptor.getKeyDescriptors().add(signKeyDescriptor);
                spssoDescriptor.setAuthnRequestsSigned(Boolean.TRUE);
            }

            if (config.wantAssertionsSigned()) {
                KeyDescriptor encryptionKeyDescriptor = openSAML.buildSAMLObject(KeyDescriptor.class);
                encryptionKeyDescriptor.setUse(UsageType.ENCRYPTION);
                encryptionKeyDescriptor.setKeyInfo(keyInfoGenerator.generate(encryptionCredential));
                spssoDescriptor.getKeyDescriptors().add(encryptionKeyDescriptor);
                spssoDescriptor.setWantAssertionsSigned(Boolean.TRUE);
            }
        } catch (SecurityException e) {
            throw SAMLExceptionCode.KEYSTORE_PROBLEM.create(e, e.getMessage());
        }

        spssoDescriptor = customizeDescriptor(spssoDescriptor);
        try {
            EntityDescriptor spDescriptor = openSAML.buildSAMLObject(EntityDescriptor.class);
            spDescriptor.setEntityID(config.getEntityID());
            spDescriptor.getRoleDescriptors().add(spssoDescriptor);
            Element element = openSAML.getMarshallerFactory().getMarshaller(spDescriptor).marshall(spDescriptor);
            return XMLHelper.prettyPrintXML(element);
        } catch (MarshallingException e) {
            throw SAMLExceptionCode.MARSHALLING_PROBLEM.create(e, e.getMessage());
        }
    }

    private SPSSODescriptor customizeDescriptor(SPSSODescriptor spssoDescriptor) throws OXException {
        ServiceProviderCustomizer customizer = this.customizer;
        if (customizer != null) {
            return customizer.customizeSPSSODescriptor(spssoDescriptor);
        }

        return spssoDescriptor;
    }

    public void respondWithAuthnRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException, IOException {
        checkInitialized();
        AuthnRequest authnRequest = customizeAuthnRequest(prepareAuthnRequest(), httpRequest, httpResponse);

        /*
         * We don't use the encoders of OpenSAML here for <several reasons>.
         */
        try {
            String authnRequestXML = openSAML.marshall(authnRequest);
            switch (config.getProtocolBinding()) {
            case HTTP_POST:
                // sendFormResponse(authnRequestXML, httpResponse);
                break;
            case HTTP_REDIRECT:
                sendRedirect(authnRequestXML, httpResponse);
                break;
            default:
                // throw new ServletException("Cannot handle SAML 2.0 protocol binding '" + config.getProtocolBinding().name() + "'!");
            }
        } catch (MarshallingException e) {
            throw SAMLExceptionCode.MARSHALLING_PROBLEM.create(e, e.getMessage());
        }
    }

    private void sendRedirect(String authnRequestXML, HttpServletResponse httpResponse) throws OXException, IOException {
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
            // TODO: Do we need a RelayState?
            URIBuilder redirectLocationBuilder = new URIBuilder(config.getIdentityProviderURL()).setParameter("SAMLRequest", encoded);
            if (config.signAuthnRequest()) {
                String sigAlg = openSAML.getGlobalSecurityConfiguration().getSignatureAlgorithmURI(
                    signingCredential.getPrivateKey().getAlgorithm());
                redirectLocationBuilder.setParameter("SigAlg", sigAlg);
                byte[] rawSignature = SigningUtil.signWithURI(
                    signingCredential,
                    sigAlg,
                    redirectLocationBuilder.build().getRawQuery().getBytes(Charsets.UTF_8));
                String signature = Base64.encodeBase64String(rawSignature);
                redirectLocationBuilder.setParameter("Signature", signature);
            }

            redirectLocation = redirectLocationBuilder.build().toString();
        } catch (URISyntaxException e) {
            throw SAMLExceptionCode.ENCODING_ERROR.create(e, "Could not construct redirect location");
        } catch (SecurityException e) {
            throw SAMLExceptionCode.ENCODING_ERROR.create(e, "Could not compute authentication request signature");
        }

        Tools.disableCaching(httpResponse);
        httpResponse.setCharacterEncoding(Charsets.UTF_8_NAME);
        httpResponse.sendRedirect(redirectLocation);
    }

    private AuthnRequest customizeAuthnRequest(AuthnRequest authnRequest, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        ServiceProviderCustomizer customizer = this.customizer;
        if (customizer != null) {
            RequestContext requestContext = new ServiceProviderCustomizer.RequestContext();
            requestContext.config = config;
            requestContext.openSAML = openSAML;
            requestContext.httpRequest = httpRequest;
            requestContext.httpResponse = httpResponse;
            return customizer.customizeAuthnRequest(authnRequest, requestContext);
        }
        return authnRequest;
    }

    private AuthnRequest prepareAuthnRequest() {
        Issuer issuer = openSAML.buildSAMLObject(Issuer.class);
        issuer.setFormat(NameIDType.ENTITY);
        issuer.setValue(config.getEntityID());

        String bindingURI = getBindingURI(config.getProtocolBinding());
        AuthnRequest authnRequest = openSAML.buildSAMLObject(AuthnRequest.class);
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setProviderName(config.getProviderName());
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        authnRequest.setAssertionConsumerServiceURL(config.getAssertionConsumerServiceURL());
        authnRequest.setDestination(config.getIdentityProviderURL());
        authnRequest.setIssuer(issuer);
        authnRequest.setIsPassive(Boolean.FALSE);
        authnRequest.setForceAuthn(Boolean.FALSE);
        authnRequest.setID(UUIDs.getUnformattedString(UUID.randomUUID()));
        authnRequest.setIssueInstant(new DateTime());

        // TODO:
        // - make configurable?
        // - really allow create?
        NameIDPolicy nameIDPolicy = openSAML.buildSAMLObject(NameIDPolicy.class);
        nameIDPolicy.setAllowCreate(true);
        nameIDPolicy.setFormat(NameIDType.TRANSIENT);
        authnRequest.setNameIDPolicy(nameIDPolicy);

        return authnRequest;
    }

    private Decrypter getDecrypter() {
        /*
         * Currently this decrypter is only able to decrypt assertions
         * that come along with their symmetric encrytion keys which are
         * in turn encrypted with the public key of 'encryptionCredential'
         */
        List<KeyInfoProvider> keyInfoProviders = new ArrayList<KeyInfoProvider>(4);
        keyInfoProviders.add(new InlineX509DataProvider());
        keyInfoProviders.add(new KeyInfoReferenceProvider());
        keyInfoProviders.add(new DEREncodedKeyValueProvider());
        keyInfoProviders.add(new RSAKeyValueProvider());
        keyInfoProviders.add(new DSAKeyValueProvider());

        ChainingEncryptedKeyResolver encryptedKeyResolver = new ChainingEncryptedKeyResolver();
        encryptedKeyResolver.getResolverChain().add(new InlineEncryptedKeyResolver());
        encryptedKeyResolver.getResolverChain().add(new EncryptedElementTypeEncryptedKeyResolver());
        encryptedKeyResolver.getResolverChain().add(new SimpleRetrievalMethodEncryptedKeyResolver());
        encryptedKeyResolver.getResolverChain().add(new SimpleKeyInfoReferenceEncryptedKeyResolver());

        StaticKeyInfoCredentialResolver skicr = new StaticKeyInfoCredentialResolver(encryptionCredential);
        Decrypter decrypter = new Decrypter(null, skicr, new InlineEncryptedKeyResolver());
        decrypter.setRootInNewDocument(true);
        return decrypter;
    }

    public void handleRedirectAuthnResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        handleAuthnResponse(httpRequest, httpResponse, new HTTPRedirectDeflateDecoder());
    }

    public void handlePOSTAuthnResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        handleAuthnResponse(httpRequest, httpResponse, new HTTPPostDecoder());
    }

    private void handleAuthnResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse, SAMLMessageDecoder decoder) throws OXException {
        BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject> context = new BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject>();
        context.setCommunicationProfileId("urn:oasis:names:tc:SAML:2.0:profiles:SSO:browser");
        context.setInboundMessageTransport(new HttpServletRequestAdapter(httpRequest));
        context.setInboundSAMLProtocol(SAMLConstants.SAML20P_NS);
        context.setPeerEntityRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);

        try {
            decoder.decode(context);
            Response response = (Response) context.getInboundSAMLMessage();

            SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
            SignatureValidator signatureValidator = new SignatureValidator(idpCertificateCredential);
            if (response.isSigned() && validateSignatures()) {
                Signature signature = response.getSignature();
                profileValidator.validate(signature);
                signatureValidator.validate(signature);
            }

            List<Assertion> assertions = decryptAndCollectAssertions(response);
            for (Assertion assertion : assertions) {
                if (assertion.isSigned()) {
                    Signature signature = assertion.getSignature();
                    profileValidator.validate(signature);
                    signatureValidator.validate(signature);
                }
            }

            System.out.println(assertions);
        } catch (MessageDecodingException e) {
            throw SAMLExceptionCode.DECODING_ERROR.create(e, e.getMessage());
        } catch (SecurityException e) {
            throw SAMLExceptionCode.DECODING_ERROR.create(e, e.getMessage());
        } catch (ValidationException e) {
            throw SAMLExceptionCode.SIGNATURE_VALIDATION_FAILED.create(e, e.getMessage());
        } catch (DecryptionException e) {
            throw SAMLExceptionCode.DECRYPTION_FAILED.create(e, e.getMessage());
        }

        /*
         * TODO: - resolve user and context - acquire token - redirect to token-login
         */
    }

    private List<Assertion> decryptAndCollectAssertions(Response response) throws DecryptionException {
        List<Assertion> assertions = new LinkedList<Assertion>();
        List<EncryptedAssertion> encryptedAssertions = response.getEncryptedAssertions();
        if (encryptedAssertions.size() > 0) {
            Decrypter decrypter = getDecrypter();
            for (EncryptedAssertion encryptedAssertion : encryptedAssertions) {
                assertions.add(decrypter.decrypt(encryptedAssertion));
            }
        }

        for (Assertion assertion : response.getAssertions()) {
            assertions.add(assertion);
        }

        return assertions;
    }

    private boolean validateSignatures() {
        return idpCertificateCredential != null;
    }

    public void setCustomizer(ServiceProviderCustomizer customizer) {
        this.customizer = customizer;
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("You must call init() before using this instance!");
        }
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
