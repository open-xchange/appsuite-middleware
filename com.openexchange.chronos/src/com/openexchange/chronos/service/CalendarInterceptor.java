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
