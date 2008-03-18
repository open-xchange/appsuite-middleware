package com.openexchange.admin.console;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogManager;

import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.database.DatabaseInit;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;

public class ReportingTool extends BasicCommandlineOptions {
	
	private static final String OPT_TOTAL_STATS = "total"; // for listing total stats like 1212 contexts and 222 users in the system
	private static final String OPT_DETAILED_STATS = "detail"; // for listing total stats like 1212 contexts and 222 users in the system
	private Option OPTION_TOTAL_STATS = null;
	private Option OPTION_DETAILED_STATS = null;
	private SimpleDateFormat DATE_FORMATTER = null;
	
	AdminParser parser = null;
	Credentials RMI_AUTH = null;

	Context[] ALL_SYSTEM_CONTEXTS = null;
	User [] ALL_SYSTEM_USER = null;
	ArrayList<context2UsersMap> ALL_CONTEXTS_WITH_USERS_LIST = null;

	public static void main(String[] args) {
		ReportingTool rt = new ReportingTool(args);		
	}

	public ReportingTool(String[] args) {
		// set up cmd parser
		DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		parser = new AdminParser("oxreport");

		try {

			init(args);
			
			if (null != parser.getOptionValue(this.OPTION_TOTAL_STATS) 
					&& null != parser.getOptionValue(this.OPTION_DETAILED_STATS)) {
				// NOT CORRECT; YOU CAN ONLY USE ONE OF THE TWO SWITCHES!
				printError("You cannot specify "+OPT_TOTAL_STATS+" and "+OPT_DETAILED_STATS+" at the same time", parser);
				parser.printUsage();
				sysexit(SYSEXIT_UNKNOWN_OPTION);
			}
			
			
			if (null != parser.getOptionValue(this.OPTION_TOTAL_STATS) || null != parser.getOptionValue(this.OPTION_DETAILED_STATS)) {
				
				start();
				
				
				if (null != parser.getOptionValue(this.OPTION_TOTAL_STATS)) {
					
					// print out data
					if (null != parser.getOptionValue(this.csvOutputOption)) {
						precvsinfostotal();
					} else {
						sysoutOutputTotal();
					}
				}
				
				if (null != parser.getOptionValue(this.OPTION_DETAILED_STATS)) {
					
					// print detailed stats
					if (null != parser.getOptionValue(this.csvOutputOption)) {
						precvsinfosdetailed();
					} else {
						sysoutOutputDetail();
					}
				}
				
				
			}
			

			sysexit(0);
		} catch (final IllegalOptionValueException e) {
			printError("Illegal option value : " + e.getMessage(), parser);
			parser.printUsage();
			sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
		} catch (final UnknownOptionException e) {
			printError("Unrecognized options on the command line: " +e.getMessage(), parser);
			parser.printUsage();
			sysexit(SYSEXIT_UNKNOWN_OPTION);
		} catch (final MissingOptionException e) {
			printError(e.getMessage(), parser);
			parser.printUsage();
			sysexit(SYSEXIT_MISSING_OPTION);
		} catch (DBPoolingException e) {
			printServerException(e, parser);
			sysexit(1);		
		} catch (InvalidDataException e) {
			printServerException(e, parser);
			sysexit(1);
		} catch (ConfigurationException e) {
			printServerException(e, parser);
			sysexit(1);
		} finally {
			shutdown();
		}
	}

	private void sysoutOutputTotal() throws InvalidDataException {
		final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		final ArrayList<String> container_data = new ArrayList<String>();  
		if (ALL_SYSTEM_CONTEXTS != null && ALL_SYSTEM_USER!=null) {	
			container_data.add(""+ALL_SYSTEM_CONTEXTS.length);
			container_data.add(""+ALL_SYSTEM_USER.length);
			data.add(container_data);
		}
		
		doOutput(new String[] { "l", "l"},new String[] { "CONTEXTS", "USERS"},data);
	}
	
	private void precvsinfostotal() throws InvalidDataException {
		
		// PRINT SYS CONTEXTS AND SYS USERS
		final ArrayList<String> columns = new ArrayList<String>();
		columns.add("contexts");
		columns.add("users");
		final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		if (ALL_SYSTEM_CONTEXTS != null && ALL_SYSTEM_USER!=null) {			
			final ArrayList<String> container_data = new ArrayList<String>();
			container_data.add(""+ALL_SYSTEM_CONTEXTS.length);
			container_data.add(""+ALL_SYSTEM_USER.length);
			data.add(container_data);
		}

		doCSVOutput(columns, data);

	}
	
