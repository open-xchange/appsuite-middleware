//package com.openexchange.admin.console.util;
//
//import java.net.MalformedURLException;
//import java.rmi.Naming;
//import java.rmi.NotBoundException;
//import java.rmi.RemoteException;
//
//import com.openexchange.admin.console.AdminParser;
//import com.openexchange.admin.console.CmdLineParser.Option;
//import com.openexchange.admin.rmi.OXUtilInterface;
//import com.openexchange.admin.rmi.dataobjects.Credentials;
//import com.openexchange.admin.rmi.dataobjects.Database;
//import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
//import com.openexchange.admin.rmi.exceptions.InvalidDataException;
//import com.openexchange.admin.rmi.exceptions.StorageException;
//
///**
// * 
// * @author d7,cutmasta
// *
// */
//public class RegisterDatabase extends UtilAbstraction {
//
//    
//    
//    public RegisterDatabase(final String[] args2) {
//
//        AdminParser parser = new AdminParser("registerdatabase");
//
//        setOptions(parser);
//
//        try {
//            parser.ownparse(args2);
//
//            final Credentials auth = new Credentials((String)parser.getOptionValue(adminUserOption),(String)parser.getOptionValue(adminPassOption));
//            
//            // get rmi ref
//            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(OXUtilInterface.RMI_NAME);
//
//            final Database db = new Database();
//            final String hostname = verifySetAndGetOption(cmd, OPT_NAME_HOSTNAME_SHORT);
//            final String name = cmd.getOptionValue(OPT_NAME_DBNAME_SHORT);
//            final String driver = verifySetAndGetOption(cmd, OPT_NAME_DB_DRIVER_SHORT);
//            final String username = verifySetAndGetOption(cmd, OPT_NAME_DB_USERNAME_SHORT);
//            final String password = cmd.getOptionValue(OPT_NAME_DB_PASSWD_SHORT);
//            boolean ismaster = false;
//            String masterid = null;
//            final String maxunits = verifySetAndGetOption(cmd, OPT_NAME_MAX_UNITS_SHORT);
//            final String pool_hard_limit = verifySetAndGetOption(cmd, OPT_NAME_POOL_HARDLIMIT_SHORT);
//            final String pool_initial = verifySetAndGetOption(cmd, OPT_NAME_POOL_INITIAL_SHORT);
//            final String pool_max = verifySetAndGetOption(cmd, OPT_NAME_POOL_MAX_SHORT);
//            final String cluster_weight = verifySetAndGetOption(cmd, OPT_NAME_WEIGHT_SHORT);
//            // add optional values if set
//            
//            if (cmd.hasOption(OPT_NAME_IS_MASTER_SHORT)) {
//                ismaster = true;
//            }
//            if (false == ismaster) {
//                if (cmd.hasOption(OPT_NAME_MASTER_ID_SHORT)) {
//                    masterid = cmd.getOptionValue(OPT_NAME_MASTER_ID_SHORT);
//                } else {
//                    throw new MissingArgumentException("master id must be set if this database isn't the master");
//                }
//            }
//            
//            // Setting the options in the dataobject
//            db.setDisplayname(name);
//            db.setDriver(testStringAndGetStringOrDefault(driver, DRIVER_DEFAULT));
//            db.setLogin(testStringAndGetStringOrDefault(username, USER_DEFAULT));
//            db.setPassword(password);
//            db.setMaster(ismaster);
//            db.setMaxUnits(testStringAndGetIntOrDefault(maxunits, MAXUNITS_DEFAULT));
//            db.setPoolHardLimit(testStringAndGetIntOrDefault(pool_hard_limit, POOL_HARD_LIMIT_DEFAULT));
//            db.setPoolInitial(testStringAndGetIntOrDefault(pool_initial, POOL_INITIAL_DEFAULT));
//            db.setPoolMax(testStringAndGetIntOrDefault(pool_max, POOL_MAX_DEFAULT));
//            
//            if (null != hostname) {
//                db.setUrl("jdbc:mysql://"+hostname+"/?useUnicode=true&characterEncoding=UTF-8&" +
//                        "autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&" +
//                        "serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
//            } else {
//                db.setUrl("jdbc:mysql://"+HOSTNAME_DEFAULT+"/?useUnicode=true&characterEncoding=UTF-8&" +
//                        "autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&" +
//                        "serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
//            }
//            db.setClusterWeight(testStringAndGetIntOrDefault(cluster_weight, CLUSTER_WEIGHT_DEFAULT));
//            if (null != masterid) {
//                db.setMasterId(Integer.parseInt(masterid));                
//            }
//            
//            System.out.println(oxutil.registerDatabase(db, auth));
//        } catch (final java.rmi.ConnectException neti) {
//            printError(neti.getMessage());
//        } catch (final java.lang.NumberFormatException num) {
//            printInvalidInputMsg("Ids must be numbers!");        
//        } catch (final MalformedURLException e) {
//            printServerResponse(e.getMessage());
//        } catch (final RemoteException e) {
//            printServerResponse(e.getMessage());
//        } catch (final NotBoundException e) {
//            printNotBoundResponse(e);
//        } catch (final StorageException e) {
//            printServerResponse(e.getMessage());
//        } catch (final InvalidCredentialsException e) {
//            printServerResponse(e.getMessage());
//        } catch (final InvalidDataException e) {
//            printServerResponse(e.getMessage());
//        }
//
//    }
//
//    public static void main(final String args[]) {
//        new RegisterDatabase(args);
//    }
//
//    private void setOptions(AdminParser parser) {
//        setDefaultCommandLineOptions(parser);
//
//        databaseNameOption = setShortLongOpt(parser, OPT_NAME_DBNAME_SHORT,OPT_NAME_DBNAME_LONG,"name of the database",true, true); 
//        
//        //FIXME ; USE -->  default HOSTNAME_DEFAULT on top after parsing
//        hostnameOption =  setShortLongOpt(parser, OPT_NAME_HOSTNAME_SHORT,OPT_NAME_HOSTNAME_LONG,"hostname of the server",true, false); 
//        // FIXME : USE -> USER_DEFAULT
//        databaseUsernameOption = setShortLongOpt(parser, OPT_NAME_DB_USERNAME_SHORT,OPT_NAME_DB_USERNAME_LONG,"name of the user for the database",true, false);
//        // FIXME : USE -> DRIVER_DEFAULT
//        databaseDriverOption = setShortLongOpt(parser, OPT_NAME_DB_DRIVER_SHORT,OPT_NAME_DB_DRIVER_LONG,"the driver to be used for the database",true, false);
//        
//        
//        databasePasswdOption = setShortLongOpt(parser, OPT_NAME_DB_PASSWD_SHORT,OPT_NAME_DB_PASSWD_LONG,"password for the database",true, true);
//        
//        databaseIsMasterOption = setShortLongOpt(parser, OPT_NAME_IS_MASTER_SHORT,OPT_NAME_IS_MASTER_LONG,"set this if the registered database is the master",false,false);
//        
//        databaseMasterIDOption = setShortLongOpt(parser, OPT_NAME_MASTER_ID_SHORT,OPT_NAME_MASTER_ID_LONG,"if this database isn't the master give the id of the master here",true,false);
//        
//        // FIXME : USE --> String.valueOf(CLUSTER_WEIGHT_DEFAULT)
//        databaseWeightOption = setShortLongOpt(parser, OPT_NAME_WEIGHT_SHORT,OPT_NAME_WEIGHT_LONG,"the db weight for this database",true,false);
//        
//        // FIXME : USE -> String.valueOf(MAXUNITS_DEFAULT)
//        maxUnitsOption = setShortLongOpt(parser, OPT_NAME_MAX_UNITS_SHORT,OPT_NAME_MAX_UNITS_LONG,"the maximum number of units in this database",true,false);
//        
//        databaseParameterOption = setShortLongOpt(parser, OPT_NAME_DBPARAM_SHORT,OPT_NAME_DBPARAM_LONG,"parameter for the database",true,false);
//        
//        // FIXME: choeger Enter right description here
//        // FIXME : USE String.valueOf(POOL_HARD_LIMIT_DEFAULT)
//        poolHardlimitOption = setShortLongOpt(parser, OPT_NAME_POOL_HARDLIMIT_SHORT,OPT_NAME_POOL_HARDLIMIT_LONG,"db pool hardlimit",true,false);
//        // FIXME: String.valueOf(POOL_INITIAL_DEFAULT)
//        poolInitialOption = setShortLongOpt(parser, OPT_NAME_POOL_INITIAL_SHORT,OPT_NAME_POOL_INITIAL_LONG,"db pool initial",true,false);
//        // FIXME : String.valueOf(POOL_MAX_DEFAULT)
//        poolMaxOption = setShortLongOpt(parser, OPT_NAME_POOL_MAX_SHORT,OPT_NAME_POOL_MAX_LONG,"db pool max",true,false);
//        
//    }
//    
//    private Option databaseNameOption = null;
//    private Option hostnameOption = null;
//    private Option databaseUsernameOption = null;
//    private Option databaseDriverOption = null;
//    private Option databasePasswdOption = null;
//    private Option databaseIsMasterOption = null;
//    private Option databaseMasterIDOption = null;
//    private Option databaseWeightOption = null;
//    
//    private Option maxUnitsOption = null;
//    private Option databaseParameterOption = null;
//    private Option poolHardlimitOption = null;
//    private Option poolInitialOption = null;
//    private Option poolMaxOption = null;
//
//}
