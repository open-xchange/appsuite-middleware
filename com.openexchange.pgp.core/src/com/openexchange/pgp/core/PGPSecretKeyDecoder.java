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

import java.util.Iterator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;

/**
 * {@link PGPSecretKeyDecoder} - Obtains the raw PGPPrivateKey from a given, password protected, PGPSecretKey.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class PGPSecretKeyDecoder {

    /**
     * Decodes the {@link PGPPrivateKey} from the given {@link PGPSecretKey} using a password.
     *
     * @param secretKey The secret key to decode the private key from
     * @param password The password
     * @return The decoded private key
     * @throws PGPException
     */
    public static PGPPrivateKey decodePrivateKey(PGPSecretKey secretKey, char[] password) throws PGPException {
        PBESecretKeyDecryptor extractor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(password);
        return secretKey.extractPrivateKey(extractor);
    }

    /**
     * Decodes a {@link PGPPrivateKey} from the given {@link PGPSecretKeyRing}.
     *
     * It is preferred to not return the master key. Only return master if no other keys found.
     *
     * @param PGPSecretKeyRing The secret key ring to decode a private key from.
     * @param password The password
     * @return The decoded private key
     * @throws PGPException
     */
    public static PGPPrivateKey decodePrivateKey(PGPSecretKeyRing secretKeyRing, char[] password) throws PGPException {
        Iterator<PGPSecretKey> it = secretKeyRing.getSecretKeys();
        PGPSecretKey master = null;
        while (it.hasNext()) {
            PGPSecretKey key = it.next();
            if (!key.isMasterKey()) { // We prefer to not return the master.  Only return master if no other encr keys found
                return decodePrivateKey(key, password);
            }
            master = key;
        }
        if (master != null) {
            return decodePrivateKey(master, password);
        }
        return null;
    }
}
