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

import java.util.Objects;
import org.bouncycastle.bcpg.PublicKeyEncSessionPacket;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.operator.PublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import com.openexchange.pgp.core.packethandling.ExtractSessionProcessorHandler.EncryptedSession;

/**
 * {@link PGPSessionDecrypter} provides functionality to decrypt a Public-Key encrypted symmetric PGP session key.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.0
 */
public class PGPSessionDecrypter {

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
