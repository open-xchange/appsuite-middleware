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

    public RegisterServer(String[] args2) {

        CommandLineParser parser = new PosixParser();

        Options options = getOptions();

        try {
            CommandLine cmd = parser.parse(options, args2);

            Credentials auth = new Credentials(cmd.getOptionValue(OPT_NAME_ADMINUSER_SHORT), cmd.getOptionValue(OPT_NAME_ADMINPASS_SHORT));

            // get rmi ref
            OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(OXUtilInterface.RMI_NAME);

            final Server srv = new Server();
            String hostname = null;
            
            hostname = cmd.getOptionValue(OPT_NAME_HOSTNAME_SHORT);

            // Setting the options in the dataobject
            if (null != hostname) {
                srv.setName(hostname);
            }

            oxutil.registerServer(srv, auth);
        } catch (java.rmi.ConnectException neti) {
            printError(neti.getMessage());
        } catch (java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
        } catch (org.apache.commons.cli.MissingArgumentException as) {
            printError("Missing arguments on the command line: " + as.getMessage());
            printHelpText(GENERAL_UTILITY_NAME, options);
        } catch (org.apache.commons.cli.UnrecognizedOptionException ux) {
            printError("Unrecognized options on the command line: " + ux.getMessage());
            printHelpText(GENERAL_UTILITY_NAME, options);
        } catch (org.apache.commons.cli.MissingOptionException mis) {
            printError("Missing options on the command line: " + mis.getMessage());
            printHelpText(GENERAL_UTILITY_NAME, options);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            printServerResponse(e.getMessage());
        } catch (RemoteException e) {
            printServerResponse(e.getMessage());
        } catch (NotBoundException e) {
            printServerResponse(e.getMessage());
        } catch (StorageException e) {
            printServerResponse(e.getMessage());
        } catch (InvalidCredentialsException e) {
            printServerResponse(e.getMessage());
        } catch (InvalidDataException e) {
            printServerResponse(e.getMessage());
        }

    }

    public static void main(String args[]) {
        new RegisterServer(args);
    }

    private Options getOptions() {
        Options retval = getDefaultCommandLineOptions();

        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_HOSTNAME_SHORT, OPT_NAME_HOSTNAME_LONG, "the hostname of the server", true, true)));
        return retval;
    }


}
