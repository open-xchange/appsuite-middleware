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
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class ExistsCore extends ContextAbstraction {

    protected void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setContextOption(parser, NeededQuadState.eitheror);
        setContextNameOption(parser, NeededQuadState.eitheror);
        setInServerOption(parser, NeededQuadState.eitheror);
    }

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);

        String successtext = null;
        try {
            Context ctx = null;
            Credentials auth = null;
            try {
                parser.ownparse(args);
                ctx = contextparsing(parser);

                // context name
                parseAndSetContextName(parser, ctx);
                parseAndSetInServer(parser);

                auth = credentialsparsing(parser);

                successtext = nameOrIdSetInt(this.ctxid, this.contextname, "context");

            } catch (RuntimeException e) {
                printError(null, null, e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
                sysexit(1);
                return;
            }
            String ctxident;
            if ( null != ctx.getId() ) {
                ctxident = String.valueOf(ctx.getId());
            } else {
                ctxident = ctx.getName();
            }
            if ( maincall(parser, ctx, inServer, auth) ) {
                System.out.println("Context " + ctxident + " exists" + (inServer ? " in server" : ""));
                sysexit(0);
            } else {
                System.out.println("Context " + ctxident + " does not exist" + (inServer ? " in server" : ""));
                sysexit(1);
            }
        } catch (Exception e) {
            printErrors(successtext, null, e, parser);
        }
        try {
            displayChangedMessage(successtext, null, parser);
            sysexit(0);
        } catch (RuntimeException e) {
            printError(null, null, e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
            sysexit(1);
        }
    }

    protected abstract boolean maincall(AdminParser parser, Context ctx, boolean inServer, Credentials auth) throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, StorageException, InvalidCredentialsException;
}
