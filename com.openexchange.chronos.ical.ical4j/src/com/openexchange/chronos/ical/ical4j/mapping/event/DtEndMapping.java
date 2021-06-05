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

package com.openexchange.chronos.ical.ical4j.mapping.event;

import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ical4j.mapping.ICalDateTimeMapping;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.util.Dates;

/**
 * {@link DtEndMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DtEndMapping extends ICalDateTimeMapping<VEvent, Event> {

    /**
     * Initializes a new {@link DtEndMapping}.
     */
	public DtEndMapping() {
		super(Property.DTEND);
	}

	@Override
    protected org.dmfs.rfc5545.DateTime getValue(Event object) {
		return object.getEndDate();
	}

	@Override
    protected void setValue(Event object, org.dmfs.rfc5545.DateTime value) {
		object.setEndDate(value);
	}

	@Override
	protected DateProperty createProperty() {
		return new DtEnd();
	}

    @Override
    protected DateProperty getProperty(VEvent component) {
        DtEnd dtEnd = (DtEnd) component.getProperty(Property.DTEND);
        if (null == dtEnd && null != component.getStartDate()) {
            /*
             * derive DTEND from DURATION or take over DTSTART
             * (similar to net.fortuna.ical4j.model.component.VEvent.getEndDate(boolean), but without bugs)
             */
            DtStart dtStart = component.getStartDate();
            Duration duration = component.getDuration();
            if (null == duration) {
                if (DateTime.class.isInstance(dtStart.getDate())) {
                    // If "DTSTART" is a DATE-TIME, then the event's duration is zero (see: RFC 5545, 3.6.1 Event Component)
                    //                    duration = new Duration(new Dur(0, 0, 0, 0));
                    return dtStart;
                } else {
                    // If "DTSTART" is a DATE, then the event's duration is one day (see: RFC 5545, 3.6.1 Event Component)
                    duration = new Duration(new Dur(1, 0, 0, 0));
                }
            }
            dtEnd = new DtEnd(Dates.getInstance(duration.getDuration().getTime(dtStart.getDate()), (Value) dtStart.getParameter(Parameter.VALUE)));
            if (dtStart.isUtc()) {
                dtEnd.setUtc(true);
            } else {
                dtEnd.setTimeZone(dtStart.getTimeZone());
            }
            return dtEnd;
        }
        return dtEnd;
    }

}
