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

package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Exists extends ExistsCore {

    public void execute(final String[] args) {
        AdminParser parser = new AdminParser("existscontext");
        commonfunctions(parser, args);
    }

    public static void main(final String args[]) {
        new Exists().execute(args);
    }

    @Override
    protected boolean maincall(AdminParser parser, Context ctx, boolean inServer, Credentials auth) throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, StorageException, InvalidCredentialsException {
        // get rmi ref
        OXContextInterface oxctx = OXContextInterface.class.cast(Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME));
        return inServer ? oxctx.existsInServer(ctx, auth) : oxctx.exists(ctx, auth);
    }
}
