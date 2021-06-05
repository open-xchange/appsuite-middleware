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

import java.util.Objects;
import org.bouncycastle.bcpg.PublicKeyEncSessionPacket;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.operator.PublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import com.openexchange.pgp.core.packethandling.ExtractSessionProcessorHandler.EncryptedSession;

/**
 * {@link PGPSessionKeyExtractor} provides functionality to extract/decrypt a Public-Key encrypted symmetric PGP session key from a PGP message.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.0
 */
public class PGPSessionKeyExtractor {

    /**
     * Internal method to create a decrypto factory
     *
     * @param privateKey The key used for decryption
     * @return A factory for decrypting
     */
    private PublicKeyDataDecryptorFactory createDecryptorFactory(PGPPrivateKey privateKey) {
        return new BcPublicKeyDataDecryptorFactory(privateKey);
    }

    /**
     * Decrypts a symmetric session key from the Public-Key encrypted PGP session
     *
     * @param sessionPaket The session paket to extract the symmetric key from
     * @param privateKey The private key used for decryption
     * @return The symmetric PGP session key
     * @throws PGPException
     */
    public byte[] decryptSymmetricSessionKey(PublicKeyEncSessionPacket sessionPaket, PGPPrivateKey privateKey) throws PGPException {
        sessionPaket = Objects.requireNonNull(sessionPaket, "sessionPaket must not be null");
        return decryptSymmetricSessionKey(sessionPaket.getAlgorithm(), sessionPaket.getEncSessionKey(), privateKey);
    }

    /**
     * Decrypts a symmetric session key from the Public-Key encrypted PGP session
     *
     * @param encryptedSession The session to extract the symmetric key from
     * @param privateKey The private key used for decryption
     * @return The symmetric PGP session key
     * @return
     * @throws PGPException
     */
    public byte[] decryptSymmetricSessionKey(EncryptedSession encryptedSession, PGPPrivateKey privateKey) throws PGPException {
        encryptedSession = Objects.requireNonNull(encryptedSession, "encryptedSession must not be null");
        return decryptSymmetricSessionKey(encryptedSession.getAlgorithm(), encryptedSession.getEncryptedSessionKey(), privateKey);
    }

    /**
     * Decrypts a symmetric session key from the Public-Key encrypted PGP session
     *
     * @param algorithm The kind public-key algorithm
     * @param encryptedSessionKey The sessionData
     * @param privateKey The private key used for decryption
     * @return The decrypted symmetric session key
     * @throws PGPException
     */
    public byte[] decryptSymmetricSessionKey(int algorithm, byte[][] encryptedSessionKey, PGPPrivateKey privateKey) throws PGPException {
        PublicKeyDataDecryptorFactory decryptorFactory = createDecryptorFactory(privateKey);
        return decryptorFactory.recoverSessionData(algorithm, encryptedSessionKey);
    }
}
