package com.openexchange.database.liquibase;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import liquibase.Liquibase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;

/**
 * {@link LiquibaseExecutor}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class LiquibaseExecutor {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LiquibaseExecutor.class);

    private static final String changelog = "dbchange/first.changelog.xml";

    /**
     * @param dbService
     */
    public static void runMigration(DatabaseService dbService) {
        if (dbService == null) {
            LOG.error("DatabaseService is null. Not able to execute database migration scripts!");
            return;
        }

        Connection writable = null;
        try {
            writable = dbService.getWritable();

            JdbcConnection jdbcConnection = new JdbcConnection(writable);
            MySQLDatabase databaseConnection = new MySQLDatabase();
            databaseConnection.setConnection(jdbcConnection);

            List<ResourceAccessor> accessors = new ArrayList<ResourceAccessor>();
            accessors.add(new ClassLoaderResourceAccessor());
            accessors.add(new FileSystemResourceAccessor());

            Liquibase liquibase = new Liquibase(changelog, new CompositeResourceAccessor(accessors), databaseConnection);
            liquibase.update("");

        } catch (LiquibaseException e) {
            e.printStackTrace();
        } catch (OXException e) {
            e.printStackTrace();
        } finally {
            if (writable != null) {
                dbService.backWritable(writable);
            }
        }
    }
}
