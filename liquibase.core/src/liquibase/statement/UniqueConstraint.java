package liquibase.statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import liquibase.structure.core.Index;

public class UniqueConstraint implements ColumnConstraint {
    private String constraintName;
    private List<String> columns = new ArrayList<String>();

    private Index backingIndex;

    public UniqueConstraint() {

    }

    public UniqueConstraint(String constraintName) {
        this.constraintName = constraintName;
    }

    public UniqueConstraint addColumns(String... columns) {
        this.columns.addAll(Arrays.asList(columns));

        return this;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public List<String> getColumns() {
        return columns;
    }
}
