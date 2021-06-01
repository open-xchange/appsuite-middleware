
package com.openexchange.pgp.core;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
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
    private boolean          withIntegrityPacket  = true;
    private final int        algorithm;
    private final int        compressionAlgorithm;

    /**
     * Initializes a new {@link PGPSymmetricEncrypter} using AES-256 for uncompressed encryption.
     */
    public PGPSymmetricEncrypter() {
        this(PGPEncryptedData.AES_256, CompressionAlgorithmTags.UNCOMPRESSED);
    }

    /**
     * Initializes a new {@link PGPSymmetricEncrypter} for uncompressed encryption.
     *
     * @param algorithm The algorithm to use for encryption. See RFC-4880 (9.2 Symmetric key algorithm) for a list of supported algorithms.
     */
    public PGPSymmetricEncrypter(int algorithm) {
        this(algorithm, CompressionAlgorithmTags.UNCOMPRESSED);
    }

    /**
     * Initializes a new {@link PGPSymmetricEncrypter}.
     *
     * @param algorithm The algorithm to use for encryption. See RFC-4880 (9.2 Symmetric-Key Algorithms) for a list of supported algorithms.
     * @param compressionAlgorithm The compression algorithm to use. See RFC-4880 (9.3 Compression Algorithms) for a list of supported algorithms.
     */
    public PGPSymmetricEncrypter(int algorithm, int compressionAlgorithm) {
        this.algorithm = algorithm;
        this.compressionAlgorithm = compressionAlgorithm;
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

        PGPCompressedDataGenerator compressionGenerator = new PGPCompressedDataGenerator(compressionAlgorithm);

        try (OutputStream out = armored ? new ArmoredOutputStream(output) : output;
             OutputStream dOut = dataGenerator.open(out, new byte[BUFFERSIZE]);
             OutputStream cOut = compressionGenerator.open(dOut, new byte[BUFFERSIZE]);
             OutputStream ldOut = lData.open(cOut,
                PGPLiteralData.BINARY,
                PGPLiteralData.CONSOLE,
                new Date(),
                new byte[BUFFERSIZE])) {

            IOUtils.copy(input, ldOut);
        }
    }
}