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

package com.openexchange.ajax.multifactor.u2fclient;

import java.security.PrivateKey;

/**
 * {@link U2FClientCrypto} - cryptographic interface required for FIDO U2F
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.1
 */
public interface U2FClientCrypto {

    /**
     * Creates a SHA256 hash from the given data
     *
     * @param data The data to create the hash from
     * @return The raw sha256 hash of the given data
     * @throws U2FClientException
     */
    public byte[] sha256(byte[] data) throws U2FClientException;

    /**
     * Signs the data with the given {@link PrivateKey}
     *
     * @param data The data to sign
     * @param key The key used for signing
     * @return The signed data
     * @throws U2FClientException
     */
    public byte[] sign(byte[] data, PrivateKey key) throws U2FClientException;
}
