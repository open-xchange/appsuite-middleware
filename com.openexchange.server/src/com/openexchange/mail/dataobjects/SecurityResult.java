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
