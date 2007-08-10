
package com.openexchange.admin.tools.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author cutmasta
 */
public class DataFetcherMysql implements DataFetcher{
    
    private Log log = LogFactory.getLog(DataFetcherMysql.class);
    
    private Connection dbConnection = null;
    
    private String selectionCriteria = null;
    private int criteriaType = -1;
    private Object criteriaMatch = null;
    private String catalogname = null;
    private Vector<TableObject> tableObjects = null;
    private DatabaseMetaData dbmetadata = null;
    
    public String getCatalogName(){
        return this.catalogname;
    }
    
    public DataFetcherMysql() {
    }
    
    public Connection getDbConnection() {
        return dbConnection;
    }
    
    public void setDbConnection(Connection dbConnection,String catalog_name) throws SQLException {
        this.dbConnection = dbConnection;
        this.dbConnection.setCatalog(catalog_name);
        this.catalogname = catalog_name;
    }
    
    public String getMatchingColumn() {
        return selectionCriteria;
    }
    
    public void setMatchingColumn(String column_name) {
        this.selectionCriteria = column_name;
    }
    
    public int getColumnMatchType(){
        return this.criteriaType;
    }
    
    public Object getColumnMatchObject(){
        return this.criteriaMatch;
    }
    
    public void setColumnMatchObject(Object match_obj,int match_type){
        this.criteriaType = match_type;
        this.criteriaMatch = match_obj;
    }
    
    public TableObject getDataForTable(TableObject to) throws SQLException{
        
        Vector<TableColumnObject> column_objects = to.getColumns();
        // build the statement string
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        for(int a = 0;a<column_objects.size();a++){
            TableColumnObject tco = (TableColumnObject)column_objects.get(a);
            sb.append(""+tco.getName()+",");
        }
        sb.delete(sb.length()-1,sb.length());
        sb.append(" FROM "+to.getName()+" WHERE "+getMatchingColumn()+" = ?");
        
        
        // fetch data from table
        PreparedStatement prep = null;
        try{
            prep = this.dbConnection.prepareStatement(sb.toString());
            prep.setObject(1,getColumnMatchObject(),getColumnMatchType());
            log.debug("######## "+sb.toString());
            ResultSet rs = prep.executeQuery();
            while(rs.next()){
                TableRowObject tro = new TableRowObject();
                for(int b = 0;b<column_objects.size();b++){
                    TableColumnObject tco = (TableColumnObject)column_objects.get(b);
                    Object o = rs.getObject(tco.getName());
                    
                    TableColumnObject tc2 = new TableColumnObject();
                    tc2.setColumnSize(tco.getColumnSize());
                    tc2.setData(o);
                    tc2.setName(tco.getName());
                    tc2.setType(tco.getType());
                    
                    tro.setColumn(tc2);
                }
                to.setDataRow(tro);
            }
            rs.close();
            prep.close();
        }finally{
            try {
                if(prep!=null){
                    prep.close();
                }
            } catch (Exception e) {
                log.error("Error closing statement",e);
            }
        }
        
        return to;
        
        
//        if(to.getDataRowCount()>0){
//            tableObjects.add(to);
//            //log.debug(to.getName() +" "+to.getDataRowCount());
//        }
        
        
    }
    
