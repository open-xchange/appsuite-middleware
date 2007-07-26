package com.openexchange.admin.console.util.filestore;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.AdminParser.NeededTriState;
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
public class RegisterFilestore extends FilestoreAbstraction {

    public RegisterFilestore(final String[] args2) {
        final AdminParser parser = new AdminParser("registerfilestore");
    
        setOptions(parser);
    
        try {
            parser.ownparse(args2);
    
            final Credentials auth = credentialsparsing(parser);
    
            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);
    
            final Filestore fstore = new Filestore();
    
            parseAndSetFilestorePath(parser, fstore);
            
            parseAndSetFilestoreSize(parser, fstore);
            
            parseAndSetFilestoreMaxCtxs(parser, fstore);
    
            displayRegisteredMessage(oxutil.registerFilestore(fstore, auth).getId());
            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(null, null, neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
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
        } catch (final URISyntaxException e) {
            printServerException(null, null, e);
            sysexit(1);
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
        new RegisterFilestore(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setPathOption(parser, NeededTriState.needed);

        setSizeOption(parser, String.valueOf(OXUtilInterface.DEFAULT_STORE_SIZE));

        setMaxCtxOption(parser, String.valueOf(OXUtilInterface.DEFAULT_STORE_MAX_CTX));
    }
}
