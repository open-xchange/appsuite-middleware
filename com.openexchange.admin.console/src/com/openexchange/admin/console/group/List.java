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

package com.openexchange.admin.console.group;

import java.rmi.RemoteException;
import java.util.ArrayList;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class List extends ListCore {

    public static void main(final String[] args) {
        new List().execute(args);
    }

    public void execute(final String[] args) {
        commonfunctions(new AdminParser("listgroup"), args);
    }

    @Override
    protected void extendscvscolumns(ArrayList<String> columns) {
        // Nothing to do here
    }

    @Override
    protected void extendmakeCSVData(Group group, ArrayList<String> grp_data) {
        // Nothing to do here
    }

    @Override
    protected void maincall(AdminParser parser, OXGroupInterface oxgrp, Context ctx, ArrayList<Group> grplist, Group[] allgrps, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        for (final Group group : allgrps) {
            grplist.add(oxgrp.getData(ctx, group, auth));
        }
    }
}
