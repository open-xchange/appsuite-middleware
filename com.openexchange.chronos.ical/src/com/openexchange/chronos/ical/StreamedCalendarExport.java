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

package com.openexchange.chronos.ical;

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
public interface StreamedCalendarExport {

    /**
     * Sets the method to be declared in the <code>VCALENDAR</code> component.
     *
     * @param method The method, or <code>null</code> to remove
     */
    void streamMethod(String method);

    /**
     * Sets the exported calendar's name using the <code>X-WR-CALNAME</code> property in the <code><code>VCALENDAR</code></code> component.
     *
     * @param name The calendar name, or <code>null</code> to remove
     */
    void streamCalendarName(String name);

    /**
     * Streams a chunk of {@link Event}s to the output stream
     * 
     * @param events The {@link List} of {@link Event}s to stream
     */
    void streamEvents(List<Event> events);

    /**
     * Streams a chunk of {@link FreeBusyData} to the output stream
     *
     * @param freeBusyData The {@link List} of {@link FreeBusyData} to write
     */
    void streamFreeBusy(List<FreeBusyData> freeBusyData);

    /**
     * Explicitly adds a timezone identifier to this <code>VCALENDAR</code>.
     *
     * @param timeZoneIDs The time zone identifiers to stream
     */
    void streamTimeZones(Set<String> timeZoneIDs);

    /**
     * Adds extended properties to the exported calendar.
     *
     * @param property The {@link List} of {@link ExtendedProperty} to stream
     */
    void streamProperties(List<ExtendedProperty> property);

    /**
     * Finish writing to the output stream
     * 
     * @throws OXException In case writing fails
     */
    void finish() throws OXException;

    /**
     * Gets a list of conversion warnings.
     *
     * @return The warnings
     */
    List<OXException> getWarnings();

}
