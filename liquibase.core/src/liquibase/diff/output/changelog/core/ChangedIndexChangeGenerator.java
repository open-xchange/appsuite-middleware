package liquibase.diff.output.changelog.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.DropIndexChange;
import liquibase.database.Database;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringUtils;

public class ChangedIndexChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Index.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Index index = (Index) changedObject;

        DropIndexChange dropIndexChange = new DropIndexChange();
        dropIndexChange.setTableName(index.getTable().getName());

        CreateIndexChange addIndexChange = new CreateIndexChange();
        addIndexChange.setTableName(index.getTable().getName());
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        for (String col : index.getColumns()) {
            columns.add(new ColumnConfig().setName(col));
        }
        addIndexChange.setColumns(columns);
        addIndexChange.setIndexName(index.getName());


        if (control.isIncludeCatalog()) {
            dropIndexChange.setCatalogName(index.getSchema().getCatalogName());
            addIndexChange.setCatalogName(index.getSchema().getCatalogName());
        }
        if (control.isIncludeSchema()) {
            dropIndexChange.setSchemaName(index.getSchema().getName());
            addIndexChange.setSchemaName(index.getSchema().getName());
        }

        Difference columnNames = differences.getDifference("columnNames");
        
        if (columnNames != null) {
            String referenceColumns = StringUtils.join(
                (Collection<String>) columnNames.getReferenceValue(), ",");
            String comparedColumns = StringUtils.join(
                (Collection<String>) columnNames.getComparedValue(), ",");
    
            control.setAlreadyHandledChanged(new Index().setTable(index.getTable()).setColumns(referenceColumns));
            if (!referenceColumns.equalsIgnoreCase(comparedColumns)) {
                control.setAlreadyHandledChanged(new Index().setTable(index.getTable()).setColumns(comparedColumns));
            }
    
            if (index.isUnique() != null && index.isUnique()) {
                control.setAlreadyHandledChanged(new UniqueConstraint().setTable(index.getTable()).setColumns(referenceColumns));
                if (!referenceColumns.equalsIgnoreCase(comparedColumns)) {
                    control.setAlreadyHandledChanged(new UniqueConstraint().setTable(index.getTable()).setColumns(comparedColumns));
                }
            }
        }

        return new Change[] { dropIndexChange, addIndexChange };
    }
}
