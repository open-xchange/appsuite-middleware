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

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
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
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class ChangeCore extends UserFilestoreAbstraction {
	
	protected CLIOption convertDriveUserFoldersOption = null;
    protected static final String OPT_CONVERT_DRIVE_USER_FOLDERS = "convert-drive-user-folders";

    protected final void setOptions(final AdminParser parser) {

        parser.setExtendedOptions();
        setDefaultCommandLineOptions(parser);

        // required
        setIdOption(parser);
        setUsernameOption(parser, NeededQuadState.eitheror);

        setMandatoryOptionsWithoutUsername(parser, NeededQuadState.notneeded);

        // add optional opts
        setOptionalOptions(parser);

        setFurtherOptions(parser);
        
        this.convertDriveUserFoldersOption = setLongOpt(parser, OPT_CONVERT_DRIVE_USER_FOLDERS, "Convert drive user folders into normal folders", false, false);

        parser.allowDynamicOptions();
    }

    protected abstract void setFurtherOptions(final AdminParser parser);

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        // set all needed options in our parser
        setOptions(parser);

        setExtendedOptions(parser);

        String successtext = null;
        try {
            parser.ownparse(args);

            // create user obj
            final User usr = new User();
            parseAndSetUserId(parser, usr);
            parseAndSetUsername(parser, usr);

            successtext = nameOrIdSetInt(this.userid, this.username, "user");

            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUserInterface oxusr = getUserInterface();


            // fill user obj with mandatory values from console
            parseAndSetMandatoryOptionsWithoutUsernameInUser(parser, usr);

            // add optional values if set
            parseAndSetOptionalOptionsinUser(parser, usr);

            applyExtendedOptionsToUser(parser, usr);

            // change module access
            // first load current module access rights from server
            UserModuleAccess access = oxusr.getModuleAccess(ctx, usr, auth);

            // apply rights from commandline
            boolean changed = setModuleAccessOptions(parser, access);

            if (changed) {
                // apply changes in module access on server
                oxusr.changeModuleAccess(ctx, usr, access, auth);
            }

            // Dynamic Options
            applyDynamicOptionsToUser(parser, usr);
            
            if (parser.hasOption(this.convertDriveUserFoldersOption)) {
            	usr.setConvertDriveUserFolders(true);
            }

            // finally do change call last (must be done last because else we cannot
            // change admin password
            maincall(parser, oxusr, ctx, usr, access, auth);

            displayChangedMessage(successtext, ctx.getId(), parser);
            sysexit(0);
        } catch (Exception e) {
            printErrors(successtext, ctxid, e, parser);
            sysexit(1);
        }

    }

    protected abstract void maincall(final AdminParser parser, final OXUserInterface oxusr, final Context ctx, final User usr, final UserModuleAccess access, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, MalformedURLException, NotBoundException, DuplicateExtensionException;

}
