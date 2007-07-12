package com.openexchange.admin.console.util;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * 
 * @author d7,cutmasta
 * 
 */
public class ChangeDatabase extends UtilAbstraction {

    // Setting names for options
    private final static char OPT_NAME_DATABASE_ID_SHORT = 'i';

    private final static String OPT_NAME_DATABASE_ID_LONG = "id";

    private Option databaseIdOption = null;

    public ChangeDatabase(final String[] args2) {

        final AdminParser parser = new AdminParser("changedatabase");

        setOptions(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);

            final Database db = new Database();

            String hostname = HOSTNAME_DEFAULT;
            if (parser.getOptionValue(this.hostnameOption) != null) {
                hostname = (String) parser.getOptionValue(this.hostnameOption);
            }

            if (parser.getOptionValue(this.databaseNameOption) != null) {
                db.setDisplayname((String) parser.getOptionValue(this.databaseNameOption));
            }

            String driver = DRIVER_DEFAULT;
            if (parser.getOptionValue(this.databaseDriverOption) != null) {
                driver = (String) parser.getOptionValue(this.databaseDriverOption);
            }

            String username = USER_DEFAULT;
            if (parser.getOptionValue(this.databaseUsernameOption) != null) {
                username = (String) parser.getOptionValue(this.databaseUsernameOption);
            }

            if (parser.getOptionValue(this.databasePasswdOption) != null) {
                db.setPassword((String) parser.getOptionValue(this.databasePasswdOption));
            }

            String maxunits = String.valueOf(MAXUNITS_DEFAULT);
            if (parser.getOptionValue(this.maxUnitsOption) != null) {
                maxunits = (String) parser.getOptionValue(this.maxUnitsOption);
            }

            String pool_hard_limit = String.valueOf(POOL_HARD_LIMIT_DEFAULT);
            if (parser.getOptionValue(this.poolHardlimitOption) != null) {
                pool_hard_limit = (String) parser.getOptionValue(this.poolHardlimitOption);
            }

            String pool_initial = String.valueOf(POOL_INITIAL_DEFAULT);
            if (parser.getOptionValue(this.poolInitialOption) != null) {
                pool_initial = (String) parser.getOptionValue(this.poolInitialOption);
            }

            boolean ismaster = false;
            String masterid = null;

            String pool_max = String.valueOf(POOL_MAX_DEFAULT);
            if (parser.getOptionValue(this.poolMaxOption) != null) {
                pool_max = (String) parser.getOptionValue(this.poolMaxOption);
            }

            String cluster_weight = String.valueOf(CLUSTER_WEIGHT_DEFAULT);
            if (parser.getOptionValue(this.databaseWeightOption) != null) {
                cluster_weight = (String) parser.getOptionValue(this.databaseWeightOption);
            }

            if (parser.getOptionValue(this.databaseIsMasterOption) != null) {
                ismaster = true;
            }
            if (false == ismaster) {
                if (parser.getOptionValue(this.databaseMasterIDOption) != null) {
                    masterid = (String) parser.getOptionValue(this.databaseMasterIDOption);
                } else {
                    printError(" master id must be set if this database isn't the master");
                    parser.printUsage();
                    System.exit(1);
                }
            }

            // Setting the options in the dataobject

            db.setDriver(testStringAndGetStringOrDefault(driver, DRIVER_DEFAULT));
            if (null != hostname) {
                db.setUrl("jdbc:mysql://" + hostname + "/?useUnicode=true&characterEncoding=UTF-8&" + "autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&" + "serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
            } else {
                db.setUrl("jdbc:mysql://" + HOSTNAME_DEFAULT + "/?useUnicode=true&characterEncoding=UTF-8&" + "autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&" + "serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
            }
            db.setLogin(testStringAndGetStringOrDefault(username, USER_DEFAULT));

            db.setMaster(ismaster);
            if (null != masterid) {
                db.setMasterId(Integer.parseInt(masterid));
            }
            db.setClusterWeight(testStringAndGetIntOrDefault(cluster_weight, CLUSTER_WEIGHT_DEFAULT));
            db.setMaxUnits(testStringAndGetIntOrDefault(maxunits, MAXUNITS_DEFAULT));
            db.setPoolHardLimit(testStringAndGetIntOrDefault(pool_hard_limit, POOL_HARD_LIMIT_DEFAULT));
            db.setPoolInitial(testStringAndGetIntOrDefault(pool_initial, POOL_INITIAL_DEFAULT));
            db.setPoolMax(testStringAndGetIntOrDefault(pool_max, POOL_MAX_DEFAULT));
            // NEEDED ID
            db.setId(Integer.parseInt((String) parser.getOptionValue(this.databaseIdOption)));
            oxutil.changeDatabase(db, auth);
            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerException(e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printNotBoundResponse(e);
            sysexit(1);
        } catch (final StorageException e) {
            printServerException(e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final InvalidDataException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final IllegalOptionValueException e) {
            printError("Illegal option value : " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final UnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (final MissingOptionException e) {
            printError(e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        }

    }

    public static void main(final String args[]) {
        new ChangeDatabase(args);
    }

    private void setOptions(final AdminParser parser) {
        // oxadmin,oxadmin passwd
        setDefaultCommandLineOptions(parser);

        setDatabaseNameOption(parser, false);
        setDatabaseHostnameOption(parser, false);
        setDatabaseUsernameOption(parser, false);
        setDatabaseDriverOption(parser, false);
        setDatabasePasswdOption(parser, false);
        setDatabaseIsMasterOption(parser, false);
        setDatabaseMasterIDOption(parser, false);

        setDatabaseWeightOption(parser, false);
        setDatabaseMaxUnitsOption(parser, false);
        setDatabasePoolHardlimitOption(parser, false);
        setDatabasePoolInitialOption(parser, false);
        setDatabasePoolMaxOption(parser, false);

        this.databaseIdOption = setShortLongOpt(parser, OPT_NAME_DATABASE_ID_SHORT, OPT_NAME_DATABASE_ID_LONG, "The id of the database which should be changed", true, true);
    }
}
