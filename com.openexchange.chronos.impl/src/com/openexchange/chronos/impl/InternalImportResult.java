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

import java.util.Collection;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.ImportResult;
import com.openexchange.exception.OXException;

/**
 * {@link InternalImportResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class InternalImportResult extends InternalCalendarResult {

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    private final InternalCalendarResult delegate;
    private final ImportResult importResult;

    /**
     * Initializes a new {@link InternalImportResult} for a successful import operation.
     *
     * @param calendarResult The corresponding calendar result
     * @param eventID The identifier of the imported event, or <code>null</code> if no event was created
     * @param index The sequential index of the event in the source data, or <code>0</code> if unknown
     * @param warnings A list of (non-fatal) warnings that occurred during import, or an empty list if there are none
     */
    public InternalImportResult(InternalCalendarResult calendarResult, EventID eventID, int index, List<OXException> warnings) {
        this(calendarResult, eventID, index, warnings, null);
    }

    /**
     * Initializes a new {@link InternalImportResult} for an unsuccessful import operation.
     *
     * @param calendarResult The corresponding calendar result
     * @param index The sequential index of the event in the source data, or <code>0</code> if unknown
     * @param warnings A list of (non-fatal) warnings that occurred during import, or an empty list if there are none
     * @param error The (fatal) error that prevented the event from being stored successfully
     */
    public InternalImportResult(InternalCalendarResult calendarResult, int index, List<OXException> warnings, OXException error) {
        this(calendarResult, null, index, warnings, error);
    }

    /**
     * Initializes a new {@link InternalImportResult}.
     *
     * @param delegate The corresponding calendar result
     * @param eventID The identifier of the imported event, or <code>null</code> if no event was created
     * @param index The sequential index of the event in the source data, or <code>0</code> if unknown
     * @param warnings A list of (non-fatal) warnings that occurred during import, or an empty list if there are none
     * @param error A (fatal) error that prevented the event from being stored successfully, or <code>null</code> if there is none
     */
    private InternalImportResult(InternalCalendarResult delegate, EventID eventID, int index, List<OXException> warnings, OXException error) {
        super(delegate.getSession(), delegate.getCalendarUserId(), delegate.getFolder());
        this.delegate = delegate;
        this.importResult = new DefaultImportResult(getUserizedResult(), eventID, index, warnings, error);
    }

    /**
     * Gets the import result providing additional information about the import operation.
     *
     * @return The import result
     */
    public ImportResult getImportResult() {
        return importResult;
    }

    /**
     * Gets the calendar event representing the system-wide view on the performed calendar changes.
     *
     * @param parameters Additional calendar parameters, or <code>null</code> if not available
     * @return The calendar event
     */
    @Override
    public CalendarEvent getCalendarEvent() {
        return delegate.getCalendarEvent();
    }

    /**
     * Gets the <i>userized</i> calendar result representing the acting client's point of view on the performed changes.
     *
     * @return The calendar result
     */
    @Override
    public CalendarResult getUserizedResult() {
        return delegate.getUserizedResult();
    }

    @Override
    public void addAffectedFolderIds(String folderId, Collection<? extends String> folderIds) {
        delegate.addAffectedFolderIds(folderId, folderIds);
    }

    @Override
    public void addAffectedFolderIds(String folderId, Collection<? extends String> folderIds, Collection<? extends String> otherFolderIds) {
        delegate.addAffectedFolderIds(folderId, folderIds, otherFolderIds);
    }

    @Override
    public InternalCalendarResult addPlainDeletion(long timestamp, Event event) {
        return delegate.addPlainDeletion(timestamp, event);
    }

    @Override
    public InternalCalendarResult addUserizedDeletion(long timestamp, Event event) {
        return delegate.addUserizedDeletion(timestamp, event);
    }

    @Override
    public InternalCalendarResult addPlainCreation(Event createdEvent) {
        return delegate.addPlainCreation(createdEvent);
    }

    @Override
    public InternalCalendarResult addUserizedCreation(Event createdEvent) {
        return delegate.addUserizedCreation(createdEvent);
    }

    @Override
    public InternalCalendarResult addUserizedCreations(List<Event> createdEvents) {
        return delegate.addUserizedCreations(createdEvents);
    }

    @Override
    public InternalCalendarResult addPlainUpdate(Event originalEvent, Event updatedEvent) throws OXException {
        return delegate.addPlainUpdate(originalEvent, updatedEvent);
    }

    @Override
    public InternalCalendarResult addUserizedUpdate(Event originalEvent, Event updatedEvent) throws OXException {
        return delegate.addUserizedUpdate(originalEvent, updatedEvent);
    }

}