    public Vector<TableObject> fetchTableObjects() throws SQLException{
        tableObjects = new Vector<TableObject>();
        
        dbmetadata = dbConnection.getMetaData();
        // get the tables to check
        ResultSet rs2 = dbmetadata.getTables(null,null,null,null);
        TableObject to = null;
        while(rs2.next()){
            String table_name = rs2.getString("TABLE_NAME");
            to = new TableObject();
            to.setName(table_name);
            // fetch all columns from table and see if it contains matching column
            ResultSet columns_res = dbmetadata.getColumns(getCatalogName(),null,table_name,null);
            boolean table_matches = false;
            while(columns_res.next()){
                
                TableColumnObject tco = new TableColumnObject();
                String column_name = columns_res.getString("COLUMN_NAME");
                tco.setName(column_name);
                tco.setType(columns_res.getInt("DATA_TYPE"));
                tco.setColumnSize(columns_res.getInt("COLUMN_SIZE"));
                
                // if table has our ciriteria column, we should fetch data from it
                if(column_name.equals(getMatchingColumn())){
                    table_matches = true;
                }
                // add column to table
                to.setColumn(tco);
            }
            columns_res.close();
            if(table_matches){
                tableObjects.add(to);
            }
        }
        log.debug("####### Found -> "+tableObjects.size()+" tables");

        return tableObjects;
    }

//    TODO: This function is for testing purposes only, and may be uncommented then
//    private void printTables(Vector tablelist) {
//        final StringBuilder sb = new StringBuilder();
//        for(int a = 0;a<tablelist.size();a++){
//            TableObject to = (TableObject)tablelist.get(a);
//            sb.append("Table: ");
//            sb.append(to.getName());
//            sb.append(", cross references: ");
//            Iterator iter = to.getCrossReferenceTables();
//            while (iter.hasNext()) {
//                sb.append(iter.next());
//                sb.append(',');
//            }
//            sb.deleteCharAt(sb.length() - 1);
//            sb.append(", referenced by: ");
//            iter = to.getReferencedByTables();
//            while (iter.hasNext()) {
//                sb.append(iter.next());
//                sb.append(',');
//            }
//            sb.deleteCharAt(sb.length() - 1);
//            sb.append('\n');
//        }
//        log.error(sb.toString());
//    }
    
    public Vector<TableObject> sortTableObjects() throws SQLException {
        
        findReferences();
        // thx http://de.wikipedia.org/wiki/Topologische_Sortierung :)
        return sortTablesByForeignKey();
        
    }
    
    
    /**
     * Finds references for each table
     */
    private void findReferences() throws SQLException{
        for(int v = 0;v<tableObjects.size();v++){
            TableObject to = (TableObject)tableObjects.get(v);
            // get references from this table to another
            String table_name = to.getName();
            //ResultSet table_references = dbmetadata.getCrossReference("%",null,table_name,getCatalogName(),null,getCatalogName());
            ResultSet table_references = dbmetadata.getImportedKeys(getCatalogName(),null,table_name);
            log.debug("Table "+table_name+" has pk reference to table-column:");
            while(table_references.next()){
                String pk = table_references.getString("PKTABLE_NAME");
                String pkc = table_references.getString("PKCOLUMN_NAME");
                log.debug("--> Table: "+pk+" column ->"+pkc);
                to.addCrossReferenceTable(pk);
                int pos_in_list = tableListContainsObject(pk);
                if(pos_in_list!=-1){
                    log.debug("Found referenced by "+table_name+"<->"+pk+"->"+pkc);
                    TableObject edit_me = (TableObject)tableObjects.get(pos_in_list);
                    edit_me.addReferencedBy(table_name);
                }
            }
            table_references.close();
        }
    }
    
    
    /**
     * Returns -1 if not found else the position in the Vector where the object is located.
     */
    private int tableListContainsObject(String table_name){
        int found_at_position = -1;
        for(int v = 0;v<tableObjects.size();v++){
            TableObject to = (TableObject)tableObjects.get(v);
            if(to.getName().equals(table_name)){
                found_at_position = v;
            }
        }
        return found_at_position;
    }
    
    private Vector<TableObject> sortTablesByForeignKey() {
        Vector<TableObject> nasty_order = new Vector<TableObject>();
        
        Vector<TableObject> unsorted = new Vector<TableObject>();
        unsorted.addAll(tableObjects);
        
        // now sort the table with a topological sort mech :)
        // work with the unsorted vector
        while(unsorted.size()>0){
            for(int a = 0;a<unsorted.size();a++){
                TableObject to = (TableObject)unsorted.get(a);
                if(!to.hasCrossReferences()){
                    //log.error("removing "+to.getName());
                    nasty_order.add(to);
                    // remove object from list and sort the references new
                    removeAndSortNew(unsorted,to);
                    a--;
                }
            }
        }
        
        //printTables(nasty_order);
        
        return nasty_order;
    }
    
    /*
     * remove no more needed element from list and remove the reference to removed element
     * so that a new element exists which has now references.
     */
    private void removeAndSortNew(Vector<TableObject> v,TableObject to){
        v.remove(to);
        for(int i = 0;i<v.size();i++){
            TableObject tob = (TableObject)v.get(i);
            tob.removeCrossReferenceTable(to.getName());
        }
    }
    
}
