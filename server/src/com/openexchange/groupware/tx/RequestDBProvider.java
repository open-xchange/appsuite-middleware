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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.tx;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajp13.AJPv13ListenerThread;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.contexts.Context;

import com.openexchange.groupware.AbstractOXException.Category;

@OXExceptionSource(classId=Classes.COM_OPENEXCHANGE_GROUPWARE_TX_REQUESTDBPROVIDER, component= Component.TRANSACTION)

@OXThrowsMultiple(
		category={Category.SUBSYSTEM_OR_SERVICE_DOWN,Category.SUBSYSTEM_OR_SERVICE_DOWN,Category.SUBSYSTEM_OR_SERVICE_DOWN},
		desc={"","",""},
		exceptionId={0,1,2},
		msg={"Cannot commit transaction to write DB", "Cannot rollback transaction in write DB", "Cannot finish transaction"}
)
public class RequestDBProvider implements DBProvider {

	private static final ThreadLocal<DBTransaction> txIds = new ThreadLocal<DBTransaction>();

	private static final Log LOG = LogFactory.getLog(RequestDBProvider.class);
	private static final TXExceptionFactory EXCEPTIONS = new TXExceptionFactory(RequestDBProvider.class);
    private boolean commits;


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
	
	public void startTransaction() throws TransactionException {
		final DBTransaction tx = createTransaction();
		tx.transactional = transactional;
		tx.commit = commits;
        txIds.set(tx);
	}
	
	public void commit() throws TransactionException{
		checkThreadDeath();
		commit(getActiveTransaction());
	}
	
	private void checkThreadDeath() {
		final Thread current = Thread.currentThread();
		if (current instanceof AJPv13ListenerThread) {
			final AJPv13ListenerThread ajpv13Thread = (AJPv13ListenerThread) current;
			if(ajpv13Thread.isDead()) {
				try {
					rollback();
				} catch (final TransactionException x) {
					LOG.debug("",x);
				}
				try {
					finish();
				} catch (final TransactionException x) {
					LOG.debug("",x);
				}
				
				throw new ThreadDeath();
				
			}
		}
	}

	public void rollback() throws TransactionException{
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

	protected void commit(final DBTransaction tx) throws TransactionException {
		try {
			if (tx.writeConnection != null && !tx.writeConnection.getAutoCommit()) {
				if(tx.commit) {
                    tx.writeConnection.commit();                        
                }
            }
		} catch (final SQLException e) {
			throw EXCEPTIONS.create(0,e);
		}
	}

	protected void rollback(final DBTransaction tx) throws TransactionException {
		if(tx == null) {
			return;
		}
		try {
			if(tx.writeConnection!=null) {
				if(tx.writeConnection.getAutoCommit()) {
					throw new IllegalStateException("This request cannot be rolled back because it wasn't part of a transaction");
				}
				tx.writeConnection.rollback();
			}
		} catch (final SQLException e) {
			throw EXCEPTIONS.create(1,e);
		}
	}
	
	public Connection getReadConnection(final Context ctx) throws TransactionException{
		checkThreadDeath();
		final DBTransaction tx = getActiveTransaction();
		int rc = readCount.get();
		if(tx != null && tx.ctx == null) {
			tx.ctx = ctx;
		}
		if(tx != null && tx.writeConnection != null) {
			return tx.writeConnection;
		}
		if(tx != null && tx.readConnection != null) {
			final Throwable t = new Throwable();
			rc++;
			t.fillInStackTrace();
			tx.readConnectionStacks.set(rc, t);
			readCount.set(rc);
			return tx.readConnection;
		}
		
		final Connection readCon = getProvider().getReadConnection(ctx);
		if(LOG.isDebugEnabled()) {
			LOG.debug("---> "+readCon);
		}
		if(tx != null) {
			tx.readConnection = readCon;
			rc++;
			final Throwable t = new Throwable();
			t.fillInStackTrace();
			tx.readConnectionStacks.set(rc, t);
			readCount.set(rc);
		}
		return readCon;
	}
	
	public Connection getWriteConnection(final Context ctx) throws TransactionException{
		checkThreadDeath();
		final DBTransaction tx = getActiveTransaction();
		if(tx == null) {
			return getProvider().getWriteConnection(ctx);
		}
		final int rc = readCount.get();
		if(rc>0) {
			throw new IllegalStateException("Don't use a read and write connection in parallel. Read Connections in use: "+rc, tx.readConnectionStacks.get(rc));
		}
		if(tx.writeConnection != null) {
			return tx.writeConnection;
		}
		tx.writeConnection = getProvider().getWriteConnection(ctx);
		try {
			tx.autoCommit = tx.writeConnection.getAutoCommit();
			if(tx.writeConnection.getAutoCommit() == tx.transactional) {
				tx.writeConnection.setAutoCommit(!tx.transactional);
			}
		} catch (final SQLException e) {
			return null;
		}
		if(tx != null && tx.ctx == null)
			tx.ctx = ctx;
		return tx.writeConnection;
	}
	
	public void releaseReadConnection(final Context ctx, final Connection con){
		final DBTransaction tx = getActiveTransaction();
		//if(tx == null) {
		//	throw new IllegalStateException("There is no transaction active at the moment.");
		//}
		if(tx != null && tx.writeConnection != null && tx.writeConnection.equals(con)) {
			return;
		}
		if(tx != null && tx.readConnection != null && tx.readConnection.equals(con)) {
			int rc = readCount.get();
			rc--;
			if(rc==0) {
				if(LOG.isDebugEnabled()) {
					LOG.debug("<--- "+con);
				}
				getProvider().releaseReadConnection(ctx,con);
				tx.readConnection=null;
			}
			readCount.set(rc);
			return;
		}
		if(tx != null) {
			return;
		}
		if(LOG.isDebugEnabled()) {
			LOG.debug("<--- "+con);
		}
		getProvider().releaseReadConnection(ctx,con);
		
	}
	
	public void releaseWriteConnection(final Context ctx, final Connection con){
		final DBTransaction tx = getActiveTransaction();
		if(tx == null) {
			getProvider().releaseWriteConnection(ctx, con);
		}
	}
	
	public void finish() throws TransactionException {
		final DBTransaction tx = getActiveTransaction();
		if(tx == null) {
			return;
		}
		try {
			if (tx.writeConnection != null) {
				if(tx.writeConnection.getAutoCommit() != tx.autoCommit) {
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
		} catch (final SQLException e) {
			throw EXCEPTIONS.create(2,e);
		}
		txIds.set(null);
		final DBProvider prov = getProvider();
		if(prov instanceof Closeable) {
			try {
				((Closeable)prov).close();
			} catch (final IOException e) {
				LOG.debug("",e);
			}
		}
	}
	
	public void setTransactional(final boolean transactional) {
		this.transactional = transactional;
	}
	
	public boolean isTransactional(){
		final DBTransaction tx = getActiveTransaction();
		if(tx != null && tx.transactional) {
			return true;
		}
		return this.transactional;
	}
	
	public void setRequestTransactional(final boolean transactional) {
		final DBTransaction tx = getActiveTransaction();
		if(tx == null) {
			throw new IllegalStateException("No Transaction Active");
		}
		if(tx.writeConnection != null && transactional) {
			throw new IllegalStateException("Cannot switch on transaction after a write occurred");
		}
		tx.transactional = transactional;
	}


    public void setCommitsTransaction(boolean commits) {
        this.commits = commits;
    }

}
