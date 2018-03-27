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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import com.openexchange.pgp.core.exceptions.PGPCoreExceptionCodes;

/**
 * {@link PGPDecrypter} - Wrapper for providing stream based PGP decryption
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v2.4.2
 */
public class PGPDecrypter {

    private final PGPKeyRetrievalStrategy keyRetrievalStrategy;
    private static final int BUFFERSIZE = 1024;

    /**
     * Initializes a new {@link PGPDecrypter}.
     *
     * @param keyRetrievalStrategy A strategy for retrieving public and private PGP keys in order to decrypt data and verify signatures
     */
    public PGPDecrypter(PGPKeyRetrievalStrategy keyRetrievalStrategy) {
        this.keyRetrievalStrategy = keyRetrievalStrategy;
    }

    /**
     * Initializes a new {@link PGPDecrypter}.
     *
     * @param secretKey The secret key used for decryption
     * @param publicKeyRetrievalStrategy A strategy to retrieve public keys only for signature verification.
     */
    public PGPDecrypter(final PGPSecretKey secretKey, final PGPKeyRetrievalStrategy publicKeyRetrievalStrategy) {
        this.keyRetrievalStrategy = new PGPKeyRetrievalStrategy() {

            @Override
            public PGPPrivateKey getSecretKey(long keyId, String userIdentity, char[] password) throws Exception {
                return PGPSecretKeyDecoder.decodePrivateKey(secretKey, password);
            }

            @Override
            public PGPPublicKey getPublicKey(long keyId) throws Exception {
                return publicKeyRetrievalStrategy.getPublicKey(keyId);
            }
        };
    }

    /**
     * Internal method to retrieve a private key
     *
     * @param encryptedData A encrypted data packet to receive the key for
     * @param userID the ID of the user to get a key for
     * @param password The user's password of the key
     * @return The decoded private key for the given packet and userID, or null, if no such key was found
     * @throws Exception
     */
    private PGPPrivateKey getPrivateKey(PGPPublicKeyEncryptedData encryptedData, String userID, char[] password) throws Exception {
        return this.keyRetrievalStrategy.getSecretKey(encryptedData.getKeyID(), userID,password);
    }

    /**
     * Internal method to retrieve a public key
     *
     * @param onePassSignature The signature packet to get the public key for
     * @return The public key for the given packet, or null if no such key was found
     * @throws Exception
     */
    private PGPPublicKey getPublicKey(PGPOnePassSignature onePassSignature) throws Exception {
        return this.keyRetrievalStrategy.getPublicKey(onePassSignature.getKeyID());
    }

