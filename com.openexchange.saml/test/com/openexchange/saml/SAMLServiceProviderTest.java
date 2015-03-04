package com.openexchange.saml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
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
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.security.SAMLProtocolMessageXMLSignatureSecurityPolicyRule;
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
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.DOMMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.security.MetadataCredentialResolver;
import org.opensaml.security.MetadataCriteria;
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
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
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
import org.opensaml.xml.security.x509.X509Util;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.X509Certificate;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.spi.AuthnResponseHandler;

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

        /*
         * Init service provider
         */
        config = new TestConfig();
        openSAML = new OpenSAML();
        serviceProvider = new SAMLServiceProvider(config, openSAML, new TestResponseHandler());
        serviceProvider.init();
    }

    private SAMLServiceProvider serviceProvider;
    private TestConfig config;
    private OpenSAML openSAML;

    private static final class TestConfig implements SAMLConfig {

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
        public boolean signAuthnRequests() {
            return true;
        }

        @Override
        public boolean supportSingleLogout() {
            return true;
        }

        @Override
        public String getSingleLogoutServiceURL() {
            return "http://test.saml.open-xchange.com/ajax/saml/logout";
        }

        @Override
        public String getKeyStorePath() {
            return "/home/steffen/tmp/spassmitkeys/saml.jks";
        }

        @Override
        public String getKeyStorePassword() {
            return "secret";
        }

        @Override
        public String getSigningKeyAlias() {
            return "saml-signing-key";
        }

        @Override
        public String getSigningKeyPassword() {
            return "secret1";
        }

        @Override
        public String getEncryptionKeyAlias() {
            return "saml-encryption-key";
        }

        @Override
        public String getEncryptionKeyPassword() {
            return "secret2";
        }

        @Override
        public String getIDPCertificateAlias() {
            return "testshib-certificate";
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

    private static final class TestResponseHandler implements AuthnResponseHandler {

        @Override
        public boolean beforeDecode(HttpServletRequest httpRequest, HttpServletResponse httpResponse, OpenSAML openSAML) throws OXException {
            return false;
        }

        @Override
        public boolean beforeValidate(Response response, OpenSAML openSAML) throws OXException {
            return false;
        }

        @Override
        public boolean afterValidate(Response response, List<Assertion> assertions, OpenSAML openSAML) throws OXException {
            return false;
        }

        @Override
        public Principal resolvePrincipal(Response response, List<Assertion> assertions, OpenSAML openSAML) throws OXException {
            return new Principal(0, 0);
        }

    }



    @BeforeClass
    public static void beforeClass() throws ConfigurationException {
        DefaultBootstrap.bootstrap();
    }

    private static final String SAML_RESPONSE ="PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOlJlc3BvbnNlIHhtbG5zOnNhbWwycD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnByb3RvY29sIiBEZXN0aW5hdGlvbj0iaHR0cDovL3Rlc3Quc2FtbC5vcGVuLXhjaGFuZ2UuY29tL2FqYXgvc2FtbC9hY3MiIElEPSJfM2I5ZmE1NzdmMmY4ODQ2Zjk1NGZlNDM4NjQ5ZGY5ODUiIEluUmVzcG9uc2VUbz0iNTJmYjc0ZGU2ZmQ1NGIyMTlkZTQxNzI2YjM3ODZiYTciIElzc3VlSW5zdGFudD0iMjAxNS0wMi0yN1QxNzo0OToyNC4zMTNaIiBWZXJzaW9uPSIyLjAiPjxzYW1sMjpJc3N1ZXIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR5Ij5odHRwczovL2lkcC50ZXN0c2hpYi5vcmcvaWRwL3NoaWJib2xldGg8L3NhbWwyOklzc3Vlcj48c2FtbDJwOlN0YXR1cz48c2FtbDJwOlN0YXR1c0NvZGUgVmFsdWU9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpzdGF0dXM6U3VjY2VzcyIvPjwvc2FtbDJwOlN0YXR1cz48c2FtbDI6RW5jcnlwdGVkQXNzZXJ0aW9uIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj48eGVuYzpFbmNyeXB0ZWREYXRhIHhtbG5zOnhlbmM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jIyIgSWQ9Il9hYmZiZjM5ZmM4OWYxZjgxNjA3NzJlMGJlNWI4MjY2MCIgVHlwZT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjRWxlbWVudCI+PHhlbmM6RW5jcnlwdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI2FlczEyOC1jYmMiIHhtbG5zOnhlbmM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jIyIvPjxkczpLZXlJbmZvIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48eGVuYzpFbmNyeXB0ZWRLZXkgSWQ9Il81YTBkOTBkNDdiOGYwYmRjM2E5NWM2NWI2YzQxYWJlMiIgeG1sbnM6eGVuYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjIj48eGVuYzpFbmNyeXB0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjcnNhLW9hZXAtbWdmMXAiIHhtbG5zOnhlbmM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jIyI+PGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNzaGExIiB4bWxuczpkcz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyIvPjwveGVuYzpFbmNyeXB0aW9uTWV0aG9kPjxkczpLZXlJbmZvPjxkczpYNTA5RGF0YT48ZHM6WDUwOUNlcnRpZmljYXRlPk1JSUZpekNDQTNPZ0F3SUJBZ0lFV3hRSlNqQU5CZ2txaGtpRzl3MEJBUXNGQURCMk1Rc3dDUVlEVlFRR0V3SkVSVEVNTUFvR0ExVUUKQ0JNRFRsSlhNUTB3Q3dZRFZRUUhFd1JQYkhCbE1Sb3dHQVlEVlFRS0V4RlBjR1Z1TFZoamFHRnVaMlVnUjIxaVNERVVNQklHQTFVRQpDeE1MUlc1bmFXNWxaWEpwYm1jeEdEQVdCZ05WQkFNVEQxTjBaV1ptWlc0Z1ZHVnRjR3hwYmpBZUZ3MHhOVEF5TWpjeE56RTNNRGhhCkZ3MHhOakF5TWpJeE56RTNNRGhhTUhZeEN6QUpCZ05WQkFZVEFrUkZNUXd3Q2dZRFZRUUlFd05PVWxjeERUQUxCZ05WQkFjVEJFOXMKY0dVeEdqQVlCZ05WQkFvVEVVOXdaVzR0V0dOb1lXNW5aU0JIYldKSU1SUXdFZ1lEVlFRTEV3dEZibWRwYm1WbGNtbHVaekVZTUJZRwpBMVVFQXhNUFUzUmxabVpsYmlCVVpXMXdiR2x1TUlJQ0lqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FnOEFNSUlDQ2dLQ0FnRUFwZzJ1ClBsaUZXd1VFeWphQVQ3K2VvSGlwdnltYnQzS0d6dUJsWWUzMyswbndvZW1WWENaQzlvS1dtWlRsVHFVMVdmWmk0THpVRFU4ajZwVEUKajVZY0hYb3lJTEVidng4MFdqRjJ6WnZ1WVZxaitPc1MwT2hxelB2TmtWcmZNWkxmVmI2UlkrMG1KYXZMMit4NUxOaGNYai8ybE1pTAo0UjZ4akt3ejNrdFFGcERLSS9NMmVNSGR4cWt6Wk96MjZma0IzUlEwVWc0SXk1QW15Q2IycWFnLzJkUEVaYlo4NmhEV3B4K0N6eUdmClJtS3lMbW4rZkxtSCtwckdSQXFjSGt2aEFGNjBEZUVXeEthMjlONGQwTTZaaWN1YmlVallYbDdzN0U1bGxFclM4eG9GdklOZGJXdmEKRUJPQnp1SUduUFdQLzRLY3ZsTkU0cW1qeFJvY04yaHQ1cENEamE3bU9xMnA5cnh2K1k2emY1RFR3MHEwcEFRTmFxckFhRzNEbnBBdgpzbWpJY3FqcHdadVBsMFdQeGJMck5SMjBkdmlsUk1RZWRHRTJSR2ZMRm1uT01QT1pwT1hLdEViSWc1Q09uRWRSK3dTOGEvN3J3UGFLClY1RXp6cW1vQ2RQNmltdGV2Y0J6R1RkbUxBa1RMSVEzM3F4ekkvdWhLVUdjYlJWUGdWbzROTzJEWW1XT0s1ODV5WUY3ZUphWGhDR1oKejlzTUxjMFJWQmJwSXdwc3ZRckw2ZFJoOHhkalByUm1XYTdUZkJ4TVF6d2VkWGtUTkh0NlRuOExKcjBwSENuOGNPZUVuSHMrZmhFVQpPRU1HS3VoQlR6eThvb2h6TndnWm5HWllBWlZNcjNNN2hmNFdnbGFmdDI2b0kzSzhlYjlqeVJhdkN2bkV6NnRwSFlKVG9Hc0NBd0VBCkFhTWhNQjh3SFFZRFZSME9CQllFRkJ0c25pN3RJWmFyZFBkaldWT1FMcTJBTUR2eE1BMEdDU3FHU0liM0RRRUJDd1VBQTRJQ0FRQ2IKNGgra212cFhIdmIxdDdReXBhRzBnUEpLOUxXTk1peTQ4em1STlcydjFzWCs2ZkhJYW5WYUNob0w4cG81NnhLdFZYTHIrQUhyRFlUYQpEU3JSUGZLUDJFWDI2QjNic1RqcGNZQjY3WGdxaEhCWUpvcTMxMkt2ZWZWcm1XM2FkV3hrL25sM2FDMFFIRUhXSDRDcjk5OWtwTXB0CldrQmlvSE1weGN1Uzh1VXZ3aHh5OUtWQlVoVkg2bVgxeWVPVC9NMjFNbk56MWVZbnpyMXBtR3FuNUJPeTFicUhsSWNteUNRMEFCcEMKbEtXZjRobkdyOFVyS0FPYUNJYk1aN2p1a1lnbk9wN3FzaFI5TlJFcU0rUW0zcXNUOXNSR1pnOVRoRUMxSHcvYS8yRFRvUmtWK0JNbwpzZXpETlpZTjhyQnUrVkFaNjc4UjNzZ3ViWDg3b1hQWkR6NzdodjZYQTkxWW5uY0FoRktQZVpSRFVDM2pkREtFOTh4Q3k5Q0N5TGczClRFbFJBYmFWQ1ZKTlFYSHp5THoxOHBGUmtRVFRidmRYUkxsWENNTng0cXhtVnlHTW43WjdDZU1UODlEeTdud2VrUWtDbTdreWZTOXIKZU9vOUx5amZLdXU2bGVpNkJvR3M3Ym9jbW55N1pGZDc1cmJQK1MzNnJHS1p5V1I4Q0FUcFBlL0Erbm1aa2lPS1pJMklEYlg2SEN5MApMNVI0Qmw2QjhWM2xvQ2VOanh1enN4T0Q5cENkK21TQnVjcVpHTlU0S3JXZW9JcFc0R2NZSVhiZXBEZDkxNUlQdTZYWFhxSUxpMnlyCk5BbS9FODU2VkVWVkRZOHFnY01Venh2ZjdOS3Y2eTdVUjdMd3VScjU2QjlaMlp3b0NwTVNtT0RlQSswQld5YlZTSnRNR1k4QW5BPT08L2RzOlg1MDlDZXJ0aWZpY2F0ZT48L2RzOlg1MDlEYXRhPjwvZHM6S2V5SW5mbz48eGVuYzpDaXBoZXJEYXRhIHhtbG5zOnhlbmM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jIyI+PHhlbmM6Q2lwaGVyVmFsdWU+QUhIcW9abS9HYjUzZUxBWTBkYlVnY3pJeU1tZWcwdnJ4bzBPUW52RDU4VEdtQ0RPbEVwWU5kUmh1Si85RFpTSE5iR25YMzRhZ0l2N2pOdVJxZnY5NDlCM1lnSGQ3Wk5TdDRINjJhQk0vaVN1YnptSElReG1GaXl1NlJGdGFoa0lkUWFwcDFoZEtZUnRWRXd2NWV0c0NiREdtUncvTHBmMEh5RHk2M0FCaEt0TXlTbVFHbitBcXNPVDc4MXBWd0kzRHBWRlNlMDBQRzVKZHZPenZiUUVsK1ozbTVMMlRvVVJza2M5L0oxeHhxV001dDlvTWl4U2lRZWoyRVZKa0xERFYyY0JTRXNWd0JjRFVBNjlxcTk0TEFrSnVKMHc4aitWd2xFdnV2ZXZVaUd4cDF5b2J2WVorVEdjSVBneGpUeTd2K0g4TG9qUTFCcGRLcGw4RTd4TEhRQ2lDRW0vYldBdmJ4S2F5ZUFmUWpNT2NNbkI2VytDZFJ5cGo2UnIrWk95MHJYQjJhZWNxbjVBbzlORHZNSHdrYnZPdG42M29GeUZCTDY1Q3JwdFZ4Umd4dzJaN0pYdk83MVZuVXBNRUZ6THAvaENVQnM2R3FHR1VPNDM3UzFlb0V5RkNTSHB2bDM2UkQzVkNjQVZYVUpZNVQvdUF1V2p5cFBtMWZYOURWQ1lNSlIzKzU0UDNwTDNvQ3l3T0pTdFhtb2U2bWdyTHJqRmhMeDdqcFBJd3M1cmZldUVyYlJxeVBoc1JJWTdsaSt6eUtzVEVsMUE0eFFPQ3pDQzBSOTlnK2g1TWcrSzVJSHgwd1BOOTRFR2o0RVk2MGk0aXFoT3NEY29OYmhYSytRVythd0FsYWxubGpzUzJ0Qnc5dHdSY0t4bHNjWm8zcHg2dDNydFJFMkFHQWc9PC94ZW5jOkNpcGhlclZhbHVlPjwveGVuYzpDaXBoZXJEYXRhPjwveGVuYzpFbmNyeXB0ZWRLZXk+PC9kczpLZXlJbmZvPjx4ZW5jOkNpcGhlckRhdGEgeG1sbnM6eGVuYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjIj48eGVuYzpDaXBoZXJWYWx1ZT5SYlBqU0YwQytsME5KN2lPUTRCNjlqTFNUWU1KMFE0Rml4ZXc3S1hZZHo4YTBrOGVWSjFrL2wwR3Rac2JWQytIQi9CY2o0elhqZU52TXQ0aDZDY0tvYzhodloxMnpxaC9xNjJYNk40WVEzT0t0ZzJXUUx2MktLbDNweTRnSVRGRkZHYnd5WDJ1enR5NWZoVUM4UXh2ZzU3VmtkYUd1SmVkTjZ2T0dwQ2dubkRtY3NBeVMyYXFMSzVHbzVrR3V3TnpybzVyMEpLaHp0WlgrNExTQVV1YXk0Q0c3TG15RlI1NjVRZm5ISVlGazRTcSs3MW9IeS9mUlo4MDhDMnc5NnpXSUZPaXBuK2JIYnlNRnhNSlI2N2VwOVFTQ0dRTm0rMm93ZmRGYnJHL3VNaFpUZHhnNWpoaTZjdmxqaURWdHBObzIyWVhYbXVCU2U2OTFORUVRWERxeTFacVZSN0VrcDJ0Ynk2NytYQUtGYmFpNi9NVmgya0NreWFVeXlBWTZya0c4ZS9FUWVwTHFtM2JBQVZnbkY2VWtDUjR5ZmhMd3dXNDJYMmRoS1g4NVBjRnUyZmtjYm5sZWdHaDhuN0xaWGpXMlpsdkZ1Z2NGdzI3ZzFKNjBkTXF4eFNnd1ZyM05rdWd5TVI5S0hwQlFidWFRd2NWUStoTEg1NnpwV2ltb3pxcnE1dFBtMmJEekJzNlJhcWlZcDQyQjNtN3hsYW9jYld0eDRTT09YYXhCT3FlODg0bld3THZDWmpyODZrY1BpQiswekYybkxhQk5sb3ZZcHBJRm1QUS9IWUhjYnFONVhKWWJNNVJJNVFuRmtRNjFFejFTTnA5ZFlicDdDSnVXTDA1Z1pVS1ZiZjh1NWNRaXduUStGOHhJT3JqZDNSSVF1SEVMaXloVzRSemZrV0Y0TGgrRE5sd0FQY3VCRlRhT3J0cTl1Sm94ZkpwRHoyK21HRnp6ZzV2U0FNU2hQTGNHeEVUMTRSSmNCVEUyQVB4aDk4WkgrM3lMYS9Wbks5clFQRFduOHNqS3VpR0dWN0xJNUd3bW5sRFNKbmFhb1g4aDl1R0kvdWlidEZDaWhINzJreGNlbzVqU0pTRHZOazZzcndtL0ltQjFSanFOV3lNaHV3OUVIY0JGa3BmbkZWbnQyR1FBNDFzaFloYTlXNnBNM1NjekhYNHllN3dkZzI4SlBLc280RXRiVjRMZEhFVTRpclpJSjdlcHcyOWRxd05mTDZIeTlUNjV1TmlVUk5aemgwMURYS1NGY2lTWFltZDlvbElZZkVubDlHZGhXSEgyM3JpQTVSSWRaMHhkaUJybmdIdXQ5NXFWUlJ2SzM2WUhlVHVqaDZPaUh1N3ZIbUpiYkljenFnZ3BJM0owd0VTZWVsZ0hmM2pmZjF0NzlRUnVTLzQ3dFpXTXlwc1ovN1RuamluWU9IdlgzTkZnT3FielZUL3c4TmdSVlZwZmdqTmZCLzJvQTdTQWhXTHBZL1NXN2F6UW92M29IOEZyUnVtRUE3TmVSM2ExdEtYQS96Q0RsT0NxdlNOQUUrbEhnRGNwL01wOG1PQjVHbUdON2JwRGthNjBVYzZzU2RJaXNKS1crOUJSZVRVVzBXbGJ0NkYwQnJNMHplZ2pMYktTR0U0eEdFNlNtR0lCV010Qk9lZXBuZmx4UDR4Zjc2VWxKY0lDZE9nVlh2N1J4VE04bm4rSFM0T3U3SXVBeW1LK0hyTnBnUXg3Q1B4Vk5JTk0vWmhzempId2hWOUdXOVFjTG5hT25ETXUyVzJXSUNNY3piN1BnM2dEU3lad05TT0FJTldJNkdlME9XTlVNSWpPeGJWdTZFVDRZRnRWZGN2Uzk5OW81dXVXYVBDeTU4NmF3TnNFQ2NjRG50WmRZd2ZpYW1KTEJGRGN3dTB2TWdtS0RmRXJaODIrSHdvODZWZlBVMVBjVkl0VERNU3BTc2RCY1UrWGpGcmhxVnB3OG9hZnl1OVkxOG1DYmNHUmU5cW5rUDlycFFjUkhNUHRWWFlBYkpUUGN6RElYaGJkcHJNYTB2VEF6RHVGaVBHaHdQcE9rMFZHM05SRnlweStMNTZXTk93enN5UTQxT21uNVN6bUw5azhFTHFKaVIxYVd4TDNEQ3oxNHRsSmpna21vRE90MTd2dHJjQ1BzSTVxVUJXNGdxczB2UkdaSUVuS0FsTG5TSWhWWjRWOXlqRFNQVEI2bTExQ2t2L01YWVNUSVVVSVdWY2h3a3BhUmQ3RS9lNlZwQzVEQmpjNTdLNUozK0lGQ1BRTi80cy9ieGdkNVJVOUh6SHFiZTVGa3l2OHQwNDRhSVJQVUViRzVyRUJrU1BKUVdjQ0EwVmpqZUdzSUkyM053MUl3ZnVJRWJjZG5WNXJ1Wm1xN3pMcDZaaU1iWWNBbm0wZWdhYS9nOGtXOXNnSzRXVGppbGJqMGR1bEs2NVovU29ma3dnanZDQlhLUStSRTIzMGxmZ2JDWGZJbU5RK0ppSlNzM0hVanVZRHJvM3llakVoSHhSdjBGVWlocXZCQTlRSkIwbGtBdEh1VEFMZG9RWVhiK0lUQkFyU08xd0hId2pEVjc5UzM1emo3VHp1Y1JGOGlrK2lYazBwUGEvQUZtYWJDelJLVUJkZzAxVS96WUVtZzJSdXRUTjNxVmJJdTBndVBEOWZlMkRSWFFrOE0xcWNBMlJDSTJ0aWYveENLK21FdnpGcm8vK1hLWlArWG9yV2l6b0FPVStobEZ3aDZhLzdPUCtsNm9oZ0l6ajg0QlNFdndNK05yYVBZNy9tdFdiVVViNUQrZTU4RFhNb1NlSG1EdVFKSngyUmQ4K3FvT1QzR0ZDSjY0eTVKVTVhL09iOW0zaE8zY2hRejAwQnJ2SkpiVmhvZ0F6OVlDZ3FWWWN1LzhFOGZnZTUwUUJ2akRVNUlTZ1M4anRKM2VSQ2JicUljb1Z1VmV5b3FsKzdpVmxia0JlbkpPUHRpMmV0V0FSY2Fwb1hyajFIcGY0dnlXM2dGZjFWTENMeVR0OVljWDF3SmdsNWJYNlV1eGFDcnQ2Nyt1T2t1cjliZXlBa25wYzl4amREU3BoSnp3RExpc1BJall6UlVUZmhZL3V5c1FON1FFYlUzZU5YVllVZS9CNk9SQ0hmSWlqS2QyTllEd1JDUTQxSG9Ka3RaUDZZSzdTVURSR0xjWXh3WmtoSnByTjdvSWpNL0FySXlyUWwvQkhnTTBBeFY5VVdYeDBzTGdNTmhEUnA3bTdRWmRieWg4M21GeXR4YXlYbWVoREVma01YYzlFb1JuUHVtaFdsd3c0NnlOOWVpRHkycWN2NDB5am5YNWVQUDFaMFZhMlVCSGZ6OVV1ZFJJMStBLzNxYVV4bEtlc2NPVnZBRk56czlhdGUrVFkrbmxMUTlBWVA2VU94Y25HemNYQ1RlL05uOUJRczZOeHRuSUNTR1ErNDhkL21OR2hlZjJVSlBuVkJwZnBKUFpPdHgzZzRiczd1N1JTZ2Jpb0V6R3RTbWc1b2RYMHZDSWlrMXJBRmdFZGt6MVQ4Q251QTJhR2djNTBvd3BLMGR5R3psZU1SaWtwblEwMFpwdk9DZlV0LytQa2VzK3grQ1VEaStCZzMrWWcycHZDeEJudXg5NEFrMDc0elhjbFAyNU9ZY3VvbFNBNU40ZnJkUTk3MXFRb2lONi9USWZRL1h2T3p6Umo2ditxdnMvS1JlQW8xNElOS2t6ZkcwTW5VZ0xySTRKd2tZYlphNVhhRzhEQ1d4eTd1Q2taNDB4TENaTEJkNzFUVWFQMllWL1lDUUxXNERYdm5JdlFKWFhCNVh3WlY5ZGk1azJrMWlUOGdVSDFoV1M2L3IzVkFYd3d1a251SFBnbDh1aEhQUnAvUkhKcnVRa1oveVpKclhJZ1MxeDNNVHhWem14TmlPWi82YlkrbFBlSWlnS3Myb1V4ajNEdzhxVFZQUXNqbEl0ajB4YVY0NkNndFc2Y1lEZXRKTExHb0pVVDA2VWhKZGVGZXd4TVM2R0Z0Rkl0c09VOEtNN2dYRUF4bERZTlh2TGNQQ0JVaExDcG5tRWFIZ09GN2VPaUV1NWYxcGRya1pOUzB2WXpFZkxGK2tVNUpDTlUrakloTHpBdjRzSW9qaDhUS0kvWG42K2FVTHFpS1JsUEtKQjZaVDd3RlhrSkNQc0JUcHJlTEd3ZnlENmkwVHovMkRzVXNmcDZtSlJ3V2R4RHVhYWFMUUxnMlVQRHVwM1c3ZlpBdTVZUHlZVlF3a3VYcWtiQ0x1SEQ5S3ZXVElnQmliQjcrS2xvcnhENWQzaDBRQzhYOEl3RzVrQmRVSWZFU21jRnpxTTRmYXl4Z3NyV0RteHRPZGdZU3BzT29NdURjeVVVTmhEbnlLb3hBTXNsbCttaWxBVUtxN05wNHNDM2JhZ1d3ajRiSm5qTTYyZGgzcFRFc1FyeWZCTDFBbXBWQWROaXByVFMyTEdISEtoTm5mcXBvVU1zK2VCaVZYS1BBT3RSYzFjK0ZlazR3UnJpU2JnWkROclNEN0tZb2NZam5qWTBsNXlTMWZOc1p4SFFDeEpqVnYzMENQa1VCRk1KZ3hPRndGUjZFK2llQmxDZWZCVnFYL0FtVUt2RGNXYkYzN2EwbzZmTG1xaXQ5MWI4bU1Pdm92WU94bTZES1pvYkUvSXZiTzU4dVg2RXFNblgwSERyVVZnYVZVVzUxeEk0Q3ZOL0kwUVYwZlVueWtaanNEdkRiUlRhbStaL081eWlTaXZRbXRWWDA5a2FUMUxuK3ZGRWQvemh4TTFJU3lqQno3OU00OVhUR1RJTjBvdEMxcDZoWXNkUnZQS0huQnZkWVZ2bVZRM05pL0E0enpFOHJyY3RRMys3Qm1VdmdCVEFxNFZzZHlNZytMZDIwenlnV2JSaDJ5RXUvMjBDVFQzR0Z6bHJFTzQzNnV4VW1wYm5pSjUxcEFTUUJvNjI3TlhuYlN6L3B4RWtmbnA0cTk1VEUxWUhFTEQzQWt5WDAwMThaOWFwcm1XUEpybndMRWtsaVJncUFCVUF1S0k0QjMwV2wxK09peTdlQ2RyVm5PQTlLM29Eb1hyR0JPNnF1eHZnbW5XRFZRWlVkSXpwNG9JTkZsa29ObDkxdG9yeWRmR0JkVFk5K0F1R0VKakJFenBBWHh4REh1YS8vbFdENlQwMjl6MVhaZVptTEVYR0pJcys2dEE4ZzFZZ05SdHovVlUxOC9aR21USXJ5ZE14ZmFNdjRvMkV5NHpCMlB1VEpjSVoxaG5NV21kZlB5RDhHbEFtWjBWK25OR2N5bzVUemtwTXl1QTV1L0ZIdThRRTJqSmJybDlsc25GcmZxVXRLbzgzN3lMd2F6dGNZTlg4Z1N4NjRGMlJyY0ZucTdDbVZ6NVFINGsxSzY0MkpxQUVJTFdJbS9KOVRpa01Nb0lnU25RUFo4aXVnM0Y2eWN3WHNEN0JMVVdVdXpFbVlwaU5MVVVqS1BVZFlMbVZBWEJaV1AydmFPVFdmc3h6U0YxZ2s0eEFBVFRsZnZCekpQVXBDNlhpUU8xT3BlMG5kOVpKekRoTUhmLzdMcTFoWUNKc2dYbUtpMCtqTldrN2NtVkNxdUVFMUNLZFFVMmtmcXVNUmtZVE5oTC9sQnl3d2VSclFWYUhWNnJqUGJUakpTOVRPMkpiUEVwVVUxSkFJeVByZTlTQTd3T21RT21yV09NV2NId2NucFdNN09md0x4cElHQ2lSRFpYYTJHWXB2TTRhQU1IdkFaNi9SOUdKMVRtZ3NOTUtwNDk2VkxtRzVvb3pIdFc5MEVVSGFQbkFwcVQrcm9GVFpWUkZVazBja3pFcnpkZDlRb3hoVHZVbkNNNXlqQkJiakY2dms0SUFWWGNuSTBITG9nV2pYMFVnVG0veXNONjdEYmw0T0VUVWFwTzdUUWNJMDZENUsxazBPdXUySnl5YzdMbXB4bkdRQnJNZHFRa2NLT2tPY0Y0SFhCcFM2V1hwR3kzQUxlL1R6SE5pMzR6K1U0cGVsQmJndlk4M0x0WGdXY2FNam9SV3FIa1J0ZE5vWHNMSW1tTURXcXlJL2lQZnZ3aEhXRDVCV3FqV2FvMmh6RCtJY1hpdnZPRkk2K3pxeVNLd2VuWVpwRzVlem8weHFjemxKQ0JESU1hTkhlUWFCdmF2cVRnVWt1bXQ1Um1BNy9ScXNOa05SNEIyaElLYkZIUFFIZ2ZRTW1iQ2R1ZEpBcXZodFVIejdnYWhZNTc0YmgwYmhRNm5MV29XMmNiUnBhcEMyRVhtZjF0K21GQWhlSWtxTFU0U3dETnJDZXZvL0xyS3NHMlVwUnpTdC91K2sxVmMzZWxFLzQ4WWVUdmxSTlo2YmQ0T2FjRnpWd2pTWVJxU01OZTN3QittUU5yTXJtMXhMajgzMVdRLytHZ1htcWRZaUZYNWtWVTZGRm1TRkt4SHYzOE1wc1JjYkx1QnJUZkp4TTZhQ29lZDlSOFh6VWVvQkFLZUJxUms5T0dMK3NQR2l1VSs4SVZsV0Z3OTlHM2IyYmVoY01NL291OG0wRWxQR1ovazRNQUZ0cjNxaWZoUVZTOXc0YmFFanpzN3BaVVRJSWNmQlNUVGR5ZzE1OEYxZW5ocDc3YjVXMitDemtnQjB4aUdhQlp0OFJLRSsya0VOSlRETFcweGU0Yjc5NzcwUk43UWh5SzNrelVWVVNvM01pajVObjdBL3J1REtteFdLME1SYlluaUJnbmlHRHhBWTZhZ3lwTlVkK2hSQzdmY2pkMzdTR3JsOEh6cEhzczFsRFBYL1FZR25oQlJqckplQzYwbjBESEh2VHEyWDZwd2h3TUJIQjdCZVdxMlB6YUtTQjY4TllLVzhPRUVoMC95empCVUc4Q2Rvd1dVQlJVVHFlak04ZU5Bejd1cDJLSC9SZmswQlNDR0QrSC9Ua0t6Wk9JRnhrcFI2RzRuekRtZHpiSm92RHB5RnZ2UzczWFJ1MlJLN0xBOTNsQjUrckxyMExQNkR1VkVlcjk2cmFhbkg2UjhmWDRITzhQeER6eGhGUEZJSm1RSzlMSENpOFA2N0R2TkQyd0FWaE9wakZiQ1pvYnZyTzFOUFdpekJLUVFqTXh3Zm9XVFRQcHQwaXpzUlZqN3NSbngrVDVzdnFmYTNZSXlsY3A2QlNTMGdXc2FuNmFZMWx6aUZOUWpoeXorbjNTT1RFNUV2SmZodXZTTnYwditkMHJvYk5FbEtnWHdnSW00cnNaTU0rRmdpSUM5MVRGcWhqWitRSjFESGh4NTZtS3lValNZTHhESTVMTFFkWjRod2g2V0RUbEUyWk1Mb2owNWU0clZnc2dsMENIempBcXQ2WkV0dTEzeTAwTGZEdjNCT0YwbmxNT1RRVGZ4QVpzNHZXWW5FUDlKdHZXU0JFUEE4NW1zdncyMnlWbkdDY2dLdGR3cDZEL2FTR3EwNzJmS0VuS3hPQkdHU0l2OUZmSlNJVE1RamNxTXIrdnhSdnJBeFo3UnJ3MlRUamNHR3VnOW5kT2dpSXJ0Z1BjRVlPb0I3b1FSUXhraGNiRTgveHF3UER5blhoQlBaZVNPWk8rMm53V2pJSGhkV2JlWmZqQmtOaW1RQXQyMnZQVEtsQTZiZ21KVjAvbGVrdmRhZnJwUDBFcGNsSXN5SG1XNkVaWjZTU1JFVVl2MHZPckZwNE92bkRidEpJWHpGS2I5RmZoNE1qZUo3UGp6NXd3eHVhSW0wSHVNdEZRYytJcHBMK1R4dndXN2FTdVFCdnNLaVVRSkoxbjNPYkFjVitVS1pOa05QR0NDeHNSU3dVRi90Uy9QV1Jvc2lWVjgvRnRnV0ZQL29kcHF0MFdJWUxPUEQ4VlNoTVJvUWpjeGN3dmNyTnJWSDhia01zYndHd1FaT0pQaVBCc1dPemE4SEdIK2xyZ2haTGhvRzltV2ViWlVLVjV3bGNGZUtBOWRkbHBnZ0VwV1FLTnZEWWw2eWJqbk5xenliWFFaeG9VTHJ2N3hQSHNIREJpcUkyOTB4RnhEdG1TdW1yd2ZiU0szVGludFA0T1RDc3QxODlpYjhCVnBuWmIwcUl5N0pJZ1JxWUcwdkZUMDVXQUNuY2RBNTRxejYzTlVjQXI4Q25lSlhFY1hwYlIyWGVtcDRZekQwTUIzK2VQNDgvMGRsVVoyQkY2eVZ6Tnk0U3lZYThqaTJpM3Zpc2FuTHJ6TCsvMlBTTmlkdjl2cEFidWo0OGN1bGlRR1NyTTJzZWJWUUM3SWVmUmp6by9NcU5LbFFWRW5tK3lQaFRsbktzTW9Wcm42N2Z1a2dXTUJGOU1HRFpYVEppS1RRSWJOaUxpR3labFZnZTRUbTRacUg3ZFcyR2JXbGthajJoYmZTaGFTbkJCRDdPOGVQMjQ4TzIvT1RqQ3hJT1V5dTZlTEZTeHhLQzRlbHVRVmprRzZaQ29iYk96dUVsYVVHOUZBKytkUW9ScFVXQkRLYmNiSHZFUFFNUVRZL3RuYmlyM1FiSDhETWRsb0VPRXQ0YTRhUm9udG11U2NCdzRmYzZBUFlocG02MDdQNlhuaVVKZml0NU9hcTVYZDhQT0taS3NpdG5tN1JCZ1IwUkVJQ1ZFS21LM2xpa0RCcmNKY0UrWFZ2T0pzUGxpSiswMkc2dEZzMDZDekpteWFkL1orNFZqZEVWQVRoZmZxMG1uTktqUlNIdGNyUXp5UCtadmJWWXR4ZDRoaXNTWHcvNU56M1BOVmJQbDlLVzdwbENUbUdBWGhMVHBqQWRORFRJZVhhZU50K1VTVGx0K0hpT2tnbjcxZTl0WjlLdVNlQlJpWVlTSi9BbkZGWFdaRklkNmNLWEdtWjlGUWRKODYzbUVxWCtyWmhOUEw5UnpIR3ZrZEJOcWFHazZ1TDdqdnpnSGJzRGE5ZThqNW5iR2hKL3VBWDROUTB5L3dZRERvN2VEaG9yRHNreXBJQWR1dGRiQUlKWTBJN3l1TG93UlZjcGNSWkVuZG8rNWNoTzM2SWxtdXBEdkhjRGtxL3BpT0ZLRGorZFNYSUdPYk01d204NnM0R1Z1VENCY09obGg3Z2ZFRDJSM2JsM3JhZ3BFUWVGN2doNFd4RW9uMU1ERFkyZUNGZFo3UllCaU1xck4xUU16T1pwY2ZVNHdJMG1INS9yTHQzb0oyTzJydTNLYkVkVi9ieklhWVZ2aFNFUk56SnVJanByNTQydFpMTHl0TldjdTk5Nmk2cmRvV01jUmNSYnA1RUJMd2lhazl1V2NSNHR4WnBnZkFVbUVkZ0g2R1BSS3pGRkwra0FRSisyTDMzWmZBbWJJQ0dsMU1qbkhSeW1wREdkZHd5MEZPamk5N3N3SEd2NzNJekJTYUFpT01DanlOc1I3WTdCb3Y3UEJkZEhPMzZzTTJWc3hrR3BKeXZLN3I4ZjBXcWFUUWFOWWk1ci9NQXRxREZudWhWN3RVVTROcWlpblhMekpKU1FvOWJ4Wm1iOGtsOUlUZ2tOSnZLN3l4d3kwaDhERHNLSVQzeUJ5NWYzK2cwOE5odmN2b1YzSGdRK0xKbjg2ZFl3dUtGVEl2MDJ2LzZzMTdKOXUzdzJtV0l3RVpMZmhERGYxd28yZHdaUkxaQ3VkOGdPY2dUWndlbEdEZ1JZS0dHcWFHUUVNYmFsMFpoNGdCSC82Y1N5Z2Jvc2drY2RpS3cvUVN0OUR3d3g4N0JqVVp5NmtjcEE0RVdxV01FMVJtaHRYdGcxcHgyMDNzNU1QWGE1RW1IMGtKWmpnN2JHME01YWNBQnQ5RVhNOXlEdWtBN3BPeUVpVjBQZVhvVVBGTVBIL21PV0pqdWxzU05oWEpqbFpLR2VwWndrL3F1cmtUc05MVFZ6TGJ1VS95bkV3dENsY3pMdEtQZm8yd3VBUzJMUEc5amhzMHM2dGpPQXp4aHh5SUlQNlJORFpTV3RRbys1b1A2ZEpXK05mRkVmS1UxQ1N1bGJteVN1WU02TnNxMHhyelR5TnRubnRTQTA5c3R3MXh3N1d3VGpBUjRLcy9qdWs3TWhIK0V0TkxwTm01RENkdnBLMFVNU01YaHZ1bUc4QTlUVEpZcEtUeFJJSHdraEpJRERxQ3NQU2pVODRYMlhLZXI2eGxrRnQ3YmdqcXBsT3lZSnR1SUdHY09yaEd3d2htR0VNaTV2SGthRGhDeFlUc1pFeTZaMGZ5U0NPOHZ4QnRQc1BoVWgzbFo3aEpGdlBDM0s4NFJRakhaa0ZybWVoVWZQT01vSmxLeFF1MGxPVk1iNWQycnBTaXpyVHZoWDFUMm9ORit4NlFJTWRRcmZiOUpGWDZyeHppcGYxNmdVSW9rN2JOVGsxWjBvSkVwcmdUOXdjd3RaNjkxNjk5aDNPWXF5QVdLQ2tZSlpZMEtINHl3NlZYTm9DdUxNZTJvK2RNMFV5WmRPWnFGbjVXTUFoM0s4UjhKMEFqcTRlQ2ZKUlBIRWNtTFRwMWpvdjRacjd1bGVzY2VDWldrRUdDeGg5NWtqWWpFSlExTllpeENuUE1NSERtcllZR08xR3d5cTVlSmZEYW9UdmprY2l6SGw0Y1gvRGd4enBIYUpUQXU0dHVmdFVEeUxxNFgzbFEzTlBwT3NlMWpWYm5vVXVTdVFOY25ORFpobEhmdGIrK0NUM1RCcW5GYlZ5V29zRGk2b1pQbk5semVncDBoRC9LL0poZUhYT3p2R012S2JpUGpOb0FqeGUxZmE0MFA0NkFvWVFUSmR4NEE4WnlnbDI2QjIzYWFCelI4TE03UDFCU0pwR2NuS3FoNnIxdFZoYWNJQWJsdy9YSHJFZ21NWGxad1JJbVpHQloraVhxQWp1V0oycGh6bjFITlNZTEtuYnRydUdYZ2cvRjFYME1oTU9VWkNYRngzSmhZUW1odHdqZTZGYis0V1lnY21pYUtTWTUxendBcHNOTTRNVzY2bThSc25UclhiWW9UQU1UQUg2d21nZlRucU4yR005QzFKT2VCQWt3empWNUI3aE9ZTzdxS3pkS0pSRzdyUW5HRFZYbFpKSVlUbzVLTE5QRXluMzZIZjJTSVhFeTMwYkhCa1VZMU5DQlJLQitiRFl1Z3Y1RGg2aFZVWFBuSmNzZ3R4dGhwdytSa3hhTzNQVFF0Nk9hdnJRVEd3N1c5bW5qbXdaaUZLMk1sRk1lVm1DTGhjWjRud2NPQVAvd3Z3c2d5dk5qdCszYnlUQzZpS1BreGFiR1VjTXI0aSs1ZTJWeFJSSHM5NWUwWHhFcVBLMjB1WUYxU3ozYXZRc0hXcEtzd0MzRzc2ZVNWWTlDNTdMYitES3VUY1VLTTl3czN0bG56MXRldmtEazQ2NUI5WlZmSlJ3YjdMaTRXN3hmNmFYL0E4V2R6djBXOHBlVVl4VE9kUjlMandYcU5JYVdEWGJVaVFudXhybGdNZkZCY3U2ZTZWOHlXVG9yR0pLME5FS3ROeUdMeCtCSGVMKzVRUUNQOHQrVFhJMDFHaEpMQkZjQm1MdW9sOUNXbVJvdjM1YzJLMmhWRm4vaVhOQldaaHc2OCszVUo3WW9QZDM1S3ZvOGhaN2M0RCtxRjlTdXV4T2xwREpqazh2eXU4b2Y4MDFtTGxPL3JkWnVSc3U2NUZvWXhnaWZWR2phUnVXajFsdHpXbWFXekZoU3F5T1NkWU9iSlNLeUxJNDJoK3NxbFBxK2UrVG9tYXZsVTNKOUZOZkxHenJiQzA0OU9KbWN6dFBmTnhNL1QwckYxQitVdHJ2WGttNzR0QzU3R1ROQ0VTdEU2K1BhM1ZRaGdFcHRYdE5GVU1aOHVlYUVEZmd6bUdXN0tWYWdzMGVBN09HSjJZMTV2NmVVWllsRHRQQ0M4UExXQUhsQktaZ0NjNnFEa05vQUZXTU9Xa2tjaHFubXQxMDdGcFdzcGF4SkVQaFE5UjJ3WUhhK2kzUW8xbnpGOFQrNGpQZ2QyMnVhTTZ4NG9ZQlU3M1RROHlxNGxKMDNlNkRGOXNvTVFDTndjVXNMQnkrUWIvRUs4ZmxCdk4xNHE5ZFlGQ2hvQ1JtaWQ2VElrbmR6YXllU2lmYU5JVHJBdkw2dUhSa3g5ZHRpMHJ2d0xtSjJBWTUxSTN1WUpuKzVXOFlJdmFvdGhxREFSK29ERHhsRUowS1JOckM0ZUNiN1lGdWRNVUNrZkhSWENEdlc1QzZaTjhmakFGL0xwdXU0K1hSdlJ5Sm1hYTQxTHA0aVhwWHJSSW9tVEJCSFVwOTFzUURnVG9Cc0haV3lORzdUY2dsTzhuSnZKc0RObkRqRlZUTDZUMVBqaXM5QzR6dExSVEk1ZUEwQTYyeTRWNTBoY0pQRXlndGpYTnlLRVJSdDRZRno2Ni9jSWRlZVlUdi9nTkVWbHdqMGNRSjlMUkdrQ0M5YmpWYlY4Sy95SVZ5NUhpdVc5b2R4dDNnQThhRWxiQklBdDYwQjVkL0tFcXlUSHk0UEpXb0FPTWtPZzhOTTJ4T1VoM3BINFFXa08xSHNxRkRQVkJ0UDFaZUhMdWxQSHFvUDNqSGRwdDRaN3prbm5MT25PYnVPZjBrTjBZOHBaVTE3bHlEWElJSjh2c2V3bVZ1b1YrOE9OTlp3TnRyaDRqQlYvS2NqcHFVdnNpYmFvQW5BVXgxdmJ6VHE2S1hwY2J3b05jMXdScHJUN2dVR0lkRWVRaXNXWktwaGxYSG5kcTZqZzQrdkY0SjJXMGJPZGNPbTJxQzVSQVpIMjVjbTRuQzVhMTE1ZUh2a1FmRnlJWU9nVGE5YjNQTnZzV2RmTUNvaERsMTNtalI5NTJkdmgxcWhNaUlDbmpadUFndVdMNXpuYStmeEtuOTNaZmk0NXE1dTZ2RWw3ellWSC93anA5aDlYRjh6OW0yVWRnM0ZMMVltZE1BWm1Oc2tkK2xUU09PTm1kTklaWERMZlcxRzIySk9UaTg1VmN6bEZCdDhmaEpvU3RMWko5U1EzQWhnaFhaMHF2SmxHQXBnMkt1SytZRmdSc0M5UGZDdG5wRlYvZ1FSOWczTUpBclF5UDVyUFN1dUxkdkI2T3V3R0RUeTFKMWhaYjdGbHBLeWRlVDdxS0hxdUlNTW9mK1BNcGllWk5PbDR6WTZOcGorSE9OWUFNQTlJOEpIVXU5YUU5SzB6VUpHeVVBNFJ4dlViQTFzakcvdk5DdEs3WUozbnFZWU5RRWYwK0VJdnNrTlZHK1NWeGV1a2V2QUdWUHpOa0tRVXQ2djFNUWsrZnJ2NmlFUmxiL2lNcTFpcmNnQWlNRFJwNjFFOW1Xc3lhbVpoUWE2ajNHbTgzNllHdDNGSVN0dUxNTjRWTi9BcEFzN2pFU3p5YnJzd3Q2QVJnVDgwakxlWkJXV1Q1ZE93RTlVK0lWVG02QXVBY2hOSisvelhSM1BQbGNvVlhEZ3lzbHlmNW14YmZWbmV4R2x4czJxRmNFdmxSVTRMZ3ludmh0WURhVzN2RWxvL0NyQ2VHTThsdmVEOTBaQ2t6WWU2ZjRsM1AxOURIQWtVTEpzMGNVVDlIbEpacVBTMWpNRy9ENFhTSmhKc1hHcTk1ZlNaRk1GWXZyYlYxUTNYZnd4R1l6aTg2c2l4c2cwMHh5VDYvSm9IL3RURzlqaHpQNEZ2cTV3eWw1dWJrdTRIZTh1VjFxSzM5Mkxrbzdja1JweWVVcVFraFd1bU9sNE9QZW5BbTRwU2lWN1JIVG5rVDFHSTRMVlBLVlFSd251ZEdKOEZqYWtEN2VkeVBDL21FQSt5cHJKWDUxaTwveGVuYzpDaXBoZXJWYWx1ZT48L3hlbmM6Q2lwaGVyRGF0YT48L3hlbmM6RW5jcnlwdGVkRGF0YT48L3NhbWwyOkVuY3J5cHRlZEFzc2VydGlvbj48L3NhbWwycDpSZXNwb25zZT4=";

    private static final String IDP_METADATA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<EntityDescriptor xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:mdalg=\"urn:oasis:names:tc:SAML:metadata:algsupport\" xmlns:mdui=\"urn:oasis:names:tc:SAML:metadata:ui\" xmlns:shibmd=\"urn:mace:shibboleth:metadata:1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" entityID=\"https://idp.testshib.org/idp/shibboleth\">\n" +
        "  <Extensions>\n" +
        "    <mdalg:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\" />\n" +
        "    <mdalg:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#sha384\" />\n" +
        "    <mdalg:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\" />\n" +
        "    <mdalg:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\" />\n" +
        "    <mdalg:SigningMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha512\" />\n" +
        "    <mdalg:SigningMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha384\" />\n" +
        "    <mdalg:SigningMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\" />\n" +
        "    <mdalg:SigningMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\" />\n" +
        "  </Extensions>\n" +
        "  <IDPSSODescriptor protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:1.1:protocol urn:mace:shibboleth:1.0 urn:oasis:names:tc:SAML:2.0:protocol\">\n" +
        "    <Extensions>\n" +
        "      <shibmd:Scope regexp=\"false\">testshib.org</shibmd:Scope>\n" +
        "      <mdui:UIInfo>\n" +
        "        <mdui:DisplayName xml:lang=\"en\">TestShib Test IdP</mdui:DisplayName>\n" +
        "        <mdui:Description xml:lang=\"en\">TestShib IdP. Use this as a source of attributes\n" +
        "                        for your test SP.</mdui:Description>\n" +
        "        <mdui:Logo height=\"88\" width=\"253\">https://www.testshib.org/testshibtwo.jpg</mdui:Logo>\n" +
        "      </mdui:UIInfo>\n" +
        "    </Extensions>\n" +
        "    <KeyDescriptor>\n" +
        "      <ds:KeyInfo>\n" +
        "        <ds:X509Data>\n" +
        "          <ds:X509Certificate>MIIEDjCCAvagAwIBAgIBADANBgkqhkiG9w0BAQUFADBnMQswCQYDVQQGEwJVUzEV\n" +
        "                            MBMGA1UECBMMUGVubnN5bHZhbmlhMRMwEQYDVQQHEwpQaXR0c2J1cmdoMREwDwYD\n" +
        "                            VQQKEwhUZXN0U2hpYjEZMBcGA1UEAxMQaWRwLnRlc3RzaGliLm9yZzAeFw0wNjA4\n" +
        "                            MzAyMTEyMjVaFw0xNjA4MjcyMTEyMjVaMGcxCzAJBgNVBAYTAlVTMRUwEwYDVQQI\n" +
        "                            EwxQZW5uc3lsdmFuaWExEzARBgNVBAcTClBpdHRzYnVyZ2gxETAPBgNVBAoTCFRl\n" +
        "                            c3RTaGliMRkwFwYDVQQDExBpZHAudGVzdHNoaWIub3JnMIIBIjANBgkqhkiG9w0B\n" +
        "                            AQEFAAOCAQ8AMIIBCgKCAQEArYkCGuTmJp9eAOSGHwRJo1SNatB5ZOKqDM9ysg7C\n" +
        "                            yVTDClcpu93gSP10nH4gkCZOlnESNgttg0r+MqL8tfJC6ybddEFB3YBo8PZajKSe\n" +
        "                            3OQ01Ow3yT4I+Wdg1tsTpSge9gEz7SrC07EkYmHuPtd71CHiUaCWDv+xVfUQX0aT\n" +
        "                            NPFmDixzUjoYzbGDrtAyCqA8f9CN2txIfJnpHE6q6CmKcoLADS4UrNPlhHSzd614\n" +
        "                            kR/JYiks0K4kbRqCQF0Dv0P5Di+rEfefC6glV8ysC8dB5/9nb0yh/ojRuJGmgMWH\n" +
        "                            gWk6h0ihjihqiu4jACovUZ7vVOCgSE5Ipn7OIwqd93zp2wIDAQABo4HEMIHBMB0G\n" +
        "                            A1UdDgQWBBSsBQ869nh83KqZr5jArr4/7b+QazCBkQYDVR0jBIGJMIGGgBSsBQ86\n" +
        "                            9nh83KqZr5jArr4/7b+Qa6FrpGkwZzELMAkGA1UEBhMCVVMxFTATBgNVBAgTDFBl\n" +
        "                            bm5zeWx2YW5pYTETMBEGA1UEBxMKUGl0dHNidXJnaDERMA8GA1UEChMIVGVzdFNo\n" +
        "                            aWIxGTAXBgNVBAMTEGlkcC50ZXN0c2hpYi5vcmeCAQAwDAYDVR0TBAUwAwEB/zAN\n" +
        "                            BgkqhkiG9w0BAQUFAAOCAQEAjR29PhrCbk8qLN5MFfSVk98t3CT9jHZoYxd8QMRL\n" +
        "                            I4j7iYQxXiGJTT1FXs1nd4Rha9un+LqTfeMMYqISdDDI6tv8iNpkOAvZZUosVkUo\n" +
        "                            93pv1T0RPz35hcHHYq2yee59HJOco2bFlcsH8JBXRSRrJ3Q7Eut+z9uo80JdGNJ4\n" +
        "                            /SJy5UorZ8KazGj16lfJhOBXldgrhppQBb0Nq6HKHguqmwRfJ+WkxemZXzhediAj\n" +
        "                            Geka8nz8JjwxpUjAiSWYKLtJhGEaTqCYxCCX2Dw+dOTqUzHOZ7WKv4JXPK5G/Uhr\n" +
        "                            8K/qhmFT2nIQi538n6rVYLeWj8Bbnl+ev0peYzxFyF5sQA==</ds:X509Certificate>\n" +
        "        </ds:X509Data>\n" +
        "      </ds:KeyInfo>\n" +
        "      <EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#aes256-cbc\" />\n" +
        "      <EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#aes192-cbc\" />\n" +
        "      <EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#aes128-cbc\" />\n" +
        "      <EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#tripledes-cbc\" />\n" +
        "      <EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p\" />\n" +
        "      <EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#rsa-1_5\" />\n" +
        "    </KeyDescriptor>\n" +
        "    <ArtifactResolutionService Binding=\"urn:oasis:names:tc:SAML:1.0:bindings:SOAP-binding\" Location=\"https://idp.testshib.org:8443/idp/profile/SAML1/SOAP/ArtifactResolution\" index=\"1\" />\n" +
        "    <ArtifactResolutionService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:SOAP\" Location=\"https://idp.testshib.org:8443/idp/profile/SAML2/SOAP/ArtifactResolution\" index=\"2\" />\n" +
        "    <NameIDFormat>urn:mace:shibboleth:1.0:nameIdentifier</NameIDFormat>\n" +
        "    <NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:transient</NameIDFormat>\n" +
        "    <SingleSignOnService Binding=\"urn:mace:shibboleth:1.0:profiles:AuthnRequest\" Location=\"https://idp.testshib.org/idp/profile/Shibboleth/SSO\" />\n" +
        "    <SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"https://idp.testshib.org/idp/profile/SAML2/POST/SSO\" />\n" +
        "    <SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\" Location=\"https://idp.testshib.org/idp/profile/SAML2/Redirect/SSO\" />\n" +
        "    <SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:SOAP\" Location=\"https://idp.testshib.org/idp/profile/SAML2/SOAP/ECP\" />\n" +
        "  </IDPSSODescriptor>\n" +
        "  <Organization>\n" +
        "    <OrganizationName xml:lang=\"en\">TestShib Two Identity Provider</OrganizationName>\n" +
        "    <OrganizationDisplayName xml:lang=\"en\">TestShib Two</OrganizationDisplayName>\n" +
        "    <OrganizationURL xml:lang=\"en\">http://www.testshib.org/testshib-two/</OrganizationURL>\n" +
        "  </Organization>\n" +
        "  <ContactPerson contactType=\"technical\">\n" +
        "    <GivenName>Nate</GivenName>\n" +
        "    <SurName>Klingenstein</SurName>\n" +
        "    <EmailAddress>ndk@internet2.edu</EmailAddress>\n" +
        "  </ContactPerson>\n" +
        "</EntityDescriptor>";

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
        samlResponseRequest.setParameter("SAMLResponse", SAML_RESPONSE);

        HttpServletRequestAdapter inTransport = new HttpServletRequestAdapter(samlResponseRequest);
        BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject> context = new BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject>();
        context.setCommunicationProfileId("urn:oasis:names:tc:SAML:2.0:profiles:SSO:browser");

        DOMMetadataProvider metadataProvider = new DOMMetadataProvider(openSAML.getParserPool().parse(new StringReader(IDP_METADATA)).getDocumentElement());
        metadataProvider.initialize();
        X509Certificate idpCertificate = metadataProvider.getEntityDescriptor("https://idp.testshib.org/idp/shibboleth").getIDPSSODescriptor("urn:oasis:names:tc:SAML:2.0:protocol").getKeyDescriptors().get(0).getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0);
        java.security.cert.X509Certificate idpSigningCert = X509Util.decodeCertificate(Base64.decode(idpCertificate.getValue())).iterator().next();

        Assert.assertNotNull(idpSigningCert);
        context.setMetadataProvider(metadataProvider);

        List<KeyInfoProvider> keyInfoProviders = new ArrayList<KeyInfoProvider>(4);
        keyInfoProviders.add(new InlineX509DataProvider());
        keyInfoProviders.add(new KeyInfoReferenceProvider());
        keyInfoProviders.add(new DEREncodedKeyValueProvider());
        keyInfoProviders.add(new RSAKeyValueProvider());
        keyInfoProviders.add(new DSAKeyValueProvider());
        BasicProviderKeyInfoCredentialResolver keyInfoCredentialResolver = new BasicProviderKeyInfoCredentialResolver(keyInfoProviders);
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolver(metadataProvider);
        ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(metadataCredentialResolver, keyInfoCredentialResolver);
        SAMLProtocolMessageXMLSignatureSecurityPolicyRule xmlSignatureRule = new SAMLProtocolMessageXMLSignatureSecurityPolicyRule(trustEngine);
        SecurityPolicy policy = new BasicSecurityPolicy();
        policy.getPolicyRules().add(xmlSignatureRule);

        context.setSecurityPolicyResolver(new StaticSecurityPolicyResolver(policy));
        context.setInboundMessageTransport(inTransport);
        context.setInboundSAMLProtocol(SAMLConstants.SAML20P_NS);
        context.setPeerEntityRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        context.setOutboundMessageTransport(new OutputStreamOutTransportAdapter(output));
        context.setOutboundSAMLProtocol(SAMLConstants.SAML20P_NS);

        HTTPPostDecoder decoder = new HTTPPostDecoder();
        decoder.decode(context);
        Response samlMessage = (Response) context.getInboundSAMLMessage();
        System.out.println("Response:");
        System.out.println(XMLHelper.prettyPrintXML(samlMessage.getDOM()));
        List<EncryptedAssertion> encryptedAssertions = samlMessage.getEncryptedAssertions();
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(new FileInputStream(config.getKeyStorePath()), config.getKeyStorePassword() == null ? null : config.getKeyStorePassword().toCharArray());

        BasicCredential decryptCredential = new BasicCredential();
        decryptCredential.setUsageType(UsageType.ENCRYPTION);
        PrivateKeyEntry entry = (PrivateKeyEntry) keystore.getEntry(config.getEncryptionKeyAlias(), new PasswordProtection(config.getEncryptionKeyPassword().toCharArray()));
        decryptCredential.setPrivateKey(entry.getPrivateKey());
        StaticKeyInfoCredentialResolver skicr = new StaticKeyInfoCredentialResolver(decryptCredential);

        Decrypter samlDecrypter = new Decrypter(null, skicr, new InlineEncryptedKeyResolver());
        samlDecrypter.setRootInNewDocument(true);
        Assert.assertEquals(1, encryptedAssertions.size());
        SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
        BasicCredential idpSigningCredential = new BasicCredential();
        idpSigningCredential.setEntityId("https://idp.testshib.org/idp/shibboleth");
        idpSigningCredential.setPublicKey(idpSigningCert.getPublicKey());
        idpSigningCredential.setUsageType(UsageType.SIGNING);
        SignatureValidator signatureValidator = new SignatureValidator(idpSigningCredential);
        for (EncryptedAssertion encryptedAssertion : encryptedAssertions) {
            Assertion assertion = samlDecrypter.decrypt(encryptedAssertion);
            Element assertionElement = assertion.getDOM();
            Document ownerDocument = assertionElement.getOwnerDocument();
            System.out.println("Assertion:");
            System.out.println(XMLHelper.prettyPrintXML(assertionElement));
            System.out.println("Owner DOC:");
            System.out.println(XMLHelper.prettyPrintXML(ownerDocument));

            Assert.assertTrue(assertion.isSigned());
            Signature signature = assertion.getSignature();
            profileValidator.validate(signature);
            signatureValidator.validate(signature);
            CriteriaSet criteriaSet = new CriteriaSet();
            criteriaSet.add(new EntityIDCriteria("https://idp.testshib.org/idp/shibboleth"));
            criteriaSet.add(new MetadataCriteria(IDPSSODescriptor.DEFAULT_ELEMENT_NAME, SAMLConstants.SAML20P_NS));
            criteriaSet.add(new UsageCriteria(UsageType.SIGNING));
            if (!trustEngine.validate(signature, criteriaSet)) {
                Assert.fail();
            }
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
        SimHttpServletRequest samlResponseRequest = new SimHttpServletRequest();
        samlResponseRequest.setRequestURI(new URI(config.getAssertionConsumerServiceURL()).getPath());
        samlResponseRequest.setRequestURL(config.getAssertionConsumerServiceURL());
        samlResponseRequest.setMethod("POST");
        samlResponseRequest.setParameter("SAMLResponse", SAML_RESPONSE);
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
        response.setDestination("http://some.where.at.ox/acs");
        response.setID("_" + UUIDs.getUnformattedString(UUID.randomUUID()));
        response.setInResponseTo("_" + UUIDs.getUnformattedString(UUID.randomUUID()));
        response.setIssueInstant(new DateTime());
        response.setVersion(SAMLVersion.VERSION_20);

        Issuer responseIssuer = openSAML.buildSAMLObject(Issuer.class);
        responseIssuer.setValue("http://somewhere.at.ox/idp");
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
        assertionIssuer.setValue("http://somewhere.at.ox/idp");
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
        subjectConfirmationData.setRecipient("http://some.where.at.ox/acs");
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subject.getSubjectConfirmations().add(subjectConfirmation);
        assertion.setSubject(subject);

        Conditions conditions = openSAML.buildSAMLObject(Conditions.class);
        conditions.setNotBefore(new DateTime(System.currentTimeMillis() - 60 * 1000));
        conditions.setNotOnOrAfter(new DateTime(System.currentTimeMillis() + 60 *60 * 1000));
        AudienceRestriction audienceRestriction = openSAML.buildSAMLObject(AudienceRestriction.class);
        Audience audience = openSAML.buildSAMLObject(Audience.class);
        audience.setAudienceURI("http://some.where.at.ox/acs");
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
