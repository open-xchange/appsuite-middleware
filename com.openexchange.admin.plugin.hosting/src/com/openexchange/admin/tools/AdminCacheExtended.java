package com.openexchange.admin.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.exceptions.PoolException;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolDBPoolExtension;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolInterfaceExtension;

public class AdminCacheExtended extends AdminCache {
    
    private PropertyHandlerExtended prop = null;
    private OXAdminPoolInterfaceExtension pool = null;
    
    private ArrayList<String> sequence_tables = null;

    private ArrayList<String> ox_queries_consistency = null;
    private ArrayList<String> ox_queries_optimize = null;
    private ArrayList<String> ox_queries_initial = null;

    private Log log = LogFactory.getLog(this.getClass());
    
    // sql filenames order and directory
    private String DATABASE_INIT_SCRIPTS_ERROR_MESSAGE = "An error occured while reading the database initialization scripts.";

    public void initCacheExtended() {
        prop = new PropertyHandlerExtended(System.getProperties());
        readSequenceTables();
        cacheSqlScripts();
        pool = new OXAdminPoolDBPoolExtension(prop);
    }
    
    private void cacheSqlScripts() {

        if (prop.getSqlProp("LOG_PARSED_QUERIES", "false").equalsIgnoreCase("true")) {
            log_parsed_sql_queries = true;
        }

        // ox
        ox_queries_consistency = convertData2Objects(getConsistencyOXSqlDir(), getConsistencyOXOrder());
        ox_queries_optimize = convertData2Objects(getOptimizeOXSqlDir(), getOptimizeOXOrder());
        ox_queries_initial = convertData2Objects(getInitialOXDBSqlDir(), getInitialOXDBOrder());
    }


    
    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public int getDBPoolIdForContextId(int context_id) throws PoolException {
        return pool.getDBPoolIdForContextId(context_id);
    }
    
    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public Connection getWRITEConnectionForPoolId(int db_pool_id,String db_schema) throws PoolException{
        return pool.getWRITEConnectionForPoolId(db_pool_id,db_schema);
    }
    
    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public void pushWRITEConnectionForPoolId(int db_pool_id,Connection conny) throws PoolException {
        pool.pushWRITEConnectionForPoolId(db_pool_id,conny);
    }
    
    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public void resetPoolMappingForContext(int context_id) throws PoolException {
        pool.resetPoolMappingForContext(context_id);
    }
    
    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public String getSchemeForContextId(int context_id) throws PoolException{
        return pool.getSchemeForContextId(context_id);
    }
    
    public ArrayList getOXDBConsistencyQueries() throws OXGenericException {
        if (ox_queries_consistency == null) {
            throw new OXGenericException(DATABASE_INIT_SCRIPTS_ERROR_MESSAGE);
        }
        return ox_queries_consistency;
    }

    public ArrayList<String> getOXDBOptimizeQueries() throws OXGenericException {
        if (ox_queries_optimize == null) {
            throw new OXGenericException(DATABASE_INIT_SCRIPTS_ERROR_MESSAGE);
        }
        return ox_queries_optimize;
    }

    public ArrayList<String> getOXDBInitialQueries() throws OXGenericException {
        if (ox_queries_initial == null) {
            throw new OXGenericException(DATABASE_INIT_SCRIPTS_ERROR_MESSAGE);

        }
        return ox_queries_initial;
    }

