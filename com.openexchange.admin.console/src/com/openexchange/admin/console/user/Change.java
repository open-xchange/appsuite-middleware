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
import java.util.Set;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Change extends ChangeCore {

    public static void main(String[] args) {
        new Change().execute(args);
    }

    // ---------------------------------------------------------------------------------------------------

    public Change() {
        super();
    }

    private void execute(String[] args) {
        AdminParser parser = new AdminParser("changeuser");
        commonfunctions(parser, args);
    }

    @Override
    protected void maincall(AdminParser parser, OXUserInterface oxusr, Context ctx, User usr, UserModuleAccess access, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        parseAndSetUserQuota(parser, usr);

        oxusr.change(ctx, usr, auth);

        // Change module access IF an access combination name was supplied!
        // The normal change of access rights is done in the "commonfunctions"
        String accesscombinationname = parseAndSetAccessCombinationName(parser);
        if (null != accesscombinationname) {
            // Change user with access rights combination name
            oxusr.changeModuleAccess(ctx, usr, accesscombinationname, auth);
        }

        Set<String> capabilitiesToAdd = parseAndSetCapabilitiesToAdd(parser);
        Set<String> capabilitiesToRemove = parseAndSetCapabilitiesToRemove(parser);
        Set<String> capabilitiesToDrop = parseAndSetCapabilitiesToDrop(parser);
        if ((null != capabilitiesToAdd && !capabilitiesToAdd.isEmpty()) || (null != capabilitiesToRemove && !capabilitiesToRemove.isEmpty()) || (null != capabilitiesToDrop && !capabilitiesToDrop.isEmpty())) {
            oxusr.changeCapabilities(ctx, usr, capabilitiesToAdd, capabilitiesToRemove, capabilitiesToDrop, auth);
        }

        String personal = parseAndSetPersonal(parser);
        if (null != personal) {
            oxusr.changeMailAddressPersonal(ctx, usr, "NULL".equals(personal) ? null : personal, auth);
        }

    }

    @Override
    protected void setFurtherOptions(AdminParser parser) {
        setAddAccessRightCombinationNameOption(parser);
        setCapsToAdd(parser);
        setCapsToRemove(parser);
        setCapsToDrop(parser);
        setPersonal(parser);
        setUserQuotaOption(parser, false);
    }

}
