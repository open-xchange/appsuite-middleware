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

        final AdminParser parser = new AdminParser("listServers");

        setOptions(parser);
        setCSVOutputOption(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);

            String searchpattern = "*";
            if (parser.getOptionValue(this.searchOption) != null) {
                searchpattern = (String) parser.getOptionValue(this.searchOption);
            }
            // Setting the options in the dataobject
            final Server[] servers = oxutil.searchForServer(searchpattern, auth);

            // needed for csv output, KEEP AN EYE ON ORDER!!!
            final ArrayList<String> columns = new ArrayList<String>();
            columns.add("id");
            columns.add("name");

            final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

            final String HEADER_FORMAT = "%-7s %-35s\n";
            final String VALUE_FORMAT  = "%-7s %-35s\n";
            if(parser.getOptionValue(this.csvOutputOption) == null) {
                System.out.format(HEADER_FORMAT, "id", "name");
            }
            for (final Server server : servers) {
                if (parser.getOptionValue(this.csvOutputOption) != null) {
                    final ArrayList<String> srv_data = new ArrayList<String>();
                    srv_data.add(String.valueOf(server.getId()));
                    if (server.getName() != null) {
                        srv_data.add(server.getName());
                    } else {
                        srv_data.add(null);
                    }
                    data.add(srv_data);
                } else {
                    System.out.format(VALUE_FORMAT,server.getId(),server.getName());
                }
            }

            if (parser.getOptionValue(this.csvOutputOption) != null) {
                doCSVOutput(columns, data);
            }

            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerResponse(e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printNotBoundResponse(e);
            sysexit(1);
        } catch (final StorageException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final InvalidDataException e) {
            printServerResponse(e);
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
        new ListServer(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        setSearchOption(parser);
    }
}
