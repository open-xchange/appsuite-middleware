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
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Strings;
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

    abstract public CalendarResult createEvent(String folderId, Event event) throws OXException ;

    abstract public void updateEvent(EventID eventId, Event event) throws OXException ;

    abstract public void writeResult();

    @Override
    public TruncationInfo importData(UserizedFolder userizedFolder, InputStream is, List<ImportResult> list, Map<String, String[]> optionalParams, Map<String, String> uidReplacements) throws OXException {
        ICalService iCalService= ImportExportServices.getICalService();
        ICalParameters iCalParameters = iCalService.initParameters();
        ImportedCalendar importedCalendar = iCalService.importICal(is, iCalParameters);
        return importEvents(userizedFolder, importedCalendar.getEvents(), optionalParams, uidReplacements);
    }

    private TruncationInfo importEvents(UserizedFolder userizedFolder, List<Event> events, Map<String, String[]> optionalParams, Map<String, String> uidReplacements) {
        boolean ignoreUIDs = isIgnoreUIDs(optionalParams);
        events = sortEvents(events);
        final Map<Integer, Integer> pos2Master = determineMasterEvents(events);
        final Map<Integer, String> master2id = new HashMap<>();
        int index = 0;
        try {
            for (Event event : events) {
                Event eventToImport = handleUIDReplacement(event, ignoreUIDs, uidReplacements);
                final boolean isMaster = event.containsUid() && !pos2Master.containsKey(index);
                final boolean isChange = event.containsUid() && pos2Master.containsKey(index);
                if (isChange) {
                    final Integer masterPos = pos2Master.get(index);
                    final String masterID = master2id.get(masterPos.intValue());
                    if (masterID == null) {
                        continue;
                    } else {
                        updateEvent(new EventID(userizedFolder.getID(), masterID), eventToImport);
                    }
                } else {
                    CalendarResult result = createEvent(userizedFolder.getID(), eventToImport);
                    if(isMaster && !Strings.isEmpty(result.getCreations().get(0).getCreatedEvent().getId())) {
                        master2id.put(index, result.getCreations().get(0).getCreatedEvent().getId());
                    }
                }
                //TODO
//              writeImportResult(list, importResult, eventToImport, userizedFolder.getID(), index);
                index++;
            }
        } catch (OXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new TruncationInfo(index, events.size());
    }

    public List<Event> sortEvents(List<Event> events) {
        return CalendarUtils.sortSeriesMasterFirst(events);
    }

    public Event handleUIDReplacement(Event event, boolean ignoreUIDs, Map<String, String> uidReplacements) {
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

}
