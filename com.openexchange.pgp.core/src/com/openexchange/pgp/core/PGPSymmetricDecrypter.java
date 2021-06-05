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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPBEEncryptedData;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBEDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;

/**
 * {@link PGPSymmetricDecrypter} offers PGP based, symmetric decryption
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class PGPSymmetricDecrypter {

    private static final int BUFFERSIZE = 256;

    /**
     * Decrypts, pgp based, symmetric encrypted data
     *
     * @param input The data to decrypt
     * @param output The output stream to write the decrypted data to
     * @param key The secret symmetric key required for decryption
     * @throws IOException
     * @throws PGPException
     */
    public void decrypt(InputStream input, OutputStream output, char[] key) throws IOException, PGPException {
        try (InputStream decoderStream = PGPUtil.getDecoderStream(input)) {
            PGPObjectFactory objectFact = new PGPObjectFactory(decoderStream, new BcKeyFingerprintCalculator());
            Object pgpObject = objectFact.nextObject();
            if (pgpObject instanceof PGPEncryptedDataList) {
                Object nextObj = ((PGPEncryptedDataList) pgpObject).get(0);
                if (nextObj instanceof PGPPBEEncryptedData) {
                    PGPPBEEncryptedData encrypted = (PGPPBEEncryptedData) nextObj;
                    InputStream decrypted = encrypted
                        .getDataStream(new BcPBEDataDecryptorFactory(key, new BcPGPDigestCalculatorProvider()));
                    PGPObjectFactory decrFactor = new PGPObjectFactory(decrypted, new BcKeyFingerprintCalculator());
                    Object decrKey = decrFactor.nextObject();
                    // Check for compressed data
                    if (decrKey instanceof PGPCompressedData) {
                        PGPCompressedData compressedData = (PGPCompressedData) decrKey;
                        objectFact = new PGPObjectFactory(compressedData.getDataStream(), new BcKeyFingerprintCalculator());
                        decrKey = objectFact.nextObject();
                    }
                    // Handle literal data
                    if (decrKey instanceof PGPLiteralData) {
                        PGPLiteralData decrData = (PGPLiteralData) decrKey;
                        byte[] buffer = new byte[BUFFERSIZE];
                        int len = 0;
                        while ((len = decrData.getDataStream().read(buffer)) > -1) {
                            output.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }
}
