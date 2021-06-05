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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBProviderUser;
import com.openexchange.database.provider.LoggingDBProvider;
import com.openexchange.database.provider.RequestDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tx.ConnectionHolder;
import com.openexchange.tx.TransactionAware;
import com.openexchange.tx.TransactionExceptionCodes;
import com.openexchange.tx.Undoable;
import com.openexchange.tx.UndoableAction;

public abstract class DBService implements TransactionAware, DBProviderUser, DBProvider {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DBService.class);

    private RequestDBProvider provider;

    private final ThreadLocal<ThreadState> txState = new ThreadLocal<ThreadState>();

    private static final class ThreadState {

        protected final List<Undoable> undoables = new ArrayList<Undoable>();
        protected boolean preferWriteCon;
        private boolean foreignTransaction;
        private Connection foreignConnection;
        protected final Set<Connection> writeCons = new HashSet<Connection>();

        protected ThreadState() {
            super();
        }
    }

    public DBProvider getProvider() {
        return this.provider;
    }

    @Override
    public void setProvider(final DBProvider provider) {
        this.provider = new RequestDBProvider(new LoggingDBProvider(provider));
        this.provider.setTransactional(true);
    }

    /**
     * Initializes a new {@link DBService}.
     */
    public DBService() {
        super();
    }

    /**
     * Initializes a new {@link DBService}.
     *
     * @param provider The initial database provider instance
     */
    public DBService(final DBProvider provider) {
        super();
        setProvider(provider);
    }

    @Override
    public Connection getReadConnection(final Context ctx) throws OXException {
        final ThreadState threadState = txState.get();
        if (threadState != null) {
            if (threadState.foreignTransaction) {
                return threadState.foreignConnection;
            } else if (threadState.preferWriteCon) {
                return getWriteConnection(ctx);
            }
        }
        return provider.getReadConnection(ctx);
    }

    @Override
    public Connection getWriteConnection(final Context ctx) throws OXException {
        final ThreadState threadState = txState.get();
        if (threadState != null) {
            if (threadState.foreignTransaction) {
                return threadState.foreignConnection;
            } else if (threadState.preferWriteCon) {
                final Connection writeCon = provider.getWriteConnection(ctx);
                threadState.writeCons.add(writeCon);
                return writeCon;
            } else {
                threadState.preferWriteCon = true;
            }
        }

        return provider.getWriteConnection(ctx);
    }

    @Override
    public void releaseReadConnection(final Context ctx, final Connection con) {
        final ThreadState threadState = txState.get();
        if (threadState != null) {
            if (threadState.foreignTransaction) {
                return;
            } else if (threadState.preferWriteCon && threadState.writeCons.contains(con)) {
                releaseWriteConnectionAfterReading(ctx, con);
                return;
            }
        }
        provider.releaseReadConnection(ctx, con);
    }

    @Override
    public void releaseWriteConnection(final Context ctx, final Connection con) {
        final ThreadState threadState = txState.get();
        if (threadState != null) {
            if (threadState.foreignTransaction) {
                return;
            } else if (threadState.preferWriteCon && threadState.writeCons.contains(con)) {
                threadState.writeCons.remove(con);
            }
        }
        provider.releaseWriteConnection(ctx, con);
    }

    @Override
    public void releaseWriteConnectionAfterReading(final Context ctx, final Connection con) {
        final ThreadState threadState = txState.get();
        if (threadState != null) {
            if (threadState.foreignTransaction) {
                return;
            } else if (threadState.preferWriteCon && threadState.writeCons.contains(con)) {
                threadState.writeCons.remove(con);
            }
        }
        provider.releaseWriteConnectionAfterReading(ctx, con);
    }

    public void commitDBTransaction() throws OXException {
        provider.commit();
    }

    public void commitDBTransaction(final Undoable undo) throws OXException {
        provider.commit();
        addUndoable(undo);
    }

    public void rollbackDBTransaction() throws OXException {
        provider.rollback();
    }

    public void startDBTransaction() throws OXException {
        provider.startTransaction();
    }

    public void finishDBTransaction() throws OXException {
        provider.finish();
    }

    @Override
    public void startTransaction() throws OXException {
        ThreadState threadState = new ThreadState();
        Connection connection = ConnectionHolder.CONNECTION.get();
        try {
            if (connection != null && !connection.getAutoCommit() && !connection.isReadOnly()) {
                threadState.foreignTransaction = true;
                threadState.foreignConnection = connection;
            }
        } catch (SQLException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        }

        txState.set(threadState);
    }

    @Override
    public void finish() throws OXException {
        provider.finish();
        txState.set(null);
    }

    @Override
    public void rollback() throws OXException {
        final ThreadState threadState = txState.get();
        if (null == threadState) {
            // Nothing to do
            return;
        }
        final List<Undoable> undos = new ArrayList<Undoable>(threadState.undoables);
        if (undos.isEmpty()) {
            // Nothing to do
            return;
        }
        final List<Undoable> failed = new LinkedList<Undoable>();
        Collections.reverse(undos);
        for (final Undoable undo : undos) {
            try {
                undo.undo();
            } catch (OXException x) {
                LOG.error("", x);
                failed.add(undo);
            }
        }
        if (!failed.isEmpty()) {
            final OXException exception = TransactionExceptionCodes.NO_COMPLETE_ROLLBACK.create();
            final StringBuilder explanations = new StringBuilder();
            for (final Undoable undo : failed) {
                explanations.append(undo.error());
                explanations.append('\n');
            }
            LOG.error(explanations.toString(), exception);
            throw exception;
        }
    }

    @Override
    public void commit() throws OXException {
        // Nothing to do.
    }

    @Override
    public void setRequestTransactional(final boolean transactional) {
        provider.setRequestTransactional(transactional);
    }

    @Override
    public void setCommitsTransaction(final boolean mustCommit) {
        provider.setCommitsTransaction(false);
    }

    /**
     * Closes given SQL resources.
     *
     * @param stmt The optional statement to close
     * @param rs The optional result set to close
     */
    public void close(PreparedStatement stmt, ResultSet rs) {
        Databases.closeSQLStuff(rs, stmt);
    }

    protected void addUndoable(final Undoable undo) {
        final ThreadState threadState = txState.get();
        if (null == threadState || null == threadState.undoables) {
            return;
        }
        threadState.undoables.add(undo);
    }

    protected void perform(final UndoableAction action, final boolean dbTransaction) throws OXException {
        if (dbTransaction) {
            int rollback = 0;
            try {
                startDBTransaction();
                rollback = 1;
                
                action.perform();
                
                commitDBTransaction(action);
                rollback = 2;
            } finally {
                if (rollback == 1) {
                    try {
                        rollbackDBTransaction();
                    } catch (OXException x) {
                        log(x);
                    }
                }
                
                try {
                    finishDBTransaction();
                } catch (OXException e) {
                    log(e);
                }
            }
        } else {
            action.perform();
            addUndoable(action);
        }
    }

    @Override
    @Deprecated
    public void setTransactional(final boolean tx) {
        // Nothing to do.
    }

    public boolean inTransaction() {
        return txState.get() != null;
    }

    private void log(OXException x) {
        switch (x.getCategories().get(0).getLogLevel()) {
            case TRACE:
                LOG.trace("", x);
                break;
            case DEBUG:
                LOG.debug("", x);
                break;
            case INFO:
                LOG.info("", x);
                break;
            case WARNING:
                LOG.warn("", x);
                break;
            case ERROR:
                LOG.error("", x);
                break;
            default:
                break;
        }
    }
}
