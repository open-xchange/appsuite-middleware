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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.DatabaseLockedException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolDBPool;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolInterface;
import com.openexchange.groupware.delete.DeleteRegistry;
import com.openexchange.tools.sql.DBUtils;

public class AdminCache {
    private static final String DATABASE_INIT_SCRIPTS_ERROR_MESSAGE = "An error occured while reading the database initialization scripts.";

    public final static String DATA_TRUNCATION_ERROR_MSG = "Data too long for column(s)";
    
    private PropertyHandler prop = null;

    private final Log log = LogFactory.getLog(this.getClass());

    private OXRunner oxrunner = null;
    
    private OXAdminPoolInterface pool = null;

    private ArrayList<String> sequence_tables = null;

    private ArrayList<String> ox_queries_initial = null;
    
    /**
     * toggle auth for hosting features like create context etc. where master auth is needed
     */
    private boolean masterAuthenticationDisabled = false;
    
    /**
     * togle auth when a context based method authentication is need like create user in a context
     */
    private  boolean contextAuthenticationDisabled = false;
    
    public static DeleteRegistry delreg = null;

    // sql filenames order and directory
    protected boolean log_parsed_sql_queries = false;
    
    // master credentials for authenticating master admin
    private Credentials masterCredentials = null;
    
    private Hashtable<Integer, Credentials> adminCredentialsCache = null;
    private Hashtable<Integer, String>      adminAuthMechCache = null;
    
    // A flag to lock the database if an update is made
    private boolean lockdb = false;

    // the patterns for parsing the sql scripts
    public static final String PATTERN_REGEX_NORMAL = "(" + "CREATE\\s+TABLE|" + "DELETE|" + "UPDATE|" + "ALTER|" + "DROP|" + "SET|" + "RENAME)" + " (.*?)\\s*;";

    public static final String PATTERN_REGEX_DELIMITER = "DELIMITER (.*?)\\n";

    public static final String PATTERN_REGEX_FUNCTION = "CREATE\\s+(FUNCTION|PROCEDURE) (.*?)END\\s*//";

    public AdminCache() {
    }

    /**
     * 
     * 
     */
    public void initCache() {

        this.prop = new PropertyHandler(System.getProperties());
        readSequenceTables();
        cacheSqlScripts();
        readMasterCredentials();
        configureAuthentication(); // disabling authentication mechs
        initOXProccess();        
        this.log.info("Init Cache");
        initPool();
        this.adminCredentialsCache = new Hashtable<Integer, Credentials>();
        this.adminAuthMechCache    = new Hashtable<Integer, String>();
    }

    protected void initPool() {
        this.pool = new OXAdminPoolDBPool(this.prop);
    }
    
    public Credentials getMasterCredentials() {
        return this.masterCredentials;
    }

    public PropertyHandler getProperties() {
        if (this.prop == null) {
            initCache();
        }
        return this.prop;
    }

    public Connection getREADConnectionForContext(final int context_id) throws PoolException {
        checkDatabaseLocked();
        return this.pool.getREADConnectionForContext(context_id);
    }

    public Connection getWRITEConnectionForContext(final int context_id) throws PoolException {
        checkDatabaseLocked();
        return this.pool.getWRITEConnectionForContext(context_id);
    }

    public boolean pushOXDBWrite(final int context_id, final Connection con) throws PoolException {
        return this.pool.pushOXDBWrite(context_id, con);
    }

    public boolean pushOXDBRead(final int context_id, final Connection con) throws PoolException {
        return this.pool.pushOXDBRead(context_id, con);
    }

    public Connection getREADConnectionForCONFIGDB() throws PoolException {
        return this.pool.getREADConnectionForCONFIGDB();
    }

    public Connection getWRITEConnectionForCONFIGDB() throws PoolException {
        return this.pool.getWRITEConnectionForCONFIGDB();
    }

    public boolean pushConfigDBRead(final Connection con) throws PoolException {
        return this.pool.pushConfigDBRead(con);
    }

    public boolean pushConfigDBWrite(final Connection con) throws PoolException {
        return this.pool.pushConfigDBWrite(con);
    }

    /**
     * @return the adminCredentials
     */
    public final Credentials getAdminCredentials(Context ctx) {        
        return this.adminCredentialsCache.get(ctx.getId());
    }

    /**
     * @return authMech
     */
    public final String getAdminAuthMech(Context ctx) {        
        return this.adminAuthMechCache.get(ctx.getId());
    }

    /**
     * @param adminCredentials the adminCredentials to set
     */
    public final void setAdminCredentials(Context ctx, String authMech, Credentials adminCredentials) {
        this.adminCredentialsCache.put(ctx.getId(),adminCredentials);
        this.adminAuthMechCache.put(ctx.getId(),authMech);
    }

    /**
     * @param ctx
     */
    public final void removeAdminCredentials(final Context ctx) {
        if( this.adminCredentialsCache.contains(ctx) ) {
            this.adminCredentialsCache.remove(ctx);
        }
        if( this.adminAuthMechCache.contains(ctx) ) {
            this.adminAuthMechCache.remove(ctx);
        }
    }
    
    public final synchronized boolean isLockdb() {
        return lockdb;
    }
    
    public boolean masterAuthenticationDisabled() {
        return masterAuthenticationDisabled;
    }
    
    public boolean contextAuthenticationDisabled() {
        return contextAuthenticationDisabled;
    }

    public final synchronized void setLockdb(boolean lockdb) {
        this.lockdb = lockdb;
    }

