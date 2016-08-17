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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

/**
 *
 */
package com.openexchange.admin.contextrestore.storage.mysqlStorage;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import com.openexchange.admin.contextrestore.rmi.exceptions.OXContextRestoreException;
import com.openexchange.admin.contextrestore.rmi.exceptions.OXContextRestoreException.Code;
import com.openexchange.admin.contextrestore.rmi.impl.OXContextRestore.Parser.PoolIdSchemaAndVersionInfo;
import com.openexchange.admin.contextrestore.storage.sqlStorage.OXContextRestoreSQLStorage;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;

/**
 * This class contains all the mysql database related code
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public final class OXContextRestoreMySQLStorage extends OXContextRestoreSQLStorage {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXContextRestoreMySQLStorage.class);

    @Override
    public String restorectx(final Context ctx, final PoolIdSchemaAndVersionInfo poolidandschema, String configdbname) throws SQLException, IOException, OXContextRestoreException, StorageException {
        Connection connection = null;
        Connection connection2 = null;
        PreparedStatement prepareStatement = null;
        PreparedStatement prepareStatement2 = null;
        PreparedStatement prepareStatement3 = null;
        final int poolId = poolidandschema.getPoolId();
        boolean doRollback = false;
        final Map<String, File> tempfilemap = poolidandschema.getTempfilemap();
        try {
            File file = tempfilemap.get(poolidandschema.getSchema());
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(file));
            } catch (final FileNotFoundException e1) {
                throw new OXContextRestoreException(Code.CONFIGDB_FILE_NOT_FOUND);
            }
            try {
                connection = Database.get(poolId, poolidandschema.getSchema());
                connection.setAutoCommit(false);
                doRollback = true;
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        prepareStatement = connection.prepareStatement(line);
                        prepareStatement.execute();
                        prepareStatement.close();
                    } catch (SQLException e) {
                        LOG.error("Executing the following SQL statement failed: {}", line, e);
                        throw e;
                    }
                }
            } finally {
                close(reader);
            }
            file = tempfilemap.get(configdbname);
            try {
                reader = new BufferedReader(new FileReader(file));
            } catch (final FileNotFoundException e1) {
                throw new OXContextRestoreException(Code.USERDB_FILE_NOT_FOUND);
            }
            try {
                connection2 = Database.get(true);
                connection2.setAutoCommit(false);
                doRollback = true;
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        prepareStatement2 = connection2.prepareStatement(line);
                        prepareStatement2.execute();
                        prepareStatement2.close();
                    } catch (SQLException e) {
                        LOG.error("Executing the following SQL statement failed: {}", line, e);
                        throw e;
                    }
                }
            } finally {
                close(reader);
            }
            connection.commit();
            connection2.commit();
            doRollback = false;

            connection2.setAutoCommit(true);
            prepareStatement3 = connection2.prepareStatement("SELECT `filestore_name`, `uri` FROM `context` INNER JOIN `filestore` ON context.filestore_id = filestore.id WHERE cid=?");
            prepareStatement3.setInt(1, ctx.getId().intValue());
            final ResultSet executeQuery = prepareStatement3.executeQuery();
            if (!executeQuery.next()) {
                throw new OXContextRestoreException(Code.NO_FILESTORE_VALUE);
            }
            final String filestore_name = executeQuery.getString(1);
            final String uri = executeQuery.getString(2);
            return uri + File.separatorChar + filestore_name;
        } catch (final OXException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            if (doRollback) {
                dorollback(connection, connection2);
            }
            closeSQLStuff(prepareStatement, prepareStatement2, prepareStatement3);
            if (null != connection) {
                autocommit(connection);
                Database.back(poolId, connection);
            }
            if (null != connection2) {
                autocommit(connection2);
                Database.back(true, connection2);
            }
            for (final File file : tempfilemap.values()) {
                file.delete();
            }
        }
    }

    private static void dorollback(final Connection... connections) {
        for (final Connection con : connections) {
            if (null != con) {
                try {
                    con.rollback();
                } catch (final Exception e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Closes the ResultSet.
     *
     * @param result <code>null</code> or a ResultSet to close.
     */
    private static void closeSQLStuff(final ResultSet result) {
        if (result != null) {
            try {
                result.close();
            } catch (final SQLException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Closes the {@link Statement}.
     *
     * @param stmt <code>null</code> or a {@link Statement} to close.
     */
    private static void closeSQLStuff(final Statement... stmts) {
        if (null == stmts || stmts.length <= 0) {
            return;
        }
        for (final Statement stmt : stmts) {
            if (null != stmt) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    LOG.error("", e);
                }
            }
        }
    }

    /**
     * Closes the ResultSet and the Statement.
     *
     * @param result <code>null</code> or a ResultSet to close.
     * @param stmt <code>null</code> or a Statement to close.
     */
    private static void closeSQLStuff(final ResultSet result, final Statement stmt) {
        closeSQLStuff(result);
        closeSQLStuff(stmt);
    }

    /**
     * Convenience method to set the auto-commit of a connection to <code>true</code>.
     *
     * @param con connection that should go into auto-commit mode.
     */
    private static void autocommit(final Connection con) {
        if (null == con) {
            return;
        }
        try {
            if (!con.isClosed() && !con.getAutoCommit()) {
                con.setAutoCommit(true);
            }
        } catch (final SQLException e) {
            LOG.error("", e);
        }
    }

    /**
     * Safely closes specified {@link Closeable} instance.
     *
     * @param toClose The {@link Closeable} instance
     */
    private static void close(final Closeable toClose) {
        if (null != toClose) {
            try {
                toClose.close();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

}
