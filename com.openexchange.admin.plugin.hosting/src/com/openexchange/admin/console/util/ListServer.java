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
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * 
 * @author d7
 *
 */
public class ListServer extends UtilAbstraction {

    
    public ListServer(final String[] args2) {

        AdminParser parser = new AdminParser("listServers");

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
            // Setting the options in the dataobject
            final Server[] servers = oxutil.searchForServer(searchpattern, auth);
            
//          needed for csv output, KEEP AN EYE ON ORDER!!!
            ArrayList<String> columns = new ArrayList<String>();
            columns.add("id");
            columns.add("name");
            
            ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
            
            for (final Server server : servers) {
                if (parser.getOptionValue(csvOutputOption) != null) {
                    ArrayList<String> srv_data = new ArrayList<String>();
                    srv_data.add(String.valueOf(server.getId()));
                    if(server.getName()!=null){
                        srv_data.add(server.getName());
                    }else{
                        srv_data.add(null);
                    }
                    data.add(srv_data);
                }else{
                    System.out.println(server);
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
        new ListServer(args);
    }

    private void setOptions(AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        setSearchOption(parser);
    }

}
