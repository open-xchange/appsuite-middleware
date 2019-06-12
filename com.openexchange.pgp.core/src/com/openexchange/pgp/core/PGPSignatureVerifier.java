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

import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.l;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import com.openexchange.pgp.core.exceptions.PGPCoreExceptionCodes;

/**
 * {@link PGPSignatureVerifier} - Wrapper for verifying PGP signature
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class PGPSignatureVerifier {

    private static final int BUFFERSIZE = 256;
    private final PGPKeyRetrievalStrategy keyRetrievalStrategy;

    /**
     * Initializes a new {@link PGPSignatureVerifier}.
     *
     * @param keyRetrievalStrategy A strategy for retrieving public key in order to verify signatures
     */
    public PGPSignatureVerifier(PGPKeyRetrievalStrategy keyRetrievalStrategy) {
        this.keyRetrievalStrategy = keyRetrievalStrategy;
    }

    /**
     * Internal method to get a public key for a given signature
     *
     * @param signature The signature to retrieve the key for
     * @return The PGPPublicKey related to the given signature, or null if no such key was found
     * @throws Exception
     */
    private PGPPublicKey getPublicKey(PGPSignature signature) throws Exception {
        return this.keyRetrievalStrategy.getPublicKey(signature.getKeyID());
    }

    /**
     * Gets the ID of the master key for a given PGPPublicKey
     *
     * This method searches for sub-key-binding on the given public key and return the ID of the issuer key.
     *
     * @param publicKey The public key to get the master key ID from
     * @return The ID of the publicKey's master key, or null if the mater key could not be determined.
     */
    private static Long getPublicMasterKeyId(PGPPublicKey publicKey) {

        if(publicKey.isMasterKey()) {
            //The given key is the actual master key
            return L(publicKey.getKeyID());
        }

        //Get the subkey binding and return the ID of the master key
        @SuppressWarnings("rawtypes") Iterator subkeyBinding = publicKey.getSignaturesOfType(PGPSignature.SUBKEY_BINDING);
        if(subkeyBinding.hasNext()) {
            PGPSignature signature = (PGPSignature)subkeyBinding.next();
            return L(signature.getKeyID());
        }
        return null;
    }

    /**
     * Tries to obtain the master key for the given key
     *
     * @param key The key to obtain the master key from
     * @return The key if it is an master key, the master-key obtained from sub-key-binding signatures, or null if no binding found
     * @throws Exception
     */
    private PGPPublicKey getMasterKey(PGPPublicKey key) throws Exception {
        if(key.isMasterKey()) {
           return key;
        }
        Long masterKeyId = getPublicMasterKeyId(key);
        if(masterKeyId != null) {
            return this.keyRetrievalStrategy.getPublicKey(l(masterKeyId));
        }

        return null;
    }

    /**
     * Verifies signatures
     *
     * @param signedData The data which are signed
     * @param signatureData The data containing one or more signatures
     * @return A list of verification results
     * @throws Exception
     */
    public List<PGPSignatureVerificationResult> verifySignatures(InputStream signedData, InputStream signatureData) throws Exception {
        List<PGPSignatureVerificationResult> ret = new ArrayList<PGPSignatureVerificationResult>();
        signatureData = PGPUtil.getDecoderStream(signatureData);
        PGPObjectFactory objectFactory = new PGPObjectFactory(signatureData, new BcKeyFingerprintCalculator());
        Object pgpObject = objectFactory.nextObject();

        //Trying to get the signature list from the stream
        PGPSignatureList signatureList = pgpObject instanceof PGPSignatureList ? (PGPSignatureList) pgpObject : null;

        //If no plain signature list was found, we check if we have a compressed signature list
        if(signatureList == null && pgpObject instanceof PGPCompressedData) {
            PGPCompressedData compressedData = (PGPCompressedData)pgpObject;
            pgpObject = new PGPObjectFactory(compressedData.getDataStream(), new BcKeyFingerprintCalculator()).nextObject();
            //Check again if we now have a decomprssed signature list
            signatureList = pgpObject instanceof PGPSignatureList ? (PGPSignatureList) pgpObject : null;
        }

        Hashtable<PGPSignature,PGPPublicKey> keysForSignature = new Hashtable<PGPSignature, PGPPublicKey>();

        if (signatureList != null) {
            //Initializing each signature
            Iterator<PGPSignature> iterator = signatureList.iterator();
            while (iterator.hasNext()) {
                PGPSignature signature = iterator.next();
                PGPPublicKey publicKey = getPublicKey(signature);
                if(publicKey != null) {
                    signature.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), publicKey);
                    keysForSignature.put(signature, publicKey);
                }
            }

            //Reading data and updating each signature
            byte[] buffer = new byte[BUFFERSIZE];
            int len = 0;
            while ((len = signedData.read(buffer)) > -1) {
                iterator = signatureList.iterator();
                while (iterator.hasNext()) {
                    PGPSignature signature = iterator.next();
                    if(keysForSignature.containsKey(signature)) /* only update if initialized with found key */ {
                        signature.update(buffer, 0, len);
                    }
                }
            }

            //Verify each signature
            iterator = signatureList.iterator();
            while (iterator.hasNext()) {
                PGPSignature signature = iterator.next();
                if(keysForSignature.containsKey(signature)) {
                    PGPPublicKey issuerKey = keysForSignature.get(signature);
                    Iterator<String> userIds = null;
                    PGPPublicKey masterKey = getMasterKey(issuerKey);
                    if(masterKey != null) {
                        userIds = masterKey.getUserIDs();
                    }
                    PGPSignatureVerificationResult verificationResult = new PGPSignatureVerificationResult(signature, signature.verify());
                    verificationResult.setIssuerKey(issuerKey);
                    if (userIds != null) {
                        //Adding user-id so that it is possible for a caller to determine who created this signature
                        while(userIds.hasNext()) {
                            verificationResult.addIssuerUserId(userIds.next());
                        }
                    }
                    ret.add(verificationResult);
                }
                else {
                    //Key not found; KeyRetrievalStrategy is responsible for logging this;
                    ret.add(new PGPSignatureVerificationResult(signature, false));
                }
            }
            return ret;
        }
        else {
            throw PGPCoreExceptionCodes.NO_PGP_SIGNATURE_FOUND.create();
        }
    }
}
