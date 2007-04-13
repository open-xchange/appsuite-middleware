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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.sql.DBUtils;

public abstract class AbstractDBAction extends AbstractUndoable implements
		UndoableAction {
	
	private static final Log LOG = LogFactory.getLog(AbstractDBAction.class);
	
	private DBProvider provider = null;
	private Context context = null;

	
	protected int doUpdates(final UpdateBlock...updates) throws UpdateException, TransactionException {
		Connection writeCon = null;
		UpdateBlock current = null;
		int counter = 0;
		try {
			writeCon = getProvider().getWriteConnection(getContext());
			for(UpdateBlock update : updates) {
				current = update;
				counter += current.performUpdate(writeCon);
				current.close();
			}
		} catch (SQLException e) {
			throw new UpdateException(e,current);
		} finally {
			if(current != null) {
				current.close();
			}
			if(writeCon != null) {
				provider.releaseWriteConnection(getContext(), writeCon);
			}
		}
		return counter;
	}
	
	public void setContext(final Context context) {
		this.context = context;
	}
	
	public Context getContext(){
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
	
	protected abstract class Update implements UpdateBlock{

		protected PreparedStatement stmt;
		protected ResultSet rs;
		protected String sql;
		protected String statementString = null;
		
		public Update(String sql) {
			this.sql = sql;
		}
		
		public void close() {
			DBUtils.closeSQLStuff(rs, stmt);
		}

		public int performUpdate(final Connection writeCon) throws SQLException {
			stmt = writeCon.prepareStatement(sql);
			fillStatement();
			statementString = stmt.toString();
			if(LOG.isTraceEnabled()) {
				LOG.trace(statementString);
			}
			final int updated =  stmt.executeUpdate();
			//System.out.println(String.format("%d ::: %s",updated,statementString));
			return updated;
		}
		
		public abstract void fillStatement() throws SQLException;

		public String getStatement() {
			return statementString == null ? sql : statementString;
		}
		
	}
	
	protected class UpdateException extends Exception {
		
		private static final long serialVersionUID = -3823990951502455901L;
		private UpdateBlock update;
		private SQLException sqle;

		public UpdateException(SQLException sqle, UpdateBlock update) {
			super(sqle);
			this.sqle = sqle;
			this.update = update;
		}
		
		public SQLException getSQLException(){
			return sqle;
		}
		
		public UpdateBlock getUpdate(){
			return update;
		}
		
		public String getStatement(){
			return update.getStatement();
		}
	}
}
