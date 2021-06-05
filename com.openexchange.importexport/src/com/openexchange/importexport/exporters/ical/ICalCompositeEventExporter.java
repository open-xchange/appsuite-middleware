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

package com.openexchange.importexport.exporters.ical;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.Lists;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ical.StreamedCalendarExport;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ICalCompositeEventExporter}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class ICalCompositeEventExporter extends AbstractICalEventExporter {

    /** The event fields used for the export */
    private static final EventField[] EXPORTED_FIELDS = com.openexchange.tools.arrays.Arrays.remove(EventField.values(), EventField.ALARMS, EventField.ATTACHMENTS, EventField.FLAGS);

    public ICalCompositeEventExporter(String folderId, Map<String, List<String>> batchIds) {
        super(folderId, batchIds);
    }

    @Override
    protected ThresholdFileHolder exportFolderData(ServerSession session, OutputStream out) throws OXException {
        /*
         * get identifiers of all events in exported folder
         */
        IDBasedCalendarAccess calendarAccess = getCalendarAccess(session);
        calendarAccess.set(CalendarParameters.PARAMETER_FIELDS, new EventField[] { EventField.FOLDER_ID, EventField.ID, EventField.START_DATE, EventField.END_DATE });
        calendarAccess.set(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.FALSE);
        int limit = getExportLimit(session);
        if (-1 < limit) {
            calendarAccess.set(CalendarParameters.PARAMETER_RIGHT_HAND_LIMIT, I(limit));
        }

        List<EventID> eventIDs = new LinkedList<>();
        Set<String> timezoneIDs = new HashSet<>();
        parseEvents(calendarAccess.getEventsInFolder(getFolderId()), eventIDs, timezoneIDs);

        /*
         * prepare export
         */
        calendarAccess = getCalendarAccess(session);
        calendarAccess.set(CalendarParameters.PARAMETER_FIELDS, EXPORTED_FIELDS);
        calendarAccess.set(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.FALSE);

        if (null != out) {
            stream(session, out, calendarAccess, eventIDs, timezoneIDs);
            return null;
        }

        boolean success = false;
        ThresholdFileHolder sink = null;
        try {
            sink = new ThresholdFileHolder();
            stream(session, sink.asOutputStream(), calendarAccess, eventIDs, timezoneIDs);
            success = true;
            return sink;
        } finally {
            if (false == success) {
                Streams.close(sink);
            }
        }
    }

    @Override
    protected ThresholdFileHolder exportBatchData(ServerSession session, OutputStream out) throws OXException {
        List<EventID> eventIds = convertBatchDataToEventIds();
        if (eventIds.size() == 1) {
            return exportChronosEvents(Collections.singletonList(getCalendarAccess(session).getEvent(eventIds.get(0))), out, session);
        } else {
            return exportChronosEvents(getCalendarAccess(session).getEvents(eventIds), out, session);
        }
    }

    private IDBasedCalendarAccess getCalendarAccess(ServerSession session) throws OXException {
        return ImportExportServices.getIDBasedCalendarAccessFactory().createAccess(session);
    }

    private void stream(ServerSession session, OutputStream out, IDBasedCalendarAccess calendarAccess, List<EventID> eventIDs, Set<String> timezoneIDs) throws OXException {
        /*
         * load full event data in chunks & add to export
         */
        try (StreamedCalendarExport streamedExport = ImportExportServices.getICalService().getStreamedExport(out, null)) {
            streamedExport.writeMethod("PUBLISH");
            String name = extractName(session, getFolderId());
            if (Strings.isNotEmpty(name)) {
                streamedExport.writeCalendarName(extractName(session, getFolderId()));
            }
            streamedExport.writeTimeZones(timezoneIDs);
            for (List<EventID> chunk : Lists.partition(eventIDs, 100)) {
                /*
                 * serialize calendar
                 */
                streamedExport.writeEvents(prepareForExport(calendarAccess.getEvents(chunk)));
            }
            streamedExport.finish();
        } catch (IOException e) {
            throw ImportExportExceptionCodes.IOEXCEPTION.create(e);
        }
    }
    
    /**
     * Gets the full identifiers for the supplied events holding the folder and file identifiers
     * and get all used timezones
     * 
     * @param eventsInFolder The events to get the full identifiers for
     * @param eventIDs A {@link List} to write the {@link EventID}s to
     * @param timezoneIDs A {@link Set} to write the timezones to
     */
    private static void parseEvents(List<Event> eventsInFolder, List<EventID> eventIDs, Set<String> timezoneIDs) {
        if (null == eventsInFolder) {
            return;
        }
        for (Event event : eventsInFolder) {
            eventIDs.add(new EventID(event.getFolderId(), event.getId()));
            addTimeZone(timezoneIDs, event.getStartDate());
            addTimeZone(timezoneIDs, event.getEndDate());
        }
    }

    private static boolean addTimeZone(Set<String> timeZones, org.dmfs.rfc5545.DateTime dateTime) {
        if (null != dateTime && false == dateTime.isFloating() && null != dateTime.getTimeZone() && false == "UTC".equals(dateTime.getTimeZone().getID())) {
            return timeZones.add(dateTime.getTimeZone().getID());
        }
        return false;
    }

}
