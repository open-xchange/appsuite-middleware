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
    ENDPOINT("endpoint", "s3.amazonaws.com"),
    BUCKET_NAME("bucketName", null),
    REGION("region", "us-west-2"),
    PATH_STYLE_ACCESS("pathStyleAccess", true),
    ACCESS_KEY("accessKey", null),
    SECRET_KEY("secretKey", null),
    ENCRYPTION("encryption", EncryptionType.NONE.getName()),
    SIGNER_OVERRIDE("signerOverride", "S3SignerType"),
    CHUNK_SIZE("chunkSize", "5 MB"),
    RSA_KEYSTORE("encryption.rsa.keyStore", null),
    RSA_PASSWORD("encryption.rsa.password", null),

    METRIC_COLLECTION("com.openexchange.filestore.s3.", "metricCollection", false)
    ;

    public static final String OPTIONAL_NAME = "filestoreID";
    private static final String PREFIX = "com.openexchange.filestore.s3.[" + OPTIONAL_NAME + "].";

    private final Object defaultValue;
    private final String propName;
    private final String prefix;

    /**
     * Initializes a new {@link S3Properties}.
     */
    private S3Properties(String prefix, String propName, Object defaultValue) {
        this.propName = propName;
        this.defaultValue = defaultValue;
        this.prefix = prefix;
    }

    /**
     * Initializes a new {@link S3Properties}.
     */
    private S3Properties(String propName, Object defaultValue) {
        this.propName = propName;
        this.defaultValue = defaultValue;
        this.prefix = PREFIX;
    }

    @Override
    public String getFQPropertyName() {
        return prefix + propName;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}
