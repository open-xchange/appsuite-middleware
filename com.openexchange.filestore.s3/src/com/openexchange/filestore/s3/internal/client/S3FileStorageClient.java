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

import com.amazonaws.services.s3.AmazonS3Client;
import com.openexchange.filestore.s3.internal.config.S3ClientScope;
import com.openexchange.filestore.s3.internal.config.S3EncryptionConfig;

/**
 * A wrapper class holding the actual {@link AmazonS3Client} along with some
 * runtime configuration values that are needed to perform requests using this
 * client and managing the lifecycle of this instance.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class S3FileStorageClient {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(S3FileStorageClient.class);

    private final String key;
    private final AmazonS3Client client;
    private final S3EncryptionConfig encryptionConfig;
    private final long chunkSize;
    private final S3ClientScope scope;
    private final int configFingerprint;

    /**
     * Initializes a new {@link S3FileStorageClient}.
     *
     * @param key The client key to identify it within the registry
     * @param client The {@link AmazonS3Client}
     * @param encryptionConfig The {@link S3EncryptionConfig}
     * @param chunkSize The chunk size
     * @param scope The client scope
     * @param configFingerprint The config fingerprint at the time of this clients creation
     */
    S3FileStorageClient(String key, AmazonS3Client client, S3EncryptionConfig encryptionConfig, long chunkSize, S3ClientScope scope, int configFingerprint) {
        super();
        this.key = key;
        this.client = client;
        this.encryptionConfig = encryptionConfig;
        this.chunkSize = chunkSize;
        this.scope = scope;
        this.configFingerprint = configFingerprint;
    }

    /**
     * Gets the key that uniquely identifies the client in configuration and the registry
     *
     * @return The key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the actual AWS S3 SDK client
     *
     * @return The client
     */
    public AmazonS3Client getSdkClient() {
        return client;
    }

    /**
     * Gets the object encryption configuration
     *
     * @return The config
     */
    public S3EncryptionConfig getEncryptionConfig() {
        return encryptionConfig;
    }

    /**
     * Gets the chunk size to use for multipart uploads
     *
     * @return The chunkSize
     */
    public long getChunkSize() {
        return chunkSize;
    }

    /**
     * Gets the client scope
     *
     * @return The scope
     */
    public S3ClientScope getScope() {
        return scope;
    }

    /**
     * Gets the configuration fingerprint to compare two instances of the same client configuration
     *
     * @return The fingerprint
     */
    int getConfigFingerprint() {
        return configFingerprint;
    }

    @Override
    protected void finalize() throws Throwable {
        LOGGER.info("Going to destroy S3 file storage client having key {} with scope {}", key, scope);
        super.finalize();
    }

}
