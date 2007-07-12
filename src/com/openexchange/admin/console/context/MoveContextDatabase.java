package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.exceptions.OXContextException;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class MoveContextDatabase extends ContextAbtraction {

    private final static char OPT_DATABASE_SHORT = 'd';

    private final static String OPT_DATABASE_LONG = "database";

    protected Option targetDatabaseIDOption = null;

    public MoveContextDatabase(final String[] args2) {

        final AdminParser parser = new AdminParser("movecontextdatabase");
        setOptions(parser);

        try {

            parser.ownparse(args2);
            final Context ctx = new Context();

            ctx.setID(Integer.parseInt((String) parser.getOptionValue(this.contextOption)));

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            final Database db = new Database(Integer.parseInt((String) parser.getOptionValue(this.targetDatabaseIDOption)));
            final MaintenanceReason mr = new MaintenanceReason(Integer.parseInt((String) parser.getOptionValue(this.maintenanceReasonIDOption)));

            oxres.moveContextDatabase(ctx, db, mr, auth);

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
        } catch (final NoSuchContextException e) {
            printServerException(e);
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final DatabaseUpdateException e) {
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
        } catch (final OXContextException e) {
            printServerException(e);
            sysexit(1);
        }

    }

    public static void main(final String args[]) {
        new MoveContextDatabase(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        setMaintenanceReasodIDOption(parser, true);

        this.targetDatabaseIDOption = setShortLongOpt(parser, OPT_DATABASE_SHORT, OPT_DATABASE_LONG, "Target database id", true, true);
    }
}
