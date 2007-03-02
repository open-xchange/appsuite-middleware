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
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class RegisterDatabase extends UtilAbstraction {

    private final static String GENERAL_UTILITY_NAME="registerDatabase";
    
    // Setting default values for some options
    private final static String DRIVER_DEFAULT = "com.mysql.jdbc.Driver";
    private final static int POOL_HARD_LIMIT_DEFAULT = 20;
    private final static int POOL_INITIAL_DEFAULT = 2;
    private final static int POOL_MAX_DEFAULT = 100;
    
    // Setting names for options
    private final static String OPT_NAME_DB_DRIVER_SHORT="d";
    private final static String OPT_NAME_DB_DRIVER_LONG="dbdriver";
    
    public RegisterDatabase(final String[] args2) {

        final CommandLineParser parser = new PosixParser();

        final Options options = getOptions();

        try {
            final CommandLine cmd = parser.parse(options, args2);

            final Credentials auth = new Credentials(cmd.getOptionValue(OPT_NAME_ADMINUSER_SHORT), cmd.getOptionValue(OPT_NAME_ADMINPASS_SHORT));

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(OXUtilInterface.RMI_NAME);

            final Database db = new Database();
            final String hostname = verifySetAndGetOption(cmd, OPT_NAME_HOSTNAME_SHORT);
            final String name = cmd.getOptionValue(OPT_NAME_DBNAME_SHORT);
            final String driver = verifySetAndGetOption(cmd, OPT_NAME_DB_DRIVER_SHORT);
            final String username = verifySetAndGetOption(cmd, OPT_NAME_DB_USERNAME_SHORT);
            final String password = cmd.getOptionValue(OPT_NAME_DB_PASSWD_SHORT);
            boolean ismaster = false;
            String masterid = null;
            final String maxunits = verifySetAndGetOption(cmd, OPT_NAME_MAX_UNITS_SHORT);
            final String pool_hard_limit = verifySetAndGetOption(cmd, OPT_NAME_POOL_HARDLIMIT_SHORT);
            final String pool_initial = verifySetAndGetOption(cmd, OPT_NAME_POOL_INITIAL_SHORT);
            final String pool_max = verifySetAndGetOption(cmd, OPT_NAME_POOL_MAX_SHORT);
            final String cluster_weight = verifySetAndGetOption(cmd, OPT_NAME_WEIGHT_SHORT);
            // add optional values if set
            
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
            db.setPassword(password);
            db.setMaster(ismaster);
            if (null != maxunits) {
                db.setMaxUnits(Integer.parseInt(maxunits));
            } else {
                db.setMaxUnits(MAXUNITS_DEFAULT);                
            }
            db.setPoolHardLimit(testStringAndGetIntOrDefault(pool_hard_limit, POOL_HARD_LIMIT_DEFAULT));
            db.setPoolInitial(testStringAndGetIntOrDefault(pool_initial, POOL_INITIAL_DEFAULT));
            db.setPoolMax(testStringAndGetIntOrDefault(pool_max, POOL_MAX_DEFAULT));
            
            if (null != hostname) {
                db.setUrl("jdbc:mysql://"+hostname+"/?useUnicode=true&characterEncoding=UTF-8&" +
                        "autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&" +
                        "serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
            } else {
                db.setUrl("jdbc:mysql://"+HOSTNAME_DEFAULT+"/?useUnicode=true&characterEncoding=UTF-8&" +
                        "autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&" +
                        "serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
            }
            db.setClusterWeight(testStringAndGetIntOrDefault(cluster_weight, CLUSTER_WEIGHT_DEFAULT));
            if (null != masterid) {
                db.setMasterId(Integer.parseInt(masterid));                
            }
            
            oxutil.registerDatabase(db, auth);
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
        } catch (final org.apache.commons.cli.MissingArgumentException as) {
            printError("Missing arguments on the command line: " + as.getMessage());
            printHelpText(GENERAL_UTILITY_NAME, options);
        } catch (final org.apache.commons.cli.UnrecognizedOptionException ux) {
            printError("Unrecognized options on the command line: " + ux.getMessage());
            printHelpText(GENERAL_UTILITY_NAME, options);
        } catch (final org.apache.commons.cli.MissingOptionException mis) {
            printError("Missing options on the command line: " + mis.getMessage());
            printHelpText(GENERAL_UTILITY_NAME, options);
        } catch (final ParseException e) {
            e.printStackTrace();
        } catch (final MalformedURLException e) {
            printServerResponse(e.getMessage());
        } catch (final RemoteException e) {
            printServerResponse(e.getMessage());
        } catch (final NotBoundException e) {
            printServerResponse(e.getMessage());
        } catch (final StorageException e) {
            printServerResponse(e.getMessage());
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e.getMessage());
        } catch (final InvalidDataException e) {
            printServerResponse(e.getMessage());
        }

    }

    public static void main(final String args[]) {
        new RegisterDatabase(args);
    }

    private Options getOptions() {
        final Options retval = getDefaultCommandLineOptions();

        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_DBNAME_SHORT, OPT_NAME_DBNAME_LONG, "name of the database", true, true)));
        retval.addOption(addDefaultArgName(getShortLongOptWithDefault(OPT_NAME_HOSTNAME_SHORT, OPT_NAME_HOSTNAME_LONG, "hostname of the server", HOSTNAME_DEFAULT, true, false)));
        retval.addOption(addDefaultArgName(getShortLongOptWithDefault(OPT_NAME_DB_USERNAME_SHORT, OPT_NAME_DB_USERNAME_LONG, "name of the user for the database", USER_DEFAULT, true, false)));
        retval.addOption(addDefaultArgName(getShortLongOptWithDefault(OPT_NAME_DB_DRIVER_SHORT, OPT_NAME_DB_DRIVER_LONG, "the driver to be used for the database", DRIVER_DEFAULT, true, false)));
        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_DB_PASSWD_SHORT, OPT_NAME_DB_PASSWD_LONG, "password for the database", true, true)));
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
