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

package com.openexchange.admin.reseller.console;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * {@link GetReseller} - getadmin command line tool
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetReseller extends ResellerAbstraction {

    /**
     * Entry point
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        new GetReseller().start(args);
    }

    /**
     * Initializes a new {@link GetReseller}.
     */
    private GetReseller() {
        super();
    }

    /**
     * Start the clt
     *
     * @param args the command line arguments
     */
    private void start(String[] args) {
        AdminParser parser = new AdminParser("getadmin");

        setOptions(parser);

        String successtext = null;
        // parse the command line
        try {
            parser.ownparse(args);

            Credentials auth = credentialsparsing(parser);
            ResellerAdmin admin = new ResellerAdmin();
            parseAndSetAdminId(parser, admin);
            parseAndSetAdminname(parser, admin);
            nameOrIdSetInt(this.adminid, this.adminname, "admin");
            System.out.println(getResellerInterface().getSelfData(admin, auth).printOut());
            sysexit(0);
        } catch (Exception e) {
            printErrors(successtext, null, e, parser);
            sysexit(1);
        }
    }

    /**
     * Sets the extra options for the clt
     *
     * @param parser The {@link AdminParser}
     */
    private void setOptions(AdminParser parser) {
        setNameAndIdOptions(parser);
        parser.allowDynamicOptions();
        parser.allowFlexibleDynamicOptions();
    }
}
