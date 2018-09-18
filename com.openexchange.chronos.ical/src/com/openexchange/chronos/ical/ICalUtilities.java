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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.data.FoldingWriter;

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
     * Exports one or more alarms to <code>VALARM</code> components and writes them to the supplied output stream.
     * <p/>
     * Note that the generated data will just contain <code>VALARM</code> components (<code>BEGIN:VALARM...END:VALARM</code>), i.e.
     * syntactically the <i>alarmc</i> elements as per
     * <a href="https://tools.ietf.org/html/rfc5545#section-3.6.6">RFC 5545, section 3.6.6</a>.
     *
     * @param writer The {@link FoldingWriter} to write to
     * @param alarms The alarms to export
     * @param parameters Further parameters for the iCalendar export, or <code>null</code> to stick with the defaults
     * @throws OXException If exporting fails
     */
    void exportAlarms(FoldingWriter writer, List<Alarm> alarms, ICalParameters parameters) throws OXException;

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
     * Exports one or more time zones to <code>VTIMEZONE</code> components and writes them to the supplied output stream.
     * <p/>
     * Note that the generated data will just contain <code>VTIMEZONE</code> components (<code>BEGIN:VTIMEZONE...END:VTIMEZONE</code>),
     * i.e. syntactically the <i>timezonec</i> elements as per
     * <a href="https://tools.ietf.org/html/rfc5545#section-3.6.5">RFC 5545, section 3.6.5</a>.
     *
     * @param writer The {@link FoldingWriter} to write to
     * @param timeZoneIDs The identifiers of the time zones to export
     * @param parameters Further parameters for the iCalendar export, or <code>null</code> to stick with the defaults
     * @throws OXException If exporting fails
     */
    void exportTimeZones(FoldingWriter writer, List<String> timeZoneIDs, ICalParameters parameters) throws OXException;

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
     * Exports one or more events to <code>VTEVENT</code> components and writes them to the supplied output stream.
     * <p/>
     * Note that the generated data will just contain <code>VEVENT</code> components (<code>BEGIN:VEVENT...END:VEVENT</code>),
     * i.e. syntactically the <i>event</i> elements as per
     * <a href="https://tools.ietf.org/html/rfc5545#section-3.6.1">RFC 5545, section 3.6.1</a>.
     *
     * @param writer The {@link FoldingWriter} to write to
     * @param events The events to export
     * @param parameters Further parameters for the iCalendar export, or <code>null</code> to stick with the defaults
     * @throws OXException If exporting fails
     */
    void exportEvent(FoldingWriter writer, List<Event> events, ICalParameters parameters) throws OXException;

    List<TimeZone> importTimeZones(InputStream inputStream, ICalParameters parameters) throws OXException;

}
