
package com.openexchange.pgp.core;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.operator.bc.BcPBEKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;

/**
 * {@link PGPSymmetricEncrypter} offers PGP based, symmetric encryption
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class PGPSymmetricEncrypter {

    private static final int BUFFERSIZE          = 256;
    private boolean          withIntegrityPacket = true;
    private final int        algorithm;

    /**
     * Initializes a new {@link PGPSymmetricEncrypter} using AES-256 for encryption.
     */
    public PGPSymmetricEncrypter() {
        this(PGPEncryptedData.AES_256);
    }

    /**
     * Initializes a new {@link PGPSymmetricEncrypter}.
     *
     * @param algorithm The algorithm to use for encryption. See RFC-4880 (9.2 Symmetric key algorithm) for a list of supported algorithms.
     */
    public PGPSymmetricEncrypter(int algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Enables or disables adding MDC for integrity validation
     *
     * @param withIntegrityPacket true, to add a MDC packet, false otherwise
     * @return this
     */
    public PGPSymmetricEncrypter setWithIntegrityPacket(boolean withIntegrityPacket) {
        this.withIntegrityPacket = withIntegrityPacket;
        return this;
    }

    /**
     * Symetric, pgp based, encryption of data
     *
     * @param input The plaintext data to encrypt
     * @param output The output stream to write the encrypted data to
     * @param armored True, if the encrypted data should be written ASCII-Armored, false if binary
     * @param key The secret symmetric key used for encryption
     * @throws IOException
     * @throws PGPException
     */
    public void encrypt(InputStream input, OutputStream output, boolean armored, char[] key) throws IOException, PGPException {
        final PGPEncryptedDataGenerator dataGenerator = new PGPEncryptedDataGenerator(
            new BcPGPDataEncryptorBuilder(algorithm)
                .setSecureRandom(new SecureRandom())
                .setWithIntegrityPacket(withIntegrityPacket));
        dataGenerator.addMethod(new BcPBEKeyEncryptionMethodGenerator(key));
        final PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();

        try (OutputStream out = armored ? new ArmoredOutputStream(output) : output;
             OutputStream cOut = dataGenerator.open(out, new byte[BUFFERSIZE]);
             OutputStream ldOut = lData.open(cOut,
                PGPLiteralData.BINARY,
                PGPLiteralData.CONSOLE,
                new Date(),
                new byte[BUFFERSIZE])) {

            IOUtils.copy(input, ldOut);
        }
    }
}