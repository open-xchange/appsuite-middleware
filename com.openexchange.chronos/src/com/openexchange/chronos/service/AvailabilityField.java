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

package com.openexchange.chronos.service;

import java.util.EnumSet;

/**
 * {@link AvailabilityField}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum AvailabilityField implements CalendarAvailabilityField {
    
    id(true, false),

    // the following are REQUIRED but MUST NOT occur more than once
    dtstamp(true, false),
    uid(true, false),
    user(true, false),

    // the following are OPTIONAL but MUST NOT occur more than once
    busytype(false, false),
    classification(false, false),
    created(false, false),
    createdBy(false, false),
    description(false, false),
    dtstart(false, false),
    lastModified(false, false),
    location(false, false),
    organizer(false, false),
    priority(false, false),
    seq(false, false),
    summary(false, false),
    url(false, false),
    dtend(false, false),
    duration(false, false),

    // the following are OPTIONAL and MAY occur more than once
    categories(false, true),
    comment(false, true),
    contact(false, true),
    extendedProperties(false, true),
    ianaProperties(false, true),;

    private final boolean mandatory;

    private final boolean multiOccurrent;

    /**
     * Initialises a new {@link AvailabilityField}.
     * 
     * @param mandatory whether the field is mandatory
     */
    private AvailabilityField(boolean mandatory, boolean multiOccurrent) {
        this.mandatory = mandatory;
        this.multiOccurrent = multiOccurrent;
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    @Override
    public boolean isMultiOccurrent() {
        return multiOccurrent;
    }

    /**
     * Returns the mandatory fields
     * 
     * @return The mandatory fields
     */
    public static EnumSet<AvailabilityField> getMandatoryFields() {
        return EnumSet.of(dtstart, uid);
    }

}
