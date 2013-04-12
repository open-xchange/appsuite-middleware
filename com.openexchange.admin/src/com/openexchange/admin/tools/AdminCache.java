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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import com.damienmiller.BCrypt;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.PasswordMechObject;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseLockedException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolDBPool;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolInterface;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Streams;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.sql.DBUtils;

public class AdminCache {

    private static final AtomicReference<BundleContext> BUNDLE_CONTEXT = new AtomicReference<BundleContext>();

    /**
     * Gets the <tt>BundleContext</tt>.
     *
     * @return The <tt>BundleContext</tt> or <code>null</code>
     */
    public static BundleContext getBundleContext() {
        return BUNDLE_CONTEXT.get();
    }

    /**
     * Atomically sets the <tt>BundleContext</tt> to the given updated <tt>BundleContext</tt> reference if the current value <tt>==</tt> the expected value.
     *
     * @param expect the expected <tt>BundleContext</tt>
     * @param update the new <tt>BundleContext</tt>
     * @return <code>true</code> if successful. <code>false</code> return indicates that the actual <tt>ConfigurationService</tt> was not equal to the expected <tt>ConfigurationService</tt>.
     */
    public static boolean compareAndSetBundleContext(final BundleContext expect, final BundleContext update) {
        return BUNDLE_CONTEXT.compareAndSet(expect, update);
    }

    /**
     * Sets the <tt>BundleContext</tt>.
     *
     * @param service The <tt>BundleContext</tt> to set
     */
    public static void setBundleContext(final BundleContext service) {
        BUNDLE_CONTEXT.set(service);
    }

    private static final AtomicReference<ConfigurationService> CONF_SERVICE = new AtomicReference<ConfigurationService>();

    /**
     * Gets the <tt>ConfigurationService</tt>.
     *
     * @return The <tt>ConfigurationService</tt> or <code>null</code>
     */
    public static ConfigurationService getConfigurationService() {
        return CONF_SERVICE.get();
    }

    /**
     * Atomically sets the <tt>ConfigurationService</tt> to the given updated <tt>ConfigurationService</tt> reference if the current value <tt>==</tt> the expected value.
     *
     * @param expect the expected <tt>ConfigurationService</tt>
     * @param update the new <tt>ConfigurationService</tt>
     * @return <code>true</code> if successful. <code>false</code> return indicates that the actual <tt>ConfigurationService</tt> was not equal to the expected <tt>ConfigurationService</tt>.
     */
    public static boolean compareAndSetConfigurationService(final ConfigurationService expect, final ConfigurationService update) {
        return CONF_SERVICE.compareAndSet(expect, update);
    }

    /**
     * Sets the <tt>ConfigurationService</tt>.
     *
     * @param service The <tt>ConfigurationService</tt> to set
     */
    public static void setConfigurationService(final ConfigurationService service) {
        CONF_SERVICE.set(service);
    }

    private static final String DATABASE_INIT_SCRIPTS_ERROR_MESSAGE = "An error occured while reading the database initialization scripts.";

    public final static String DATA_TRUNCATION_ERROR_MSG = "Data too long for column(s)";

    private PropertyHandler prop = null;

    private final Log log = LogFactory.getLog(this.getClass());

    private OXAdminPoolInterface pool = null;

    private ArrayList<String> ox_queries_initial = null;

    /**
     * toggle auth for hosting features like create context etc. where master auth is needed
     */
    private boolean masterAuthenticationDisabled = false;

    /**
     * togle auth when a context based method authentication is need like create user in a context
     */
    private boolean contextAuthenticationDisabled = false;

    // sql filenames order and directory
    protected boolean log_parsed_sql_queries = false;

    // master credentials for authenticating master admin
    private Credentials masterCredentials = null;

    private Hashtable<Integer, Credentials> adminCredentialsCache = null;

    private Hashtable<Integer, String> adminAuthMechCache = null;

    // A flag to lock the database if an update is made
    private boolean lockdb = false;

    // the patterns for parsing the sql scripts
    public static final String PATTERN_REGEX_NORMAL = "(" + "CREATE\\s+TABLE|" + "DELETE|" + "UPDATE|" + "ALTER|" + "DROP|" + "SET|" + "RENAME)" + " (.*?)\\s*;";

    public static final String PATTERN_REGEX_DELIMITER = "DELIMITER (.*?)\\n";

    public static final String PATTERN_REGEX_FUNCTION = "CREATE\\s+(FUNCTION|PROCEDURE) (.*?)END\\s*//";

