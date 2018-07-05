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
import static com.openexchange.java.Autoboxing.i;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.UIDConflictStrategy;
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

    /**
     * Initializes a new {@link ICalEventImporter}.
     *
     * @param session The user session
     * @param userizedFolder The target folder to use for importing data
     */
    public ICalEventImporter(ServerSession session, UserizedFolder userizedFolder) {
        super(session, userizedFolder);
    }

    @Override
    public TruncationInfo importData(InputStream inputStream, List<ImportResult> list, Map<String, String[]> optionalParameters) throws OXException {
        /*
         * parse iCal data
         */
        ImportedCalendar importedCalendar;
        try {
            importedCalendar = parseICalendar(inputStream);
        } catch (OXException e) {
            if ("ICAL-0003".equals(e.getErrorCode())) {
                // "No calendar data found", silently ignore, as expected by com.openexchange.ajax.importexport.Bug9209Test.test9209ICal()
            } else {
                ImportResult errorResult = new ImportResult();
                errorResult.setException(e);
                list.add(errorResult);
            }
            return null;
        }

        // Workaround for bug 57282:
        stripAttendeesAndOrganizer(importedCalendar);

        /*
         * store imported events & track corresponding results
         */
        List<com.openexchange.chronos.service.ImportResult> results = importEvents(getUserizedFolder(), importedCalendar.getEvents(), optionalParameters);
        list.addAll(getImportResults(results));
        /*
         * extract information for possibly truncated results
         */
        return extractTruncationInfo(importedCalendar);
    }

    /**
     * Parses the supplied stream carrying iCalendar data.
     *
     * @param inputStream The input stream to parse
     * @return The imported calendar
     */
    private ImportedCalendar parseICalendar(InputStream inputStream) throws OXException {
        ICalService iCalService = ImportExportServices.getICalService();
        ICalParameters iCalParameters = iCalService.initParameters();
        iCalParameters.set(ICalParameters.SANITIZE_INPUT, Boolean.TRUE);
        iCalParameters.set(ICalParameters.DEFAULT_TIMEZONE, TimeZone.getTimeZone(getSession().getUser().getTimeZone()));
        return iCalService.importICal(inputStream, iCalParameters);
    }

    /**
     * Stores imported events in the target folder.
     *
     * @param targetFolder The target folder to store the imported events in
     * @param events The events to store
     * @param optionalParameters The optional parameters as sent from the client
     * @return The import results
     */
    private List<com.openexchange.chronos.service.ImportResult> importEvents(UserizedFolder targetFolder, List<Event> events, Map<String, String[]> optionalParameters) throws OXException {
        /*
         * store imported events...
         */
        List<com.openexchange.chronos.service.ImportResult> results;
        if (hasAccountID(targetFolder)) {
            /*
             * ... via ID-based access for folders in calendar accounts
             */
            IDBasedCalendarAccess calendarAccess = ImportExportServices.getIDBasedCalendarAccessFactory().createAccess(getSession());
            applyParameters(calendarAccess, optionalParameters);
            boolean committed = false;
            try {
                calendarAccess.startTransaction();
                results = calendarAccess.importEvents(targetFolder.getID(), events);
                calendarAccess.commit();
                committed = true;
            } finally {
                if (false == committed) {
                    calendarAccess.rollback();
                }
                calendarAccess.finish();
            }
        } else {
            /*
             * ... directly via calendar service for legacy folders
             */
            CalendarSession calendarSession = ImportExportServices.getCalendarService().init(getSession());
            applyParameters(calendarSession, optionalParameters);
            results = calendarSession.getCalendarService().importEvents(calendarSession, targetFolder.getID(), events);
        }
        return results;
    }

    /**
     * Extracts a possible truncation info based on the warnings present in the given calendar import.
     *
     * @param calendarImport The calendar import to extract a possible truncation info from
     * @return The truncation info, or <code>null</code> if nothing was truncated
     */
    private static TruncationInfo extractTruncationInfo(ImportedCalendar calendarImport) {
        for (OXException warning : calendarImport.getWarnings()) {
            if ("ICAL-0006".equals(warning.getErrorCode()) && null != warning.getLogArgs() && 1 < warning.getLogArgs().length && Integer.class.isInstance(warning.getLogArgs()[0]) && Integer.class.isInstance(warning.getLogArgs()[1])) {
                return new TruncationInfo(i((Integer) warning.getLogArgs()[0]), i((Integer) warning.getLogArgs()[1]));
            }
        }
        return null;
    }

    /**
     * Gets a list of import results as used by the iCalendar importer from a plain service result list.
     *
     * @param results The import results as received by the calendar service
     * @return The converted import results
     */
    private static List<ImportResult> getImportResults(List<com.openexchange.chronos.service.ImportResult> results) {
        if (null == results || results.isEmpty()) {
            return Collections.emptyList();
        }
        List<ImportResult> importResults = new ArrayList<ImportResult>(results.size());
        for (com.openexchange.chronos.service.ImportResult result : results) {
            importResults.add(getImportResult(result));
        }
        return importResults;
    }

    /**
     * Gets an import result as used by the iCalendar importer from a plain service result.
     *
     * @param result The import result as received by the calendar service
     * @return The converted import result
     */
    private static ImportResult getImportResult(com.openexchange.chronos.service.ImportResult result) {
        ImportResult importResult = new ImportResult();
        importResult.setEntryNumber(result.getIndex());
        importResult.setDate(new Date(result.getTimestamp()));
        importResult.setException(result.getError());
        importResult.addWarnings(getConversionWarnings(result.getIndex(), result.getWarnings()));
        if (null != result.getId()) {
            importResult.setFolder(result.getId().getFolderID());
            importResult.setObjectId(result.getId().getObjectID());
        } else if (false == importResult.hasError()) {
            importResult.setException(ImportExportExceptionCodes.COULD_NOT_CREATE.create());
        }
        if (false == importResult.hasError() && null != importResult.getWarnings() && 0 < importResult.getWarnings().size()) {
            importResult.setException(ImportExportExceptionCodes.WARNINGS.create(I(importResult.getWarnings().size())));
        }
        return importResult;
    }

    /**
     * Gets a list of conversion warnings as used in an import result from plain {@link OXException} warnings.
     *
     * @param index The imported component's index in the source
     * @param causes The exceptions to get the conversion warning for
     * @return The conversion warnings
     */
    private static List<ConversionWarning> getConversionWarnings(int index, List<OXException> causes) {
        if (null == causes || causes.isEmpty()) {
            return Collections.emptyList();
        }
        List<ConversionWarning> conversionWarnings = new ArrayList<ConversionWarning>(causes.size());
        for (OXException cause : causes) {
            if ("CAL-5070".equals(cause.getErrorCode())) {  // Data truncation [field %1$s, limit %2$d, current %3$d]
                conversionWarnings.add(new ConversionWarning(index, ConversionWarning.Code.TRUNCATION_WARNING, cause.getMessage()));
            } else {
                conversionWarnings.add(new ConversionWarning(index, cause));
            }
        }
        return conversionWarnings;
    }

    /**
     * Gets a value indicating whether the supplied folder has a unique account identifier set or not.
     *
     * @param folder The folder to check
     * @return <code>true</code> if the folder has a unique account identifier, <code>false</code>, otherwise
     */
    private static boolean hasAccountID(UserizedFolder folder) {
        return null != folder.getAccountID();
    }

    /**
     * Applies calendar parameters for the import operation, optionally taking into account client-supplied settings.
     *
     * @param parameters A calendar parameters reference to apply the parameters in
     * @param optionalParameters The optional parameters as sent from the client
     * @return The passed parameters reference
     */
    private static CalendarParameters applyParameters(CalendarParameters parameters, Map<String, String[]> optionalParameters) {
        parameters.set(CalendarParameters.PARAMETER_CHECK_CONFLICTS, Boolean.FALSE);
        if (null != optionalParameters) {
            if (optionalParameters.containsKey("suppressNotification")) {
                parameters.set(CalendarParameters.PARAMETER_NOTIFICATION, Boolean.FALSE);
            }
            String[] ignoreUIDsValue = optionalParameters.get("ignoreUIDs");
            if (null != ignoreUIDsValue && 0 < ignoreUIDsValue.length && Boolean.parseBoolean(ignoreUIDsValue[0])) {
                parameters.set(CalendarParameters.UID_CONFLICT_STRATEGY, UIDConflictStrategy.REASSIGN);
            }
        }
        parameters.set(CalendarSession.PARAMETER_SUPPRESS_ITIP, Boolean.TRUE);
        return parameters;
    }

    /**
     * Strips all attendees and the original organizer from all events in the given calendar.
     * 
     * @param calendar The calendar to strip the calendar users from
     */
    private void stripAttendeesAndOrganizer(ImportedCalendar calendar) {
        for (Event event : calendar.getEvents()) {
            if (event.containsAttendees()) {
                event.removeAttendees();
            }
            if (event.containsOrganizer()) {
                event.removeOrganizer();
            }
        }
    }
}
