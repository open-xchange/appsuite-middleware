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

package com.openexchange.importexport.importers.ical;

import static com.openexchange.java.Autoboxing.I;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.ical.ImportedComponent;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EventID;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.TruncationInfo;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractICalEventImporter}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public abstract class AbstractICalEventImporter extends AbstractICalImporter{

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractICalEventImporter.class);

    public AbstractICalEventImporter(ServerSession session) {
        super(session);
    }

    /**
     * Creates an event
     *
     * @param folderId The folder to import to
     * @param event The event to be created
     * @return CalendarResult The result of the event creation
     * @throws OXException if event creation fails
     */
    abstract protected CalendarResult createEvent(String folderId, Event event) throws OXException ;

    /**
     * Updates an event
     *
     * @param eventId The {@link EventID} of the event which gets an update
     * @param event The new event data to use for the update
     * @return CalendarResult The result of the event update
     * @throws OXException if event creation fails
     */
    abstract protected CalendarResult updateEvent(EventID eventId, Event event) throws OXException ;

    /**
     * Initializes the appropriate calendar access
     *
     * @param userizedFolder The folder to import to
     * @param eventList The event list to import
     * @param optionalParams The optional parameters of the request
     * @param list The list which contains the import results
     * @return TruncationInfo The truncation info of the import
     * @throws OXException if the import fails
     */
    abstract protected TruncationInfo initImporter(UserizedFolder userizedFolder, List<Event> eventList, Map<String, String[]> optionalParams, List<ImportResult> list) throws OXException;

    @Override
    public TruncationInfo importData(UserizedFolder userizedFolder, InputStream is, List<ImportResult> list, Map<String, String[]> optionalParams) throws OXException {
        ICalService iCalService = ImportExportServices.getICalService();
        ICalParameters iCalParameters = iCalService.initParameters();
        iCalParameters.set(ICalParameters.SANITIZE_INPUT, Boolean.TRUE);
        iCalParameters.set(ICalParameters.DEFAULT_TIMEZONE, TimeZone.getTimeZone(session.getUser().getTimeZone()));
        ImportedCalendar calendarImport;
        try {
            calendarImport = iCalService.importICal(is, iCalParameters);
            return initImporter(userizedFolder, calendarImport.getEvents(), optionalParams, list);
        } catch (OXException e) {
            if ("ICAL-0003".equals(e.getErrorCode())) {
                // "No calendar data found", silently ignore, as expected by com.openexchange.ajax.importexport.Bug9209Test.test9209ICal()
                list = Collections.emptyList();
                //TODO: change return value
                return new TruncationInfo(0, 0);
            }
            ImportResult result = new ImportResult();
            result.setException(e);
            list = Collections.singletonList(result);
            //TODO: change return value
            return new TruncationInfo(0, 0);
        }
    }

    protected TruncationInfo importEvents(UserizedFolder userizedFolder, List<Event> eventList, Map<String, String[]> optionalParams, List<ImportResult> list) {
        boolean ignoreUIDs = isIgnoreUIDs(optionalParams);
        List<ImportResult> importResults = new ArrayList<>();
        int createdEventCount = 0;
        for (Map.Entry<String, List<Event>> entry : getEventsByUID(eventList, true).entrySet()) {
            List<Event> events = sortSeriesMasterFirst(entry.getValue());
            /*
             * (re-) assign UID to imported event if required
             */
            String uid = entry.getKey();
            if (null == uid || ignoreUIDs) {
                uid = UUID.randomUUID().toString();
                for (Event event : events) {
                    event.setUid(uid);
                }
            }
            /*
             * create first event (master or non-recurring)
             */
            ImportResult result = createImportResult(userizedFolder.getID(), null, events.get(0));
            importResults.add(result);
            EventID masterEventID = new EventID(userizedFolder.getID(), result.getObjectId());
            /*
             * create further events as change exceptions
             */
            if (1 < events.size() && false == result.hasError()) {
                createdEventCount++;
                for (int i = 1; i < events.size(); i++) {
                    importResults.add(createImportResult(null, masterEventID, events.get(i)));
                    createdEventCount++;
                }
            }
        }
        list.addAll(importResults);
        return new TruncationInfo(eventList.size(), createdEventCount);
    }

    private ImportResult createImportResult(String folder, EventID masterEventID, Event importedEvent) {
        final int MAX_RETRIES = 5;
        ImportResult importResult = prepareResult(importedEvent);
        for (int retryCount = 1; retryCount <= MAX_RETRIES; retryCount++) {
            try {
                CalendarResult result = null == masterEventID ? createEvent(folder, importedEvent) : createEventException(masterEventID, importedEvent);
                importResult.setDate(new Date(result.getTimestamp()));
                if (result.getCreations().isEmpty()) {
                    importResult.setException(ImportExportExceptionCodes.COULD_NOT_CREATE.create(importedEvent));
                } else {
                    importResult.setFolder(result.getCreations().get(0).getCreatedEvent().getFolderId());
                    importResult.setObjectId(result.getCreations().get(0).getCreatedEvent().getId());
                }
            } catch (OXException e) {
                if (retryCount < MAX_RETRIES && handle(e, importedEvent)) {
                    // try again
                    LOG.debug("{} - trying again ({}/{})", e.getMessage(), retryCount, MAX_RETRIES, e);
                    importResult.addWarnings(Collections.singletonList(
                        new ConversionWarning(importResult.getEntryNumber(), ConversionWarning.Code.TRUNCATION_WARNING, e.getMessage())));
                    continue;
                }
                // "re-throw"
                importResult.setException(e);
            }
            if (false == importResult.hasError() && null != importResult.getWarnings() && 0 < importResult.getWarnings().size()) {
                importResult.setException(ImportExportExceptionCodes.WARNINGS.create(I(importResult.getWarnings().size())));
            }
            return importResult;
        }
        throw new AssertionError(); // should not get here
    }

    private CalendarResult createEventException(EventID eventId, Event event) throws OXException {
        return updateEvent(eventId, event);
    }

    private Map<String, List<Event>> getEventsByUID(List<Event> events, boolean assignIfEmpty) {
        return CalendarUtils.getEventsByUID(events, assignIfEmpty);
    }

    private List<Event> sortSeriesMasterFirst(List<Event> events) {
        return CalendarUtils.sortSeriesMasterFirst(events);
    }

    /**
     * Tries to handle data truncation and incorrect string errors automatically.
     *
     * @param session The calendar session
     * @param e The exception to handle
     * @param event The event being saved
     * @return <code>true</code> if the exception could be handled and the operation should be tried again, <code>false</code>, otherwise
     */
    protected static boolean handle(OXException e, Event event) {
        CalendarUtilities calendarUtilities = ImportExportServices.getCalendarUtilities();
        try {
            switch (e.getErrorCode()) {
                case "CAL-4227": // Incorrect string [string %1$s, field %2$s, column %3$s]
                    return calendarUtilities.handleIncorrectString(e, event);
                case "CAL-5070": // Data truncation [field %1$s, limit %2$d, current %3$d]
                    return calendarUtilities.handleDataTruncation(e, event);
            }
        } catch (Exception x) {
            LOG.warn("Error during automatic handling of {}", e.getErrorCode(), x);
        }
        return false;
    }

    private static ImportResult prepareResult(Event importedEvent) {
        ImportResult importResult = new ImportResult();
        if (ImportedComponent.class.isInstance(importedEvent)) {
            ImportedComponent component = (ImportedComponent) importedEvent;
            importResult.setEntryNumber(component.getIndex());
            List<OXException> importWarnings = component.getWarnings();
            if (null != importWarnings && 0 < importWarnings.size()) {
                List<ConversionWarning> conversionWarnings = new ArrayList<>(importWarnings.size());
                for (OXException importWarning : importWarnings) {
                    conversionWarnings.add(new ConversionWarning(component.getIndex(), importWarning));
                }
                importResult.addWarnings(conversionWarnings);
            }
        }
        return importResult;
    }

    /**
     * @param optionalParams The optional parameters of the request
     * @return <code>true</code> if the notification should be suppressed, <code>false</code>, otherwise
     */
    protected boolean isSupressNotification(Map<String, String[]> optionalParams) {
        return null != optionalParams && optionalParams.containsKey("suppressNotification") ? true : false;
    }

}
