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

package com.openexchange.mail.authenticity.mechanism.dkim;

import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;

/**
 * {@link DKIMResult} - The evaluation states of the DKIM signature.
 * The ordinal defines the significance of each result.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @see <a href="https://tools.ietf.org/html/rfc7601#section-2.7.1">RFC 7601, Section 2.7.1</a>
 */
public enum DKIMResult implements AuthenticityMechanismResult {

    /**
     * The message was signed, the signature or signatures were
     * acceptable to the ADMD, and the signature(s) passed verification
     * tests.
     */
    PASS("Pass", "pass"),
    /**
     * The message was signed, but the signature or signatures
     * contained syntax errors or were not otherwise able to be
     * processed. This result is also used for other failures not
     * covered elsewhere in this list.
     *
     */
    NEUTRAL("Neutral", "neutral"),
    /**
     * The message was signed, but some aspect of the signature or
     * signatures was not acceptable to the ADMD.
     */
    POLICY("Policy", "policy"),
    /**
     * The message was not signed.
     */
    NONE("None", "none"),
    /**
     * The message could not be verified due to some error that
     * is likely transient in nature, such as a temporary inability to
     * retrieve a public key. A later attempt may produce a final
     * result.
     */
    TEMPERROR("Temporary Error", "temperror"),
    /**
     * The message could not be verified due to some error that
     * is unrecoverable, such as a required header field being absent. A
     * later attempt is unlikely to produce a final result.
     */
    PERMFAIL("Permanent Failure", "permfail"),
    /**
     * The message was signed and the signature or signatures were
     * acceptable to the ADMD, but they failed the verification test(s).
     */
    FAIL("Fail", "fail"),
    ;

    private final String displayName;
    private final String technicalName;

    /**
     * Initialises a new {@link DKIMResult}.
     */
    private DKIMResult(String displayName, String technicalName) {
        this.displayName = displayName;
        this.technicalName = technicalName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getTechnicalName() {
        return technicalName;
    }

    @Override
    public int getCode() {
        return ordinal();
    }

    /**
     * Gets the DKIM result for given technical name.
     *
     * @param technicalName The technical name to look-up by
     * @return The associated DKIM result or <code>null</code>
     */
    public static DKIMResult dkimResultFor(String technicalName) {
        if (technicalName == null) {
            return null;
        }
        for (DKIMResult dkimResult : DKIMResult.values()) {
            if (dkimResult.technicalName.equals(technicalName)) {
                return dkimResult;
            }
        }
        return null;
    }

}
