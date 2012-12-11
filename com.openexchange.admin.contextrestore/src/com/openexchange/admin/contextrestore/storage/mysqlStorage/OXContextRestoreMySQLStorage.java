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
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.admin.contextrestore.dataobjects.UpdateTaskEntry;
import com.openexchange.admin.contextrestore.dataobjects.UpdateTaskInformation;
import com.openexchange.admin.contextrestore.dataobjects.VersionInformation;
import com.openexchange.admin.contextrestore.rmi.exceptions.OXContextRestoreException;
import com.openexchange.admin.contextrestore.rmi.exceptions.OXContextRestoreException.Code;
import com.openexchange.admin.contextrestore.rmi.impl.OXContextRestore.Parser.PoolIdSchemaAndVersionInfo;
import com.openexchange.admin.contextrestore.storage.sqlStorage.OXContextRestoreSQLStorage;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;

/**
 * This class contains all the mysql database related code
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 * 
 */
public final class OXContextRestoreMySQLStorage extends OXContextRestoreSQLStorage {

    private final static Log LOG = LogFactory.getLog(OXContextRestoreMySQLStorage.class);
    
    @Override
    public String restorectx(final Context ctx, final PoolIdSchemaAndVersionInfo poolidandschema) throws SQLException, FileNotFoundException, IOException, OXContextRestoreException, StorageException {
        Connection connection = null;
        Connection connection2 = null;
        PreparedStatement prepareStatement = null;
        PreparedStatement prepareStatement2 = null;
        PreparedStatement prepareStatement3 = null;
        final int poolId = poolidandschema.getPoolId();
        boolean doRollback = false;
        try {
            File file = new File("/tmp/" + poolidandschema.getSchema() + ".txt");
            if (!file.exists()) {
                file = new File(poolidandschema.getFileName());
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                connection = Database.get(poolId, poolidandschema.getSchema());
                connection.setAutoCommit(false);
                doRollback = true;
                String line;
                while ((line = reader.readLine()) != null) {
                    prepareStatement = connection.prepareStatement(line);
                    prepareStatement.execute();
                    prepareStatement.close();
                }
            } finally {
                close(reader);
            }
            file = new File("/tmp/configdb.txt");
            reader = new BufferedReader(new FileReader(file));
            try {
                connection2 = Database.get(true);
                connection2.setAutoCommit(false);
                doRollback = true;
                String line;
                while ((line = reader.readLine()) != null) {
                    prepareStatement2 = connection2.prepareStatement(line);
                    prepareStatement2.execute();
                    prepareStatement2.close();
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
                Database.back(poolId, connection2);
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

    @Override
    public void checkVersion(final PoolIdSchemaAndVersionInfo infoObject) throws SQLException, OXContextRestoreException, StorageException {
        final VersionInformation versionInfo = infoObject.getVersionInformation();
        if (null != versionInfo) {
            Connection connection = null;
            PreparedStatement prepareStatement = null;
            ResultSet result = null;
            final int poolId = infoObject.getPoolId();
            try {
                connection = Database.get(poolId, infoObject.getSchema());
                prepareStatement =
                    connection.prepareStatement("SELECT `version`, `locked`, `gw_compatible`, `admin_compatible`, `server` FROM `version`");

                result = prepareStatement.executeQuery();
                if (!result.next()) {
                    // Error there must be at least one row
                    throw new OXContextRestoreException(Code.NO_ENTRIES_IN_VERSION_TABLE);
                }
                final VersionInformation versionInformation2 =
                    new VersionInformation(result.getInt(4), result.getInt(3), result.getInt(2), result.getString(5), result.getInt(1));
                if (!versionInformation2.equals(versionInfo)) {
                    throw new OXContextRestoreException(Code.VERSION_TABLES_INCOMPATIBLE);
                }

            } catch (final OXException e) {
                throw new StorageException(new PoolException(e.getMessage()));
            } finally {
                closeSQLStuff(result, prepareStatement);
                if (null != connection) {
                    Database.back(poolId, connection);
                }
            }
        }
        final UpdateTaskInformation updateTaskInfo = infoObject.getUpdateTaskInformation();
        if (null != updateTaskInfo) {
            Connection connection = null;
            PreparedStatement prepareStatement = null;
            ResultSet result = null;
            final int poolId = infoObject.getPoolId();
            final Set<UpdateTaskEntry> current;
            try {
                connection = Database.get(poolId, infoObject.getSchema());
                prepareStatement = connection.prepareStatement("SELECT cid, taskName, successful, lastModified FROM `updateTask`");

                result = prepareStatement.executeQuery();
                if (!result.next()) {
                    throw new OXContextRestoreException(Code.NO_ENTRIES_IN_UPDATE_TASK_TABLE);
                }
                current = new HashSet<UpdateTaskEntry>(128);
                do {
                    final int contextId = result.getInt(1);
                    if (contextId <= 0 || contextId == infoObject.getContextId()) {
                        current.add(new UpdateTaskEntry(contextId, result.getString(2), result.getInt(3) > 0, result.getLong(4)));
                    }
                } while (result.next());
            } catch (final OXException e) {
                throw new StorageException(new PoolException(e.getMessage()));
            } finally {
                closeSQLStuff(result, prepareStatement);
                if (null != connection) {
                    Database.back(poolId, connection);
                }
            }

            Set<UpdateTaskEntry> set = updateTaskInfo.asSet();
            if (!set.removeAll(current) || !set.isEmpty()) {
                throw new OXContextRestoreException(Code.UPDATE_TASK_TABLES_INCOMPATIBLE);
            }
            set = updateTaskInfo.asSet();
            if (!current.removeAll(set) || !current.isEmpty()) {
                throw new OXContextRestoreException(Code.UPDATE_TASK_TABLES_INCOMPATIBLE);
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
                LOG.error(e.getMessage(), e);
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
                    LOG.error(e.getMessage(), e);
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
            LOG.error(e.getMessage(), e);
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