    /**
     * Decrypts data
     *
     * @param input The input stream to read the data from
     * @param output The output stream to write the decoded data to
     * @param userID The PGP user identity of the user who want's to decode the data
     * @param password The password of the user's key which will be retrieved using the given strategy
     * @return A list of Signature verification results, or an empty list, if the encrypted data was not signed
     * @throws Exception
     */
    public List<PGPSignatureVerificationResult> decrypt(InputStream input, OutputStream output, String userID, char[] password) throws Exception {
        List<PGPSignatureVerificationResult> ret = new ArrayList<>();
        try (InputStream decoderStream = PGPUtil.getDecoderStream(input)) {
            PGPObjectFactory pgpObjectFactory = new PGPObjectFactory(decoderStream, new BcKeyFingerprintCalculator());

            //reading first part of the stream
            Object firstObject = pgpObjectFactory.nextObject();
            if (firstObject == null) {
                throw PGPCoreExceptionCodes.NO_PGP_DATA_FOUND.create();
            }

            PGPEncryptedDataList encryptedDataList;
            //the first object might be a PGP marker packet.
            if (firstObject instanceof PGPEncryptedDataList) {
                encryptedDataList = (PGPEncryptedDataList) firstObject;
            } else {
                encryptedDataList = (PGPEncryptedDataList) pgpObjectFactory.nextObject();
            }

            if(encryptedDataList == null) {
                //No encrypted data found (i.e if a signature was supplied)
                throw PGPCoreExceptionCodes.NO_PGP_DATA_FOUND.create();
            }

            //Processing decrypted data
            PGPPrivateKey privateKey = null;
            PGPPublicKeyEncryptedData encryptedData = null;
            Iterator<PGPPublicKeyEncryptedData> encryptedDataListIterator = encryptedDataList.getEncryptedDataObjects();
            while (encryptedDataListIterator.hasNext()) {
                encryptedData = encryptedDataListIterator.next();
                privateKey = getPrivateKey(encryptedData, userID, password);
                if (privateKey != null) {
                    break;
                }
            }

            if (privateKey == null) {
                throw PGPCoreExceptionCodes.PRIVATE_KEY_NOT_FOUND.create(userID + getMissingKeyIds(encryptedDataList));
            }

            InputStream clearDataStream = encryptedData.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(privateKey));

            //Processing pgp data
            PGPObjectFactory plainFact = new PGPObjectFactory(clearDataStream, new BcKeyFingerprintCalculator());
            Object pgpObject = plainFact.nextObject();
            PGPOnePassSignatureList onePassSignatureList = null;
            PGPOnePassSignature onePassSignature = null;
            PGPPublicKey singatureVerifyKey = null;
            boolean signatureInitialized = false;
            boolean signatureVerificationKeyFound = false;
            while (pgpObject != null) {

                //Handling compressed data
                if (pgpObject instanceof PGPCompressedData) {
                    PGPCompressedData compressedData = (PGPCompressedData) pgpObject;
                    plainFact = new PGPObjectFactory(compressedData.getDataStream(), new BcKeyFingerprintCalculator());
                    pgpObject = plainFact.nextObject();
                }

                //Processing the raw data
                else if (pgpObject instanceof PGPLiteralData) {
                    PGPLiteralData pgpLiteralData = (PGPLiteralData) pgpObject;
                    InputStream literalInputStream = pgpLiteralData.getInputStream();
                    byte[] buffer = new byte[BUFFERSIZE];
                    int len = 0;
                    while ((len = literalInputStream.read(buffer)) > -1) {
                        if (signatureInitialized) {
                            //If we have a signature we are going to update the OPS for verifying the signature later on
                            onePassSignature.update(buffer, 0, len);
                        }

                        //Writing the decrypted message to the output stream
                        output.write(buffer, 0, len);
                    }
                    try {
                        pgpObject = plainFact.nextObject();
                    } catch (IOException ex) {
                        // If next object is a signature, and we simply don't know the algorithm, don't fail
                        // Ignore signature for now
                        if (ex.getMessage() != null && ex.getMessage().contains("unknown signature key algorithm")) {
                            pgpObject = null;
                        } else {
                            throw ex;
                        }
                    }
                }

                //handling one pass signatures for initializing signature calculation
                else if (pgpObject instanceof PGPOnePassSignatureList) {
                    onePassSignatureList = (PGPOnePassSignatureList) pgpObject;
                    //By now we are only supporting one signature
                    onePassSignature = onePassSignatureList.get(0);
                    singatureVerifyKey = getPublicKey(onePassSignature);
                    if (singatureVerifyKey != null) {
                        onePassSignature.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), singatureVerifyKey);
                        signatureInitialized = true;
                        signatureVerificationKeyFound = true;
                    }
                    pgpObject = plainFact.nextObject();
                }

                //Handling signatures
                else if (pgpObject instanceof PGPSignatureList) {
                    PGPSignatureList signatureList = (PGPSignatureList) pgpObject;
                    if (signatureList.size() > 0) {
                        //By now we are only supporting one signature
                        PGPSignature signature = signatureList.get(0);
                        if (signatureInitialized) {
                            //Verify signatures
                            ret.add(new PGPSignatureVerificationResult(signature, onePassSignature.verify(signature)));
                        }
                        else if (!signatureVerificationKeyFound) {
                            //Key not found for verifying the signature; KeyRetrievalStrategy is responsible for logging this;
                            ret.add(new PGPSignatureVerificationResult(signature, false, true));
                        }
                    }
                    pgpObject = plainFact.nextObject();
                }
            }
        }
        output.flush();
        return ret;
    }

    /**
     * Get a list of keyIds from an EncryptedDataList
     * Padded with "("
     * @param encryptedDataList
     * @return formatted list of 8 digit hex key Ids
     */
    private String getMissingKeyIds (PGPEncryptedDataList encryptedDataList) {
        StringBuilder sb = new StringBuilder();
        Iterator<PGPPublicKeyEncryptedData> dataListIterator = encryptedDataList.getEncryptedDataObjects();
        PGPPublicKeyEncryptedData encryptedData = null;
        sb.append(" ( ");
        while (dataListIterator.hasNext()) {
            encryptedData = dataListIterator.next();
            String keyId = Long.toHexString(encryptedData.getKeyID()).substring(8).toUpperCase();
            if (!sb.toString().contains(keyId)) { // avoid repeats
                if (sb.length() > 8) sb.append(", "); // already more than one added
                sb.append("0x");
                sb.append(keyId);
            }
        }
        sb.append(" )");
        return (sb.toString());
    }
}
