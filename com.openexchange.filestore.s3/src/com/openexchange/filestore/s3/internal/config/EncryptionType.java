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

import java.util.Arrays;
import java.util.Optional;

/**
 * All available and supported {@link EncryptionType}s.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public enum EncryptionType {
    /**
     * No encryption at all
     */
    NONE("none", true),
    /**
     * Client side encryption via rsa
     */
    RSA("rsa", true),
    /**
     * Server side encryption
     */
    SSE_S3("sse-s3", false);

    private String name;
    private boolean clientSideEncryption;

    /**
     * Initializes a new {@link EncryptionType}.
     */
    private EncryptionType(String name, boolean clientSide) {
        this.name = name;
        this.clientSideEncryption = clientSide;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the clientSideEncryption
     *
     * @return The clientSideEncryption
     */
    public boolean isClientSideEncryption() {
        return clientSideEncryption;
    }

    /**
     * Returns the {@link EncryptionType} or null if no {@link EncryptionType} with this name exists.
     *
     * @param name The name of the {@link EncryptionType}
     * @return The {@link EncryptionType} or null
     */
    public static Optional<EncryptionType> getTypeByName(String name) {
        return Arrays.asList(values()).parallelStream().filter((t) -> t.getName().equals(name)).findAny();
    }

}
