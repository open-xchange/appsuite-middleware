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

package com.openexchange.chronos;

import com.openexchange.java.EnumeratedProperty;

/**
 * {@link EventStatus}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.8.1.11">RFC 5545, section 3.8.1.11</a>
 */
public class EventStatus extends EnumeratedProperty {

    /**
     * Indicates event is tentative.
     */
    public static final EventStatus TENTATIVE = new EventStatus("TENTATIVE");

    /**
     * Indicates event is definite.
     */
    public static final EventStatus CONFIRMED = new EventStatus("CONFIRMED");

    /**
     * Indicates event was cancelled.
     */
    public static final EventStatus CANCELLED = new EventStatus("CANCELLED");

    /**
     * Initializes a new {@link EventStatus}.
     *
     * @param value The action value
     */
    public EventStatus(String value) {
        super(value);
    }

    @Override
    protected String[] getStandardValues() {
        return getValues(TENTATIVE, CONFIRMED, CANCELLED);
    }

}
