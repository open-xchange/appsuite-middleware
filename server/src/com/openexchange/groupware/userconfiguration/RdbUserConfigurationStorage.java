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

package com.openexchange.groupware.userconfiguration;

import static com.openexchange.tools.sql.DBUtils.closeResources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationException.UserConfigurationCode;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.Initialization;

/**
 * RdbUserConfigurationStorage
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class RdbUserConfigurationStorage extends UserConfigurationStorage implements Initialization {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(RdbUserConfigurationStorage.class);

	/**
	 * Constructor
	 * 
	 * @param context -
	 *            the context
	 */
	public RdbUserConfigurationStorage() {
		super();
	}

	@Override
	protected void startInternal() throws AbstractOXException {
		/*
		 * Nothing to start
		 */
	}

	@Override
	protected void stopInternal() throws AbstractOXException {
		/*
		 * Nothing to stop
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.UserConfigurationStorage#getUserConfiguration(int,
	 *      int[])
	 */
	@Override
	public UserConfiguration getUserConfiguration(final int userId, final int[] groups, final Context ctx)
			throws UserConfigurationException {
		try {
			return loadUserConfiguration(userId, groups, ctx);
		} catch (final LdapException e) {
			throw new UserConfigurationException(e);
		} catch (final DBPoolingException e) {
			throw new UserConfigurationException(e);
		} catch (final OXException e) {
			throw new UserConfigurationException(e);
		} catch (final SQLException e) {
			throw new UserConfigurationException(UserConfigurationCode.SQL_ERROR, e, e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.UserConfigurationStorage#clearStorage()
	 */
	@Override
	public void clearStorage() throws UserConfigurationException {
		/*
		 * Since this storage implementation directly fetches data from database
		 * this method has no effect
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.UserConfigurationStorage#removeUserConfiguration(int,
	 *      com.openexchange.groupware.contexts.Context)
	 */
	@Override
	public void removeUserConfiguration(final int userId, final Context ctx) throws UserConfigurationException {
		/*
		 * Since this storage implementation directly fetches data from database
		 * this method has no effect
		 */
	}

	/*
	 * ------------- Methods for saving -------------
	 */

	/**
	 * Saves given user configuration to database. If <code>insert</code> is
	 * <code>true</code> an INSERT command is performed, otherwise an UPDATE
	 * command.
	 * 
	 * @param userConfig -
	 *            the user configuration to save
	 * @param insert -
	 *            <code>true</code> for an INSERT; otherwise UPDATE
	 * @param writeCon -
	 *            the writeable connection; may be <code>null</code>
	 * @throws SQLException -
	 *             if saving fails due to a SQL error
	 * @throws DBPoolingException -
	 *             if a writeable connection could not be obtained from database
	 */
	public static void saveUserConfiguration(final UserConfiguration userConfig, final boolean insert,
			final Connection writeCon) throws SQLException, DBPoolingException {
		saveUserConfiguration(userConfig, insert, userConfig.getContext(), writeCon);
	}

	private static final String SQL_SELECT = "SELECT user FROM user_configuration WHERE cid = ? AND user = ?";

	/**
	 * Saves given user configuration to database by self-determining if an
	 * INSERT or UPDATE is going to be performed.
	 * 
	 * @param userConfig -
	 *            the user configuration to save
	 * @throws OXException -
	 *             if saving fails
	 */
	public static void saveUserConfiguration(final UserConfiguration userConfig) throws OXException {
		boolean insert = false;
		try {
			Connection readCon = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				readCon = DBPool.pickup(userConfig.getContext());
				stmt = readCon.prepareStatement(SQL_SELECT);
				stmt.setInt(1, userConfig.getContext().getContextId());
				stmt.setInt(2, userConfig.getUserId());
				rs = stmt.executeQuery();
				insert = !rs.next();
			} finally {
				closeResources(rs, stmt, readCon, true, userConfig.getContext());
			}
			saveUserConfiguration(userConfig, insert, userConfig.getContext(), null);
		} catch (final SQLException e) {
			throw new UserConfigurationException(UserConfigurationCode.SQL_ERROR, e, new Object[0]);
		} catch (final DBPoolingException e) {
			throw new UserConfigurationException(UserConfigurationCode.DBPOOL_ERROR, e, new Object[0]);
		}
	}

	private static final String INSERT_USER_CONFIGURATION = "INSERT INTO user_configuration (cid, user, permissions) VALUES (?, ?, ?)";

	private static final String UPDATE_USER_CONFIGURATION = "UPDATE user_configuration SET permissions = ? WHERE cid = ? AND user = ?";

	/**
	 * Saves given user configuration to database. If <code>insert</code> is
	 * <code>true</code> an INSERT command is performed, otherwise an UPDATE
	 * command.
	 * 
	 * @param userConfig -
	 *            the user configuration to save
	 * @param insert -
	 *            <code>true</code> for an INSERT; otherwise UPDATE
	 * @param ctx -
	 *            the context
	 * @param writeConArg -
	 *            the writebale connection; may be <code>null</code>
	 * @throws SQLException -
	 *             if saving fails due to a SQL error
	 * @throws DBPoolingException -
	 *             if a writeable connection could not be obtained from database
	 */
	public static void saveUserConfiguration(final UserConfiguration userConfig, final boolean insert,
			final Context ctx, final Connection writeConArg) throws SQLException, DBPoolingException {
		Connection writeCon = writeConArg;
		boolean closeConnection = false;
		PreparedStatement stmt = null;
		try {
			if (writeCon == null) {
				writeCon = DBPool.pickupWriteable(ctx);
				closeConnection = true;
			}
			if (insert) {
				stmt = writeCon.prepareStatement(INSERT_USER_CONFIGURATION);
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, userConfig.getUserId());
				stmt.setInt(3, userConfig.getPermissionBits());
			} else {
				stmt = writeCon.prepareStatement(UPDATE_USER_CONFIGURATION);
				stmt.setInt(1, userConfig.getPermissionBits());
				stmt.setInt(2, ctx.getContextId());
				stmt.setInt(3, userConfig.getUserId());
			}
			stmt.executeUpdate();
			if (!insert) {
				try {
					UserConfigurationStorage.getInstance().removeUserConfiguration(userConfig.getUserId(),
							userConfig.getContext());
				} catch (UserConfigurationException e) {
					LOG.warn("User Configuration could not be removed from cache", e);
				}
			}
		} finally {
			closeResources(null, stmt, closeConnection ? writeCon : null, false, ctx);
		}
	}

	/*
	 * ------------- Methods for loading -------------
	 */

	/**
	 * Loads the user configuration from database specified through user ID and
	 * context
	 * 
	 * @param userId -
	 *            the user ID
	 * @param ctx -
	 *            the context
	 * @return the instance of <code>{@link UserConfiguration}</code>
	 * @throws SQLException -
	 *             if user configuration could not be loaded from database
	 * @throws LdapException -
	 *             if user's groups are <code>null</code> and could not be
	 *             determined by <code>{@link UserStorage}</code>
	 *             implementation
	 * @throws DBPoolingException -
	 *             if a readable connection could not be obtained from
	 *             connection pool
	 * @throws OXException -
	 *             if no matching user configuration is kept in database
	 */
	public static UserConfiguration loadUserConfiguration(final int userId, final Context ctx) throws SQLException,
			LdapException, DBPoolingException, OXException {
		return loadUserConfiguration(userId, null, ctx, null);
	}

	/**
	 * Loads the user configuration from database specified through user ID and
	 * context
	 * 
	 * @param userId -
	 *            the user ID
	 * @param groups -
	 *            the group IDs the user belongs to; may be <code>null</code>
	 * @param ctx -
	 *            the context
	 * @return the instance of <code>{@link UserConfiguration}</code>
	 * @throws SQLException -
	 *             if user configuration could not be loaded from database
	 * @throws LdapException -
	 *             if user's groups are <code>null</code> and could not be
	 *             determined by <code>{@link UserStorage}</code>
	 *             implementation
	 * @throws DBPoolingException -
	 *             if a readable connection could not be obtained from
	 *             connection pool
	 * @throws OXException -
	 *             if no matching user configuration is kept in database
	 */
	public static UserConfiguration loadUserConfiguration(final int userId, final int[] groups, final Context ctx)
			throws SQLException, LdapException, DBPoolingException, OXException {
		return loadUserConfiguration(userId, groups, ctx, null);
	}

	/**
	 * Special method invoked by admin to load user configuration since no
	 * exception is thrown if no matching config could be found. In this case an
	 * instance of {@link UserConfiguration} is returned that does not hold any
	 * permissions.
	 * 
	 * @param userId -
	 *            the user ID
	 * @param groups -
	 *            the group IDs the user belongs to; may be <code>null</code>
	 * @param cid -
	 *            the context ID
	 * @param readConArg-
	 *            the readable context; may be <code>null</code>
	 * @return the instance of <code>{@link UserConfiguration}</code>
	 * @throws SQLException -
	 *             if user configuration could not be loaded from database
	 * @throws DBPoolingException -
	 *             if a readable connection could not be obtained from
	 *             connection pool
	 */
	public static UserConfiguration adminLoadUserConfiguration(final int userId, final int[] groups, final int cid,
			final Connection readConArg) throws SQLException, DBPoolingException {
		final Context ctx = new ContextImpl(cid);
		Connection readCon = readConArg;
		boolean closeReadCon = false;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if (readCon == null) {
				readCon = DBPool.pickup(ctx);
				closeReadCon = true;
			}
			stmt = readCon.prepareStatement(LOAD_USER_CONFIGURATION);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, userId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return new UserConfiguration(rs.getInt(1), userId, groups, ctx);
			}
			return new UserConfiguration(0, userId, groups, ctx);
		} finally {
			closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
		}
	}

	private static final String LOAD_USER_CONFIGURATION = "SELECT permissions FROM user_configuration WHERE cid = ? AND user = ?";

	/**
	 * Loads the user configuration from database specified through user ID and
	 * context
	 * 
	 * @param userId -
	 *            the user ID
	 * @param groupsArg -
	 *            the group IDs the user belongs to; may be <code>null</code>
	 * @param ctx -
	 *            the context
	 * @param readConArg -
	 *            the readable context; may be <code>null</code>
	 * @return the instance of <code>{@link UserConfiguration}</code>
	 * @throws SQLException -
	 *             if user configuration could not be loaded from database
	 * @throws LdapException -
	 *             if user's groups are <code>null</code> and could not be
	 *             determined by <code>{@link UserStorage}</code>
	 *             implementation
	 * @throws DBPoolingException -
	 *             if a readable connection could not be obtained from
	 *             connection pool
	 * @throws OXException -
	 *             if no matching user configuration is kept in database
	 */
	public static UserConfiguration loadUserConfiguration(final int userId, final int[] groupsArg, final Context ctx,
			final Connection readConArg) throws SQLException, LdapException, DBPoolingException, OXException {
		int[] groups = groupsArg;
		Connection readCon = readConArg;
		boolean closeCon = false;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if (readCon == null) {
				readCon = DBPool.pickup(ctx);
				closeCon = true;
			}
			stmt = readCon.prepareStatement(LOAD_USER_CONFIGURATION);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, userId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				if (groups == null) {
					groups = UserStorage.getInstance().getUser(userId, ctx).getGroups();
				}
				return new UserConfiguration(rs.getInt(1), userId, groups, ctx);
			}
			throw new UserConfigurationException(UserConfigurationCode.NOT_FOUND, Integer.valueOf(userId), Integer
					.valueOf(ctx.getContextId()));
		} finally {
			closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
		}
	}

	/*
	 * ------------- Methods for deleting -------------
	 */

	/**
	 * Deletes the user configuration from database specified through ID and
	 * context. This is a convenience method that delegates invokation to
	 * <code>{@link #deleteUserConfiguration(int, Connection, Context)}</code>.
	 * whereby connection is set to <code>null</code>, thus a new writeable
	 * connection is going to be obtained from connection pool.
	 * 
	 * @param userId -
	 *            the user ID
	 * @param ctx -
	 *            the context
	 * @throws SQLException -
	 *             if user configuration cannot be removed from database
	 * @throws DBPoolingException -
	 *             if no writeable connection could be obtained
	 */
	public static void deleteUserConfiguration(final int userId, final Context ctx) throws SQLException,
			DBPoolingException {
		RdbUserConfigurationStorage.deleteUserConfiguration(userId, null, ctx);
	}

	private static final String DELETE_USER_CONFIGURATION = "DELETE FROM user_configuration WHERE cid = ? AND user = ?";

	/**
	 * Deletes the user configuration from database specified through ID and
	 * context.
	 * 
	 * @param userId -
	 *            the user ID
	 * @param writeConArg -
	 *            the writeable connection
	 * @param ctx -
	 *            the context
	 * @throws SQLException -
	 *             if user configuration cannot be removed from database
	 * @throws DBPoolingException -
	 *             if no writeable connection could be obtained
	 */
	public static void deleteUserConfiguration(final int userId, final Connection writeConArg, final Context ctx)
			throws SQLException, DBPoolingException {
		Connection writeCon = writeConArg;
		boolean closeWriteCon = false;
		PreparedStatement stmt = null;
		try {
			if (writeCon == null) {
				writeCon = DBPool.pickupWriteable(ctx);
				closeWriteCon = true;
			}
			stmt = writeCon.prepareStatement(DELETE_USER_CONFIGURATION);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, userId);
			stmt.executeUpdate();
			try {
				UserConfigurationStorage.getInstance().removeUserConfiguration(userId, ctx);
			} catch (UserConfigurationException e) {
				LOG.warn("User Configuration could not be removed from cache", e);
			}
		} finally {
			closeResources(null, stmt, closeWriteCon ? writeCon : null, false, ctx);
		}
	}

}
