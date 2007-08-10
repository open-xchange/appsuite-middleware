
package com.openexchange.admin.tools.database;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author cutmasta
 */
public class TableRowObject {
    
    private Hashtable<String, TableColumnObject> columns = null;
    
    /**
     * Creates a new instance of TableRowObject
     */
    public TableRowObject() {
        columns = new Hashtable<String, TableColumnObject>();
    }
    
    public void setColumn(TableColumnObject tc){
        columns.put(tc.getName(),tc);
    }
    
    public TableColumnObject getColumn(String name){
        return (TableColumnObject)columns.get(name);
    }
    
    public Enumeration<String> getColumnNames(){
        return this.columns.keys();
    }
    
}
