package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.AdminParser.NeededTriState;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.exceptions.OXContextException;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.NoSuchReasonException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class MoveContextFilestore extends ContextHostingAbstraction {

    private final static char OPT_FILESTORE_SHORT = 'f';

    private final static String OPT_FILESTORE_LONG = "filestore";

    protected Option targetFilestoreIDOption = null;

    public MoveContextFilestore(final String[] args2) {

        final AdminParser parser = new AdminParser("movecontextfilestore");
        setOptions(parser);

        Integer filestoreid = null;
        
        try {

            parser.ownparse(args2);
            final Context ctx = contextparsing(parser);
            
            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            filestoreid = Integer.parseInt((String) parser.getOptionValue(this.targetFilestoreIDOption));
            final Filestore fs = new Filestore(filestoreid);

            /*final MaintenanceReason mr = new MaintenanceReason(Integer.parseInt((String) parser.getOptionValue(this.maintenanceReasonIDOption)));

            oxres.moveContextFilestore(ctx, fs, mr, auth);*/
            final int jobId = oxres.moveContextFilestore(ctx, fs, auth);

            displayMovedMessage(ctxid, null, "to filestore " + filestoreid + " scheduled as job " + jobId);
            sysexit(0);
        } catch (final ConnectException neti) {
            // In this special case the second parameter is not the context id but the filestore id
            // this also applies to all following error outputting methods
            // see com.openexchange.admin.console.context.ContextHostingAbstraction.printFirstPartOfErrorText(Integer, Integer)
            printError(ctxid, null, neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final NumberFormatException num) {
            printInvalidInputMsg(ctxid, filestoreid, "Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerException(ctxid, filestoreid, e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(ctxid, filestoreid, e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printNotBoundResponse(ctxid, filestoreid, e);
            sysexit(1);
        } catch (final StorageException e) {
            printServerException(ctxid, filestoreid, e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerException(ctxid, filestoreid, e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final InvalidDataException e) {
            printServerException(ctxid, filestoreid, e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final NoSuchContextException e) {
            printServerException(ctxid, filestoreid, e);
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final IllegalOptionValueException e) {
            printError(ctxid, filestoreid, "Illegal option value : " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final UnknownOptionException e) {
            printError(ctxid, filestoreid, "Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (final MissingOptionException e) {
            printError(ctxid, filestoreid, e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        } catch (final NoSuchFilestoreException e) {
            printServerException(ctxid, filestoreid, e);
            sysexit(1);
        } catch (final NoSuchReasonException e) {
            printServerException(ctxid, filestoreid, e);
            sysexit(1);
        } catch (final OXContextException e) {
            printServerException(ctxid, filestoreid, e);
            sysexit(1);
        }

    }

    public static void main(final String args[]) {
        new MoveContextFilestore(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        //setMaintenanceReasodIDOption(parser, true);
        this.targetFilestoreIDOption = setShortLongOpt(parser, OPT_FILESTORE_SHORT, OPT_FILESTORE_LONG, "Target filestore id", true, NeededTriState.needed);

    }

}
