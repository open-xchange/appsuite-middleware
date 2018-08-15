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

package com.openexchange.chronos.common;

import static com.openexchange.chronos.common.CalendarUtils.getMaximumTimestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.session.Session;

/**
 * {@link DefaultCalendarEvent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultCalendarEvent implements CalendarEvent {

    /** The fixed identifier for an account of the internal calendar provider */
    public static final int ACCOUNT_ID = 0;

    private final int contextId;
    private final int accountId;
    private final int calendarUserId;
    private final Map<Integer, List<String>> affectedFoldersPerUser;
    private final List<CreateResult> creations;
    private final List<UpdateResult> updates;
    private final List<DeleteResult> deletions;
    private final Session session;
    private final CalendarParameters calendarParameters;
    private final EntityResolver entityResolver;

    /**
     * Initializes a new {@link DefaultCalendarEvent}.
     *
     * @param contextId The context identifier
     * @param accountId The account identifier
     * @param calendarUserId The calendar user id, or <code>-1</code> if not available
     * @param affectedFoldersPerUser The identifiers of the affected folders for each user
     * @param creations The create results, or <code>null</code> if there are none
     * @param deletions The delete results, or <code>null</code> if there are none
     * @param updates The update results, or <code>null</code> if there are none
     * @param session The client session, or <code>null</code> if not available
     * @param entityResolver The entity resolver, or <code>null</code> if not available
     * @param calendarParameters Optional {@link CalendarParameters} can be empty or <code>null</code> if there are none
     */
    public DefaultCalendarEvent(int contextId, int accountId, int calendarUserId, Map<Integer, List<String>> affectedFoldersPerUser, List<CreateResult> creations, List<UpdateResult> updates, List<DeleteResult> deletions, Session session, EntityResolver entityResolver, CalendarParameters calendarParameters) {
        super();
        this.contextId = contextId;
        this.accountId = accountId;
        this.calendarUserId = calendarUserId;
        this.affectedFoldersPerUser = affectedFoldersPerUser;
        this.creations = creations;
        this.updates = updates;
        this.deletions = deletions;
        this.session = session;
        this.entityResolver = entityResolver;
        this.calendarParameters = calendarParameters;
    }

    /**
     * Initializes a new {@link DefaultCalendarEvent}.
     *
     * @param session The calendar session
     * @param calendarUserId The calendar user id, or <code>-1</code> if not available
     * @param affectedFoldersPerUser The identifiers of the affected folders for each user
     * @param creations The create results, or <code>null</code> if there are none
     * @param deletions The delete results, or <code>null</code> if there are none
     * @param updates The update results, or <code>null</code> if there are none
     */
    public DefaultCalendarEvent(CalendarSession session, int calendarUserId, Map<Integer, List<String>> affectedFoldersPerUser, List<CreateResult> creations, List<UpdateResult> updates, List<DeleteResult> deletions) {
        this(session.getContextId(), ACCOUNT_ID, calendarUserId, affectedFoldersPerUser, creations, updates, deletions, session.getSession(), session.getEntityResolver(), session);
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public int getAccountId() {
        return accountId;
    }

    @Override
    public int getCalendarUser() {
        return calendarUserId;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    @Override
    public Map<Integer, List<String>> getAffectedFoldersPerUser() {
        return affectedFoldersPerUser;
    }

    @Override
    public List<DeleteResult> getDeletions() {
        return null == deletions ? Collections.<DeleteResult>emptyList() : Collections.unmodifiableList(deletions);
    }

    @Override
    public List<UpdateResult> getUpdates() {
        return null == updates ? Collections.<UpdateResult>emptyList() : Collections.unmodifiableList(updates);
    }

    @Override
    public List<CreateResult> getCreations() {
        return null == creations ? Collections.<CreateResult>emptyList() : Collections.unmodifiableList(creations);
    }

    @Override
    public CalendarParameters getCalendarParameters() {
        return calendarParameters;
    }

    @Override
    public long getTimestamp() {
        return Math.max(getMaximumTimestamp(creations), Math.max(getMaximumTimestamp(deletions), getMaximumTimestamp(updates)));
    }

    @Override
    public String toString() {
        return "DefaultCalendarEvent [contextId=" + contextId + ", accountId=" + accountId + ", creations=" + creations + ", updates=" + updates + ", deletions=" + deletions + "]";
    }

}
