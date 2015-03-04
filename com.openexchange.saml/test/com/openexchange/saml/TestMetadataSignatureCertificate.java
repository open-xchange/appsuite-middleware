package com.openexchange.saml;
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



import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Principal;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorStreamException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
import org.opensaml.saml2.binding.security.SAML2HTTPRedirectDeflateSignatureRule;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.metadata.AuthnQueryService;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.DOMMetadataProvider;
import org.opensaml.security.MetadataCredentialResolver;
import org.opensaml.ws.security.provider.BasicSecurityPolicy;
import org.opensaml.ws.security.provider.StaticSecurityPolicyResolver;
import org.opensaml.ws.transport.OutputStreamOutTransportAdapter;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.BasicProviderKeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.KeyInfoProvider;
import org.opensaml.xml.security.keyinfo.provider.InlineX509DataProvider;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.util.Base64;


/**
 * {@link TestMetadataSignatureCertificate}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class TestMetadataSignatureCertificate {

    private static final String AUTHN_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" \n" +
        "    AssertionConsumerServiceURL=\"http://localhost/sp/acs\" \n" +
        "    ID=\"dnbjbbemgmnmcplpfojepmnemofmlaffclfjpbnh\" \n" +
        "    IsPassive=\"false\" \n" +
        "    IssueInstant=\"2015-02-20T19:05:25Z\" \n" +
        "    ProtocolBinding=\"urn:oasis:names.tc:SAML:2.0:bindings:HTTP-Redirect\" \n" +
        "    ProviderName=\"test\" \n" +
        "    Destination=\"http://localhost/idp/authn\" \n" +
        "    Version=\"2.0\">\n" +
        "    \n" +
        "    <saml:Issuer xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">http://localhost/sp/test</saml:Issuer>\n" +
        "</samlp:AuthnRequest>";

    private DOMMetadataProvider metadataProvider;

    private static byte[] CERT;

    private static KeyPair SIGNING_KEY_PAIR;

    private static String SP_METADATA;

    @BeforeClass
    public static void beforeClass() throws Exception {
        DefaultBootstrap.bootstrap();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPairGenerator.initialize(1024);
        SIGNING_KEY_PAIR = keyPairGenerator.generateKeyPair();

     // generate the certificate
     // first parameter  = Algorithm
     // second parameter = signrature algorithm
     // third parameter  = the provider to use to generate the keys (may be null or
//                         use the constructor without provider)
//     sun.security.CertAndKeyGen certGen = new sun.security.CertAndKeyGen("RSA", "SHA256WithRSA", null);
     // generate it with 2048 bits
//     certGen.generate(2048);

     // prepare the validity of the certificate
//     long validSecs = (long) 365 * 24 * 60 * 60; // valid for one year
     // add the certificate information, currently only valid for one year.
//     X509Certificate cert = certGen.getSelfCertificate(
        // enter your details according to your application
//        new sun.security.X500Name("CN=My Application,O=My Organisation,L=My City,C=DE"), validSecs);


//        AsymmetricKeyParameter asymmetricKeyParameter = PrivateKeyFactory.createKey(SIGNING_KEY_PAIR.getPrivate().getEncoded());
//        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("DSAWithSHA1");
//        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
//        AlgorithmIdentifier sigAlgId = new AlgorithmIdentifier(new ASN1ObjectIdentifier("1.2.840.10040.4.1"));
//        AlgorithmIdentifier digAlgId = new AlgorithmIdentifier((ASN1ObjectIdentifier)null);
//        ContentSigner contentSigner = new JcaContentSignerBuilder("DSA").build(SIGNING_KEY_PAIR.getPrivate());
//        X509CertificateHolder certificateHolder = new X509v3CertificateBuilder(
//            new X500Name("CN=" + "tester"),
//            BigInteger.valueOf(new SecureRandom().nextLong()),
//            new Date(System.currentTimeMillis() - 10000),
//            new Date(System.currentTimeMillis() + 24L * 3600 * 1000),
//            new X500Name("CN=" + "tester"),
//            SubjectPublicKeyInfo.getInstance(SIGNING_KEY_PAIR.getPublic().getEncoded())).build(new ContentSigner()
//            {
//                private SignatureOutputStream stream = new SignatureOutputStream(Signature.getInstance("DSA"));
//
//                @Override
//                public AlgorithmIdentifier getAlgorithmIdentifier()
//                {
//                    return new AlgorithmIdentifier(new ASN1ObjectIdentifier("1.2.840.10040.4.1"));
//                }
//
//                @Override
//                public OutputStream getOutputStream()
//                {
//                    return stream;
//                }
//
//                @Override
//                public byte[] getSignature()
//                {
//                    try
//                    {
//                        return stream.getSignature();
//                    }
//                    catch (SignatureException e)
//                    {
//                        throw new RuntimeOperatorException("exception obtaining signature: " + e.getMessage(), e);
//                    }
//                }
//            });
//        CERT = certificateHolder.getEncoded();

        SP_METADATA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<md:EntityDescriptor xmlns:md=\"urn:oasis:names:tc:SAML:2.0:metadata\" entityID=\"http://localhost/sp/test\">\n" +
        "  <md:SPSSODescriptor protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n" +
        "    <md:KeyDescriptor use=\"signing\">\n" +
        "      <ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">\n" +
        "        <ds:X509Data>\n" +
        "          <ds:X509Certificate>\n" +
                     Base64.encodeBytes(CERT) +
        "          </ds:X509Certificate>\n" +
        "        </ds:X509Data>\n" +
        "      </ds:KeyInfo>\n" +
        "    </md:KeyDescriptor>\n" +
        "    <md:NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:transient</md:NameIDFormat>\n" +
        "    <md:AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\" Location=\"http://localhost/sp/acs\" index=\"0\" />\n" +
        "    <md:AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"http://localhost/sp/acs\" index=\"1\" />\n" +
        "  </md:SPSSODescriptor>\n" +
        "</md:EntityDescriptor>";
    }

    @Before
    public void before() throws Exception {
        metadataProvider = new DOMMetadataProvider(Configuration.getParserPool().parse(new StringReader(SP_METADATA)).getDocumentElement());
        metadataProvider.initialize();
    }

    @Test
    public void printAlgorithms() throws Exception {
        final DefaultSignatureAlgorithmIdentifierFinder finder = new DefaultSignatureAlgorithmIdentifierFinder();
        for (Provider p : Security.getProviders()) {
            System.out.println("Provider: "  + p.getName());
            final Set<String> found = new HashSet<String>();
            final Set<String> missing = new HashSet<String>();
            for (Service service : p.getServices()) {
                if ("Signature".equals(service.getType())) {
                    final String algorithm = service.getAlgorithm();
                    try {
                        finder.find(algorithm);
                        found.add(algorithm);
                    } catch (IllegalArgumentException ex) {
                        missing.add(algorithm);
                    }
                }
            }
            System.out.println("Found: " + found);
            System.out.println("Missing: " + missing);
        }
    }

    @Test
    public void testValidationWithMetadataProvider() throws Exception {
        /*
         * Fake resource request that causes redirect to IDP with authentication request
         */
        BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject> spContext = new BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject>();
        spContext.setCommunicationProfileId("urn:oasis:names:tc:SAML:2.0:profiles:SSO:browser");
        SimHttpServletResponse accessResourceResponse = new SimHttpServletResponse();
        HttpServletResponseAdapter accessResourceOutTransport = new HttpServletResponseAdapter(accessResourceResponse, true);
        spContext.setOutboundMessageTransport(accessResourceOutTransport);
        spContext.setOutboundSAMLProtocol(SAMLConstants.SAML20P_NS);
        spContext.setOutboundSAMLMessage((AuthnRequest) Configuration.getUnmarshallerFactory().getUnmarshaller(AuthnRequest.DEFAULT_ELEMENT_NAME).unmarshall(Configuration.getParserPool().parse(new StringReader(AUTHN_REQUEST)).getDocumentElement()));
        spContext.setOutboundMessageIssuer("http://localhost/sp/test");
        spContext.setOutboundSAMLMessageId(UUID.randomUUID().toString());
        spContext.setOutboundSAMLMessageIssueInstant(new DateTime());
        BasicCredential credential = new BasicCredential();
        credential.setUsageType(UsageType.SIGNING);
        credential.setEntityId("tester");
        credential.setPrivateKey(SIGNING_KEY_PAIR.getPrivate());
        spContext.setOutboundSAMLMessageSigningCredential(credential);
        spContext.setPeerEntityRole(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        AuthnQueryService endpoint = (AuthnQueryService) Configuration.getBuilderFactory().getBuilder(AuthnQueryService.DEFAULT_ELEMENT_NAME).buildObject(AuthnQueryService.DEFAULT_ELEMENT_NAME);
        endpoint.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
        endpoint.setLocation("http://localhost/idp/authn");
        spContext.setPeerEntityEndpoint(endpoint);
        new HTTPRedirectDeflateEncoder().encode(spContext);

        /*
         * Fake redirect request based on location header
         */
        String location = accessResourceResponse.getHeader("location");
        SimHttpServletRequest authnServletRequest = new SimHttpServletRequest();
        authnServletRequest.setRequestURI(location.substring(0, location.indexOf('?')));
        authnServletRequest.setRequestURL(authnServletRequest.getRequestURI());
        authnServletRequest.setMethod("GET");
        authnServletRequest.setQueryString(location.substring(location.indexOf('?') + 1));
        for (String kv : new URI(location).getQuery().split("&")) {
            int i = kv.indexOf('=');
            authnServletRequest.setParameter(kv.substring(0, i), kv.substring(i + 1));
        }

        /*
         * IDP processes authentication request
         */
        HttpServletRequestAdapter authnRequestInTransport = new HttpServletRequestAdapter(authnServletRequest);
        BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject> idpContext = new BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject>();
        idpContext.setCommunicationProfileId("urn:oasis:names:tc:SAML:2.0:profiles:SSO:browser");
        idpContext.setMetadataProvider(metadataProvider);
        List<KeyInfoProvider> keyInfoProviders = new ArrayList<KeyInfoProvider>(4);
        keyInfoProviders.add(new InlineX509DataProvider());
        BasicProviderKeyInfoCredentialResolver keyInfoCredentialResolver = new BasicProviderKeyInfoCredentialResolver(keyInfoProviders);
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolver(metadataProvider);
        ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(metadataCredentialResolver, keyInfoCredentialResolver);
        SAML2HTTPRedirectDeflateSignatureRule rule = new SAML2HTTPRedirectDeflateSignatureRule(trustEngine);
        BasicSecurityPolicy policy = new BasicSecurityPolicy();
        policy.getPolicyRules().add(rule);
        idpContext.setSecurityPolicyResolver(new StaticSecurityPolicyResolver(policy));
        idpContext.setInboundMessageTransport(authnRequestInTransport);
        idpContext.setInboundSAMLProtocol(SAMLConstants.SAML20P_NS);
        idpContext.setPeerEntityRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        idpContext.setOutboundMessageTransport(new OutputStreamOutTransportAdapter(output));
        idpContext.setOutboundSAMLProtocol(SAMLConstants.SAML20P_NS);
        new HTTPRedirectDeflateDecoder().decode(idpContext);
    }

    private static class SignatureOutputStream
        extends OutputStream
    {
        private Signature sig;

        SignatureOutputStream(Signature sig)
        {
            this.sig = sig;
        }

        @Override
        public void write(byte[] bytes, int off, int len)
            throws IOException
        {
            try
            {
                sig.update(bytes, off, len);
            }
            catch (SignatureException e)
            {
                throw new OperatorStreamException("exception in content signer: " + e.getMessage(), e);
            }
        }

        @Override
        public void write(byte[] bytes)
            throws IOException
        {
            try
            {
                sig.update(bytes);
            }
            catch (SignatureException e)
            {
                throw new OperatorStreamException("exception in content signer: " + e.getMessage(), e);
            }
        }

        @Override
        public void write(int b)
            throws IOException
        {
            try
            {
                sig.update((byte)b);
            }
            catch (SignatureException e)
            {
                throw new OperatorStreamException("exception in content signer: " + e.getMessage(), e);
            }
        }

        byte[] getSignature()
            throws SignatureException
        {
            return sig.sign();
        }
    }

    private static class SimHttpServletRequest implements HttpServletRequest {

        private String charset;
        private final Map<String, Object> attributes;
        private final Map<String, String> headers;
        private final Map<String, String> parameters;
        private ServletInputStream inputStream;
        private String protocol;
        private String scheme;
        private String serverName;
        private int serverPort;
        private String remoteAddr;
        private String remoteHost;
        private Locale locale;
        private boolean secure;
        private RequestDispatcher requestDispatcher;
        private String realPath;
        private int remotePort;
        private String localName;
        private String localAddr;
        private int localPort;
        private String authType;
        private List<String> cookies;
        private long dateHeader;
        private String pathInfo;
        private String method;
        private Principal principal;
        private String requestedSessionId;
        private String requestURI;
        private String requestURL;
        private String remoteUser;
        private String queryString;
        private String contextPath;
        private String pathTranslated;
        private String servletPath;
        private HttpSession httpSession;

        public SimHttpServletRequest() {
            super();
            attributes = new HashMap<String, Object>(4);
            headers = new HashMap<String, String>(8);
            parameters = new HashMap<String, String>(8);
        }

        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public Enumeration getAttributeNames() {
            final Iterator<String> iterator = attributes.keySet().iterator();
            return new Enumeration() {

                @Override
                public boolean hasMoreElements() {
                    return iterator.hasNext();
                }

                @Override
                public Object nextElement() {
                    return iterator.next();
                }
            };
        }

        @Override
        public String getCharacterEncoding() {
            return charset;
        }

        @Override
        public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
            charset = env;
        }

        @Override
        public int getContentLength() {
            final String string = headers.get("content-length");
            if (null == string) {
                return -1;
            }

            try {
                final int ret = (int) Long.parseLong(string);
                return ret < 0 ? -1 : ret;
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        public void setContentLength(long contentLength) {
            headers.put("content-length", Long.toString(contentLength));
        }

        @Override
        public String getContentType() {
            return headers.get("content-type");
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return inputStream;
        }

        public void setInputStream(ServletInputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void setParameter(String name, String value) {
            parameters.put(name, value);
        }

        @Override
        public String getParameter(String name) {
            return parameters.get(name);
        }

        @Override
        public Enumeration getParameterNames() {
            final Iterator<String> iterator = parameters.keySet().iterator();
            return new Enumeration() {

                @Override
                public boolean hasMoreElements() {
                    return iterator.hasNext();
                }

                @Override
                public Object nextElement() {
                    return iterator.next();
                }
            };
        }

        @Override
        public String[] getParameterValues(String name) {
            String string = parameters.get(name);
            return null == string ? null : new String[] {string};
        }

        @Override
        public Map getParameterMap() {
            return parameters;
        }

        @Override
        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        @Override
        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        @Override
        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        @Override
        public int getServerPort() {
            return serverPort;
        }

        public void setServerPort(int serverPort) {
            this.serverPort = serverPort;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(inputStream, charset));
        }

        @Override
        public String getRemoteAddr() {
            return remoteAddr;
        }

        public void setRemoteAddr(String remoteAddr) {
            this.remoteAddr = remoteAddr;
        }

        @Override
        public String getRemoteHost() {
            return remoteHost;
        }

        public void setRemoteHost(String remoteHost) {
            this.remoteHost = remoteHost;
        }

        @Override
        public void setAttribute(String name, Object o) {
            attributes.put(name, o);
        }

        public void setHeader(String name, String value) {
            headers.put(name.toLowerCase(), value);
        }

        @Override
        public void removeAttribute(String name) {
            attributes.remove(name);
        }

        @Override
        public Locale getLocale() {
            return locale;
        }

        public void setLocale(Locale locale) {
            this.locale = locale;
        }

        @Override
        public Enumeration getLocales() {
            return null;
        }

        @Override
        public boolean isSecure() {
            return secure;
        }

        public void setSecure(boolean secure) {
            this.secure = secure;
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            return requestDispatcher;
        }

        public void setRequestDispatcher(RequestDispatcher requestDispatcher) {
            this.requestDispatcher = requestDispatcher;
        }

        @Override
        public String getRealPath(String path) {
            return realPath;
        }

        public void setRealPath(String realPath) {
            this.realPath = realPath;
        }

        @Override
        public int getRemotePort() {
            return remotePort;
        }

        public void setRemotePort(int remotePort) {
            this.remotePort = remotePort;
        }

        @Override
        public String getLocalName() {
            return localName;
        }

        public void setLocalName(String localName) {
            this.localName = localName;
        }

        @Override
        public String getLocalAddr() {
            return localAddr;
        }

        public void setLocalAddr(String localAddr) {
            this.localAddr = localAddr;
        }

        @Override
        public int getLocalPort() {
            return localPort;
        }

        public void setLocalPort(int localPort) {
            this.localPort = localPort;
        }

        @Override
        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        @Override
        public Cookie[] getCookies() {
            return cookies.toArray(new Cookie[0]);
        }

        public void setCookies(List<String> cookies) {
            this.cookies = cookies;
        }

        @Override
        public long getDateHeader(String name) {
            return dateHeader;
        }

        public void setDateHeader(long dateHeader) {
            this.dateHeader = dateHeader;
        }

        @Override
        public String getHeader(String name) {
            return headers.get(name.toLowerCase());
        }

        @Override
        public Enumeration getHeaders(String name) {
            final Iterator<String> iterator = parameters.values().iterator();
            return new Enumeration<String>() {

                @Override
                public boolean hasMoreElements() {
                    return iterator.hasNext();
                }

                @Override
                public String nextElement() {
                    return iterator.next();
                }
            };
        }

        @Override
        public Enumeration getHeaderNames() {
            final Iterator<String> iterator = headers.keySet().iterator();
            return new Enumeration<String>() {

                @Override
                public boolean hasMoreElements() {
                    return iterator.hasNext();
                }

                @Override
                public String nextElement() {
                    return iterator.next();
                }
            };
        }

        @Override
        public int getIntHeader(String name) {
            final String string = headers.get(name);
            if (null == string) {
                return -1;
            }

            try {
                final int ret = Integer.parseInt(string);
                return ret < 0 ? -1 : ret;
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        @Override
        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        @Override
        public String getPathInfo() {
            return pathInfo;
        }

        public void setPathInfo(String pathInfo) {
            this.pathInfo = pathInfo;
        }

        @Override
        public String getPathTranslated() {
            return pathTranslated;
        }

        public void setPathTranslated(String pathTranslated) {
            this.pathTranslated = pathTranslated;
        }

        @Override
        public String getContextPath() {
            return contextPath;
        }

        public void setContextPath(String contextPath) {
            this.contextPath = contextPath;
        }

        @Override
        public String getQueryString() {
            return queryString;
        }

        public void setQueryString(String queryString) {
            this.queryString = queryString;
        }

        @Override
        public String getRemoteUser() {
            return remoteUser;
        }

        public void setRemoteUser(String remoteUser) {
            this.remoteUser = remoteUser;
        }

        @Override
        public boolean isUserInRole(String role) {
            return false;
        }

        @Override
        public Principal getUserPrincipal() {
            return principal;
        }

        public void setPrincipal(Principal principal) {
            this.principal = principal;
        }

        @Override
        public String getRequestedSessionId() {
            return requestedSessionId;
        }

        public void setRequestedSessionId(String requestedSessionId) {
            this.requestedSessionId = requestedSessionId;
        }

        @Override
        public String getRequestURI() {
            return requestURI;
        }

        public void setRequestURI(String requestURI) {
            this.requestURI = requestURI;
        }

        @Override
        public StringBuffer getRequestURL() {
            return new StringBuffer(requestURL);
        }

        public void setRequestURL(String requestURL) {
            this.requestURL = requestURL;
        }

        @Override
        public String getServletPath() {
            return servletPath;
        }

        public void setServletPath(String servletPath) {
            this.servletPath = servletPath;
        }

        @Override
        public HttpSession getSession(boolean create) {
            return httpSession;
        }

        public void setHttpSession(HttpSession httpSession) {
            this.httpSession = httpSession;
        }

        @Override
        public HttpSession getSession() {
            return httpSession;
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromUrl() {
            return false;
        }

    }

    private static final class SimHttpServletResponse implements HttpServletResponse {

        private final Map<String, String> headers;
        private String characterEncoding;
        private ServletOutputStream outputStream;
        private boolean committed;
        private Locale locale;
        private List<Cookie> cookies;
        private int status;
        private String statusMessage;

        public SimHttpServletResponse() {
            super();
            headers = new HashMap<String, String>(8);
        }

        @Override
        public String getCharacterEncoding() {
            return characterEncoding;
        }

        @Override
        public String getContentType() {
            return headers.get("content-type");
        }

        public int getContentLength() {
            final String string = headers.get("content-length");
            if (null == string) {
                return -1;
            }

            try {
                final int ret = (int) Long.parseLong(string);
                return ret < 0 ? -1 : ret;
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return outputStream;
        }

        public void setOutputStream(ServletOutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(new OutputStreamWriter(outputStream, characterEncoding));
        }

        @Override
        public void setCharacterEncoding(String charset) {
            characterEncoding = charset;
        }

        @Override
        public void setContentLength(int len) {
            headers.put("content-length", Integer.toString(len));
        }

        @Override
        public void setContentType(String type) {
            headers.put("content-type", type);
        }

        @Override
        public void setBufferSize(int size) {
        }

        @Override
        public int getBufferSize() {
            return 0;
        }

        @Override
        public void flushBuffer() throws IOException {
        }

        @Override
        public void resetBuffer() {
        }

        @Override
        public boolean isCommitted() {
            return committed;
        }

        public void setCommitted(boolean committed) {
            this.committed = committed;
        }

        @Override
        public void reset() {
        }

        @Override
        public void setLocale(Locale loc) {
            this.locale = loc;
        }

        @Override
        public Locale getLocale() {
            return locale;
        }

        @Override
        public void addCookie(Cookie cookie) {
            cookies.add(cookie);
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public String getHeader(String name) {
            return headers.get(name.toLowerCase());
        }

        @Override
        public boolean containsHeader(String name) {
            return headers.containsKey(name.toLowerCase());
        }

        @Override
        public String encodeURL(String url) {
            return null;
        }

        @Override
        public String encodeRedirectURL(String url) {
            return null;
        }

        @Override
        public String encodeUrl(String url) {
            return null;
        }

        @Override
        public String encodeRedirectUrl(String url) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            status = sc;
            statusMessage = msg;
        }

        @Override
        public void sendError(int sc) throws IOException {
            status = sc;
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            setStatus(HttpServletResponse.SC_FOUND);
            setHeader("location", location);
        }

        @Override
        public void setDateHeader(String name, long date) {
            headers.put(name.toLowerCase(), Long.toString(date));
        }

        @Override
        public void addDateHeader(String name, long date) {
            headers.put(name.toLowerCase(), Long.toString(date));
        }

        @Override
        public void setHeader(String name, String value) {
            headers.put(name.toLowerCase(), value);
        }

        @Override
        public void addHeader(String name, String value) {
            headers.put(name.toLowerCase(), value);
        }

        @Override
        public void setIntHeader(String name, int value) {
            headers.put(name.toLowerCase(), Integer.toString(value));
        }

        @Override
        public void addIntHeader(String name, int value) {
            headers.put(name.toLowerCase(), Integer.toString(value));
        }

        @Override
        public void setStatus(int sc) {
            this.status = sc;
        }

        public int getStatus() {
            return status;
        }

        @Override
        public void setStatus(int sc, String sm) {
            this.status = sc;
            this.statusMessage = sm;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

    }



}
