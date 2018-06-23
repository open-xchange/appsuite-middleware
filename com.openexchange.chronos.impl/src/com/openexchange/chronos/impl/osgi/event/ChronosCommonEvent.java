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

package com.openexchange.chronos.impl.osgi.event;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.session.Session;

/**
 * {@link ChronosCommonEvent} - The {@link CommonEvent} to thrown on {@link Event} changes
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ChronosCommonEvent implements CommonEvent {

    /** The logger of this class */
    private final static Logger LOGGER = LoggerFactory.getLogger(ChronosCommonEvent.class);

    private final Session session;
    private final int actionID;
    private final Event actionEvent;
    private final Event oldEvent;
    private final Map<Integer, Set<Integer>> affectedUsersWithFolders;

    /**
     *
     * Initializes a new {@link ChronosCommonEvent}.
     *
     * @param calendarSession The calendar session
     * @param actionID The actionID propagate by this event
     * @param actionEvent The new or updated {@link Event}
     */
    public ChronosCommonEvent(CalendarSession calendarSession, int actionID, Event actionEvent) {
        this(calendarSession, actionID, actionEvent, null);
    }

    /**
     *
     * Initializes a new {@link ChronosCommonEvent}.
     *
     * @param calendarSession The calendar session
     * @param actionID The actionID propagate by this event
     * @param actionEvent The new or updated {@link Event}
     * @param oldEvent The old {@link Event} if an update was made, else <code>null</code>
     */
    public ChronosCommonEvent(CalendarSession calendarSession, int actionID, Event actionEvent, Event oldEvent) {
        super();
        this.session = calendarSession.getSession();
        this.actionID = actionID;
        this.actionEvent = actionEvent;
        this.oldEvent = oldEvent;
        this.affectedUsersWithFolders = getAffectedFoldersPerUser(calendarSession, actionEvent, oldEvent);
    }

    @Override
    public int getContextId() {
        return session.getContextId();
    }

    @Override
    public int getUserId() {
        return session.getUserId();
    }

    @Override
    public int getModule() {
        return Types.APPOINTMENT;
    }

    @Override
    public Object getActionObj() {
        return actionEvent;
    }

    @Override
    public Object getOldObj() {
        return oldEvent;
    }

    /**
     * Get the old {@link Event} in case of an update
     *
     * @return The old event or <code>null</code>
     */
    public Event getOldEvent() {
        return oldEvent;
    }

    @Override
    public Object getSourceFolder() {
        return null == oldEvent ? null : oldEvent.getFolderId();
    }

    @Override
    public Object getDestinationFolder() {
        return null == actionEvent ? null : actionEvent.getFolderId();
    }

    @Override
    public int getAction() {
        return actionID;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public Map<Integer, Set<Integer>> getAffectedUsersWithFolder() {
        return affectedUsersWithFolders;
    }

    private Map<Integer, Set<Integer>> getAffectedFoldersPerUser(CalendarSession session, Event actionEvent, Event oldEvent) {
        Set<String> folderIds = new HashSet<String>();
        if (null != actionEvent) {
            folderIds.addAll(Utils.getPersonalFolderIds(actionEvent.getAttendees()));
            if (null != actionEvent.getFolderId()) {
                folderIds.add(actionEvent.getFolderId());
            }
        }
        if (null != oldEvent) {
            folderIds.addAll(Utils.getPersonalFolderIds(oldEvent.getAttendees()));
            if (null != oldEvent.getFolderId()) {
                folderIds.add(oldEvent.getFolderId());
            }
        }
        try {
            return getAffected(Utils.getAffectedFoldersPerUser(session, folderIds));
        } catch (OXException e) {
            LOGGER.error("Error deriving affected folders per user", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Converts the output from {@link CalendarEvent#getAffectedFoldersPerUser()} to a map that can be used for {@link CommonEvent#getAffectedUsersWithFolder()}
     *
     * @param affectedUsersWithFolders Output from {@link CalendarEvent#getAffectedFoldersPerUser()}
     * @return A {@link Map} with converted value to a {@link Set} of {@link Integer}s
     */
    private Map<Integer, Set<Integer>> getAffected(Map<Integer, List<String>> affectedUsersWithFolders) {
        Map<Integer, Set<Integer>> retval = new LinkedHashMap<Integer, Set<Integer>>(affectedUsersWithFolders.size());
        for (Map.Entry<Integer, List<String>> entry : affectedUsersWithFolders.entrySet()) {
            // Convert for each user
            List<String> folderIds = entry.getValue();
            Set<Integer> folders = new HashSet<>(folderIds.size());
            for (String folderId : folderIds) {
                try {
                    // Multiple folder get silently dropped
                    folders.add(Integer.valueOf(folderId));
                } catch (NumberFormatException e) {
                    LOGGER.error("Can't parse folder with ID {}. The folder won't be part of the OSGi event to be propagated.", folderId, e);
                }
            }
            // Add the user-folder-pair
            Integer user = entry.getKey();
            retval.put(user, folders);
        }
        return retval;
    }

}
