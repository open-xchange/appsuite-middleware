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

package com.openexchange.chronos.impl;

import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.L;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.DefaultEventsResult;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.performer.ChangeExceptionsPerformer;
import com.openexchange.chronos.impl.performer.CountEventsPerformer;
import com.openexchange.chronos.impl.performer.ForeignEventsPerformer;
import com.openexchange.chronos.impl.performer.GetPerformer;
import com.openexchange.chronos.impl.performer.ResolveFilenamePerformer;
import com.openexchange.chronos.impl.performer.ResolveIdPerformer;
import com.openexchange.chronos.impl.performer.ResolveUidPerformer;
import com.openexchange.chronos.service.CalendarServiceUtilities;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.quota.Quota;

/**
 * {@link CalendarServiceUtilitiesImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarServiceUtilitiesImpl implements CalendarServiceUtilities {

    private static CalendarServiceUtilities instance = null;

    /**
     * Gets the calendar service utilities instance.
     *
     * @return The calendar service utilities
     */
    public static CalendarServiceUtilities getInstance() {
        if(instance == null){
            instance = new CalendarServiceUtilitiesImpl();
        }
        return instance;
    }

    /**
     * Initializes a new {@link CalendarServiceUtilitiesImpl}.
     */
    private CalendarServiceUtilitiesImpl() {
        super();
    }

    @Override
    public boolean containsForeignEvents(CalendarSession session, String folderId) throws OXException {
        return new InternalCalendarStorageOperation<Boolean>(session) {

            @Override
            protected Boolean execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return B(new ForeignEventsPerformer(session, storage).perform(folderId));
            }
        }.executeQuery().booleanValue();
    }

    @Override
    public long countEvents(CalendarSession session, String folderId) throws OXException {
        return new InternalCalendarStorageOperation<Long>(session) {

            @Override
            protected Long execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return L(new CountEventsPerformer(session, storage).perform(folderId));
            }
        }.executeQuery().intValue();
    }

    @Override
    public String resolveByUID(CalendarSession session, final String uid) throws OXException {
        return new InternalCalendarStorageOperation<String>(session) {

            @Override
            protected String execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ResolveUidPerformer(storage).perform(uid);
            }
        }.executeQuery();
    }

    @Override
    public String resolveByFilename(CalendarSession session, final String filename) throws OXException {
        return new InternalCalendarStorageOperation<String>(session) {

            @Override
            protected String execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ResolveFilenamePerformer(storage).perform(filename);
            }
        }.executeQuery();
    }

    @Override
    public Quota[] getQuotas(CalendarSession session) throws OXException {
        return new InternalCalendarStorageOperation<Quota[]>(session) {

            @Override
            protected Quota[] execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new Quota[] { Utils.getQuota(session, storage) };
            }
        }.executeQuery();
    }

    @Override
    public Event resolveByID(CalendarSession session, String id) throws OXException {
        return new InternalCalendarStorageOperation<Event>(session) {

            @Override
            protected Event execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ResolveIdPerformer(session, storage).perform(id);
            }
        }.executeQuery();
    }

    @Override
    public List<Event> resolveResource(CalendarSession session, String folderId, String resourceName) throws OXException {
        return new InternalCalendarStorageOperation<List<Event>>(session) {

            @Override
            protected List<Event> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                /*
                 * resolve by UID or filename
                 */
                String id = new ResolveUidPerformer(storage).perform(resourceName);
                if (null == id) {
                    id = new ResolveFilenamePerformer(storage).perform(resourceName);
                    if (null == id) {
                        return null;
                    }
                }
                return resolveEvent(session, storage, folderId, id).getEvents();
            }
        }.executeQuery();
    }

    @Override
    public Map<String, EventsResult> resolveResources(CalendarSession session, String folderId, List<String> resourceNames) throws OXException {
        return new InternalCalendarStorageOperation<Map<String, EventsResult>>(session) {

            @Override
            protected Map<String, EventsResult> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                Map<String, EventsResult> resultsPerResourceName = new HashMap<String, EventsResult>(resourceNames.size());
                /*
                 * batch-resolve by UID, falling back to filename as needed, and wrap into appropriate events results
                 */
                Map<String, String> eventIdsByResourceName = new ResolveUidPerformer(storage).perform(resourceNames);
                for (String resourceName : resourceNames) {
                    String id = eventIdsByResourceName.get(resourceName);
                    if (null == id) {
                        id = new ResolveFilenamePerformer(storage).perform(resourceName);
                        if (null == id) {
                            DefaultEventsResult result = new DefaultEventsResult(CalendarExceptionCodes.EVENT_NOT_FOUND_IN_FOLDER.create(folderId, resourceName));
                            resultsPerResourceName.put(resourceName, result);
                            continue;
                        }
                    }
                    resultsPerResourceName.put(resourceName, resolveEvent(session, storage, folderId, id));
                }
                return resultsPerResourceName;
            }
        }.executeQuery();
    }

    static EventsResult resolveEvent(CalendarSession session, CalendarStorage storage, String folderId, String id) {
        /*
         * get event & any overridden instances in folder
         */
        try {
            Event event = new GetPerformer(session, storage).perform(folderId, id, null);
            List<Event> events = new ArrayList<Event>();
            events.add(event);
            if (isSeriesMaster(event)) {
                events.addAll(new ChangeExceptionsPerformer(session, storage).perform(folderId, id));
            }
            return new DefaultEventsResult(events);
        } catch (OXException e) {
            if ("CAL-4041".equals(e.getErrorCode())) {
                /*
                 * "Event not found in folder..." -> try to load detached occurrences
                 */
                try {
                    List<Event> detachedOccurrences = new ChangeExceptionsPerformer(session, storage).perform(folderId, id);
                    if (0 < detachedOccurrences.size()) {
                        return new DefaultEventsResult(detachedOccurrences);
                    }
                } catch (OXException x) {
                    // ignore
                }
            }
            return new DefaultEventsResult(e);
        }
    }

}
