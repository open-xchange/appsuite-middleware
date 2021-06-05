
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

package com.openexchange.pgp.core;

import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;

/**
 * {@link PGPKeyRetrievalStrategy} - A strategy for retrieving PGP keys by their PGP ID
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public interface PGPKeyRetrievalStrategy {

    /**
     * Retrieves a private key
     *
     * @param keyId the ID of the secret key to retrieve
     * @param keyId the ID to check for (can be null in order to check only for keyId)
     * @param password The password in order to retrieve the private key
     * @return The secret key with the given ID , or null if no such key was found
     * @throws Exception
     */
    PGPPrivateKey getSecretKey(long keyId, String userIdentity, char[] password) throws Exception;

    /**
     * Retrieves a public key
     *
     * @param keyId the ID of the public key to retrieved
     * @return The public key with the given ID, or null if no such key was found
     * @throws Exception
     */
    PGPPublicKey getPublicKey(long keyId) throws Exception;
}
