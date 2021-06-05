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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPUserAttributeSubpacketVector;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;

/**
 * {@link PGPKeySignatureVerifier} - Wrapper for verifying PGP key signatures.
 *
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc4880#section-5.2.1">RFC 4880 - OpenPGP Message Format - 5.2.1. Signature Types</a>
 */
public class PGPKeySignatureVerifier {

    private final PGPKeyRetrievalStrategy       keyRetrievalStrategy;
    private static final List<SignatureHandler> HANDLER_LIST;

    static {
        HANDLER_LIST = new ArrayList<>();

        //-- KEY_REVOCATION --
        HANDLER_LIST.add(new SignatureHandler() {
            @Override
            public PGPSignatureVerificationResult verify(PGPSignature signature, PGPPublicKey publicKey, PGPPublicKey verificationKey) throws PGPException {
                if (signature.getSignatureType() == PGPSignature.KEY_REVOCATION) {
                    signature.init(new JcaPGPContentVerifierBuilderProvider(), verificationKey);
                    return new PGPSignatureVerificationResult(signature, signature.verifyCertification(publicKey))
                        .setPublicKey(publicKey)
                        .setIssuerKey(verificationKey);
                }
                return null;
            }
        });

        //-- USER IDs --
        HANDLER_LIST.add(new SignatureHandler() {
            @Override
            public PGPSignatureVerificationResult verify(PGPSignature signature, PGPPublicKey publicKey, PGPPublicKey verificationKey) throws PGPException {
                if (signature.isCertification()) {
                    Iterator<String> userIds = publicKey.getUserIDs();
                    while (userIds.hasNext()) {
                        final String userId = userIds.next();
                        final Iterator<PGPSignature> signatures = publicKey.getSignaturesForID(userId);
                        if (signatures != null) {
                            final ArrayList<PGPSignature> list = new ArrayList<>();
                            signatures.forEachRemaining(list::add);
                            //Check if the signature is for the current user-id
                            final boolean isForUserId = list.stream().anyMatch(s -> s.getCreationTime().equals(signature.getCreationTime()));
                            if (isForUserId) {
                                signature.init(new JcaPGPContentVerifierBuilderProvider(), verificationKey);
                                return new PGPSignatureVerificationResult(signature, signature.verifyCertification(userId, publicKey))
                                    .setUserId(userId)
                                    .setPublicKey(publicKey)
                                    .setIssuerKey(verificationKey);
                            }
                        }
                    }
                }
                return null;
            }
        });

        // -- User attributes --
        HANDLER_LIST.add(new SignatureHandler() {
            @Override
            public PGPSignatureVerificationResult verify(PGPSignature signature, PGPPublicKey publicKey, PGPPublicKey verificationKey) throws PGPException {
                Iterator<PGPUserAttributeSubpacketVector> userAttributes = publicKey.getUserAttributes();
                while (userAttributes.hasNext()) {
                    PGPUserAttributeSubpacketVector userAttributeVector = userAttributes.next();
                    signature.init(new JcaPGPContentVerifierBuilderProvider(), verificationKey);
                    boolean verified = signature.verifyCertification(userAttributeVector, publicKey);
                    if (verified) {
                        return new PGPSignatureVerificationResult(signature, verified)
                            .setPublicKey(publicKey)
                            .setUserAttributes(userAttributeVector)
                            .setIssuerKey(verificationKey);
                    }
                }
                return null;
            }
        });

        //-- ANY OTHER SIGNATURES (INCL. SUB_KEY_REVOCATION)
        HANDLER_LIST.add(new SignatureHandler() {
            @Override
            public PGPSignatureVerificationResult verify(PGPSignature signature, PGPPublicKey publicKey, PGPPublicKey verificationKey) throws PGPException {
                if (signature.getSignatureType() != PGPSignature.KEY_REVOCATION && !signature.isCertification()) {
                    signature.init(new JcaPGPContentVerifierBuilderProvider(), verificationKey);
                    return new PGPSignatureVerificationResult(signature, signature.verifyCertification(verificationKey, publicKey))
                        .setPublicKey(publicKey)
                        .setIssuerKey(verificationKey);
                }
                return null;
            }
        });
    }

    private interface SignatureHandler {

        PGPSignatureVerificationResult verify(PGPSignature signature, PGPPublicKey publicKey, PGPPublicKey verificationKey) throws PGPException;
    }

    /**
     * Initializes a new {@link PGPKeySignatureVerifier}.
     *
     * @param keyRetrievalStrategy A strategy for retrieving public key in order to verify signatures
     */
    public PGPKeySignatureVerifier(PGPKeyRetrievalStrategy keyRetrievalStrategy) {
        this.keyRetrievalStrategy = keyRetrievalStrategy;
    }

    /**
     * Internal method to retrieve the public key for a given signature
     *
     * @param signature The signature
     * @return The public key which is suitable to verify the given signature, or null if no such key was found
     * @throws Exception
     */
    private PGPPublicKey getPublicKey(PGPSignature signature) throws Exception {
        return this.keyRetrievalStrategy.getPublicKey(signature.getKeyID());
    }

    /**
     * Verifies the given signature.
     *
     * @param signature The signature to verify
     * @param publicKey The key containing the given signature.
     * @return The verification result, or null if the given signature type is unknown and not able to be verified.
     * @throws Exception
     */
    public PGPSignatureVerificationResult verifySignatures(PGPSignature signature, PGPPublicKey publicKey) throws Exception {
        signature = Objects.requireNonNull(signature, "signature must not be null");
        publicKey = Objects.requireNonNull(publicKey, "publicKey must not be null");
        if (!HANDLER_LIST.isEmpty()) {
            PGPPublicKey verificationKey = getPublicKey(signature);
            if (verificationKey == null) {
                //We do not know the public key suitable to verify this signature
                final boolean verified = false;
                final boolean keyMissing = true;
                return new PGPSignatureVerificationResult(signature, verified, keyMissing);
            } else {
                //Passing the signature to all known handlers
                for (SignatureHandler handler : HANDLER_LIST) {
                    PGPSignatureVerificationResult verificationResult = handler.verify(signature, publicKey, verificationKey);
                    if (verificationResult != null) {
                        //A handler was able to verify the signature
                        return verificationResult;
                    }
                }
            }
        }
        return null;
    }
}
