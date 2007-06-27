package com.openexchange.admin.console.taskmanagement;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXTaskMgmtInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.exceptions.TaskManagerException;

public class JobControl extends BasicCommandlineOptions {
    
    private static final char OPT_LIST_SHORT = 'l';

    private static final String OPT_LIST_LONG = "list";

    private static final char OPT_DELETE_SHORT = 'd';

    private static final String OPT_DELETE_LONG = "delete";

    private static final char OPT_DETAILS_SHORT = 't';

    private static final String OPT_DETAILS_LONG = "details";

    private static final String OPT_FLUSH_LONG = "flush";

    private static final char OPT_FLUSH_SHORT = 'f';
    
    private Option list = null;

    private Option delete = null;

    private Option details = null;

    private Option flush = null;
    
    public static void main(final String[] args) {
        new JobControl(args);
    }

    
    public JobControl(final String[] args2) {

        final AdminParser parser = new AdminParser("jobControl");

        setOptions(parser);
        
        try {
            parser.ownparse(args2);

            final Context ctx = new Context(DEFAULT_CONTEXT);

            if (parser.getOptionValue(this.contextOption) != null) {
                ctx.setID(Integer.parseInt((String) parser.getOptionValue(this.contextOption)));
            }

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            final OXTaskMgmtInterface oxtask = (OXTaskMgmtInterface) Naming.lookup(RMI_HOSTNAME + OXTaskMgmtInterface.RMI_NAME);

            final String deleteValue = (String) parser.getOptionValue(this.delete);
            final String detailValue = (String) parser.getOptionValue(this.details);
            if (null != parser.getOptionValue(this.list)) {
                System.out.println(oxtask.getJobList(ctx, auth));
            } else  if (null != deleteValue) {
                oxtask.deleteJob(ctx, auth, Integer.parseInt(deleteValue));
                System.out.println("Deleted job with ID" + deleteValue);
            } else if (null != detailValue) {
                try {
                    oxtask.getTaskResults(ctx, auth, Integer.parseInt(detailValue));
                } catch (InterruptedException e) {
                    System.err.println("This job was interrupted with the following exception: ");
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    System.err.println("The execution of this job was aborted by the following exception: ");
                    e.getCause().printStackTrace();
                }
            } else if (null != parser.getOptionValue(this.flush)) {
                oxtask.flush(ctx, auth);
                System.out.println("All finished jobs flushed.");
            } else {
                System.err.println("No option selected (list, delete, details).");
                parser.printUsage();
            }
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerResponse(e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printServerResponse(e);
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
        } catch (final InvalidDataException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final StorageException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (TaskManagerException e) {
            printServerResponse(e);
            sysexit(1);
        }
    }


    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        
        this.list = setShortLongOpt(parser, OPT_LIST_SHORT, OPT_LIST_LONG, "list the jobs of this open-xchange server", false, false);
        this.delete = setShortLongOpt(parser, OPT_DELETE_SHORT, OPT_DELETE_LONG, "id", "delete the the given job id", false);
        this.details = setShortLongOpt(parser, OPT_DETAILS_SHORT, OPT_DETAILS_LONG, "id", "show details for the given job", false);
        this.flush = setShortLongOpt(parser, OPT_FLUSH_SHORT, OPT_FLUSH_LONG, "flush all finished jobs from the queue", false, false);
    }
}
