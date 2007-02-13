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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.Component;

@OXExceptionSource(
		classId = Classes.COM_OPENEXCHANGE_GROUPWARE_TX_DBSERVICE, 
		component = Component.TRANSACTION
)
public abstract class DBService implements Service, DBProviderUser, DBProvider{
	private RequestDBProvider provider;
	
	private static final Log LOG = LogFactory.getLog(DBService.class);
	private static final TXExceptionFactory EXCEPTIONS = new TXExceptionFactory(DBService.class);
		
	private ThreadLocal<ThreadState> txState = new ThreadLocal<ThreadState>();
	
	private static final class ThreadState {
		public List<Undoable> undoables = new ArrayList<Undoable>();
		public boolean preferWriteCon = false;
		public Set<Connection> writeCons = new HashSet<Connection>();
	}
	
	
	public DBProvider getProvider() {
		return this.provider;
	}
	
	public void setProvider(DBProvider provider) {
		this.provider = new RequestDBProvider(provider);
		this.provider.setTransactional(true);
	}
	
	public DBService(){}
	
	public DBService(DBProvider provider) {
		setProvider(provider);
	}

	public Connection getReadConnection(Context ctx) throws TransactionException {
		if(txState.get() != null && txState.get().preferWriteCon) {
			return getWriteConnection(ctx);
		}
		return provider.getReadConnection(ctx);
	}

	public Connection getWriteConnection(Context ctx) throws TransactionException {
		Connection writeCon = provider.getWriteConnection(ctx);
		if(txState.get() != null && txState.get().preferWriteCon) {
			txState.get().writeCons.add(writeCon);
			return writeCon;
		} else if(txState.get() != null){
			txState.get().preferWriteCon = true;
		}
		
		return writeCon;
	}

	public void releaseReadConnection(Context ctx, Connection con) {
		if(txState.get() != null && txState.get().preferWriteCon && txState.get().writeCons.contains(con)){
			releaseWriteConnection(ctx,con);
			return;
		}
		provider.releaseReadConnection(ctx, con);
	}

	public void releaseWriteConnection(Context ctx, Connection con) {
		if(txState.get() != null && txState.get().preferWriteCon) {
			txState.get().writeCons.remove(con);
		}
		provider.releaseWriteConnection(ctx, con);
	}

	public void commitDBTransaction() throws TransactionException {
		provider.commit();
	}

	public void commitDBTransaction(Undoable undo) throws TransactionException {
		provider.commit();
		addUndoable(undo);
	}

	public void rollbackDBTransaction() throws TransactionException {
		provider.rollback();
	}

	public void startDBTransaction() throws TransactionException {
		provider.startTransaction();
	}
	
	public void finishDBTransaction() throws TransactionException {
		provider.finish();
	}
	
	public void startTransaction() throws TransactionException {
		txState.set(new ThreadState());
	}
	
	public void finish() throws TransactionException {
		provider.finish();
		txState.set(null);
	}
	
	@OXThrows(category=Category.INTERNAL_ERROR, desc="This transaction could not be fully undone. Some components are probably not consistent anymore. Run the recovery tool!", exceptionId=1, msg="This transaction could not be fully undone. Some components are probably not consistent anymore. Run the recovery tool!")
	public void rollback() throws TransactionException {
		List<Undoable> failed = new ArrayList<Undoable>();
		List<Undoable> undos = new ArrayList<Undoable>(txState.get().undoables);
		Collections.reverse(undos);
		for(Undoable undo : undos) {
			try {
				undo.undo();
			} catch (AbstractOXException x) {
				LOG.fatal(x.getMessage(),x);
				failed.add(undo);
			}
		}
		if(failed.size() != 0) {
			TransactionException exception = EXCEPTIONS.create(1);
			if(LOG.isFatalEnabled()) {
				StringBuilder explanations = new StringBuilder();
				for(Undoable undo : failed) {
					explanations.append(undo.error());
					explanations.append("\n");
				}
				LOG.fatal(explanations.toString(),exception);
			}
			throw exception;
		}
	}
	
	public void commit() throws TransactionException {
	
	}
	
	public void setRequestTransactional(boolean transactional) {
		provider.setRequestTransactional(transactional);
	}
		
	protected void close(PreparedStatement stmt, ResultSet rs) {
		if(stmt != null)
			try {
				stmt.close();
			} catch (SQLException e) {
				LOG.debug("",e);
			}
		if(rs != null)
			try {
				rs.close();
			} catch (SQLException e) {
				LOG.debug("",e);
			}
	}
	protected void addUndoable(Undoable undo) throws TransactionException {
		if(null == txState.get() || null == txState.get().undoables) 
			return;
		txState.get().undoables.add(undo);
	}
	
	protected void perform(UndoableAction action, boolean dbTransaction) throws AbstractOXException {
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
		} catch (AbstractOXException e) {	
			if(dbTransaction)
				rollbackDBTransaction();
			throw e;
		} finally {
			if(dbTransaction)
				finishDBTransaction();
		}
	}
	@Deprecated
	public void setTransactional(boolean tx){}
	
	public boolean inTransaction(){
		return txState.get() != null;
	}
}
