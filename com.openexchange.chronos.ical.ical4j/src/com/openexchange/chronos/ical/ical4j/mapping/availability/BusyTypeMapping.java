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

package com.openexchange.chronos.ical.ical4j.mapping.availability;

import com.openexchange.chronos.BusyType;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.ical.ical4j.mapping.ICalTextMapping;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAvailability;

/**
 * {@link BusyTypeMapping}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BusyTypeMapping extends ICalTextMapping<VAvailability, Availability> {

    /**
     * Initialises a new {@link BusyTypeMapping}.
     */
    public BusyTypeMapping() {
        super(Property.BUSYTYPE);
    }

    @Override
    protected String getValue(Availability object) {
        BusyType value = object.getBusyType();
        return value != null ? value.getValue() : null;
    }

    @Override
    protected void setValue(Availability object, String value) {
        BusyType busyType = null;
        if (value != null) {
            try {
                busyType = BusyType.valueOf(value);
            } catch (IllegalArgumentException e) {
                // Should never happen; if it does, fall back to 'BUSY-UNAVAILABLE'
                busyType = BusyType.BUSY_UNAVAILABLE;
            }
        }
        object.setBusyType(busyType);
    }

    @Override
    protected Property createProperty() {
        return new net.fortuna.ical4j.model.property.BusyType();
    }
}
