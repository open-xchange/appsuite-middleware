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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CreateResultImpl;
import com.openexchange.chronos.common.DefaultCalendarResult;
import com.openexchange.chronos.common.DeleteResultImpl;
import com.openexchange.chronos.common.UpdateResultImpl;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;

/**
 * {@link InternalCalendarResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class InternalCalendarResult {

    private final CalendarSession session;
    private final int calendarUserId;
    private final Set<String> affectedFolderIds;
    private final CalendarFolder folder;

    private List<CreateResult> creations;
    private List<CreateResult> userizedCreations;
    private List<UpdateResult> updates;
    private List<UpdateResult> userizedUpdates;
    private List<DeleteResult> deletions;
    private List<DeleteResult> userizedDeletions;

    /**
     * Initializes a new {@link InternalCalendarResult}.
     *
     * @param session The calendar session
     * @param calendarUserId The actual calendar user
     * @param folder The calendar folder representing the current view on the events
     */
    public InternalCalendarResult(CalendarSession session, int calendarUserId, CalendarFolder folder) {
        super();
        this.session = session;
        this.calendarUserId = calendarUserId;
        this.folder = folder;
        this.affectedFolderIds = new HashSet<String>();
    }

    public CalendarSession getSession() {
        return session;
    }

    public int getCalendarUserId() {
        return calendarUserId;
    }

    public CalendarFolder getFolder() {
        return folder;
    }

    /**
     * Adds identifiers of folders that are affected by the result.
     *
     * @param folderId A single folder identifier to add, or <code>null</code> to ignore
     * @param folderIds A collection of folder identifiers to add, or <code>null</code> to ignore
     */
    public void addAffectedFolderIds(String folderId, Collection<? extends String> folderIds) {
        addAffectedFolderIds(folderId, folderIds, null);
    }

    /**
     * Adds identifiers of folders that are affected by the result.
     *
     * @param folderId A single folder identifier to add, or <code>null</code> to ignore
     * @param folderIds A collection of folder identifiers to add, or <code>null</code> to ignore
     * @param otherFolderIds A collection of folder identifiers to add, or <code>null</code> to ignore
     */
    public void addAffectedFolderIds(String folderId, Collection<? extends String> folderIds, Collection<? extends String> otherFolderIds) {
        if (null != folderId) {
            affectedFolderIds.add(folderId);
        }
        if (null != folderIds) {
            affectedFolderIds.addAll(folderIds);
        }
        if (null != otherFolderIds) {
            affectedFolderIds.addAll(otherFolderIds);
        }
    }

    /**
     * Adds a plain/vanilla deletion to this calendar result.
     *
     * @param timestamp The timestamp
     * @param event The original event
     * @return A self reference
     */
    public InternalCalendarResult addPlainDeletion(long timestamp, Event event) {
        /*
         * merge with existing create result for same event if already contained
         */
        CreateResult existingCreation = findCreation(creations, event.getFolderId(), event.getId(), event.getRecurrenceId());
        if (null != existingCreation && creations.remove(existingCreation)) {
            return this;
        }
        /*
         * merge with existing update result for same event if already contained, or treat as new delete result, otherwise
         */
        UpdateResult existingUpdate = findUpdate(updates, event.getFolderId(), event.getId(), event.getRecurrenceId());
        if (null != existingUpdate && updates.remove(existingUpdate)) {
            event = existingUpdate.getOriginal();
        }
        if (null == deletions) {
            deletions = new ArrayList<DeleteResult>();
        }
        deletions.add(new DeleteResultImpl(timestamp, event));
        return this;
    }

    /**
     * Adds a <i>userized</i> deletion to this calendar result.
     *
     * @param timestamp The timestamp
     * @param event The original event
     * @return A self reference
     */
    public InternalCalendarResult addUserizedDeletion(long timestamp, Event event) {
        /*
         * merge with existing create result for same event if already contained
         */
        CreateResult existingCreation = findCreation(userizedCreations, event.getFolderId(), event.getId(), event.getRecurrenceId());
        if (null != existingCreation && userizedCreations.remove(existingCreation)) {
            return this;
        }
        /*
         * merge with existing update result for same event if already contained, or treat as new delete result, otherwise
         */
        UpdateResult existingUpdate = findUpdate(userizedUpdates, event.getFolderId(), event.getId(), event.getRecurrenceId());
        if (null != existingUpdate && userizedUpdates.remove(existingUpdate)) {
            event = existingUpdate.getOriginal();
        }
        if (null == userizedDeletions) {
            userizedDeletions = new ArrayList<DeleteResult>();
        }
        userizedDeletions.add(new DeleteResultImpl(timestamp, event));
        return this;
    }

    /**
     * Adds a plain/vanilla creation to this calendar result.
     *
     * @param createdEvent The created event
     * @return A self reference
     */
    public InternalCalendarResult addPlainCreation(Event createdEvent) {
        if (null == creations) {
            creations = new ArrayList<CreateResult>();
        }
        creations.add(new CreateResultImpl(createdEvent));
        return this;
    }

    /**
     * Adds a <i>userized</i> creation to this calendar result.
     *
     * @param createdEvent The created event
     * @return A self reference
     */
    public InternalCalendarResult addUserizedCreation(Event createdEvent) {
        if (null == userizedCreations) {
            userizedCreations = new ArrayList<CreateResult>();
        }
        userizedCreations.add(new CreateResultImpl(createdEvent));
        return this;
    }

    /**
     * Adds a <i>userized</i> creations to this calendar result.
     *
     * @param createdEvents The created events
     * @return A self reference
     */
    public InternalCalendarResult addUserizedCreations(List<Event> createdEvents) {
        if (null == userizedCreations) {
            userizedCreations = new ArrayList<CreateResult>(createdEvents.size());
        }
        for (Event createdEvent : createdEvents) {
            userizedCreations.add(new CreateResultImpl(createdEvent));
        }
        return this;
    }

    /**
     * Adds a plain/vanilla update to this calendar result.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @return A self reference
     */
    public InternalCalendarResult addPlainUpdate(Event originalEvent, Event updatedEvent) throws OXException {
        /*
         * merge with existing create result for same event if already contained
         */
        CreateResult existingCreation = findCreation(creations, originalEvent.getFolderId(), originalEvent.getId(), originalEvent.getRecurrenceId());
        if (null != existingCreation && creations.remove(existingCreation)) {
            creations.add(new CreateResultImpl(updatedEvent));
            return this;
        }
        /*
         * merge with existing update result for same event if already contained, or treat as new update result, otherwise
         */
        UpdateResult existingUpdate = findUpdate(updates, originalEvent.getFolderId(), originalEvent.getId(), originalEvent.getRecurrenceId());
        if (null != existingUpdate && updates.remove(existingUpdate)) {
            originalEvent = existingUpdate.getOriginal();
        } else if (null == updates) {
            updates = new ArrayList<UpdateResult>();
        }
        updates.add(new UpdateResultImpl(originalEvent, updatedEvent));
        return this;
    }

    /**
     * Adds a <i>userized</i> update to this calendar result.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @return A self reference
     */
    public InternalCalendarResult addUserizedUpdate(Event originalEvent, Event updatedEvent) throws OXException {
        /*
         * merge with existing create result for same event if already contained
         */
        CreateResult existingCreation = findCreation(userizedCreations, originalEvent.getFolderId(), originalEvent.getId(), originalEvent.getRecurrenceId());
        if (null != existingCreation && userizedCreations.remove(existingCreation)) {
            userizedCreations.add(new CreateResultImpl(updatedEvent));
            return this;
        }
        /*
         * merge with existing update result for same event if already contained, or treat as new update result, otherwise
         */
        UpdateResult existingUpdate = findUpdate(userizedUpdates, originalEvent.getFolderId(), originalEvent.getId(), originalEvent.getRecurrenceId());
        if (null != existingUpdate && userizedUpdates.remove(existingUpdate)) {
            originalEvent = existingUpdate.getOriginal();
        } else if (null == userizedUpdates) {
            userizedUpdates = new ArrayList<UpdateResult>();
        }
        userizedUpdates.add(new UpdateResultImpl(originalEvent, updatedEvent));
        return this;
    }

    /**
     * Gets the calendar event representing the system-wide view on the performed calendar changes.
     *
     * @return The calendar event
     */
    public CalendarEvent getCalendarEvent() {
        return new DefaultCalendarEvent(session.getContextId(), Utils.ACCOUNT_ID, calendarUserId, session.getSession(), getAffectedFoldersPerUser(), creations, updates, deletions, session);
    }

    /**
     * Gets the <i>userized</i> calendar result representing the acting client's point of view on the performed changes.
     *
     * @return The calendar result
     */
    public CalendarResult getUserizedResult() {
        return new DefaultCalendarResult(session.getSession(), calendarUserId, folder.getId(), userizedCreations, userizedUpdates, userizedDeletions);
    }

    private Map<Integer, List<String>> getAffectedFoldersPerUser() {
        if (null == affectedFolderIds || 0 == affectedFolderIds.size()) {
            return Collections.emptyMap();
        }
        if (1 == affectedFolderIds.size() && null != folder && folder.getId().equals(affectedFolderIds.iterator().next())) {
            Map<Integer, List<String>> affectedFoldersPerUser = new HashMap<Integer, List<String>>();
            for (Integer userId : Utils.getAffectedUsers(folder, session.getEntityResolver())) {
                affectedFoldersPerUser.put(userId, Collections.singletonList(folder.getId()));
            }
            return affectedFoldersPerUser;
        }
        try {
            return Utils.getAffectedFoldersPerUser(session, affectedFolderIds);
        } catch (OXException e) {
            org.slf4j.LoggerFactory.getLogger(InternalCalendarResult.class).warn("Error getting affected users", e);
            return Collections.emptyMap();
        }
    }

    private static CreateResult findCreation(List<CreateResult> creations, String folderId, String id, RecurrenceId recurrenceId) {
        if (null != creations) {
            for (CreateResult creation : creations) {
                Event event = creation.getCreatedEvent();
                if (Objects.equals(folderId, event.getFolderId()) && Objects.equals(id, event.getId()) && Objects.equals(recurrenceId, event.getRecurrenceId())) {
                    return creation;
                }
            }
        }
        return null;
    }

    private static UpdateResult findUpdate(List<UpdateResult> updates, String folderId, String id, RecurrenceId recurrenceId) {
        if (null != updates) {
            for (UpdateResult update : updates) {
                Event event = update.getOriginal();
                if (Objects.equals(folderId, event.getFolderId()) && Objects.equals(id, event.getId()) && Objects.equals(recurrenceId, event.getRecurrenceId())) {
                    return update;
                }
            }
        }
        return null;
    }

}
