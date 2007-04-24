package com.openexchange.admin.console.util;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

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
public class ListDatabase extends UtilAbstraction {

    

    public ListDatabase(final String[] args2) {

        AdminParser parser = new AdminParser("listDatabases");

        setOptions(parser);
        setCSVOutputOption(parser);
        try {
            parser.ownparse(args2);

            final Credentials auth = new Credentials((String)parser.getOptionValue(adminUserOption),(String)parser.getOptionValue(adminPassOption));
            
            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(OXUtilInterface.RMI_NAME);
            
            String searchpattern = "*";
            if(parser.getOptionValue(searchOption)!=null){
                searchpattern = (String)parser.getOptionValue(searchOption);
            }
            final Database[] databases = oxutil.searchForDatabase(searchpattern, auth);
            
            // needed for csv output, KEEP AN EYE ON ORDER!!!
            ArrayList<String> columns = new ArrayList<String>();
            columns.add("id");
            columns.add("url");
            columns.add("display_name");
            columns.add("login");
            columns.add("password");
            columns.add("driver");
            columns.add("clusterweight");
            columns.add("maxUnits");
            columns.add("poolHardLimit");
            columns.add("poolInitial");
            columns.add("poolMax");
            columns.add("masterid");
            columns.add("currentunits");
            columns.add("master");
            columns.add("read_id");
            columns.add("scheme");
            
            ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
            
            for (final Database database : databases) {
                if (parser.getOptionValue(csvOutputOption) != null) {                   
                    data.add(makeCSVData(database));
                }else{
                    System.out.println(database);
                }
            }
            
            if (parser.getOptionValue(csvOutputOption) != null) {
                doCSVOutput(columns, data);
            }
            
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
        new ListDatabase(args);
    }

    private void setOptions(AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        setSearchOption(parser);       
    }
    /**     
     * @param db
     * @return
     */
    private ArrayList<String> makeCSVData(Database db){
        ArrayList<String> rea_data = new ArrayList<String>();
        
        rea_data.add(db.getId().toString());
        
        if(db.getUrl()!=null){
            rea_data.add(db.getUrl());
        }else{
            rea_data.add(null);
        }
        
        if(db.getDisplayname()!=null){
            rea_data.add(db.getDisplayname());
        }else{
            rea_data.add(null);            
        }
        
        if(db.getLogin()!=null){
            rea_data.add(db.getLogin());
        }else{
            rea_data.add(null);            
        }
        
        if(db.getPassword()!=null){
            rea_data.add(db.getPassword());
        }else{
            rea_data.add(null);            
        }
        
        if(db.getDriver()!=null){
            rea_data.add(db.getDriver());
        }else{
            rea_data.add(null);            
        }
        
        if(db.getClusterWeight()!=null){
            rea_data.add(db.getClusterWeight().toString());
        }else{
            rea_data.add(null);            
        }
        
        if(db.getMaxUnits()!=null){
            rea_data.add(db.getMaxUnits().toString());
        }else{
            rea_data.add(null);            
        }
        
        if(db.getPoolHardLimit()!=null){
            rea_data.add(db.getPoolHardLimit().toString());
        }else{
            rea_data.add(null);            
        }
        
        if(db.getPoolInitial()!=null){
            rea_data.add(db.getPoolInitial().toString());
        }else{
            rea_data.add(null);            
        }
        
        if(db.getPoolMax()!=null){
            rea_data.add(db.getPoolMax().toString());
        }else{
            rea_data.add(null);            
        }
        
        if(db.getMasterId()!=null){
            rea_data.add(db.getMasterId().toString());
        }else{
            rea_data.add(null);            
        }
        
        if(db.getCurrentUnits()!=null){
            rea_data.add(db.getCurrentUnits().toString());
        }else{
            rea_data.add(null); 
        }
        
        if(db.isMaster()!=null){
            rea_data.add(db.isMaster().toString());
        }else{
            rea_data.add(null); 
        }
        
        if(db.getRead_id()!=null){
            rea_data.add(db.getRead_id().toString());
        }else{
            rea_data.add(null); 
        }
        
        if(db.getScheme()!=null){
            rea_data.add(db.getScheme().toString());
        }else{
            rea_data.add(null); 
        }
        
        
        return rea_data;
    }
}
