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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
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

	private OXAdminPoolInterface pool = null;

	private ArrayList<String> sequence_tables = null;

	private ArrayList<String> ox_queries_initial = null;

	/**
	 * toggle auth for hosting features like create context etc. where master
	 * auth is needed
	 */
	private boolean masterAuthenticationDisabled = false;

	/**
	 * togle auth when a context based method authentication is need like create
	 * user in a context
	 */
	private boolean contextAuthenticationDisabled = false;

	public static DeleteRegistry delreg = null;

	// sql filenames order and directory
	protected boolean log_parsed_sql_queries = false;

	// master credentials for authenticating master admin
	private Credentials masterCredentials = null;

	private Hashtable<Integer, Credentials> adminCredentialsCache = null;
	private Hashtable<Integer, String> adminAuthMechCache = null;

	// A flag to lock the database if an update is made
	private boolean lockdb = false;

	// the patterns for parsing the sql scripts
	public static final String PATTERN_REGEX_NORMAL = "(" + "CREATE\\s+TABLE|"
			+ "DELETE|" + "UPDATE|" + "ALTER|" + "DROP|" + "SET|" + "RENAME)"
			+ " (.*?)\\s*;";

	public static final String PATTERN_REGEX_DELIMITER = "DELIMITER (.*?)\\n";

	public static final String PATTERN_REGEX_FUNCTION = "CREATE\\s+(FUNCTION|PROCEDURE) (.*?)END\\s*//";
	
	private Properties fallback_access_combinations = new Properties();	
	
	private HashMap<String, UserModuleAccess> named_access_combinations = null;

	public AdminCache() {
	}

	/**
	 * @throws ClassNotFoundException 
	 * @throws OXGenericException 
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
		this.adminAuthMechCache = new Hashtable<Integer, String>();
	}
	
	public UserModuleAccess getNamedAccessCombination(String name){
		return named_access_combinations.get(name);
	}
	
	public boolean existsNamedAccessCombination(String name){
		return named_access_combinations.containsKey(name);
	}

	public void initAccessCombinations() throws ClassNotFoundException, OXGenericException {
		
		try {
			log.debug("Processing access combinations...");
			named_access_combinations = getAccessCombinations(loadValidAccessModules(),loadAccessCombinations());
			log.debug("Access combinations processed!");
		} catch (ClassNotFoundException e) {			
			log.error("Error loading access modules and methods!",e);	
			throw e;
		} catch (OXGenericException e) {
			log.error("Error processing access combinations config file!!",e);
			throw e;
		}
		
	}

	private HashMap<String, UserModuleAccess> getAccessCombinations(HashMap<String, Method> module_method_mapping,Properties access_props) throws OXGenericException {
			HashMap<String, UserModuleAccess> combis = new HashMap<String, UserModuleAccess>();
			// Now check if predefined combinations are valid
			Enumeration predefined_access_combinations = access_props.keys();
			while (predefined_access_combinations.hasMoreElements()) {
				String predefined_combination_name = (String) predefined_access_combinations.nextElement();
				String predefined_modules = (String) access_props.get(predefined_combination_name);
				if (predefined_modules != null && predefined_modules.trim().length() > 0) {
					UserModuleAccess us = new UserModuleAccess();
					us.disableAll();
					String[] modules = predefined_modules.split(",");
					for (String module : modules) {
						if (!module_method_mapping.containsKey(module)) {
							log.error("Predefined combination \""+ predefined_combination_name+ "\" contains invalid module \"" + module+ "\" ");
							// AS DEFINED IN THE CONTEXT WIDE ACCES SPECIFICAION , THE SYSTEM WILL STOP IF IT FINDS AN INVALID CONFIGURATION!
							
							// TODO: Lets stop the admin daemon
							throw new OXGenericException("Invalid access combinations found in config file!");
						} else {
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
			
			// Load the available combinations directly from the usermoduleaccess object		
			Class tmp = Class.forName(UserModuleAccess.class.getCanonicalName());
			Method methlist[] = tmp.getDeclaredMethods();
			HashMap<String, Method> module_method_mapping = new HashMap<String, Method>();

			log.debug("Listing available modules for use in access combinations...");
			for (Method method : methlist) {
				String meth_name = method.getName();
				if (meth_name.startsWith("set")) {
					// remember all valid modules and its set methods
					String module_name = meth_name.substring(3, meth_name.length()).toLowerCase();
					if (BLACKLIST != null && !BLACKLIST.contains(module_name)) {
						module_method_mapping.put(module_name, method);
						log.debug(module_name);
					}
				}
			}
			log.debug("End of list for access combinations!");
			return module_method_mapping;
		} catch (ClassNotFoundException e1) {
			log.error("UserModuleAccess class not found!Exiting application!",e1);
			throw e1;
		}
	}

	private Properties loadAccessCombinations() {
		// Load properties from file , if does not exists use fallback properties!
		final String access_prop_file = this.prop.getProp("ACCESS_COMBINATIONS_FILE","/opt/open-xchange/admindaemon/etc/ModuleAccessDefinitions.properties");

		Properties access_props = new Properties();

		try {
			// Load all defined combinations from file
			access_props.load(new FileInputStream(access_prop_file));
		} catch (FileNotFoundException e) {
			log.error("Access combinations file \"" + access_prop_file+ "\" could not be found!!!", e);
			// If no combinations were found, init defaults
			access_props = getFallbackAccessCombinations();
		} catch (IOException e) {
			log.error("IO Error occured while processing access combinations file \""+ access_prop_file + "\"");
			// If no combinations were found, init defaults
			access_props = getFallbackAccessCombinations();
		}
		return access_props;
	}

	private Properties getFallbackAccessCombinations() {
		// here we init the fallback access combinations when the file is not
		// present.
		fallback_access_combinations.put("basic", "contacts,webmail");
		fallback_access_combinations.put("premium","contacts,webmail,calendar,publicfolder");
		fallback_access_combinations.put("standard","contacts,webmail,calendar");
		return fallback_access_combinations;
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

	public Connection getConnectionForContext(final int context_id)
			throws PoolException {
		checkDatabaseLocked();
		return this.pool.getConnectionForContext(context_id);
	}

	public boolean pushConnectionForContext(final int context_id,
			final Connection con) throws PoolException {
		return this.pool.pushConnectionForContext(context_id, con);
	}

	public Connection getConnectionForConfigDB() throws PoolException {
		return this.pool.getConnectionForConfigDB();
	}

	public boolean pushConnectionForConfigDB(final Connection con)
			throws PoolException {
		return this.pool.pushConnectionForConfigDB(con);
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
	 * @param adminCredentials
	 *            the adminCredentials to set
	 */
	public final void setAdminCredentials(Context ctx, String authMech,
			Credentials adminCredentials) {
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
		if (this.adminCredentialsCache.contains(ctx)) {
			this.adminCredentialsCache.remove(ctx);
		}
		if (this.adminAuthMechCache.contains(ctx)) {
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

	public Connection getSimpleSqlConnection(String url, String user,
			String password, String driver) throws SQLException,
			ClassNotFoundException {
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
				final String stable = Pattern.compile(
						"^CREATE TABLE (.*) \\($", Pattern.CASE_INSENSITIVE)
						.matcher(line).replaceAll("$1");
				sequence_tables.add(stable);
			}
		} catch (Exception e) {
			log.fatal("Error reading sequence tables!", e);
		}

	}

	public ArrayList<String> getSequenceTables() throws OXGenericException {
		if (sequence_tables == null) {
			throw new OXGenericException(
					"An error occured while reading the sequence tables.");
		}
		return sequence_tables;
	}

	private String getInitialOXDBSqlDir() {
		return prop.getSqlProp("INITIAL_OX_SQL_DIR",
				"/opt/openexchange-internal/system/setup/mysql");
	}

	private void cacheSqlScripts() {

		if (prop.getSqlProp("LOG_PARSED_QUERIES", "false").equalsIgnoreCase(
				"true")) {
			log_parsed_sql_queries = true;
		}

		// ox
		ox_queries_initial = convertData2Objects(getInitialOXDBSqlDir(),
				getInitialOXDBOrder());
	}

	private ArrayList<String> convertData2Objects(String sql_path,
			String[] sql_files_order) {
		ArrayList<String> al = new ArrayList<String>();

		for (int a = 0; a < sql_files_order.length; a++) {
			File tmp = new File(sql_path + "" + sql_files_order[a]);

			try {
				FileInputStream fis = new FileInputStream(tmp);
				byte[] b = new byte[(int) tmp.length()];
				fis.read(b);
				fis.close();
				String data = new String(b);
				Pattern p = Pattern.compile("(" + PATTERN_REGEX_FUNCTION + "|"
						+ PATTERN_REGEX_NORMAL + ")", Pattern.DOTALL
						+ Pattern.CASE_INSENSITIVE);
				Matcher matchy = p.matcher(data);
				while (matchy.find()) {
					String exec = matchy.group(0)
							.replaceAll("END\\s*//", "END");
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

	private String[] getInitialOXDBOrder() {
		return getOrdered(prop
				.getSqlProp(
						"INITIAL_OX_SQL_ORDER",
						"sequences.sql,ldap2sql.sql,oxfolder.sql,settings.sql"
								+ ",calendar.sql,contacts.sql,tasks.sql,projects.sql,infostore.sql,attachment.sql,forum.sql,pinboard.sql,"
								+ "misc.sql,ical_vcard.sql"));
	}

	private void configureAuthentication() {

		log.debug("Configuring authentication mechanisms ...");

		final String master_auth_disabled = this.prop.getProp(
				"MASTER_AUTHENTICATION_DISABLED", "false"); // fallback is auth
		final String context_auth_disabled = this.prop.getProp(
				"CONTEXT_AUTHENTICATION_DISABLED", "false"); // fallback is
																// auth

		masterAuthenticationDisabled = Boolean
				.parseBoolean(master_auth_disabled);
		log.debug("MasterAuthentication mechanism disabled: "
				+ masterAuthenticationDisabled);

		contextAuthenticationDisabled = Boolean
				.parseBoolean(context_auth_disabled);
		log.debug("ContextAuthentication mechanism disabled: "
				+ contextAuthenticationDisabled);
	}

	private void readMasterCredentials() {
		final String masterfile = this.prop.getProp("MASTER_AUTH_FILE",
				"/opt/open-xchange/admindaemon/etc/mpasswd");
		final File tmp = new File(masterfile);
		if (!tmp.exists()) {
			this.log.fatal("Fatal! Master auth file does not exists:\n"
					+ masterfile);
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
							this.masterCredentials = new Credentials(
									user_pass_combination[0],
									user_pass_combination[1]);
							this.log
									.debug("Master credentials successfully set!");
						}
					}
				}
			}
		} catch (final IOException e) {
			this.log.fatal("Error processing master auth file:\n" + masterfile,
					e);
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
			throw new PoolException(new DatabaseLockedException(
					"The database is locked due to an update"));
		}
	}

	private void initOXProccess() {
		try {
			this.log.info("OX init starting...");

			/*
			 * this.oxrunner = new OXRunner(); this.oxrunner.init();
			 */

			delreg = DeleteRegistry.getInstance();
			this.log.info("...OX init done!");
		} catch (final Exception ecp) {
			this.log.fatal("Error while init OX Process!", ecp);
		}
	}

}
