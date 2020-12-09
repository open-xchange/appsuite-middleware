/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.admin.reseller.console;

import java.util.Set;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.rmi.dataobjects.Credentials;

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
        setOptions(parser);

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
                System.out.println("There are no capabilities set for reseller with id " + admin.getId());
                return;
            }
            String lf = System.getProperty("line.separator");
            StringBuilder sb = new StringBuilder(2048);
            sb.append("Capabilities for reseller with id ").append(admin.getId()).append(":").append(lf);
            for (String capa : capabilities) {
                sb.append(capa).append(lf);
            }
            System.out.println(sb.toString());
        } catch (Exception e) {
            printErrors(null, null, e, parser);
            sysexit(1);
        }
    }

    /**
     * Sets the extra options for the clt
     *
     * @param parser The {@link AdminParser}
     */
    private void setOptions(final AdminParser parser) {
        setNameAndIdOptions(parser);
        parser.allowDynamicOptions();
        parser.allowFlexibleDynamicOptions();
    }
}
