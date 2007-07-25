package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class EnableAll extends ContextHostingAbstraction {

    public EnableAll(final String[] args2) {

        final AdminParser parser = new AdminParser("enableallcontexts");

        setDefaultCommandLineOptionsWithoutContextID(parser);
        try {

            parser.ownparse(args2);

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            oxres.enableAll(auth);

            displayEnabledMessage(null, null);
            sysexit(0);
        } catch (final ConnectException neti) {
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
        new EnableAll(args);
    }

    @Override
    protected String getObjectName() {
        return "all contexts";
    }
    
    
}
