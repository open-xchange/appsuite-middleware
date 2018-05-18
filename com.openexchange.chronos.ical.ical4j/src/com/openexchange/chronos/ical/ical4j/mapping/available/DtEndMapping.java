/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.ical.ical4j.mapping.ICalDateTimeMapping#getValue(java.lang.Object)
     */
    @Override
    protected org.dmfs.rfc5545.DateTime getValue(com.openexchange.chronos.Available object) {
        return object.getEndTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.ical.ical4j.mapping.ICalDateTimeMapping#setValue(java.lang.Object, org.dmfs.rfc5545.DateTime)
     */
    @Override
    protected void setValue(com.openexchange.chronos.Available object, org.dmfs.rfc5545.DateTime value) {
        object.setEndTime(value);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.ical.ical4j.mapping.ICalDateTimeMapping#createProperty()
     */
    @Override
    protected DateProperty createProperty() {
        return new DtEnd();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.ical.ical4j.mapping.ICalDateTimeMapping#getProperty(net.fortuna.ical4j.model.Component)
     */
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
