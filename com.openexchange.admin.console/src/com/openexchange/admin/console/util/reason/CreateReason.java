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

package com.openexchange.admin.console.util.reason;

import java.rmi.Naming;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;

/**
 *
 * @author d7,cutmasta
 *
 */
public class CreateReason extends ReasonAbstraction {

    private final static char OPT_NAME_REASON_TEXT_SHORT = 'r';

    private final static String OPT_NAME_REASON_TEXT_LONG = "reasontext";

    private CLIOption reasonTextOption = null;

    public void execute(final String[] args2) {
        final AdminParser parser = new AdminParser("createreason");

        setOptions(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            final MaintenanceReason reason = new MaintenanceReason((String) parser.getOptionValue(this.reasonTextOption));

            displayCreatedMessage(String.valueOf(oxutil.createMaintenanceReason(reason, auth).getId()), null, parser);
            sysexit(0);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
        }

    }

    public static void main(final String args[]) {
        new CreateReason().execute(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        this.reasonTextOption = setShortLongOpt(parser, OPT_NAME_REASON_TEXT_SHORT, OPT_NAME_REASON_TEXT_LONG, "the text for the added reason", true, NeededQuadState.needed);

    }
}
