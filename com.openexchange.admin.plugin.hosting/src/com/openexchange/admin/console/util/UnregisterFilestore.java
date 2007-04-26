package com.openexchange.admin.console.util;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * 
 * @author d7,cutmasta
 * 
 */
public class UnregisterFilestore extends UtilAbstraction {

    private Option filestoreIdOption = null;

    // Setting names for options
    public UnregisterFilestore(final String[] args2) {
    
        final AdminParser parser = new AdminParser("unregisterFilestore");
    
        setOptions(parser);
    
        try {
            parser.ownparse(args2);
    
            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));
    
            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(OXUtilInterface.RMI_NAME);
    
            oxutil.unregisterFilestore(Integer.parseInt((String) parser.getOptionValue(this.filestoreIdOption)), auth);
            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
            sysexit(1);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        } catch (final RemoteException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        } catch (final NotBoundException e) {
            printNotBoundResponse(e);
            sysexit(1);
        } catch (final StorageException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        } catch (final InvalidDataException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        } catch (final IllegalOptionValueException e) {
            printError("Illegal option value : " + e.getMessage());
            parser.printUsage();
            sysexit(1);
        } catch (final UnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            sysexit(1);
        } catch (final MissingOptionException e) {
            printError(e.getMessage());
            parser.printUsage();
            sysexit(1);
        }
    
    }

    public static void main(final String args[]) {
        new UnregisterFilestore(args);
    }

    private void setOptions(final AdminParser parser) {

        setDefaultCommandLineOptions(parser);

        this.filestoreIdOption = setShortLongOpt(parser, OPT_NAME_STORE_FILESTORE_ID_SHORT, OPT_NAME_STORE_FILESTORE_ID_LONG, "The id of the filestore which should be deleted", true, true);

    }

    protected void sysexit(final int exitcode) {
        System.exit(exitcode);
    }
}
