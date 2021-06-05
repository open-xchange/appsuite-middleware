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

package com.openexchange.chronos.ical;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.FreeBusyData;
import com.openexchange.exception.OXException;

/**
 * {@link ICalUtilities}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface ICalUtilities {

    /**
     * Reads out the value of the first property with a specific name found in the supplied input stream.
     *
     * @param inputStream The input stream to read the property from
     * @param propertyName The name of the property
     * @param parameters Further iCal parameters, or <code>null</code> to stick with the defaults
     * @return The value, or <code>null</code> if not found
     * @throws OXException If reading fails
     */
    String parsePropertyValue(InputStream inputStream, String propertyName, ICalParameters parameters) throws OXException;

    /**
     * Imports one or more alarm components from the supplied input stream.
     * <p/>
     * Note that the data is expected to just contain <code>VALARM</code> components (<code>BEGIN:VALARM...END:VALARM</code>), i.e. syntactically the <i>alarmc</i> elements as
     * per <a href="https://tools.ietf.org/html/rfc5545#section-3.6.6">RFC 5545, section 3.6.6</a>.
     *
     * @param inputStream The input stream carrying <code>VALARM</code> components
     * @param parameters Further parameters for the iCalendar import, or <code>null</code> to stick with the defaults
     * @return The imported alarms, or <code>null</code> if none were found
     * @throws OXException If importing fails
     */
    List<Alarm> importAlarms(InputStream inputStream, ICalParameters parameters) throws OXException;

    /**
     * Exports one or more alarms to <code>VALARM</code> components and writes them to the supplied output stream.
     * <p/>
     * Note that the generated data will just contain <code>VALARM</code> components (<code>BEGIN:VALARM...END:VALARM</code>), i.e.
     * syntactically the <i>alarmc</i> elements as per
     * <a href="https://tools.ietf.org/html/rfc5545#section-3.6.6">RFC 5545, section 3.6.6</a>.
     *
     * @param outputStream The output stream to write to
     * @param alarms The alarms to export
     * @param parameters Further parameters for the iCalendar export, or <code>null</code> to stick with the defaults
     * @throws OXException If exporting fails
     */
    void exportAlarms(OutputStream outputStream, List<Alarm> alarms, ICalParameters parameters) throws OXException;

    /**
     * Exports one or more time zones to <code>VTIMEZONE</code> components and writes them to the supplied output stream.
     * <p/>
     * Note that the generated data will just contain <code>VTIMEZONE</code> components (<code>BEGIN:VTIMEZONE...END:VTIMEZONE</code>),
     * i.e. syntactically the <i>timezonec</i> elements as per
     * <a href="https://tools.ietf.org/html/rfc5545#section-3.6.5">RFC 5545, section 3.6.5</a>.
     *
     * @param outputStream The output stream to write to
     * @param timeZoneIDs The identifiers of the time zones to export
     * @param parameters Further parameters for the iCalendar export, or <code>null</code> to stick with the defaults
     * @throws OXException If exporting fails
     */
    void exportTimeZones(OutputStream outputStream, List<String> timeZoneIDs, ICalParameters parameters) throws OXException;

    /**
     * Exports one or more events to <code>VTEVENT</code> components and writes them to the supplied output stream.
     * <p/>
     * Note that the generated data will just contain <code>VEVENT</code> components (<code>BEGIN:VEVENT...END:VEVENT</code>),
     * i.e. syntactically the <i>event</i> elements as per
     * <a href="https://tools.ietf.org/html/rfc5545#section-3.6.1">RFC 5545, section 3.6.1</a>.
     *
     * @param outputStream The output stream to write to
     * @param events The events to export
     * @param parameters Further parameters for the iCalendar export, or <code>null</code> to stick with the defaults
     * @throws OXException If exporting fails
     */
    void exportEvent(OutputStream outputStream, List<Event> events, ICalParameters parameters) throws OXException;

    /**
     * Exports one or more free/busy data to <code>VFREEBUSY</code> components and writes them to the supplied output stream.
     * <p/>
     * Note that the generated data will just contain <code>VFREEBUSY</code> components (<code>BEGIN:VFREEBUSY...END:VFREEBUSY</code>),
     * i.e. syntactically the <i>freebusy</i> elements as per
     * <a href="https://tools.ietf.org/html/rfc5545#section-3.6.4">RFC 5545, section 3.6.4</a>.
     *
     * @param outputStream The output stream to write to
     * @param freeBusyData The {@link FreeBusyData}
     * @param parameters Further parameters for the iCalendar export, or <code>null</code> to stick with the defaults
     * @throws OXException If exporting fails
     */
    void exportFreeBusy(OutputStream outputStream, List<FreeBusyData> freeBusyData, ICalParameters parameters) throws OXException;

    /**
     * Exports one or more availability to <code>VAVAILABILITY</code> components and writes them to the supplied output stream.
     * <p/>
     * Note that the generated data will just contain <code>VAVAILABILITY</code> components (<code>BEGIN:VAVAILABILITY...END:VAVAILABILITY</code>),
     * i.e. syntactically the <i>availability</i> elements as per
     * <a href="https://tools.ietf.org/html/rfc7953#section-3.1">RFC 7953, section 3.1</a>.
     *
     * @param outputStream The output stream to write to
     * @param availibilities The {@link Availability}
     * @param parameters Further parameters for the iCalendar export, or <code>null</code> to stick with the defaults
     * @throws OXException If exporting fails
     */
    void exportAvailability(OutputStream outputStream, List<Availability> availibilities, ICalParameters parameters) throws OXException;

    List<TimeZone> importTimeZones(InputStream inputStream, ICalParameters parameters) throws OXException;

}
