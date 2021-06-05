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

package com.openexchange.admin.rmi.factory;

import org.apache.commons.lang3.RandomStringUtils;
import com.openexchange.admin.reseller.rmi.AbstractOXResellerTest;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;

/**
 * {@link ResellerAdminFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ResellerAdminFactory {

    /**
     * Creates a new {@link ResellerAdmin} object
     * 
     * @return the new {@link ResellerAdmin} object
     */
    public static ResellerAdmin createResellerAdmin() {
        return createResellerAdmin(AbstractOXResellerTest.TESTUSER, "Test Reseller Admin");
    }

    /**
     * Creates a new {@link ResellerAdmin} object
     * 
     * @param name The reseller admin's name
     * @return the new {@link ResellerAdmin} object
     */
    public static ResellerAdmin createResellerAdmin(String name) {
        return createResellerAdmin(name, "Test Display Name");
    }

    /**
     * Creates a new {@link ResellerAdmin} with a random 10 character long name
     * 
     * @return The new reseller admin object
     */
    public static ResellerAdmin createRandomResellerAdmin() {
        String user = RandomStringUtils.randomAscii(10);
        ResellerAdmin adm = createResellerAdmin(user, user + " display");
        adm.setPassword("secret");
        return adm;
    }

    /**
     * Creates a new {@link ResellerAdmin} with the specified name and display name
     * 
     * @param name The name of the reseller admin
     * @param displayname The display name of the reseller admin
     * @return The new reseller admin object
     */
    public static ResellerAdmin createResellerAdmin(String name, String displayname) {
        ResellerAdmin adm = new ResellerAdmin(name);
        adm.setDisplayname(displayname);
        adm.setPassword("secret");
        return adm;
    }
}
