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
import com.openexchange.filestore.FileStorage;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link CreateDummyFileForSnippetSolver}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class CreateDummyFileForSnippetSolver extends CreateDummyFileSolver implements ProblemSolver {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CreateDummyFileForSnippetSolver.class);

    public CreateDummyFileForSnippetSolver(final FileStorage storage) {
        super(storage);
    }

    @Override
    public void solve(final Entity entity, final Set<String> problems) {
        /*
         * Here we operate in two stages. First we create a dummy entry in the filestore. Second we update the Entries in the database
         */
        final int size = problems.size();
        final Iterator<String> it = problems.iterator();
        for (int k = 0; k < size; k++) {
            Connection con = null;
            PreparedStatement stmt = null;
            try {
                con = Database.get(entity.getContext(), true);
                final String old_identifier = it.next();
                // Not recoverable
                if (DBUtils.tableExists(con, "snippet")) {
                    stmt = con.prepareStatement("DELETE FROM snippet WHERE cid=? AND refId=? AND refType=1");
                    int pos = 0;
                    stmt.setInt(++pos, entity.getContext().getContextId());
                    stmt.setString(++pos, old_identifier);
                    stmt.executeUpdate();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }
                // Partly recoverable
                if (DBUtils.tableExists(con, "snippetAttachment")) {
                    final String identifier = createDummyFile(storage);
                    stmt = con.prepareStatement("UPDATE snippetAttachment SET referenceId=? WHERE cid=? AND referenceId=?");
                    int pos = 0;
                    stmt.setString(++pos, identifier);
                    stmt.setInt(++pos, entity.getContext().getContextId());
                    stmt.setString(++pos, old_identifier);
                    stmt.executeUpdate();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }
            } catch (SQLException | OXException | RuntimeException e) {
                LOG.error("{}", e.getMessage(), e);
            } finally {
                Databases.closeSQLStuff(stmt);
                if (null != con) {
                    Database.back(entity.getContext(), true, con);
                }
            }
        }
    }

    @Override
    public String description() {
        return "Create dummy file for snippet";
    }

}
