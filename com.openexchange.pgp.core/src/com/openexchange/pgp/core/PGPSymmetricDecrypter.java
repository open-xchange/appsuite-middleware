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

package com.openexchange.pgp.core;

import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.operator.PublicKeyDataDecryptorFactory;
import com.openexchange.pgp.keys.common.PGPSymmetricKey;

/**
 * {@link PGPSymmetricDecrypter} decrypts PGP data with a known symmetric key (PGP session key).
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.0
 */
public class PGPSymmetricDecrypter extends PGPDecrypter {

    private final byte[] key;

    /**
     * Initializes a new {@link PGPSymmetricDecrypter}.
     *
     * @param key The symmetric key data to use for decryption
     */
    PGPSymmetricDecrypter(final byte[] key) {
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
     * Initializes a new {@link PGPSymmetricDecrypter}.
     *
     * @param key The symmetric key data to use for decryption
     * @param strategy A custom strategy to retrieval public keys for signature verification
     */
    public PGPSymmetricDecrypter(final byte[] key, PGPKeyRetrievalStrategy strategy) {
        super(strategy);
        this.key = key;
    }

    /**
     * Initializes a new {@link PGPSymmetricDecrypter}.
     *
     * @param The symmetric key to use for decryption
     */
    public PGPSymmetricDecrypter(PGPSymmetricKey key) {
        this(key.getKeyData());
    }

    /**
     * Initializes a new {@link PGPSymmetricDecrypter}.
     *
     * @param The symmetric key to use for decryption
     * @param strategy A custom strategy to retrieval public keys for signature verification
     */
    public PGPSymmetricDecrypter(PGPSymmetricKey key, PGPKeyRetrievalStrategy strategy) {
        this(key.getKeyData(), strategy);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.pgp.core.PGPDecrypter#keyFound(org.bouncycastle.openpgp.PGPPrivateKey)
     */
    @Override
    protected boolean keyFound(PGPPrivateKey key) {
        //Not dealing with asymmetric keys, because this class knows the symmetric key for decrypting the PGP data.
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.pgp.core.PGPDecrypter#getDecryptionFactory(com.openexchange.pgp.core.PGPDecrypter.PGPDataContainer)
     */
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
