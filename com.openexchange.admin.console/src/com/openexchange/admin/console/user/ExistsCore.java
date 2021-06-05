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

import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class ExistsCore extends UserAbstraction {

    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);

        // required
        setIdOption(parser);
        setUsernameOption(parser, NeededQuadState.eitheror);
        setDisplayNameOption(parser, NeededQuadState.eitheror);
    }

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        // set all needed options in our parser
        setOptions(parser);
        // parse the command line
        try {
            parser.ownparse(args);

            // Parse arguments
            Context ctx = contextparsing(parser);
            User usr = new User();
            parseAndSetUserId(parser, usr);
            parseAndSetUsername(parser, usr);
            parseAndSetDisplayName(parser, usr);
            Credentials auth = credentialsparsing(parser);

            // Get RMI stub
            OXUserInterface oxusr = getUserInterface();

            String usrident;
            if (null != usr.getId()) {
                usrident = String.valueOf(usr.getId());
            } else if (null != usr.getName()) {
                usrident = usr.getName();
            } else if (null != usr.getDisplay_name()) {
                usrident = new StringBuilder(usr.getDisplay_name().length() + 2).append('"').append(usr.getDisplay_name()).append('"').toString();
            } else {
                throw new MissingOptionException("Neither identifier, name nor display name given");
            }

            if (maincall(parser, oxusr, ctx, usr, auth)) {
                System.out.println("User " + usrident + " exists");
                sysexit(0);
            } else {
                System.out.println("User " + usrident + " does not exist");
                sysexit(1);
            }

        } catch (Exception e) {
            printErrors(null, ctxid, e, parser);
        }
    }

    protected abstract boolean maincall(final AdminParser parser, final OXUserInterface oxusr, final Context ctx, final User usr, final Credentials auth) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, DatabaseUpdateException, NoSuchContextException;
}
