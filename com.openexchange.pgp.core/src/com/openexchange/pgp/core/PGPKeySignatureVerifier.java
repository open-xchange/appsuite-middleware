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
                    @SuppressWarnings("unchecked") Iterator<String> userIds = publicKey.getUserIDs();
                    while (userIds.hasNext()) {
                        final String userId = userIds.next();
                        final Iterator<PGPSignature> signatures = publicKey.getSignaturesForID(userId);
                        if(signatures != null) {
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
                Iterator userAttributes = publicKey.getUserAttributes();
                while (userAttributes.hasNext()) {
                    PGPUserAttributeSubpacketVector userAttributeVector = (PGPUserAttributeSubpacketVector) userAttributes.next();
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
