
package com.openexchange.admin.tools.database;

/**
 *
 * @author cutmasta
 */
public class TableColumnObject {
    
    private String name = null;
    private int type = java.sql.Types.NULL;
    private Object data = null;
    private int columnSize = -1;
    
    /**
     * Creates a new instance of TableColumnObject
     */
    public TableColumnObject() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }
    
}
