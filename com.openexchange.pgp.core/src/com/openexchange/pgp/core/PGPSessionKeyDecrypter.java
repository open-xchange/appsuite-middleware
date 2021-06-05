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

import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.operator.PublicKeyDataDecryptorFactory;
import com.openexchange.pgp.keys.common.PGPSymmetricKey;

/**
 * {@link PGPSessionKeyDecrypter} decrypts PGP data with the known/extracted PGP session key data.
 *
 * @see {@link PGPSessionKeyExtractor} for extracting the session key from a given PGP message.
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.0
 */
public class PGPSessionKeyDecrypter extends PGPDecrypter {

    private final byte[] key;

    /**
     * Initializes a new {@link PGPSessionKeyDecrypter}.
     *
     * @param key The symmetric key data to use for decryption
     */
    PGPSessionKeyDecrypter(final byte[] key) {
        //Not dealing with asymmetric keys, because this class knows the symmetric key for decrypting the PGP data.
        super(new PGPKeyRetrievalStrategy() {

            @Override
            public PGPPrivateKey getSecretKey(long keyId, String userIdentity, char[] password) throws Exception {
                return null;
            }

            @Override
            public PGPPublicKey getPublicKey(long keyId) throws Exception {
                return null;
            }
        });
        this.key = key;
    }

    /**
     * Initializes a new {@link PGPSessionKeyDecrypter}.
     *
     * @param key The symmetric key data to use for decryption
     * @param strategy A custom strategy to retrieval public keys for signature verification
     */
    public PGPSessionKeyDecrypter(final byte[] key, PGPKeyRetrievalStrategy strategy) {
        super(strategy);
        this.key = key;
    }

    /**
     * Initializes a new {@link PGPSessionKeyDecrypter}.
     *
     * @param The symmetric key to use for decryption
     */
    public PGPSessionKeyDecrypter(PGPSymmetricKey key) {
        this(key.getKeyData());
    }

    /**
     * Initializes a new {@link PGPSessionKeyDecrypter}.
     *
     * @param The symmetric key to use for decryption
     * @param strategy A custom strategy to retrieval public keys for signature verification
     */
    public PGPSessionKeyDecrypter(PGPSymmetricKey key, PGPKeyRetrievalStrategy strategy) {
        this(key.getKeyData(), strategy);
    }

    @Override
    protected boolean keyFound(PGPPrivateKey key) {
        //Not dealing with asymmetric keys, because this class knows the symmetric key for decrypting the PGP data.
        return true;
    }

    @Override
    protected PublicKeyDataDecryptorFactory getDecryptionFactory(PGPDataContainer publicKeyEncryptedData) {
        return new SymmetricKeyDataDecryptorFactory(key);
    }

    /**
     * Decrypts the given PGP data.
     *
     * @param input The input stream to read the PGP data from
     * @param output The output stream to write the decoded data to
     * @return The decryption result
     * @throws Exception
     */
    public PGPDecryptionResult decrypt(InputStream input, OutputStream output) throws Exception {
        return super.decrypt(input, output, null, null);
    }
}
