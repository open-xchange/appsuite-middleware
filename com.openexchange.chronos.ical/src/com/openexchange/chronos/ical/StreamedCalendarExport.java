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

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.FreeBusyData;
import com.openexchange.exception.OXException;

/**
 * {@link StreamedCalendarExport} - Exports an calendar by streaming an iCal file.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public interface StreamedCalendarExport extends Closeable {

    /**
     * Sets the method to be declared in the <code>VCALENDAR</code> component.
     *
     * @param method The method, or <code>null</code> to remove
     * 
     * @throws IOException In case writing fails
     * @throws OXException On other errors
     */
    void writeMethod(String method) throws IOException, OXException;

    /**
     * Sets the exported calendar's name using the <code>X-WR-CALNAME</code> property in the <code><code>VCALENDAR</code></code> component.
     *
     * @param name The calendar name, or <code>null</code> to remove
     * @throws IOException In case writing fails
     * @throws OXException On other errors
     */
    void writeCalendarName(String name) throws IOException, OXException;

    /**
     * Streams a chunk of {@link Event}s to the output stream
     * 
     * @param events The {@link List} of {@link Event}s to stream
     * @throws IOException In case writing fails
     * @throws OXException On other errors
     */
    void writeEvents(List<Event> events) throws IOException, OXException;

    /**
     * Streams a chunk of {@link FreeBusyData} to the output stream
     *
     * @param freeBusyData The {@link List} of {@link FreeBusyData} to write
     * @throws IOException In case writing fails
     * @throws OXException On other errors
     */
    void writeFreeBusy(List<FreeBusyData> freeBusyData) throws IOException, OXException;

    /**
     * Explicitly adds a timezone identifier to this <code>VCALENDAR</code>.
     *
     * @param timeZoneIDs The time zone identifiers to stream
     * @throws IOException In case writing fails
     * @throws OXException On other errors
     */
    void writeTimeZones(Set<String> timeZoneIDs) throws IOException, OXException;

    /**
     * Adds extended properties to the exported calendar.
     *
     * @param property The {@link List} of {@link ExtendedProperty} to stream
     * @throws IOException In case writing fails
     * @throws OXException On other errors
     */
    void writeProperties(List<ExtendedProperty> property) throws IOException, OXException;

    /**
     * Finish writing to the output stream
     * 
     * @throws IOException In case writing fails
     */
    void finish() throws IOException;

}
