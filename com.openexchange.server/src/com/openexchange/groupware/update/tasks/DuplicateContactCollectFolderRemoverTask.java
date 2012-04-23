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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderEventConstants;
import com.openexchange.groupware.calendar.CalendarCache;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.Collections.SmartIntArray;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderSQL;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMapManagement;

/**
 * {@link DuplicateContactCollectFolderRemoverTask} - Removes duplicate contact collector folders.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DuplicateContactCollectFolderRemoverTask extends UpdateTaskAdapter {

    private static final String[] DEPENDENCIES = { MailAccountAddPersonalTask.class.getName() };

    public DuplicateContactCollectFolderRemoverTask() {
        super();
    }

    @Override
    public int addedWithVersion() {
        return 98;
    }

    @Override
    public int getPriority() {
        return UpdateTaskPriority.HIGH.priority;
    }

    @Override
    public String[] getDependencies() {
        return DEPENDENCIES;
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        /*
         * Logger
         */
        final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(DuplicateContactCollectFolderRemoverTask.class));
        /*
         * Progress state
         */
        final ProgressState status = params.getProgressState();
        /*
         * Get all contexts with contained users
         */
        final TIntObjectMap<List<Integer>> m = new TIntObjectHashMap<List<Integer>>();
        final int total = getAllUsers(params.getContextId(), m);
        status.setTotal(total);
        /*
         * Iterate per context
         */
        final Map<Locale, String> names = new HashMap<Locale, String>(4);
        m.forEachEntry(new TIntObjectProcedure<List<Integer>>() {

            @Override
            public boolean execute(final int currentContextId, final List<Integer> list) {
                try {
                    iterateUsersPerContext(list, names, currentContextId, status, log);
                } catch (final OXException e) {
                    final StringBuilder sb = new StringBuilder(128);
                    sb.append("DuplicateContactCollectFolderRemoverTask experienced an error while removing duplicate contact collect folders for users in context ");
                    sb.append(currentContextId);
                    sb.append(":\n");
                    sb.append(e.getMessage());
                    log.error(sb.toString(), e);
                }
                return true;
            }
        });
    }

    private static int getAllUsers(final int contextId, final TIntObjectMap<List<Integer>> m) throws OXException {
        final Connection con = Database.getNoTimeout(contextId, true);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, id FROM user");
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return 0;
            }
            int total = 0;
            do {
                final int cid = rs.getInt(1);
                final Integer user = Integer.valueOf(rs.getInt(2));
                final List<Integer> l;
                if (!m.containsKey(cid)) {
                    l = new ArrayList<Integer>();
                    m.put(cid, l);
                } else {
                    l = m.get(cid);
                }
                l.add(user);
                total++;
            } while (rs.next());
            return total;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.backNoTimeout(contextId, true, con);
        }
    }

    static void iterateUsersPerContext(final List<Integer> users, final Map<Locale, String> names, final int contextId, final ProgressState status, final Log log) throws OXException {
        /*
         * Create context instance
         */
        final Context ctx;
        {
            final ContextImpl ctxi = new ContextImpl(contextId);
            ctxi.setMailadmin(getContextMailAdmin(contextId));
            ctx = ctxi;
        }
        /*
         * Iterate users
         */
        for (final Integer user : users) {
            /*
             * Fetch write-connection
             */
            final Connection writeCon;
            try {
                writeCon = Database.getNoTimeout(contextId, true);
                writeCon.setAutoCommit(false); // BEGIN
            } catch (final SQLException e) {
                // Auto-Commit mode could not be changed
                throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            }
            try {
                checkDuplicates4User(user, ctx, names, writeCon, log);
                status.incrementState();
                writeCon.commit(); // COMMIT
            } catch (final SQLException e) {
                rollback(writeCon);
                throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            }
            /*
             * catch (final OXException e) { rollback(writeCon); throw e; }
             */
            catch (final Exception e) {
                rollback(writeCon);
                throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                /*
                 * Release write-connection
                 */
                autocommit(writeCon); // RESTORE AUTO-COMMIT
                Database.backNoTimeout(contextId, true, writeCon);
            }
        }
    }

    private static void checkDuplicates4User(final Integer user, final Context ctx, final Map<Locale, String> names, final Connection writeCon, final Log log) {
        final int contextId = ctx.getContextId();
        /*
         * Process user
         */
        try {
            final int userId = user.intValue();
            final int parent = new OXFolderAccess(writeCon, ctx).getDefaultFolder(userId, FolderObject.CONTACT).getObjectID();
            int[] duplicateIDs;
            {
                final String name = getLocalizedName(names, userId, contextId, writeCon);
                duplicateIDs = getExistingContactCollectorFolderIDs(name, parent, userId, contextId, writeCon);
            }
            /*
             * Check if more than one contact collector folder is detected
             */
            if (duplicateIDs.length > 1) {
                /*
                 * Sort IDs
                 */
                Arrays.sort(duplicateIDs);
                int contactCollectorID = getContactCollectorFolderID(userId, contextId, writeCon);
                if (contactCollectorID <= 0) {
                    /*
                     * No folder is marked as contact collector folder; mark first one to be the contact collect folder
                     */
                    contactCollectorID = duplicateIDs[0];
                    setContactCollectorFolderID(contactCollectorID, userId, contextId, writeCon);
                    /*
                     * ... and strip first bucket from duplicate IDs
                     */
                    final int[] temp = duplicateIDs;
                    duplicateIDs = new int[temp.length - 1];
                    System.arraycopy(temp, 1, duplicateIDs, 0, duplicateIDs.length);
                } else {
                    int index = -1;
                    for (int i = 0; -1 == index && i < duplicateIDs.length; i++) {
                        if (duplicateIDs[i] == contactCollectorID) {
                            index = i;
                        }
                    }
                    if (index >= 0) {
                        /*
                         * Strip found bucket from duplicate IDs
                         */
                        final int[] temp = duplicateIDs;
                        final int mlen = temp.length - 1;
                        duplicateIDs = new int[mlen];
                        System.arraycopy(temp, 0, duplicateIDs, 0, index);
                        if (index < mlen) {
                            // Copy rest
                            System.arraycopy(temp, index + 1, duplicateIDs, index, mlen - index);
                        }
                    }
                }
                /*-
                 * For each duplicate folder:
                 *
                 * 1. If it contains contacts: Move all contacts to contact collect folder
                 * 2. Delete folder
                 */
                final long now = System.currentTimeMillis();
                for (int i = 0; i < duplicateIDs.length; i++) {
                    final int duplicateID = duplicateIDs[i];
                    if (Contacts.containsAnyObjectInFolder(duplicateID, writeCon, ctx)) {
                        moveContacts(duplicateID, contactCollectorID, now, userId, ctx, writeCon);
                    }
                    deleteFolder(duplicateID, parent, now, userId, ctx, writeCon, log);
                }
            }
        } catch (final SQLException e) {
            final StringBuilder sb = new StringBuilder(128);
            sb.append("DuplicateContactCollectFolderRemoverTask experienced an error while removing duplicate contact collect folders for user ");
            sb.append(user).append(" in context ");
            sb.append(contextId);
            sb.append(":\n");
            sb.append(e.getMessage());
            log.error(sb.toString(), e);
        } catch (final OXException e) {
            final StringBuilder sb = new StringBuilder(128);
            sb.append("DuplicateContactCollectFolderRemoverTask experienced an error while removing duplicate contact collect folders for user ");
            sb.append(user).append(" in context ");
            sb.append(contextId);
            sb.append(":\n");
            sb.append(e.getMessage());
            log.error(sb.toString(), e);
        }
    }

    private static String getLocalizedName(final Map<Locale, String> names, final int userId, final int contextId, final Connection writeCon) throws SQLException {
        final Locale l;
        {
            final Locale userLocale = getUserLocale(userId, contextId, writeCon);
            l = userLocale == null ? Locale.ENGLISH : userLocale;
        }

        String name = names.get(l);
        if (null == name) {
            name = StringHelper.valueOf(l).getString(FolderStrings.DEFAULT_CONTACT_COLLECT_FOLDER_NAME);
            names.put(l, name);
        }
        return name;
    }

    private static int getContextMailAdmin(final int cid) throws OXException {
        final Connection writeCon = Database.getNoTimeout(cid, true);
        try {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = writeCon.prepareStatement("SELECT user FROM user_setting_admin WHERE cid = ?");
                stmt.setInt(1, cid);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return -1;
            } catch (final SQLException e) {
                throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            } finally {
                closeSQLStuff(rs, stmt);
            }
        } finally {
            Database.backNoTimeout(cid, true, writeCon);
        }
    }

    private static Locale getUserLocale(final int userId, final int cid, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT preferredLanguage FROM user WHERE cid = ? AND id = ?");
            stmt.setInt(1, cid);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return LocaleTools.getLocale(rs.getString(1));
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static int[] getExistingContactCollectorFolderIDs(final String name, final int parent, final int userId, final int cid, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT fuid FROM oxfolder_tree WHERE cid = ? AND parent = ? AND created_from = ? AND fname = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, parent);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, name);
            rs = stmt.executeQuery();

            final SmartIntArray sia = new SmartIntArray(16);
            while (rs.next()) {
                sia.append(rs.getInt(1));
            }

            return sia.toArray();
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static int getContactCollectorFolderID(final int userId, final int cid, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT contact_collect_folder FROM user_setting_server WHERE cid = ? AND user = ?");
            stmt.setInt(1, cid);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return -1;
            }
            final int id = rs.getInt(1);
            if (rs.wasNull()) {
                return -1;
            }
            return id;
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static void setContactCollectorFolderID(final int id, final int userId, final int cid, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE user_setting_server SET contact_collect_folder = ? WHERE cid = ? AND user = ?");
            stmt.setInt(1, id);
            stmt.setInt(2, cid);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static void moveContacts(final int from, final int to, final long now, final int userId, final Context ctx, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE prg_contacts SET fid = ?, changing_date = ?, changed_from = ? WHERE cid = ? AND fid = ?");
            int pos = 1;
            stmt.setInt(pos++, to);
            stmt.setLong(pos++, now);
            final int admin = ctx.getMailadmin();
            stmt.setInt(pos++, admin > 0 ? admin : userId);
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, from);
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static void deleteFolder(final int id, final int parent, final long now, final int userId, final Context ctx, final Connection con, final Log log) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            /*
             * Delete permissions
             */
            stmt = con.prepareStatement("DELETE FROM oxfolder_permissions WHERE cid = ? AND fuid = ?");
            final int cid = ctx.getContextId();
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setLong(pos++, id);
            stmt.executeUpdate();
            closeSQLStuff(stmt);
            /*
             * Delete folder
             */
            stmt = con.prepareStatement("DELETE FROM oxfolder_tree WHERE cid = ? AND fuid = ?");
            pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setLong(pos++, id);
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
        /*
         * Update parent's last-modified time stamp
         */
        final int admin = ctx.getMailadmin();
        OXFolderSQL.updateLastModified(parent, now, admin > 0 ? admin : userId, con, ctx);
        /*
         * Update caches
         */
        ConditionTreeMapManagement.dropFor(ctx.getContextId());
        try {
            if (FolderCacheManager.isEnabled()) {
                FolderCacheManager.getInstance().removeFolderObject(id, ctx);
                FolderCacheManager.getInstance().removeFolderObject(parent, ctx);
            }
            broadcastEvent(id, true, userId, ctx.getContextId(), ServerServiceRegistry.getInstance().getService(EventAdmin.class));
            broadcastEvent(parent, true, userId, ctx.getContextId(), ServerServiceRegistry.getInstance().getService(EventAdmin.class));
            if (CalendarCache.isInitialized()) {
                CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
            }
        } catch (final OXException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void broadcastEvent(final int fuid, final boolean deleted, final int entity, final int contextId, final EventAdmin eventAdmin) {
        if (null == eventAdmin) {
            return;
        }
        final Dictionary<String, Object> properties = new Hashtable<String, Object>(6);
        properties.put(FolderEventConstants.PROPERTY_CONTEXT, Integer.valueOf(contextId));
        properties.put(FolderEventConstants.PROPERTY_USER, Integer.valueOf(entity));
        properties.put(FolderEventConstants.PROPERTY_FOLDER, String.valueOf(fuid));
        properties.put(FolderEventConstants.PROPERTY_CONTENT_RELATED, Boolean.valueOf(!deleted));
        /*
         * Create event with push topic
         */
        final Event event = new Event(FolderEventConstants.TOPIC, properties);
        /*
         * Finally deliver it
         */
        eventAdmin.sendEvent(event);
    }

}