    private HashMap<String, UserModuleAccess> named_access_combinations = null;

    public AdminCache() {
        super();
    }

    public void initCache(final ConfigurationService service) throws OXGenericException {
        this.prop = new PropertyHandler(System.getProperties());
        cacheSqlScripts(service);
        configureAuthentication(); // disabling authentication mechs
        readMasterCredentials(service);
        this.log.info("Init Cache");
        initPool();
        this.adminCredentialsCache = new Hashtable<Integer, Credentials>();
        this.adminAuthMechCache = new Hashtable<Integer, String>();
    }

    /**
     * return default {@link UserModuleAccess} if not specified upon creation
     *
     * @return
     */
    public static UserModuleAccess getDefaultUserModuleAccess() {
        final UserModuleAccess ret = new UserModuleAccess();
        ret.disableAll();
        ret.setWebmail(true);
        ret.setContacts(true);
        return ret;
    }

    public synchronized HashMap<String, UserModuleAccess> getAccessCombinationNames() {
        return named_access_combinations;
    }

    public synchronized UserModuleAccess getNamedAccessCombination(String name) {
        return named_access_combinations.get(name);
    }

    public synchronized boolean existsNamedAccessCombination(String name) {
        return named_access_combinations.containsKey(name);
    }

    public synchronized String getNameForAccessCombination(UserModuleAccess access_combination) {
        if(named_access_combinations.containsValue(access_combination)){
            Iterator<String> names = named_access_combinations.keySet().iterator();
            String retval = null;
            while(names.hasNext()){
                String combi_name = names.next();
                if(named_access_combinations.get(combi_name).equals(access_combination)){
                    retval =  combi_name;
                    break;
                }
            }
            return retval;
        }
        return null;
    }

    public synchronized void reinitAccessCombinations() throws ClassNotFoundException, OXGenericException  {
        named_access_combinations = null;
        initAccessCombinations();
    }

    public synchronized void initAccessCombinations() throws ClassNotFoundException, OXGenericException  {
        if (named_access_combinations == null) {
            try {
                log.info("Processing access combinations...");
                named_access_combinations = getAccessCombinations(loadValidAccessModules(), loadAccessCombinations());
                log.info("Access combinations processed!");
            } catch (ClassNotFoundException e) {
                log.error("Error loading access modules and methods!", e);
                throw e;
            } catch (OXGenericException e) {
                log.error("Error processing access combinations config file!!",e);
                throw e;
            }
        }

    }

    private HashMap<String, UserModuleAccess> getAccessCombinations(HashMap<String, Method> module_method_mapping, Properties access_props) throws OXGenericException {
        HashMap<String, UserModuleAccess> combis = new HashMap<String, UserModuleAccess>();
        // Now check if predefined combinations are valid
        Enumeration<Object> predefined_access_combinations = access_props.keys();
        while (predefined_access_combinations.hasMoreElements()) {
            String predefined_combination_name = (String) predefined_access_combinations.nextElement();
            String predefined_modules = (String) access_props.get(predefined_combination_name);
            if (predefined_modules != null && predefined_modules.trim().length() > 0) {
                UserModuleAccess us = new UserModuleAccess();
                us.disableAll();
                us.setGlobalAddressBookDisabled(false); // by default this is enabled.
                String[] modules = predefined_modules.split(",");
                for (String module : modules) {
                    if (!module_method_mapping.containsKey(module)) {
                        log.error("Predefined combination \"" + predefined_combination_name + "\" contains invalid module \"" + module + "\" ");
                        // AS DEFINED IN THE CONTEXT WIDE ACCES SPECIFICAION ,
                        // THE SYSTEM WILL STOP IF IT FINDS AN INVALID
                        // CONFIGURATION!

                        // TODO: Lets stop the admin daemon
                        throw new OXGenericException("Invalid access combinations found in config file!");
                    }
                    Method meth = module_method_mapping.get(module);
                    try {
                        meth.invoke(us, true);
                    } catch (IllegalArgumentException e) {
                        log.error("Illegal argument passed to method!", e);
                    } catch (IllegalAccessException e) {
                        log.error("Illegal access!", e);
                    } catch (InvocationTargetException e) {
                        log.error("Invocation target error!", e);
                    }
                }
                // add moduleaccess object to hashmap/list identified by
                // combinations name
                combis.put(predefined_combination_name, us);
            }
        }

        return combis;
    }

