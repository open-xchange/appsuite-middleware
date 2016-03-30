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
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBProviderUser;
import com.openexchange.database.provider.LoggingDBProvider;
import com.openexchange.database.provider.RequestDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.sql.DBUtils;
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
        if(threadState != null) {
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
        if(threadState != null) {
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
        if(threadState != null) {
            if (threadState.foreignTransaction) {
                return;
            } else if (threadState.preferWriteCon && threadState.writeCons.contains(con)){
                releaseWriteConnectionAfterReading(ctx,con);
                return;
            }
        }
        provider.releaseReadConnection(ctx, con);
    }

    @Override
    public void releaseWriteConnection(final Context ctx, final Connection con) {
        final ThreadState threadState = txState.get();
        if(threadState != null) {
            if (threadState.foreignTransaction) {
                return;
            } else if (threadState.preferWriteCon && threadState.writeCons.contains(con)){
                threadState.writeCons.remove(con);
            }
        }
        provider.releaseWriteConnection(ctx, con);
    }

    @Override
    public void releaseWriteConnectionAfterReading(final Context ctx, final Connection con) {
        final ThreadState threadState = txState.get();
        if(threadState != null) {
            if (threadState.foreignTransaction) {
                return;
            } else if (threadState.preferWriteCon && threadState.writeCons.contains(con)){
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
            } catch (final OXException x) {
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
        DBUtils.closeSQLStuff(rs, stmt);
    }

    protected void addUndoable(final Undoable undo) {
        final ThreadState threadState = txState.get();
        if(null == threadState || null == threadState.undoables) {
            return;
        }
        threadState.undoables.add(undo);
    }

    protected void perform(final UndoableAction action, final boolean dbTransaction) throws OXException {
        try {
            if(dbTransaction) {
                startDBTransaction();
            }
            action.perform();
            if(dbTransaction) {
                commitDBTransaction(action);
            } else {
                addUndoable(action);
            }
        } catch (final OXException e) {
            if(dbTransaction) {
                try {
                    rollbackDBTransaction();
                } catch (OXException x) {
                    log(x);
                }
            }
            throw e;
        } finally {
            if(dbTransaction) {
                try {
                    finishDBTransaction();
                } catch (final OXException e) {
                    log(e);
                }
            }
        }
    }

    @Override
    @Deprecated
    public void setTransactional(final boolean tx) {
        // Nothing to do.
    }

    public boolean inTransaction(){
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
