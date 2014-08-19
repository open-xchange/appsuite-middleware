package com.openexchange.database.migration.custom;

import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

/**
 * Example {@link CustomSqlChange} that executes modifying 'login_info' column type.<br>
 * <br>
 * Include this by adding the following to your main configuration file:
 *  * <pre>
 * {@code
 *  <changeSet id="1" author="martin.schneider" logicalFilePath="release-7.6.1/1.login_info.changelog.xml">
 *       <customChange class="com.openexchange.database.migration.custom.ExampleCustomSqlChange" />
 *  </changeSet>
 * }
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class ExampleCustomSqlChange implements CustomSqlChange {

    private String tableName = "login2context";

    private String columnName = "login_info";

    /**
     * Main method to execute custom SQL statements<br>
     * <br>
     * {@inheritDoc}
     */
    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
            new RawSqlStatement("ALTER TABLE " + database.escapeObjectName(tableName, Table.class) + " MODIFY " + database.escapeObjectName(columnName, Column.class) + " VARCHAR(255);")
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage() {
        return "Custom class updated "+tableName+"."+columnName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws SetupException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }
}
