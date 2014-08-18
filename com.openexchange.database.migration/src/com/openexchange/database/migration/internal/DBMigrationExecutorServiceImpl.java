package com.openexchange.database.migration.internal;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import liquibase.Liquibase;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.lang.Validate;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.exception.OXException;

/**
 * {@link DBMigrationExecutorServiceImpl}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class DBMigrationExecutorServiceImpl implements DBMigrationExecutorService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DBMigrationExecutorServiceImpl.class);

    private DatabaseService databaseService;

    private ConfigurationService configurationService;

    private List<ResourceAccessor> accessors = new CopyOnWriteArrayList<ResourceAccessor>();

    public DBMigrationExecutorServiceImpl(DatabaseService databaseService, ConfigurationService configurationService) {
        super();

        Validate.notNull(databaseService, "DatabaseService mustn't be null!");
        Validate.notNull(configurationService, "ConfigurationService mustn't be null!");
        this.databaseService = databaseService;
        this.configurationService = configurationService;

        accessors.add(new ClassLoaderResourceAccessor());
        accessors.add(new FileSystemResourceAccessor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(DatabaseChangeLog databaseChangeLog) {
        Connection writable = null;
        Liquibase liquibase = null;
        JdbcConnection jdbcConnection = null;
        try {
            writable = databaseService.getWritable();

            jdbcConnection = new JdbcConnection(writable);
            jdbcConnection.setAutoCommit(true);
            MySQLDatabase databaseConnection = new MySQLDatabase();
            databaseConnection.setConnection(jdbcConnection);

            liquibase = new Liquibase(databaseChangeLog.getPhysicalFilePath(), new CompositeResourceAccessor(accessors), databaseConnection);
            liquibase.update("");
        } catch (ValidationFailedException validationFailedException) {
            LOG.error("Validation of DatabaseChangeLog failed with the following exception: " + validationFailedException.getLocalizedMessage(), validationFailedException);
        } catch (LiquibaseException liquibaseException) {
            LOG.error("Error using/executing liquibase: " + liquibaseException.getLocalizedMessage(), liquibaseException);
        } catch (OXException oxException) {
            LOG.error("Unable to retrieve database write connection: " + oxException.getLocalizedMessage(), oxException);
            // } catch (SQLException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
        } catch (Exception exception) {
            LOG.error("An unexpected error occurred while executing database migration: " + exception.getLocalizedMessage(), exception);
        } finally {
            if (liquibase != null) {
                try {
                    liquibase.forceReleaseLocks();
                } catch (LiquibaseException liquibaseException) {
                    LOG.error("Unable to release liquibase locks: " + liquibaseException.getLocalizedMessage(), liquibaseException);
                }
            }
            if (writable != null) {
                databaseService.backWritable(writable);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(String fileName) {
        File xmlConfigFile = configurationService.getFileByName(fileName);
        if (null != xmlConfigFile) {
            execute(new DatabaseChangeLog(xmlConfigFile.getAbsolutePath()));
        } else {
            LOG.info("No database migration file with name " + fileName + " found! Execution for that file will be skipped.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(String fileName, List<ResourceAccessor> additionalAccessors) {
        if (additionalAccessors == null) {
            execute(fileName);
            return;
        }

        for (ResourceAccessor accessor : additionalAccessors) {
            accessors.add(accessor);
        }
        execute(fileName);
    }

}
