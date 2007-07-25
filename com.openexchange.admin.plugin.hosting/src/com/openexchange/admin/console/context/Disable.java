package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.exceptions.OXContextException;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchReasonException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Disable extends ContextHostingAbstraction {

    public Disable(final String[] args2) {

        final AdminParser parser = new AdminParser("disablecontext");

        setOptions(parser);

        try {

            parser.ownparse(args2);
            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            /*final MaintenanceReason mr = new MaintenanceReason(Integer.parseInt((String) parser.getOptionValue(this.maintenanceReasonIDOption)));
            oxres.disable(ctx, mr, auth); */
            oxres.disable(ctx, auth);

            displayDisabledMessage(ctxid, null);
            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(ctxid, null, neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg(ctxid, null, "Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerException(ctxid, null, e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(ctxid, null, e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printNotBoundResponse(ctxid, null, e);
            sysexit(1);
        } catch (final StorageException e) {
            printServerException(ctxid, null, e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerException(ctxid, null, e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final InvalidDataException e) {
            printServerException(ctxid, null, e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final NoSuchContextException e) {
            printServerException(ctxid, null, e);
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final IllegalOptionValueException e) {
            printError(ctxid, null, "Illegal option value : " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final UnknownOptionException e) {
            printError(ctxid, null, "Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (final MissingOptionException e) {
            printError(ctxid, null, e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        } catch (final NoSuchReasonException e) {
            printServerException(ctxid, null, e);
            sysexit(1);
        } catch (final OXContextException e) {
            printServerException(ctxid, null, e);
            sysexit(1);
        }

    }

    public static void main(final String args[]) {
        new Disable(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        //setMaintenanceReasodIDOption(parser, true);

    }
}
