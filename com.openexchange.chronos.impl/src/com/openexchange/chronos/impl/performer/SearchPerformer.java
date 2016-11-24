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
import static com.openexchange.chronos.impl.Utils.getCalendarUser;
import static com.openexchange.chronos.impl.Utils.getFields;
import static com.openexchange.chronos.impl.Utils.getFolder;
import static com.openexchange.chronos.impl.Utils.getFolderIdTerm;
import static com.openexchange.chronos.impl.Utils.getSearchTerm;
import static com.openexchange.chronos.impl.Utils.getVisibleFolders;
import static com.openexchange.chronos.impl.Utils.isIncludePrivate;
import static com.openexchange.chronos.impl.Utils.sort;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.folderstorage.Permission.READ_OWN_OBJECTS;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SortOptions;
import com.openexchange.chronos.service.UserizedEvent;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link SearchPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class SearchPerformer extends AbstractQueryPerformer {

    /**
     * Initializes a new {@link SearchPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public SearchPerformer(CalendarSession session, CalendarStorage storage) throws OXException {
        super(session, storage);
    }

    /**
     * Performs the operation.
     *
     * @param folderIDs The identifiers of the folders to perform the search in, or <code>null</code> to search all visible folders
     * @return The found events
     */
    public List<UserizedEvent> perform(int[] folderIDs, String pattern) throws OXException {
        List<UserizedFolder> folders;
        if (null == folderIDs || 0 == folderIDs.length) {
            folders = getVisibleFolders(session);
        } else {
            folders = new ArrayList<UserizedFolder>(folderIDs.length);
            for (int folderID : folderIDs) {
                folders.add(getFolder(session, folderID));
            }
        }
        List<UserizedEvent> userizedEvents = new ArrayList<UserizedEvent>();
        for (UserizedFolder folder : folders) {
            userizedEvents.addAll(searchEvents(folder, pattern));
        }
        return sort(userizedEvents, new SortOptions(session));
    }

    private List<UserizedEvent> searchEvents(UserizedFolder folder, String pattern) throws OXException {
        requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
        Check.requireMinimumSearchPatternLength(pattern);
        String wildcardPattern = pattern.startsWith("*") ? pattern : '*' + pattern;
        wildcardPattern = wildcardPattern.endsWith("*") ? wildcardPattern : wildcardPattern + '*';
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getFolderIdTerm(folder))
            .addSearchTerm(new CompositeSearchTerm(CompositeOperation.OR)
                .addSearchTerm(getSearchTerm(EventField.SUMMARY, SingleOperation.EQUALS, wildcardPattern))
                .addSearchTerm(getSearchTerm(EventField.DESCRIPTION, SingleOperation.EQUALS, wildcardPattern))
                .addSearchTerm(getSearchTerm(EventField.CATEGORIES, SingleOperation.EQUALS, wildcardPattern))
            )
        ;
        EventField[] fields = getFields(session);
        List<Event> events = storage.getEventStorage().searchEvents(searchTerm, new SortOptions(session), fields);
        readAdditionalEventData(events, getCalendarUser(folder).getId(), fields);
        return userize(events, folder, isIncludePrivate(session));
    }

}
