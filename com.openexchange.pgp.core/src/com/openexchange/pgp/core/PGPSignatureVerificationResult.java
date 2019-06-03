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
import java.util.List;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPUserAttributeSubpacketVector;

/**
 * {@link PGPSignatureVerificationResult}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class PGPSignatureVerificationResult {

    private final PGPSignature              signature;
    private boolean                         verified;
    private final boolean                   missing;
    private String                          userId;
    private String                          error;
    private PGPPublicKey                    publicKey;
    private PGPUserAttributeSubpacketVector userAttributes;
    private PGPPublicKey                    issuerKey;
    private MDCVerificationResult           mdcVerificationResult;
    private final List<String>              issuerUserIds = new ArrayList<String>();

    /**
     * Initializes a new {@link PGPSignatureVerificationResult}.
     *
     * @param signature The signature
     * @param verified Whether the signature has been verified or not
     */
    public PGPSignatureVerificationResult(PGPSignature signature, boolean verified) {
        this.signature = signature;
        this.verified = verified;
        this.missing = false;
    }

    /**
     * Initializes a new {@link PGPSignatureVerificationResult}.
     *
     * @param signature The signature
     * @param verified Whether the signature has been verified or not
     * @param missing Whether the key for verification was missing or not
     */
    public PGPSignatureVerificationResult(PGPSignature signature, boolean verified, boolean missing) {
        this.signature = signature;
        this.verified = verified;
        this.missing = missing;
    }

    public PGPSignatureVerificationResult(MDCVerificationResult mdcVerificationResult) {
        this.signature = null;
        this.verified = false;
        this.missing = false;
        this.mdcVerificationResult = mdcVerificationResult;
    }

    /**
     * Gets the signature
     *
     * @return The signature
     */
    public PGPSignature getSignature() {
        return signature;
    }

    /**
     * Gets error if any
     *
     * @return  Error message if any
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the error message
     *
     * @param error The message
     * @return this
     */
    public PGPSignatureVerificationResult setError(String error) {
        this.error = error;
        return this;
    }

    /**
     * Gets the verification result
     *
     * @return Whether the signature has been verified or not
     */
    public boolean isVerified() {
        return verified;
    }

    /**
     * Sets the verification result
     *
     * @param verified The result to set
     * @return this
     */
    public PGPSignatureVerificationResult setVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    public boolean isMissing() {
        return missing;
    }

    /**
     * Sets the PGP user id related to the signature verification, or null if this signature is not related to a user-id
     *
     * @param userId The user ID related to the signature verification
     * @return this, for a fluent like style.
     */
    public PGPSignatureVerificationResult setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Get the PGP user id related to the signature verification
     *
     * @return The PGP user id related to the signature verification, or null if this signature is not related to a user-id
     */
    public String getUserId() {
        return this.userId;
    }


    /**
     * Gets the public key for which the signature was made, or null if the signature is not related to a public key.
     * @return The public key, or null if the signature is not related to a public key
     */
    public PGPPublicKey getPublicKey() {
        return this.publicKey ;
    }

    /**
     * Sets the public key related to the signature, or null if the signature is not related to a public key.
     * @param publicKey The public key related to the signature, or null if the signature is not related to a public key.
     * @return this, for a fluent like style
     */
    public PGPSignatureVerificationResult setPublicKey(PGPPublicKey publicKey) {
       this.publicKey = publicKey;
       return this;
    }

    /**
     * Gets the user attributes related to the signature, or null if the signature is not related to any user attributes
     *
     * @return The user attributes related for the signature, or null if the signature is not related to any user attributes.
     */
    public PGPUserAttributeSubpacketVector getUserAttributes() {
        return this.userAttributes;
    }

    /**
     * Sets the user attributes related to the signature, or null if the signature is not related to any user attributes.
     * @param userAttributes The user attributes related to the signature.
     * @return this, for a fluent like style
     */
    public PGPSignatureVerificationResult setUserAttributes(PGPUserAttributeSubpacketVector userAttributes) {
       this.userAttributes = userAttributes;
       return this;
    }

    /**
     * Returns the public key of the issuer if known, null otherwise
     *
     * @return The public key of the issuer, or null if unknown
     */
    public PGPPublicKey getIssuerKey() {
        return this.issuerKey;
    }

    /**
     * Sets the public key of the issuer, or null if the issuer's key is not known
     *
     * @param issuerKey The public key of the issuer, or null if the public key is unknown.
     * @return this
     */
    public PGPSignatureVerificationResult setIssuerKey(PGPPublicKey issuerKey) {
        this.issuerKey = issuerKey;
        return this;
    }

    /**
     * Returns the user IDs of the issuer if known, an empty list otherwise
     *
     * @return The user IDs of the signature's issuer, or an empty list if unknown
     */
    public List<String> getIssuerUserIds() {
        return this.issuerUserIds;
    }

    /**
     * Adds a user ID of the issuer
     *
     * @param  issuerUserId The ID of the issuer to add
     * @return this
     */
    public PGPSignatureVerificationResult addIssuerUserId(String issuerUserId) {
        this.issuerUserIds.add(issuerUserId);
        return this;
    }

    public MDCVerificationResult getMDCVerifiacionResult() {
        return this.mdcVerificationResult;
    }
}
