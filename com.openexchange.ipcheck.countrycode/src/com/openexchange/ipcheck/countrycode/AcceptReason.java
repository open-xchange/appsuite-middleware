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

package com.openexchange.ipcheck.countrycode;

/**
 * {@link AcceptReason}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum AcceptReason {
    /**
     * One of the IP addresses lies with in the private range
     */
    PRIVATE_IPV4("One of the IP addresses lies with in the private range."),
    /**
     * One of the IP addresses is white-listed
     */
    WHITE_LISTED("One of the IP addresses is white-listed."),
    /**
     * The IP change was eligible
     */
    ELIGIBLE("Change was eligible");

    private final String message;

    /**
     * Initialises a new {@link AcceptReason}.
     */
    private AcceptReason(String message) {
        this.message = message;
    }

    /**
     * Gets the message
     *
     * @return The message
     */
    public String getMessage() {
        return message;
    }

}
