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

package com.openexchange.database.tx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tx.AbstractUndoable;
import com.openexchange.tx.UndoableAction;

public abstract class AbstractDBAction extends AbstractUndoable implements UndoableAction {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractDBAction.class);

    private DBProvider provider = null;
    private Context context = null;

    /**
     * Initializes a new {@link AbstractDBAction}.
     */
    protected AbstractDBAction() {
        super();
    }

    /**
     * Performs given update blocks.
     *
     * @param updates The update blocks
     * @return The number of updated rows
     * @throws OXException If an error occurs
     */
    protected int doUpdates(final UpdateBlock... updates) throws OXException {
        Connection writeCon = null;
        UpdateBlock current = null;
        int counter = 0;
        try {
            writeCon = getProvider().getWriteConnection(getContext());
            for (final UpdateBlock update : updates) {
                current = update;
                counter += current.performUpdate(writeCon);
                current.close();
            }
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (current != null) {
                try {
                    current.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
            if (writeCon != null) {
                provider.releaseWriteConnection(getContext(), writeCon);
            }
        }
        return counter;
    }

    protected int doUpdates(final List<UpdateBlock> updates) throws OXException {
        return doUpdates(updates.toArray(new UpdateBlock[updates.size()]));
    }

    public void setContext(final Context context) {
        this.context = context;
    }

    public Context getContext() {
        return this.context;
    }

    public void setProvider(final DBProvider provider) {
        this.provider = provider;
    }

    public DBProvider getProvider() {
        return provider;
    }

    protected interface UpdateBlock {

        public int performUpdate(Connection writeCon) throws SQLException;

        public String getStatement();

        public void close();
    }

    protected abstract class Update implements UpdateBlock {

        protected PreparedStatement stmt;
        protected ResultSet rs;
        protected final String sql;
        protected String statementString;

        public Update(final String sql) {
            this.sql = sql;
        }

        @Override
        public void close() {
            Databases.closeSQLStuff(rs, stmt);
        }

        @Override
        public int performUpdate(final Connection writeCon) throws SQLException {
            stmt = writeCon.prepareStatement(sql);
            fillStatement();
            LOG.trace("{}", stmt);
            return stmt.executeUpdate();
        }

        public abstract void fillStatement() throws SQLException;

        @Override
        public String getStatement() {
            return statementString == null ? sql : statementString;
        }

    }

    protected static class OXExceptionRenamed extends Exception {

        private static final long serialVersionUID = -3823990951502455901L;
        private final UpdateBlock update;
        private final SQLException sqle;

        public OXExceptionRenamed(final SQLException sqle, final UpdateBlock update) {
            super(sqle);
            this.sqle = sqle;
            this.update = update;
        }

        public SQLException getSQLException() {
            return sqle;
        }

        public UpdateBlock getUpdate() {
            return update;
        }

        public String getStatement() {
            return update.getStatement();
        }
    }
}
