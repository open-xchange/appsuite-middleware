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

import com.openexchange.config.lean.Property;

/**
 * {@link S3Properties}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public enum S3Properties implements Property {

    /**
     * "com.openexchange.filestore.s3.[filestoreID].endpoint"
     */
    ENDPOINT("endpoint", "s3.amazonaws.com"),
    /**
     * "com.openexchange.filestore.s3.[filestoreID].bucketName"
     */
    BUCKET_NAME("bucketName"),
    /**
     * "com.openexchange.filestore.s3.[filestoreID].region"
     */
    REGION("region", "us-west-2"),
    /**
     * "com.openexchange.filestore.s3.[filestoreID].pathStyleAccess"
     */
    PATH_STYLE_ACCESS("pathStyleAccess", Boolean.TRUE),
    /**
     * "com.openexchange.filestore.s3.[filestoreID].accessKey"
     */
    ACCESS_KEY("accessKey"),
    /**
     * "com.openexchange.filestore.s3.[filestoreID].secretKey"
     */
    SECRET_KEY("secretKey"),
    /**
     * "com.openexchange.filestore.s3.[filestoreID].encryption"
     */
    ENCRYPTION("encryption", EncryptionType.NONE.getName()),
    /**
     * "com.openexchange.filestore.s3.[filestoreID].signerOverride"
     */
    SIGNER_OVERRIDE("signerOverride", "S3SignerType"),
    /**
     * "com.openexchange.filestore.s3.[filestoreID].chunkSize"
     */
    CHUNK_SIZE("chunkSize", "5 MB"),
    /**
     * "com.openexchange.filestore.s3.[filestoreID].encryption.rsa.keyStore"
     */
    RSA_KEYSTORE("encryption.rsa.keyStore"),
    /**
     * "com.openexchange.filestore.s3.[filestoreID].encryption.rsa.password"
     */
    RSA_PASSWORD("encryption.rsa.password"),
    /**
     * "com.openexchange.filestore.s3.[filestoreID].connectTimeout"
     */
    CONNECT_TIMEOUT("connectTimeout", "10000"),
    /**
     * "com.openexchange.filestore.s3.[filestoreID].readTimeout"
     */
    READ_TIMEOUT("readTimeout", "50000"),
    /**
     * "com.openexchange.filestore.s3.[filestoreID].maxConnectionPoolSize"
     */
    MAX_CONNECTION_POOL_SIZE("maxConnectionPoolSize", "50"),

    /**
     * "com.openexchange.filestore.s3.metricCollection"
     */
    METRIC_COLLECTION("com.openexchange.filestore.s3.", "metricCollection", Boolean.FALSE)
    ;

    public static final String OPTIONAL_NAME = "filestoreID";
    private static final String PREFIX = "com.openexchange.filestore.s3.[" + OPTIONAL_NAME + "].";

    private final Object defaultValue;
    private final String fqn;

    /**
     * Initializes a new {@link S3Properties}.
     */
    private S3Properties(String propName) {
        this(propName, null);
    }

    /**
     * Initializes a new {@link S3Properties}.
     */
    private S3Properties(String propName, Object defaultValue) {
        this(PREFIX, propName, defaultValue);
    }

    /**
     * Initializes a new {@link S3Properties}.
     */
    private S3Properties(String prefix, String propName, Object defaultValue) {
        this.fqn = prefix + propName;
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

}
