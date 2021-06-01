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

package com.openexchange.admin.console.user;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.ParseException;
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
import au.com.bytecode.opencsv.CSVReader;

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
            
            applyDriveFolderModeOption(parser, usr);

            final String filename = (String) parser.getOptionValue(parser.getCsvImportOption());

            if (null != filename) {
                csvParsing(filename, oxusr);
            } else {
                applyExtendedOptionsToUser(parser, usr);

                maincall(parser, oxusr, ctx, usr, auth);
            }

            sysexit(0);
        } catch (Exception e) {
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
                    } catch (RemoteException e) {
                        System.err.println("Failed to create user \"" + adminuser.getName() + "\" in context " + context.getId() + ": " + e);
                    } catch (StorageException e) {
                        System.err.println("Failed to create user \"" + adminuser.getName() + "\" in context " + context.getId() + ": " + e);
                    } catch (InvalidCredentialsException e) {
                        System.err.println("Failed to create user \"" + adminuser.getName() + "\" in context " + context.getId() + ": " + e);
                    } catch (NoSuchContextException e) {
                        System.err.println("Failed to create user \"" + adminuser.getName() + "\" in context " + context.getId() + ": " + e);
                    } catch (InvalidDataException e) {
                        System.err.println("Failed to create user \"" + adminuser.getName() + "\" in context " + context.getId() + ": " + e);
                    } catch (DatabaseUpdateException e) {
                        System.err.println("Failed to create user \"" + adminuser.getName() + "\" in context " + context.getId() + ": " + e);
                    }
                } catch (ParseException e1) {
                    System.err.println("Failed to create user in context " + context.getId() + ": " + e1);
                }
            } catch (ParseException e1) {
                System.err.println("Failed to create user in context in line" + linenumber + ": " + e1);
            }
            linenumber++;
        }
    }
}
