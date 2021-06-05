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

import static com.openexchange.java.Autoboxing.I;
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
public class DeleteReason extends ReasonAbstraction {

    private final static char OPT_NAME_REASON_ID_SHORT = 'i';

    private final static String OPT_NAME_REASON_ID_LONG = "reasonid";

    private CLIOption reasonIDOption = null;

    public void execute(final String[] args2) {

        final AdminParser parser = new AdminParser("deletereason");

        setOptions(parser);

        String reason_id = null;
        try {
            parser.ownparse(args2);

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            reason_id = (String) parser.getOptionValue(this.reasonIDOption);
            final MaintenanceReason[] mrs = new MaintenanceReason[1];
            mrs[0] = new MaintenanceReason();
            mrs[0].setId(I(Integer.parseInt(reason_id)));
            oxutil.deleteMaintenanceReason(mrs, auth);

            displayDeletedMessage(reason_id, null, parser);
            sysexit(0);
        } catch (Exception e) {
            printErrors(reason_id, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new DeleteReason().execute(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        this.reasonIDOption = setShortLongOpt(parser, OPT_NAME_REASON_ID_SHORT, OPT_NAME_REASON_ID_LONG, "the id for the reason to be deleted", true, NeededQuadState.needed);
    }
}
