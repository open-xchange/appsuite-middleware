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

package com.openexchange.admin.console.util.server;

import java.rmi.Naming;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Server;

/**
 *
 * @author d7,cutmasta
 *
 */
public class UnregisterServer extends ServerAbstraction {

    public void execute(final String[] args2) {
        AdminParser parser = new AdminParser("unregisterserver");

        setOptions(parser);

        String successtext = null;
        try {
            parser.ownparse(args2);

            final Server sv = new Server();

            parseAndSetServerID(parser, sv);
            parseAndSetServername(parser, sv);

            successtext = nameOrIdSet(this.serverid, this.servername, "server");

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            oxutil.unregisterServer(sv, auth);

            displayUnregisteredMessage(successtext, parser);
            sysexit(0);
        } catch (Exception e) {
            printErrors(successtext, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new UnregisterServer().execute(args);
    }

    private void setOptions(AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setServeridOption(parser);
        setServernameOption(parser, NeededQuadState.eitheror);
    }
}
