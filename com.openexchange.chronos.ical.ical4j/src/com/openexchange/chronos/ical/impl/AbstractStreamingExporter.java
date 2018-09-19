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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.ical.ICalExceptionCodes;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalUtilities;
import com.openexchange.chronos.ical.StreamingExporter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import net.fortuna.ical4j.data.FoldingWriter;
import net.fortuna.ical4j.extensions.property.WrCalName;
import net.fortuna.ical4j.model.PropertyFactoryImpl;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

/**
 * {@link AbstractStreamingExporter}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public abstract class AbstractStreamingExporter implements StreamingExporter {

    private final static List<Method> METHODS = ImmutableList.of(Method.ADD, Method.CANCEL, Method.COUNTER, Method.DECLINE_COUNTER, Method.PUBLISH, Method.REFRESH, Method.REPLY, Method.REQUEST);

    // ------------------------------------------------------------------------------------------------

    protected FoldingWriter writer;

    protected ICalParameters parameters;

    // ------------------------------------------------------------------------------------------------

    private final ICalUtilities iCalUtilities;

    private Method method;

    private WrCalName calendarName;

    // ------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link AbstractStreamingExporter}.
     * 
     * @param iCalUtilities The {@link ICalUtilities}
     * @param parameters The {@link ICalParameters}
     * 
     */
    public AbstractStreamingExporter(ICalUtilities iCalUtilities, ICalParameters parameters) {
        super();
        this.iCalUtilities = iCalUtilities;
        this.parameters = parameters;
    }

    // ------------------------------------------------------------------------------------------------

    @Override
    public void prepare(String methodName, String calendarName) {
        this.method = getMethod(methodName);
        WrCalName calName = new WrCalName(PropertyFactoryImpl.getInstance());
        calName.setValue(calendarName);
        this.calendarName = calName;
    }

    @Override
    public void start(OutputStream outputStream) throws OXException {
        writer = new FoldingWriter(new OutputStreamWriter(outputStream, Charsets.UTF_8), FoldingWriter.MAX_FOLD_LENGTH);
        write(getStart());
        write(getProperties());
    }

    @Override
    public void streamChunk(List<Event> events) throws OXException {
        iCalUtilities.exportEvent(writer, events, parameters);
    }

    @Override
    public void finish() throws OXException {
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
        StringBuilder sb = new StringBuilder();
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
     */
    private String getProperties() {
        StringBuilder sb = new StringBuilder();
        sb.append(Version.VERSION_2_0);
        sb.append(getProdID());
        sb.append(method);
        sb.append(calendarName);
        return sb.toString();
    }

    private ProdId getProdID() {
        StringBuilder sb = new StringBuilder();
        sb.append("-//").append(com.openexchange.version.Version.NAME).append("//");
        String versionString = com.openexchange.version.Version.getInstance().optVersionString();
        if (Strings.isEmpty(versionString)) {
            sb.append("<unknown version>");
        } else {
            sb.append(versionString);
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

    // ------------------------------------------------------------------------------------------------

    protected void write(String str) throws OXException {
        boolean error = false;
        try {
            writer.write(str);
        } catch (IOException e) {
            error = true;
            throw ICalExceptionCodes.IO_ERROR.create(e);
        } finally {
            if (error) {
                Streams.close(writer);
            }
        }
    }

    protected boolean setTimeZones(Event event, Set<VTimeZone> timeZones) {
        boolean added = false;
        if (false == CalendarUtils.isFloating(event)) {
            added |= setTimeZone(timeZones, event.getStartDate());
            added |= setTimeZone(timeZones, event.getEndDate());
        }
        return added;
    }

    protected boolean setTimeZone(Set<VTimeZone> timeZones, org.dmfs.rfc5545.DateTime dateTime) {
        if (null != dateTime && false == dateTime.isFloating() && null != dateTime.getTimeZone() && false == "UTC".equals(dateTime.getTimeZone().getID())) {
            TimeZoneRegistry timeZoneRegistry = parameters.get(ICalParametersImpl.TIMEZONE_REGISTRY, TimeZoneRegistry.class);
            net.fortuna.ical4j.model.TimeZone timeZone = timeZoneRegistry.getTimeZone(dateTime.getTimeZone().getID());
            return timeZones.add(timeZone.getVTimeZone());
        }
        return false;
    }
}
