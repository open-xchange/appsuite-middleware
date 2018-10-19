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

package com.openexchange.importexport.exporters.ical;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.ical.CalendarExport;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;

/**
 * {@link AbstractICalEventExporter}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public abstract class AbstractICalEventExporter extends AbstractICalExporter {

    public AbstractICalEventExporter(String folderId, Map<String, List<String>> batchIds) {
        super(folderId, batchIds);
    }

    /**
     * Converts the batchId list to a list of {@link EventID}
     *
     * @return List The list of {@link EventID}
     */
    protected List<EventID> convertBatchDataToEventIds() {
        List<EventID> events = new ArrayList<>();
        for (Map.Entry<String, List<String>> batchEntry : getBatchIds().entrySet()) {
            for (String objectId : batchEntry.getValue()) {
                events.add(new EventID(batchEntry.getKey(), objectId));
            }
        }
        return events;
    }

    /**
     * Exports a list of {@link Event}
     *
     * @param eventList The event list to export
     * @param optOut The output stream
     * @param session The session
     * @return ThresholdFileHolder The file holder
     * @throws OXException if the export fails
     */
    protected ThresholdFileHolder exportChronosEvents(List<Event> eventList, OutputStream optOut, Session session) throws OXException {
        ICalService iCalService = ImportExportServices.getICalService();
        ICalParameters iCalParameters = iCalService.initParameters();
        CalendarExport calendarExport = iCalService.exportICal(iCalParameters);
        for (Event event : eventList) {
            if (event == null) {
                // Skip not existing events
                continue;
            }
            calendarExport.add(prepareForExport(event));
        }
        return write(calendarExport, optOut);
    }

    /**
     * Serializes a calendar export. In case an output stream is available, the iCalendar data is written directly. Otherwise, the data is
     * written into a new file holder sink.
     * 
     * @param calendarExport The calendar export to serialize
     * @param optOut The output stream to write to, or <code>null</code> to write to a new file holder
     * @return The file holder sink, or <code>null</code> if the export was directly written to the output stream
     */
    protected ThresholdFileHolder write(CalendarExport calendarExport, OutputStream optOut) throws OXException {
        if (null != optOut) {
            calendarExport.writeVCalendar(optOut);
            return null;
        }
        ThresholdFileHolder sink = new ThresholdFileHolder();
        boolean error = true;
        try {
            calendarExport.writeVCalendar(sink.asOutputStream());
            error = false;
            return sink;
        } finally {
            if (error) {
                Streams.close(sink);
            }
        }
    }

    /**
     * Prepares an event for export.
     *
     * @param event The event
     * @return The prepared event
     */
    protected static Event prepareForExport(Event event) {
        if (CalendarUtils.isPseudoGroupScheduled(event)) {
            Event copy;
            try {
                copy = EventMapper.getInstance().copy(event, null, (EventField[]) null);
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(AbstractICalExporter.class).warn("Error copying event, falling back to original event data", e);
                return event;
            }
            copy.removeAttendees();
            copy.removeOrganizer();
            return copy;
        }
        return event;
    }

    /**
     * Prepares an list of events for export.
     *
     * @param events The events to export
     * @return The prepared event as {@link List}
     */
    protected static List<Event> prepareForExport(List<Event> events) {
        List<Event> copied = new LinkedList<Event>();
        for (Event event : events) {
            if (null != event) {
                copied.add(prepareForExport(event));
            }
        }
        return copied;
    }

}
