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

import static com.openexchange.filestore.utils.PropertyNameBuilder.optBoolProperty;
import static com.openexchange.filestore.utils.PropertyNameBuilder.optProperty;
import static com.openexchange.filestore.utils.PropertyNameBuilder.requireProperty;
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
import com.amazonaws.services.s3.model.StaticEncryptionMaterialsProvider;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageProvider;
import com.openexchange.filestore.utils.PropertyNameBuilder;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
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
            ConfigurationService configService = services.getOptionalService(ConfigurationService.class);
            if (null == configService) {
                throw ServiceExceptionCode.absentService(ConfigurationService.class);
            }
            PropertyNameBuilder propNameBuilder = new PropertyNameBuilder("com.openexchange.filestore.s3.");
            AmazonS3ClientInfo clientInfo = initClient(filestoreID, propNameBuilder, configService);
            AmazonS3Client client = clientInfo.client;
            String bucketName = initBucket(client, filestoreID, propNameBuilder, configService);
            LOG.debug("Using \"{}\" as bucket name.", bucketName);
            return new S3FileStorage(client, clientInfo.encrypted, bucketName, extractFilestorePrefix(uri), clientInfo.chunkSize);
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
     * @return The client
     * @throws OXException
     */
    private AmazonS3ClientInfo initClient(String filestoreID, PropertyNameBuilder propNameBuilder, ConfigurationService configService) throws OXException {
        /*
         * prepare credentials
         */
        String accessKey = requireProperty(filestoreID, "accessKey", propNameBuilder, configService);
        String secretKey = requireProperty(filestoreID, "secretKey", propNameBuilder, configService);
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        /*
         * instantiate client
         */
        ClientConfiguration clientConfiguration = getClientConfiguration(filestoreID, propNameBuilder, configService);
        AmazonS3Builder<?, ?> clientBuilder;
        boolean encrypted;
        {
            String encryption = optProperty(filestoreID, "encryption", "none", propNameBuilder, configService);
            if (Strings.isEmpty(encryption) || "none".equals(encryption)) {
                AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withClientConfiguration(clientConfiguration);
                clientBuilder = builder;
                encrypted = false;
            } else {
                AmazonS3EncryptionClientBuilder builder = AmazonS3EncryptionClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withClientConfiguration(clientConfiguration)
                    .withEncryptionMaterials(new StaticEncryptionMaterialsProvider(getEncryptionMaterials(filestoreID, encryption, propNameBuilder, configService)))
                    .withCryptoConfiguration(new CryptoConfiguration());
                clientBuilder = builder;
                encrypted = true;
            }
        }
        /*
         * configure client
         */
        String endpoint = optProperty(filestoreID, "endpoint", null, propNameBuilder, configService);
        if (false == Strings.isEmpty(endpoint)) {
            clientBuilder.setEndpointConfiguration(new EndpointConfiguration(endpoint, null));
        } else {
            String region = optProperty(filestoreID, "region", "us-west-2", propNameBuilder, configService);
            try {
                clientBuilder.withRegion(Regions.fromName(region));
            } catch (IllegalArgumentException e) {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, region);
            }
        }
        if (optBoolProperty(filestoreID, "pathStyleAccess", true, propNameBuilder, configService)) {
            clientBuilder.setPathStyleAccessEnabled(Boolean.TRUE);
        }
        clientBuilder.withRequestHandlers(ETagCorrectionHandler.getInstance());
        long chunkSize;
        String chunkSizeValue = optProperty(filestoreID, "chunkSize", "5MB", propNameBuilder, configService);
        try {
            chunkSize = ConfigTools.parseBytes(chunkSizeValue);
        } catch (NumberFormatException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, chunkSizeValue);
        }
        return new AmazonS3ClientInfo((AmazonS3Client) clientBuilder.build(), encrypted, chunkSize);
    }

    private ClientConfiguration getClientConfiguration(String filestoreID, PropertyNameBuilder propNameBuilder, ConfigurationService configService) {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        String signerOverride = optProperty(filestoreID, "signerOverride", "S3SignerType", propNameBuilder, configService);
        if (false == Strings.isEmpty(signerOverride)) {
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

    private EncryptionMaterials getEncryptionMaterials(String filestoreID, String encryptionMode, PropertyNameBuilder propNameBuilder, ConfigurationService configService) throws OXException {
        if (!"rsa".equalsIgnoreCase(encryptionMode)) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Unknown encryption mode: " + encryptionMode);
        }

        String keyStore = requireProperty(filestoreID, "encryption.rsa.keyStore", propNameBuilder, configService);
        String password = requireProperty(filestoreID, "encryption.rsa.password", propNameBuilder, configService);
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
     * @return The bucket name
     * @throws OXException If initialization fails
     */
    private String initBucket(AmazonS3Client s3client, String filestoreID, PropertyNameBuilder propNameBuilder, ConfigurationService configService) throws OXException {
        String bucketName = requireProperty(filestoreID, "bucketName", propNameBuilder, configService);

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
            String region = optProperty(filestoreID, "region", "us-west-2", propNameBuilder, configService);
            try {
                s3client.createBucket(new CreateBucketRequest(bucketName, Region.fromValue(region)));
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
     * Extracts the filestore prefix from the configured file store URI, i.e. the 'path' part of the URI.
     *
     * @param uri The file store URI
     * @return The prefix to use
     * @throws IllegalArgumentException If the specified bucket name doesn't follow Amazon S3's guidelines
     */
    private static String extractFilestorePrefix(URI uri) throws IllegalArgumentException {
        String path = uri.getPath();
        while (0 < path.length() && '/' == path.charAt(0)) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        /*
         * Remove underscore characters to be conform to bucket name & prefix restrictions
         * http://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html /
         * http://docs.aws.amazon.com/AmazonS3/latest/dev/ListingKeysHierarchy.html
         */
        if (path.endsWith("ctx_store")) {
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

        // Any path that serves as prefix; e.g. "photos"
        return sanitizePathForPrefix(path, uri);
    }

    private static String sanitizePathForPrefix(String path, URI uri) {
        if (Strings.isEmpty(path)) {
            throw new IllegalArgumentException("Path is empty in URI: " + uri);
        }

        StringBuilder sb = null;
        for (int k = path.length(), i = 0; k-- > 0; i++) {
            char ch = path.charAt(i);
            if ('_' == ch) {
                // Underscore not allowed
                if (null == sb) {
                    sb = new StringBuilder(path.length());
                    sb.append(path, 0, i);
                }
            } else {
                // Append
                if (null != sb) {
                    sb.append(ch);
                }
            }
        }
        return null == sb ? path : sb.toString();
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

}
