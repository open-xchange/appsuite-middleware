/**
 * 
 */
package com.openexchange.admin.contextrestore.storage.mysqlStorage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import com.openexchange.admin.contextrestore.dataobjects.VersionInformation;
import com.openexchange.admin.contextrestore.rmi.exceptions.OXContextRestoreException;
import com.openexchange.admin.contextrestore.rmi.exceptions.OXContextRestoreException.Code;
import com.openexchange.admin.contextrestore.rmi.impl.OXContextRestore.Parser.PoolIdSchemaAndVersionInfo;
import com.openexchange.admin.contextrestore.storage.sqlStorage.OXContextRestoreSQLStorage;
import com.openexchange.admin.lib.rmi.dataobjects.Context;
import com.openexchange.admin.lib.rmi.exceptions.PoolException;
import com.openexchange.admin.lib.rmi.exceptions.StorageException;
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
    public String restorectx(Context ctx, PoolIdSchemaAndVersionInfo poolidandschema) throws SQLException, FileNotFoundException, IOException, OXContextRestoreException, StorageException {
        Connection connection = null;
        Connection connection2 = null;
        PreparedStatement prepareStatement = null;
        PreparedStatement prepareStatement2 = null;
        PreparedStatement prepareStatement3 = null;
        final int pool_id = poolidandschema.getPool_id();
        try {
            File file = new File("/tmp/" + poolidandschema.getSchema() + ".txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String in = null;
            connection = Database.get(pool_id, poolidandschema.getSchema());
            connection.setAutoCommit(false);
            while ((in = reader.readLine()) != null) {
                prepareStatement = connection.prepareStatement(in);
                prepareStatement.execute();
                prepareStatement.close();
            }
            file = new File("/tmp/configdb.txt");
            reader = new BufferedReader(new FileReader(file));
            in = null;
            connection2 = Database.get(true);
            connection2.setAutoCommit(false);
            while ((in = reader.readLine()) != null) {
                prepareStatement2 = connection2.prepareStatement(in);
                prepareStatement2.execute();
                prepareStatement2.close();
            }
            connection.commit();
            connection.setAutoCommit(true);
            connection2.commit();
            connection2.setAutoCommit(true);

            prepareStatement3 = connection2.prepareStatement("SELECT `filestore_name`, `uri` FROM `context` INNER JOIN `filestore` ON context.filestore_id = filestore.id WHERE cid=?");
            prepareStatement3.setInt(1, ctx.getId());
            final ResultSet executeQuery = prepareStatement3.executeQuery();
            if (executeQuery.next()) {
                final String filestore_name = executeQuery.getString(1);
                final String uri = executeQuery.getString(2);
                return uri + File.separatorChar + filestore_name;
            } else {
                throw new OXContextRestoreException(Code.NO_FILESTORE_VALUE);
            }
        } catch (final SQLException e) {
            dorollback(connection, connection2, e);
            throw e;
        } catch (final FileNotFoundException e) {
            dorollback(connection, connection2, e);
            throw e;
        } catch (final OXException e) {
            dorollback(connection, connection2, e);
            throw new StorageException(new PoolException(e.getMessage()));
        } catch (final IOException e) {
            dorollback(connection, connection2, e);
            throw e;
        } finally {
            closePreparedStatement(prepareStatement);
            closePreparedStatement(prepareStatement2);
            closePreparedStatement(prepareStatement3);
            if (null != connection) {
                Database.back(pool_id, connection);
            }
        }
    }

    private void dorollback(Connection conn, Connection conn2, Exception e2) throws OXContextRestoreException {
        if (null != conn) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                LOG.error(e2.getMessage(), e2);
                throw new OXContextRestoreException(Code.ROLLBACK_ERROR, e.getMessage());
            }
        }
        if (null != conn2) {
            try {
                conn2.rollback();
            } catch (SQLException e) {
                LOG.error(e2.getMessage(), e2);
                throw new OXContextRestoreException(Code.ROLLBACK_ERROR, e.getMessage());
            }
        }
    }

    private void closePreparedStatement(final PreparedStatement ps) {
        try {
            if (null != ps) {
                ps.close();
            }
        } catch (final SQLException e) {
            LOG.error("Error closing prepared statement!", e);
        }
    }

    @Override
    public void checkVersion(final PoolIdSchemaAndVersionInfo poolIdAndSchema) throws SQLException, OXContextRestoreException, StorageException {
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        final int pool_id = poolIdAndSchema.getPool_id();
        try {
            connection = Database.get(pool_id, poolIdAndSchema.getSchema());
            prepareStatement = connection.prepareStatement("SELECT `version`, `locked`, `gw_compatible`, `admin_compatible`, `server` FROM `version`");
            
            final ResultSet result = prepareStatement.executeQuery();
            if (result.next()) {
                final VersionInformation versionInformation2 = new VersionInformation(result.getInt(4), result.getInt(3), result.getInt(2), result.getString(5), result.getInt(1));
                if (!versionInformation2.equals(poolIdAndSchema.getVersionInformation())) {
                    throw new OXContextRestoreException(Code.VERSION_TABLES_INCOMPATIBLE);
                }
            } else {
                // Error there must be at least one row
                throw new OXContextRestoreException(Code.NO_ENTRIES_IN_VERSION_TABLE);
            }
            
        } catch (final OXException e) {
            throw new StorageException(new PoolException(e.getMessage()));
        } finally {
            if (null != prepareStatement) {
                prepareStatement.close();
            }
            if (null != connection) {
                Database.back(pool_id, connection);
            }
        }
    }

}