	private void precvsinfosdetailed() throws InvalidDataException {
		
		// PRINT contexts and all its users
		final ArrayList<String> columns = new ArrayList<String>();
		columns.add("id");
		columns.add("users");
		columns.add("age");
		columns.add("created");			
		columns.add("mappings");
		
		final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		if (ALL_CONTEXTS_WITH_USERS_LIST != null) {
			for (context2UsersMap ctxmap : ALL_CONTEXTS_WITH_USERS_LIST) {
				final ArrayList<String> container_data = new ArrayList<String>();
				container_data.add(ctxmap.getContext().getIdAsString());
				container_data.add(""+ctxmap.getUsers().length);
				// age
				container_data.add(""+ctxmap.getAge()); 
				// created
				container_data.add(formatToDisplayDate(ctxmap.getCreatingdate()));				
				
				container_data.add(getObjectsAsString(ctxmap.getContext().getLoginMappings().toArray()));
				data.add(container_data);
			}
		}

		doCSVOutput(columns, data);

	}
	
	private void sysoutOutputDetail() throws InvalidDataException {
		
		final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		if (ALL_CONTEXTS_WITH_USERS_LIST != null) {
			for (context2UsersMap ctxmap : ALL_CONTEXTS_WITH_USERS_LIST) {
				final ArrayList<String> container_data = new ArrayList<String>();
				container_data.add(ctxmap.getContext().getIdAsString());
				container_data.add(""+ctxmap.getUsers().length);
				//age
				container_data.add(""+ctxmap.getAge());
				// created
				container_data.add(formatToDisplayDate(ctxmap.getCreatingdate()));				
				container_data.add(getObjectsAsString(ctxmap.getContext().getLoginMappings().toArray()));
				
				data.add(container_data);
			}
		}
		
		doOutput(new String[] { "l", "l","l","l","l"},new String[] { "ID","USERS","AGE","CREATED","MAPPINGS"},data);
	}

	private void setOptions() {
		setDefaultCommandLineOptionsWithoutContextID(parser);
		setCSVOutputOption(parser);		
		this.OPTION_TOTAL_STATS = setLongOpt(parser, OPT_TOTAL_STATS, "List total contexts/users", false, false);
		this.OPTION_DETAILED_STATS = setLongOpt(parser, OPT_DETAILED_STATS, "List detailed stats for every context", false, false);
	}

	private void shutdown() {
		// close connections etc
		DatabaseInit.getInstance().stop();
	}

	private void start() throws DBPoolingException, ConfigurationException {
		// init db pool to fetch directly the data
		SystemConfig.getInstance().start();
		DatabaseInit.getInstance().start();
		//DatabaseInit.init();
		// fetch data via sql or rmi interface
		fetchAllSystemContexts();
		fetchAllUsers();
		fetchAgeForSystemContexts(); // check when the context was created
	}
	
	

