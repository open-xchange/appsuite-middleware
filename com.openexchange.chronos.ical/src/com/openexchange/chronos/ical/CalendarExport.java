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
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.FreeBusyData;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarExport}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarExport {

    /**
     * Adds a new event component to this <code>VCALENDAR</code> component.
     * <p/>
     * For slipstreaming arbitrary iCal parameters to the exported component, an {@link EventComponent} can be passed.
     *
     * @param event The event component to add
     * @return A self reference
     */
    CalendarExport add(Event event) throws OXException;

    /**
     * Adds a new free/busy data component to this <code>VCALENDAR</code> component.
     *
     * @param freeBusyData The free/busy component to add
     * @return A self reference
     */
    CalendarExport add(FreeBusyData freeBusyData) throws OXException;

    /**
     * Explicitly adds a timezone identifier to this <code>VCALENDAR</code> component.
     *
     * @param timeZoneID The time zone identifier to add
     * @return A self reference
     */
    CalendarExport add(String timeZoneID);

    /**
     * Adds an extended property to the exported calendar component.
     *
     * @param property The extended property to add
     * @return A self reference
     */
    CalendarExport add(ExtendedProperty property);

    /**
     * Adds the availability componts to the exported calendar.
     * 
     * @param calendarAvailability The Calendar availability component to add
     * @return A self reference
     */
    CalendarExport add(Availability calendarAvailability) throws OXException;

    /**
     * Sets the method to be declared in the <code>VCALENDAR</code> component.
     *
     * @param method The method, or <code>null</code> to remove
     */
    void setMethod(String method);

    /**
     * Sets the exported calendar's name using the <code>X-WR-CALNAME</code> property in the <code><code>VCALENDAR</code></code> component.
     *
     * @param name The calendar name, or <code>null</code> to remove
     */
    void setName(String name);

    /**
     * Gets a list of conversion warnings.
     *
     * @return The warnings
     */
    List<OXException> getWarnings();

    /**
     * Writes the <code>VCALENDAR</code> to the supplied output stream.
     *
     * @param outputStream The output stream to write to
     */
    void writeVCalendar(OutputStream outputStream) throws OXException;

    /**
     * Gets a file holder storing the exported <code>VCALENDAR</code>.
     *
     * @return The exported <code>VCALENDAR</code>, or <code>null</code> if not available
     * @throws OXException
     */
    IFileHolder getVCalendar() throws OXException;

    /**
     * Gets the input stream carrying the <code>VCALENDAR</code> contents.
     * <p>
     * Closing the stream will also {@link #close() close} this {@link CalendarExport} instance.
     *
     * @return The input stream
     */
    InputStream getClosingStream() throws OXException;

    /**
     * Gets the exported <code>VCALENDAR</code> as byte array.
     *
     * @return The <code>VCALENDAR</code> bytes
     */
    byte[] toByteArray() throws OXException;

}
