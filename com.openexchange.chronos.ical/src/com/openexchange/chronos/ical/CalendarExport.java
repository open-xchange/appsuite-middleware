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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH.
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

    /**
     * Tracks the timezones of each event
     * 
     * @param event The {@link Event}
     * @return <code>true</code> if a timezone was added
     *         <code>false</code> if no timezone was added
     */
    boolean trackTimeZones(Event event);

}
