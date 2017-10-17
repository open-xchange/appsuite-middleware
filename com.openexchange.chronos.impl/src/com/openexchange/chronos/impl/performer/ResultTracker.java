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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.getEventID;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.chronos.impl.Utils.getPersonalFolderIds;
import static com.openexchange.chronos.impl.Utils.isInFolder;
import static com.openexchange.chronos.impl.Utils.isResolveOccurrences;
import static com.openexchange.chronos.impl.Utils.mapEventOccurrences;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.SelfProtectionFactory.SelfProtection;
import com.openexchange.chronos.impl.AbstractStorageOperation;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.InternalCalendarStorageOperation;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;

/**
 * {@link ResultTracker}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ResultTracker {

    private final CalendarStorage storage;
    private final CalendarSession session;
    private final UserizedFolder folder;
    private final long timestamp;
    private final InternalCalendarResult result;
    private final SelfProtection protection;

    /**
     * Initializes a new {@link ResultTracker}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @param timestamp The timestamp to apply for the result
     */
    public ResultTracker(CalendarStorage storage, CalendarSession session, UserizedFolder folder, long timestamp, SelfProtection protection) throws OXException {
        super();
        this.storage = storage;
        this.session = session;
        this.folder = folder;
        this.timestamp = timestamp;
        this.result = new InternalCalendarResult(session, getCalendarUserId(folder), folder);
        this.protection = protection;
    }

    /**
     * Gets the calendar result.
     *
     * @return The calendar result
     */
    public InternalCalendarResult getResult() {
        return result;
    }

    /**
     * Tracks suitable results for a created event in the current internal calendar result, which includes adding a <i>plain</i> creation
     * for the new event data, as well as an appropriate creation from the acting user's point of view.
     *
     * @param createdEvent The created event
     */
    public void trackCreation(Event createdEvent) throws OXException {
        /*
         * track affected folders and add 'plain' event creation
         */
        result.addAffectedFolderIds(folder.getID(), getPersonalFolderIds(createdEvent.getAttendees()));
        result.addPlainCreation(createdEvent);
        if (isInFolder(createdEvent, folder)) {
            /*
             * prepare 'userized' version of created event & expand event series if needed prior adding results
             */
            if (isSeriesMaster(createdEvent) && isResolveOccurrences(session)) {
                result.addUserizedCreations(resolveOccurrences(userize(createdEvent)));
            } else {
                result.addUserizedCreation(userize(createdEvent));
            }
        } else {
            /*
             * possible for a new change exception w/o the calendar user attending
             */
            result.addUserizedDeletion(timestamp, new EventID(folder.getID(), createdEvent.getId(), createdEvent.getRecurrenceId()));
        }
    }

    /**
     * Tracks suitable results for an updated event in the current internal calendar result, which includes adding a <i>plain</i> update
     * for the modified event data, as well as an update, deletion or creation from the acting user's point of view.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     */
    public void trackUpdate(Event originalEvent, Event updatedEvent) throws OXException {
        /*
         * track affected folders and add a 'plain' event update
         */
        result.addAffectedFolderIds(folder.getID(), getPersonalFolderIds(originalEvent.getAttendees()), getPersonalFolderIds(updatedEvent.getAttendees()));
        result.addPlainUpdate(originalEvent, updatedEvent);
        /*
         * check whether original and updated event are visible in actual folder view & add corresponding result
         */
        if (isInFolder(originalEvent, folder)) {
            if (isInFolder(updatedEvent, folder)) {
                /*
                 * "update" from calendar user's point of view
                 */
                if (isResolveOccurrences(session) && (isSeriesMaster(originalEvent) || isSeriesMaster(updatedEvent))) {
                    if (isSeriesMaster(originalEvent) && isSeriesMaster(updatedEvent)) {
                        for (Entry<Event, Event> entry : mapEventOccurrences(resolveOriginalUserizedOccurrences(originalEvent), resolveOccurrences(userize(updatedEvent)))) {
                            if (null == entry.getKey()) {
                                result.addUserizedCreation(entry.getValue());
                            } else if (null == entry.getValue()) {
                                result.addUserizedDeletion(timestamp, new EventID(folder.getID(), entry.getKey().getId(), entry.getKey().getRecurrenceId()));
                            } else {
                                result.addUserizedUpdate(entry.getKey(), entry.getValue());
                            }
                        }
                    } else if (isSeriesMaster(originalEvent)) {
                        for (Event originalOccurrence : resolveOriginalUserizedOccurrences(originalEvent)) {
                            result.addUserizedDeletion(timestamp, new EventID(folder.getID(), originalOccurrence.getId(), originalOccurrence.getRecurrenceId()));
                        }
                        result.addUserizedDeletion(timestamp, new EventID(folder.getID(), originalEvent.getId(), originalEvent.getRecurrenceId()));
                        result.addUserizedCreation(userize(updatedEvent));
                    } else if (isSeriesMaster(updatedEvent)) {
                        result.addUserizedDeletion(timestamp, new EventID(folder.getID(), originalEvent.getId(), originalEvent.getRecurrenceId()));
                        result.addUserizedCreations(resolveOccurrences(userize(updatedEvent)));
                    }
                } else {
                    result.addUserizedUpdate(userize(originalEvent), userize(updatedEvent));
                }
            } else {
                /*
                 * "delete" from calendar user's point of view
                 */
                if (isSeriesMaster(originalEvent) && isResolveOccurrences(session)) {
                    for (Event originalOccurrence : resolveOriginalUserizedOccurrences(originalEvent)) {
                        result.addUserizedDeletion(timestamp, new EventID(folder.getID(), originalOccurrence.getId(), originalOccurrence.getRecurrenceId()));
                    }
                } else {
                    result.addUserizedDeletion(timestamp, new EventID(folder.getID(), originalEvent.getId(), originalEvent.getRecurrenceId()));
                }
            }
        } else if (isInFolder(updatedEvent, folder)) {
            /*
             * "create" from calendar user's point of view - possible for attendee being added to an existing event, so that it shows up
             * in the new attendee's folder afterwards (after #needsExistenceCheckInTargetFolder() was false)
             */
            if (isSeriesMaster(updatedEvent) && isResolveOccurrences(session)) {
                result.addUserizedCreations(resolveOccurrences(userize(updatedEvent)));
            } else {
                result.addUserizedCreation(userize(updatedEvent));
            }
        }
    }

    /**
     * Tracks suitable results for a deleted event in the current internal calendar result, which includes adding a <i>plain</i> deletion
     * for the removed event data, as well as an appropriate deletion from the acting user's point of view.
     *
     * @param deletedEvent The deleted event
     */
    public void trackDeletion(Event deletedEvent) throws OXException {
        result.addAffectedFolderIds(folder.getID(), getPersonalFolderIds(deletedEvent.getAttendees()));
        result.addPlainDeletion(timestamp, getEventID(deletedEvent));
        if (isSeriesMaster(deletedEvent) && isResolveOccurrences(session)) {
            for (Event deletedOccurrence : resolveOccurrences(userize(deletedEvent))) {
                result.addUserizedDeletion(timestamp, new EventID(folder.getID(), deletedOccurrence.getId(), deletedOccurrence.getRecurrenceId()));
            }
        } else {
            result.addUserizedDeletion(timestamp, new EventID(folder.getID(), deletedEvent.getId(), deletedEvent.getRecurrenceId()));
        }
    }

    private List<Event> resolveOccurrences(Event master) throws OXException {
        Iterator<Event> iterator = Utils.resolveOccurrences(storage, session, master);

        List<Event> list = new ArrayList<Event>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
            protection.checkEventCollection(list);
        }
        return list;
    }

    private List<Event> resolveOriginalUserizedOccurrences(Event masterEvent) throws OXException {
        Connection oldConnection = session.get(AbstractStorageOperation.PARAM_CONNECTION, Connection.class);
        session.set(AbstractStorageOperation.PARAM_CONNECTION, null);
        try {
            return new InternalCalendarStorageOperation<List<Event>>(session) {

                @Override
                protected List<Event> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                    Event userizedMasterEvent = Utils.userize(session, storage, masterEvent, getCalendarUserId(folder));
                    return Utils.asList(Utils.resolveOccurrences(storage, session, userizedMasterEvent));
                }
            }.executeQuery();
        } finally {
            session.set(AbstractStorageOperation.PARAM_CONNECTION, oldConnection);
        }
    }

    private Event userize(Event event) throws OXException {
        return Utils.userize(session, storage, event, getCalendarUserId(folder));
    }

}
