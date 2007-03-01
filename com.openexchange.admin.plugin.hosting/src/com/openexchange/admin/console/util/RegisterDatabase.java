package com.openexchange.admin.console.util;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.MissingArgumentException;
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
    
    // Setting default values for some options
    private final static String USER_DEFAULT = "openexchange";
    private final static String PASSWD_DEFAULT = "secret";
    private final static String DRIVER_DEFAULT = "com.mysql.jdbc.Driver";
    private final static int MAXUNITS_DEFAULT = 1000;
    private final static int POOL_HARD_LIMIT_DEFAULT = 20;
    private final static int POOL_INITIAL_DEFAULT = 2;
    private final static int POOL_MAX_DEFAULT = 100;
    private final static int CLUSTER_WEIGHT_DEFAULT = 100;
    
    // Setting names for options
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
    private final static String OPT_NAME_MAX_UNITS_SHORT="x";
    private final static String OPT_NAME_MAX_UNITS_LONG="maxuser";
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
            String masterid = null;
            String maxunits = null;
            String pool_hard_limit = null;
            String pool_initial = null;
            String pool_max = null;
            String cluster_weight = null;
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
            if (false == ismaster) {
                if (cmd.hasOption(OPT_NAME_MASTER_ID_SHORT)) {
                    masterid = cmd.getOptionValue(OPT_NAME_MASTER_ID_SHORT);
                } else {
                    throw new MissingArgumentException("master id must be set if this database isn't the master");
                }
            }
            if (cmd.hasOption(OPT_NAME_MAX_UNITS_SHORT)) {
                maxunits = cmd.getOptionValue(OPT_NAME_MAX_UNITS_SHORT);
            }
            if (cmd.hasOption(OPT_NAME_POOL_HARDLIMIT_SHORT)) {
                pool_hard_limit = cmd.getOptionValue(OPT_NAME_POOL_HARDLIMIT_SHORT);
            }
            if (cmd.hasOption(OPT_NAME_POOL_INITIAL_SHORT)) {
                pool_initial = cmd.getOptionValue(OPT_NAME_POOL_INITIAL_SHORT);
            }
            if (cmd.hasOption(OPT_NAME_POOL_MAX_SHORT)) {
                pool_max = cmd.getOptionValue(OPT_NAME_POOL_MAX_SHORT);
            }
            if (cmd.hasOption(OPT_NAME_WEIGHT_SHORT)) {
                cluster_weight = cmd.getOptionValue(OPT_NAME_WEIGHT_SHORT);
            }
            
            // Setting the options in the dataobject
            db.setDisplayname(name);
            if (null != driver) {
                db.setDriver(driver);
            } else {
                db.setDriver(DRIVER_DEFAULT);
            }
            if (null != username) {
                db.setLogin(username);
            } else {
                db.setLogin(USER_DEFAULT);
            }
            if (null != password) {
                db.setPassword(password);
            } else {
                db.setPassword(PASSWD_DEFAULT);    
            }
            db.setMaster(ismaster);
            if (null != maxunits) {
                db.setMaxUnits(Integer.parseInt(maxunits));
            } else {
                db.setMaxUnits(MAXUNITS_DEFAULT);                
            }
            if (null != pool_hard_limit) {
                db.setPoolHardLimit(Integer.parseInt(pool_hard_limit));
            } else {
                db.setPoolHardLimit(POOL_HARD_LIMIT_DEFAULT);                
            }
            if (null != pool_initial) {
                db.setPoolInitial(Integer.parseInt(pool_initial));
            } else {
                db.setPoolInitial(POOL_INITIAL_DEFAULT);
            }
            if (null != pool_max) {
                db.setPoolMax(Integer.parseInt(pool_max));
            } else {
                db.setPoolMax(POOL_MAX_DEFAULT);
            }
            
            db.setUrl("jdbc:mysql://"+hostname+"/?useUnicode=true&characterEncoding=UTF-8&" +
                    "autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&" +
                    "serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
            if (null != cluster_weight) {
                db.setClusterWeight(Integer.parseInt(cluster_weight));
            } else {
                db.setClusterWeight(CLUSTER_WEIGHT_DEFAULT);
            }
            if (null != masterid) {
                db.setMasterId(Integer.parseInt(masterid));                
            }
            
            oxutil.registerDatabase(db, auth);
        } catch (java.rmi.ConnectException neti) {
            printError(neti.getMessage());
        } catch (java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
        } catch (org.apache.commons.cli.MissingArgumentException as) {
            printError("Missing arguments on the command line: " + as.getMessage());
            printHelpText(GENERAL_UTILITY_NAME, options);
        } catch (org.apache.commons.cli.UnrecognizedOptionException ux) {
            printError("Unrecognized options on the command line: " + ux.getMessage());
            printHelpText(GENERAL_UTILITY_NAME, options);
        } catch (org.apache.commons.cli.MissingOptionException mis) {
            printError("Missing options on the command line: " + mis.getMessage());
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

        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_NAME_SHORT, OPT_NAME_NAME_LONG, "name of the database", true, true)));
        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_HOSTNAME_SHORT, OPT_NAME_HOSTNAME_LONG, "hostname of the server", true, true)));
        retval.addOption(addDefaultArgName(getShortLongOptWithDefault(OPT_NAME_DB_USERNAME_SHORT, OPT_NAME_DB_USERNAME_LONG, "name of the user for the database", USER_DEFAULT, true, false)));
        retval.addOption(addDefaultArgName(getShortLongOptWithDefault(OPT_NAME_DB_DRIVER_SHORT, OPT_NAME_DB_DRIVER_LONG, "the driver to be used for the database", DRIVER_DEFAULT, true, false)));
        retval.addOption(addDefaultArgName(getShortLongOptWithDefault(OPT_NAME_DB_PASSWD_SHORT, OPT_NAME_DB_PASSWD_LONG, "password for the database", PASSWD_DEFAULT, true, false)));
        retval.addOption(getShortLongOpt(OPT_NAME_IS_MASTER_SHORT, OPT_NAME_IS_MASTER_LONG, "set this if the registered database is the master", false, false));
        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_MASTER_ID_SHORT, OPT_NAME_MASTER_ID_LONG, "if this database isn't the master give the id of the master here", true, false)));
        retval.addOption(addDefaultArgName(getShortLongOptWithDefault(OPT_NAME_WEIGHT_SHORT, OPT_NAME_WEIGHT_LONG, "the db weight for this database", String.valueOf(CLUSTER_WEIGHT_DEFAULT), true, false)));
        retval.addOption(addDefaultArgName(getShortLongOptWithDefault(OPT_NAME_MAX_UNITS_SHORT, OPT_NAME_MAX_UNITS_LONG, "the maximum number of units in this database", String.valueOf(MAXUNITS_DEFAULT), true, false)));
        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_DBPARAM_SHORT, OPT_NAME_DBPARAM_LONG, "parameter for the database", true, false)));
        // FIXME: choeger Enter right description here
        retval.addOption(addDefaultArgName(getShortLongOptWithDefault(OPT_NAME_POOL_HARDLIMIT_SHORT, OPT_NAME_POOL_HARDLIMIT_LONG, "db pool hardlimit", String.valueOf(POOL_HARD_LIMIT_DEFAULT), true, true)));
        retval.addOption(addDefaultArgName(getShortLongOptWithDefault(OPT_NAME_POOL_INITIAL_SHORT, OPT_NAME_POOL_INITIAL_LONG, "db pool initial", String.valueOf(POOL_INITIAL_DEFAULT), true, false)));
        retval.addOption(addDefaultArgName(getShortLongOptWithDefault(OPT_NAME_POOL_MAX_SHORT, OPT_NAME_POOL_MAX_LONG, "db pool max", String.valueOf(POOL_MAX_DEFAULT), true, false)));
        return retval;
    }

}
