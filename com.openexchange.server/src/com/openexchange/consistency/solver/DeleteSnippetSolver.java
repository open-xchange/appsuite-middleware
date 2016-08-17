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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import com.openexchange.consistency.Entity;
import com.openexchange.consistency.Entity.EntityType;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
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
    public void solve(final Entity entity, final Set<String> problems) {
        if (entity.getType().equals(EntityType.Context)) {
            // Now we go through the set an delete each superfluous entry:
            for (Iterator<String> it = problems.iterator(); it.hasNext();) {
                String old_identifier = it.next();

                Connection con = null;
                PreparedStatement stmt = null;
                boolean rollback = false;
                try {
                    con = Database.get(entity.getContext(), true);
                    con.setAutoCommit(false);
                    rollback = true;

                    int contextId = entity.getContext().getContextId();
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
                        Database.back(entity.getContext(), true, con);
                    }
                }
            }
        }
    }

    @Override
    public String description() {
        return "delete snippet";
    }
}
