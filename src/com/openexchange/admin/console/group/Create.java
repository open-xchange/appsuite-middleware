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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.admin.console.group;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Create extends GroupAbstraction {

    public static void main(final String[] args) {
        new Create(args);
    }

    private Create() {

    }

    public Create(final String[] args2) {

        final AdminParser parser = new AdminParser("create");

        setOptions(parser);

        try {
            parser.ownparse(args2);
            final Context ctx = new Context(DEFAULT_CONTEXT);

            if (parser.getOptionValue(this.contextOption) != null) {
                ctx.setID(Integer.parseInt((String) parser.getOptionValue(this.contextOption)));
            }

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            final OXGroupInterface oxgrp = (OXGroupInterface) Naming.lookup(RMI_HOSTNAME + OXGroupInterface.RMI_NAME);
            final Group grp = new Group();

            grp.setName((String) parser.getOptionValue(this.nameOption));
            grp.setDisplayname((String) parser.getOptionValue(this.displayNameOption));

            System.out.println(oxgrp.create(ctx, grp, auth));

            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final MalformedURLException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        } catch (final RemoteException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final NoSuchContextException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final StorageException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidDataException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_INVALID_DATA);
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
        } catch (final DatabaseUpdateException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        } catch (final NoSuchUserException e) {
           printServerResponse(e.getMessage());
           sysexit(SYSEXIT_NO_SUCH_USER);
        }

    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);

        // create options for this command line tool
        setGroupNameOption(parser, true);
        setGroupDisplayNameOption(parser, true);
    }
    
    protected void sysexit(final int exitcode) {
        System.exit(exitcode);
    }
}
