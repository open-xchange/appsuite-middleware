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

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.data.conversion.ical.TruncationInfo;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.ProblematicAttribute;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Strings;
import com.openexchange.tools.exceptions.SimpleTruncatedAttribute;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractICalEventImporter}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public abstract class AbstractICalEventImporter extends AbstractICalImporter{

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
     * @return CalendarResult The result of the event creation
     * @throws OXException if event creation fails
     */
    abstract protected CalendarResult updateEvent(EventID eventId, Event event) throws OXException ;

    @Override
    public TruncationInfo importData(UserizedFolder userizedFolder, InputStream is, List<ImportResult> list, Map<String, String[]> optionalParams) throws OXException {
        ICalService iCalService= ImportExportServices.getICalService();
        ICalParameters iCalParameters = iCalService.initParameters();
        iCalParameters.set(ICalParameters.SANITIZE_INPUT, Boolean.TRUE);
        ImportedCalendar importedCalendar = iCalService.importICal(is, iCalParameters);
        return importEvents(userizedFolder, importedCalendar.getEvents(), optionalParams, list);
    }

    private TruncationInfo importEvents(UserizedFolder userizedFolder, List<Event> events, Map<String, String[]> optionalParams, List<ImportResult> list) {
        boolean ignoreUIDs = isIgnoreUIDs(optionalParams);
        Map<String, String> uidReplacements = ignoreUIDs ? new HashMap<>() : null;
        events = sortEvents(events);
        final Map<Integer, Integer> pos2Master = determineMasterEvents(events);
        final Map<Integer, String> master2id = new HashMap<>();
        int index = 0;
        CalendarResult result;
        for (Event event : events) {
            final ImportResult importResult = new ImportResult();
            try {
                Event eventToImport = handleUIDReplacement(event, ignoreUIDs, uidReplacements);
                final boolean isMaster = event.containsUid() && !pos2Master.containsKey(index);
                final boolean isChange = event.containsUid() && pos2Master.containsKey(index);
                if (isChange) {
                    final Integer masterPos = pos2Master.get(index);
                    final String masterID = master2id.get(masterPos.intValue());
                    if (masterID == null) {
                        continue;
                    } else {
                        result = updateEvent(new EventID(userizedFolder.getID(), masterID), eventToImport);
                    }
                } else {
                    result = createEvent(userizedFolder.getID(), eventToImport);
                    if(isMaster && !Strings.isEmpty(result.getCreations().get(0).getCreatedEvent().getId())) {
                        master2id.put(index, result.getCreations().get(0).getCreatedEvent().getId());
                    }
                }
                writeResult(importResult, result, userizedFolder.getID());
            } catch (OXException e) {
                OXException ne = makeMoreInformative(e);
                importResult.setException(ne);
            }
            importResult.setEntryNumber(index);
            list.add(importResult);
            index++;
        }
        return new TruncationInfo(index, events.size());
    }

    private void writeResult(ImportResult importResult, CalendarResult result, String folderId) {
        if (null!= result) {
            importResult.setObjectId(result.getCreations().get(0).getCreatedEvent().getId());
            importResult.setDate(result.getCreations().get(0).getCreatedEvent().getLastModified());
            importResult.setFolder(folderId);
        }
    }

    private List<Event> sortEvents(List<Event> events) {
        return CalendarUtils.sortSeriesMasterFirst(events);
    }

    private Event handleUIDReplacement(Event event, boolean ignoreUIDs, Map<String, String> uidReplacements) {
        if (ignoreUIDs && event.containsUid()) {
            // perform fixed UID replacement to keep recurring appointment relations
            String originalUID = event.getUid();
            String replacedUID = uidReplacements.get(originalUID);
            if (null == replacedUID) {
                replacedUID = UUID.randomUUID().toString();
                uidReplacements.put(originalUID, replacedUID);
            }
            event.setUid(replacedUID);
        }
        return event;
    }

    private Map<Integer, Integer> determineMasterEvents(final List<Event> events) {
        final Map<String, Integer> uid2master = new HashMap<>();
        final Map<Integer, Integer> map = new HashMap<>();
        Event app;
        //find master
        for(int pos = 0, len = events.size(); pos < len; pos++){
            app = events.get(pos);
            if(! app.containsUid()) {
                continue;
            }
            final String uid = app.getUid();
            if(! uid2master.containsKey(uid)) {
                uid2master.put(uid, pos);
            }
        }
        //references to master
        for(int pos = 0, len = events.size(); pos < len; pos++){
            app = events.get(pos);
            if(! app.containsUid()) {
                continue;
            }

            final String uid = app.getUid();
            final Integer masterPos = uid2master.get(uid);

            if(pos > masterPos) {
                map.put(pos, uid2master.get(uid));
            }
        }
        return map;
    }

    private OXException makeMoreInformative(OXException e) {
        if( e.getCategory() == Category.CATEGORY_TRUNCATED){
            ProblematicAttribute[] problematics = e.getProblematics();
            StringBuilder bob = new StringBuilder();
            for(ProblematicAttribute att: problematics){
                if((att instanceof SimpleTruncatedAttribute) && ((SimpleTruncatedAttribute) att).getValue() != null){
                    SimpleTruncatedAttribute temp = (SimpleTruncatedAttribute) att;
                    bob.append(temp.getValue());
                    bob.append(" (>");
                    bob.append(temp.getMaxSize());
                    bob.append(")\n");
                }
            }
            OXException exception = ImportExportExceptionCodes.TRUNCATION.create(bob.toString());
            for (ProblematicAttribute problematic : problematics) {
                exception.addProblematic(problematic);
            }
            return exception;
        }
        return e;
    }

}
