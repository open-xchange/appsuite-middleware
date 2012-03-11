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

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link ContactDeleteListener} - The delete listener for contact module
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ContactDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link ContactDeleteListener}
     */
    public ContactDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(final DeleteEvent deleteEvent, final Connection readCon, final Connection writeCon) throws OXException {
        try {
            if (deleteEvent.getType() == DeleteEvent.TYPE_USER) {
                /*
                 * Drop affected distribution list entries
                 */
                dropDListEntries("prg_dlist", "prg_contacts", deleteEvent.getId(), deleteEvent.getContext().getContextId(), writeCon);
                dropDListEntries("del_dlist", "del_contacts", deleteEvent.getId(), deleteEvent.getContext().getContextId(), writeCon);
                /*
                 * Proceed
                 */
                Contacts.trashAllUserContacts(deleteEvent.getId(), deleteEvent.getSession(), readCon, writeCon);
            }
        } catch (final OXException ox) {
            throw new OXException(ox);
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

}
