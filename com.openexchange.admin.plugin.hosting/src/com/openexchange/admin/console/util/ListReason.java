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
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * 
 * @author d7,cutmasta
 *
 */
public class ListReason extends UtilAbstraction {
    

    public ListReason(final String[] args2) {

        AdminParser parser = new AdminParser("listReasons");
        setOptions(parser);
        setCSVOutputOption(parser);
        try {
            parser.ownparse( args2);

            final Credentials auth = new Credentials((String)parser.getOptionValue(adminUserOption),(String)parser.getOptionValue(adminPassOption));
            
            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(OXUtilInterface.RMI_NAME);
            

            final MaintenanceReason[] mrs = oxutil.getAllMaintenanceReasons(auth);
            
            // needed for csv output, KEEP AN EYE ON ORDER!!!
            ArrayList<String> columns = new ArrayList<String>();
            columns.add("id");
            columns.add("text");
            
            
            // Needed for csv output
            ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
            
            for (final MaintenanceReason mr : mrs) {
                if (parser.getOptionValue(csvOutputOption) != null) {
                    ArrayList<String> rea_data = new ArrayList<String>();                    
                    rea_data.add(mr.getId().toString());
                    rea_data.add(mr.getText());
                    data.add(rea_data);
                }else{
                    System.out.println(mr);
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
        new ListReason(args);
    }

    private void setOptions(AdminParser parser) {
        setDefaultCommandLineOptions(parser);        
        
    }
    
    

}
