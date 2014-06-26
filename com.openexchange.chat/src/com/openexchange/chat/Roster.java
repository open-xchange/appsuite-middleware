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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.chat;

import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link Roster} - Represents a user's roster, which is the collection of users a person receives presence updates for.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Roster {

    /**
     * Returns an unmodifiable map containing all users in the roster.
     *
     * @return All entries in the roster.
     */
    public Map<String, ChatUser> getEntries() throws OXException;

    /**
     * Returns the presence info for a particular user. If the user is offline, or if no presence data is available (such as when you are
     * not subscribed to the user's presence updates), unavailable presence will be returned.
     *
     * @param user The user to receive the presence from
     * @return The user's current presence, or unavailable presence if the user is offline or if no presence information is available.
     * @throws OXException If presence cannot be returned
     */
    public Presence getPresence(ChatUser user) throws OXException;

    /**
     * Adds a listener to this roster. The listener will be fired anytime one or more changes to the roster are pushed from the server.
     *
     * @param rosterListener A roster listener.
     */
    public void addRosterListener(RosterListener rosterListener) throws OXException;

    /**
     * Removes a listener from this roster. The listener will be fired anytime one or more changes to the roster are pushed from the server.
     *
     * @param rosterListener A roster listener.
     */
    public void removeRosterListener(RosterListener rosterListener) throws OXException;

}