    public ArrayList<String> getOXDBInitialQueries() throws OXGenericException {
        if (ox_queries_initial == null) {
            throw new OXGenericException(DATABASE_INIT_SCRIPTS_ERROR_MESSAGE);

        }
        return ox_queries_initial;
    }

    public Connection getSimpleSqlConnection(String url, String user, String password, String driver) throws SQLException, ClassNotFoundException {
        // System.err.println("-->"+driver+" ->"+url+" "+user+" "+password);
        Class.forName(driver);
        // give database some time to react (in seconds)
        DriverManager.setLoginTimeout(120);
        return DriverManager.getConnection(url, user, password);
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

    private void readSequenceTables() {
        final String seqfile = getInitialOXDBSqlDir() + "/sequences.sql";

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

    public ArrayList<String> getSequenceTables() throws OXGenericException {
        if (sequence_tables == null) {
            throw new OXGenericException("An error occured while reading the sequence tables.");
        }
        return sequence_tables;
    }

    private String getInitialOXDBSqlDir() {
        return prop.getSqlProp("INITIAL_OX_SQL_DIR", "/opt/openexchange-internal/system/setup/mysql");
    }

    private void cacheSqlScripts() {

        if (prop.getSqlProp("LOG_PARSED_QUERIES", "false").equalsIgnoreCase("true")) {
            log_parsed_sql_queries = true;
        }

        // ox
        ox_queries_initial = convertData2Objects(getInitialOXDBSqlDir(), getInitialOXDBOrder());
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
                    if(log.isInfoEnabled()){
                    log.info(tmp + " PARSED!");
                    }
                }
            } catch (Exception exp) {
                log.fatal("Parse/Read error on " + tmp, exp);
                al = null;
            }

        }
        return al;
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
    
    /**
     * Parses the truncated fields out of the DataTruncation exception and
     * transforms this to a StorageException.
     * */
    public final static StorageException parseDataTruncation(DataTruncation dt){
        final String[] fields = DBUtils.parseTruncatedFields(dt);
        final StringBuilder sFields = new StringBuilder();
        sFields.append("Data too long for underlying storage!Error field(s): ");
        for (String field : fields) {
            sFields.append(field);
            sFields.append(",");
        }
        sFields.deleteCharAt(sFields.length()-1);
        
        final StorageException st = new StorageException(sFields.toString());
        st.setStackTrace(dt.getStackTrace());
        return st;
    }

    private String[] getInitialOXDBOrder() {
        return getOrdered(prop.getSqlProp("INITIAL_OX_SQL_ORDER", "sequences.sql,ldap2sql.sql,oxfolder.sql,settings.sql" + ",calendar.sql,contacts.sql,tasks.sql,projects.sql,infostore.sql,attachment.sql,forum.sql,pinboard.sql," + "misc.sql,ical_vcard.sql"));
    }
    
    private void configureAuthentication(){
        
        log.debug("Configuring authentication mechanisms ...");
        
        final String master_auth_disabled = this.prop.getProp("MASTER_AUTHENTICATION_DISABLED", "false"); // fallback is auth
        final String context_auth_disabled = this.prop.getProp("CONTEXT_AUTHENTICATION_DISABLED", "false"); // fallback is auth
        
        masterAuthenticationDisabled = Boolean.parseBoolean(master_auth_disabled);
        log.debug("MasterAuthentication mechanism disabled: "+masterAuthenticationDisabled);
        
        contextAuthenticationDisabled = Boolean.parseBoolean(context_auth_disabled);
        log.debug("ContextAuthentication mechanism disabled: "+contextAuthenticationDisabled);
    }
    

    private void readMasterCredentials() {
        final String masterfile = this.prop.getProp("MASTER_AUTH_FILE", "/opt/open-xchange/admindaemon/etc/mpasswd");
        final File tmp = new File(masterfile);
        if (!tmp.exists()) {
            this.log.fatal("Fatal! Master auth file does not exists:\n" + masterfile);
        }
        if (!tmp.canRead()) {
            this.log.fatal("Cannot read master auth file " + masterfile + "!");
        }
        BufferedReader bf = null;
        try {
            bf = new BufferedReader(new FileReader(tmp));
            String line = null;
            while ((line = bf.readLine()) != null) {
                if (!line.startsWith("#")) {
                    if (line.indexOf(":") != -1) {
                        // ok seems to be a line with user:pass entry
                        final String[] user_pass_combination = line.split(":");
                        if (user_pass_combination.length > 0) {
                            this.masterCredentials = new Credentials(user_pass_combination[0], user_pass_combination[1]);
                            this.log.debug("Master credentials successfully set!");
                        }
                    }
                }
            }
        } catch (final IOException e) {
            this.log.fatal("Error processing master auth file:\n" + masterfile, e);
        } finally {
            try {
                if (bf != null) {
                    bf.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
    
        }
    
    }

    private final void checkDatabaseLocked() throws PoolException {
        if (this.isLockdb()) {
            throw new PoolException(new DatabaseLockedException("The database is locked due to an update"));
        }
    }

    private void initOXProccess() {
        try {
            this.log.info("OX init starting...");
    
            this.oxrunner = new OXRunner();
            this.oxrunner.init();
            
            delreg = DeleteRegistry.getInstance();
            this.log.info("...OX init done!");
        } catch (final Exception ecp) {
            this.log.fatal("Error while init OX Process!", ecp);
        }
    }

}
