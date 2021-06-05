/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.service;

import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarInterceptor}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public interface CalendarInterceptor {

    /**
     * Gets the event fields that are of relevance for the interceptor.
     * <p/>
     * If specified, these properties will be included in the events referencing the <i>original</i> objects when triggering the
     * interceptor. Otherwise, only some identifying fields may be set.
     *
     * @return The relevant fields, or <code>null</code> no special metadata is required
     */
    Set<EventField> getRelevantFields();

    /**
     * Invoked prior an existing event is updated in the storage, within the calendar storage transaction.
     * <p/>
     * Implementations may adjust the passed <code>updatedEvent</code> reference for their needs.
     * <p/>
     * When an {@link OXException} is thrown during handling, no changes will be persisted and the whole calendar operation is rolled back.
     * Non-fatal errors can still be added as <i>warning</i> in the supplied calendar session instead.
     *
     * @param session The calendar session
     * @param folderId The identifier of the folder the operation is executed in
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @throws OXException If handling fails and the calendar operation should be aborted
     */
    void onBeforeUpdate(CalendarSession session, String folderId, Event originalEvent, Event updatedEvent) throws OXException;

    /**
     * Invoked prior a new event is created in the storage, within the calendar storage transaction.
     * <p/>
     * Implementations may adjust the passed <code>newEvent</code> reference for their needs.
     * <p/>
     * When an {@link OXException} is thrown during handling, no changes will be persisted and the whole calendar operation is rolled back.
     * Non-fatal errors can still be added as <i>warning</i> in the supplied calendar session instead.
     *
     * @param session The calendar session
     * @param folderId The identifier of the folder the operation is executed in
     * @param newEvent The new event
     * @throws OXException If handling fails and the calendar operation should be aborted
     */
    void onBeforeCreate(CalendarSession session, String folderId, Event newEvent) throws OXException;

    /**
     * Invoked prior an existing event is deleted in the storage, within the calendar storage transaction.
     * <p/>
     * Note that not all fields may be present in the passed <code>deletedEvent</code> reference, so ensure to include the required ones
     * via {@link #getRelevantFields()}.
     * <p/>
     * When an {@link OXException} is thrown during handling, no changes will be persisted and the whole calendar operation is rolled back.
     * Non-fatal errors can still be added as <i>warning</i> in the supplied calendar session instead.
     *
     * @param session The calendar session
     * @param folderId The identifier of the folder the operation is executed in
     * @param deletedEvent The deleted event
     * @throws OXException If handling fails and the calendar operation should be aborted
     */
    void onBeforeDelete(CalendarSession session, String folderId, Event deletedEvent) throws OXException;

}
