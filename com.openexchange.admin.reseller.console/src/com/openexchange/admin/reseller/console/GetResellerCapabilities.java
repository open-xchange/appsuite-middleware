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

import java.util.Set;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.java.Strings;

/**
 * {@link GetResellerCapabilities}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class GetResellerCapabilities extends ResellerAbstraction {

    /**
     * Entry point
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        new GetResellerCapabilities().execute(args);
    }

    /**
     * Initializes a new {@link GetResellerCapabilities}.
     */
    private GetResellerCapabilities() {
        super();
    }

    /**
     * Executes the command line tool
     *
     * @param args The command-line arguments
     */
    private void execute(String[] args) {
        AdminParser parser = new AdminParser("getresellercapabilities");
        setNameAndIdOptions(parser);

        try {
            parser.ownparse(args);

            ResellerAdmin admin = new ResellerAdmin();
            parseAndSetAdminId(parser, admin);
            parseAndSetAdminname(parser, admin);

            Credentials credentials = credentialsparsing(parser);
            OXResellerInterface rsi = getResellerInterface();
            if (admin.getId() == null && admin.getName() == null) {
                System.out.println("Either adminid or adminname must be specified");
                parser.printUsage();
                sysexit(1);
            }

            Set<String> capabilities = rsi.getCapabilities(admin, credentials);
            if (capabilities.isEmpty()) {
                if (admin.getId() != null) {
                    System.out.println("There are no capabilities set for reseller with id " + admin.getId());
                } else if (Strings.isNotEmpty(admin.getName())) {
                    System.out.println("There are no capabilities set for reseller with name " + admin.getName());
                } else {
                    System.out.println("There are no capabilities for the specified reseller");
                }
                return;
            }
            String lf = System.getProperty("line.separator");
            StringBuilder sb = new StringBuilder(2048);
            sb.append("Capabilities for reseller ");
            if (admin.getId() != null) {
                sb.append("with id ").append(admin.getId());
            } else if (Strings.isNotEmpty(admin.getName())) {
                sb.append("with name ").append(admin.getName());
            }
            sb.append(":").append(lf);
            for (String capa : capabilities) {
                sb.append(capa).append(lf);
            }
            System.out.println(sb.toString());
        } catch (Exception e) {
            printErrors(null, null, e, parser);
            sysexit(1);
        }
    }
}
