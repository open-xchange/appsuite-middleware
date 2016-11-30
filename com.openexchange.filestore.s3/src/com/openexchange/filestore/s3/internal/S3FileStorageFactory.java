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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.Region;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageProvider;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

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

    private final ConcurrentMap<URI, S3FileStorage> storages;
    private final ConfigurationService configService;

    /**
     * Initializes a new {@link S3FileStorageFactory}.
     *
     * @param configService The configuration service to use
     */
    public S3FileStorageFactory(ConfigurationService configService) {
        super();
        this.configService = configService;
        this.storages = new ConcurrentHashMap<URI, S3FileStorage>();
    }

    @Override
    public S3FileStorage getFileStorage(URI uri) throws OXException {
        try{
        S3FileStorage storage = storages.get(uri);
        if (null == storage) {
            LOG.debug("Initializing S3 client for {}", uri);
            /*
             * extract filestore ID from authority part of URI
             */
            String filestoreID = extractFilestoreID(uri);
            LOG.debug("Using \"{}\" as filestore ID.", filestoreID);
            /*
             * create client
             */
            AmazonS3ClientInfo clientInfo = initClient(filestoreID);
            AmazonS3Client client = clientInfo.client;
            String bucketName = initBucket(client, filestoreID);
            LOG.debug("Using \"{}\" as bucket name.", bucketName);
            S3FileStorage newStorage = new S3FileStorage(client, clientInfo.encrypted, bucketName, extractFilestorePrefix(uri), clientInfo.chunkSize);
            storage = storages.putIfAbsent(uri, newStorage);
            if (null == storage) {
                storage = newStorage;
            }
        }
            return storage;
        }catch(OXException ex){
            if(ex.getCause() instanceof AmazonS3Exception && ((AmazonS3Exception)ex.getCause()).getStatusCode() == 400){
                // throw a simple OXException here to be able to forward this exception to rmi clients (Bug #42102)
                throw new OXException(S3ExceptionCode.BadRequest.getNumber(), S3ExceptionMessages.BadRequest_MSG);
            }else{
                throw ex;
            }
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
    private AmazonS3ClientInfo initClient(String filestoreID) throws OXException {
        /*
         * prepare credentials
         */
        String accessKey = requireProperty("com.openexchange.filestore.s3." + filestoreID + ".accessKey");
        String secretKey = requireProperty("com.openexchange.filestore.s3." + filestoreID + ".secretKey");
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        /*
         * instantiate client
         */
        ClientConfiguration clientConfiguration = getClientConfiguration(filestoreID);
        AmazonS3Client client = null;
        String encryption = configService.getProperty("com.openexchange.filestore.s3." + filestoreID + ".encryption", "none");
        boolean encrypted;
        if (Strings.isEmpty(encryption) || "none".equals(encryption)) {
            /*
             * use default AmazonS3Client
             */
            client = new AmazonS3Client(credentials, clientConfiguration);
            encrypted = false;
        } else {
            /*
             * use AmazonS3EncryptionClient
             */
            client = new AmazonS3EncryptionClient(credentials, getEncryptionMaterials(filestoreID, encryption), clientConfiguration, new CryptoConfiguration());
            encrypted = true;
        }
        /*
         * configure client
         */
        String endpoint = configService.getProperty("com.openexchange.filestore.s3." + filestoreID + ".endpoint");
        if (false == Strings.isEmpty(endpoint)) {
            client.setEndpoint(endpoint);
        } else {
            String region = configService.getProperty("com.openexchange.filestore.s3." + filestoreID + ".region", "us-west-2");
            try {
                client.setRegion(com.amazonaws.regions.Region.getRegion(Regions.fromName(region)));
            } catch (IllegalArgumentException e) {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, region);
            }
        }
        if (configService.getBoolProperty("com.openexchange.filestore.s3." + filestoreID + ".pathStyleAccess", true)) {
            client.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));
        }
        long chunkSize;
        String chunkSizeValue = configService.getProperty("com.openexchange.filestore.s3." + filestoreID + ".chunkSize", "5MB");
        try {
            chunkSize = ConfigTools.parseBytes(chunkSizeValue);
        } catch (NumberFormatException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, chunkSizeValue);
        }
        return new AmazonS3ClientInfo(client, encrypted, chunkSize);
    }

    private ClientConfiguration getClientConfiguration(String filestoreID) {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        String signerOverride = configService.getProperty("com.openexchange.filestore.s3." + filestoreID + ".signerOverride", "S3SignerType");
        if (false == Strings.isEmpty(signerOverride)) {
            clientConfiguration.setSignerOverride(signerOverride);
        }
        return clientConfiguration;
    }

    private EncryptionMaterials getEncryptionMaterials(String filestoreID, String encryptionMode) throws OXException {
        if (!"rsa".equalsIgnoreCase(encryptionMode)) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Unknown encryption mode: " + encryptionMode);
        }

        String keyStore = requireProperty("com.openexchange.filestore.s3." + filestoreID + ".encryption.rsa.keyStore");
        String password = requireProperty("com.openexchange.filestore.s3." + filestoreID + ".encryption.rsa.password");
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
    private String initBucket(AmazonS3Client s3client, String filestoreID) throws OXException {
        String bucketName = requireProperty("com.openexchange.filestore.s3." + filestoreID + ".bucketName");
        try {
            if (false == s3client.doesBucketExist(bucketName)) {
                String region = configService.getProperty("com.openexchange.filestore.s3." + filestoreID + ".region", "us-west-2");
                s3client.createBucket(bucketName, Region.fromValue(region));
            }
            return bucketName;
        } catch (IllegalArgumentException e) {
            throw S3ExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (AmazonClientException e) {
            throw S3ExceptionCode.wrap(e);
        } catch (RuntimeException e) {
            throw S3ExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private String requireProperty(String propertyName) throws OXException {
        String property = configService.getProperty(propertyName);
        if (Strings.isEmpty(property)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(propertyName);
        }
        return property;
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
