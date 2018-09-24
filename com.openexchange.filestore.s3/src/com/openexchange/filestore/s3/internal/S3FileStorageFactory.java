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

package com.openexchange.filestore.s3.internal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Builder;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.SetBucketPolicyRequest;
import com.amazonaws.services.s3.model.StaticEncryptionMaterialsProvider;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.auth.policy.conditions.BooleanCondition;
import com.amazonaws.auth.policy.conditions.StringCondition;
import com.amazonaws.auth.policy.conditions.StringCondition.StringComparisonType;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageProvider;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link S3FileStorageFactory}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class S3FileStorageFactory implements FileStorageProvider {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(S3FileStorageFactory.class);

    /**
     * The URI scheme identifying S3 file storages.
     */
    private static final String S3_SCHEME = "s3";

    /**
     * The expected pattern for file store names associated with a context - defined by
     * com.openexchange.filestore.FileStorages.getNameForContext(int) ,
     * so expect nothing else; e.g. <code>"57462_ctx_store"</code>
     */
    private static final Pattern CTX_STORE_PATTERN = Pattern.compile("(\\d+)_ctx_store");

    /**
     * The expected pattern for file store names associated with a user - defined by
     * com.openexchange.filestore.FileStorages.getNameForUser(int, int) ,
     * so expect nothing else; e.g. <code>"57462_ctx_5_user_store"</code>
     */
    private static final Pattern USER_STORE_PATTERN = Pattern.compile("(\\d+)_ctx_(\\d+)_user_store");

    /**
     * Expected pattern for legacy Guard files
     */
    private static final Pattern GUARD_STORAGE_PATTERN = Pattern.compile("ext_[\\d]+_[\\d]+");

    /**
     * The file storage's ranking compared to other sharing the same URL scheme.
     */
    private static final int RANKING = 5634;

    private final ServiceLookup services;

    /**
     * Initializes a new {@link S3FileStorageFactory}.
     *
     * @param configService The configuration service to use
     */
    public S3FileStorageFactory(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public S3FileStorage getFileStorage(URI uri) throws OXException {
        try {
                LOG.debug("Initializing S3 client for {}", uri);
                /*
                 * extract filestore ID from authority part of URI
                 */
                String filestoreID = extractFilestoreID(uri);
                LOG.debug("Using \"{}\" as filestore ID.", filestoreID);
                /*
                 * create client
                 */
                LeanConfigurationService configService = services.getServiceSafe(LeanConfigurationService.class);
                Map<String, String> optional = getOptional(filestoreID);
                String encryption = getPropertySafe(S3Properties.ENCRYPTION, configService, optional);
                S3EncryptionConfig s3EncryptionConfig = new S3EncryptionConfig(encryption);
                AmazonS3ClientInfo clientInfo = initClient(filestoreID, configService, s3EncryptionConfig);
                AmazonS3Client client = clientInfo.client;
                String bucketName = initBucket(client, filestoreID, configService, s3EncryptionConfig);
                LOG.debug("Using \"{}\" as bucket name.", bucketName);
                return new S3FileStorage(client, clientInfo.encrypted, s3EncryptionConfig.getServerSideEncryption() != null, bucketName, extractFilestorePrefix(uri), clientInfo.chunkSize);
        } catch (OXException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof AmazonS3Exception) {
                AmazonS3Exception s3Exception = (AmazonS3Exception) cause;
                if (s3Exception.getStatusCode() == 400) {
                    // throw a simple OXException here to be able forwarding this exception to RMI clients (Bug #42102)
                    throw new OXException(S3ExceptionCode.BadRequest.getNumber(), S3ExceptionMessages.BadRequest_MSG, s3Exception);
                }
            }

            throw ex;
        }
    }

    @Override
    public FileStorage getInternalFileStorage(URI uri) throws OXException {
        return getFileStorage(uri);
    }

    @Override
    public boolean supports(URI uri) {
         return null != uri && S3_SCHEME.equalsIgnoreCase(uri.getScheme());
    }

    @Override
    public int getRanking() {
        return RANKING;
    }

    /**
     * Initializes an {@link AmazonS3Client} as configured by the referenced authority part of the supplied URI.
     *
     * @param filestoreID The filestore ID
     * @param configService The {@link LeanConfigurationService}
     * @param encryptionConfig The {@link S3EncryptionConfig} of the given filestore
     * @return The client
     * @throws OXException
     */
    private AmazonS3ClientInfo initClient(String filestoreID, LeanConfigurationService configService, S3EncryptionConfig encryptionConfig) throws OXException {
        /*
         * prepare credentials
         */
        Map<String, String> optional = getOptional(filestoreID);
        String accessKey = getPropertySafe(S3Properties.ACCESS_KEY, configService, optional);
        String secretKey = getPropertySafe(S3Properties.SECRET_KEY, configService, optional);

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        /*
         * instantiate client
         */
        ClientConfiguration clientConfiguration = getClientConfiguration(filestoreID, configService);
        AmazonS3Builder<?, ?> clientBuilder;
        boolean encrypted;
        {
            if (encryptionConfig.getClientEncryption() == null || encryptionConfig.getClientEncryption().equals(EncryptionType.NONE)) {
                AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withClientConfiguration(clientConfiguration);
                clientBuilder = builder;
                encrypted = false;
            } else {
                AmazonS3EncryptionClientBuilder builder = AmazonS3EncryptionClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withClientConfiguration(clientConfiguration)
                    .withEncryptionMaterials(new StaticEncryptionMaterialsProvider(getEncryptionMaterials(filestoreID, encryptionConfig.getClientEncryption(), configService)))
                    .withCryptoConfiguration(new CryptoConfiguration());
                clientBuilder = builder;
                encrypted = true;
            }
        }
        /*
         * configure client
         */
        String endpoint = configService.getProperty(S3Properties.ENDPOINT, optional);

        if (Strings.isNotEmpty(endpoint)) {
            clientBuilder.setEndpointConfiguration(new EndpointConfiguration(endpoint, null));
        } else {
            String region = configService.getProperty(S3Properties.REGION, optional);
            try {
                clientBuilder.withRegion(Regions.fromName(region));
            } catch (IllegalArgumentException e) {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, region);
            }
        }
        if (configService.getBooleanProperty(S3Properties.PATH_STYLE_ACCESS, optional)) {
            clientBuilder.setPathStyleAccessEnabled(Boolean.TRUE);
        }
        clientBuilder.withRequestHandlers(ETagCorrectionHandler.getInstance());
        long chunkSize;
        String chunkSizeValue = configService.getProperty(S3Properties.CHUNK_SIZE, optional);
        try {
            chunkSize = ConfigTools.parseBytes(chunkSizeValue);
        } catch (NumberFormatException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, chunkSizeValue);
        }
        return new AmazonS3ClientInfo((AmazonS3Client) clientBuilder.build(), encrypted, chunkSize);
    }

    private String getPropertySafe(S3Properties prop, LeanConfigurationService configurationService, Map<String, String> optionals) throws OXException {
        String property = configurationService.getProperty(prop, optionals);
        if (Strings.isEmpty(property)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(prop.getFQPropertyName(optionals));
        }
        return property;
    }

    private Map<String, String> getOptional(String filestoreId) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(S3Properties.OPTIONAL_NAME, filestoreId);
        return hashMap;
    }

    private ClientConfiguration getClientConfiguration(String filestoreID, LeanConfigurationService configService) {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        String signerOverride = configService.getProperty(S3Properties.SIGNER_OVERRIDE, getOptional(filestoreID));

        if (Strings.isNotEmpty(signerOverride)) {
            clientConfiguration.setSignerOverride(signerOverride);
        }

        String proxyHost = System.getProperty("http.proxyHost");
        if (proxyHost != null) {
            clientConfiguration.setProxyHost(proxyHost);
            String proxyPort = System.getProperty("http.proxyPort");
            if (proxyPort != null) {
                clientConfiguration.setProxyPort(Integer.parseInt(proxyPort));
            }

            String nonProxyHosts = System.getProperty("http.nonProxyHosts");
            if (Strings.isNotEmpty(nonProxyHosts)) {
                clientConfiguration.setNonProxyHosts(nonProxyHosts);
            }

            String login = System.getProperty("http.proxyUser");
            String password = System.getProperty("http.proxyPassword");

            if (login != null && password != null) {
                clientConfiguration.setProxyUsername(login);
                clientConfiguration.setProxyPassword(password);
            }
        }
        return clientConfiguration;
    }

    private EncryptionMaterials getEncryptionMaterials(String filestoreID, EncryptionType clientType, LeanConfigurationService configurationService) throws OXException {
        if (!EncryptionType.RSA.equals(clientType)) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Unsupported encryption type: " + clientType.getName());
        }
        Map<String, String> optional = getOptional(filestoreID);
        String keyStore = getPropertySafe(S3Properties.RSA_KEYSTORE, configurationService, optional);
        String password = getPropertySafe(S3Properties.RSA_PASSWORD, configurationService, optional);
        KeyPair keyPair = extractKeys(keyStore, password);
        return new EncryptionMaterials(keyPair);
    }

    /**
     * Extracts the private/public key pair from a PKCS #12 keystore file referenced by the supplied path.
     *
     * @param pathToKeyStore The path to the keystore file
     * @param password The password to access the keystore
     * @return The key pair
     * @throws OXException
     */
    private static KeyPair extractKeys(String pathToKeyStore, String password) throws OXException {
        PrivateKey privateKey = null;
        PublicKey publicKey = null;
        char[] passwordChars = null == password ? null : password.toCharArray();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(pathToKeyStore);
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(inputStream, passwordChars);
            for (Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements();) {
                String alias = aliases.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    Key key = keyStore.getKey(alias, passwordChars);
                    if (null != key && PrivateKey.class.isInstance(key)) {
                        privateKey = (PrivateKey) keyStore.getKey(alias, passwordChars);
                        Certificate certificate = keyStore.getCertificate(alias);
                        publicKey = certificate.getPublicKey();
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "Error reading " + pathToKeyStore);
        } catch (KeyStoreException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "Error reading " + pathToKeyStore);
        } catch (NoSuchAlgorithmException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "Error reading " + pathToKeyStore);
        } catch (CertificateException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "Error reading " + pathToKeyStore);
        } catch (IOException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "Error reading " + pathToKeyStore);
        } catch (UnrecoverableKeyException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "Error reading " + pathToKeyStore);
        } finally {
            Streams.close(inputStream);
        }
        if (null == privateKey) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("No private key found in " + pathToKeyStore);
        }
        if (null == publicKey) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("No public key found in " + pathToKeyStore);
        }
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Initializes the bucket denoted by the supplied URI, creating the bucket dynamically if needed.
     *
     * @param s3client The S3 client
     * @param filestoreID The filestore ID
     * @param configService The {@link LeanConfigurationService}
     * @param encryptionConfig The {@link S3EncryptionConfig} for the given filestore
     * @return The bucket name
     * @throws OXException If initialization fails
     */
    private String initBucket(AmazonS3Client s3client, String filestoreID, LeanConfigurationService configService, S3EncryptionConfig encryptionConfig) throws OXException {
        Map<String, String> optional = getOptional(filestoreID);
        String bucketName = getPropertySafe(S3Properties.BUCKET_NAME, configService, optional);

        boolean bucketExists = false;
        try {
            bucketExists = s3client.doesBucketExist(bucketName);
        } catch (IllegalArgumentException e) {
            throw S3ExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (AmazonClientException e) {
            throw S3ExceptionCode.wrap(e);
        } catch (RuntimeException e) {
            throw S3ExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }

        if (false == bucketExists) {
            String region = configService.getProperty(S3Properties.REGION, getOptional(filestoreID));

            try {
                s3client.createBucket(new CreateBucketRequest(bucketName, Region.fromValue(region)));

                if (encryptionConfig.getServerSideEncryption() != null) {
                    s3client.setBucketPolicy(new SetBucketPolicyRequest(bucketName, getSSEOnlyBucketPolicy(bucketName)));
                }
            } catch (IllegalArgumentException e) {
                throw S3ExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (AmazonS3Exception e) {
                if ("InvalidLocationConstraint".equals(e.getErrorCode())) {
                    // Failed to create such a bucket
                    throw S3ExceptionCode.BUCKET_CREATION_FAILED.create(bucketName, region);
                }
                throw S3ExceptionCode.wrap(e);
            } catch (AmazonServiceException e) {
                throw S3ExceptionCode.wrap(e);
            } catch (AmazonClientException e) {
                throw S3ExceptionCode.wrap(e);
            } catch (RuntimeException e) {
                throw S3ExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }

        return bucketName;
    }

    /**
     * Gets the bucket policy for a server side encryption only bucket.
     *
     * @param bucket_name The name of the bucket
     * @return The encryption only policy
     */
    private String getSSEOnlyBucketPolicy(String bucket_name) {
        Policy bucket_policy = new Policy().withStatements(
            new Statement(Statement.Effect.Deny)
                .withId("DenyIncorrectEncryptionHeader")
                .withPrincipals(Principal.AllUsers)
                .withActions(S3Actions.PutObject)
                .withResources(new Resource("arn:aws:s3:::" + bucket_name + "/*"))
                .withConditions(new StringCondition(StringComparisonType.StringNotEquals, "s3:x-amz-server-side-encryption", "AES256")),
            new Statement(Statement.Effect.Deny)
                .withId("DenyUnEncryptedObjectUploads")
                .withPrincipals(Principal.AllUsers)
                .withActions(S3Actions.PutObject)
                .withResources(new Resource("arn:aws:s3:::" + bucket_name + "/*"))
                .withConditions(new BooleanCondition("s3:x-amz-server-side-encryption", true))
                );
        return bucket_policy.toJson();
    }

    /**
     * Extracts the filestore prefix from the configured file store URI, i.e. the 'path' part of the URI.
     *
     * @param uri The file store URI
     * @return The prefix to use
     * @throws IllegalArgumentException If the specified bucket name doesn't follow Amazon S3's guidelines
     */
    private static String extractFilestorePrefix(URI uri) throws IllegalArgumentException {
        // Extract & prepare path to be used as prefix
        String path = uri.getPath();
        while (0 < path.length() && '/' == path.charAt(0)) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.endsWith("ctx_store")) {
            // Expect context store identifier
            Matcher matcher = CTX_STORE_PATTERN.matcher(path);
            if (false == matcher.matches()) {
                throw new IllegalArgumentException("Path does not match the expected pattern \"\\d+_ctx_store\" in URI: " + uri);
            }
            return new StringBuilder(16).append(matcher.group(1)).append("ctxstore").toString();
        }

        if (path.endsWith("user_store")) {
            // Expect user store identifier
            Matcher matcher = USER_STORE_PATTERN.matcher(path);
            if (false == matcher.matches()) {
                throw new IllegalArgumentException("Path does not match the expected pattern \"(\\d+)_ctx_(\\d+)_user_store\" in URI: " + uri);
            }
            return new StringBuilder(24).append(matcher.group(1)).append("ctx").append(matcher.group(2)).append("userstore").toString();
        }

        if (path.startsWith("ext_")) {
            Matcher matcher = GUARD_STORAGE_PATTERN.matcher(path);
            // Legacy Guard S3 storage.  Don't remove underscore for these files
            if (matcher.matches()) {
                return path;
            }
        }
        // Any path that serves as prefix; e.g. "photos"
        return sanitizePathForPrefix(path, uri);
    }

    /**
     * Strips all characters from specified prefix path, which are no allows according to
     * <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/UsingMetadata.html">this article</a>
     *
     * @param path The path to sanitize
     * @param uri The file store URI containing the path
     * @return The sanitized path ready to be used as prefix
     */
    private static String sanitizePathForPrefix(String path, URI uri) {
        if (Strings.isEmpty(path)) {
            throw new IllegalArgumentException("Path is empty in URI: " + uri);
        }

        StringBuilder sb = null;
        for (int k = path.length(), i = 0; k-- > 0; i++) {
            char ch = path.charAt(i);
            if (Strings.isAsciiLetterOrDigit(ch) || isAllowedSpecial(ch)) {
                // Append
                if (null != sb) {
                    sb.append(ch);
                }
            } else {
                // Not allowed in prefix
                if (null == sb) {
                    sb = new StringBuilder(path.length());
                    if (i > 0) {
                        sb.append(path, 0, i);
                    }
                }
            }
        }
        return null == sb ? path : sb.toString();
    }

    private static boolean isAllowedSpecial(char ch) {
        switch (ch) {
            case '!':
            case '-':
            case '_':
            case '.':
            case '*':
            case '\'':
            case '(':
            case ')':
                return true;
            default:
                return false;
        }
    }

    /**
     * Extracts the filestore ID from the configured file store URI, i.e. the 'authority' part from the URI.
     *
     * @param uri The file store URI
     * @return The filestore ID
     * @throws IllegalArgumentException If no valid ID could be extracted
     */
    private static String extractFilestoreID(URI uri) throws IllegalArgumentException {
        String authority = uri.getAuthority();
        if (null == authority) {
            throw new IllegalArgumentException("No 'authority' part specified in filestore URI");
        }
        while (0 < authority.length() && '/' == authority.charAt(0)) {
            authority = authority.substring(1);
        }
        if (0 == authority.length()) {
            throw new IllegalArgumentException("No 'authority' part specified in filestore URI");
        }
        return authority;
    }

    private static class AmazonS3ClientInfo {

        /** The Amazon S3 client reference */
        final AmazonS3Client client;

        /** Whether associated Amazon S3 client reference has encryption enabled */
        final boolean encrypted;

        /** The chunk size to use for multipart uploads */
        final long chunkSize;

        AmazonS3ClientInfo(AmazonS3Client client, boolean encrypted, long chunkSize) {
            super();
            this.client = client;
            this.encrypted = encrypted;
            this.chunkSize = chunkSize;
        }
    }

    /**
     *
     * {@link S3EncryptionConfig} holds encryption types for client- and server-side encryption for a single filestore.
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.1
     */
    private static class S3EncryptionConfig {

        private final EncryptionType clientEncryption;
        private final EncryptionType serverSideEncryption;

        /**
         * Initializes a new {@link S3FileStorageFactory.S3EncryptionConfig}.
         *
         * @throws OXException In case the configuration is invalid
         */
        S3EncryptionConfig(String config) throws OXException {
            if (Strings.isEmpty(config)) {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("An empty encryption type is invalid");
            }
            int index = config.indexOf('+');
            if (index > 0) {
                // E.g. rsa+sse-s3
                String[] encryptionTypes = Strings.splitBy(config, '+', true);
                if (encryptionTypes.length != 2) {
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("It's only allowed to combine one client side encryption type and one server side encryption type.");
                }

                EncryptionType typeA = EncryptionType.getTypeByName(encryptionTypes[0]);
                if (typeA == null) {
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Unkown encryption type: " + encryptionTypes[0]);
                }

                EncryptionType typeB = EncryptionType.getTypeByName(encryptionTypes[1]);
                if (typeB == null) {
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Unkown encryption type: " + encryptionTypes[1]);
                }

                if (typeA.isClientSideEncryption() == typeB.isClientSideEncryption()) {
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("It's only allowed to combine one client side encryption type and one server side encryption type.");
                }

                if (typeA.isClientSideEncryption()) {
                    clientEncryption = typeA;
                    serverSideEncryption = typeB;
                } else {
                    clientEncryption = typeB;
                    serverSideEncryption = typeA;
                }
            } else {
                EncryptionType type = EncryptionType.getTypeByName(config);
                if (type == null) {
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Unkown encryption type: " + config);
                }
                if (type.isClientSideEncryption()) {
                    clientEncryption = type;
                    serverSideEncryption = null;
                } else {
                    clientEncryption = null;
                    serverSideEncryption = type;
                }
            }
        }

        /**
         * Gets the client encryption
         *
         * @return The client encryption
         */
        public EncryptionType getClientEncryption() {
            return clientEncryption;
        }

        /**
         * Gets the server-side encryption
         *
         * @return The server-side encryption
         */
        public EncryptionType getServerSideEncryption() {
            return serverSideEncryption;
        }

    }

}
