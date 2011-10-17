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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.chat.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.chat.ChatUser;
import com.openexchange.chat.Presence;
import com.openexchange.chat.Roster;
import com.openexchange.chat.RosterListener;
import com.openexchange.exception.OXException;

/**
 * {@link RosterImpl}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RosterImpl implements Roster {

    private static final String DEFAULT_RESOURCE = "default";

    private final ConcurrentMap<String, ChatUser> entries;

    private final List<RosterListener> rosterListeners;

    private final Map<String, Map<String, Presence>> presenceMap;

    /**
     * Initializes a new {@link RosterImpl}.
     */
    public RosterImpl() {
        super();
        entries = new ConcurrentHashMap<String, ChatUser>();
        rosterListeners = new CopyOnWriteArrayList<RosterListener>();
        presenceMap = new ConcurrentHashMap<String, Map<String, Presence>>();
    }

    @Override
    public Collection<ChatUser> getEntries() throws OXException {
        return Collections.unmodifiableCollection(entries.values());
    }

    @Override
    public Presence getPresence(final ChatUser user) throws OXException {
        final Map<String, Presence> resources = presenceMap.get(user.getId());
        if (null == resources) {
            final PresenceImpl packetUnavailable = new PresenceImpl(Presence.Type.UNAVAILABLE);
            packetUnavailable.setFrom(user);
            return packetUnavailable;
        }
        final Presence presence = resources.get(DEFAULT_RESOURCE);
        if (null == presence) {
            final PresenceImpl packetUnavailable = new PresenceImpl(Presence.Type.UNAVAILABLE);
            packetUnavailable.setFrom(user);
            return packetUnavailable;
        }
        return presence;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.chat.Roster#addRosterListener(com.openexchange.chat.RosterListener)
     */
    @Override
    public void addRosterListener(final RosterListener rosterListener) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.chat.Roster#removeRosterListener(com.openexchange.chat.RosterListener)
     */
    @Override
    public void removeRosterListener(final RosterListener rosterListener) throws OXException {
        // TODO Auto-generated method stub

    }

}
