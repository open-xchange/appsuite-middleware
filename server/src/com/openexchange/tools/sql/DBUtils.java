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

package com.openexchange.tools.sql;

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.database.Database;
import com.openexchange.groupware.contexts.Context;

/**
 * Utilities for database resource handling.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBUtils {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory.getLog(DBUtils.class);

	/**
	 * Prevent instantiation
	 */
	private DBUtils() {
		super();
	}

	/**
	 * Closes the ResultSet.
	 * 
	 * @param result
	 *            <code>null</code> or a ResultSet to close.
	 */
	public static void closeSQLStuff(final ResultSet result) {
		if (result != null) {
			try {
				result.close();
			} catch (final SQLException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Closes the ResultSet and the Statement.
	 * 
	 * @param result
	 *            <code>null</code> or a ResultSet to close.
	 * @param stmt
	 *            <code>null</code> or a Statement to close.
	 */
	public static void closeSQLStuff(final ResultSet result, final Statement stmt) {
		closeSQLStuff(result);
		if (null != stmt) {
			try {
				stmt.close();
			} catch (final SQLException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * <p>
	 * Closes given <code>java.sql.ResultSet</code> and
	 * <code>java.sql.Statement</code> reference and puts back given
	 * <code>java.sql.Connection</code> reference into pool. The flag
	 * <code>isReadCon</code> determines if connection instance is of type
	 * readable or writeable.
	 * </p>
	 * <p>
	 * <b>NOTE:</b> References are not set to <code>null</code>, so the
	 * caller has to ensure that these references are not going to be used
	 * anymore.
	 * </p>
	 */
	public static void closeResources(final ResultSet rs, final Statement stmt, final Connection con,
			final boolean isReadCon, final Context ctx) {
		closeResources(rs, stmt, con, isReadCon, ctx.getContextId());
	}
	
	/**
	 * <p>
	 * Closes given <code>java.sql.ResultSet</code> and
	 * <code>java.sql.Statement</code> reference and puts back given
	 * <code>java.sql.Connection</code> reference into pool. The flag
	 * <code>isReadCon</code> determines if connection instance is of type
	 * readable or writeable.
	 * </p>
	 * <p>
	 * <b>NOTE:</b> References are not set to <code>null</code>, so the
	 * caller has to ensure that these references are not going to be used
	 * anymore.
	 * </p>
	 */
	public static void closeResources(final ResultSet rs, final Statement stmt, final Connection con,
			final boolean isReadCon, final int cid) {
		/*
		 * Close ResultSet
		 */
		if (rs != null) {
			try {
				rs.close();
			} catch (final SQLException e) {
				if (LOG.isErrorEnabled()) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		/*
		 * Close Statement
		 */
		if (stmt != null) {
			try {
				stmt.close();
			} catch (final SQLException e) {
				if (LOG.isErrorEnabled()) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		/*
		 * Close connection
		 */
		if (con != null) {
			if (isReadCon) {
				Database.back(cid, false, con);
			} else {
				Database.back(cid, true, con);
			}
		}
	}

	public static String getStatement(final Statement stmt) {
		return (stmt == null) ? "" : stmt.toString();
	}

	public static String getStatement(final PreparedStatement stmt, final String query) {
		if (stmt == null) {
			return query;
		}
		try {
			return stmt.toString();
		} catch (final Exception x) {
			return query;
		}
	}

	/**
	 * Rolls a transaction of a connection back.
	 * 
	 * @param con
	 *            connection to roll back.
	 */
	public static void rollback(final Connection con) {
		try {
			con.rollback();
		} catch (final SQLException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * This method tries to parse the truncated fields out of the DataTruncation
	 * exception. This method has been implemented because mysql doesn't return
	 * the column identifier of the truncated field through the getIndex()
	 * method of the DataTruncation exception. This method uses the fact that
	 * the exception sent by the mysql server encapsulates the truncated fields
	 * into single quotes.
	 * 
	 * @param e
	 *            DataTruncation exception to parse.
	 * @return a string array containing all truncated field from the exception.
	 */
	public static String[] parseTruncatedFields(final DataTruncation trunc) {
		final Pattern pattern = Pattern.compile("([^']*')(\\S+)('[^']*)");
		final Matcher matcher = pattern.matcher(trunc.getMessage());
		final List<String> retval = new ArrayList<String>();
		if (matcher.find()) {
			for (int i = 2; i < matcher.groupCount(); i++) {
				retval.add(matcher.group(i));
			}
		}
		return retval.toArray(new String[retval.size()]);
	}

    /**
     * Extends a SQL statement with enough ? characters in the last IN argument.
     * @param sql SQL statment ending with "IN (";
     * @param length number of entries.
     * @return the ready to use SQL statement.
     */
    public static String getIN(final String sql, final int length) {
        final StringBuilder retval = new StringBuilder(sql);
        for (int i = 0; i < length; i++) {
            retval.append("?,");
        }
        retval.setCharAt(retval.length() - 1, ')');
        return retval.toString();
    }
}