    public void closeSimpleConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (Exception ecp) {
                log.warn("Error closing simple CONNECTION!", ecp);
            }
        }
    }

    public Connection getSimpleSqlConnection(String url, String user, String password, String driver) throws SQLException, ClassNotFoundException {
        // System.err.println("-->"+driver+" ->"+url+" "+user+" "+password);
        Class.forName(driver);
        return DriverManager.getConnection(url, user, password);
    }

    public ArrayList<String> getSequenceTables() throws OXGenericException {
        if (sequence_tables == null) {
            throw new OXGenericException("An error occured while reading the sequence tables.");
        }
        return sequence_tables;
    }

    public PropertyHandlerExtended getProperties() {
        if (prop == null) {
            initCacheExtended();
        }
        return prop;
    }

    private ArrayList<String> convertData2Objects(String sql_path, String[] sql_files_order) {
        ArrayList<String> al = new ArrayList<String>();

        for (int a = 0; a < sql_files_order.length; a++) {
            File tmp = new File(sql_path + "" + sql_files_order[a]);

            try {
                FileInputStream fis = new FileInputStream(tmp);
                byte[] b = new byte[(int) tmp.length()];
                fis.read(b);
                fis.close();
                String data = new String(b);
                Pattern p = Pattern.compile("(" + PATTERN_REGEX_FUNCTION + "|" + PATTERN_REGEX_NORMAL + ")", Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
                Matcher matchy = p.matcher(data);
                while (matchy.find()) {
                    String exec = matchy.group(0).replaceAll("END\\s*//", "END");
                    al.add(exec);
                    if (log_parsed_sql_queries) {
                        log.info(exec);
                    }
                }
                if (log_parsed_sql_queries) {
                    log.info(tmp + " PARSED!");
                }
            } catch (Exception exp) {
                log.fatal("Parse/Read error on " + tmp, exp);
                al = null;
            }

        }

        return al;
    }

    private String getConsistencyOXSqlDir() {
        return prop.getSqlProp("CONSISTENCY_OX_SQL_DIR", "/opt/openexchange-internal/system/setup/mysql/consistency/");
    }

    private String[] getConsistencyOXOrder() {
        return getOrdered(prop.getSqlProp("CONSISTENCY_OX_SQL_ORDER", "first.sql,sequences.sql,ldap2sql.sql,oxfolder.sql," + "settings.sql,tasks.sql,projects.sql,attachment.sql,misc.sql,ical_vcard.sql,last.sql"));
    }

    private String[] getOrdered(String data) {
        String[] ret = new String[0];
        if (data != null) {
            StringTokenizer st = new StringTokenizer(data, ",");
            ret = new String[st.countTokens()];
            int a = 0;
            while (st.hasMoreTokens()) {
                ret[a] = "" + st.nextToken();
                a++;
            }
        }
        return ret;
    }

    private String getOptimizeOXSqlDir() {
        return prop.getSqlProp("OPTIMIZE_OX_SQL_DIR", "/opt/openexchange-internal/system/setup/mysql/optimize/");
    }

    private String[] getOptimizeOXOrder() {

        return getOrdered(prop.getSqlProp("OPTIMIZE_OX_SQL_ORDER", "sequences.sql,ldap2sql.sql,oxfolder.sql,settings.sql," + "calendar.sql,contacts.sql,tasks.sql,projects.sql,forum.sql,pinboard.sql,infostore.sql,attachment.sql," + "misc.sql,ical_vcard.sql"));
    }

    private String[] getInitialOXDBOrder() {
        return getOrdered(prop.getSqlProp("INITIAL_OX_SQL_ORDER", "sequences.sql,ldap2sql.sql,oxfolder.sql,settings.sql" + ",calendar.sql,contacts.sql,tasks.sql,projects.sql,infostore.sql,attachment.sql,forum.sql,pinboard.sql," + "misc.sql,ical_vcard.sql"));
    }

    private void readSequenceTables() {
        final String seqfile = getInitialOXDBSqlDir() + "/optimize/sequences.sql";

        final File f = new File(seqfile);
        if (!f.canRead()) {
            log.fatal("Cannot read file " + seqfile + "!");
        }
        sequence_tables = new ArrayList<String>();
        try {
            final BufferedReader bf = new BufferedReader(new FileReader(f));
            String line = null;
            while ((line = bf.readLine()) != null) {
                if (!line.startsWith("CREATE TABLE")) {
                    continue;
                }
                final String stable = Pattern.compile("^CREATE TABLE (.*) \\($", Pattern.CASE_INSENSITIVE).matcher(line).replaceAll("$1");
                sequence_tables.add(stable);
            }
        } catch (Exception e) {
            log.fatal("Error reading sequence tables!", e);
        }

    }

    private String getInitialOXDBSqlDir() {
        return prop.getSqlProp("INITIAL_OX_SQL_DIR", "/opt/openexchange-internal/system/setup/mysql");
    }


}
