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

import java.util.List;

/**
 * {@link SecurityResult}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.8.4
 */
public abstract class SecurityResult {

    /**
     * Type of encryption for future options
     * {@link EncryptionType}
     *
     * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
     * @since v2.8.0
     */
    public static enum EncryptionType {
        PGP ("PGP"),
        SMIME ("SMIME");

        private final String name;

        private EncryptionType (String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    // ---------------------------------------------------------------------------------------------

    protected boolean decryptSuccess;  // If successfully decoded
    protected String error; // Any error messages
    protected List<SignatureResult> signatureResults;
    protected EncryptionType type;  // Type of encryption
    protected boolean pgpInline;

    /**
     * Initializes a new {@link SecurityResult}.
     */
    protected SecurityResult() {
        super();
    }

    /**
     * Return true if E-Mail action successful
     * @return
     */
    public boolean getSuccess () {
        return decryptSuccess;
    }

    /**
     * Return true if has error
     * @return
     */
    public boolean hasError () {
        return (error != null && !error.isEmpty());
    }

    /**
     * Get error message if any
     * @return
     */
    public String getError () {
        return error;
    }

    /**
     * Get list of signature results
     * @return
     */
    public List<SignatureResult> getSignatureResults() {
        return signatureResults;
    }

    /**
     * Return true if has signature results
     * @return
     */
    public boolean hasSignatureResults () {
        return (signatureResults != null && !signatureResults.isEmpty());
    }

    /**
     * Get type of encryption.  PGP vs SMIME
     * @return
     */
    public EncryptionType getType () {
        return type;
    }

    /**
     * Returns true if pgpInline
     * @return
     */
    public boolean isPgpInline() {
        return pgpInline;
    }


}
