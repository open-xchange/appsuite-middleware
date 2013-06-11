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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.links;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.logging.Log;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogFactory;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link Links} - Provides static access to link module
 *
 * @author <a href="mailto:ben.pahne@open-xchange.com">Benjamin Frederic
 *         Pahne</a>
 */
public class Links {

    static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Links.class));

    private static interface ModuleAccess {

        /**
         * Checks if denoted item is readable by specified user in given folder.
         *
         * @param oid The item's unique ID
         * @param folder The associated folder
         * @param user The user ID
         * @param group The user's group IDs
         * @param so The session
         * @return <code>true</code> if denoted item is readable by specified
         *         user in given folder; otherwise <code>false</code>
         * @throws OXException If specified context ID cannot be resolved
         */
        boolean isReadable(int oid, int folder, int user, int[] group, Session so) throws OXException;

        /**
         * Checks if denoted item is readable by specified user.
         *
         * @param oid The item's unique ID
         * @param user The user ID
         * @param group The user's group IDs
         * @param so The session
         * @return <code>true</code> if denoted item is readable by specified
         *         user; otherwise <code>false</code>
         * @throws OXException If specified context ID cannot be resolved
         * @throws UnsupportedOperationException If method is not supported
         * @see #supportsAccessByID()
         */
        boolean isReadableByID(int oid, int user, int[] group, Session so) throws OXException,
                UnsupportedOperationException;

        /**
         * Tests if {@link #isReadableByID(int, int, int[], Session)} is
         * supported.
         *
         * @return <code>true</code> if
         *         {@link #isReadableByID(int, int, int[], Session)} is
         *         supported; otherwise <code>false</code>.
         */
        boolean supportsAccessByID();

        /**
         * Checks if specified user has appropriate module access.
         *
         * @param so The session
         * @return <code>true</code> if specified user has appropriate module
         *         access; otherwise <code>false</code>
         * @throws OXException If session's context ID cannot be resolved
         */
        boolean hasModuleRights(Session so) throws OXException;
    }

    private static final Map<Integer, ModuleAccess> modules;

    /*
     * Some Modules are deprecated but you never know what comes
     */
    static {
        modules = new HashMap<Integer, ModuleAccess>(4);
        modules.put(Integer.valueOf(Types.APPOINTMENT), new ModuleAccess() {
            CalendarCollectionService calColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
            @Override
            public boolean supportsAccessByID() {
                return true;
            }

            @Override
            public boolean isReadable(final int oid, final int fid, final int user, final int[] group, final Session so)
                    throws OXException {
                final Context ct = ContextStorage.getStorageContext(so.getContextId());
                if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ct).hasCalendar()) {
                    return false;
                }
                try {
                    return calColl.getReadPermission(oid, fid, so, ct);
                } catch (final OXException ox) {
                    LOG.error("UNABLE TO CHECK CALENDAR READRIGHT FOR LINK", ox);
                    return false;
                }
            }

            @Override
            public boolean hasModuleRights(final Session so) throws OXException {
                final Context ct = ContextStorage.getStorageContext(so.getContextId());
                if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ct).hasCalendar()) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean isReadableByID(final int oid, final int user, final int[] group, final Session so)
                    throws OXException {
                final Context ct = ContextStorage.getStorageContext(so.getContextId());
                if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ct).hasCalendar()) {
                    return false;
                }
                int fid = -1;
                try {
                    fid = calColl.getAppointmentFolder(oid, user, ct);
                } catch (final OXException ox) {
                    LOG.error("UNABLE TO CHECK CALENDAR READRIGHT FOR LINK", ox);
                    return false;
                }
                try {
                    return calColl.getReadPermission(oid,fid, so, ct);
                } catch (final OXException ox) {
                    LOG.error("UNABLE TO CHECK CALENDAR READRIGHT FOR LINK", ox);
                    return false;
                }
            }
        });
        modules.put(Integer.valueOf(Types.TASK), new ModuleAccess() {
            @Override
            public boolean supportsAccessByID() {
                return true;
            }

            @Override
            public boolean isReadable(final int oid, final int fid, final int user, final int[] group, final Session so)
                    throws OXException {
                final Context ctx = ContextStorage.getStorageContext(so.getContextId());
                final UserConfiguration userConfig = UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx);
                if (!userConfig.hasTask()) {
                    return false;
                }
                return com.openexchange.groupware.tasks.Task2Links.checkMayReadTask(so, ctx, userConfig, oid, fid);
            }

            @Override
            public boolean hasModuleRights(final Session so) throws OXException {
                final Context ct = ContextStorage.getStorageContext(so.getContextId());
                if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ct).hasTask()) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean isReadableByID(final int oid, final int user, final int[] group, final Session so) throws OXException {
                final Context ctx = ContextStorage.getStorageContext(so.getContextId());
                final UserConfiguration userConfig = UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx);
                if (!userConfig.hasTask()) {
                    return false;
                }
                return com.openexchange.groupware.tasks.Task2Links.checkMayReadTask(so, ctx, userConfig, oid);
            }
        });
        modules.put(Integer.valueOf(Types.CONTACT), new ModuleAccess() {
            @Override
            public boolean supportsAccessByID() {
                return false;
            }

            @Override
            public boolean isReadable(final int oid, final int fid, final int user, final int[] group, final Session so)
                    throws OXException {
                final Context ct = ContextStorage.getStorageContext(so.getContextId());

                if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ct).hasContact()) {
                    return false;
                }
                try {
                    return Contacts.performContactReadCheckByID(oid, user, ct, UserConfigurationStorage
                            .getInstance().getUserConfigurationSafe(so.getUserId(), ct));
                } catch (final Exception e) {
                    // System.out.println("UNABLE TO CHECK CONTACT READRIGHT FOR LINK");
                    LOG.error("UNABLE TO CHECK CONTACT READRIGHT FOR LINK", e);
                    return false;
                }
            }

            @Override
            public boolean hasModuleRights(final Session so) throws OXException {
                final Context ct = ContextStorage.getStorageContext(so.getContextId());
                if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ct).hasContact()) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean isReadableByID(final int oid, final int user, final int[] group, final Session so) {
                throw new UnsupportedOperationException("isReadableByID() not supported for contact module");
            }
        });
        modules.put(Integer.valueOf(Types.INFOSTORE), new ModuleAccess() {
            @Override
            public boolean supportsAccessByID() {
                return false;
            }

            @Override
            public boolean isReadable(final int oid, final int fid, final int user, final int[] group, final Session so)
                    throws OXException {
                final InfostoreFacade DATABASE = new InfostoreFacadeImpl(new DBPoolProvider());
                final Context ct = ContextStorage.getStorageContext(so.getContextId());
                try {
                    return DATABASE.exists(oid, InfostoreFacade.CURRENT_VERSION, ct, UserStorage.getStorageUser(so
                            .getUserId(), ct), UserConfigurationStorage.getInstance().getUserConfigurationSafe(
                            so.getUserId(), ct));
                } catch (final OXException e) {
                    LOG.error("UNABLE TO CHECK INFOSTORE READRIGHT FOR LINK", e);
                    return false;
                }
            }

            @Override
            public boolean hasModuleRights(final Session so) throws OXException {
                final Context ct = ContextStorage.getStorageContext(so.getContextId());
                if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ct).hasInfostore()) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean isReadableByID(final int oid, final int user, final int[] group, final Session so) {
                throw new UnsupportedOperationException("isReadableByID() not supported for infostore module");
            }
        });
    }

    private Links() {
        super();
    }

    public static void performLinkStorage(final LinkObject l, final int user, final int[] group, final Session so,
            final Connection writecon) throws OXException {

        final Context ct = ContextStorage.getStorageContext(so.getContextId());

        if (!modules.get(I(l.getFirstType())).isReadable(l.getFirstId(), l.getFirstFolder(), user, group, so) ||
            !modules.get(I(l.getSecondType())).isReadable(l.getSecondId(), l.getSecondFolder(), user, group, so)) {
            throw LinkExceptionCodes.NO_LINK_ACCESS_PERMISSION.create(
                I(l.getFirstId()),
                I(l.getFirstFolder()),
                I(l.getSecondId()),
                I(l.getSecondFolder()),
                I(so.getContextId()));
        }

        Statement stmt = null;
        ResultSet rs = null;
        Connection readCon = null;
        final LinksSql lms = new LinksMySql();
        try {
            readCon = DBPool.pickup(ct);
            stmt = readCon.createStatement();
            rs = stmt.executeQuery(lms.iFperformLinkStorage(l, so.getContextId()));

            if (rs.next()) {
                throw LinkExceptionCodes.ALREADY_LINKED.create(
                    I(l.getFirstId()),
                    I(l.getFirstFolder()),
                    I(l.getSecondId()),
                    I(l.getSecondFolder()),
                    I(so.getContextId()));
            }
        } catch (final SQLException e) {
            throw LinkExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            closeSQLStuff(rs, stmt);
            if (readCon != null) {
                DBPool.closeReaderSilent(ct, readCon);
            }
        }

        PreparedStatement ps = null;
        UUID uuid = UUID.randomUUID();
        byte[] uuidBinary = UUIDs.toByteArray(uuid);
        try {
            ps = writecon.prepareStatement(lms.iFperformLinkStorageInsertString());
            ps.setInt(1, l.getFirstId());
            ps.setInt(2, l.getFirstType());
            ps.setInt(3, l.getFirstFolder());
            ps.setInt(4, l.getSecondId());
            ps.setInt(5, l.getSecondType());
            ps.setInt(6, l.getSecondFolder());
            ps.setInt(7, l.getContectId());
            ps.setBytes(8, uuidBinary);
            ps.execute();
        } catch (final SQLException e) {
            throw LinkExceptionCodes.SQL_PROBLEM.create(e, getStatement(ps));
        } finally {
            closeSQLStuff(ps);
        }
    }

    public static LinkObject[] getAllLinksFromObject(final int id, final int type, final int folderId, final int user, final int[] group, final Session so, final Connection readcon) throws OXException {
        final List<LinkObject> tmp = new ArrayList<LinkObject>();
        Statement stmt = null;
        ResultSet rs = null;
        final LinksSql lms = new LinksMySql();
        try {
            stmt = readcon.createStatement();
            rs = stmt.executeQuery(lms.iFgetAllLinksFromObject(id, type, folderId, so.getContextId()));
            while (rs.next()) {
                final LinkObject lo = new LinkObject(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs
                        .getInt(5), rs.getInt(6), rs.getInt(7));
                if (modules.get(Integer.valueOf(lo.getFirstType())).isReadable(lo.getFirstId(), lo.getFirstFolder(),
                        user, group, so)
                        && modules.get(Integer.valueOf(lo.getSecondType())).isReadable(lo.getSecondId(),
                                lo.getSecondFolder(), user, group, so)) {
                    tmp.add(lo);
                }
            }
        } catch (final SQLException e) {
            throw LinkExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            closeSQLStuff(rs, stmt);
        }
        return tmp.toArray(new LinkObject[tmp.size()]);
    }

    public static int[][] deleteLinkFromObject(final int id, final int type, final int folder, final int[][] data,
            final int user, final int[] group, final Session so, final Connection readcon, final Connection writecon)
            throws OXException {
        Statement stmt = null;
        Statement del = null;
        ResultSet rs = null;
        final LinksSql lms = new LinksMySql();
        final List<int[]> resp = new ArrayList<int[]>();

        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder("Fetching rights for Module: " + type + " id:" + id + " folder:" + folder
                    + " user:" + user + " group:" + Arrays.toString(group)));
        }

        if (!modules.get(Integer.valueOf(type)).isReadable(id, folder, user, group, so)) {
            for (final int[] tmp : data) {
                resp.add(tmp);
            }
        }

        try {
            del = writecon.createStatement();
            stmt = readcon.createStatement();
            rs = stmt.executeQuery(lms.iFgetAllLinksFromObject(id, type, folder, so.getContextId()));
            int cnt = 0;
            while (rs.next()) {
                int loadid = rs.getInt(1);
                int loadtype = rs.getInt(2);
                int loadfolder = rs.getInt(3);
                boolean second = false;

                if (loadid == id) {
                    loadid = rs.getInt(4);
                    loadtype = rs.getInt(5);
                    loadfolder = rs.getInt(6);
                    second = true;
                }

                for (int i = 0; i < data.length; i++) {
                    if ((data[i][0] == loadid) && (data[i][1] == loadtype) && (data[i][2] == loadfolder)) {
                        try {
                            if (!modules.get(I(loadtype)).isReadable(loadid, loadfolder, user, group, so)) {
                                throw LinkExceptionCodes.NO_LINK_ACCESS_PERMISSION.create(
                                    I(loadid),
                                    I(loadfolder),
                                    I(id),
                                    I(folder),
                                    I(so.getContextId()));
                            }
                            lms.iFDeleteLinkFromObject(del, second, id, type, folder, loadid, loadfolder, loadtype, so
                                    .getContextId());
                            cnt++;
                        } catch (final OXException e) {
                            LOG.error("Unable to delete Link!", e);
                            resp.add(new int[] { loadid, loadtype, loadfolder });
                        }
                    }
                }
            }
            if (cnt == 0) {
                for (final int[] tmp : data) {
                    resp.add(tmp);
                }
            }
        } catch (final SQLException e) {
            throw LinkExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt) + ',' + getStatement(del));
        } finally {
            closeSQLStuff(rs, stmt);
            closeSQLStuff(del);
        }

        return resp.toArray(new int[resp.size()][]);
    }

    public static void deleteAllObjectLinks(final int id, final int type, final int cid, final Connection writecon) throws OXException {
        // TODO RIGHTS CHECK on onject id and fid!
        /*
         * this right check is realy not requiered because this method only
         * comes up when we delete an object and all its links. and at this
         * point, all rights are already checked
         */
        final LinksSql lms = new LinksMySql();
        PreparedStatement stmt = null;
        try {
            stmt = writecon.prepareStatement(lms.iFdeleteAllObjectLinks());
            stmt.setInt(1, id);
            stmt.setInt(2, type);
            stmt.setInt(3, id);
            stmt.setInt(4, type);
            stmt.setInt(5, cid);
            stmt.execute();
        } catch (final SQLException e) {
            throw LinkExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /**
     * Deletes all links whose objects references the specified folder ID
     *
     * @param folderId The folder ID
     * @param cid The context ID
     * @param writecon A connection with write capability
     * @throws OXException If deleting all folder links fails
     */
    public static void deleteAllFolderLinks(final int folderId, final int cid, final Connection writecon) throws OXException {
        final LinksSql lms = new LinksMySql();
        PreparedStatement stmt = null;
        try {
            stmt = writecon.prepareStatement(lms.iFdeleteAllFolderLinks());
            int pos = 1;
            stmt.setInt(pos++, folderId);
            stmt.setInt(pos++, folderId);
            stmt.setInt(pos++, cid);
            stmt.execute();
        } catch (final SQLException e) {
            throw LinkExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            closeSQLStuff(stmt);
        }
    }

    public static LinkObject[] getAllLinksByObjectID(final int id, final int type, final int user, final int[] group, final Session so, final Connection readcon) throws OXException {
        final List<LinkObject> tmp = new ArrayList<LinkObject>();
        Statement stmt = null;
        ResultSet rs = null;
        final LinksSql lms = new LinksMySql();
        try {
            stmt = readcon.createStatement();
            rs = stmt.executeQuery(lms.iFgetAllLinksByObjectID(id, type, so.getContextId()));
            while (rs.next()) {
                final LinkObject lo = new LinkObject(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs
                        .getInt(5), rs.getInt(6), rs.getInt(7));
                final boolean isFirstReadable;
                {
                    final ModuleAccess firstAccess = modules.get(Integer.valueOf(lo.getFirstType()));
                    isFirstReadable = firstAccess.supportsAccessByID() ? firstAccess.isReadableByID(lo.getFirstId(),
                            user, group, so) : firstAccess.isReadable(lo.getFirstId(), lo.getFirstFolder(), user,
                            group, so);
                }
                final boolean isSecondReadable;
                {
                    final ModuleAccess secondAccess = modules.get(Integer.valueOf(lo.getSecondType()));
                    isSecondReadable = secondAccess.supportsAccessByID() ? secondAccess.isReadableByID(
                            lo.getSecondId(), user, group, so) : secondAccess.isReadable(lo.getSecondId(), lo
                            .getSecondFolder(), user, group, so);
                }
                if (isFirstReadable && isSecondReadable) {
                    tmp.add(lo);
                }
            }
        } catch (final SQLException e) {
            throw LinkExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            closeSQLStuff(rs, stmt);
        }
        return tmp.toArray(new LinkObject[tmp.size()]);
    }
}
