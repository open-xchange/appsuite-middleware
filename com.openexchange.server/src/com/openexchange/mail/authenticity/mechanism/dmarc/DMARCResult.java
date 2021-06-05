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

package com.openexchange.mail.authenticity.mechanism.dmarc;

import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;

/**
 * {@link DMARCResult} - Defines the possible results as defined in
 * <a href="https://tools.ietf.org/html/rfc7489#section-11.2">RFC-7489, Section 11.2</a>.
 * The ordinal defines the significance of each result.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @see <a href="https://tools.ietf.org/html/rfc7489#section-11.2">RFC-7489, Section 11.2</a>
 */
public enum DMARCResult implements AuthenticityMechanismResult {
    /**
     * A DMARC policy record was published for the aligned
     * identifier, and at least one of the authentication mechanisms
     * passed.
     */
    PASS("Pass", "pass"),
    /**
     * No DMARC policy record was published for the aligned
     * identifier, or no aligned identifier could be extracted.
     */
    NONE("None", "none"),
    /**
     * A temporary error occurred during DMARC evaluation. A
     * later attempt might produce a final result.
     */
    TEMPERROR("Temporary Error", "temperror"),
    /**
     * A permanent error occurred during DMARC evaluation, such as
     * encountering a syntactically incorrect DMARC record. A later
     * attempt is unlikely to produce a final result.
     */
    PERMERROR("Permanent Error", "permerror"),
    /**
     * A DMARC policy record was published for the aligned
     * identifier, and none of the authentication mechanisms passed.
     */
    FAIL("Fail", "fail");

    private final String displayName;
    private final String technicalName;

    /**
     * Initialises a new {@link DMARCResult}.
     */
    private DMARCResult(String displayName, String technicalName) {
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
     * Gets the DMARC result for given technical name.
     *
     * @param technicalName The technical name to look-up by
     * @return The associated DMARC result or <code>null</code>
     */
    public static DMARCResult dmarcResultFor(String technicalName) {
        if (technicalName != null) {
            for (DMARCResult dmarcResult : DMARCResult.values()) {
                if (dmarcResult.technicalName.equals(technicalName)) {
                    return dmarcResult;
                }
            }
        }
        return null;
    }

}
