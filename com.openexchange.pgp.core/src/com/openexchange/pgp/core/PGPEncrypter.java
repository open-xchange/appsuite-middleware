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
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;
import com.openexchange.exception.OXException;
import com.openexchange.pgp.core.exceptions.PGPCoreExceptionCodes;

/**
 * {@link PGPEncrypter} - Wrapper for providing stream based PGP encryption
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class PGPEncrypter {

    private static final int BUFFERSIZE = 256;
    private boolean withIntegrityPacket = true;

    /**
     * Encrypts data
     *
     * @param input The input stream to read the data from
     * @param output The output stream to write the encrypted data to
     * @param armored True, if the encrypted data should be written ASCII-Armored, false if binary
     * @param recipientsKeys A collection of public keys which are used for encrypting the data
     * @throws IOException
     * @throws PGPException
     * @throws OXException
     */
    public void encrypt(InputStream input, OutputStream output, boolean armored, PGPPublicKey... recipientsKeys) throws IOException, PGPException, OXException {
        encryptSigned(input,
            output,
            armored,
            (PGPSecretKey) null /* do not sign */,
            null /* no signing password required */,
            recipientsKeys);
    }

    /**
     * Enables or disabled adding MDC for integrity validation
     *
     * @param withIntegrityPacket true, to add a MDC packet, false otherwise
     * @return this
     */
    PGPEncrypter setWithIntegrityPacket(boolean withIntegrityPacket) {
        this.withIntegrityPacket = withIntegrityPacket;
        return this;
    }

    /**
     * Encrypts and signs data
     *
     * @param input The input stream to read the data from
     * @param output The output stream to write the encrypted data to
     * @param armored True, if the encrypted data should be written ASCII-Armored, false if binary
     * @param signingKey The private key used for signing
     * @param password The password of the private key
     * @param recipientsKeys A collection of public keys which are used for encrypting the data
     * @throws IOException
     * @throws PGPException
     * @throws OXException
     */
    public void encryptSigned(InputStream input, OutputStream output, boolean armored, PGPSecretKey signingKey, char[] password, PGPPublicKey... recipientsKeys) throws IOException, PGPException, OXException {
        boolean signing = signingKey != null;
        output = armored ? new ArmoredOutputStream(output) : output;

        //Initialize signing if a signing key was provided
        PGPSignatureGenerator signatureGenerator = null;
        if (signing) {
            @SuppressWarnings("null") int algorithm = signingKey.getPublicKey().getAlgorithm(); // Guarded by 'signing'
            signatureGenerator = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(algorithm, PGPUtil.SHA512).setProvider("BC"));
            try {
                PBESecretKeyDecryptor extractor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(password);
                PGPPrivateKey extractedPrivateKey = signingKey.extractPrivateKey(extractor);
                if (null == extractedPrivateKey) {
                    throw new IllegalArgumentException("Could not extract private PGP key from specified signing key.");
                }
                signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, extractedPrivateKey);
            }
            catch(Exception e) {
                if (armored) {
                    output.close();
                }
                throw PGPCoreExceptionCodes.BAD_PASSWORD.create();
            }
            Iterator<String> iter = signingKey.getPublicKey().getUserIDs();
            if (iter.hasNext()) {
                PGPSignatureSubpacketGenerator signatureSubpacketGenerator = new PGPSignatureSubpacketGenerator();
                signatureSubpacketGenerator.setSignerUserID(false, iter.next());
                signatureGenerator.setHashedSubpackets(signatureSubpacketGenerator.generate());
            }
        }

        //Initialize encrypting
        JcePGPDataEncryptorBuilder builder = new JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256);
        builder.setSecureRandom(new SecureRandom());
        builder.setWithIntegrityPacket(withIntegrityPacket);
        //Adding recipients
        PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(builder);
        for (PGPPublicKey recipient : recipientsKeys) {
            JcePublicKeyKeyEncryptionMethodGenerator encKeyGen = new JcePublicKeyKeyEncryptionMethodGenerator(recipient).setProvider("BC");
            encryptedDataGenerator.addMethod(encKeyGen);
        }

        //Encrypt
        PGPCompressedDataGenerator compressedDataGenerator = null;
        PGPLiteralDataGenerator literalDataGenerator = null;
        OutputStream encryptingStream = null;
        BCPGOutputStream compressedEncryptingStream = null;
        OutputStream literalDataStream = null;
        try {
            compressedDataGenerator = new PGPCompressedDataGenerator(PGPCompressedData.ZLIB);
            encryptingStream = encryptedDataGenerator.open(output, new byte[4028]);
            compressedEncryptingStream = new BCPGOutputStream(compressedDataGenerator.open(encryptingStream));
            if (signing) {
                //Generate signature
                signatureGenerator.generateOnePassVersion(false).encode(compressedEncryptingStream);
            }

            literalDataGenerator = new PGPLiteralDataGenerator();
            literalDataStream = literalDataGenerator.open(compressedEncryptingStream, PGPLiteralData.BINARY, "encrypted.asc", new Date(), new byte[BUFFERSIZE]);
            byte[] buffer = new byte[BUFFERSIZE];
            int len = 0;
            while ((len = input.read(buffer)) > -1) {
                literalDataStream.write(buffer, 0, len);
                if (signing) {
                    signatureGenerator.update(buffer, 0, len);
                }
            }
        } finally {
            if (literalDataGenerator != null) {
                literalDataGenerator.close();
            }

            if (signing && compressedEncryptingStream != null) {
                //Finalize signature
                signatureGenerator.generate().encode(compressedEncryptingStream);
            }

            if (compressedEncryptingStream != null) {
                compressedDataGenerator.close();
            }

            if (encryptingStream != null) {
                encryptingStream.close();
            }

            if (armored) {
                output.close();
            }
        }
    }
}
