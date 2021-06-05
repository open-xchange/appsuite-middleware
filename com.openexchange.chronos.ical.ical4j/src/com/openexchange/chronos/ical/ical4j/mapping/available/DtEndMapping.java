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

package com.openexchange.chronos.ical.ical4j.mapping.available;

import com.openexchange.chronos.ical.ical4j.mapping.ICalDateTimeMapping;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.Available;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.util.Dates;

/**
 * {@link DtEndMapping}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DtEndMapping extends ICalDateTimeMapping<Available, com.openexchange.chronos.Available> {

    /**
     * Initialises a new {@link DtEndMapping}.
     */
    public DtEndMapping() {
        super(Property.DTEND);
    }

    @Override
    protected org.dmfs.rfc5545.DateTime getValue(com.openexchange.chronos.Available object) {
        return object.getEndTime();
    }

    @Override
    protected void setValue(com.openexchange.chronos.Available object, org.dmfs.rfc5545.DateTime value) {
        object.setEndTime(value);
    }

    @Override
    protected DateProperty createProperty() {
        return new DtEnd();
    }

    @Override
    protected DateProperty getProperty(Available component) {
        DtEnd dtEnd = (DtEnd) component.getProperty(Property.DTEND);
        if (dtEnd != null) {
            return dtEnd;
        }
        DtStart dtStart = (DtStart) component.getProperty(Property.DTSTART);
        if (dtStart == null) {
            return new DtEnd(new Date(0));
        }

        Duration duration = (Duration) component.getProperty(Property.DURATION);
        if (null == duration) {
            // If "DTSTART" is not present, then the start time is unbounded. (see: RFC 7953, 3.1 VAvailability Component)
            duration = new Duration(new Dur(0, 0, 0, 0));
        }
        dtEnd = new DtEnd(Dates.getInstance(duration.getDuration().getTime(dtStart.getDate()), (Value) dtStart.getParameter(Parameter.VALUE)));
        if (dtStart.isUtc()) {
            dtEnd.setUtc(true);
        } else {
            dtEnd.setTimeZone(dtStart.getTimeZone());
        }
        return dtEnd;
    }
}
