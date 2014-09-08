package com.openexchange.database.migration.internal;

import java.sql.Connection;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.migration.DBMigrationExceptionCodes;
import com.openexchange.exception.OXException;

public class LiquibaseHelper {
	
	private static final Logger LOG = LoggerFactory.getLogger(LiquibaseHelper.class);
	
	public static final String LIQUIBASE_NO_DEFINED_CONTEXT = "";

    /**
     * Prepares a new liquibase instance for the given file location.
     * The instance is initialized with a writable non-timeout connection
     * to the config database.
     *
     * @param databaseService The database service for obtaining the connection
     * @param fileLocation The file location
     * @param accessor Needed to access the given file
     * @return The initialized liquibase instance
     * @throws LiquibaseException
     * @throws OXException 
     */
    public static Liquibase prepareLiquibase(DatabaseService databaseService, String fileLocation, ResourceAccessor accessor) throws LiquibaseException, OXException {
        Connection connection = databaseService.getForUpdateTask();
        MySQLDatabase database = new MySQLDatabase();
        database.setConnection(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase(
            fileLocation,
            accessor,
            database);
        return liquibase;
    }

    /**
     * All liquibase locks are released and the underlying connection is closed
     * (resp. returned to the connection pool via the given {@link DatabaseService}.
     *
     * @param databaseService The database service for returning the connection
     * @param liquibase The liquibase instance. If <code>null</code>, calling this
     * 	                method has no effect.
     * @throws OXException If an error occurs while releasing the locks
     */
    public static void cleanUpLiquibase(DatabaseService databaseService, Liquibase liquibase) throws OXException {
        if (liquibase != null) {
            try {
                liquibase.forceReleaseLocks();
            } catch (LiquibaseException liquibaseException) {
                throw DBMigrationExceptionCodes.LIQUIBASE_ERROR.create(liquibaseException);
            } finally {
            	Database database = liquibase.getDatabase();
            	if (database != null) {            		
            		DatabaseConnection connectionWrapper = database.getConnection();
            		if (connectionWrapper != null) {
	            		try {
	            			Connection connection = ((JdbcConnection) connectionWrapper).getUnderlyingConnection();
	            			Databases.autocommit(connection);
	            			databaseService.backForUpdateTask(connection);
	            		} catch (ClassCastException e) {
	            			LOG.warn("An unexpected connection instance was passed, it will be closed manually.", e);
	            			try {
	            				connectionWrapper.close();
	            			} catch (DatabaseException d) {
	            				LOG.error("Could not close unknown connection instance!", d);
	            			}
	            		}
            		}
            	}
            }
        }
    }

}
