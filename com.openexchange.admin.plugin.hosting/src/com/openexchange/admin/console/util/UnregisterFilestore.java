package com.openexchange.admin.console.util;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * 
 * @author d7,cutmasta
 * 
 */
public class UnregisterFilestore extends FilestoreAbstraction {

    // Setting names for options
    public UnregisterFilestore(final String[] args2) {
    
        final AdminParser parser = new AdminParser("unregisterfilestore");
    
        setOptions(parser);
    
        try {
            parser.ownparse(args2);
    
            final Credentials auth = credentialsparsing(parser);
    
            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);
            final Filestore fstore = new Filestore();
            parseAndSetFilestoreID(parser, fstore);

            oxutil.unregisterFilestore(fstore, auth);
            
            displayUnregisteredMessage(filestoreid);
            sysexit(0);
        } catch (final ConnectException neti) {
            printError(filestoreid, null, neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg(filestoreid, null, "Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerException(filestoreid, null, e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(filestoreid, null, e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printNotBoundResponse(filestoreid, null, e);
            sysexit(1);
        } catch (final StorageException e) {
            printServerException(filestoreid, null, e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerException(filestoreid, null, e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final InvalidDataException e) {
            printServerException(filestoreid, null, e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final IllegalOptionValueException e) {
            printError(filestoreid, null, "Illegal option value : " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final UnknownOptionException e) {
            printError(filestoreid, null, "Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (final MissingOptionException e) {
            printError(filestoreid, null, e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        }
    
    }

    public static void main(final String args[]) {
        new UnregisterFilestore(args);
    }

    private void setOptions(final AdminParser parser) {

        setDefaultCommandLineOptionsWithoutContextID(parser);

        setFilestoreIDOption(parser);

    }
}
