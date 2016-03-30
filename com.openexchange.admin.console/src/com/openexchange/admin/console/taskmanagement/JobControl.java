/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package com.openexchange.admin.console.taskmanagement;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.CLIIllegalOptionValueException;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.CLIParseException;
import com.openexchange.admin.console.CLIUnknownOptionException;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
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

    private CLIOption list = null;

    private CLIOption delete = null;

    private CLIOption details = null;

    private CLIOption flush = null;

    public static void main(final String[] args) {
        new JobControl(args);
    }


    public JobControl(final String[] args2) {

        final AdminParser parser = new AdminParser("jobControl");

        setOptions(parser);

        try {
            parser.ownparse(args2);

            Context ctx = null;

            if (parser.getOptionValue(this.contextOption) != null) {
                ctx = new Context();
                ctx.setId(Integer.parseInt((String) parser.getOptionValue(this.contextOption)));
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
                } catch (final InterruptedException e) {
                    System.err.println("This job was interrupted with the following exception: ");
                    e.printStackTrace();
                } catch (final ExecutionException e) {
                    System.err.println("The execution of this job was aborted by the following exception: ");
                    e.getCause().printStackTrace();
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
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage(), parser);
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerException(e,parser);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(e,parser);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printServerException(e,parser);
            sysexit(1);
        } catch (final CLIParseException e) {
            printError("Parsing command-line failed : " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final CLIIllegalOptionValueException e) {
            printError("Illegal option value : " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final CLIUnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (final MissingOptionException e) {
            printError(e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        } catch (final InvalidDataException e) {
            printServerException(e,parser);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final InvalidCredentialsException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final StorageException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final TaskManagerException e) {
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
