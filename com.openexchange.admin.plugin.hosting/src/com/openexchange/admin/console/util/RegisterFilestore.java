package com.openexchange.admin.console.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * 
 * @author d7
 *
 */
public class RegisterFilestore extends UtilAbstraction {

    private final static String GENERAL_UTILITY_NAME = "registerFilestore";

    public RegisterFilestore(final String[] args2) {

        final CommandLineParser parser = new PosixParser();

        final Options options = getOptions();

        try {
            final CommandLine cmd = parser.parse(options, args2);

            final Credentials auth = new Credentials(cmd.getOptionValue(OPT_NAME_ADMINUSER_SHORT), cmd.getOptionValue(OPT_NAME_ADMINPASS_SHORT));

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(OXUtilInterface.RMI_NAME);

            final Filestore fstore = new Filestore();
            final String store_path = cmd.getOptionValue(OPT_NAME_STORE_PATH_SHORT);
            final String store_size = verifySetAndGetOption(cmd, OPT_NAME_STORE_SIZE_SHORT);
            final String store_max_ctx = verifySetAndGetOption(cmd, OPT_NAME_STORE_MAX_CTX_SHORT);
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

            System.out.println(oxutil.registerFilestore(fstore, auth));
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
        } catch (final org.apache.commons.cli.MissingArgumentException as) {
            printError("Missing arguments on the command line: " + as.getMessage());
            printHelpText(GENERAL_UTILITY_NAME, options);
        } catch (final org.apache.commons.cli.UnrecognizedOptionException ux) {
            printError("Unrecognized options on the command line: " + ux.getMessage());
            printHelpText(GENERAL_UTILITY_NAME, options);
        } catch (final org.apache.commons.cli.MissingOptionException mis) {
            printError("Missing options on the command line: " + mis.getMessage());
            printHelpText(GENERAL_UTILITY_NAME, options);
        } catch (final ParseException e) {
            e.printStackTrace();
        } catch (final MalformedURLException e) {
            printServerResponse(e.getMessage());
        } catch (final RemoteException e) {
            printServerResponse(e.getMessage());
        } catch (final NotBoundException e) {
            printNotBoundResponse(e);
        } catch (final StorageException e) {
            printServerResponse(e.getMessage());
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e.getMessage());
        } catch (final InvalidDataException e) {
            printServerResponse(e.getMessage());
        } catch (final URISyntaxException e) {
            printServerResponse(e.getMessage());
        }

    }

    public static void main(final String args[]) {
        new RegisterFilestore(args);
    }

    private Options getOptions() {
        final Options retval = getDefaultCommandLineOptions();

        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_STORE_PATH_SHORT, OPT_NAME_STORE_PATH_LONG, "where to store filestore contents", true, true)));
        retval.addOption(addDefaultArgName(getShortLongOptWithDefault(OPT_NAME_STORE_SIZE_SHORT, OPT_NAME_STORE_SIZE_LONG, "the maximum size of the filestore", String.valueOf(STORE_SIZE_DEFAULT), true, false)));
        retval.addOption(addDefaultArgName(getShortLongOptWithDefault(OPT_NAME_STORE_MAX_CTX_SHORT, OPT_NAME_STORE_MAX_CTX_LONG, "the maximum number of contexts", String.valueOf(STORE_MAX_CTX_DEFAULT), true, false)));
        return retval;
    }

}
