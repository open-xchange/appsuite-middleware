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

package com.openexchange.filestore.s3.internal.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import java.util.Arrays;
import java.util.Enumeration;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.ApacheHttpClientConfig;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Builder;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.StaticEncryptionMaterialsProvider;
import com.openexchange.config.ConfigTools;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.s3.internal.config.EncryptionType;
import com.openexchange.filestore.s3.internal.config.S3ClientConfig;
import com.openexchange.filestore.s3.internal.config.S3ClientConfig.ConfigProperty;
import com.openexchange.filestore.s3.internal.config.S3ClientProperty;
import com.openexchange.filestore.s3.internal.config.S3CredentialsSource;
import com.openexchange.filestore.s3.internal.config.S3EncryptionConfig;
import com.openexchange.filestore.s3.metrics.PerClientMetricCollector;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.systemproperties.SystemPropertiesUtils;

/**
 * Creates new S3 client instances.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class S3ClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(S3ClientFactory.class);

    /**
     * Initializes a new AWS S3 SDK client
     *
     * @param clientConfig The {@link S3ClientConfig}
     * @return The {@link AmazonS3Client}
     * @throws OXException
     */
    public S3FileStorageClient initS3Client(S3ClientConfig clientConfig) throws OXException {
        /*
         * prepare credentials provider
         */
        AWSCredentialsProvider credentialsProvider;
        {
            String property = clientConfig.getValue(S3ClientProperty.CREDENTIALS_SOURCE);
            if (Strings.isEmpty(property)) {
                property = S3CredentialsSource.STATIC.getIdentifier();
            }
            S3CredentialsSource credentialsSource = S3CredentialsSource.credentialsSourceFor(property);
            if (credentialsSource == null) {
                LOG.warn("Invalid value specified for \"{}\" property: {}. Assuming \"{}\" instead. Known values are: {}",
                    clientConfig.getExpectedFQN(S3ClientProperty.CREDENTIALS_SOURCE), property, S3CredentialsSource.STATIC, Arrays.toString(S3CredentialsSource.values()));
                credentialsSource = S3CredentialsSource.STATIC;
            }
            if (S3CredentialsSource.IAM == credentialsSource) {
                credentialsProvider = InstanceProfileCredentialsProvider.getInstance();
            } else {
                String accessKey = clientConfig.getValueSafe(S3ClientProperty.ACCESS_KEY);
                String secretKey = clientConfig.getValueSafe(S3ClientProperty.SECRET_KEY);
                AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
                credentialsProvider = new AWSStaticCredentialsProvider(credentials);
            }
        }
        /*
         * instantiate client
         */
        ClientConfiguration clientConfiguration = getClientConfiguration(clientConfig);
        AmazonS3Builder<?, ?> clientBuilder;
        String encryption = clientConfig.getValueSafe(S3ClientProperty.ENCRYPTION);
        S3EncryptionConfig encryptionConfig = new S3EncryptionConfig(encryption);
        {
            if (encryptionConfig.getClientEncryption() == null || encryptionConfig.getClientEncryption().equals(EncryptionType.NONE)) {
                AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .withClientConfiguration(clientConfiguration);
                clientBuilder = builder;
            } else {
                AmazonS3EncryptionClientBuilder builder = AmazonS3EncryptionClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .withClientConfiguration(clientConfiguration)
                    .withEncryptionMaterials(new StaticEncryptionMaterialsProvider(getEncryptionMaterials(clientConfig, encryptionConfig.getClientEncryption())))
                    .withCryptoConfiguration(new CryptoConfiguration());
                clientBuilder = builder;
            }
        }
        /*
         * configure client
         */
        String endpoint = clientConfig.getValue(S3ClientProperty.ENDPOINT);
        if (Strings.isNotEmpty(endpoint)) {
            clientBuilder.setEndpointConfiguration(new EndpointConfiguration(endpoint, null));
        } else {
            ConfigProperty region = clientConfig.getPropertySafe(S3ClientProperty.REGION);
            try {
                clientBuilder.withRegion(Regions.fromName(region.getValue()));
            } catch (IllegalArgumentException e) {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, region);
            }
        }
        String pathStyleAccess = clientConfig.getValue(S3ClientProperty.PATH_STYLE_ACCESS);
        clientBuilder.setPathStyleAccessEnabled(Boolean.valueOf(pathStyleAccess));
        clientBuilder.withRequestHandlers(ETagCorrectionHandler.getInstance());

        if (isMetricsEnabled(clientConfig)) {
            clientBuilder.setMetricsCollector(new PerClientMetricCollector(clientConfig));
        }

        long chunkSize;
        ConfigProperty chunkSizeProperty = clientConfig.getPropertySafe(S3ClientProperty.CHUNK_SIZE);
        try {
            chunkSize = ConfigTools.parseBytes(chunkSizeProperty.getValue());
        } catch (NumberFormatException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, chunkSizeProperty);
        }
        String key = clientConfig.getClientID().orElse(clientConfig.getFilestoreID());
        return new S3FileStorageClient(key, (AmazonS3Client) clientBuilder.build(), encryptionConfig, chunkSize, clientConfig.getClientScope(), clientConfig.getFingerprint());
    }

    /**
     * Whether per-client monitoring metrics shall be gathered.
     *
     * @param clientConfig The client config
     * @return <code>true</code> if metrics shall be collected
     */
    private boolean isMetricsEnabled(S3ClientConfig clientConfig) {
        return clientConfig.getClientScope().isShared()
               && clientConfig.enableMetricCollection()
               && clientConfig.getNumberOfConfiguredClients() <= clientConfig.getMaxNumberOfMonitoredClients();
    }

    /**
     * Creates a new {@link EncryptionMaterials} for the given filestore id and also checks if the client encryption type is applicable.
     *
     * @param clientConfig The {@link S3ClientConfig}
     * @param clientEncryptionType The {@link EncryptionType} of the client
     * @return The {@link EncryptionMaterials}
     * @throws OXException in case of errors or if the client encryption type is not applicable
     */
    private EncryptionMaterials getEncryptionMaterials(S3ClientConfig clientConfig, EncryptionType clientEncryptionType) throws OXException {
        if (!EncryptionType.RSA.equals(clientEncryptionType)) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Unsupported encryption type: " + clientEncryptionType.getName());
        }
        String keyStore = clientConfig.getValueSafe(S3ClientProperty.RSA_KEYSTORE);
        String password = clientConfig.getValueSafe(S3ClientProperty.RSA_PASSWORD);
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
     * Gets the S3 SDK client configuration
     *
     * @param clientConfig The client config
     * @return An according {@link ClientConfiguration} instance
     * @throws OXException If required services are absent
     */
    private ClientConfiguration getClientConfiguration(S3ClientConfig clientConfig) throws OXException {
        SSLSocketFactoryProvider factoryProvider = clientConfig.getServices().getServiceSafe(SSLSocketFactoryProvider.class);
        SSLConfigurationService sslConfig = clientConfig.getServices().getServiceSafe(SSLConfigurationService.class);

        ClientConfiguration clientConfiguration = new ClientConfiguration();

        {
            ApacheHttpClientConfig apacheHttpClientConfig = clientConfiguration.getApacheHttpClientConfig();
            apacheHttpClientConfig.setSslSocketFactory(new SSLConnectionSocketFactory(factoryProvider.getDefault(), sslConfig.getSupportedProtocols(), sslConfig.getSupportedCipherSuites(), NoopHostnameVerifier.INSTANCE));
        }

        {
            String signerOverride = clientConfig.getValue(S3ClientProperty.SIGNER_OVERRIDE);
            if (Strings.isNotEmpty(signerOverride)) {
                clientConfiguration.setSignerOverride(signerOverride);
            }
        }

        {
            ConfigProperty connectTimeout = clientConfig.getProperty(S3ClientProperty.CONNECT_TIMEOUT);
            if (Strings.isNotEmpty(connectTimeout.getValue())) {
                try {
                    clientConfiguration.setConnectionTimeout(Integer.parseInt(connectTimeout.getValue().trim()));
                } catch (NumberFormatException e) {
                    // Invalid connect timeout.
                    LOG.warn("Invalid integer value specified for {}", connectTimeout.getKey(), e);
                }
            }
        }

        {
            ConfigProperty retries = clientConfig.getProperty(S3ClientProperty.MAX_RETRIES);
            if (Strings.isNotEmpty(retries.getValue())) {
                try {
                    clientConfiguration.setMaxErrorRetry(Integer.parseInt(retries.getValue().trim()));
                } catch (NumberFormatException e) {
                    // Invalid max retries
                    LOG.warn("Invalid integer value specified for {}", retries.getKey(), e);
                }
            }
        }

        {
            ConfigProperty readTimeout = clientConfig.getProperty(S3ClientProperty.READ_TIMEOUT);
            if (Strings.isNotEmpty(readTimeout.getValue())) {
                try {
                    clientConfiguration.setSocketTimeout(Integer.parseInt(readTimeout.getValue().trim()));
                } catch (NumberFormatException e) {
                    // Invalid connect timeout.
                    LOG.warn("Invalid integer value specified for {}", readTimeout.getKey(), e);
                }
            }
        }

        {
            ConfigProperty maxConnectionPoolSize = clientConfig.getProperty(S3ClientProperty.MAX_CONNECTION_POOL_SIZE);
            if (Strings.isNotEmpty(maxConnectionPoolSize.getValue())) {
                try {
                    clientConfiguration.setMaxConnections(Integer.parseInt(maxConnectionPoolSize.getValue().trim()));
                } catch (NumberFormatException e) {
                    // Invalid connect timeout.
                    LOG.warn("Invalid integer value specified for {}", maxConnectionPoolSize.getKey(), e);
                }
            }
        }

        String proxyHost = SystemPropertiesUtils.getProperty("http.proxyHost");
        if (proxyHost != null) {
            clientConfiguration.setProxyHost(proxyHost);
            String proxyPort = SystemPropertiesUtils.getProperty("http.proxyPort");
            if (proxyPort != null) {
                clientConfiguration.setProxyPort(Integer.parseInt(proxyPort));
            }

            String nonProxyHosts = SystemPropertiesUtils.getProperty("http.nonProxyHosts");
            if (Strings.isNotEmpty(nonProxyHosts)) {
                clientConfiguration.setNonProxyHosts(nonProxyHosts);
            }

            String login = SystemPropertiesUtils.getProperty("http.proxyUser");
            String password = SystemPropertiesUtils.getProperty("http.proxyPassword");

            if (login != null && password != null) {
                clientConfiguration.setProxyUsername(login);
                clientConfiguration.setProxyPassword(password);
            }
        }
        return clientConfiguration;
    }

}
