
/*
 * DataFetcher.java
 */

package com.openexchange.admin.tools.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

/**
 *
 * @author cutmasta
 *
 * Should retrieve all data which is related to an context.
 *
 */
public interface DataFetcher {
    
     public String getCatalogName();
     
     public Connection getDbConnection();
     
     public void setDbConnection(Connection dbConnection,String catalog_name) throws SQLException;
     
     public String getMatchingColumn();
     
     /**
     * Set the column which should be matched
     */
     public void setMatchingColumn(String column_name);
     
     public int getColumnMatchType();
     
     public Object getColumnMatchObject();
     
     // fetches data for a table object
     public TableObject getDataForTable(TableObject to) throws SQLException;
     
     /**
     * Sets the criteria match object and its correspoding type.<br>
     * For example to match an integer:<br>
     * setCriteriaMatchObject(1337,java.sql.Types.INTEGER)
     */    
     public void setColumnMatchObject(Object match_obj,int match_type);
     
     /**
     * Returns an unsorted list of tables with their data.<br>
     * Perhaps tables must be sorted cause of contraints etc.
     */
    public Vector<TableObject> fetchTableObjects() throws SQLException;
    
    /**
     * Returns an sorted list of tables with their data.<br>
     * Needed for contraints and primarys etc.
     */
    public Vector<TableObject> sortTableObjects() throws SQLException;
}
