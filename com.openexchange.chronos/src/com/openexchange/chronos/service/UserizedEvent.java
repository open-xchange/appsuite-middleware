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

package com.openexchange.chronos.service;

import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UserizedEvent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UserizedEvent {

    private final Event event;
    private final ServerSession session;

    private List<Alarm> alarms;
    private boolean alarmsSet;
    private int folderId;
    private boolean folderIdSet;

    /**
     * Initializes a new {@link UserizedEvent}.
     *
     * @param session The current user's session
     * @param event The underlying event data
     */
    public UserizedEvent(ServerSession session, Event event) {
        super();
        this.session = session;
        this.event = event;
    }

    /**
     * Initializes a new {@link UserizedEvent}.
     *
     * @param session The current user's session
     * @param event The underlying event data
     * @param folderId The folder identifier representing the view on the event.
     * @param alarms The attendee's alarms of the event.
     */
    public UserizedEvent(ServerSession session, Event event, int folderId, List<Alarm> alarms) {
        this(session, event);
        setFolderId(folderId);
        setAlarms(alarms);
    }

    /**
     * Gets the underlying event data.
     *
     * @return The event data
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Gets the user's session.
     *
     * @return The session
     */
    public ServerSession getSession() {
        return session;
    }

    /**
     * Gets the attendee's alarms for the event.
     *
     * @return The alarms
     */
    public List<Alarm> getAlarms() {
        return alarms;
    }

    /**
     * Gets the attendee's alarms for the event.
     *
     * @param alarms The alarms to set
     */
    public void setAlarms(List<Alarm> alarms) {
        this.alarms = alarms;
        alarmsSet = true;
    }

    /**
     * Gets a value indicating whether attendee's alarms for the event have been set or not.
     *
     * @return <code>true</code> if the attendee's alarms are set, <code>false</code>, otherwise
     */
    public boolean containsAlarms() {
        return alarmsSet;
    }

    /**
     * Removes the attendee's alarms of the event.
     */
    public void removeAlarms() {
        alarms = null;
        alarmsSet = false;
    }

    /**
     * Gets the folder identifier representing the view on the event.
     *
     * @return The folder identifier
     */
    public int getFolderId() {
        return folderId;
    }

    /**
     * Sets the folder identifier representing the view on the event.
     *
     * @param folderId The folder identifier
     */
    public void setFolderId(int folderId) {
        this.folderId = folderId;
        folderIdSet = true;
    }

    /**
     * Gets a value indicating whether folder identifier representing the view on the event has been set or not.
     *
     * @return <code>true</code> if the folder identifier is set, <code>false</code>, otherwise
     */
    public boolean containsFolderId() {
        return folderIdSet;
    }

    /**
     * Removes the folder identifier of the event.
     */
    public void removeFolderId() {
        folderId = 0;
        folderIdSet = false;
    }

    @Override
    public String toString() {
        return "UserizedEvent [userId=" + session.getUserId() + ", folderId=" + folderId + ", event=" + event + "]";
    }

}
