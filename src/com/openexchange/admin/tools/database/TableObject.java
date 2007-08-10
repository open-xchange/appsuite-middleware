
package com.openexchange.admin.tools.database;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author cutmasta
 */
public class TableObject {
    
    // table name
    private String name = null;
    
    private HashSet<String> xreferencetables = null;
    private HashSet<String> referencedby = null;
    private Vector<TableColumnObject> columns = null;
    // all rows in table
    private Vector<TableRowObject> table_rows = null;
    
    // Holds all infos about a table
    public TableObject() {
        table_rows = new Vector<TableRowObject>();
        xreferencetables = new HashSet<String>();
        referencedby = new HashSet<String>();
        columns = new Vector<TableColumnObject>();
    }
    
    public String getName(){
        return name;
    }
    
    public void setColumn(TableColumnObject to){
        this.columns.add(to);
    }
    
    public void removeColumn(TableColumnObject to){
        this.columns.remove(to);
    }
    
    public Vector<TableColumnObject> getColumns(){
        return this.columns;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDataRow(TableRowObject tc){
        this.table_rows.add(tc);
    }
    
    public TableRowObject getDataRow(int position){
        return (TableRowObject)table_rows.get(position);
    }
    
    public int getDataRowCount(){
        return table_rows.size();
    }
    
    public void addCrossReferenceTable(String reference_table){
        this.xreferencetables.add(reference_table);
    }
    
    public void removeCrossReferenceTable(String reference_table){
        this.xreferencetables.remove(reference_table);
    }
    
    public Iterator<String> getCrossReferenceTables(){
        return this.xreferencetables.iterator();
    }
    
    public boolean hasCrossReferences(){
        //System.out.println(this.name+"--> "+xreferencetables.size());        
        if(xreferencetables.size()>0){
            return true;
        }else{
            return false;
        }
    }
    
    public boolean hasCrossReference2Table(TableObject checkref){
        return this.xreferencetables.contains(checkref.getName());
    }
    
    public void addReferencedBy(String table_name) {
        this.referencedby.add(table_name);
    }
    
    public void removeReferencedBy(String reference_table){
        this.referencedby.remove(reference_table);
    }
    
    public Iterator<String> getReferencedByTables(){
        return this.referencedby.iterator();
    }
    
    public boolean isReferencedByTables(){
        //System.out.println(this.name+"--> "+referencedby.size());
        if(referencedby.size()>0){
            return true;
        }else{
            return false;
        }
    }    

    public boolean isReferencedBy(final TableObject checkref) {
        return referencedby.contains(checkref.getName());
    }
}
