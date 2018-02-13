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

package com.openexchange.chronos.impl.groupware;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.DeleteResultImpl;
import com.openexchange.chronos.common.UpdateResultImpl;
import com.openexchange.chronos.impl.DefaultCalendarEvent;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link SimpleResultTracker} - Tracks update and delete operations on calendar events. This tracker
 * is only meant to be used by the {@link CalendarDeleteListener}. For other purposes use {@link com.openexchange.chronos.impl.performer.ResultTracker}.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
class SimpleResultTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleResultTracker.class);

    private List<UpdateResult>          updateResults;
    private List<DeleteResult>          deleteResults;
    private Set<String>                 affectedFolders;
    private Set<CalendarHandler> calendarHandlers;

    /**
     * Initializes a new {@link SimpleResultTracker}.
     *
     * @param calendarHandlers The {@link CalendarHandler}s to notify
     */
    public SimpleResultTracker(Set<CalendarHandler> calendarHandlers) {
        super();
        this.updateResults = new LinkedList<>();
        this.deleteResults = new LinkedList<>();
        this.affectedFolders = new HashSet<>();
        this.calendarHandlers = calendarHandlers;
    }

    /**
     * Creates a new {@link CalendarEvent} and calls {@link CalendarHandler#handle(CalendarEvent)}
     *
     * @param connection The {@link Connection}
     * @param context The {@link Context}
     * @param session The {@link Session} of the context admin
     * @param entityResolver The {@link EntityResolver}
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     */
    public void notifyCalenderHandlers(Connection connection, Context context, Session session, EntityResolver entityResolver, CalendarParameters parameters) {
        Map<Integer, List<String>> affectedFoldersPerUser = getAffectedFoldersPerUser(context, connection, entityResolver);
        if (false == affectedFoldersPerUser.isEmpty()) {
            DefaultCalendarEvent calendarEvent = new DefaultCalendarEvent(context.getContextId(), CalendarAccount.DEFAULT_ACCOUNT.getAccountId(), -1, session, affectedFoldersPerUser, Collections.emptyList(), updateResults, deleteResults, parameters);
            for (CalendarHandler handler : calendarHandlers) {
                handler.handle(calendarEvent);
            }
        }
    }

    /**
     * Add a deleted event as appropriated {@link CalendarEvent}.
     *
     * @param event The {@link Event} to delete
     * @param timestamp The timestamp of the deletion
     */
    public void addDelete(Event event, long timestamp) {
        addFolders(event);
        DeleteResult newResult = new DeleteResultImpl(timestamp, event);
        deleteResults.add(newResult);
    }

    /**
     * Add an updated event as appropriated {@link CalendarEvent}.
     *
     * @param originalEvent The original {@link Event}
     * @param updatedEvent The updated {@link Event}
     * @throws OXException See {@link UpdateResultImpl#UpdateResultImpl(Event, Event)}
     */
    public void addUpdate(Event originalEvent, Event updatedEvent) throws OXException {
        addFolders(updatedEvent);
        UpdateResult newResult = new UpdateResultImpl(originalEvent, updatedEvent);
        updateResults.add(newResult);
    }

    /**
     * Track the affected folders.
     *
     * @param event The {@link Event}
     */
    private void addFolders(Event event) {
        affectedFolders.addAll(Utils.getPersonalFolderIds(event.getAttendees()));
        if (null != event.getFolderId()) {
            affectedFolders.add(event.getFolderId());
        }
    }

    /**
     * Get the affected folders per user
     *
     * @param context The {@link Context}
     * @param connection The {@link Connection}
     * @param entityResolver The {@link EntityResolver}
     * @return A {@link Map} containing changed folders per user
     */
    private Map<Integer, List<String>> getAffectedFoldersPerUser(Context context, Connection connection, EntityResolver entityResolver) {
        Map<Integer, List<String>> affectedFoldersPerUser = new HashMap<Integer, List<String>>();
        OXFolderAccess folderAccess = new OXFolderAccess(connection, context);
        for (String folderId : affectedFolders) {
            try {
                FolderObject folder = folderAccess.getFolderObject(Integer.valueOf(folderId).intValue());
                Set<Integer> affectedUsers = Utils.getAffectedUsers(folder, entityResolver);
                for (Integer userId : affectedUsers) {
                    com.openexchange.tools.arrays.Collections.put(affectedFoldersPerUser, userId, folderId);
                }
            } catch (NumberFormatException e) {
                LOGGER.debug("Could not convert id {} to an Integer", e, folderId);
            } catch (OXException e) {
                LOGGER.debug("Could not get folder with id {}", e, folderId);
            }
        }
        return affectedFoldersPerUser;
    }
}
