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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
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
import java.util.concurrent.TimeUnit;
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
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
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
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.ChainingEncryptedKeyResolver;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.encryption.InlineEncryptedKeyResolver;
import org.opensaml.xml.encryption.SimpleKeyInfoReferenceEncryptedKeyResolver;
import org.opensaml.xml.encryption.SimpleRetrievalMethodEncryptedKeyResolver;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.XMLParserException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.spi.AuthnResponseHandler;
import com.openexchange.saml.spi.AuthnResponseHandler.Principal;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.spi.ServiceProviderCustomizer;
import com.openexchange.saml.spi.ServiceProviderCustomizer.RequestContext;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link #init()} must be called before usage!
 *
 * Several validation steps in this class are commented with excerpts from the SAML 2.0 specification.
 * Those excerpts are always annotated with their origin. E.g. [core 06 - 1.1p7] means "Cited from core
 * specification, working draft 06, section 1.1 on page 7". The "errata composite" documents from
 * https://wiki.oasis-open.org/security/FrontPage have been used as implementation reference.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SAMLServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SAMLServiceProvider.class);

    private final SAMLConfig config;

    private final OpenSAML openSAML;

    private final AuthnResponseHandler responseHandler;

    private final SessionReservationService sessionReservationService;

    private volatile ServiceProviderCustomizer customizer;

    private CredentialProvider credentialProvider;

    private boolean initialized;


    public SAMLServiceProvider(SAMLConfig config, OpenSAML openSAML, AuthnResponseHandler responseHandler, SessionReservationService sessionReservationService) throws OXException {
        super();
        this.config = config;
        this.openSAML = openSAML;
        this.responseHandler = responseHandler;
        this.sessionReservationService = sessionReservationService;
        initialized = false;
    }

    public synchronized void init() throws OXException {
        initialized = true;
    }
    
    // TODO: make mandatory
    public void setCredentialProvider(CredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    public String getMetadataXML() throws OXException {
        checkInitialized();

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

        NameIDFormat nameIDFormat = openSAML.buildSAMLObject(NameIDFormat.class);
        nameIDFormat.setFormat(NameIDType.TRANSIENT);
        spssoDescriptor.getNameIDFormats().add(nameIDFormat);

        try {
            X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
            keyInfoGeneratorFactory.setEmitEntityCertificate(true);
            KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
            if (credentialProvider.hasSigningCredential()) {
                KeyDescriptor signKeyDescriptor = openSAML.buildSAMLObject(KeyDescriptor.class);
                signKeyDescriptor.setUse(UsageType.SIGNING);
                KeyInfo keyInfo = keyInfoGenerator.generate(credentialProvider.getSigningCredential());
                signKeyDescriptor.setKeyInfo(keyInfo);
                spssoDescriptor.getKeyDescriptors().add(signKeyDescriptor);
                spssoDescriptor.setAuthnRequestsSigned(Boolean.TRUE);
            }

            if (credentialProvider.hasDecryptionCredential()) {
                KeyDescriptor encryptionKeyDescriptor = openSAML.buildSAMLObject(KeyDescriptor.class);
                encryptionKeyDescriptor.setUse(UsageType.ENCRYPTION);
                encryptionKeyDescriptor.setKeyInfo(keyInfoGenerator.generate(credentialProvider.getDecryptionCredential()));
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
            return XMLHelper.nodeToString(element);
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
            switch (config.getRequestBinding()) {
            case HTTP_POST:
                // sendFormResponse(authnRequestXML, httpResponse);
                break;
            case HTTP_REDIRECT:
                sendRedirect(authnRequestXML, httpRequest, httpResponse);
                break;
            default:
                throw SAMLExceptionCode.UNSUPPORTED_BINDING.create(config.getRequestBinding());
            }
        } catch (MarshallingException e) {
            throw SAMLExceptionCode.MARSHALLING_PROBLEM.create(e, e.getMessage());
        }
    }

    private void sendRedirect(String authnRequestXML, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException, IOException {
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
            URIBuilder redirectLocationBuilder = new URIBuilder(config.getIdentityProviderURL()).setParameter("SAMLRequest", encoded).setParameter("RelayState", httpRequest.getServerName());
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

        /*
         * The use of the AllowCreate attribute MUST NOT be used and SHOULD be ignored in conjunction with
         * requests for or assertions issued with name identifiers with a Format of
         * urn:oasis:names:tc:SAML:2.0:nameid-format:transient (they preclude any such state in and of themselves).
         *
         * [core06 - 3.4.1.1p51]
         */
        NameIDPolicy nameIDPolicy = openSAML.buildSAMLObject(NameIDPolicy.class);
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

        // FIXME:
        StaticKeyInfoCredentialResolver skicr = new StaticKeyInfoCredentialResolver(credentialProvider.getDecryptionCredential());
        Decrypter decrypter = new Decrypter(null, skicr, new InlineEncryptedKeyResolver());
        decrypter.setRootInNewDocument(true);
        return decrypter;
    }

    private Credential getSignatureValidationCredential(Signature signature) throws OXException {
        /*
         * Using certificates that are part of KeyInfo elements in the signed XML itself would require
         * us to verify those in terms of if we trust them. In case we need to extract and use those certificates at a later point
         * that is how it works:
         *
         *     List<KeyInfoProvider> keyInfoProviders = new ArrayList<KeyInfoProvider>(4);
         *     keyInfoProviders.add(new InlineX509DataProvider());
         *     keyInfoProviders.add(new KeyInfoReferenceProvider());
         *     keyInfoProviders.add(new DEREncodedKeyValueProvider());
         *     keyInfoProviders.add(new RSAKeyValueProvider());
         *     keyInfoProviders.add(new DSAKeyValueProvider());
         *     BasicProviderKeyInfoCredentialResolver keyInfoCredentialResolver = new BasicProviderKeyInfoCredentialResolver(keyInfoProviders);
         *
         *     ChainingCredentialResolver credentialResolver = new ChainingCredentialResolver();
         *     credentialResolver.getResolverChain().add(keyInfoCredentialResolver);
         *     if (idpCertificateCredential != null) {
         *         credentialResolver.getResolverChain().add(new StaticCredentialResolver(idpCertificateCredential));
         *     }
         *
         *     Credential credential;
         *     try {
         *         credential = credentialResolver.resolveSingle(new CriteriaSet(new KeyInfoCriteria(signature.getKeyInfo())));
         *     } catch (SecurityException e) {
         *         throw SAMLExceptionCode.SIGNATURE_VALIDATION_FAILED.create(e, e.getMessage());
         *     }
         *
         *     if (credential == null) {
         *         throw SAMLExceptionCode.SIGNATURE_VALIDATION_FAILED.create("Could not find a certificate for signature validation.");
         *     }
         */

        Credential credential = credentialProvider.getValidationCredential();
        if (credential == null) {
            throw SAMLExceptionCode.SIGNATURE_VALIDATION_FAILED.create("Could not find a credential for signature validation.");
        }

        return credential;
    }

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

        if (responseHandler.beforeDecode(httpRequest, httpResponse, openSAML)) {
            Response response = decodeResponse(httpRequest, httpResponse);
            try {
                if (responseHandler.beforeValidate(response, openSAML)) {
                    boolean responseWasSigned = false;
                    if (response.isSigned()) {
                        /*
                         * All SAML protocol request and response messages MAY be signed using XML Signature.
                         * [core 06 - 5.1p70]
                         */
                        Signature signature = response.getSignature();
                        SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
                        profileValidator.validate(signature);

                        Credential validationCredential = getSignatureValidationCredential(signature);
                        SignatureValidator signatureValidator = new SignatureValidator(validationCredential);
                        signatureValidator.validate(signature);
                        responseWasSigned = true;
                    }

                    Status status = response.getStatus();
                    if (status == null) {
                        xmlDebugLog("status missing in response", response);
                        throw SAMLExceptionCode.INVALID_RESPONSE.create("status was missing");
                    }

                    if (!StatusCode.SUCCESS_URI.equals(status.getStatusCode().getValue())) {
                        String idpMessage = "none";
                        StatusMessage message = status.getStatusMessage();
                        if (message != null) {
                            idpMessage = message.getMessage();
                        }

                        xmlDebugLog("unsuccessful authentication response", response);
                        throw SAMLExceptionCode.AUTHENTICATION_FAILED.create(idpMessage);
                    }

                    /*
                     * If the <Response> message is signed or
                     * if an enclosed assertion is encrypted, then the <Issuer> element MUST be present. Otherwise it
                     * MAY be omitted. If present it MUST contain the unique identifier of the issuing identity provider; the
                     * Format attribute MUST be omitted or have a value of urn:oasis:names:tc:SAML:2.0:nameid-format:entity.
                     * [profiles 06 - 4.1.4.2p19]
                     */
//                    if (responseWasSigned || response.getEncryptedAssertions().size() > 0) {
//                        Issuer issuer = response.getIssuer();
//                        if (issuer == null) {
//                            xmlDebugLog("issuer missing in response", response);
//                            throw SAMLExceptionCode.INVALID_RESPONSE.create("issuer was missing");
//                        }
//
//                        if (issuer.getFormat() != null && !NameIDType.ENTITY.equals(issuer.getFormat())) {
//                            xmlDebugLog("invalid issuer format in assertion", response);
//                            throw SAMLExceptionCode.INVALID_RESPONSE.create("invalid issuer format");
//                        }
//
//                        if (!issuer.getValue().equals(config.getIdentityProviderEntityID())) {
//                            xmlDebugLog("invalid issuer value in assertion", response);
//                            throw SAMLExceptionCode.INVALID_RESPONSE.create("invalid issuer value");
//                        }
//                    }

                    if (response.getAssertions().size() + response.getEncryptedAssertions().size() == 0) {
                        xmlDebugLog("no assertion contained in response", response);
                        throw SAMLExceptionCode.INVALID_RESPONSE.create("no assertion was contained");
                    }

                    /*
                     * A SAML assertion may be embedded within another SAML element, such as an enclosing <Assertion>
                     * or a request or response, which may be signed. When a SAML assertion does not contain a
                     * <ds:Signature> element, but is contained in an enclosing SAML element that contains a
                     * <ds:Signature> element, and the signature applies to the <Assertion> element and all its children,
                     * then the assertion can be considered to inherit the signature from the enclosing element. The resulting
                     * interpretation should be equivalent to the case where the assertion itself was signed with the same key
                     * and signature options.
                     * [core 06 - 5.3p70/71]
                     *
                     *
                     * The <Assertion> element(s) in the <Response> MUST be signed, if the HTTP POST binding is used,
                     * and MAY be signed if the HTTP-Artifact binding is used.
                     * [profiles 06 - 4.1.3.5p18]
                     */
                    List<Assertion> allAssertions = decryptAndCollectAssertions(response);
                    List<Assertion> bearerAssertions = new LinkedList<Assertion>();
                    boolean enforceSignature = binding == Binding.HTTP_POST && !responseWasSigned;
                    for (Assertion assertion : allAssertions) {
                        validateAssertionSignatureAndIssuer(response, assertion, enforceSignature);
                        if (isValidBearerAssertion(assertion)) {
                            bearerAssertions.add(assertion);
                        }
                    }

                    if (bearerAssertions.isEmpty()) {
                        throw SAMLExceptionCode.INVALID_RESPONSE.create("no bearer assertion was contained");
                    }

                    Assertion finalAssertion = null;
                    for (Assertion assertion : bearerAssertions) {
                        if (conditionsMet(assertion) && hasValidAuthnStatement(assertion)) {
                            finalAssertion = assertion;
                            break;
                        }
                    }

                    if (finalAssertion == null) {
                        throw SAMLExceptionCode.INVALID_RESPONSE.create("no valid assertion was contained");
                    }

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

                    /*
                     * TODO:
                     * The service provider MUST ensure that bearer assertions are not replayed, by maintaining the set of used
                     * ID values for the length of time for which the assertion would be considered valid based on the
                     * NotOnOrAfter attribute in the <SubjectConfirmationData>.
                     * [profiles 06 - 4.1.4.2p21]
                     */

                    Principal principal = responseHandler.resolvePrincipal(response, finalAssertion, openSAML);

                    /*
                     * TODO state:
                     *  - session index for logout
                     *  - SessionNotOnOrAfter
                     */
                    String sessionToken = sessionReservationService.reserveSessionFor(principal.getUserId(), principal.getContextId(), 60l, TimeUnit.SECONDS, null);
                    String redirectHost = httpRequest.getParameter("RelayState"); // TODO: integrity check and fail on null
                    // TODO redirect to relay state
                    URI redirectLocation = new URIBuilder()
                        .setScheme("https")
                        .setHost(redirectHost)
                        .setPath("/ajax/login")
                        .setParameter("action", "supertoken")
                        .setParameter("token", sessionToken)
                        .build();

                    httpResponse.sendRedirect(redirectLocation.toString());
                }
            } catch (ValidationException e) {
                xmlDebugLog("Response signature validation failed", response, e);
                throw SAMLExceptionCode.SIGNATURE_VALIDATION_FAILED.create(e, e.getMessage());
            } catch (DecryptionException e) {
                xmlDebugLog("Assertion decryption failed", response, e);
                throw SAMLExceptionCode.DECRYPTION_FAILED.create(e, e.getMessage());
            } catch (URISyntaxException e) {
                throw SAMLExceptionCode.INTERNAL_ERROR.create(e.getMessage());
            }

        }
    }

    private boolean conditionsMet(Assertion assertion) {
        /*
         * Each bearer assertion(s) containing a bearer subject confirmation MUST contain an
         * <AudienceRestriction> including the service provider's unique identifier as an <Audience>.
         * Other conditions (and other <Audience> elements) MAY be included as requested by the service
         * provider or at the discretion of the identity provider. (Of course, all such conditions MUST be
         * understood by and accepted by the service provider in order for the assertion to be considered valid.)
         * [profiles 06 - 4.1.4.2p20]
         */
        Conditions conditions = assertion.getConditions();
        if (conditions == null) {
            return false;
        }

        DateTime now = new DateTime();
        DateTime notBefore = conditions.getNotBefore();
        if (notBefore != null && now.isBefore(notBefore)) {
            return false;
        }

        DateTime notOnOrAfter = conditions.getNotOnOrAfter();
        if (notOnOrAfter != null && !now.isBefore(notOnOrAfter)) {
            return false;
        }


        List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
        if (audienceRestrictions.isEmpty()) {
            return false;
        }

        boolean audienceValid = false;
        outer: for (AudienceRestriction audienceRestriction : audienceRestrictions) {
            List<Audience> audiences = audienceRestriction.getAudiences();
            for (Audience audience : audiences) {
                if (config.getEntityID().equals(audience.getAudienceURI())) {
                    audienceValid = true;
                    break outer;
                }
            }
        }

        if (!audienceValid) {
            return false;
        }

        /*
         * TODO:
         * OneTimeUse
         * ProxyRestriction
         * Both not relevant for us? Provide an extension point (maybe even for custom conditions)?
         * Fail on unknown conditions?
         */

        return true;
    }

    private boolean hasValidAuthnStatement(Assertion assertion) {
        /*
         * The set of one or more bearer assertions MUST contain at least one <AuthnStatement> that
         * reflects the authentication of the principal to the identity provider. Multiple <AuthnStatement>
         * elements MAY be included, but the semantics of multiple statements is not defined by this profile.
         * [profiles 06 - 4.1.4.2p20]
         */
        return !assertion.getAuthnStatements().isEmpty();
    }

    private boolean isValidBearerAssertion(Assertion assertion) {
        /*
         * Any assertion issued for consumption using this profile MUST contain a <Subject> element with at
         * least one <SubjectConfirmation> element containing a Method of
         * urn:oasis:names:tc:SAML:2.0:cm:bearer. Such an assertion is termed a bearer assertion.
         * Bearer assertions MAY contain additional <SubjectConfirmation> elements.
         * [profiles 06 - 4.1.4.2p20]
         */
        Subject subject = assertion.getSubject();
        if (subject == null) {
            return false;
        }

        List<SubjectConfirmation> bearerConfirmations = new LinkedList<SubjectConfirmation>();
        for (SubjectConfirmation confirmation : subject.getSubjectConfirmations()) {
            if ("urn:oasis:names:tc:SAML:2.0:cm:bearer".equals(confirmation.getMethod()) && confirmation.getSubjectConfirmationData() != null) {
                bearerConfirmations.add(confirmation);
            }
        }

        if (bearerConfirmations.isEmpty()) {
            return false;
        }

        /*
         * At lease one bearer <SubjectConfirmation> element MUST contain a
         * <SubjectConfirmationData> element that itself MUST contain a Recipient attribute containing
         * the service provider's assertion consumer service URL and a NotOnOrAfter attribute that limits the
         * window during which the assertion can be [E52]confirmed by the relying party. It MAY also contain an
         * Address attribute limiting the client address from which the assertion can be delivered. It MUST NOT
         * contain a NotBefore attribute. If the containing message is in response to an <AuthnRequest>,
         * then the InResponseTo attribute MUST match the request's ID.
         * [profiles 06 - 4.1.4.2p20]
         */
        List<SubjectConfirmation> validConfirmations = new LinkedList<SubjectConfirmation>();
        for (SubjectConfirmation confirmation : bearerConfirmations) {
            SubjectConfirmationData confirmationData = confirmation.getSubjectConfirmationData();
            String recipient = confirmationData.getRecipient();
            if (recipient == null) {
                continue;
            }

            if (!config.getAssertionConsumerServiceURL().equals(recipient)) {
                continue;
            }

            DateTime notOnOrAfter = confirmationData.getNotOnOrAfter();
            if (notOnOrAfter == null) {
                continue;
            }

            if (!new DateTime().isBefore(notOnOrAfter)) {
                continue;
            }

            String inResponseTo = confirmationData.getInResponseTo();
            if (inResponseTo != null) {
                // TODO validate
            }

            validConfirmations.add(confirmation);
        }


        if (validConfirmations.isEmpty()) {
            return false;
        }

        return true;
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

    private void validateAssertionSignatureAndIssuer(Response response, Assertion assertion, boolean enforceSignature) throws OXException {
        if (enforceSignature && !assertion.isSigned()) {
            String msg = "assertion must be signed but was not";
            xmlDebugLog(msg, assertion);
            throw SAMLExceptionCode.INVALID_ASSERTION.create(msg);
        }

        if (assertion.isSigned()) {
            try {
                Signature signature = assertion.getSignature();
                SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
                profileValidator.validate(signature);
                Credential validationCredential = getSignatureValidationCredential(signature);
                SignatureValidator signatureValidator = new SignatureValidator(validationCredential);
                signatureValidator.validate(signature);
            } catch (ValidationException e) {
                xmlDebugLog("Assertion signature validation failed", assertion, e);
                throw SAMLExceptionCode.SIGNATURE_VALIDATION_FAILED.create(e, e.getMessage());
            }
        }

        validateAssertionIssuer(assertion);
    }

    private void validateAssertionIssuer(Assertion assertion) throws OXException {
        Issuer issuer = assertion.getIssuer();
        if (issuer == null) {
            xmlDebugLog("issuer missing in assertion", assertion);
            throw SAMLExceptionCode.INVALID_ASSERTION.create("issuer was missing");
        } else {
            if (issuer.getFormat() != null && !NameIDType.ENTITY.equals(issuer.getFormat())) {
                xmlDebugLog("invalid issuer format in assertion", assertion);
                throw SAMLExceptionCode.INVALID_ASSERTION.create("invalid issuer format");
            }

            if (!issuer.getValue().equals(config.getIdentityProviderEntityID())) {
                xmlDebugLog("invalid issuer value in assertion", assertion);
                throw SAMLExceptionCode.INVALID_ASSERTION.create("invalid issuer value");
            }
        }
    }

    private void xmlDebugLog(String logMessage, final XMLObject object) {
        LOG.debug(logMessage + ":\n{}", new Object() {
            @Override
            public String toString() {
                try {
                    return XMLHelper.prettyPrintXML(openSAML.getMarshallerFactory().getMarshaller(object).marshall(object));
                } catch (MarshallingException e) {
                    return "XML not available due to marshalling error: " + e.getMessage();
                }
            }
        });
    }

    private void xmlDebugLog(String logMessage, final XMLObject object, Throwable t) {
        LOG.debug(logMessage + ":\n{}", new Object() {
            @Override
            public String toString() {
                try {
                    return XMLHelper.prettyPrintXML(openSAML.getMarshallerFactory().getMarshaller(object).marshall(object));
                } catch (MarshallingException e) {
                    return "XML not available due to marshalling error: " + e.getMessage();
                }
            }
        }, t);
    }

    private Response decodeResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        String b64Response = httpRequest.getParameter("SAMLResponse");
        if (b64Response == null) {
            throw SAMLExceptionCode.DECODING_ERROR.create("The 'SAMLResponse' parameter was not set");
        }

        try {
            byte[] responseBytes = Base64.decodeBase64(b64Response);
            Element responseElement = openSAML.getParserPool().parse(new ByteArrayInputStream(responseBytes)).getDocumentElement();
            XMLObject unmarshalledResponse = openSAML.getUnmarshallerFactory().getUnmarshaller(responseElement).unmarshall(responseElement);
            if (!(unmarshalledResponse instanceof Response)) {
                throw SAMLExceptionCode.DECODING_ERROR.create("XML was not a valid Response element");
            }

            return (Response) unmarshalledResponse;
        } catch (XMLParserException e) {
            throw SAMLExceptionCode.DECODING_ERROR.create(e, e.getMessage());
        } catch (UnmarshallingException e) {
            throw SAMLExceptionCode.DECODING_ERROR.create(e, e.getMessage());
        }
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
