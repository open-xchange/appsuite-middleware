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
 * {@link ParticipationStatus}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.2.12">RFC 5545, section 3.2.12</a>
 */
public class ParticipationStatus extends EnumeratedProperty {

    /**
     * Event needs action.
     */
    public static final ParticipationStatus NEEDS_ACTION = new ParticipationStatus("NEEDS-ACTION");

    /**
     * Event accepted.
     */
    public static final ParticipationStatus ACCEPTED = new ParticipationStatus("ACCEPTED");

    /**
     * Event declined.
     */
    public static final ParticipationStatus DECLINED = new ParticipationStatus("DECLINED");

    /**
     * Event tentatively accepted.
     */
    public static final ParticipationStatus TENTATIVE = new ParticipationStatus("TENTATIVE");

    /**
     * Event delegated.
     */
    public static final ParticipationStatus DELEGATED = new ParticipationStatus("DELEGATED");

    /**
     * Initializes a new {@link ParticipationStatus}.
     *
     * @param value The property value
     */
    public ParticipationStatus(String value) {
        super(value);
    }

    @Override
    public String getDefaultValue() {
        return NEEDS_ACTION.getValue();
    }

    @Override
    protected String[] getStandardValues() {
        return getValues(NEEDS_ACTION, ACCEPTED, DECLINED, TENTATIVE, DELEGATED);
    }

}
