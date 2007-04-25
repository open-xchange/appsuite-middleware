package com.openexchange.admin.console.util;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
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
public class RegisterDatabase extends UtilAbstraction {
    
    public RegisterDatabase(final String[] args2) {

        AdminParser parser = new AdminParser("registerdatabase");

        setOptions(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = new Credentials((String)parser.getOptionValue(adminUserOption),(String)parser.getOptionValue(adminPassOption));
            
            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(OXUtilInterface.RMI_NAME);

            final Database db = new Database();
            
            String hostname = HOSTNAME_DEFAULT;
            if(parser.getOptionValue(hostnameOption)!=null){
                hostname = (String)parser.getOptionValue(hostnameOption);
            }
            
            String db_dispname = (String)parser.getOptionValue(databaseNameOption);
            
            String driver = DRIVER_DEFAULT;
            if(parser.getOptionValue(databaseDriverOption)!=null){
                driver = (String)parser.getOptionValue(databaseDriverOption);
            }            
            
            String username =  USER_DEFAULT;
            if(parser.getOptionValue(databaseUsernameOption)!=null){
                username = (String)parser.getOptionValue(databaseUsernameOption);
            }
            
            String password = (String)parser.getOptionValue(databasePasswdOption);
            
            
           
            String maxunits = String.valueOf(MAXUNITS_DEFAULT);
            if(parser.getOptionValue(maxUnitsOption)!=null){
                maxunits = (String)parser.getOptionValue(maxUnitsOption);
            }
            
            
            String pool_hard_limit  = String.valueOf(POOL_HARD_LIMIT_DEFAULT);
            if(parser.getOptionValue(poolHardlimitOption)!=null){
                pool_hard_limit = (String)parser.getOptionValue(poolHardlimitOption);
            }
            
            String pool_initial = String.valueOf(POOL_INITIAL_DEFAULT);
            if(parser.getOptionValue(poolInitialOption)!=null){
                pool_initial = (String)parser.getOptionValue(poolInitialOption);
            }
            
            boolean ismaster = false;
            String masterid = null;
            
           String pool_max = String.valueOf(POOL_MAX_DEFAULT);
            if(parser.getOptionValue(poolMaxOption)!=null){
                pool_max = (String)parser.getOptionValue(poolMaxOption);
            }
            
            String cluster_weight = String.valueOf(CLUSTER_WEIGHT_DEFAULT);
            if(parser.getOptionValue(databaseWeightOption)!=null){
                cluster_weight = (String)parser.getOptionValue(databaseWeightOption);
            }
            
            if (parser.getOptionValue(databaseIsMasterOption)!=null) {
                ismaster = true;
            }
            if (false == ismaster) {
                if (parser.getOptionValue(databaseMasterIDOption)!=null) {
                    masterid = (String)parser.getOptionValue(databaseMasterIDOption);
                } else {
                    printError(" master id must be set if this database isn't the master");
                    parser.printUsage(); 
                    System.exit(1);
                }
            }
            
            // Setting the options in the dataobject
            db.setDisplayname(db_dispname);
            db.setDriver(testStringAndGetStringOrDefault(driver, DRIVER_DEFAULT));
            db.setLogin(testStringAndGetStringOrDefault(username, USER_DEFAULT));
            db.setPassword(password);
            db.setMaster(ismaster);
            db.setMaxUnits(testStringAndGetIntOrDefault(maxunits, MAXUNITS_DEFAULT));
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
            
            System.out.println(oxutil.registerDatabase(db, auth));
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
            System.exit(1);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            System.exit(1);
        } catch (final MalformedURLException e) {
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final RemoteException e) {
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final NotBoundException e) {
            printNotBoundResponse(e);
            System.exit(1);
        } catch (final StorageException e) {
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final InvalidDataException e) {
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (IllegalOptionValueException e) {            
            printError("Illegal option value : " + e.getMessage());
            parser.printUsage();
            System.exit(1);
        } catch (UnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            System.exit(1);
        } catch (MissingOptionException e) {
            printError(e.getMessage());            
            parser.printUsage();
            System.exit(1);
        }

    }

    public static void main(final String args[]) {
        new RegisterDatabase(args);
    }

    private void setOptions(AdminParser parser) {
        setDefaultCommandLineOptions(parser);

        setDatabaseNameOption(parser,true);
        setDatabaseHostnameOption(parser,false);
        setDatabaseUsernameOption(parser,false);
        setDatabaseDriverOption(parser,false);
        setDatabasePasswdOption(parser,true);
        setDatabaseIsMasterOption(parser,false);
        setDatabaseMasterIDOption(parser,false);
        setDatabaseWeightOption(parser,false);
        setDatabaseMaxUnitsOption(parser,false);
        setDatabasePoolHardlimitOption(parser,false);
        setDatabasePoolInitialOption(parser,false);
        setDatabasePoolMaxOption(parser,false);
        
    }
    
    

}
