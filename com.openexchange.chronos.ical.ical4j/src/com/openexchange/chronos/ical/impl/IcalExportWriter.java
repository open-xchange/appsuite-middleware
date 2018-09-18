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

package com.openexchange.chronos.ical.impl;

import static net.fortuna.ical4j.model.Calendar.BEGIN;
import static net.fortuna.ical4j.model.Calendar.END;
import static net.fortuna.ical4j.model.Calendar.VCALENDAR;
import static net.fortuna.ical4j.util.Strings.LINE_SEPARATOR;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ICalExceptionCodes;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalUtilities;
import com.openexchange.chronos.ical.ical4j.VCalendar;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import net.fortuna.ical4j.data.FoldingWriter;

/**
 * {@link IcalExportWriter} - Streams events to export directly on the {@link OutputStream}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class IcalExportWriter {

    private final ICalUtilities iCalUtilities;

    private FoldingWriter writer;

    private boolean initialized = false;

    // ------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link IcalExportWriter}.
     * 
     * @param iCalUtilities The {@link ICalUtilities}
     * 
     */
    public IcalExportWriter(ICalUtilities iCalUtilities) {
        super();
        this.iCalUtilities = iCalUtilities;
    }

    // ------------------------------------------------------------------------------------------------

    public void writeEventChunk(OutputStream outputStream, VCalendar vCalendar, Set<String> timezoneIDs, ICalParameters parameters, List<Event> events) throws OXException {
        if (false == initialized) {
            initialized = true;
            writer = new FoldingWriter(new OutputStreamWriter(outputStream, Charsets.UTF_8), FoldingWriter.MAX_FOLD_LENGTH);
            write(getStart());
            write(getProperties(vCalendar));
            iCalUtilities.exportTimeZones(writer, new ArrayList<>(timezoneIDs), parameters);
        }
        iCalUtilities.exportEvent(writer, events, parameters);
    }

    public void build() throws OXException {
        if (false == initialized) {
            return;
        }
        try {
            write(getEnd());
        } finally {
            Streams.close(writer);
        }
    }

    // ------------------------------------------------------------------------------------------------

    /**
     * Writes the begin of the calendar to the stream.
     * <p/>
     * Note that the generated data will only contain part of <code>VCALENDAR</code> component, the (<code>BEGIN:VCALENDAR</code>),
     * <a href="https://tools.ietf.org/html/rfc5545#section-3.4">RFC 5545, section 3.4</a>.
     */
    private String getStart() {
        // Begin calendar 
        final StringBuilder sb = new StringBuilder();
        sb.append(BEGIN);
        sb.append(':');
        sb.append(VCALENDAR);
        sb.append(LINE_SEPARATOR);
        return sb.toString();
    }

    /**
     * Writes the end of the calendar to the stream and closes internal resources. Writing will fail afterwards.
     * <p/>
     * Note that the generated data will only contain part of <code>VCALENDAR</code> component, the (<code>END:VCALENDAR</code>),
     * <a href="https://tools.ietf.org/html/rfc5545#section-3.4">RFC 5545, section 3.4</a>.
     */
    private String getEnd() {
        // End calendar
        final StringBuilder sb = new StringBuilder();
        sb.append(END);
        sb.append(':');
        sb.append(VCALENDAR);
        sb.append(LINE_SEPARATOR);
        return sb.toString();
    }

    /**
     * Writes the properties of the calendar
     * <p/>
     * Note that the generated data will only contain property components, e.g. the (<code>VERSION:2.0</code>),
     * <a href="https://tools.ietf.org/html/rfc5545#section-3.4">RFC 5545, section 3.4</a>.
     * 
     * @param vCalendar The {@link VCalendar} holding the properties
     */

    @SuppressWarnings("unchecked")
    private String getProperties(VCalendar vCalendar) {
        final StringBuilder sb = new StringBuilder();
        vCalendar.getProperties().stream().forEach(p -> sb.append(p));
        return sb.toString();
    }

    // ------------------------------------------------------------------------------------------------

    private void write(String str) throws OXException {
        try {
            writer.write(str);
        } catch (IOException e) {
            throw ICalExceptionCodes.IO_ERROR.create(e);
        }
    }
}
