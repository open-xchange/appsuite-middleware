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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.consistency.solver;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link DeleteSnippetSolver}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class DeleteSnippetSolver implements ProblemSolver {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteSnippetSolver.class);

    @Override
    public void solve(final Context ctx, final Set<String> problems) {
        // Now we go through the set an delete each superfluous entry:
        final Iterator<String> it = problems.iterator();
        while (it.hasNext()) {
            Connection con = null;
            PreparedStatement stmt = null;
            boolean rollback = false;
            try {
                con = Database.get(ctx, true);
                con.setAutoCommit(false);
                rollback = true;

                final int contextId = ctx.getContextId();
                final String old_identifier = it.next();
                // Not recoverable
                if (DBUtils.tableExists(con, "snippet")) {
                    stmt = con.prepareStatement("DELETE FROM snippet WHERE cid=? AND refId=? AND refType=1");
                    int pos = 0;
                    stmt.setInt(++pos, contextId);
                    stmt.setString(++pos, old_identifier);
                    stmt.executeUpdate();
                    DBUtils.closeSQLStuff(stmt);
                    stmt = null;
                }
                // Partly recoverable
                if (DBUtils.tableExists(con, "snippetAttachment")) {
                    final List<int[]> pairs = new LinkedList<int[]>();
                    {
                        ResultSet rs = null;
                        try {
                            stmt = con.prepareStatement("SELECT user, id FROM snippetAttachment WHERE cid=? AND referenceId=?");
                            int pos = 0;
                            stmt.setInt(++pos, contextId);
                            stmt.setString(++pos, old_identifier);
                            rs = stmt.executeQuery();
                            while (rs.next()) {
                                pairs.add(new int[] { rs.getInt(1), rs.getInt(2) });
                            }
                        } finally {
                            DBUtils.closeSQLStuff(rs, stmt);
                        }
                    }
                    for (final int[] pair : pairs) {
                        final int userId = pair[0];
                        final int id = pair[1];
                        deleteSnippet(id, userId, contextId, con);
                    }
                }
                con.commit();
                rollback = false;
            } catch (final SQLException e) {
                LOG.error("", e);
            } catch (final OXException e) {
                LOG.error("", e);
            } catch (final RuntimeException e) {
                LOG.error("", e);
            } finally {
                if (rollback) {
                    DBUtils.rollback(con);
                }
                DBUtils.closeSQLStuff(stmt);
                if (null != con) {
                    DBUtils.autocommit(con);
                    Database.back(ctx, true, con);
                }
            }
        }
    }

    private void deleteSnippet(final int id, final int userId, final int contextId, final Connection con) {
        PreparedStatement stmt = null;
        try {
            // Delete attachments
            stmt = con.prepareStatement("DELETE FROM snippetAttachment WHERE cid=? AND user=? AND id=?");
            int pos = 0;
            stmt.setLong(++pos, contextId);
            stmt.setLong(++pos, userId);
            stmt.setLong(++pos, id);
            stmt.executeUpdate();
            closeSQLStuff(stmt);
            stmt = null;
            // Delete content
            stmt = con.prepareStatement("DELETE FROM snippetContent WHERE cid=? AND user=? AND id=?");
            pos = 0;
            stmt.setLong(++pos, contextId);
            stmt.setLong(++pos, userId);
            stmt.setLong(++pos, id);
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(stmt);
            stmt = null;
            // Delete JSON object
            stmt = con.prepareStatement("DELETE FROM snippetMisc WHERE cid=? AND user=? AND id=?");
            pos = 0;
            stmt.setLong(++pos, contextId);
            stmt.setLong(++pos, userId);
            stmt.setLong(++pos, id);
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(stmt);
            stmt = null;
            // Delete unnamed properties
            final int confId;
            {
                ResultSet rs = null;
                try {
                    stmt = con.prepareStatement("SELECT refId FROM snippet WHERE cid=? AND user=? AND id=? AND refType=0");
                    pos = 0;
                    stmt.setLong(++pos, contextId);
                    stmt.setLong(++pos, userId);
                    stmt.setString(++pos, Integer.toString(id));
                    rs = stmt.executeQuery();
                    confId = rs.next() ? Integer.parseInt(rs.getString(1)) : -1;
                } finally {
                    closeSQLStuff(rs, stmt);
                    stmt = null;
                    rs = null;
                }
            }
            if (confId > 0) {
                stmt = con.prepareStatement("DELETE FROM genconf_attributes_strings WHERE cid = ? AND id = ?");
                pos = 0;
                stmt.setLong(++pos, contextId);
                stmt.setLong(++pos, confId);
                stmt.executeUpdate();
                DBUtils.closeSQLStuff(stmt);
                stmt = con.prepareStatement("DELETE FROM genconf_attributes_bools WHERE cid = ? AND id = ?");
                pos = 0;
                stmt.setLong(++pos, contextId);
                stmt.setLong(++pos, confId);
                stmt.executeUpdate();
                DBUtils.closeSQLStuff(stmt);
            }
            // Delete snippet
            stmt = con.prepareStatement("DELETE FROM snippet WHERE cid=? AND user=? AND id=? AND refType=0");
            pos = 0;
            stmt.setLong(++pos, contextId);
            stmt.setLong(++pos, userId);
            stmt.setString(++pos, Integer.toString(id));
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(stmt);
            stmt = null;
        } catch (final SQLException e) {
            LOG.error("", e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    public String description() {
        return "delete snippet";
    }
}
