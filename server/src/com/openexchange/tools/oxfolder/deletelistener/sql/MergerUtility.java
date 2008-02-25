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

package com.openexchange.tools.oxfolder.deletelistener.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link MergerUtility}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MergerUtility {

	/**
	 * Initializes a new {@link MergerUtility}
	 */
	private MergerUtility() {
		super();
	}

	private static final String SQL_DELETE_PERM = "DELETE FROM oxfolder_permissions "
			+ "WHERE cid = ? AND permission_id = ? AND fuid = ?";

	/**
	 * Deletes specified permission
	 * 
	 * @param entity
	 *            The entity ID
	 * @param fuid
	 *            The folder ID
	 * @param cid
	 *            The context ID
	 * @param con
	 *            A connection to MySQL database
	 * @throws SQLException
	 *             If deletion fails due to a SQL error
	 */
	public static void deletePermission(final int entity, final int fuid, final int cid, final Connection con)
			throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(SQL_DELETE_PERM);
			stmt.setInt(1, cid);
			stmt.setInt(2, entity);
			stmt.setInt(3, fuid);
			stmt.executeUpdate();
		} finally {
			if (null != stmt) {
				try {
					stmt.close();
				} catch (final SQLException e) {
					System.err.println("Warning: Statement could not be properly closed");
					e.printStackTrace(System.err);
				}
			}
		}
	}

	private static final String SQL_SELECT_ADMIN = "SELECT user FROM user_setting_admin WHERE cid = ?";

	/**
	 * Determines the ID of the user who is defined as admin for given context
	 * or <code>-1</code> if none found
	 * 
	 * @param cid
	 *            The context ID
	 * @param con
	 *            A readable connection
	 * @return The ID of context admin or <code>-1</code> if none found
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static int getContextAdminID(final int cid, final Connection con) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.prepareStatement(SQL_SELECT_ADMIN);
			stmt.setInt(1, cid);
			rs = stmt.executeQuery();
			if (!rs.next()) {
				return -1;
			}
			return rs.getInt(1);
		} finally {
			if (null != rs) {
				try {
					rs.close();
					rs = null;
				} catch (final SQLException e) {
					System.err.println("Warning: Result set could not be properly closed");
					e.printStackTrace(System.err);
				}
			}
			if (null != stmt) {
				try {
					stmt.close();
				} catch (final SQLException e) {
					System.err.println("Warning: Statement could not be properly closed");
					e.printStackTrace(System.err);
				}
			}
		}
	}
}
