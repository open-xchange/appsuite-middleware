package com.openexchange.admin.console.util.server;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.AdminParser.NeededTriState;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * 
 * @author d7,cutmasta
 * 
 */
public class UnregisterServer extends ServerAbstraction {

    // Setting names for options
    private final static char OPT_NAME_SERVER_ID_SHORT = 'i';

    private final static String OPT_NAME_SERVER_ID_LONG = "id";

    private Option serverIdOption = null;

    public UnregisterServer(final String[] args2) {

        AdminParser parser = new AdminParser("unregisterserver");

        setOptions(parser);

        Integer serverid = null;
        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);
            Server sv = new Server();
            serverid = Integer.parseInt((String) parser.getOptionValue(serverIdOption));
            sv.setId(serverid);
            oxutil.unregisterServer(sv, auth);
            
            displayUnregisteredMessage(sv.getId());
            sysexit(0);
        } catch (final ConnectException neti) {
            printError(serverid, null, neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final NumberFormatException num) {
            printInvalidInputMsg(serverid, null, "Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerException(serverid, null, e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(serverid, null, e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printNotBoundResponse(serverid, null, e);
            sysexit(1);
        } catch (final StorageException e) {
            printServerException(serverid, null, e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerException(serverid, null, e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final InvalidDataException e) {
            printServerException(serverid, null, e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (IllegalOptionValueException e) {
            printError(serverid, null, "Illegal option value : " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (UnknownOptionException e) {
            printError(serverid, null, "Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (MissingOptionException e) {
            printError(serverid, null, e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        }
    }

    public static void main(final String args[]) {
        new UnregisterServer(args);
    }

    private void setOptions(AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        serverIdOption = setShortLongOpt(parser, OPT_NAME_SERVER_ID_SHORT, OPT_NAME_SERVER_ID_LONG, "The id of the server which should be deleted", true, NeededTriState.needed);
    }
}
