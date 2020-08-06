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

import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 *
 * {@link S3EncryptionConfig} holds encryption types for client- and server-side encryption for a single filestore.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class S3EncryptionConfig {

    private final EncryptionType clientEncryption;
    private final EncryptionType serverSideEncryption;

    /**
     * Initializes a new {@link S3EncryptionConfig}.
     *
     * @param config The configuration string containing a combination of client-side and server-side encryption separated by <code>+</code> symbol (e.g. <code>rsa+sse-s3</code>)
     * @throws OXException In case the configuration is invalid
     */
    public S3EncryptionConfig(String config) throws OXException {
        if (Strings.isEmpty(config)) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("An empty encryption type is invalid");
        }
        int index = config.indexOf('+');
        if (index > 0) {
            // E.g. rsa+sse-s3
            String[] encryptionTypes = Strings.splitBy(config, '+', true);
            if (encryptionTypes.length != 2) {
                throw invalidEncryptionTypeCombination();
            }

            EncryptionType typeA = EncryptionType.getTypeByName(encryptionTypes[0]).orElseThrow(() -> unknownEncryptionTypeException(encryptionTypes[0]));
            EncryptionType typeB = EncryptionType.getTypeByName(encryptionTypes[1]).orElseThrow(() -> unknownEncryptionTypeException(encryptionTypes[1]));

            if (typeA.isClientSideEncryption() == typeB.isClientSideEncryption()) {
                throw invalidEncryptionTypeCombination();
            }

            if (typeA.isClientSideEncryption()) {
                clientEncryption = typeA;
                serverSideEncryption = typeB;
            } else {
                clientEncryption = typeB;
                serverSideEncryption = typeA;
            }
        } else {
            EncryptionType type = EncryptionType.getTypeByName(config).orElseThrow(() -> unknownEncryptionTypeException(config));
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

    /**
     * Checks whether client-side encryption is enabled or not
     *
     * @return <code>true</code> if the client-side encryption is enabled
     */
    public boolean isClientEncryptionEnabled() {
        return clientEncryption != null && clientEncryption != EncryptionType.NONE;
    }

    /**
     * Checks whether server-side encryption is enabled or not
     *
     * @return <code>true</code> if server-side encryption is enabled, <code>false</code> otherwise
     */
    public boolean isServerSideEncryptionEnabled() {
        return serverSideEncryption != null && clientEncryption != EncryptionType.NONE;
    }

    private static OXException unknownEncryptionTypeException(String type) {
        return ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Unknown encryption type: " + type);
    }

    private static OXException invalidEncryptionTypeCombination() {
        return ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("It's only allowed to combine one client side encryption type and one server side encryption type.");
    }

}