package liquibase.snapshot.jvm;

import java.sql.ResultSet;
import java.sql.SQLException;
import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.util.JdbcUtils;

public class SchemaSnapshotGenerator extends JdbcSnapshotGenerator {


    public SchemaSnapshotGenerator() {
        super(Schema.class);
    }


    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        ResultSet schemas = null;
        Schema match = null;

        String catalogName = ((Schema) example).getCatalogName();
        String schemaName = example.getName();
        if (database.supportsSchemas()) {
            if (catalogName == null) {
                catalogName = database.getDefaultCatalogName();
            }
            if (schemaName == null) {
                schemaName = database.getDefaultSchemaName();
            }
        } else {
            if (database.supportsCatalogs()) {
                if (catalogName == null && schemaName != null) {
                    catalogName = schemaName;
                    schemaName = null;
                }
            } else {
                catalogName = null;
                schemaName = null;
            }
        }

        example = new Schema(catalogName, schemaName);

        // use LEGACY quoting since we're dealing with system objects
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            if (database.supportsSchemas()) {
                schemas = ((JdbcConnection) database.getConnection()).getMetaData().getSchemas();
                while (schemas.next()) {
                    String tableSchema = JdbcUtils.getValueForColumn(schemas, "TABLE_SCHEM", database);
                    CatalogAndSchema schemaFromJdbcInfo = ((AbstractJdbcDatabase) database).getSchemaFromJdbcInfo(null, tableSchema);

                    Catalog catalog = new Catalog(schemaFromJdbcInfo.getCatalogName());

                    Schema schema = new Schema(catalog, schemaFromJdbcInfo.getSchemaName());
                    if (DatabaseObjectComparatorFactory.getInstance().isSameObject(schema, example, database)) {
                        if (match == null) {
                            match = schema;
                        } else {
                            throw new InvalidExampleException("Found multiple catalog/schemas matching " + ((Schema) example).getCatalogName() + "." + example.getName());
                        }
                    }
                }
            } else {
                Catalog catalog = new Catalog(catalogName);
                return new Schema(catalog, null);
            }

        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
            if (schemas != null) {
                try {
                    schemas.close();
                } catch (SQLException ignore) {

                }
            }
        }
        return match;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        //no other types
    }
}
