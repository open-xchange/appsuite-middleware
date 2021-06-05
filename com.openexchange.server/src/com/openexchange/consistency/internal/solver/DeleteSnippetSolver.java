/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.consistency.internal.solver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import com.openexchange.consistency.Entity;
import com.openexchange.database.Databases;
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
    
    
    /**
     * Initialises a new {@link DeleteSnippetSolver}.
     */
    public DeleteSnippetSolver() {
        super();
    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteSnippetSolver.class);

    @Override
    public void solve(final Entity entity, final Set<String> problems) {
        // Now we go through the set an delete each superfluous entry:
        for (Iterator<String> it = problems.iterator(); it.hasNext();) {
            String old_identifier = it.next();

            Connection con = null;
            PreparedStatement stmt = null;
            int rollback = 0;
            try {
                con = Database.get(entity.getContext(), true);
                con.setAutoCommit(false);
                rollback = 1;

                int contextId = entity.getContext().getContextId();
                // Not recoverable
                if (DBUtils.tableExists(con, "snippet")) {
                    stmt = con.prepareStatement("DELETE FROM snippet WHERE cid=? AND refId=? AND refType=1");
                    int pos = 0;
                    stmt.setInt(++pos, contextId);
                    stmt.setString(++pos, old_identifier);
                    stmt.executeUpdate();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }

                con.commit();
                rollback = 2;
            } catch (SQLException | OXException | RuntimeException e) {
                LOG.error("{}", e.getMessage(), e);
            } finally {
                Databases.closeSQLStuff(stmt);
                if (rollback > 0) {
                    if (rollback==1) {
                        Databases.rollback(con);
                    }
                    Databases.autocommit(con);
                }
                if (null != con) {
                    Database.back(entity.getContext(), true, con);
                }
            }
        }
    }

    @Override
    public String description() {
        return "delete snippet";
    }
}
