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

package com.openexchange.chat.db;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import gnu.trove.ConcurrentTIntObjectHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.chat.ChatExceptionCodes;
import com.openexchange.chat.ChatUser;
import com.openexchange.chat.Presence;
import com.openexchange.chat.Presence.Mode;
import com.openexchange.chat.Roster;
import com.openexchange.chat.RosterListener;
import com.openexchange.chat.util.ChatUserImpl;
import com.openexchange.chat.util.PresenceImpl;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.user.UserService;

/**
 * {@link DBRoster}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBRoster implements Roster {

    private static final AtomicReference<SessiondService> SERVICE_REF = new AtomicReference<SessiondService>();

    /**
     * Sets the {@link SessiondService} service instance.
     *
     * @param service The service
     */
    public static void set(final SessiondService service) {
        SERVICE_REF.set(service);
    }

    /**
     * Gets the service.
     *
     * @return The service
     */
    public static SessiondService get() {
        return SERVICE_REF.get();
    }

    /**
     * Gets the first session that matches the given userId and contextId.
     */
    public static Session getAnyActiveSessionForUser(final int userId, final int contextId) {
        final SessiondService sessiondService = SERVICE_REF.get();
        return null == sessiondService ? null : sessiondService.getAnyActiveSessionForUser(userId, contextId);
    }

    private static final ConcurrentTIntObjectHashMap<DBRoster> ROSTER_MAP = new ConcurrentTIntObjectHashMap<DBRoster>(128);

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
     * @throws OXException If initialization fails
     */
    public static DBRoster getRosterFor(final int contextId) throws OXException {
        return getRosterFor(DBChatServiceLookup.getService(ContextService.class).getContext(contextId));
    }

    /**
     * Gets the roster for specified context.
     *
     * @param context The context
     * @return The roster
     * @throws OXException If initialization fails
     */
    public static DBRoster getRosterFor(final Context context) throws OXException {
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

    private final Map<String, ChatUser> entries;

    /**
     * Initializes a new {@link DBRoster}.
     *
     * @throws OXException If initialization fails
     */
    private DBRoster(final Context context) throws OXException {
        super();
        this.context = context;
        listeners = new CopyOnWriteArrayList<RosterListener>();
        entries = Collections.unmodifiableMap(generateEntries());
    }

    private Map<String, ChatUser> generateEntries() throws OXException {
        final UserService userService = getService(UserService.class);
        final User[] users = userService.getUser(context);
        final Map<String, ChatUser> tmp = new HashMap<String, ChatUser>(users.length);
        for (int i = 0; i < users.length; i++) {
            final User u = users[i];
            final ChatUserImpl chatUser = new ChatUserImpl();
            final String id = Integer.toString(u.getId());
            chatUser.setId(id);
            chatUser.setName(u.getDisplayName());
            tmp.put(id, chatUser);
        }
        return tmp;
    }

    private <S> S getService(final Class<? extends S> clazz) throws OXException {
        final S service = DBChatServiceLookup.getService(clazz);
        if (null == service) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(clazz.getName());
        }
        return service;
    }

    @Override
    public Map<String, ChatUser> getEntries() throws OXException {
        return entries;
    }

    /**
     * Updates specified user's presence.
     *
     * @param presence The presence
     * @throws OXException If updating presence fails
     */
    public void updatePresence(final Presence presence) throws OXException {
        updatePresence(presence.getFrom(), presence);
    }

    /**
     * Updates specified user's presence.
     *
     * @param user The user
     * @param presence The presence
     * @throws OXException If updating presence fails
     */
    public void updatePresence(final ChatUser user, final Presence presence) throws OXException {
        final DatabaseService databaseService = getService(DatabaseService.class);
        PreparedStatement stmt = null;
        final Connection con = databaseService.getWritable(context);
        try {
            stmt = con.prepareStatement("UPDATE chatPresence SET mode = ?, statusMessage = ?, lastModified = ? WHERE cid = ? AND user = ?");
            int pos = 1;
            {
                final Mode mode = presence.getMode();
                stmt.setInt(pos++, (null == mode ? Mode.AVAILABLE : mode).ordinal());
            }
            {
                final String status = presence.getStatus();
                if (null == status) {
                    stmt.setNull(pos++, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(pos++, status);
                }
            }
            stmt.setLong(pos++, System.currentTimeMillis());
            stmt.setInt(pos++, context.getContextId());
            stmt.setInt(pos, DBChatUtility.parseUnsignedInt(user.getId()));
            final int rowCount = stmt.executeUpdate();
            if (rowCount <= 0) {
                try {
                    insertPresence(user, presence);
                } catch (final Exception e) {
                    // Concurrent insert attempt
                    updatePresence(user, presence);
                }
            }
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            databaseService.backWritable(context, con);
        }
        /*
         * Notify roster listeners
         */
        notifyRosterListeners(presence);
    }

    /**
     * Inserts given presence.
     *
     * @param user The user
     * @param presence The presence
     * @throws OXException If updating presence fails
     */
    public void insertPresence(final Presence presence) throws OXException {
        insertPresence(presence.getFrom(), presence);
    }

    /**
     * Inserts given presence.
     *
     * @param user The user
     * @param presence The presence
     * @throws OXException If updating presence fails
     */
    public void insertPresence(final ChatUser user, final Presence presence) throws OXException {
        final DatabaseService databaseService = getService(DatabaseService.class);
        PreparedStatement stmt = null;
        final Connection con = databaseService.getWritable(context);
        try {
            stmt = con.prepareStatement("INSERT INTO chatPresence (cid, user, statusMessage, mode, lastModified) VALUES (?, ?, ?, ?, ?)");
            int pos = 1;
            stmt.setInt(pos++, context.getContextId());
            stmt.setInt(pos++, DBChatUtility.parseUnsignedInt(user.getId()));
            stmt.setString(pos++, presence.getStatus());
            stmt.setInt(pos++, presence.getMode().ordinal());
            stmt.setLong(pos, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            databaseService.backWritable(context, con);
        }
        /*
         * Notify roster listeners
         */
        notifyRosterListeners(presence);
    }

    @Override
    public Presence getPresence(final ChatUser user) throws OXException {
        final DatabaseService databaseService = getService(DatabaseService.class);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final Connection con = databaseService.getReadOnly(context);
        try {
            stmt = con.prepareStatement("SELECT mode, statusMessage, type FROM chatPresence WHERE cid = ? AND user = ?");
            final int userId = DBChatUtility.parseUnsignedInt(user.getId());
            int pos = 1;
            stmt.setInt(pos++, context.getContextId());
            stmt.setInt(pos++, userId);
            rs = stmt.executeQuery();
            final boolean hasSession = (null != SERVICE_REF.get().getAnyActiveSessionForUser(userId, context.getContextId()));
            if (!rs.next()) {
                if (hasSession) {
                    final PresenceImpl presence = new PresenceImpl();
                    presence.setFrom(user);
                    presence.setStatus("Hey there, I'm using OX7 chat...");
                    try {
                        insertPresence(user, presence);
                        return presence;
                    } catch (final Exception e) {
                        // Ignore
                    }
                }
                final PresenceImpl presence = new PresenceImpl(hasSession ? Presence.Type.AVAILABLE : Presence.Type.UNAVAILABLE);
                presence.setFrom(user);
                return presence;
            }
            if (!hasSession) {
                final PresenceImpl packetUnavailable = new PresenceImpl(Presence.Type.UNAVAILABLE);
                packetUnavailable.setMode(Presence.Mode.modeOf(rs.getInt(1)));
                packetUnavailable.setFrom(user);
                packetUnavailable.setStatus(rs.getString(2));
                return packetUnavailable;
            }
            final PresenceImpl packetAvailable = new PresenceImpl(Presence.Type.AVAILABLE);
            packetAvailable.setMode(Presence.Mode.modeOf(rs.getInt(1)));
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
     * Notifies listeners about changed presence.
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

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("DBRoster {");
        if (context != null) {
            builder.append("context=").append(context).append(", ");
        }
        if (entries != null) {
            builder.append("entries=").append(entries);
        }
        if (listeners != null && !listeners.isEmpty()) {
            builder.append("listeners=").append(listeners).append(", ");
        }
        builder.append('}');
        return builder.toString();
    }

}
