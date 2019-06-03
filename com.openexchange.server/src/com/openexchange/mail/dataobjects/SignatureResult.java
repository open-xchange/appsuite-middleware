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

