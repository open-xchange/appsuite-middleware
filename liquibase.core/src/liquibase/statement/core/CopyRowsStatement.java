package liquibase.statement.core;

import java.util.List;
import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;

public class CopyRowsStatement extends AbstractSqlStatement {

	private String sourceTable;
	private String targetTable;
	private List<ColumnConfig> copyColumns;
	
	
	public CopyRowsStatement(String sourceTable, String targetTable, 
			List<ColumnConfig> copyColumns) {
		this.sourceTable = sourceTable;
		this.targetTable = targetTable;
		this.copyColumns = copyColumns;
	}
	
	public String getSourceTable() {
		return this.sourceTable;
	}
	
	public String getTargetTable() {
		return this.targetTable;
	}
	
	public List<ColumnConfig> getCopyColumns() {
		return this.copyColumns;
	}
}
