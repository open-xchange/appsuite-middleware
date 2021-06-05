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

package com.openexchange.mail.dataobjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a signature verification
 * {@link SignatureResult}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v2.8.0
 */
public class SignatureResult {

    private boolean verified;  // Signature is verified
    private boolean missing;   // Missing key/info to verify
    private long date;         // Tick representation date
    private String error;      // Error in verifying signature
    private long issuerKeyId;  // The ID of the issuer key
    private String issuerKeyFingerprint; // The fingerprint of the issuer key
    private List<String> issuerUserIds; //The PGP userIds of the issuer

    /**
     * Default constructor
     * Initializes a new {@link SignatureResult}.
     */
    public SignatureResult () {
        verified = false;
        missing = false;
        date = 0l;
        error = null;
        issuerUserIds = new ArrayList<String>();
    }

    /**
     * Set if the signature was verified or failed verification
     * True if verified
     * @param verified
     */
    public void setVerified (boolean verified) {
        this.verified = verified;
    }

    /**
     * Set if the signature was missing data for verification
     * @param missing
     */
    public void setMissing (boolean missing) {
        this.missing = missing;
    }

    /**
     * Long representation of the date
     * @param date
     */
    public void setDate (long date) {
        this.date = date;
    }

    /**
     * Return true if the signature was successfully verified
     * @return
     */
    public boolean isVerified () {
        return verified;
    }

    /**
     * Return true if information was missing for verification
     * @return
     */
    public boolean isMissing () {
        return missing;
    }

    /**
     * Return long representation of the date
     * @return
     */
    public long getDate () {
        return date;
    }

    /**
     * Sets an error message
     * @param error
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Retrieves any error message
     * @return error
     */
    public String getError() {
        return error;
    }

    /**
     * Gets the ID of the issuer key
     *
     * @return The ID of the issuer key
     */
    public long getIssuerKeyId() {
        return this.issuerKeyId;
    }

    /**
     * Sets the ID of the issuer key
     *
     * @param keyId The ID of the issuer key
     */
    public void setIssuerKeyId(long keyId) {
        this.issuerKeyId = keyId;
    }

    /**
     * Gets the fingerprint of the issuer key
     *
     * @return The Fingerprint of the issuer key, or null if unknown
     */
    public String getIssuerKeyFingerprint() {
        return this.issuerKeyFingerprint;
    }

    /**
     * Sets the fingerprint of the issuer key
     *
     * @param fingerprint The fingerprinta of the issuer key
     */
    public void setIssuerKeyFingerprint(String fingerprint) {
        this.issuerKeyFingerprint = fingerprint;
    }

    /**
     * Gets the PGP user IDs related to the issuer key
     *
     * @return a list of user IDs related to the issuer key
     */
    public List<String> getIssuerUserIds(){
       return this.issuerUserIds;
    }

    /**
     * Sets the PGP user IDs related to the issuer key
     *
     * @param issuerUserIds The IDs related to the issuer key
     */
    public void setIssuerUserIds(List<String> issuerUserIds) {
       this.issuerUserIds = issuerUserIds;
    }
}

