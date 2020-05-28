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

import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.event.CommonEvent;
import com.openexchange.groupware.Types;
import com.openexchange.session.Session;

/**
 * {@link ChronosCommonEvent} - The {@link CommonEvent} to thrown on {@link Event} changes
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ChronosCommonEvent implements CommonEvent {

    private final Session session;
    private final int actionID;
    private final Event actionEvent;
    private final Event oldEvent;
    private final Map<Integer, Set<Integer>> affectedUsersWithFolders;

    /**
     * Initializes a new {@link ChronosCommonEvent}.
     *
     * @param session The associated session
     * @param actionID The common event action identifier to take over
     * @param actionEvent The new, updated or deleted event
     * @param oldEvent The original event in case of updates
     * @param affectedUsersWithFolders A map containing the affected user identifiers associated with the corresponding folder identifiers
     */
    public ChronosCommonEvent(Session session, int actionID, Event actionEvent, Event oldEvent, Map<Integer, Set<Integer>> affectedUsersWithFolders) {
        super();
        this.session = session;
        this.actionID = actionID;
        this.actionEvent = actionEvent;
        this.oldEvent = oldEvent;
        this.affectedUsersWithFolders = affectedUsersWithFolders;
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

}
