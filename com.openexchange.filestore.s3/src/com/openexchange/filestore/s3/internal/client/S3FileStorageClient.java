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

}
