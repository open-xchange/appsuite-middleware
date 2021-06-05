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
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Create extends CreateCore {

    public static void main(String[] args) {
        new Create().execute(args);
    }

    // ---------------------------------------------------------------------------------------------------------------------

    public void execute(String[] args) {
        commonfunctions(new AdminParser("createuser"), args);
    }

    @Override
    protected void maincall(AdminParser parser, OXUserInterface oxusr, Context ctx, User usr, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        parseAndSetUserQuota(parser, usr);
        parseAndSetFilestoreId(parser, usr);
        parseAndSetFilestoreOwner(parser, usr);
        String accesscombinationname = parseAndSetAccessCombinationName(parser);
        if (null != accesscombinationname) {
            // Create user with access rights combination name
            Integer id = oxusr.create(ctx, usr, accesscombinationname, auth).getId();
            displayCreatedMessage(String.valueOf(id), ctx.getId(), parser);
        } else {
            // get the context-admins module access rights as baseline
            UserModuleAccess access = oxusr.getContextAdminUserModuleAccess(ctx, auth);

            if (access.isPublicFolderEditable()) {
                // publicFolderEditable can only be applied to the context administrator.
                access.setPublicFolderEditable(false);
            }

            // adjust module access rights according to parameters given on the command line
            setModuleAccessOptions(parser, access);

            // create the user with the adjusted module access rights
            Integer id = oxusr.create(ctx, usr, access, auth).getId();
            displayCreatedMessage(String.valueOf(id), ctx.getId(), parser);
        }
    }

    @Override
    protected void setFurtherOptions(AdminParser parser) {
        setAddAccessRightCombinationNameOption(parser);
        setFilestoreIdOption(parser, false);
        setFilestoreOwnerOption(parser, false);
        setUserQuotaOption(parser, false);
    }
}
