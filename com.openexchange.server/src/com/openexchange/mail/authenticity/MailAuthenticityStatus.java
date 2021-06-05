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

package com.openexchange.mail.authenticity;

/**
 * {@link MailAuthenticityStatus} - Defines the different status types of the overall status
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum MailAuthenticityStatus {

    /**
     * Passed authentication status
     */
    PASS("Pass", "pass"),
    /**
     * Failed authentication status
     */
    FAIL("Fail", "fail"),
    /**
     * Suspicious content
     */
    SUSPICIOUS("Suspicious", "suspicious"),
    /**
     * The authentication result was analysed but either no status
     * could be determined, or temporary errors have occurred.
     */
    NEUTRAL("Neutral", "neutral"),
    /**
     * Nothing has been analysed (Used in case of an error
     * before the actual analysis (e.g. IMAP down) or for
     * e-mails before the defined cut-off-date)
     */
    NOT_ANALYZED("Not Analyzed", "not-analyzed"),
    /**
     * None. No mechanism result was defined in the authentication result.
     * Note that this is an internal status and is not meant to be used
     * for the clients. 
     */
    NONE("None", "none"),
    /**
     * Passed authentication status is also from a trusted mail address
     */
    TRUSTED("Trusted", "trusted");

    private final String displayName;
    private final String technicalName;

    /**
     * Initialises a new {@link MailAuthenticityStatus}.
     */
    private MailAuthenticityStatus(String displayName, String technicalName) {
        this.displayName = displayName;
        this.technicalName = technicalName;
    }

    /**
     * Gets the displayName
     *
     * @return The displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the technical name of the AuthenticationStatus
     *
     * @return the technical name of the AuthenticationStatus
     */
    public String getTechnicalName() {
        return technicalName;
    }
}
