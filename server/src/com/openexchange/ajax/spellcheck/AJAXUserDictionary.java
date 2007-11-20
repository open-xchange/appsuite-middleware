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

package com.openexchange.ajax.spellcheck;

import static com.openexchange.tools.sql.DBUtils.closeResources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.openexchange.ajax.spellcheck.AJAXUserDictionaryException.DictionaryCode;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;

/**
 * AJAXUserDictionary
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class AJAXUserDictionary implements DeleteListener, Cloneable {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AJAXUserDictionary.class);

	private static final String SQL_INSERT = "INSERT INTO user_setting_spellcheck (cid, user, user_dic) VALUES (?, ?, ?)";

	private static final String SQL_LOAD = "SELECT user_dic FROM user_setting_spellcheck WHERE cid = ? AND user = ?";

	private static final String SQL_UPDATE = "UPDATE user_setting_spellcheck SET user_dic = ? WHERE cid = ? AND user = ?";

	private static final String SQL_DELETE = "DELETE FROM user_setting_spellcheck WHERE cid = ? AND user = ?";

	private Context ctx;

	private int user;

	private Set<IgnoreCaseString> words;

	public AJAXUserDictionary() {
		super();
	}

	public AJAXUserDictionary(final int user, final Context ctx) {
		super();
		this.ctx = ctx;
		this.user = user;
		words = new HashSet<IgnoreCaseString>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		try {
			final AJAXUserDictionary clone = (AJAXUserDictionary) super.clone();
			clone.words = new HashSet<IgnoreCaseString>();
			final Iterator<IgnoreCaseString> iter = words.iterator();
			for (int i = 0, n = words.size(); i < n; i++) {
				clone.words.add((IgnoreCaseString) iter.next().clone());
			}
			return clone;
		} catch (final CloneNotSupportedException e) {
			LOG.error(e.getMessage(), e);
			throw new InternalError(e.getMessage());
		}
	}

	public void addWord(final String word) throws OXException {
		if (!validateWord(word)) {
			throw new AJAXUserDictionaryException(DictionaryCode.INVALID_WORD, word);
		}
		words.add(new IgnoreCaseString(word));
	}

	public void removeWord(final String word) {
		words.remove(new IgnoreCaseString(word));
	}

	public boolean containsWord(final String word) {
		return words.contains(new IgnoreCaseString(word));
	}

	public String[] getAllWords() {
		final String[] retval = new String[words.size()];
		int index = 0;
		final int size = words.size();
		final Iterator<IgnoreCaseString> iter = words.iterator();
		for (int i = 0; i < size; i++) {
			final IgnoreCaseString word = iter.next();
			retval[index++] = word.str;
		}
		return retval;
	}

	private boolean validateWord(final String word) {
		return (word.indexOf(',') == -1);
	}

	private void parseCommaSeperatedWords(final String csw) {
		if (csw == null) {
			return;
		}
		final IgnoreCaseString ics = new IgnoreCaseString();
		final String[] sa = csw.split(" *, *");
		for (int i = 0; i < sa.length; i++) {
			ics.setString(sa[i]);
			words.add((IgnoreCaseString) ics.clone());
		}
	}

	private String getCommaSeperatedWords() {
		final StringBuilder sb = new StringBuilder();
		final int size = words.size();
		final Iterator<IgnoreCaseString> iter = words.iterator();
		for (int i = 0; i < size; i++) {
			final IgnoreCaseString word = iter.next();
			sb.append(word.str);
			if (i < size - 1) {
				sb.append(',');
			}
		}
		return sb.toString();
	}

	public boolean saveUserDictionary() throws SQLException, DBPoolingException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection readCon = null;
		boolean insert = false;
		try {
			readCon = DBPool.pickup(ctx);
			stmt = readCon.prepareStatement(SQL_LOAD);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, user);
			rs = stmt.executeQuery();
			insert = !rs.next();
		} finally {
			closeResources(rs, stmt, readCon, true, ctx);
			rs = null;
			stmt = null;
			readCon = null;
		}
		Connection writeCon = null;
		try {
			writeCon = DBPool.pickupWriteable(ctx);
			if (insert) {
				stmt = writeCon.prepareStatement(SQL_INSERT);
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, user);
				if (words.isEmpty()) {
					stmt.setNull(3, Types.BLOB);
				} else {
					stmt.setString(3, getCommaSeperatedWords());
				}
			} else {
				stmt = writeCon.prepareStatement(SQL_UPDATE);
				if (words.isEmpty()) {
					stmt.setNull(1, Types.BLOB);
				} else {
					stmt.setString(1, getCommaSeperatedWords());
				}
				stmt.setInt(2, ctx.getContextId());
				stmt.setInt(3, user);
			}
			return (stmt.executeUpdate() == 1);
		} finally {
			closeResources(null, stmt, writeCon, false, ctx);
		}
	}

	public boolean loadUserDictionary() throws SQLException, DBPoolingException, OXException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection readCon = null;
		try {
			readCon = DBPool.pickup(ctx);
			stmt = readCon.prepareStatement(SQL_LOAD);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, user);
			rs = stmt.executeQuery();
			if (!rs.next()) {
				throw new AJAXUserDictionaryException(DictionaryCode.NOT_LOADED, Integer.valueOf(user), Integer
						.valueOf(ctx.getContextId()));
			}
			parseCommaSeperatedWords(rs.getString(1));
			return true;
		} finally {
			closeResources(rs, stmt, readCon, true, ctx);
		}
	}

	public boolean deleteUserDictionary() throws SQLException, DBPoolingException {
		return deleteUserDictionary(user, ctx, null);
	}

	private static boolean deleteUserDictionary(final int user, final Context ctx, final Connection writeConArg)
			throws SQLException, DBPoolingException {
		PreparedStatement stmt = null;
		Connection writeCon = writeConArg;
		boolean closeCon = false;
		try {
			if (writeCon == null) {
				writeCon = DBPool.pickupWriteable(ctx);
				closeCon = true;
			}
			stmt = writeCon.prepareStatement(SQL_DELETE);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, user);
			return stmt.executeUpdate() > 0;
		} finally {
			closeResources(null, stmt, closeCon ? writeCon : null, false, ctx);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return words.toString();
	}

	private static class IgnoreCaseString implements Cloneable {

		public String str;

		public IgnoreCaseString() {
			super();
		}

		public IgnoreCaseString(final String str) {
			this.str = str;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return str.toLowerCase().hashCode();
		}

		public void setString(final String str) {
			this.str = str;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object o) {
			if (o instanceof IgnoreCaseString) {
				final IgnoreCaseString otherStr = (IgnoreCaseString) o;
				return str.equalsIgnoreCase(otherStr.str);
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return str;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#clone()
		 */
		@Override
		public Object clone() {
			// First make exact bitwise copy
			IgnoreCaseString copy;
			try {
				copy = (IgnoreCaseString) super.clone();
			} catch (final CloneNotSupportedException e) {
				return null;
			}
			return copy;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.delete.DeleteListener#deletePerformed(com.openexchange.groupware.delete.DeleteEvent,
	 *      java.sql.Connection, java.sql.Connection)
	 */
	public void deletePerformed(final DeleteEvent delEvent, final Connection readConArg, final Connection writeConArg)
			throws DeleteFailedException {
		if (delEvent.getType() == DeleteEvent.TYPE_USER) {
			try {
				final Context ctx = delEvent.getContext();
				final int userId = delEvent.getId();
				/*
				 * Delete user dictionary
				 */
				deleteUserDictionary(userId, ctx, writeConArg);
			} catch (final SQLException e) {
				throw new DeleteFailedException(DeleteFailedException.Code.SQL_ERROR, e, e.getMessage());
			} catch (final DBPoolingException e) {
				throw new DeleteFailedException(e);
			}
		}
	}

}
