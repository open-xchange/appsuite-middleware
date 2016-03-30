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

package com.openexchange.admin.console.user;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.ParseException;
import au.com.bytecode.opencsv.CSVReader;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class CreateCore extends UserFilestoreAbstraction {

    protected final void setOptions(final AdminParser parser) {

        parser.setExtendedOptions();
        setCsvImport(parser);
        setDefaultCommandLineOptions(parser);

        // add mandatory options
        setMandatoryOptions(parser);

        // add optional opts
        setOptionalOptions(parser);

        setFurtherOptions(parser);

        parser.allowDynamicOptions();
    }

    protected abstract void setFurtherOptions(final AdminParser parser);

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        // set all needed options in our parser
        setOptions(parser);

        setExtendedOptions(parser);

        // parse the command line
        try {
            parser.ownparse(args);

            final Context ctx = contextparsing(parser);
            ctxid = ctx.getId();

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUserInterface oxusr = getUserInterface();

            // create user obj
            final User usr = new User();

            // fill user obj with mandatory values from console
            parseAndSetMandatoryOptionsinUser(parser, usr);

            // add optional values if set
            parseAndSetOptionalOptionsinUser(parser, usr);

            applyExtendedOptionsToUser(parser, usr);

            applyDynamicOptionsToUser(parser, usr);

            final String filename = (String) parser.getOptionValue(parser.getCsvImportOption());

            if (null != filename) {
                csvParsing(filename, oxusr);
            } else {
                applyExtendedOptionsToUser(parser, usr);

                maincall(parser, oxusr, ctx, usr, auth);
            }

            sysexit(0);
        } catch (final Exception e) {
            printErrors(null, ctxid, e, parser);
        }
    }

    protected abstract void maincall(final AdminParser parser, final OXUserInterface oxusr, final Context ctx, final User usr, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, DuplicateExtensionException, MalformedURLException, NotBoundException, ConnectException;

    private void csvParsing(final String filename, final OXUserInterface oxuser) throws FileNotFoundException, IOException, InvalidDataException {
        final CSVReader reader = new CSVReader(new FileReader(filename), ',', '"');
        int[] idarray = csvParsingCommon(reader);
        int linenumber = 2;
        String [] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            // nextLine[] is an array of values from the line
            try {
                final Context context = getContext(nextLine, idarray);
                try {
                    final User adminuser = getUser(nextLine, idarray);
                    final Credentials auth = getCreds(nextLine, idarray);
                    final int i = idarray[AccessCombinations.ACCESS_COMBI_NAME.getIndex()];
                    try {
                        final User create;
                        if (-1 != i) {
                            // create call
                            create = oxuser.create(context, adminuser, nextLine[i], auth);
                        } else {
                            final UserModuleAccess moduleacess = getUserModuleAccess(nextLine, idarray);
                            if (!NO_RIGHTS_ACCESS.equals(moduleacess)) {
                                // with module access
                                create = oxuser.create(context, adminuser, moduleacess, auth);
                            } else {
                                // without module access
                                create = oxuser.create(context, adminuser, auth);
                            }
                        }
                        System.out.println("User " + create.getId() + " successfully created in context " + context.getId());
                    } catch (final RemoteException e) {
                        System.err.println("Failed to create user \"" + adminuser.getName() + "\" in context " + context.getId() + ": " + e);
                    } catch (final StorageException e) {
                        System.err.println("Failed to create user \"" + adminuser.getName() + "\" in context " + context.getId() + ": " + e);
                    } catch (final InvalidCredentialsException e) {
                        System.err.println("Failed to create user \"" + adminuser.getName() + "\" in context " + context.getId() + ": " + e);
                    } catch (final NoSuchContextException e) {
                        System.err.println("Failed to create user \"" + adminuser.getName() + "\" in context " + context.getId() + ": " + e);
                    } catch (final InvalidDataException e) {
                        System.err.println("Failed to create user \"" + adminuser.getName() + "\" in context " + context.getId() + ": " + e);
                    } catch (final DatabaseUpdateException e) {
                        System.err.println("Failed to create user \"" + adminuser.getName() + "\" in context " + context.getId() + ": " + e);
                    }
                } catch (final ParseException e1) {
                    System.err.println("Failed to create user in context " + context.getId() + ": " + e1);
                }
            } catch (final ParseException e1) {
                System.err.println("Failed to create user in context in line" + linenumber + ": " + e1);
            }
            linenumber++;
        }
    }
}
