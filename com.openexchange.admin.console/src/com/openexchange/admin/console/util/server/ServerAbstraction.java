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

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.util.UtilAbstraction;
import com.openexchange.admin.rmi.dataobjects.Server;

public abstract class ServerAbstraction extends UtilAbstraction {

    static final String OPT_NAME_LONG = "name";
    static final String OPT_NAME_SERVER_ID_LONG = "id";
    static final char OPT_NAME_SERVER_ID_SHORT = 'i';
    static final char OPT_NAME_SHORT = 'n';

    protected String serverid = null;
    protected CLIOption serverIdOption = null;

    protected String servername = null;
    protected CLIOption serverNameOption = null;

    @Override
    protected String getObjectName() {
        return "server";
    }

    protected void parseAndSetServerID(final AdminParser parser, final Server sv) {
        serverid = (String) parser.getOptionValue(this.serverIdOption);
        if (null != serverid) {
            sv.setId(I(Integer.parseInt(serverid)));
        }
    }

    protected void parseAndSetServername(final AdminParser parser, final Server sv) {
        servername = (String) parser.getOptionValue(this.serverNameOption);
        if (null != servername) {
            sv.setName(servername);
        }
    }

    protected void setServeridOption(final AdminParser parser) {
        serverIdOption = setShortLongOpt(parser, OPT_NAME_SERVER_ID_SHORT, OPT_NAME_SERVER_ID_LONG, "The id of the server which should be deleted", true, NeededQuadState.eitheror);
    }

    protected void setServernameOption(final AdminParser parser, final NeededQuadState needed) {
        this.serverNameOption = setShortLongOpt(parser, OPT_NAME_SHORT, OPT_NAME_LONG, "The name of the server", true, needed);
    }
}
