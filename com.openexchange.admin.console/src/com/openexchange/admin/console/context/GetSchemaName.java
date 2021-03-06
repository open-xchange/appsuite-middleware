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
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * {@link GetSchemaName} - Determines the name of the database schema in which a specified context is located.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.4.2
 */
public class GetSchemaName extends ContextAbstraction {

    /**
     * Executes the command
     *
     * @param args The CLI arguments
     */
    public void execute(String[] args) {
        final AdminParser parser = new AdminParser("getschemaname");
        commonfunctions(parser, args);
    }

    protected void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setContextOption(parser, NeededQuadState.eitheror);
        setContextNameOption(parser, NeededQuadState.eitheror);
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

                auth = credentialsparsing(parser);

                parseAndSetContextName(parser, ctx);

                successtext = nameOrIdSetInt(this.ctxid, this.contextname, "context");

            } catch (RuntimeException e) {
                printError(null, null, e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
                sysexit(1);
            }
            Context context = maincall(ctx, auth);

            System.out.println("Schema name for context " + context.getId() + (context.getWriteDatabase() == null ? "" : ": " + context.getWriteDatabase().getScheme()));
        } catch (Exception e) {
            printErrors(successtext, null, e, parser);
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        }
    }

    private Context maincall(Context ctx, Credentials auth) throws MalformedURLException, RemoteException, NotBoundException, InvalidCredentialsException, StorageException, NoSuchContextException, InvalidDataException {
        final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME);
        return oxres.getData(ctx, auth);
    }

    public static void main(String[] args) {
        new GetSchemaName().execute(args);
    }
}
