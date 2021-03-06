package liquibase.change.core;

import java.util.ArrayList;
import java.util.List;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.UpdateExecutablePreparedStatement;
import liquibase.statement.core.UpdateStatement;

@DatabaseChange(name = "update", description = "Updates data in an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class UpdateDataChange extends AbstractModifyDataChange implements ChangeWithColumns<ColumnConfig> {

    private List<ColumnConfig> columns;

    public UpdateDataChange() {
        columns = new ArrayList<ColumnConfig>();
    }

    @Override
    @DatabaseChangeProperty(description = "Data to update", requiredForDatabase = "all")
    public List<ColumnConfig> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    @Override
    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
        columns.remove(column);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

    	boolean needsPreparedStatement = false;
        for (ColumnConfig column : getColumns()) {
            if (column.getValueBlobFile() != null) {
                needsPreparedStatement = true;
            }
            if (column.getValueClobFile() != null) {
                needsPreparedStatement = true;
            }
        }

        if (needsPreparedStatement) {
            return new SqlStatement[] {
                    new UpdateExecutablePreparedStatement(database, catalogName, schemaName, tableName, columns, getChangeSet())
            };
        }
    	
        UpdateStatement statement = new UpdateStatement(getCatalogName(), getSchemaName(), getTableName());

        for (ColumnConfig column : getColumns()) {
            statement.addNewColumnValue(column.getName(), column.getValueObject());
        }

        statement.setWhereClause(where);

        for (ColumnConfig whereParam : whereParams) {
            if (whereParam.getName() != null) {
                statement.addWhereColumnName(whereParam.getName());
            }
            statement.addWhereParameter(whereParam.getValueObject());
        }

        return new SqlStatement[]{
                statement
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Data updated in " + getTableName();
    }

}
