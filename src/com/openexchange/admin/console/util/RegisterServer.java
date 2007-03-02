package com.openexchange.admin.console.util;

import java.net.MalformedURLException;
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
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class RegisterServer extends UtilAbstraction {

    private final static String GENERAL_UTILITY_NAME = "registerServer";

    // Setting names for options
    private final static String OPT_NAME_HOSTNAME_SHORT = "h";
    private final static String OPT_NAME_HOSTNAME_LONG = "hostname";

    public RegisterServer(final String[] args2) {

        final CommandLineParser parser = new PosixParser();

        final Options options = getOptions();

        try {
            final CommandLine cmd = parser.parse(options, args2);

            final Credentials auth = new Credentials(cmd.getOptionValue(OPT_NAME_ADMINUSER_SHORT), cmd.getOptionValue(OPT_NAME_ADMINPASS_SHORT));

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(OXUtilInterface.RMI_NAME);

            final Server srv = new Server();
            final String hostname = cmd.getOptionValue(OPT_NAME_HOSTNAME_SHORT);
            
            // Setting the options in the dataobject
            if (null != hostname) {
                srv.setName(hostname);
            }

            oxutil.registerServer(srv, auth);
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
            printServerResponse(e.getMessage());
        } catch (final StorageException e) {
            printServerResponse(e.getMessage());
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e.getMessage());
        } catch (final InvalidDataException e) {
            printServerResponse(e.getMessage());
        }

    }

    public static void main(final String args[]) {
        new RegisterServer(args);
    }

    private Options getOptions() {
        final Options retval = getDefaultCommandLineOptions();

        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_HOSTNAME_SHORT, OPT_NAME_HOSTNAME_LONG, "the hostname of the server", true, true)));
        return retval;
    }


}
