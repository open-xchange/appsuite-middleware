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

package com.openexchange.pgp.keys.common;

import com.openexchange.tools.encoding.Base64;

/**
 * {@link PGPSymmetricKey} represents a symmetric PGP session key
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.0
 */
public class PGPSymmetricKey implements AutoCloseable{

    private byte[] keyData;

    /**
     * Initializes a new {@link PGPSymmetricKey}.
     *
     * @param value The symmetric key value
     */
    private PGPSymmetricKey(byte[] keyData) {
        this.keyData = keyData;
    }

    /**
     * Creates a new {@link PGPSymmetricKey} from base64 encoded data
     *
     * @param base64 the encoded data
     * @return A new {@link PGPSymmetricKey}
     */
    public static PGPSymmetricKey fromBase64(String base64) {
        return new PGPSymmetricKey(Base64.decode(base64));
    }

    /**
     * Gets the raw symmetric key data.
     *
     * @return The raw symmetric key data
     */
    public byte[] getKeyData() {
        return this.keyData;
    }

    /**
     * Wipes the key data from memory
     */
    public void wipe() {
        for (int i = 0; i < keyData.length; i++) {
            keyData[i] = 0x0;
        }
    }

    @Override
    public void close() throws Exception {
        wipe();
    }
}
