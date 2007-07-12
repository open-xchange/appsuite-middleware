package com.openexchange.admin.console.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
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
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * 
 * @author d7,cutmasta
 * 
 */
public class RegisterFilestore extends UtilAbstraction {

    private Option filestorePathOption = null;

    private Option filestoreSizeOption = null;

    private Option filestoreMaxContextsOption = null;

    public RegisterFilestore(final String[] args2) {
    
        final AdminParser parser = new AdminParser("registerFilestore");
    
        setOptions(parser);
    
        try {
            parser.ownparse(args2);
    
            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));
    
            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);
    
            final Filestore fstore = new Filestore();
    
            final String store_path = (String) parser.getOptionValue(this.filestorePathOption);
    
            String store_size = String.valueOf(STORE_SIZE_DEFAULT);
            if (parser.getOptionValue(this.filestoreSizeOption) != null) {
                store_size = (String) parser.getOptionValue(this.filestoreSizeOption);
            }
            String store_max_ctx = String.valueOf(STORE_MAX_CTX_DEFAULT);
            if (parser.getOptionValue(this.filestoreMaxContextsOption) != null) {
                store_max_ctx = (String) parser.getOptionValue(this.filestoreMaxContextsOption);
            }
            // add optional values if set
    
            // Setting the options in the dataobject
            final java.net.URI uri = new java.net.URI(store_path);
            fstore.setUrl(uri.toString());
            new java.io.File(uri.getPath()).mkdir();
    
            if (null != store_size) {
                fstore.setSize(Long.parseLong(store_size));
            } else {
                fstore.setSize(STORE_SIZE_DEFAULT);
            }
            fstore.setMaxContexts(testStringAndGetIntOrDefault(store_max_ctx, STORE_MAX_CTX_DEFAULT));
    
            System.out.println(oxutil.registerFilestore(fstore, auth).getId());
            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerException(e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printNotBoundResponse(e);
            sysexit(1);
        } catch (final StorageException e) {
            printServerException(e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final InvalidDataException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final URISyntaxException e) {
            printServerException(e);
            sysexit(1);
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
        new RegisterFilestore(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);

        this.filestorePathOption = setShortLongOpt(parser, OPT_NAME_STORE_PATH_SHORT, OPT_NAME_STORE_PATH_LONG, "Path to store filestore contents", true, true);

        this.filestoreSizeOption = setShortLongOpt(parser, OPT_NAME_STORE_SIZE_SHORT, OPT_NAME_STORE_SIZE_LONG, "The maximum size of the filestore", true, false);

        this.filestoreMaxContextsOption = setShortLongOpt(parser, OPT_NAME_STORE_MAX_CTX_SHORT, OPT_NAME_STORE_MAX_CTX_LONG, "the maximum number of contexts", true, false);

    }
}
