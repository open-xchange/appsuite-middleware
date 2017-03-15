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
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import com.openexchange.exception.OXException;
import com.openexchange.pgp.core.exceptions.PGPCoreExceptionCodes;

/**
 * {@link PGPEncrypter} - Wrapper for providing stream based PGP encryption
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v2.4.2
 */
public class PGPEncrypter {

    private static final int BUFFERSIZE = 256;

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
            int algorithm = signingKey.getPublicKey().getAlgorithm();
            signatureGenerator = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(algorithm, PGPUtil.SHA512).setProvider("BC"));
            try {
                PBESecretKeyDecryptor extractor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(password);
                signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, signingKey.extractPrivateKey(extractor));
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
        BcPGPDataEncryptorBuilder builder = new BcPGPDataEncryptorBuilder(PGPEncryptedData.AES_256);
        builder.setSecureRandom(new SecureRandom());
        builder.setWithIntegrityPacket(true);

        //Adding recipients
        PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(builder);
        for (PGPPublicKey recipient : recipientsKeys) {
            BcPublicKeyKeyEncryptionMethodGenerator encKeyGen = new BcPublicKeyKeyEncryptionMethodGenerator(recipient);
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
