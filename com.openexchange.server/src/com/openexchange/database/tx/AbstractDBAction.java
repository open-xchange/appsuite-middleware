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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tx.AbstractUndoable;
import com.openexchange.tx.UndoableAction;

public abstract class AbstractDBAction extends AbstractUndoable implements UndoableAction {

	static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AbstractDBAction.class));

	private DBProvider provider = null;
	private Context context = null;

	/**
	 * Initializes a new {@link AbstractDBAction}.
	 */
	protected AbstractDBAction() {
	    super();
	}

	protected int doUpdates(final UpdateBlock...updates) throws OXException {
		Connection writeCon = null;
		UpdateBlock current = null;
		int counter = 0;
		try {
			writeCon = getProvider().getWriteConnection(getContext());
			for(final UpdateBlock update : updates) {
				current = update;
				counter += current.performUpdate(writeCon);
				current.close();
			}
		} catch (final SQLException e) {
			throw new OXException(e);
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

    protected int doUpdates(final List<UpdateBlock> updates) throws OXException, OXException {
        return doUpdates(updates.toArray(new UpdateBlock[updates.size()]));
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
		protected final String sql;
		protected String statementString;

		public Update(final String sql) {
			this.sql = sql;
		}

		@Override
        public void close() {
			DBUtils.closeSQLStuff(rs, stmt);
		}

		@Override
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
