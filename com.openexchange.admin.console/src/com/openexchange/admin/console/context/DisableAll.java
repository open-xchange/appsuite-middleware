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

import java.rmi.Naming;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;

public class DisableAll extends ContextAbstraction {

    /**
     * Entry point
     *
     * @param args command line arguments
     */
    public static void main(final String args[]) {
        new DisableAll().execute(args);
    }

    /**
     * Initializes a new {@link DisableAll}.
     */
    private DisableAll() {
        super();
    }

    /**
     * Executes the command
     *
     * @param args the command line arguments
     */
    private void execute(final String[] args) {
        final AdminParser parser = new AdminParser("disableallcontext");
        setOptions(parser);
        try {
            parser.ownparse(args);
            Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            OXContextInterface oxres = OXContextInterface.class.cast(Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME));

            /*
             * final MaintenanceReason mr = new MaintenanceReason(Integer.parseInt((String) parser.getOptionValue(this.maintenanceReasonIDOption)));
             * oxres.disableAll(mr, auth);
             */
            oxres.disableAll(auth);

            displayDisabledMessage(null, null, parser);
            sysexit(0);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
        }
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        //setMaintenanceReasodIDOption(parser, true);
    }

    @Override
    protected String getObjectName() {
        return "all contexts";
    }
}
