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

package com.openexchange.groupware.contact;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.event.impl.EventClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.log.LogFactory;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link ContactDeleteListener} - The delete listener for contact module
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ContactDeleteListener implements DeleteListener {

    private static Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ContactDeleteListener.class));

    /**
     * Initializes a new {@link ContactDeleteListener}
     */
    public ContactDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(final DeleteEvent deleteEvent, final Connection readCon, final Connection writeCon) throws OXException {
        if (deleteEvent.getType() == DeleteEvent.TYPE_USER) {
            /*
             * Drop affected distribution list entries
             */
            dropDListEntries("prg_dlist", "prg_contacts", deleteEvent.getId(), deleteEvent.getContext().getContextId(), writeCon);
            dropDListEntries("del_dlist", "del_contacts", deleteEvent.getId(), deleteEvent.getContext().getContextId(), writeCon);
            /*
             * Proceed
             */
            trashAllUserContacts(deleteEvent.getContext(), deleteEvent.getId(), deleteEvent.getSession(), readCon, writeCon);
        }
    }

    private static void dropDListEntries(final String dlistTable, final String contactTable, final int userId, final int contextId, final Connection writeCon) throws OXException {
        String sql = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            /*
             * Get those distribution lists which carry the user as an entry
             */
            sql =
                "SELECT d.intfield01, d.intfield02 FROM " + dlistTable + " AS d JOIN " + contactTable + " AS c ON d.cid = ? AND c.cid = ? AND d.intfield02 = c.intfield01 WHERE c.userId IS NOT NULL AND c.userId = ?";
            stmt = writeCon.prepareStatement(sql);
            stmt.setInt(1, contextId);
            stmt.setInt(2, contextId);
            stmt.setInt(3, userId);
            rs = stmt.executeQuery();
            final List<int[]> l = new ArrayList<int[]>();
            while (rs.next()) {
                l.add(new int[] { rs.getInt(1), rs.getInt(2) }); // distribution-list-id, contact-id
            }
            DBUtils.closeSQLStuff(rs, stmt);
            /*
             * Delete the entries which refer to the user which should be deleted
             */
            sql = "DELETE FROM " + dlistTable + " WHERE cid = ? AND intfield01 = ? AND intfield02 = ?";
            stmt = writeCon.prepareStatement(sql);
            for (final int[] arr : l) {
                stmt.setInt(1, contextId);
                stmt.setInt(2, arr[0]);
                stmt.setInt(3, arr[1]);
                stmt.addBatch();
            }
            stmt.executeBatch();
            DBUtils.closeSQLStuff(rs, stmt);
            /*
             * Check if any distribution list has no entry after deleting user's entries
             */
            final TIntList toDelete = new TIntArrayList();
            sql = "SELECT COUNT(intfield02) FROM " + dlistTable + " WHERE cid = ? AND intfield01 = ?";
            for (final int[] arr : l) {
                final int dlistId = arr[0];
                stmt = writeCon.prepareStatement(sql);
                stmt.setInt(1, contextId);
                stmt.setInt(2, dlistId);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    final int count = rs.getInt(1);
                    if (0 == count) {
                        /*
                         * The distribution list is now empty
                         */
                        toDelete.add(dlistId);
                    }
                }
                DBUtils.closeSQLStuff(rs, stmt);
            }
            /*
             * Delete empty distribution lists
             */
            if (!toDelete.isEmpty()) {
                sql = "DELETE FROM " + contactTable + " WHERE cid = ? AND intfield01 = ?";
                stmt = writeCon.prepareStatement(sql);
                for (final int id : toDelete.toArray()) {
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, id);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                DBUtils.closeSQLStuff(rs, stmt);
            }
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    /*
     * taken as-is from previous Contacts.java and ContactMySql.java implementations
     */
    private static void trashAllUserContacts(Context ct, int uid, Session so, Connection readcon, Connection writecon) throws OXException {
        Statement stmt = null;
        Statement del = null;
        ResultSet rs = null;

        try {
            final int contextId = so.getContextId();
            stmt = readcon.createStatement();
            del = writecon.createStatement();
            FolderObject contactFolder = null;

            /*
             * Get all contacts which were created by specified user. This includes the user's contact as well since the user is always the
             * creator.
             */
            rs = stmt.executeQuery(iFgetRightsSelectString(uid, contextId));

            int fid = 0;
            int oid = 0;
            int created_from = 0;
            boolean delete = false;
            int pflag = 0;

            final EventClient ec = new EventClient(so);
            OXFolderAccess oxfs = null;

            while (rs.next()) {
                delete = false;
                oid = rs.getInt(1);
                fid = rs.getInt(5);
                created_from = rs.getInt(6);
                pflag = rs.getInt(7);
                if (rs.wasNull()) {
                    pflag = 0;
                }

                boolean folder_error = false;

                try {
                    if (FolderCacheManager.isEnabled()) {
                        contactFolder = FolderCacheManager.getInstance().getFolderObject(fid, true, ct, readcon);
                    } else {
                        contactFolder = FolderObject.loadFolderObjectFromDB(fid, ct, readcon);
                    }
                    if (contactFolder.getModule() != FolderObject.CONTACT) {
                        throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(fid), I(contextId), I(uid));
                    }
                    if (contactFolder.getType() == FolderObject.PRIVATE) {
                        delete = true;
                    }

                } catch (final Exception oe) {
                    if (LOG.isWarnEnabled()) {
                        final StringBuilder sb = new StringBuilder(128);
                        sb.append("WARNING: During the delete process 'delete all contacts from one user', a contact was found who has no folder.");
                        sb.append("This contact will be modified and can be found in the administrator address book.");
                        sb.append(" Context=").append(contextId);
                        sb.append(" Folder=").append(fid);
                        sb.append(" User=").append(uid);
                        sb.append(" Contact=").append(oid);
                        LOG.warn(sb.toString());
                    }
                    folder_error = true;
                    delete = true;
                }

                if (folder_error && (pflag == 0)) {
                    try {
                        final int mailadmin = ct.getMailadmin();
                        if (null == oxfs) {
                            oxfs = new OXFolderAccess(readcon, ct);
                        }
                        final FolderObject xx = oxfs.getDefaultFolder(mailadmin, FolderObject.CONTACT);

                        final int admin_folder = xx.getObjectID();
                        iFgiveUserContacToAdmin(del, oid, admin_folder, ct);
                    } catch (final Exception oxee) {
                        LOG.error("ERROR: It was not possible to move this contact (without paren folder) to the admin address book!." + "This contact will be deleted." + "Context " + contextId + " Folder " + fid + " User" + uid + " Contact" + oid, oxee);

                        folder_error = false;
                    }
                } else if (folder_error && (pflag != 0)) {
                    folder_error = false;
                }

                if (!folder_error) {
                    iFtrashAllUserContacts(delete, del, contextId, oid, uid, rs, so, ct);
                    final Contact co = new Contact();
                    try {
                        co.setCreatedBy(created_from);
                        co.setParentFolderID(fid);
                        co.setObjectID(oid);
                        ec.delete(co);
                    } catch (final Exception e) {
                        LOG.error(
                            "Unable to trigger delete event for contact delete: id=" + co.getObjectID() + " cid=" + co.getContextId(),
                            e);
                    }
                }
            }
            if (uid == ct.getMailadmin()) {
                iFtrashAllUserContactsDeletedEntriesFromAdmin(del, contextId, uid);
            } else {
                iFtrashAllUserContactsDeletedEntries(del, contextId, uid, ct);
            }
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            closeSQLStuff(rs, stmt);
            closeSQLStuff(del);
        }
    }

    private static String rightsSelectString =
        "SELECT co.intfield01,co.intfield02,co.intfield03,co.intfield04,co.fid,co.created_from,co.pflag,co.cid FROM prg_contacts AS co ";

    private static String iFgetRightsSelectString(final int uid, final int cid) {
        return new StringBuilder(rightsSelectString).append(" where created_from = ").append(uid).append(" AND cid = ").append(cid).toString();
    }

    private static void iFgiveUserContacToAdmin(final Statement smt, final int oid, final int admin_fid, final Context ct) throws SQLException {
        final StringBuilder tmp =
            new StringBuilder("UPDATE prg_contacts SET changed_from = ").append(ct.getMailadmin()).append(", created_from = ").append(
                ct.getMailadmin()).append(", changing_date = ").append(System.currentTimeMillis()).append(", fid = ").append(admin_fid).append(
                " WHERE intfield01 = ").append(oid).append(" and cid = ").append(ct.getContextId());
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.toString());
        }
        smt.execute(tmp.toString());
    }

    private static void iFtrashAllUserContacts(final boolean delete, final Statement del, final int cid, final int oid, final int uid, final ResultSet rs, final Session so, Context ctx) throws SQLException {

        final StringBuilder tmp = new StringBuilder(256);

        if (delete) {
            tmp.append("DELETE from prg_dlist where intfield01 = ").append(oid).append(" AND cid = ").append(cid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("DELETE from prg_contacts_linkage where (intfield01 = ").append(oid).append(" OR intfield02 = ").append(oid).append(
                ") AND cid = ").append(cid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("DELETE from prg_contacts_image where intfield01 = ").append(oid).append(" AND cid = ").append(cid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("DELETE from prg_contacts WHERE cid = ").append(cid).append(" AND intfield01 = ").append(oid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            // FIXME quick fix. deleteRow doesn't work because del.execute
            // creates new resultset
            del.execute(tmp.toString());
            // rs.deleteRow();

        } else {
            /*
             * tmp = newStringBuilder( "INSERT INTO del_contacts_image SELECT * FROM prg_contacts_image WHERE intfield01 = " + oid +
             * " AND  cid = "+cid); LOG.debug(tmp.toString()); del.execute(tmp.toString()); tmp = new
             * StringBuilder("DELETE from prg_contacts_image where intfield01 = " +oid+" AND cid = "+cid); LOG.debug(tmp.toString());
             * del.execute(tmp.toString()); tmp = newStringBuilder( "INSERT INTO del_dlist SELECT * FROM prg_dlist WHERE intfield01 = " +
             * oid + " AND  cid = "+cid); LOG.debug(tmp.toString()); del.execute(tmp.toString()); tmp = new
             * StringBuilder("DELETE FROM prg_dlist WHERE cid = " + cid + " AND intfield01 = " + oid); LOG.debug(tmp.toString());
             * del.execute(tmp.toString()); tmp = new StringBuilder("DELETE from prg_contacts_linkage where (intfield01 = "
             * +oid+" OR intfield02 = "+oid+") AND cid = "+cid); LOG.debug(tmp.toString()); del.execute(tmp.toString()); tmp =
             * newStringBuilder( "INSERT INTO del_contacts SELECT * FROM prg_contacts WHERE intfield01 = " + oid + " AND  cid = "+cid);
             * LOG.debug(tmp.toString()); del.execute(tmp.toString()); tmp = new StringBuilder("DELETE from prg_contacts WHERE cid = "+cid
             * +" AND intfield01 = "+oid); LOG.debug(tmp.toString()); del.execute(tmp.toString()); // rs.deleteRow(); tmp = new
             * StringBuilder("UPDATE del_contacts SET changed_from = "+ so.getContext ().getMailadmin()+", created_from = "+so.getContext()
             * .getMailadmin()+", changing_date = "+System.currentTimeMillis()+ " WHERE intfield01 = "+oid); LOG.debug(tmp.toString());
             * del.execute(tmp.toString());
             */

            tmp.append("UPDATE prg_contacts SET changed_from = ").append(ctx.getMailadmin()).append(", created_from = ").append(
                ctx.getMailadmin()).append(", changing_date = ").append(System.currentTimeMillis()).append(" WHERE intfield01 = ").append(
                oid).append(" AND cid = ").append(cid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

        }
    }

    private static void iFtrashAllUserContactsDeletedEntriesFromAdmin(final Statement del, final int cid, final int uid) throws SQLException {
        final StringBuilder tmp =
            new StringBuilder("DELETE FROM del_contacts WHERE created_from = ").append(uid).append(" and cid = ").append(cid);
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());
    }

    private static void iFtrashAllUserContactsDeletedEntries(final Statement del, final int cid, final int uid, final Context ct) throws SQLException {
        final StringBuilder tmp =
            new StringBuilder("UPDATE del_contacts SET changed_from = ").append(ct.getMailadmin()).append(", created_from = ").append(
                ct.getMailadmin()).append(", changing_date = ").append(System.currentTimeMillis()).append(" WHERE created_from = ").append(
                uid).append(" and cid = ").append(cid);
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());
    }

}
