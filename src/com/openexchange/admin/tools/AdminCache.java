/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package com.openexchange.admin.tools;



import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.exceptions.PoolException;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolDBPool;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolInterface;
import com.openexchange.groupware.delete.DeleteRegistry;
import java.sql.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AdminCache {
    
    private Vector          plugins         = null;
    protected PropertyHandler prop            = null;
    private Log log = LogFactory.getLog(this.getClass());
    private OXAdminPoolInterface pool            = null;
    public static DeleteRegistry delreg = null;
    // sql filenames order and directory
    private boolean log_parsed_sql_queries = false;
    private ArrayList ox_queries_initial = null;
    private ArrayList ox_queries_optimize = null;
    private ArrayList ox_queries_consistency = null;
    private ArrayList<String> sequence_tables = null;
    
    //  master credentials for authenticating master admin
    private Credentials masterCredentials = null;
    
    // the patterns for parsing the sql scripts
    public static final String PATTERN_REGEX_NORMAL = "(" +
            "CREATE\\s+TABLE|" +
            "DELETE|" +
            "UPDATE|" +
            "ALTER|" +
            "DROP|" +
            "SET|" +
            "RENAME)" +
            " (.*?)\\s*;";
    public static final String PATTERN_REGEX_DELIMITER = "DELIMITER (.*?)\\n";
    public static final String PATTERN_REGEX_FUNCTION = "CREATE\\s+(FUNCTION|PROCEDURE) (.*?)END\\s*//";

    private String DATABASE_INIT_SCRIPTS_ERROR_MESSAGE = "An error occured while reading the database initialization scripts.";
    
    
    public AdminCache() {
    }
    
    /**
     *
     *
     */
    public void initCache() {

        prop = new PropertyHandler(System.getProperties());
        readMasterCredentials();
        cacheSqlScripts();
        readSequenceTables();
        initOXProccess();
        log.info("Init Cache");
        pool = new OXAdminPoolDBPool(prop);
    }
    
    
    public Credentials getMasterCredentials(){
        return this.masterCredentials;        
}

private void readMasterCredentials() {
    String masterfile = prop.getProp("MASTER_AUTH_FILE", "/opt/open-xchange/admindaemon/etc/mpasswd");
    File tmp = new File(masterfile);
    if(!tmp.exists()){
            log.fatal("Fatal! Master auth file does not exists:\n"+masterfile);            
    }
    if( ! tmp.canRead() ) {
        log.fatal("Cannot read master auth file "+masterfile+"!");
    }
    BufferedReader bf = null;        
    try {
        bf = new BufferedReader(new FileReader(tmp));
        String line = null;
        while( (line = bf.readLine()) != null ) {
            if(!line.startsWith("#")){
                if(line.indexOf(":")!=-1){
                    // ok seems to be a line with user:pass entry
                    String [] user_pass_combination = line.split(":");
                    if(user_pass_combination.length>0){
                        masterCredentials = new Credentials(user_pass_combination[0],user_pass_combination[1]);
                        log.debug("Master credentials successfully set!");
                    }
                }                    
            }
        }
    } catch (Exception e) {
        log.fatal("Error processing master auth file:\n"+masterfile,e);
    }finally{
        try {
            if(bf!=null){
                bf.close();
            }
        } catch (IOException e) {                
            e.printStackTrace();
        }
        
    }
    
}
    
    
    public PropertyHandler getProperties() {
        if ( prop == null ) {
            initCache();
        }
        return prop;
    }
    
    public Vector getPlugins() {
        if ( plugins == null ) {
            initCache();
        }
        return plugins;
    }
    
    
    private void readSequenceTables() {
        String seqfile = getInitialOXDBSqlDir() + "/optimize/sequences.sql";
        
        File f = new File(seqfile);
        if( ! f.canRead() ) {
            log.fatal("Cannot read file "+seqfile+"!");
        }
        BufferedReader bf;
        sequence_tables = new ArrayList<String>();
        try {
            bf = new BufferedReader(new FileReader(f));
            String line = null;
            while( (line = bf.readLine()) != null ) {
                if( ! line.startsWith("CREATE TABLE") ) {
                    continue;
                }
                String stable = Pattern.compile("^CREATE TABLE (.*) \\($",Pattern.CASE_INSENSITIVE).matcher(line).replaceAll("$1");
                sequence_tables.add(stable);
            }
        } catch (Exception e) {
            log.fatal("Error reading sequence tables!",e);
        }
        
    }
    
    private void cacheSqlScripts() {
        
        if(prop.getSqlProp("LOG_PARSED_QUERIES","false").equalsIgnoreCase("true")){
            log_parsed_sql_queries = true;
        }
        
        // ox
        ox_queries_initial = convertData2Objects(getInitialOXDBSqlDir(),getInitialOXDBOrder());
        ox_queries_optimize = convertData2Objects(getOptimizeOXSqlDir(),getOptimizeOXOrder());
        ox_queries_consistency = convertData2Objects(getConsistencyOXSqlDir(),getConsistencyOXOrder());
        
    }
    
    
    public String getInitialOXDBSqlDir(){
        return prop.getSqlProp("INITIAL_OX_SQL_DIR","/opt/openexchange-internal/system/setup/mysql");
    }
    
    public String [] getInitialOXDBOrder(){
        return getOrdered(prop.getSqlProp("INITIAL_OX_SQL_ORDER","sequences.sql,ldap2sql.sql,oxfolder.sql,settings.sql" +
                ",calendar.sql,contacts.sql,tasks.sql,projects.sql,infostore.sql,attachment.sql,forum.sql,pinboard.sql," +
                "misc.sql,ical_vcard.sql"));
    }
    
    public String getConsistencyOXSqlDir(){
        return prop.getSqlProp("CONSISTENCY_OX_SQL_DIR","/opt/openexchange-internal/system/setup/mysql/consistency/");
    }
    
    public String [] getConsistencyOXOrder(){
        return getOrdered(prop.getSqlProp("CONSISTENCY_OX_SQL_ORDER","first.sql,sequences.sql,ldap2sql.sql,oxfolder.sql," +
                "settings.sql,tasks.sql,projects.sql,attachment.sql,misc.sql,ical_vcard.sql,last.sql"));
    }
    
    public String getOptimizeOXSqlDir(){
        return prop.getSqlProp("OPTIMIZE_OX_SQL_DIR","/opt/openexchange-internal/system/setup/mysql/optimize/");
    }
    
    public String [] getOptimizeOXOrder(){
        
        return getOrdered(prop.getSqlProp("OPTIMIZE_OX_SQL_ORDER","sequences.sql,ldap2sql.sql,oxfolder.sql,settings.sql," +
                "calendar.sql,contacts.sql,tasks.sql,projects.sql,forum.sql,pinboard.sql,infostore.sql,attachment.sql," +
                "misc.sql,ical_vcard.sql"));
    }
    
    private String [] getOrdered(String data){
        String [] ret = new String[0];
        if(data!=null){
            StringTokenizer st = new StringTokenizer(data,",");
            ret = new String[st.countTokens()];
            int a = 0;
            while (st.hasMoreTokens()) {
                ret[a] = ""+st.nextToken();
                a++;
            }
        }
        return ret;
    }
    
    private ArrayList convertData2Objects(String sql_path, String[] sql_files_order)  {
        ArrayList<String> al = new ArrayList<String>();
        
        for(int a = 0;a < sql_files_order.length;a++){
            File tmp = new File(sql_path+""+sql_files_order[a]);
            
            try{
                FileInputStream fis = new FileInputStream(tmp);
                byte [] b = new byte[(int)tmp.length()];
                fis.read(b);
                fis.close();
                String data = new String(b);
                Pattern p = Pattern.compile("("+PATTERN_REGEX_FUNCTION+"|"+PATTERN_REGEX_NORMAL+")",Pattern.DOTALL+Pattern.CASE_INSENSITIVE);
                Matcher matchy = p.matcher(data);
                while(matchy.find()){
                    String exec = matchy.group(0).replaceAll("END\\s*//","END");
                    al.add(exec);
                    if(log_parsed_sql_queries){
                        log.info(exec);
                    }
                }
                if(log_parsed_sql_queries){
                    log.info(tmp+" PARSED!");
                }
            }catch(Exception exp){
                log.fatal("Parse/Read error on "+tmp,exp);
                al = null;
            }
            
        }
        
        
        return al;
    }
    
    public Connection getSimpleSqlConnection(String url,String user,String password,String driver) throws SQLException,ClassNotFoundException{
        //System.err.println("-->"+driver+" ->"+url+" "+user+" "+password);
        Class.forName(driver);
        return DriverManager.getConnection(url, user, password);
    }
    
    public void closeSimpleConnection(Connection con){
        if(con!=null){
            try{
                con.close();
            }catch(Exception ecp){
                log.warn("Error closing simple CONNECTION!", ecp );
            }
        }
    }
    
    public ArrayList<String> getSequenceTables() throws  OXGenericException{
        if( sequence_tables == null ){
            throw new OXGenericException("An error occured while reading the sequence tables.");
        }
        return sequence_tables;
    }
    
    public ArrayList getOXDBInitialQueries() throws OXGenericException {
        if( ox_queries_initial == null ){
            throw new OXGenericException(DATABASE_INIT_SCRIPTS_ERROR_MESSAGE);
            
        }
        return ox_queries_initial;
    }
    
    public ArrayList getOXDBOptimizeQueries() throws OXGenericException {
        if( ox_queries_optimize == null ){
            throw new OXGenericException(DATABASE_INIT_SCRIPTS_ERROR_MESSAGE);
        }
        return ox_queries_optimize;
    }
    
    public ArrayList getOXDBConsistencyQueries() throws OXGenericException {
        if( ox_queries_consistency == null ){
            throw new OXGenericException(DATABASE_INIT_SCRIPTS_ERROR_MESSAGE);
        }
        return ox_queries_consistency;
    }
    
    private void initOXProccess() {
        try{
            log.info("OX init starting...");
            //String driver = prop.getSqlProp("CONFIGDB_WRITE_DRIVER", "com.mysql.jdbc.Driver" );
            //String url = prop.getSqlProp("CONFIGDB_WRITE_URL", "jdbc:mysql://127.0.0.1:3306/configdb?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true" );
            //String username = prop.getSqlProp("CONFIGDB_WRITE_USERNAME","openexchange");
            //String password = prop.getSqlProp("CONFIGDB_WRITE_PASSWORD","secret");
            //int pool_size = Integer.parseInt(prop.getSqlProp("CONFIGDB_WRITE_POOL_SIZE","5"));
            //oxrunner = new OXRunner(pool_size,5,url,driver,username,password);

            OXRunner.init();
            delreg = DeleteRegistry.getInstance();            
            log.info("...OX init done!");
        }catch(Exception ecp){
            log.fatal("Error while init OX Process!",ecp);
        }
    }
    
    public Connection getREADConnectionForContext( int context_id ) throws PoolException {
        return pool.getREADConnectionForContext(context_id);
    }
    
    public Connection getWRITEConnectionForContext( int context_id ) throws PoolException {
        return pool.getWRITEConnectionForContext(context_id);
    }

    public boolean pushOXDBWrite(int context_id,Connection con) throws PoolException{
        return pool.pushOXDBWrite(context_id, con);
    }
    
    public boolean pushOXDBRead(int context_id,Connection con) throws PoolException{
        return pool.pushOXDBRead(context_id, con);
    }

    public Connection getREADConnectionForCONFIGDB() throws PoolException {
        return pool.getREADConnectionForCONFIGDB();
    }
    
    public Connection getWRITEConnectionForCONFIGDB() throws PoolException {
        return pool.getWRITEConnectionForCONFIGDB();
    }

    public boolean pushConfigDBRead(Connection con) throws PoolException {
        return pool.pushConfigDBRead(con);
    }
    
    public boolean pushConfigDBWrite(Connection con) throws PoolException{
        return pool.pushConfigDBWrite(con);
    }

    
}
