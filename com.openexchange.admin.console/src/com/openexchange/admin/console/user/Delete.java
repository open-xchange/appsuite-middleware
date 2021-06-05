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
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Delete extends DeleteCore {

    private static final String REASSIGN_LONG = "reassign";
    private static final char REASSIGN_SHORT = 'r';
    private static final String NO_REASSIGN_LONG = "no-reassign";
    private CLIOption reassignCLI;
    private CLIOption noReassignCLI;

    public static void main(final String[] args) {
        new Delete().execute(args);
    }

    public void execute(final String[] args) {
        commonfunctions(new AdminParser("deleteuser"), args);
    }

    @Override
    protected void maincall(final AdminParser parser, final OXUserInterface oxusr, final Context ctx, final User usr, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        Integer destUser = null;
        if (parser.hasOption(noReassignCLI)) {
            destUser = Integer.valueOf(0);
        } else {
            Object o = parser.getOptionValue(reassignCLI);
            if (o != null) {
                destUser = o instanceof String ? Integer.valueOf((String) o) : Integer.class.cast(o);
            }
        }
        oxusr.delete(ctx, usr, destUser, auth);
    }

    @Override
    protected void setFurtherOptions(AdminParser parser) {

        reassignCLI = parser.addOption(REASSIGN_SHORT, REASSIGN_LONG, "The user id shared data will be assigned to. If omitted the context admin will be used instead.", false);
        noReassignCLI = parser.addSettableBooleanOption(NO_REASSIGN_LONG, null, "If set all shared data will be deleted instead of being assigned.", false, false, false);
    }
}
