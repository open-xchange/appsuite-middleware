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
 *    trademarks of the OX Software GmbH. group of companies.
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
