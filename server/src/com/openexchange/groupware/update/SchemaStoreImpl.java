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

package com.openexchange.groupware.update;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.database.Database;
import com.openexchange.database.Server;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.SchemaException;
import com.openexchange.groupware.update.exception.SchemaExceptionFactory;
import com.openexchange.server.impl.DBPoolingException;

/**
 * Implements loading and storing the schema version information.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@OXExceptionSource(classId = Classes.SCHEMA_STORE_IMPL, component = Component.UPDATE)
public class SchemaStoreImpl extends SchemaStore {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SchemaStoreImpl.class);

	private final AtomicBoolean tableCreated;

	private final Lock createTableLock;

	/**
	 * SQL command for selecting the version from the schema.
	 */
	private static final String SELECT = "SELECT version,locked,gw_compatible,"
			+ "admin_compatible,server FROM version FOR UPDATE";

	/**
	 * For creating exceptions.
	 */
	private static final SchemaExceptionFactory EXCEPTION = new SchemaExceptionFactory(SchemaStoreImpl.class);

	/**
	 * Default constructor.
	 */
	public SchemaStoreImpl() {
		super();
		tableCreated = new AtomicBoolean();
		createTableLock = new ReentrantLock();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Schema getSchema(final int contextId) throws SchemaException {
		if (!tableCreated.get()) {
			createTableLock.lock();
			try {
				if (!tableCreated.get()) {
					if (!existsTable(contextId)) {
						createVersionTable(contextId);
						insertInitialEntry(contextId);
					} else if (!hasEntry(contextId)) {
						insertInitialEntry(contextId);
					}
					tableCreated.set(true);
				}
			} finally {
				createTableLock.unlock();
			}
		}
		return loadSchema(contextId);
	}

	@OXThrowsMultiple(category = { Category.CODE_ERROR, Category.CODE_ERROR }, desc = { "", "" }, exceptionId = { 14,
			15 }, msg = { "An SQL error occurred while creating table 'version': %1$s.",
			"A database error occurred while creating table 'version': %1$s." })
	private static final void createVersionTable(final int contextId) throws SchemaException {
		/*
		 * Create table 'version'
		 */
		try {
			Connection writeCon = null;
			PreparedStatement stmt = null;
			try {
				writeCon = Database.get(contextId, true);
				stmt = writeCon.prepareStatement(CREATE);
				stmt.executeUpdate();
			} finally {
				closeSQLStuff(null, stmt);
				if (writeCon != null) {
					Database.back(contextId, true, writeCon);
				}
			}
		} catch (final SQLException e) {
			throw EXCEPTION.create(14, e, e.getMessage());
		} catch (final DBPoolingException e) {
			throw EXCEPTION.create(15, e, e.getMessage());
		}
	}

	@OXThrowsMultiple(category = { Category.CODE_ERROR, Category.CODE_ERROR }, desc = { "", "" }, exceptionId = { 30,
			31 }, msg = { "An SQL error occurred while creating table 'version': %1$s.",
			"A database error occurred while creating table 'version': %1$s." })
	private static final void insertInitialEntry(final int contextId) throws SchemaException {
		/*
		 * Insert initial entry
		 */
		try {
			Connection writeCon = null;
			PreparedStatement stmt = null;
			try {
				writeCon = Database.get(contextId, true);
				stmt = writeCon.prepareStatement(INSERT);
				stmt.setInt(1, SchemaImpl.FIRST.getDBVersion());
				stmt.setBoolean(2, SchemaImpl.FIRST.isLocked());
				stmt.setBoolean(3, SchemaImpl.FIRST.isGroupwareCompatible());
				stmt.setBoolean(4, SchemaImpl.FIRST.isAdminCompatible());
				stmt.setString(5, Server.getServerName());
				stmt.executeUpdate();
			} finally {
				closeSQLStuff(null, stmt);
				if (writeCon != null) {
					Database.back(contextId, true, writeCon);
				}
			}
		} catch (final SQLException e) {
			throw EXCEPTION.create(14, e, e.getMessage());
		} catch (final DBPoolingException e) {
			throw EXCEPTION.create(15, e, e.getMessage());
		}
	}

	private static final String CREATE = "CREATE TABLE version (" + "version INT4 UNSIGNED NOT NULL,"
			+ "locked BOOLEAN NOT NULL," + "gw_compatible BOOLEAN NOT NULL," + "admin_compatible BOOLEAN NOT NULL,"
			+ "server VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL)" + " ENGINE = InnoDB";

	private static final String INSERT = "INSERT INTO version VALUES (?, ?, ?, ?, ?)";

	private static final String SQL_SELECT_LOCKED_FOR_UPDATE = "SELECT locked FROM version FOR UPDATE";

	private static final String SQL_UPDATE_LOCKED = "UPDATE version SET locked = ?";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.update.SchemaStore#lockSchema(com.openexchange.groupware.update.Schema)
	 */
	@OXThrowsMultiple(category = { Category.CODE_ERROR, Category.INTERNAL_ERROR, Category.PERMISSION,
			Category.INTERNAL_ERROR }, desc = { "", "", "", "" }, exceptionId = { 6, 7, 8, 9 }, msg = {
			"An SQL error occurred while reading schema version information: %1$s.",
			"Though expected, SQL query returned no result.",
			"Update conflict detected. Another process is currently updating schema %1$s.",
			"Table update failed. Schema %1$s could not be locked." })
	@Override
	public void lockSchema(final Schema schema, final int contextId) throws SchemaException {
		/*
		 * Start of update process, so lock schema
		 */
		try {
			boolean error = false;
			Connection writeCon = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				/*
				 * Try to obtain exclusive lock on table 'version'
				 */
				writeCon = Database.get(contextId, true);
				writeCon.setAutoCommit(false); // BEGIN
				stmt = writeCon.prepareStatement(SQL_SELECT_LOCKED_FOR_UPDATE);
				rs = stmt.executeQuery();
				if (!rs.next()) {
					error = true;
					throw EXCEPTION.create(7);
				} else if (rs.getBoolean(1)) {
					/*
					 * Schema is already locked by another update process
					 */
					error = true;
					throw EXCEPTION.create(8, schema.getSchema());
				}
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
				/*
				 * Lock schema
				 */
				stmt = writeCon.prepareStatement(SQL_UPDATE_LOCKED);
				stmt.setBoolean(1, true);
				if (stmt.executeUpdate() == 0) {
					/*
					 * Schema could not be locked
					 */
					error = true;
					throw EXCEPTION.create(9, schema.getSchema());
				}
				/*
				 * Everything went fine. Schema is marked as locked
				 */
				writeCon.commit(); // COMMIT
			} finally {
				closeSQLStuff(rs, stmt);
				if (writeCon != null) {
					if (error) {
						try {
							writeCon.rollback();
						} catch (final SQLException e) {
							LOG.error(e.getMessage(), e);
						}
					}
					if (!writeCon.getAutoCommit()) {
						try {
							writeCon.setAutoCommit(true);
						} catch (final SQLException e) {
							LOG.error(e.getMessage(), e);
						}
					}
					Database.back(contextId, true, writeCon);
				}
			}
		} catch (final DBPoolingException e) {
			LOG.error(e.getMessage(), e);
			throw new SchemaException(e);
		} catch (final SQLException e) {
			throw EXCEPTION.create(6, e, e.getMessage());
		}

	}

	private static final String SQL_UPDATE_VERSION = "UPDATE version SET version = ?, locked = ?, gw_compatible = ?, admin_compatible = ?";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.update.SchemaStore#unlockSchema(com.openexchange.groupware.update.Schema)
	 */
	@OXThrowsMultiple(category = { Category.CODE_ERROR, Category.INTERNAL_ERROR, Category.PERMISSION,
			Category.INTERNAL_ERROR }, desc = { "", "", "", "" }, exceptionId = { 10, 11, 12, 13 }, msg = {
			"An SQL error occurred while reading schema version information: %1$s.",
			"Though expected, SQL query returned no result.",
			"Update conflict detected. Schema %1$s is not marked as LOCKED.",
			"Table update failed. Schema %1$s could not be unlocked." })
	@Override
	public void unlockSchema(final Schema schema, final int contextId) throws SchemaException {
		/*
		 * End of update process, so unlock schema
		 */
		try {
			boolean error = false;
			Connection writeCon = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				/*
				 * Try to obtain exclusive lock on table 'version'
				 */
				writeCon = Database.get(contextId, true);
				writeCon.setAutoCommit(false); // BEGIN
				stmt = writeCon.prepareStatement(SQL_SELECT_LOCKED_FOR_UPDATE);
				rs = stmt.executeQuery();
				if (!rs.next()) {
					error = true;
					throw EXCEPTION.create(11);
				} else if (!rs.getBoolean(1)) {
					/*
					 * Schema is NOT locked by update process
					 */
					error = true;
					throw EXCEPTION.create(12, schema.getSchema());
				}
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
				/*
				 * Update & unlock schema
				 */
				stmt = writeCon.prepareStatement(SQL_UPDATE_VERSION);
				stmt.setInt(1, SchemaImpl.ACTUAL.getDBVersion());
				stmt.setBoolean(2, false);
				stmt.setBoolean(3, SchemaImpl.ACTUAL.isGroupwareCompatible());
				stmt.setBoolean(4, SchemaImpl.ACTUAL.isAdminCompatible());
				if (stmt.executeUpdate() == 0) {
					/*
					 * Schema could not be unlocked
					 */
					error = true;
					throw EXCEPTION.create(13, schema.getSchema());
				}
				/*
				 * Everything went fine. Schema is marked as unlocked
				 */
				writeCon.commit(); // COMMIT
			} finally {
				closeSQLStuff(rs, stmt);
				if (writeCon != null) {
					if (error) {
						try {
							writeCon.rollback();
						} catch (final SQLException e) {
							LOG.error(e.getMessage(), e);
						}
					}
					if (!writeCon.getAutoCommit()) {
						try {
							writeCon.setAutoCommit(true);
						} catch (final SQLException e) {
							LOG.error(e.getMessage(), e);
						}
					}
					Database.back(contextId, true, writeCon);
				}
			}
		} catch (final DBPoolingException e) {
			LOG.error(e.getMessage(), e);
			throw new SchemaException(e);
		} catch (final SQLException e) {
			throw EXCEPTION.create(10, e, e.getMessage());
		}
	}

	/**
	 * Loads the schema version information from the database.
	 * 
	 * @param contextId
	 *            context identifier.
	 * @return the schema version information.
	 * @throws SchemaException
	 *             if loading fails.
	 */
	@OXThrowsMultiple(category = { Category.CODE_ERROR, Category.SETUP_ERROR, Category.SETUP_ERROR, Category.CODE_ERROR }, desc = {
			"", "", "", "" }, exceptionId = { 1, 2, 4, 16 }, msg = {
			"An SQL error occurred while reading schema version information: " + "%1$s.",
			"No row found in table update.", "Multiple rows found.",
			"A database error occurred while reading schema version information: %1$s." })
	private Schema loadSchema(final int contextId) throws SchemaException {
		Connection con;
		try {
			con = Database.get(contextId, true);
		} catch (final DBPoolingException e) {
			throw new SchemaException(e);
		}
		SchemaImpl schema = null;
		Statement stmt = null;
		ResultSet result = null;
		try {
			stmt = con.createStatement();
			result = stmt.executeQuery(SELECT);
			if (result.next()) {
				schema = new SchemaImpl();
				int pos = 1;
				schema.setDBVersion(result.getInt(pos++));
				schema.setLocked(result.getBoolean(pos++));
				schema.setGroupwareCompatible(result.getBoolean(pos++));
				schema.setAdminCompatible(result.getBoolean(pos++));
				schema.setServer(result.getString(pos++));
				schema.setSchema(Database.getSchema(contextId));
			} else {
				throw EXCEPTION.create(2);
			}
			if (result.next()) {
				throw EXCEPTION.create(4);
			}
		} catch (final SQLException e) {
			throw EXCEPTION.create(1, e, e.getMessage());
		} catch (final DBPoolingException e) {
			throw EXCEPTION.create(16, e, e.getMessage());
		} finally {
			closeSQLStuff(result, stmt);
			Database.back(contextId, true, con);
		}
		return schema;
	}

	/**
	 * Checks if the schema version table exists.
	 * 
	 * @param contextId
	 *            context identifier.
	 * @return <code>true</code> if the table exists.
	 * @throws SchemaException
	 *             if the check fails
	 */
	@OXThrowsMultiple(category = { Category.CODE_ERROR, Category.SETUP_ERROR }, desc = {
			"Checking if a table exist failed.", "Strange context " + "identifier or a mapping is missing." }, exceptionId = {
			3, 5 }, msg = { "A SQL exception occurred while checking for schema version " + "table: %1$s.",
			"Resolving schema for context %1$d failed." })
	private static final boolean existsTable(final int contextId) throws SchemaException {
		Connection con;
		try {
			con = Database.get(contextId, true);
		} catch (final DBPoolingException e) {
			throw new SchemaException(e);
		}
		boolean retval = false;
		ResultSet result = null;
		try {
			final DatabaseMetaData meta = con.getMetaData();
			result = meta.getTables(Database.getSchema(contextId), null, "version", new String[] { "TABLE" });
			if (result.next()) {
				retval = true;
			}
		} catch (final SQLException e) {
			throw EXCEPTION.create(3, e, e.getMessage());
		} catch (final DBPoolingException e) {
			throw EXCEPTION.create(5, e, Integer.valueOf(contextId));
		} finally {
			closeSQLStuff(result);
			Database.back(contextId, true, con);
		}
		return retval;
	}

	/**
	 * Checks if table 'version' holds an entry
	 * 
	 * @param contextId
	 *            context identifier
	 * @return <code>true</code> if table 'version' holds an entry; otherwise
	 *         <code>false</code>
	 * @throws SchemaException
	 *             if the check fails
	 */
	private static final boolean hasEntry(final int contextId) throws SchemaException {
		Connection con;
		try {
			con = Database.get(contextId, true);
		} catch (final DBPoolingException e) {
			throw new SchemaException(e);
		}
		Statement stmt = null;
		ResultSet result = null;
		try {
			stmt = con.createStatement();
			result = stmt.executeQuery(SELECT);
			return result.next();
		} catch (final SQLException e) {
			throw EXCEPTION.create(1, e, e.getMessage());
		} finally {
			closeSQLStuff(result, stmt);
			Database.back(contextId, true, con);
		}
	}
}
