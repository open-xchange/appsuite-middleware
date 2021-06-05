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

package com.openexchange.filestore.s3.internal.config;

import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.lean.Property;

/**
 * {@link S3ClientProperty}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public enum S3ClientProperty implements Property {
    /**
     * "com.openexchange.filestore.s3client.[clientID].endpoint"
     */
    ENDPOINT("endpoint", "s3.amazonaws.com"),
    /**
     * "com.openexchange.filestore.s3client.[clientID].region"
     */
    REGION("region", "us-west-2"),
    /**
     * "com.openexchange.filestore.s3client.[clientID].pathStyleAccess"
     */
    PATH_STYLE_ACCESS("pathStyleAccess", Boolean.TRUE),
    /**
     * "com.openexchange.filestore.s3client.[clientID].accessKey"
     */
    ACCESS_KEY("accessKey", null),
    /**
     * "com.openexchange.filestore.s3client.[clientID].secretKey"
     */
    SECRET_KEY("secretKey", null),
    /**
     * "com.openexchange.filestore.s3client.[clientID].encryption"
     */
    ENCRYPTION("encryption", EncryptionType.NONE.getName()),
    /**
     * "com.openexchange.filestore.s3client.[clientID].signerOverride"
     */
    SIGNER_OVERRIDE("signerOverride", "S3SignerType"),
    /**
     * "com.openexchange.filestore.s3client.[clientID].chunkSize"
     */
    CHUNK_SIZE("chunkSize", "5 MB"),
    /**
     * "com.openexchange.filestore.s3client.[clientID].encryption.rsa.keyStore"
     */
    RSA_KEYSTORE("encryption.rsa.keyStore", null),
    /**
     * "com.openexchange.filestore.s3client.[clientID].encryption.rsa.password"
     */
    RSA_PASSWORD("encryption.rsa.password", null),
    /**
     * "com.openexchange.filestore.s3client.[clientID].connectTimeout"
     */
    CONNECT_TIMEOUT("connectTimeout", "10000"),
    /**
     * "com.openexchange.filestore.s3client.[clientID].readTimeout"
     */
    READ_TIMEOUT("readTimeout", "50000"),
    /**
     * "com.openexchange.filestore.s3client.[clientID].maxConnectionPoolSize"
     */
    MAX_CONNECTION_POOL_SIZE("maxConnectionPoolSize", "50"),
    /**
     * "com.openexchange.filestore.s3client.[clientID].credentialsSource"
     */
    CREDENTIALS_SOURCE("credentialsSource", S3CredentialsSource.STATIC.getIdentifier()),
    /**
     * "com.openexchange.filestore.s3client.[clientID].buckets"
     */
    BUCKETS("buckets", ""),
    /**
     * "com.openexchange.filestore.s3client.[clientID].maxRetries"
     */
    MAX_RETRIES("maxRetries", "3"),
    ;

    public static final String PREFIX = "com.openexchange.filestore.s3client.";
    public static final String QUALIFIER_CLIENT_ID = "clientID";
    private static final String FQN_PREFIX = PREFIX + '[' + QUALIFIER_CLIENT_ID + "].";

    private final String name;
    private final String fqn;
    private final Object defaultValue;

    /**
     * Initializes a new {@link S3ClientProperty}.
     *
     * @param name The name of the property
     * @param defaultValue The default value
     */
    private S3ClientProperty(String name, Object defaultValue) {
        this.name = name;
        this.fqn = FQN_PREFIX + name;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets the short name of the {@link S3ClientProperty}
     *
     * @return The short name
     */
    public String getShortName() {
        return name;
    }

    /**
     * Gets a wildcard string that can be used to signal {@link Interests} by {@link Reloadable}s
     * to get callbacks whenever a configuration property was changed and reloaded.
     */
    public static String getInterestsWildcard() {
        return PREFIX + "*";
    }

}
