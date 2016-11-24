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

import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.appendCommonTerms;
import static com.openexchange.chronos.impl.Utils.getCalendarUser;
import static com.openexchange.chronos.impl.Utils.getFields;
import static com.openexchange.chronos.impl.Utils.getFolderIdTerm;
import static com.openexchange.chronos.impl.Utils.getFrom;
import static com.openexchange.chronos.impl.Utils.getSearchTerm;
import static com.openexchange.chronos.impl.Utils.getUntil;
import static com.openexchange.chronos.impl.Utils.isIncludePrivate;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.folderstorage.Permission.READ_OWN_OBJECTS;
import static com.openexchange.java.Autoboxing.I;
import java.util.Date;
import java.util.List;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SortOptions;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.chronos.service.UserizedEvent;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link UpdatesPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UpdatesPerformer extends AbstractQueryPerformer {

    /**
     * Initializes a new {@link UpdatesPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public UpdatesPerformer(CalendarSession session, CalendarStorage storage) throws OXException {
        super(session, storage);
    }

    /**
     * Performs the operation.
     *
     * @param since The date since when the updates should be collected
     * @return The update result holding the new, modified and deleted events as requested
     */
    public UpdatesResult perform(Date since) throws OXException {
        /*
         * construct search term
         */
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, I(session.getUser().getId())));
        appendCommonTerms(searchTerm, getFrom(session), getUntil(session), since);
        /*
         * perform search & userize the results for the current session's user
         */
        String[] ignore = session.get(CalendarParameters.PARAMETER_IGNORE, String[].class);
        EventField[] fields = getFields(session, EventField.ATTENDEES);
        List<UserizedEvent> newAndModifiedEvents = null;
        if (false == com.openexchange.tools.arrays.Arrays.contains(ignore, "changed")) {
            List<Event> events = storage.getEventStorage().searchEvents(searchTerm, new SortOptions(session), fields);
            readAdditionalEventData(events, session.getUser().getId(), fields);
            newAndModifiedEvents = userize(events, session.getUser().getId(), isIncludePrivate(session));
        }
        List<UserizedEvent> deletedEvents = null;
        if (false == com.openexchange.tools.arrays.Arrays.contains(ignore, "deleted")) {
            List<Event> events = storage.getEventStorage().searchDeletedEvents(searchTerm, new SortOptions(session), fields);
            readAdditionalEventData(events, session.getUser().getId(), fields);
            deletedEvents = userize(events, session.getUser().getId(), isIncludePrivate(session));
        }
        return getResult(newAndModifiedEvents, deletedEvents);
    }

    /**
     * Performs the operation.
     *
     * @param folder The parent folder to get the event from
     * @param since The date since when the updates should be collected
     * @return The update result holding the new, modified and deleted events as requested
     */
    public UpdatesResult perform(UserizedFolder folder, Date since) throws OXException {
        requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
        /*
         * construct search term
         */
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND).addSearchTerm(getFolderIdTerm(folder));
        appendCommonTerms(searchTerm, getFrom(session), getUntil(session), since);
        /*
         * perform search & userize the results
         */
        String[] ignore = session.get(CalendarParameters.PARAMETER_IGNORE, String[].class);
        EventField[] fields = getFields(session);
        List<UserizedEvent> newAndModifiedEvents = null;
        if (false == com.openexchange.tools.arrays.Arrays.contains(ignore, "changed")) {
            List<Event> events = storage.getEventStorage().searchEvents(searchTerm, new SortOptions(session), fields);
            readAdditionalEventData(events, getCalendarUser(folder).getId(), fields);
            newAndModifiedEvents = userize(events, folder, true);
        }
        List<UserizedEvent> deletedEvents = null;
        if (false == com.openexchange.tools.arrays.Arrays.contains(ignore, "deleted")) {
            List<Event> events = storage.getEventStorage().searchDeletedEvents(searchTerm, new SortOptions(session), fields);
            readAdditionalEventData(events, getCalendarUser(folder).getId(), fields);
            deletedEvents = userize(events, folder, true);
        }
        return getResult(newAndModifiedEvents, deletedEvents);
    }

    private static UpdatesResult getResult(final List<UserizedEvent> newAndModifiedEvents, final List<UserizedEvent> deletedEvents) {
        return new UpdatesResult() {

            @Override
            public List<UserizedEvent> getNewAndModifiedEvents() {
                return newAndModifiedEvents;
            }

            @Override
            public List<UserizedEvent> getDeletedEvents() {
                return deletedEvents;
            }
        };
    }

}
