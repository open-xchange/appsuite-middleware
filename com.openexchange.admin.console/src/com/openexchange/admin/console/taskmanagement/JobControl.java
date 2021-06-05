/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */
package com.openexchange.admin.console.taskmanagement;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.CLIIllegalOptionValueException;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.CLIParseException;
import com.openexchange.admin.console.CLIUnknownOptionException;
import com.openexchange.admin.rmi.OXTaskMgmtInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;
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

    public static void main(final String[] args) {
        new JobControl().execute(args);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private CLIOption list;
    private CLIOption delete;
    private CLIOption details;
    private CLIOption flush;

    /**
     * Initializes a new {@link JobControl}.
     */
    public JobControl() {
        super();
    }

    public void execute(final String[] args2) {
        final AdminParser parser = new AdminParser("jobControl");
        setOptions(parser);
        try {
            parser.ownparse(args2);

            Context ctx = null;

            if (parser.getOptionValue(this.contextOption) != null) {
                ctx = new Context();
                ctx.setId(Integer.valueOf((String) parser.getOptionValue(this.contextOption)));
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
                    System.out.println(oxtask.getJob(ctx, auth, Integer.valueOf(detailValue)));
                } catch (@SuppressWarnings("unused") NumberFormatException e) {
                    System.out.println("Invalid job identifier");
                    sysexit(1);
                }
            } else if (null != parser.getOptionValue(this.flush)) {
                oxtask.flush(ctx, auth);
                System.out.println("All finished jobs flushed.");
            } else {
                System.err.println(new StringBuilder("No option selected (").append(OPT_LIST_LONG).append(", ")
                        .append(OPT_DELETE_LONG).append(", ").append(OPT_DETAILS_LONG).append(", ").append(OPT_FLUSH_LONG)
                        .append(")"));
                parser.printUsage();
            }
        } catch (java.rmi.ConnectException neti) {
            printError(neti.getMessage(), parser);
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (@SuppressWarnings("unused") java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            sysexit(1);
        } catch (MalformedURLException e) {
            printServerException(e,parser);
            sysexit(1);
        } catch (RemoteException e) {
            printServerException(e,parser);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (NotBoundException e) {
            printServerException(e,parser);
            sysexit(1);
        } catch (CLIParseException e) {
            printError("Parsing command-line failed : " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (CLIIllegalOptionValueException e) {
            printError("Illegal option value : " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (CLIUnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (MissingOptionException e) {
            printError(e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        } catch (InvalidDataException e) {
            printServerException(e,parser);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (InvalidCredentialsException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (StorageException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (TaskManagerException e) {
            printServerException(e, parser);
            sysexit(1);
        }
    }


    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setContextOption(parser, NeededQuadState.notneeded);

        this.list = setShortLongOpt(parser, OPT_LIST_SHORT, OPT_LIST_LONG, "list the jobs of this open-xchange server", false, NeededQuadState.notneeded);
        this.delete = setShortLongOpt(parser, OPT_DELETE_SHORT, OPT_DELETE_LONG, "id", "delete the the given job id", false);
        this.details = setShortLongOpt(parser, OPT_DETAILS_SHORT, OPT_DETAILS_LONG, "id", "show details for the given job", false);
        this.flush = setShortLongOpt(parser, OPT_FLUSH_SHORT, OPT_FLUSH_LONG, "flush all finished jobs from the queue", false, NeededQuadState.notneeded);
    }
}
