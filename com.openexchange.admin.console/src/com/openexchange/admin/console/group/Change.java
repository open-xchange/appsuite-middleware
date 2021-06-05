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
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;

public class Change extends ChangeCore {

    public static void main(final String[] args) {
        new Change().execute(args);
    }

    public void execute(final String[] args2) {
        final AdminParser parser = new AdminParser("changegroup");
        commonfunctions(parser, args2);
    }

    @Override
    protected void maincall(AdminParser parser, OXGroupInterface oxgrp, Context ctx, Group grp, Credentials auth) throws RemoteException {
        // Nothing to do here
    }

    @Override
    protected void setFurtherOptions(AdminParser parser) {
        // Nothing to do here
    }
}
