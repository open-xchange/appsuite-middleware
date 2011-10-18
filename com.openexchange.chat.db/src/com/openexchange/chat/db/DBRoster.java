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

package com.openexchange.chat.db;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import gnu.trove.ConcurrentTIntObjectHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.chat.ChatExceptionCodes;
import com.openexchange.chat.ChatUser;
import com.openexchange.chat.Presence;
import com.openexchange.chat.Roster;
import com.openexchange.chat.RosterListener;
import com.openexchange.chat.util.ChatUserImpl;
import com.openexchange.chat.util.PresenceImpl;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.user.UserService;

/**
 * {@link DBRoster}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBRoster implements Roster {

    private static final ConcurrentTIntObjectHashMap<DBRoster> ROSTER_MAP = new ConcurrentTIntObjectHashMap<DBRoster>();

    /**
     * Gets the roster for specified context.
     * 
     * @param context The context
     * @return The roster or <code>null</code>
     */
    public static DBRoster optRosterFor(final int contextId) {
        return ROSTER_MAP.get(contextId);
    }

    /**
     * Gets the roster for specified context.
     * 
     * @param context The context
     * @return The roster
     */
    public static DBRoster getRosterFor(final Context context) {
        final int key = context.getContextId();
        DBRoster dbRoster = ROSTER_MAP.get(key);
        if (null == dbRoster) {
            final DBRoster newRoster = new DBRoster(context);
            dbRoster = ROSTER_MAP.putIfAbsent(key, newRoster);
            if (null == dbRoster) {
                dbRoster = newRoster;
            }
        }
        return dbRoster;
    }

    private final Context context;

    private final List<RosterListener> listeners;

    /**
     * Initializes a new {@link DBRoster}.
     */
    private DBRoster(final Context context) {
        super();
        this.context = context;
        listeners = new CopyOnWriteArrayList<RosterListener>();
    }

    private <S> S getService(final Class<? extends S> clazz) throws OXException {
        final S service = getService(clazz);
        if (null == service) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(clazz.getName());
        }
        return service;
    }

    @Override
    public Collection<ChatUser> getEntries() throws OXException {
        final UserService userService = getService(UserService.class);
        final User[] users = userService.getUser(context);
        final List<ChatUser> ret = new ArrayList<ChatUser>(users.length);
        for (int i = 0; i < users.length; i++) {
            final User u = users[i];
            final ChatUserImpl chatUser = new ChatUserImpl();
            chatUser.setId(String.valueOf(u.getId()));
            chatUser.setName(u.getDisplayName());
            ret.add(chatUser);
        }
        return ret;
    }

    @Override
    public Presence getPresence(final ChatUser user) throws OXException {
        final DatabaseService databaseService = getService(DatabaseService.class);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final Connection con = databaseService.getReadOnly(context);
        try {
            stmt = con.prepareStatement("SELECT mode, statusMessage, type FROM chatPresence WHERE cid = ? AND user = ?");
            final int userId = Integer.parseInt(user.getId());
            int pos = 1;
            stmt.setInt(pos++, context.getContextId());
            stmt.setInt(pos++, userId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                final PresenceImpl packetUnavailable = new PresenceImpl(Presence.Type.UNAVAILABLE);
                packetUnavailable.setFrom(user);
                return packetUnavailable;
            }
            if (null == getService(SessiondService.class).getAnyActiveSessionForUser(userId, context.getContextId())) {
                final PresenceImpl packetUnavailable = new PresenceImpl(Presence.Type.UNAVAILABLE);
                packetUnavailable.setFrom(user);
                return packetUnavailable;
            }
            final PresenceImpl packetAvailable = new PresenceImpl(Presence.Type.AVAILABLE);
            packetAvailable.setMode(Presence.Mode.valueOf(rs.getString(1)));
            packetAvailable.setFrom(user);
            packetAvailable.setStatus(rs.getString(2));
            return packetAvailable;
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(context, con);
        }
    }

    /**
     * Notify listeners about changed presence.
     * 
     * @param presence The changed presence
     */
    public void notifyRosterListeners(final Presence presence) {
        for (final RosterListener listener : listeners) {
            listener.presenceChanged(presence);
        }
    }

    @Override
    public void addRosterListener(final RosterListener rosterListener) throws OXException {
        if (null == rosterListener) {
            return;
        }
        listeners.add(rosterListener);
    }

    @Override
    public void removeRosterListener(final RosterListener rosterListener) throws OXException {
        if (null == rosterListener) {
            return;
        }
        listeners.remove(rosterListener);
    }

}
