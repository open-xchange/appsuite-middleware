package com.openexchange.database.migration.custom;

import liquibase.change.custom.CustomSqlChange;
import liquibase.change.custom.CustomSqlRollback;
import liquibase.database.Database;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

public class ExampleCustomSqlChange implements CustomSqlChange, CustomSqlRollback {

    private String tableName = "login2context";

    private String columnName = "login_info";

    private String newValue = "test";

    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private ResourceAccessor resourceAccessor;


    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
            new RawSqlStatement("update "+database.escapeObjectName(tableName, Table.class)
                +" set "+database.escapeObjectName(columnName, Column.class)+" = "+newValue)
        };
    }

    @Override
    public SqlStatement[] generateRollbackStatements(Database database) throws RollbackImpossibleException {
        return new SqlStatement[]{
            new RawSqlStatement("update "+database.correctObjectName(tableName, Table.class)
                +" set "+database.escapeObjectName(columnName, Column.class)+" = null")
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Custom class updated "+tableName+"."+columnName;
    }

    @Override
    public void setUp() throws SetupException {
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

}
