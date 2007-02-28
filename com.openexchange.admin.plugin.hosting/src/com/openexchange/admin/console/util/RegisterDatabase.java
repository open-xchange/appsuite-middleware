package com.openexchange.admin.console.util;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class RegisterDatabase extends UtilAbstraction {

    private final static String GENERAL_UTILITY_NAME="registerDatabase";
    
    private final static String USER_DEFAULT = "openexchange";
    
    private final static String OPT_NAME_NAME_SHORT="n";
    private final static String OPT_NAME_NAME_LONG="name";
    private final static String OPT_NAME_HOSTNAME_SHORT="h";
    private final static String OPT_NAME_HOSTNAME_LONG="hostname";
    private final static String OPT_NAME_DB_DRIVER_SHORT="d";
    private final static String OPT_NAME_DB_DRIVER_LONG="dbdriver";
    private final static String OPT_NAME_DB_USERNAME_SHORT="u";
    private final static String OPT_NAME_DB_USERNAME_LONG="dbuser";
    private final static String OPT_NAME_DB_PASSWD_SHORT="p";
    private final static String OPT_NAME_DB_PASSWD_LONG="dbpasswd";
    private final static String OPT_NAME_IS_MASTER_SHORT="m";
    private final static String OPT_NAME_IS_MASTER_LONG="master";
    private final static String OPT_NAME_MASTER_ID_SHORT="i";
    private final static String OPT_NAME_MASTER_ID_LONG="masterid";
    private final static String OPT_NAME_WEIGHT_SHORT="w";
    private final static String OPT_NAME_WEIGHT_LONG="dbweight";
    private final static String OPT_NAME_MAX_USER_SHORT="x";
    private final static String OPT_NAME_MAX_USER_LONG="maxuser";
    private final static String OPT_NAME_DBPARAM_SHORT="p";
    private final static String OPT_NAME_DBPARAM_LONG="dbparam";
    private final static String OPT_NAME_POOL_HARDLIMIT_SHORT="l";
    private final static String OPT_NAME_POOL_HARDLIMIT_LONG="poolhardlimit";
    private final static String OPT_NAME_POOL_INITIAL_SHORT="i";
    private final static String OPT_NAME_POOL_INITIAL_LONG="poolinitial";
    private final static String OPT_NAME_POOL_MAX_SHORT="a";
    private final static String OPT_NAME_POOL_MAX_LONG="poolmax";
    
    public RegisterDatabase(String[] args2) {

        CommandLineParser parser = new PosixParser();

        Options options = getOptions();

        try {
            CommandLine cmd = parser.parse(options, args2);
            Context ctx = new Context(DEFAULT_CONTEXT);

            if (cmd.hasOption(OPT_NAME_CONTEXT_SHORT)) {
                ctx.setID(Integer.parseInt(cmd.getOptionValue(OPT_NAME_CONTEXT_SHORT)));
            }

            Credentials auth = new Credentials(cmd.getOptionValue(OPT_NAME_ADMINUSER_SHORT), cmd.getOptionValue(OPT_NAME_ADMINPASS_SHORT));

            // get rmi ref
            OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(OXUtilInterface.RMI_NAME);

            final Database db = new Database();
            String hostname = null;
            String name = null;
            String driver = null;
            String username = null;
            String password = null;
            boolean ismaster = false;
            // add optional values if set
            
            if (cmd.hasOption(OPT_NAME_NAME_SHORT)) {
                name = cmd.getOptionValue(OPT_NAME_NAME_SHORT);
            }
            if (cmd.hasOption(OPT_NAME_HOSTNAME_SHORT)) {
                hostname = cmd.getOptionValue(OPT_NAME_HOSTNAME_SHORT);
            }
            if (cmd.hasOption(OPT_NAME_DB_DRIVER_SHORT)) {
                driver = cmd.getOptionValue(OPT_NAME_DB_DRIVER_SHORT);
            }
            if (cmd.hasOption(OPT_NAME_DB_USERNAME_SHORT)) {
                username = cmd.getOptionValue(OPT_NAME_DB_DRIVER_SHORT);
            }
            if (cmd.hasOption(OPT_NAME_DB_PASSWD_SHORT)) {
                password = cmd.getOptionValue(OPT_NAME_DB_DRIVER_SHORT);
            }
            if (cmd.hasOption(OPT_NAME_IS_MASTER_SHORT)) {
                ismaster = true;
            }
            
            db.setDisplayname(name);
            if (null != driver) {
                db.setDriver(driver);
            } else {
                db.setDriver("com.mysql.jdbc.Driver");
            }
            if (null != username) {
                db.setLogin(username);
            } else {
                db.setLogin(USER_DEFAULT);
            }
            if (null != password) {
                db.setPassword(password);
            } else {
                db.setPassword("secret");    
            }
            db.setMaster(ismaster);
            db.setMaxUnits(1000);
            
            db.setPoolHardLimit(20);
            db.setPoolInitial(2);
            db.setPoolMax(100);
            db.setUrl("jdbc:mysql://"+hostname+"/?useUnicode=true&characterEncoding=UTF-8&" +
                    "autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&" +
                    "serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
            db.setClusterWeight(100);
            db.setMasterId(0);

            oxutil.registerDatabase(db, auth);
        } catch (java.rmi.ConnectException neti) {
            printError(neti.getMessage());
        } catch (java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
        } catch (org.apache.commons.cli.MissingArgumentException as) {
            printError("Missing arguments on the command line: " + as.getMessage());
            ;
            printHelpText(GENERAL_UTILITY_NAME, options);
        } catch (org.apache.commons.cli.UnrecognizedOptionException ux) {
            printError("Unrecognized options on the command line: " + ux.getMessage());
            ;
            printHelpText(GENERAL_UTILITY_NAME, options);
        } catch (org.apache.commons.cli.MissingOptionException mis) {
            printError("Missing options on the command line: " + mis.getMessage());
            ;
            printHelpText(GENERAL_UTILITY_NAME, options);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            printServerResponse(e.getMessage());
        } catch (RemoteException e) {
            printServerResponse(e.getMessage());
        } catch (NotBoundException e) {
            printServerResponse(e.getMessage());
        } catch (StorageException e) {
            printServerResponse(e.getMessage());
        } catch (InvalidCredentialsException e) {
            printServerResponse(e.getMessage());
        } catch (InvalidDataException e) {
            printServerResponse(e.getMessage());
        }

    }

    public static void main(String args[]) {
        new RegisterDatabase(args);
    }

    private Options getOptions() {
        Options retval = getDefaultCommandLineOptions();

        // add mandatory options
        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_NAME_SHORT, OPT_NAME_NAME_LONG, "name of the database", true, true)));
        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_HOSTNAME_SHORT, OPT_NAME_HOSTNAME_LONG, "hostname of the server", true, true)));
        retval.addOption(addDefaultArgName(getShortLongOptWithDefault(OPT_NAME_DB_USERNAME_SHORT, OPT_NAME_DB_USERNAME_LONG, "name of the user for the database", USER_DEFAULT, true, true)));
        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_DB_DRIVER_SHORT, OPT_NAME_DB_DRIVER_LONG, "the driver to be used for the database", true, false)));
        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_DB_PASSWD_SHORT, OPT_NAME_DB_PASSWD_LONG, "password for the database", true, true)));
        retval.addOption(getShortLongOpt(OPT_NAME_IS_MASTER_SHORT, OPT_NAME_IS_MASTER_LONG, "set this if the registered database is the master", false, false));
        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_MASTER_ID_SHORT, OPT_NAME_MASTER_ID_LONG, "if it is the master give the id here", true, true)));
        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_WEIGHT_SHORT, OPT_NAME_WEIGHT_LONG, "the db weight for this database", true, true)));
        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_MAX_USER_SHORT, OPT_NAME_MAX_USER_LONG, "the maximum number of users in this database", true, true)));
        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_DBPARAM_SHORT, OPT_NAME_DBPARAM_LONG, "parameter for the database", true, true)));
        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_POOL_HARDLIMIT_SHORT, OPT_NAME_POOL_HARDLIMIT_LONG, "db pool hardlimit", true, true)));
        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_POOL_INITIAL_SHORT, OPT_NAME_POOL_INITIAL_LONG, "db pool initial", true, true)));
        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_POOL_MAX_SHORT, OPT_NAME_POOL_MAX_LONG, "db pool max", true, true)));
        return retval;
    }

    private Option addArgName(final Option option, final String argname) {
        final Option retval = option;
        retval.setArgName(argname);
        return retval;
    }
    
    private Option addDefaultArgName(final Option option) {
        return addArgName(option, option.getLongOpt());
    }
    
    protected Option getShortLongOptWithDefault(final String shortopt, final String longopt, final String description, final String defaultvalue, final boolean hasarg, final boolean required) {
        final StringBuilder desc = new StringBuilder();
        desc.append(description);
        desc.append(". Default: ");
        desc.append(defaultvalue);
        final Option retval = new Option(shortopt, longopt, hasarg, description);
        retval.setRequired(required);
        return retval;
    }


}