    private HashMap<String, Method> loadValidAccessModules() throws ClassNotFoundException {

        // If we wanna blacklist some still unused modules, add them here!
        HashSet<String> BLACKLIST = new HashSet<String>();
        // BLACKLIST.add("");

        try {

            // Load the available combinations directly from the
            // usermoduleaccess object
            Class<?> tmp = Class.forName(UserModuleAccess.class.getCanonicalName());
            Method methlist[] = tmp.getDeclaredMethods();
            HashMap<String, Method> module_method_mapping = new HashMap<String, Method>();

            log.debug("Listing available modules for use in access combinations...");
            for (Method method : methlist) {
                String meth_name = method.getName();
                if (meth_name.startsWith("set")) {
                    // remember all valid modules and its set methods
                    String module_name = meth_name.substring(3, meth_name.length()).toLowerCase();
                    if (!BLACKLIST.contains(module_name)) {
                        module_method_mapping.put(module_name, method);
                        log.debug(module_name);
                    }
                }
            }
            log.debug("End of list for access combinations!");
            return module_method_mapping;
        } catch (ClassNotFoundException e1) {
            log.error("UserModuleAccess class not found!Exiting application!", e1);
            throw e1;
        }
    }

    private static Properties loadAccessCombinations() {
        // Load properties from file , if does not exists use fallback
        // properties!
        final ConfigurationService service = AdminServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null == service) {
            throw new IllegalStateException("Absent service: " + ConfigurationService.class.getName());
        }
        return service.getFile("ModuleAccessDefinitions.properties");
    }

    protected void initPool() {
        this.pool = new OXAdminPoolDBPool();
    }

    protected void initPool(OXAdminPoolInterface pool) {
        this.pool = pool;
    }

    public OXAdminPoolInterface getPool() {
        return pool;
    }

    public Credentials getMasterCredentials() {
        return this.masterCredentials;
    }

    public PropertyHandler getProperties() {
        return this.prop;
    }

    public Connection getConnectionForContext(final int context_id) throws PoolException {
        checkDatabaseLocked();
        return this.pool.getConnectionForContext(context_id);
    }

    public Connection getConnectionForContextNoTimeout(int contextId) throws PoolException {
        checkDatabaseLocked();
        return this.pool.getConnectionForContextNoTimeout(contextId);
    }

    public boolean pushConnectionForContext(final int context_id, final Connection con) throws PoolException {
        if(con == null) {
            return true;
        }
        return this.pool.pushConnectionForContext(context_id, con);
    }

    public boolean pushConnectionForContextNoTimeout(int contextId, Connection con) throws PoolException {
        return this.pool.pushConnectionForContextNoTimeout(contextId, con);
    }

    public Connection getConnectionForConfigDB() throws PoolException {
        return this.pool.getConnectionForConfigDB();
    }

    public boolean pushConnectionForConfigDB(final Connection con) throws PoolException {
        return this.pool.pushConnectionForConfigDB(con);
    }

    public int getServerId() throws PoolException {
        return this.pool.getServerId();
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
        // only set if authentication is enabled
        if (!this.contextAuthenticationDisabled) {
            this.adminCredentialsCache.put(ctx.getId(), adminCredentials);
            this.adminAuthMechCache.put(ctx.getId(), authMech);
        }
    }

    /**
     * @param ctx
     */
    public final void removeAdminCredentials(final Context ctx) {
        if (this.adminCredentialsCache.containsKey(ctx.getId())) {
            this.adminCredentialsCache.remove(ctx.getId());
        }
        if (this.adminAuthMechCache.containsKey(ctx.getId())) {
            this.adminAuthMechCache.remove(ctx.getId());
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

    public static Connection getSimpleSqlConnection(String url, String user, String password, String driver) throws SQLException, ClassNotFoundException {
        // System.err.println("-->"+driver+" ->"+url+" "+user+" "+password);
        Class.forName(driver);
        // give database some time to react (in seconds)
        DriverManager.setLoginTimeout(120);
        return DriverManager.getConnection(url, user, password);
    }

    private static final Pattern pattern = Pattern.compile("[\\?\\&]([\\p{ASCII}&&[^=\\&]]*)=([\\p{ASCII}&&[^=\\&]]*)");

    public Connection getSimpleSQLConnectionWithoutTimeout(String url, String user, String password, String driver) throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        DriverManager.setLoginTimeout(120);
        final int paramStart = url.indexOf('?');
        final String newUrl;
        final Properties props = new Properties();
        props.put("user", user);
        props.put("password", password);
        if (-1 != paramStart) {
            final Matcher matcher = pattern.matcher(url);
            newUrl = url.substring(0, paramStart);
            while (matcher.find()) {
                final String name = matcher.group(1);
                final String value = matcher.group(2);
                if (name != null && name.length() > 0 && value != null && value.length() > 0 && !name.toLowerCase().endsWith("timeout")) {
                    try {
                        props.put(name, URLDecoder.decode(value, "UTF-8"));
                    } catch (final UnsupportedEncodingException e) {
                        // Should not happen for UTF-8.
                        log.error(e.getMessage(), e);
                    }
                }
            }
        } else {
            newUrl = url;
        }
        return DriverManager.getConnection(newUrl, props);
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

    private void cacheSqlScripts(final ConfigurationService service) {

        if (prop.getSqlProp("LOG_PARSED_QUERIES", "false").equalsIgnoreCase("true")) {
            log_parsed_sql_queries = true;
        }

        // ox
        ox_queries_initial = convertData2Objects(getInitialOXDBOrder(), service);
    }

    private ArrayList<String> convertData2Objects(String[] sql_files_order, final ConfigurationService service) {
        final ArrayList<String> al = new ArrayList<String>(sql_files_order.length);
        final Pattern p = Pattern.compile("(" + PATTERN_REGEX_FUNCTION + "|" + PATTERN_REGEX_NORMAL + ")", Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
        for (int a = 0; a < sql_files_order.length; a++) {
            final File tmp = service.getFileByName(sql_files_order[a]);
            if (null != tmp) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(tmp);
                    final byte[] b = new byte[(int) tmp.length()];
                    fis.read(b);
                    fis.close();
                    final String data = new String(b);
                    final Matcher matchy = p.matcher(data);
                    while (matchy.find()) {
                        final String exec = matchy.group(0).replaceAll("END\\s*//", "END");
                        al.add(exec);
                        if (log_parsed_sql_queries) {
                            log.info(exec);
                        }
                    }
                    if (log_parsed_sql_queries) {
                        if (log.isInfoEnabled()) {
                            log.info(tmp + " PARSED!");
                        }
                    }
                } catch (final Exception exp) {
                    log.fatal("Parse/Read error on " + tmp, exp);
                    return null;
                } finally {
                    Streams.close(fis);
                }
            }
        }
        return al;
    }

    private static String[] getOrdered(String data) {
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
     */
    public final static StorageException parseDataTruncation(DataTruncation dt) {
        final String[] fields = DBUtils.parseTruncatedFields(dt);
        final StringBuilder sFields = new StringBuilder();
        sFields.append("Data too long for underlying storage!Error field(s): ");
        for (String field : fields) {
            sFields.append(field);
            sFields.append(",");
        }
        sFields.deleteCharAt(sFields.length() - 1);

        final StorageException st = new StorageException(sFields.toString());
        st.setStackTrace(dt.getStackTrace());
        return st;
    }

    /**
     * Encrypts password of a {@link PasswordMechObject} using the mechanism given
     * in <code>passwordMech</code>.
     * If <code>passwordMech</code> is <code>null</code>, the default mechanism as configured
     * in <code>User.properties</code> is used and set in the {@link PasswordMechObject} instance.
     *
     * @param user
     * @return the encrypted password
     * @throws StorageException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public String encryptPassword(final PasswordMechObject user) throws StorageException, NoSuchAlgorithmException, UnsupportedEncodingException {
        String passwordMech = user.getPasswordMech();
        if (isEmpty(passwordMech) || "null".equals(toLowerCase(passwordMech))) {
            String pwmech = getProperties().getUserProp(AdminProperties.User.DEFAULT_PASSWORD_MECHANISM, "SHA");
            pwmech = "{" + pwmech + "}";
            if (pwmech.equalsIgnoreCase(PasswordMechObject.CRYPT_MECH)) {
                passwordMech = PasswordMechObject.CRYPT_MECH;
            } else if (pwmech.equalsIgnoreCase(PasswordMechObject.SHA_MECH)) {
                passwordMech = PasswordMechObject.SHA_MECH;
            } else if (pwmech.equalsIgnoreCase(PasswordMechObject.BCRYPT_MECH)) {
                passwordMech = PasswordMechObject.BCRYPT_MECH;
            } else {
                log.warn("WARNING: unknown password mechanism " + pwmech + " using SHA");
                passwordMech = PasswordMechObject.SHA_MECH;
            }
            user.setPasswordMech(passwordMech);
        }
        final String passwd;
        if (PasswordMechObject.CRYPT_MECH.equals(passwordMech)) {
            passwd = UnixCrypt.crypt(user.getPassword());
        } else if (PasswordMechObject.SHA_MECH.equals(passwordMech)) {
            passwd = SHACrypt.makeSHAPasswd(user.getPassword());
        } else if (PasswordMechObject.BCRYPT_MECH.equals(passwordMech)) {
            passwd = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        } else {
            log.error("unsupported password mechanism: " + passwordMech);
            throw new StorageException("unsupported password mechanism: " + passwordMech);
        }
        return passwd;
    }

    private String[] getInitialOXDBOrder() {
        return getOrdered(prop.getSqlProp("INITIAL_OX_SQL_ORDER", "sequences.sql,ldap2sql.sql,oxfolder.sql,virtualfolder.sql,settings.sql,calendar.sql,contacts.sql,tasks.sql,infostore.sql,attachment.sql,misc.sql,ical_vcard.sql"));
    }

    private void configureAuthentication() {
        log.debug("Configuring authentication mechanisms ...");

        final String master_auth_disabled = this.prop.getProp("MASTER_AUTHENTICATION_DISABLED", "false"); // fallback is auth
        final String context_auth_disabled = this.prop.getProp("CONTEXT_AUTHENTICATION_DISABLED", "false"); // fallback is auth

        masterAuthenticationDisabled = Boolean.parseBoolean(master_auth_disabled);
        log.debug("MasterAuthentication mechanism disabled: " + masterAuthenticationDisabled);

        contextAuthenticationDisabled = Boolean.parseBoolean(context_auth_disabled);
        log.debug("ContextAuthentication mechanism disabled: " + contextAuthenticationDisabled);
    }

    private void readMasterCredentials(final ConfigurationService service) throws OXGenericException {
        BufferedReader bf = null;
        try {
            final File file = service.getFileByName("mpasswd");
            if (null != file) {
                bf = new BufferedReader(new FileReader(file), 2048);
                String line = null;
                while ((line = bf.readLine()) != null) {
                    if (!line.startsWith("#")) {
                        if (line.indexOf(':') >= 0) {
                            // ok seems to be a line with user:pass entry
                            final String[] user_pass_combination = line.split(":");
                            if (user_pass_combination.length > 0) {
                                this.masterCredentials = new Credentials(user_pass_combination[0], user_pass_combination[1]);
                                this.log.debug("Master credentials successfully set!");
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new OXGenericException("Error processing master auth file: mpasswd", e);
        } finally {
            if (null != bf) {
                try {
                    bf.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        if (masterCredentials == null) {
            final OXGenericException genericException = new OXGenericException("No master credentials defined!");
            log.warn(genericException.getMessage());
        }
    }

    private final void checkDatabaseLocked() throws PoolException {
        if (this.isLockdb()) {
            throw new PoolException(new DatabaseLockedException("The database is locked due to an update"));
        }
    }

    public void closeConfigDBSqlStuff(final Connection con, final PreparedStatement stmt, final ResultSet rs) {
        if (null != rs) {
            try {
                rs.close();
            } catch (final SQLException e) {
                log.error("Error closing resultset", e);
            }
        }
        closeConfigDBSqlStuff(con, stmt);
    }

    public void closeConfigDBSqlStuff(final Connection con, final PreparedStatement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (final SQLException e) {
            log.error("Error closing statement", e);
        }
        try {
            if (con != null) {
                pushConnectionForConfigDB(con);
            }
        } catch (final PoolException exp) {
            log.error("Pool Error pushing connection to pool!", exp);
        }
    }

    public void closeContextSqlStuff(final Connection con, final int context_id) {
        try {
            if (con != null) {
                pushConnectionForContext(context_id, con);
            }
        } catch (final PoolException exp) {
            log.error("Pool Error pushing connection to pool!", exp);
        }
    }

    public boolean isMasterAdmin(final Credentials auth) {
        return masterAuthenticationDisabled || (getMasterCredentials() != null && getMasterCredentials().getLogin().equals(auth.getLogin()));
    }

    /** ASCII-wise to lower-case */
    private static String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

    /** Check for an empty string */
    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }
}
