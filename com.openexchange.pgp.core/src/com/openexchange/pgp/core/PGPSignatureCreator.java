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
 * @since v2.4.2
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
        }
        catch(PGPException e) {
            PGPCoreExceptionCodes.PGP_EXCEPTION.create(e, e.getMessage());
        }
        catch(IOException e) {
            PGPCoreExceptionCodes.IO_EXCEPTION.create(e, e.getMessage());
        }
        finally {
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
