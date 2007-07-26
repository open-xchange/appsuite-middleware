package com.openexchange.admin.console.util.server;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
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
public class ListServers extends ServerAbstraction {

    public ListServers(final String[] args2) {

        final AdminParser parser = new AdminParser("listservers");

        setOptions(parser);
        setCSVOutputOption(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);

            String searchpattern = "*";
            if (parser.getOptionValue(this.searchOption) != null) {
                searchpattern = (String) parser.getOptionValue(this.searchOption);
            }
            // Setting the options in the dataobject
            final Server[] servers = oxutil.listServers(searchpattern, auth);

            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(servers);
            } else {
                sysoutOutput(servers);
            }

            sysexit(0);
        } catch (final ConnectException neti) {
            printError(null, null, neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final NumberFormatException num) {
            printInvalidInputMsg(null, null, "Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerException(null, null, e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(null, null, e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printNotBoundResponse(null, null, e);
            sysexit(1);
        } catch (final StorageException e) {
            printServerException(null, null, e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerException(null, null, e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final InvalidDataException e) {
            printServerException(null, null, e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final IllegalOptionValueException e) {
            printError(null, null, "Illegal option value : " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final UnknownOptionException e) {
            printError(null, null, "Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (final MissingOptionException e) {
            printError(null, null, e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        }
    }

    public static void main(final String args[]) {
        new ListServers(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setSearchOption(parser);
    }

    private void sysoutOutput(Server[] servers) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final Server server : servers) {
            data.add(makeCSVData(server));
        }
        
        doOutput(new String[] { "3r", "35l" }, new String[] { "Id", "Name" }, data);
    }

    private void precsvinfos(Server[] servers) {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("name");
    
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
    
        for (final Server server : servers) {
            data.add(makeCSVData(server));
        }
    
        doCSVOutput(columns, data);
    }

    private ArrayList<String> makeCSVData(Server server) {
        final ArrayList<String> srv_data = new ArrayList<String>();
        srv_data.add(String.valueOf(server.getId()));
        final String servername = server.getName();
        if (servername != null) {
            srv_data.add(servername);
        } else {
            srv_data.add(null);
        }
        return srv_data;
    }

    @Override
    protected final String getObjectName() {
        return "servers";
    }

}
