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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.calendar.CalendarCommonCollection;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactExceptionFactory;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link Links} - Provides static access to link module
 * 
 * @author <a href="mailto:ben.pahne@open-xchange.com">Benjamin Frederic
 *         Pahne</a>
 */
@OXExceptionSource(classId = 1, component = EnumComponent.LINKING)
public class Links {

    private static final ContactExceptionFactory EXCEPTIONS = new ContactExceptionFactory(Links.class);

    private static final Log LOG = LogFactory.getLog(Links.class);

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
         * @throws ContextException If specified context ID cannot be resolved
         */
        boolean isReadable(int oid, int folder, int user, int[] group, Session so) throws ContextException;

        /**
         * Checks if denoted item is readable by specified user.
         * 
         * @param oid The item's unique ID
         * @param user The user ID
         * @param group The user's group IDs
         * @param so The session
         * @return <code>true</code> if denoted item is readable by specified
         *         user; otherwise <code>false</code>
         * @throws ContextException If specified context ID cannot be resolved
         * @throws UnsupportedOperationException If method is not supported
         * @see #supportsAccessByID()
         */
        boolean isReadableByID(int oid, int user, int[] group, Session so) throws ContextException,
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
         * @throws ContextException If session's context ID cannot be resolved
         */
        boolean hasModuleRights(Session so) throws ContextException;
    }

    private static final Map<Integer, ModuleAccess> modules;

    /*
     * Some Modules are deprecated but you never know what comes
     */
    static {
        modules = new HashMap<Integer, ModuleAccess>(4);
        modules.put(Integer.valueOf(Types.APPOINTMENT), new ModuleAccess() {
            public boolean supportsAccessByID() {
                return true;
            }

            public boolean isReadable(final int oid, final int fid, final int user, final int[] group, final Session so)
                    throws ContextException {
                final Context ct = ContextStorage.getStorageContext(so.getContextId());
                if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ct).hasCalendar()) {
                    return false;
                }
                try {
                    return CalendarCommonCollection.getReadPermission(oid, fid, so, ct);
                } catch (final OXException ox) {
                    LOG.error("UNABLE TO CHECK CALENDAR READRIGHT FOR LINK", ox);
                    return false;
                }
            }

            public boolean hasModuleRights(final Session so) throws ContextException {
                final Context ct = ContextStorage.getStorageContext(so.getContextId());
                if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ct).hasCalendar()) {
                    return false;
                }
                return true;
            }

            public boolean isReadableByID(final int oid, final int user, final int[] group, final Session so)
                    throws ContextException {
                final Context ct = ContextStorage.getStorageContext(so.getContextId());
                if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ct).hasCalendar()) {
                    return false;
                }
                int fid = -1;
                try {
                    fid = com.openexchange.groupware.calendar.Tools.getAppointmentFolder(oid, user, ct);
                } catch (OXObjectNotFoundException x) {
                    // may not read and is not participant
                    return false;
                } catch (OXException ox) {
                    LOG.error("UNABLE TO CHECK CALENDAR READRIGHT FOR LINK", ox);
                    return false;
                }
                try {
                    return CalendarCommonCollection.getReadPermission(oid,fid, so, ct);
                } catch (final OXException ox) {
                    LOG.error("UNABLE TO CHECK CALENDAR READRIGHT FOR LINK", ox);
                    return false;
                }
            }
        });
        modules.put(Integer.valueOf(Types.TASK), new ModuleAccess() {
            public boolean supportsAccessByID() {
                return true;
            }

            public boolean isReadable(final int oid, final int fid, final int user, final int[] group, final Session so)
                    throws ContextException {
                final Context ctx = ContextStorage.getStorageContext(so.getContextId());
                final UserConfiguration userConfig = UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx);
                if (!userConfig.hasTask()) {
                    return false;
                }
                return com.openexchange.groupware.tasks.Task2Links.checkMayReadTask(so, ctx, userConfig, oid, fid);
            }

            public boolean hasModuleRights(final Session so) throws ContextException {
                final Context ct = ContextStorage.getStorageContext(so.getContextId());
                if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ct).hasTask()) {
                    return false;
                }
                return true;
            }

            public boolean isReadableByID(final int oid, final int user, final int[] group, final Session so) throws ContextException {
                final Context ctx = ContextStorage.getStorageContext(so.getContextId());
                final UserConfiguration userConfig = UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx);
                if (!userConfig.hasTask()) {
                    return false;
                }
                return com.openexchange.groupware.tasks.Task2Links.checkMayReadTask(so, ctx, userConfig, oid);
            }
        });
        modules.put(Integer.valueOf(Types.CONTACT), new ModuleAccess() {
            public boolean supportsAccessByID() {
                return false;
            }

            public boolean isReadable(final int oid, final int fid, final int user, final int[] group, final Session so)
                    throws ContextException {
                final Context ct = ContextStorage.getStorageContext(so.getContextId());

                if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ct).hasContact()) {
                    return false;
                }
                try {
                    return Contacts.performContactReadCheckByID(oid, user, group, ct, UserConfigurationStorage
                            .getInstance().getUserConfigurationSafe(so.getUserId(), ct));
                } catch (final Exception e) {
                    // System.out.println("UNABLE TO CHECK CONTACT READRIGHT FOR LINK");
                    LOG.error("UNABLE TO CHECK CONTACT READRIGHT FOR LINK", e);
                    return false;
                }
            }

            public boolean hasModuleRights(final Session so) throws ContextException {
                final Context ct = ContextStorage.getStorageContext(so.getContextId());
                if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ct).hasContact()) {
                    return false;
                }
                return true;
            }

            public boolean isReadableByID(final int oid, final int user, final int[] group, final Session so)
                    throws ContextException {
                throw new UnsupportedOperationException("isReadableByID() not supported for contact module");
            }
        });
        modules.put(Integer.valueOf(Types.INFOSTORE), new ModuleAccess() {
            public boolean supportsAccessByID() {
                return false;
            }

            public boolean isReadable(final int oid, final int fid, final int user, final int[] group, final Session so)
                    throws ContextException {
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

            public boolean hasModuleRights(final Session so) throws ContextException {
                final Context ct = ContextStorage.getStorageContext(so.getContextId());
                if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ct).hasInfostore()) {
                    return false;
                }
                return true;
            }

            public boolean isReadableByID(final int oid, final int user, final int[] group, final Session so) {
                throw new UnsupportedOperationException("isReadableByID() not supported for infostore module");
            }
        });
    }

    /**
     * Prevent instantiation
     */
    private Links() {
        super();
    }

    @OXThrowsMultiple(category = { Category.PERMISSION, Category.USER_INPUT, Category.CODE_ERROR, Category.CODE_ERROR,
            Category.CODE_ERROR }, desc = { "", "", "", "", "" }, exceptionId = { 0, 1, 2, 3, 4 }, msg = {
            "Unable to create a link between these two objects. Insufficient rights. 1. Object %1$d Folder %2$d 2. Object %3$d Folder %4$d Context %5$d",
            "Unable to create a link between these two objects. This link already exists. 1. Object %1$d Folder %2$d 2. Object %3$d Folder %4$d Context %5$d",
            ContactException.INIT_CONNECTION_FROM_DBPOOL,
            "An error occurred. Unable to save this linking between those two objects. 1. Object %1$d Folder %2$d 2. Object %3$d Folder %4$d Context %5$d",
            "An error occurred. Unable to save this linking between those two objects. 1. Object %1$d Folder %2$d 2. Object %3$d Folder %4$d Context %5$d" })
    public static void performLinkStorage(final LinkObject l, final int user, final int[] group, final Session so,
            final Connection writecon) throws OXException, ContextException {

        final Context ct = ContextStorage.getStorageContext(so.getContextId());

        if (!modules.get(Integer.valueOf(l.getFirstType())).isReadable(l.getFirstId(), l.getFirstFolder(), user, group,
                so)
                || !modules.get(Integer.valueOf(l.getSecondType())).isReadable(l.getSecondId(), l.getSecondFolder(),
                        user, group, so)) {
            throw EXCEPTIONS
                    .create(0, Integer.valueOf(l.getFirstId()), Integer.valueOf(l.getFirstFolder()), Integer.valueOf(l
                            .getSecondId()), Integer.valueOf(l.getSecondFolder()), Integer.valueOf(so.getContextId()));
            // throw new
            // OXException("THIS LINK IS NOT VISIBLE TO THE USER. MISSING READRIGHTS FOR ONE OR BOTH OBJECTS");
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
                throw EXCEPTIONS.create(1, Integer.valueOf(l.getFirstId()), Integer.valueOf(l.getFirstFolder()),
                        Integer.valueOf(l.getSecondId()), Integer.valueOf(l.getSecondFolder()), Integer.valueOf(so
                                .getContextId()));
                // throw new OXException("This Link allready exists");
            }
        } catch (final DBPoolingException se) {
            throw EXCEPTIONS.create(2, se);
        } catch (final SQLException se) {
            throw EXCEPTIONS
                    .create(3, Integer.valueOf(l.getFirstId()), Integer.valueOf(l.getFirstFolder()), Integer.valueOf(l
                            .getSecondId()), Integer.valueOf(l.getSecondFolder()), Integer.valueOf(so.getContextId()));
        } catch (final OXException se) {
            throw se;
            // throw new OXException("UNABLE TO SAVE LINK",se);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException sqle) {
                LOG.error("Unable to close Statement or ResultSet", sqle);
            }
            if (readCon != null) {
                DBPool.closeReaderSilent(ct, readCon);
            }
        }

        PreparedStatement ps = null;
        try {
            ps = writecon.prepareStatement(lms.iFperformLinkStorageInsertString());
            ps.setInt(1, l.getFirstId());
            ps.setInt(2, l.getFirstType());
            ps.setInt(3, l.getFirstFolder());
            ps.setInt(4, l.getSecondId());
            ps.setInt(5, l.getSecondType());
            ps.setInt(6, l.getSecondFolder());
            ps.setInt(7, l.getContectId());
            ps.execute();
        } catch (final SQLException se) {
            throw EXCEPTIONS.create(4, se, Integer.valueOf(l.getFirstId()), Integer.valueOf(l.getFirstFolder()),
                    Integer.valueOf(l.getSecondId()), Integer.valueOf(l.getSecondFolder()), Integer.valueOf(so
                            .getContextId()));
            // throw new OXException("UNABLE TO SAVE LINK",se);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (final SQLException se) {
                LOG.error("Unable to close Statement", se);
            }
        }
    }

    @OXThrowsMultiple(category = { Category.PERMISSION, Category.PERMISSION, Category.CODE_ERROR }, desc = { "", "", "" }, exceptionId = {
            5, 6, 7 }, msg = {
            "Unable to create a link between these two objects. Insufficient rights. 1. Object %1$d 2. Object %2$d Context %3$d",
            "Unable to create a link between these two objects. Insufficient rights. 1. Object %1$d Folder %2$d 2. Object %3$d Folder %4$d Context %5$d",
            "An error occurred. Unable to load some links for this objects. 1. Object %1$d 2. Object %2$d Context %3$d" })
    public static LinkObject getLinkFromObject(final int first_id, final int first_type, final int second_id,
            final int second_type, final int user, final int[] group, final Session so, final Connection readcon)
            throws OXException, ContextException {

        if (!modules.get(Integer.valueOf(first_type)).hasModuleRights(so)
                || !modules.get(Integer.valueOf(second_type)).hasModuleRights(so)) {
            throw EXCEPTIONS.create(5, Integer.valueOf(first_id), Integer.valueOf(second_id), Integer.valueOf(so
                    .getContextId()));
            // throw new
            // OXException("ONE OF THE REQUESTED MODULES IS NOT VISIBLE TO THE USER, MAYBE BOTH.");
        }

        LinkObject lo = null;
        Statement stmt = null;
        ResultSet rs = null;
        final LinksSql lms = new LinksMySql();
        try {
            stmt = readcon.createStatement();
            rs = stmt.executeQuery(lms.iFgetLinkFromObject(first_id, first_type, second_id, second_type, so
                    .getContextId()));

            if (rs.next()) {
                lo = new LinkObject(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6),
                        rs.getInt(7));
                if (!modules.get(Integer.valueOf(lo.getFirstType())).isReadable(lo.getFirstId(), lo.getFirstFolder(),
                        user, group, so)
                        || !modules.get(Integer.valueOf(lo.getSecondType())).isReadable(lo.getSecondId(),
                                lo.getSecondFolder(), user, group, so)) {
                    throw EXCEPTIONS.create(6, Integer.valueOf(lo.getFirstId()), Integer.valueOf(lo.getFirstFolder()),
                            Integer.valueOf(lo.getSecondId()), Integer.valueOf(lo.getSecondFolder()), Integer
                                    .valueOf(so.getContextId()));
                    // throw new
                    // OXException("THIS LINK IS NOT VISIBLE TO THE USER. MISSING READRIGHTS FOR ONE OR BOTH OBJECTS");
                }
            }
        } catch (final SQLException sql) {
            throw EXCEPTIONS.create(7, sql, Integer.valueOf(first_id), Integer.valueOf(second_id), Integer.valueOf(so
                    .getContextId()));
            // throw new OXException("UNABLE TO LOAD LINKOBJECT ",sql);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException sqle) {
                LOG.error("Unable to close Statement or ResultSet", sqle);
            }
        }
        return lo;
    }

    @OXThrows(category = Category.CODE_ERROR, desc = "", exceptionId = 9, msg = "Unable to load all links for the object. Object %1$d Folder %2$d User %3$d Context %4$d")
    public static LinkObject[] getAllLinksFromObject(final int id, final int type, final int folderId, final int user,
            final int[] group, final Session so, final Connection readcon) throws OXException, ContextException {
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
        } catch (final SQLException sql) {
            throw EXCEPTIONS.create(9, sql, Integer.valueOf(id), Integer.valueOf(folderId), Integer.valueOf(user),
                    Integer.valueOf(so.getContextId()));
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
        return tmp.toArray(new LinkObject[tmp.size()]);
    }

    @OXThrowsMultiple(category = { Category.PERMISSION, Category.CODE_ERROR }, desc = { "", "" }, exceptionId = { 10,
            11 }, msg = {
            "Unable to create a link between these two objects. Insufficient rights. Object %1$d Folder %2$d Context %3$d",
            "An error occurred. Unable to delete some links from this objects. Object %1$d Folder %2$d Context %3$d" })
    public static int[][] deleteLinkFromObject(final int id, final int type, final int folder, final int[][] data,
            final int user, final int[] group, final Session so, final Connection readcon, final Connection writecon)
            throws OXException, ContextException {
        Statement smt = null;
        Statement del = null;
        ResultSet rs = null;
        final LinksSql lms = new LinksMySql();
        final List<int[]> resp = new ArrayList<int[]>();

        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder("Fetching rights for Module: " + type + " id:" + id + " folder:" + folder
                    + " user:" + user + " group:" + group));
        }

        if (!modules.get(Integer.valueOf(type)).isReadable(id, folder, user, group, so)) {
            // System.out.println("Unable to delete Link");
            for (final int[] tmp : data) {
                resp.add(tmp);
            }
        }

        try {
            del = writecon.createStatement();
            smt = readcon.createStatement();
            rs = smt.executeQuery(lms.iFgetAllLinksFromObject(id, type, folder, so.getContextId()));
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
                            if (!modules.get(Integer.valueOf(loadtype)).isReadable(loadid, loadfolder, user, group, so)) {
                                throw EXCEPTIONS.create(10, Integer.valueOf(loadid), Integer.valueOf(loadfolder),
                                        Integer.valueOf(so.getContextId()));
                                // throw new OXException("NO RIGHT");
                            }
                            lms.iFDeleteLinkFromObject(del, second, id, type, folder, loadid, loadfolder, loadtype, so
                                    .getContextId());
                            cnt++;
                        } catch (final OXException ox) {
                            LOG.error("Unable to delete Link!", ox);
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
        } catch (final SQLException se) {
            throw EXCEPTIONS.create(11, se, Integer.valueOf(id), Integer.valueOf(folder), Integer.valueOf(so
                    .getContextId()));
            // throw new OXException("UNABLE TO DELETE LINKS",se);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (smt != null) {
                    smt.close();
                }
            } catch (final SQLException sqle) {
                LOG.error("Unable to close Statement or ResultSet", sqle);
            }
            try {
                if (del != null) {
                    del.close();
                }
            } catch (final SQLException sqle) {
                LOG.error("Unable to close Statement", sqle);
            }
        }

        return resp.toArray(new int[resp.size()][]);
    }

    @OXThrows(category = Category.CODE_ERROR, desc = "", exceptionId = 12, msg = "Unable to delete all links from this objects. Object %1$d Context %2$d")
    public static void deleteAllObjectLinks(final int id, final int type, final int cid, final Connection writecon)
            throws OXException {
        // TODO RIGHTS CHECK on onject id and fid!
        /*
         * this right check is realy not requiered because this method only
         * comes up when we delete an object and all its links. and at this
         * point, all rights are already checked
         */
        final LinksSql lms = new LinksMySql();
        PreparedStatement ps = null;
        try {
            ps = writecon.prepareStatement(lms.iFdeleteAllObjectLinks());
            ps.setInt(1, id);
            ps.setInt(2, type);
            ps.setInt(3, id);
            ps.setInt(4, type);
            ps.setInt(5, cid);
            ps.execute();
        } catch (final SQLException se) {
            throw EXCEPTIONS.create(12, se, Integer.valueOf(id), Integer.valueOf(cid));
            // throw new OXException("UNABLE TO DELETE LINKS FROM OBJECT "+id,
            // se);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (final SQLException sqle) {
                LOG.error("Unable to close Statement", sqle);
            }
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
    @OXThrows(category = Category.CODE_ERROR, desc = "", exceptionId = 13, msg = "Unable to delete all links from folder. Folder %1$d Context %2$d")
    public static void deleteAllFolderLinks(final int folderId, final int cid, final Connection writecon)
            throws OXException {
        final LinksSql lms = new LinksMySql();
        PreparedStatement ps = null;
        try {
            ps = writecon.prepareStatement(lms.iFdeleteAllFolderLinks());
            int pos = 1;
            ps.setInt(pos++, folderId);
            ps.setInt(pos++, folderId);
            ps.setInt(pos++, cid);
            ps.execute();
        } catch (final SQLException se) {
            throw EXCEPTIONS.create(12, se, Integer.valueOf(folderId), Integer.valueOf(cid));
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (final SQLException e) {
                    LOG.error("Unable to close Statement", e);
                }
            }
        }
    }

    @OXThrows(category = Category.CODE_ERROR, desc = "", exceptionId = 14, msg = "Unable to load all links for the object. Object %1$d User %2$d Context %3$d")
    public static LinkObject[] getAllLinksByObjectID(final int id, final int type, final int user, final int[] group,
            final Session so, final Connection readcon) throws OXException, ContextException {
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
        } catch (final SQLException sql) {
            throw EXCEPTIONS.create(14, sql, Integer.valueOf(id), Integer.valueOf(user), Integer.valueOf(so
                    .getContextId()));
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
        return tmp.toArray(new LinkObject[tmp.size()]);
    }
}