	private void fetchAgeForSystemContexts() {
		// select user from user_setting_admin where cid = 31; <-- ADMIN USER
		// select creating_date from prg_contacts where cid = 31 and userid = 2; LONG als creating date des context admins
		
		for (context2UsersMap ctxmap : ALL_CONTEXTS_WITH_USERS_LIST) {
			
			com.openexchange.groupware.contexts.Context tmp = new ContextImpl(ctxmap.getContext().getId());
			
			Connection con = null;
			PreparedStatement ps = null;
			
			try {
				
				con = DBPool.pickup(tmp);
				
				ps = con.prepareStatement("SELECT user FROM user_setting_admin WHERE cid = ?"); // fetch admin id
				ps.setInt(1, ctxmap.getContext().getId());
				
				ResultSet rs = ps.executeQuery();
				int admin_id = -1;
				
				while (rs.next()) {
					admin_id = rs.getInt(1);
				}
				rs.close();
				ps.close();
				
				// fetch long from database
				ps = con.prepareStatement("SELECT creating_date FROM prg_contacts WHERE cid = ? AND userid = ?"); 
				ps.setInt(1, ctxmap.getContext().getId());
				ps.setInt(2, admin_id);
				rs = ps.executeQuery();
				long ADMIN_CREATING_DAY = -1;
				while (rs.next()) {
					ADMIN_CREATING_DAY = rs.getLong(1);
				}
				rs.close();
				ps.close();
				
				ctxmap.setCreatingdate(ADMIN_CREATING_DAY); // creating day in context for calculating the age
				ctxmap.setAge(calculateContextAge(ADMIN_CREATING_DAY)); // set age in days 
				
			} catch (DBPoolingException e) {				
				e.printStackTrace();
			}catch (SQLException e) {			
				e.printStackTrace();
			}finally{
				closePreparedStatement(ps);
				try {
					DBPool.push(tmp,con);
				} catch (DBPoolingException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	private void fetchAllUsers() {
		
		// fetch all users for every contexts 
		ALL_CONTEXTS_WITH_USERS_LIST = new ArrayList<context2UsersMap>(); // CONTAINS NOW ALL CONTEXTS WITH ALL USERS
		
		final ArrayList<User> GLOBAL_USERS_LIST = new ArrayList<User>(); // this will be filled with all users accross all contexts
		
		for (Context ctx : ALL_SYSTEM_CONTEXTS) {
			
			com.openexchange.groupware.contexts.Context tmp = new ContextImpl(ctx.getId());
			Connection con = null;
			PreparedStatement ps = null;
			
			try {
				
				con = DBPool.pickup(tmp);
				
				ps = con.prepareStatement("SELECT user.id FROM user where user.cid = ?");
				ps.setInt(1, ctx.getId());
				
				ResultSet rs = ps.executeQuery();
				final ArrayList<User> users_in_context_liste = new ArrayList<User>();
				while (rs.next()) {
					User usr = new User(rs.getInt(1));
					users_in_context_liste.add(usr);
					GLOBAL_USERS_LIST.add(usr);
				}
				
				// fill the list with all contexts and its users
				context2UsersMap tmp_obj = new context2UsersMap(users_in_context_liste.toArray(new User[users_in_context_liste.size()]),ctx);
				ALL_CONTEXTS_WITH_USERS_LIST.add(tmp_obj);
				
				rs.close();	
				ps.close();
				
			} catch (DBPoolingException e) {
				e.printStackTrace();
			}catch (SQLException e) {			
				e.printStackTrace();
			}finally{
				closePreparedStatement(ps);
				try {
					DBPool.push(tmp,con);
				} catch (DBPoolingException e) {
					e.printStackTrace();
				}
			}
		}
		
		// set the ALL_SYSTEM_USER
		ALL_SYSTEM_USER = GLOBAL_USERS_LIST.toArray(new User[GLOBAL_USERS_LIST.size()]);
	}

	private void fetchAllSystemContexts() {
		// Fill up ALL_SYSTEM_CONTEXTS
		Connection con = null;
		PreparedStatement ps = null;
		PreparedStatement mapping = null;
		ResultSet mapping_rs = null;
		try {
			
			con = DBPool.pickupWriteable();
			
			ps = con.prepareStatement("SELECT context.cid FROM context");
			mapping = con.prepareStatement("SELECT login_info FROM login2context WHERE cid=?");
			
			ResultSet rs = ps.executeQuery();
			final ArrayList<Context> list = new ArrayList<Context>();
			while (rs.next()) {
				Context tmp = new Context(rs.getInt(1));
				
				// fetch login mappings
				mapping.setInt(1, rs.getInt(1));
				mapping_rs = mapping.executeQuery();
                while (mapping_rs.next()) {
                	tmp.addLoginMapping(mapping_rs.getString(1));
                }
                mapping_rs.close();
				
				list.add(tmp);
			}
			
			ALL_SYSTEM_CONTEXTS = list.toArray(new Context[list.size()]);
			
			rs.close();
			
		} catch (DBPoolingException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
			closePreparedStatement(ps);
			closePreparedStatement(mapping);
			try {
				DBPool.pushWrite(con);
			} catch (DBPoolingException e) {
				e.printStackTrace();
			}
		}

	}
	
	private void closePreparedStatement(PreparedStatement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

	private void init(String[] args) throws IllegalOptionValueException,
			UnknownOptionException, MissingOptionException {

		// init options in cmd parser
		setOptions();
		// parse options
		parser.ownparse(args);
		// get creds for auth for later user in rmi interface
		RMI_AUTH = credentialsparsing(parser);

		// try to disable logging either here via code or we supply an own
		// logging.properties file
		// via the shell wrapper
		LogManager.getLogManager().getLogger("global").setLevel(Level.OFF);

		
		
	}
	
	private class context2UsersMap{
		
		private User[] users = null;
		private Context context = null;
		private long creatingdate = -1;
		private long age = -1;
		
		public context2UsersMap(User[] usrs,Context ctx){
			this.users = usrs;
			this.context = ctx;
		}
		
		public void setUsers(User[] users) {
			this.users = users;
		}
		public User[] getUsers() {
			return users;
		}
		public void setContext(Context context) {
			this.context = context;
		}
		public Context getContext() {
			return context;
		}

		public void setCreatingdate(long creatingdate) {
			this.creatingdate = creatingdate;
		}

		public long getCreatingdate() {
			return creatingdate;
		}

		public void setAge(long age) {
			this.age = age;
		}

		public long getAge() {
			return age;
		}
	}
	
	private long calculateContextAge(long creating_date){
		
		long today = System.currentTimeMillis();
		long diff = today-creating_date;
		
		return diff/(24*60*60*1000);
		
	}
	
	private String formatToDisplayDate(long creating_date){
		Date d = new Date(creating_date);
		return DATE_FORMATTER.format(d);
	}

}