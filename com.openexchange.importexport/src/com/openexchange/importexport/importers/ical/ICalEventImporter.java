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
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
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
 * {@link ICalEventImporter}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class ICalEventImporter extends AbstractICalImporter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICalEventImporter.class);

    private CalendarService calendarService;
    private CalendarSession calendarSession;

    private IDBasedCalendarAccess calendarAccess;

    public ICalEventImporter(ServerSession session, UserizedFolder userizedFolder) {
        super(session, userizedFolder);
    }

    @Override
    public TruncationInfo importData(InputStream is, List<ImportResult> list, Map<String, String[]> optionalParams) throws OXException {
        ICalService iCalService = ImportExportServices.getICalService();
        ICalParameters iCalParameters = iCalService.initParameters();
        iCalParameters.set(ICalParameters.SANITIZE_INPUT, Boolean.TRUE);
        iCalParameters.set(ICalParameters.DEFAULT_TIMEZONE, TimeZone.getTimeZone(getSession().getUser().getTimeZone()));
        ImportedCalendar calendarImport;
        try {
            calendarImport = iCalService.importICal(is, iCalParameters);
            return doImport(calendarImport, optionalParams, list);
        } catch (OXException e) {
            TruncationInfo trunc = new TruncationInfo(list.size(), 0);
            if ("ICAL-0003".equals(e.getErrorCode())) {
                // "No calendar data found", silently ignore, as expected by com.openexchange.ajax.importexport.Bug9209Test.test9209ICal()
                list = Collections.emptyList();
                return trunc;
            }
            ImportResult result = new ImportResult();
            result.setException(e);
            list = Collections.singletonList(result);
            return trunc;
        }
    }

    private TruncationInfo doImport(ImportedCalendar calendarImport, Map<String, String[]> optionalParams, List<ImportResult> list) throws OXException {
        initICalImporter(optionalParams);
        TruncationInfo truncationInfo;
        if (checkFolderAccId()) {
            truncationInfo = importEvents(calendarImport, optionalParams, list);
        } else {
            boolean committed = false;
            try {
                calendarAccess.startTransaction();
                truncationInfo = importEvents(calendarImport, optionalParams, list);
                calendarAccess.commit();
                committed = true;
            } finally {
                if (false == committed) {
                    calendarAccess.rollback();
                }
                calendarAccess.finish();
            }
        }

        return truncationInfo;
    }

    private TruncationInfo importEvents(ImportedCalendar calendarImport, Map<String, String[]> optionalParams, List<ImportResult> list) {
        boolean ignoreUIDs = isIgnoreUIDs(optionalParams);
        List<ImportResult> importResults = new ArrayList<>();
        List<Event> eventList = calendarImport.getEvents();
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
            String folderId = getUserizedFolder().getID();
            ImportResult result = createImportResult(folderId, null, events.get(0), null);
            importResults.add(result);
            EventID masterEventID = new EventID(folderId, result.getObjectId());
            createdEventCount++;
            /*
             * create further events as change exceptions
             */
            if (1 < events.size() && false == result.hasError()) {
                Long timestamp = result.getDate().getTime();
                for (int i = 1; i < events.size(); i++) {
                    ImportResult createResult = createImportResult(null, masterEventID, events.get(i), timestamp);
                    timestamp = createResult.getDate().getTime();
                    importResults.add(createResult);
                    createdEventCount++;
                }
            }

        }
        list.addAll(importResults);
        return handleCalendarImportTruncation(calendarImport, list.size());
    }

    private ImportResult createImportResult(String folder, EventID masterEventID, Event importedEvent, Long timestamp) {
        final int MAX_RETRIES = 5;
        ImportResult importResult = prepareResult(importedEvent);
        for (int retryCount = 1; retryCount <= MAX_RETRIES; retryCount++) {
            try {
                CalendarResult result = null == masterEventID ? createEvent(folder, importedEvent) : createEventException(masterEventID, importedEvent, timestamp);
                importResult.setDate(new Date(result.getTimestamp()));
                if (result.getCreations().isEmpty() && !result.getUpdates().isEmpty()) {
                    importResult.setFolder(result.getUpdates().get(0).getUpdate().getFolderId());
                    importResult.setObjectId(result.getUpdates().get(0).getUpdate().getId());
                } else if (!result.getCreations().isEmpty() && result.getUpdates().isEmpty()) {
                    importResult.setFolder(result.getCreations().get(0).getCreatedEvent().getFolderId());
                    importResult.setObjectId(result.getCreations().get(0).getCreatedEvent().getId());
                } else if (!result.getCreations().isEmpty() && !result.getUpdates().isEmpty()) {
                    importResult.setFolder(result.getCreations().get(0).getCreatedEvent().getFolderId());
                    importResult.setObjectId(result.getCreations().get(0).getCreatedEvent().getId());
                } else {
                    importResult.setException(ImportExportExceptionCodes.COULD_NOT_CREATE.create());
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

    private CalendarResult createEventException(EventID eventId, Event event, Long timestamp) throws OXException {
        return updateEvent(eventId, event, timestamp);
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
    private boolean isSupressNotification(Map<String, String[]> optionalParams) {
        return null != optionalParams && optionalParams.containsKey("suppressNotification") ? true : false;
    }

    private void initICalImporter(Map<String, String[]> optionalParams) throws OXException {
        if (checkFolderAccId()) {
            initCalendarService(optionalParams);
        } else {
            initCalendarAccess(optionalParams);
        }
    }

    private void initCalendarService(Map<String, String[]> optionalParams) throws OXException {
        this.calendarService = ImportExportServices.getCalendarService();
        this.calendarSession = calendarService.init(getSession());
        calendarSession.set(CalendarParameters.PARAMETER_IGNORE_CONFLICTS, Boolean.TRUE);
        if (isSupressNotification(optionalParams)) {
            calendarSession.set(CalendarParameters.PARAMETER_NOTIFICATION, Boolean.FALSE);
        }
    }

    private void initCalendarAccess(Map<String, String[]> optionalParams) throws OXException {
        this.calendarAccess = ImportExportServices.getIDBasedCalendarAccessFactory().createAccess(getSession());
        calendarAccess.set(CalendarParameters.PARAMETER_IGNORE_CONFLICTS, Boolean.TRUE);
        if (isSupressNotification(optionalParams)) {
            calendarAccess.set(CalendarParameters.PARAMETER_NOTIFICATION, Boolean.FALSE);
        }
    }

    private CalendarResult createEvent(String folderId, Event event) throws OXException {
        return checkFolderAccId() ? calendarService.createEvent(calendarSession, folderId, event) : calendarAccess.createEvent(folderId, event);
    }

    private CalendarResult updateEvent(EventID eventId, Event event, Long timestamp) throws OXException {
        return checkFolderAccId() ? calendarService.updateEvent(calendarSession, eventId, event, timestamp) : calendarAccess.updateEvent(eventId, event, timestamp);
    }

    private TruncationInfo handleCalendarImportTruncation(ImportedCalendar calendarImport, int totalResults) {
        TruncationInfo trunc = null;
        for (OXException e : calendarImport.getWarnings()) {
            if (e.getErrorCode().equals("ICAL-0006")) {
                if (null != e.getLogArgs() && 1 < e.getLogArgs().length && Integer.class.isInstance(e.getLogArgs()[0]) && Integer.class.isInstance(e.getLogArgs()[1])) {
                    int limit = ((Integer)e.getLogArgs()[0]).intValue();
                    int total = ((Integer)e.getLogArgs()[1]).intValue();
                    trunc = new TruncationInfo(limit, total);
                    break;
                }
            }
        }
        if (null == trunc) {
            trunc = new TruncationInfo(calendarImport.getEvents().size(), totalResults);
        }
        return trunc;
    }

    private boolean checkFolderAccId() {
        return null == getUserizedFolder().getAccountID() ? true : false;
    }

}
