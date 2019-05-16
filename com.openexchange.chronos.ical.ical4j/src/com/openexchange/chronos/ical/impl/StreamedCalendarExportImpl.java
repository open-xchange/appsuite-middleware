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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.FreeBusyData;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalUtilities;
import com.openexchange.chronos.ical.StreamedCalendarExport;
import com.openexchange.chronos.ical.ical4j.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.version.VersionService;
import net.fortuna.ical4j.data.FoldingWriter;
import net.fortuna.ical4j.extensions.property.WrCalName;
import net.fortuna.ical4j.model.PropertyFactoryImpl;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

/**
 * {@link StreamedCalendarExportImpl} -Synchronized {@link StreamedCalendarExport} for iCal files. Timezone definitions will be written on top of the events.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class StreamedCalendarExportImpl implements StreamedCalendarExport {

    private final static List<Method> METHODS = ImmutableList.of(Method.ADD, Method.CANCEL, Method.COUNTER, Method.DECLINE_COUNTER, Method.PUBLISH, Method.REFRESH, Method.REPLY, Method.REQUEST);

    // ------------------------------------------------------------------------------------------------

    protected final FoldingWriter writer;

    protected final ICalParameters parameters;

    // ------------------------------------------------------------------------------------------------

    private final ICalUtilitiesImpl iCalUtilities;

    // ------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link StreamedCalendarExportImpl}.
     *
     * @param iCalUtilities The {@link ICalUtilities}
     * @param parameters The {@link ICalParameters}
     * @param outputStream The {@link OutputStream}
     * @throws IOException If writing fails
     */
    public StreamedCalendarExportImpl(ICalUtilitiesImpl iCalUtilities, ICalParameters parameters, OutputStream outputStream) throws IOException {
        super();
        this.iCalUtilities = iCalUtilities;
        this.parameters = parameters;

        writer = new FoldingWriter(new OutputStreamWriter(outputStream, Charsets.UTF_8), FoldingWriter.REDUCED_FOLD_LENGTH);
        write(getStart());
        write(Version.VERSION_2_0);
        write(getProdID());
    }

    // ------------------------------------------------------------------------------------------------

    @Override
    public void writeMethod(String method) throws IOException {
        write(getMethod(method));
    }

    @Override
    public void writeCalendarName(String name) throws IOException, OXException {
        WrCalName calName = new WrCalName(PropertyFactoryImpl.getInstance());
        calName.setValue(name);
        write(calName);
    }

    @Override
    public void writeEvents(List<Event> events) throws IOException, OXException {
        try {
            iCalUtilities.exportEvent(writer, events, parameters);
        } catch (OXException e) {
            unfoldIOException(e);
        }
    }

    @Override
    public void writeFreeBusy(List<FreeBusyData> freeBusyData) throws IOException, OXException {
        try {
            iCalUtilities.exportFreeBusy(writer, freeBusyData, parameters);
        } catch (OXException e) {
            unfoldIOException(e);
        }
    }

    @Override
    public void writeTimeZones(Set<String> timeZoneIDs) throws IOException, OXException {
        try {
            iCalUtilities.exportTimeZones(writer, new ArrayList<>(timeZoneIDs), parameters);
        } catch (OXException e) {
            unfoldIOException(e);
        }
    }

    @Override
    public void writeProperties(List<ExtendedProperty> property) throws IOException, OXException {
        if (null == property || property.isEmpty()) {
            return;
        }
        for (Iterator<ExtendedProperty> iterator = property.iterator(); iterator.hasNext();) {
            write(ICalUtils.exportProperty(iterator.next()));
        }
    }

    @Override
    public void finish() throws IOException {
        try {
            write(getEnd());
        } finally {
            Streams.close(writer);
        }
    }

    @Override
    public void close() throws IOException {
        Streams.close(writer);
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
        StringBuilder sb = new StringBuilder();
        sb.append(END);
        sb.append(':');
        sb.append(VCALENDAR);
        sb.append(LINE_SEPARATOR);
        return sb.toString();
    }

    private ProdId getProdID() {
        StringBuilder sb = new StringBuilder();
        sb.append("-//").append(VersionService.NAME).append("//");
        VersionService versionService = Services.getService(VersionService.class);
        if (versionService == null) {
            sb.append("<unknown version>");
        } else {
            sb.append(versionService.getVersionString());
        }
        sb.append("//EN");
        return new ProdId(sb.toString());
    }

    /**
     * Get the method for exporting
     *
     * @param toMatch The method name or <code>null</code>
     * @return The {@link Method} or {@link Method#PUBLISH} as default
     */
    private Method getMethod(String toMatch) {
        if (Strings.isNotEmpty(toMatch)) {
            for (Iterator<Method> iterator = METHODS.iterator(); iterator.hasNext();) {
                Method method = iterator.next();
                if (method.getValue().equals(toMatch)) {
                    return method;
                }
            }
        }
        return Method.PUBLISH;
    }

    private void unfoldIOException(OXException e) throws IOException, OXException {
        if (null != e.getCause() && IOException.class.isAssignableFrom(e.getCause().getClass())) {
            // Throw nested IOException prior wrapping it into an OXExcpetion
            throw (IOException) e.getCause();
        }
        throw e;
    }

    // ------------------------------------------------------------------------------------------------

    protected void write(Object o) throws IOException {
        if (null != o) {
            write(o.toString());
        }
    }

    protected void write(String str) throws IOException {
        writer.write(str);
    }
}
