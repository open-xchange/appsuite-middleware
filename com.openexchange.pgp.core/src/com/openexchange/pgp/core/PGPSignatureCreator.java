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
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import com.openexchange.exception.OXException;
import com.openexchange.pgp.core.exceptions.PGPCoreExceptionCodes;

/**
 * {@link PGPSignatureCreator} - Wrapper for creating PGP signatures
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class PGPSignatureCreator {

    private static final int BUFFERSIZE = 256;
    private final int hashAlgorithm;

    /**
     * Initializes a new {@link PGPSignatureCreator} using {@link HashAlgorithmTags.SHA512}
     */
    public PGPSignatureCreator() {
        this(HashAlgorithmTags.SHA512);
    }

    /**
     * Initializes a new {@link PGPSignatureCreator}
     *
     * @param hashAlgorithm The algorithm to use for creating the signature hash
     */
    public PGPSignatureCreator(int hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    /**
     * Create the content type for the pgp signed email, specifying the hash type
     * getHeader
     *
     * @return Content-type to be used for the signed email
     */
    public String getContentType() {
        StringBuilder sb = new StringBuilder();
        sb.append("signed; micalg=pgp-");
        switch (hashAlgorithm) {
            case HashAlgorithmTags.SHA512:
                sb.append("sha512");
                break;
            case HashAlgorithmTags.SHA256:
                sb.append("sha256");
                break;
            case HashAlgorithmTags.SHA1:
                sb.append("sha1");
                break;
            default:
                sb.append("sha1");
                break;
        }
        sb.append("; protocol=\"application/pgp-signature\"");
        return sb.toString();
    }

    /**
     * Creates a signature
     *
     * @param input The data to create a signature for
     * @param output The signature
     * @param armored True, if the signature should be written ASCII-Armored, false if binary
     * @param signingKey The key to use for singing
     * @param password The password for the key
     * @throws IOException
     * @throws PGPException
     */
    public void createSignature(InputStream input, OutputStream output, boolean armored, PGPSecretKey signingKey, char[] password) throws OXException {
        output = armored ? new ArmoredOutputStream(output) : output;

        try {
            PGPPrivateKey privateKey = null;
            try {
                //Extract private key
                PBESecretKeyDecryptor extrator = new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(password);
                privateKey = signingKey.extractPrivateKey(extrator);
            } catch (PGPException e) {
                throw PGPCoreExceptionCodes.BAD_PASSWORD.create();
            }

            //Initialize signature
            int algorithm = signingKey.getPublicKey().getAlgorithm();
            PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(algorithm, hashAlgorithm).setProvider("BC"));
            signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey);

            //Signing the data
            try (BCPGOutputStream signingStream = new BCPGOutputStream(output)) {
                byte[] buffer = new byte[BUFFERSIZE];
                int len = 0;
                while ((len = input.read(buffer)) > -1) {
                    signatureGenerator.update(buffer, 0, len);
                }
                signatureGenerator.generate().encode(signingStream);
            }
        } catch (PGPException e) {
            PGPCoreExceptionCodes.PGP_EXCEPTION.create(e, e.getMessage());
        } catch (IOException e) {
            PGPCoreExceptionCodes.IO_EXCEPTION.create(e, e.getMessage());
        } finally {
            if (armored) {
                try {
                    output.close();
                } catch (IOException e) {
                    //no-op
                }
            }
        }
    }
}
