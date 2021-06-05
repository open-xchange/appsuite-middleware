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

package com.openexchange.database.provider;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionConstants;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Streams;

public class RequestDBProvider implements DBProvider {

    private static final ThreadLocal<DBTransaction> txIds = new ThreadLocal<DBTransaction>();

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RequestDBProvider.class);
    private boolean commits = true;


    public static class DBTransaction {
        public Connection writeConnection;
        public Connection readConnection;
        public Context ctx;
        public boolean transactional;

        List<Throwable> readConnectionStacks = new ArrayList<Throwable>();
        public boolean autoCommit;

        public boolean commit = true;

        public DBTransaction() {
            for(int i = 0; i < 10; i++) {
                readConnectionStacks.add(null);
            }
        }
    }

    private final ThreadLocal<Integer> readCount = new ThreadLocal<Integer>(){

        @Override
        protected final Integer initialValue() {
            return 0;
        }

    };


    public RequestDBProvider(){}

    public RequestDBProvider(final DBProvider provider) {
        setProvider(provider);
    }

    private boolean transactional;

    private DBProvider provider;

    public DBProvider getProvider() {
        return this.provider;
    }

    public void setProvider(final DBProvider provider){
        this.provider = provider;
    }


    // Service

    public void startTransaction() throws OXException {
        final DBTransaction tx = createTransaction();
        tx.transactional = transactional;
        tx.commit = commits;
        txIds.set(tx);
    }

    public void commit() throws OXException{
        commit(getActiveTransaction());
    }

    public void rollback() throws OXException{
        rollback(getActiveTransaction());
    }

    protected DBTransaction getActiveTransaction(){
        return txIds.get();
    }

    // Abstract Service


    protected DBTransaction createTransaction() {
        final DBTransaction tx = new DBTransaction();
        return tx;
    }

    protected void commit(final DBTransaction tx) throws OXException {
        try {
            if (tx.writeConnection != null && !tx.writeConnection.getAutoCommit()) {
                if (tx.commit) {
                    tx.writeConnection.commit();
                }
            }
        } catch (SQLException e) {
            throw com.openexchange.database.DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    protected void rollback(final DBTransaction tx) throws OXException {
        if (tx == null) {
            return;
        }
        try {
            if (tx.writeConnection!=null) {
                if (tx.writeConnection.getAutoCommit()) {
                    throw new IllegalStateException("This request cannot be rolled back because it wasn't part of a transaction");
                }
                tx.writeConnection.rollback();
            }
        } catch (SQLException e) {
            throw com.openexchange.database.DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public Connection getReadConnection(final Context ctx) throws OXException{
        final DBTransaction tx = getActiveTransaction();
        int rc = readCount.get();
        if (tx != null && tx.ctx == null) {
            tx.ctx = ctx;
        }
        if (tx != null && tx.writeConnection != null) {
            return tx.writeConnection;
        }
        if (tx != null && tx.readConnection != null) {
            final Throwable t = new Throwable();
            rc++;
            t.fillInStackTrace();
            tx.readConnectionStacks.set(rc, t);
            readCount.set(rc);
            return tx.readConnection;
        }

        final Connection readCon = getProvider().getReadConnection(ctx);
        LOG.debug("---> {}", readCon);
        if (tx != null) {
            tx.readConnection = readCon;
            rc++;
            final Throwable t = new Throwable();
            t.fillInStackTrace();
            tx.readConnectionStacks.set(rc, t);
            readCount.set(rc);
        }
        return readCon;
    }

    @Override
    public Connection getWriteConnection(final Context ctx) throws OXException{
        final DBTransaction tx = getActiveTransaction();
        if (tx == null) {
            return getProvider().getWriteConnection(ctx);
        }
        final int rc = readCount.get();
        if (rc>0) {
            throw new IllegalStateException("Don't use a read and write connection in parallel. Read Connections in use: "+rc, tx.readConnectionStacks.get(rc));
        }
        if (tx.writeConnection != null) {
            return tx.writeConnection;
        }
        tx.writeConnection = getProvider().getWriteConnection(ctx);
        try {
            tx.autoCommit = tx.writeConnection.getAutoCommit();
            if (tx.writeConnection.getAutoCommit() == tx.transactional) {
                tx.writeConnection.setAutoCommit(!tx.transactional);
            }
        } catch (SQLException e) {
            throw new OXException(OXExceptionConstants.CODE_DEFAULT, e.getMessage(), e);
        }
        if (tx != null && tx.ctx == null) {
            tx.ctx = ctx;
        }
        return tx.writeConnection;
    }

    @Override
    public void releaseReadConnection(final Context ctx, final Connection con){
        final DBTransaction tx = getActiveTransaction();
        //if (tx == null) {
        //    throw new IllegalStateException("There is no transaction active at the moment.");
        //}
        if (tx != null && tx.writeConnection != null && tx.writeConnection.equals(con)) {
            return;
        }
        if (tx != null && tx.readConnection != null && tx.readConnection.equals(con)) {
            int rc = readCount.get();
            rc--;
            if (rc==0) {
                LOG.debug("<--- {}", con);
                getProvider().releaseReadConnection(ctx,con);
                tx.readConnection=null;
            }
            readCount.set(rc);
            return;
        }
        if (tx != null) {
            return;
        }
        LOG.debug("<--- {}", con);
        getProvider().releaseReadConnection(ctx,con);

    }

    @Override
    public void releaseWriteConnection(final Context ctx, final Connection con){
        final DBTransaction tx = getActiveTransaction();
        if (tx == null) {
            getProvider().releaseWriteConnection(ctx, con);
        }
    }

    @Override
    public void releaseWriteConnectionAfterReading(final Context ctx, final Connection con) {
        final DBTransaction tx = getActiveTransaction();
        if (tx == null) {
            getProvider().releaseWriteConnectionAfterReading(ctx, con);
        }
    }

    public void finish() throws OXException {
        final DBTransaction tx = getActiveTransaction();
        if (tx == null) {
            return;
        }
        try {
            if (tx.writeConnection != null) {
                if (tx.writeConnection.getAutoCommit() != tx.autoCommit) {
                    tx.writeConnection.setAutoCommit(tx.autoCommit);
                }
                getProvider().releaseWriteConnection(tx.ctx,tx.writeConnection);
                tx.writeConnection = null;
            }
            if (tx.readConnection != null) {
                getProvider().releaseReadConnection(tx.ctx,tx.readConnection);
                readCount.set(0);
                tx.readConnection = null;
            }
        } catch (SQLException e) {
            throw com.openexchange.database.DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
        txIds.set(null);
        final DBProvider prov = getProvider();
        if (prov instanceof Closeable) {
            Streams.close((Closeable) prov);
        }
    }

    public void setTransactional(final boolean transactional) {
        this.transactional = transactional;
    }

    public boolean isTransactional(){
        final DBTransaction tx = getActiveTransaction();
        if (tx != null && tx.transactional) {
            return true;
        }
        return this.transactional;
    }

    public void setRequestTransactional(final boolean transactional) {
        final DBTransaction tx = getActiveTransaction();
        if (tx == null) {
            throw new IllegalStateException("No Transaction Active");
        }
        if (tx.writeConnection != null && transactional) {
            throw new IllegalStateException("Cannot switch on transaction after a write occurred");
        }
        tx.transactional = transactional;
    }

    public void setCommitsTransaction(final boolean commits) {
        this.commits = commits;
    }
}
