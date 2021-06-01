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
import java.util.Set;
import java.util.TreeSet;
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
 * {@link GetContextCapabilities} - Determines the capabilities for a context.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.0
 */
public class GetContextCapabilities extends ContextAbstraction {

    /**
     * Executes the command
     *
     * @param args The CLI arguments
     */
    public void execute(String[] args) {
        final AdminParser parser = new AdminParser("getcontextcapabilities");
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
                return;
            }
            Set<String> caps = maincall(ctx, auth);

            if (null == caps || caps.isEmpty()) {
                System.out.println("There are no capabilities set for context " + ctx.getId());
            } else {
                final String lf = System.getProperty("line.separator");

                final StringBuilder sb = new StringBuilder(2048);
                sb.append("Capabilities for context ").append(ctx.getId()).append(":").append(lf);

                for (final String cap : new TreeSet<>(caps)) {
                    sb.append(cap).append(lf);
                }

                System.out.println(sb.toString());
            }

        } catch (Exception e) {
            printErrors(successtext, null, e, parser);
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        }
    }

    private Set<String> maincall(Context ctx, Credentials auth) throws MalformedURLException, RemoteException, NotBoundException, InvalidCredentialsException, StorageException, NoSuchContextException, InvalidDataException {
        final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME);
        return oxres.getCapabilities(ctx, auth);
    }

    public static void main(String[] args) {
        new GetContextCapabilities().execute(args);
    }
}
