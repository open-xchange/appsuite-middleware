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

package com.openexchange.admin.reseller.rmi.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.Test;
import com.openexchange.admin.reseller.rmi.AbstractOXResellerTest;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.rmi.factory.ResellerAdminFactory;

/**
 * {@link Bugs_795_801}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class Bugs_795_801 extends AbstractOXResellerTest {

    private static final String ERROR_MESSAGE = "Unable to delete %1$s, still owns Subadmin(s)";

    /**
     * Initializes a new {@link Bugs_795_801}.
     */
    public Bugs_795_801() {
        super();
    }

    /**
     * Bug 795
     */
    @Test
    public void testDeleteSubadminWithIdThatStillOwnsSubsubAdmins() throws Exception {
        executeWith("reseller-mwb-795", (a) -> {
            ResellerAdmin toDelete = new ResellerAdmin();
            toDelete.setId(a.getId());
            return toDelete;
        });
    }

    /**
     * Bug 801
     */
    @Test
    public void testDeleteSubadminWithNameThatStillOwnsSubsubAdmins() throws Exception {
        executeWith("reseller-mwb-801", (a) -> {
            ResellerAdmin toDelete = new ResellerAdmin();
            toDelete.setId(a.getId());
            return toDelete;
        });
    }

    /**
     * Executes the test case with the specified reseller name and consumer
     *
     * @param n The reseller name
     * @param consumer The consumer
     */
    private void executeWith(String n, Function<ResellerAdmin, ResellerAdmin> consumer) throws Exception {
        ResellerAdmin adm = ResellerAdminFactory.createResellerAdmin(n);
        adm.setRestrictions(new Restriction[] { CanCreateSubAdmin() });

        ResellerAdmin admin = getResellerManager().create(adm);
        ResellerAdmin subAdmin = getResellerManager().create(admin, ResellerAdminFactory.createResellerAdmin("sub" + n));

        try {
            getResellerManager().delete(consumer.apply(admin));
            fail("The parent admin should not be deletable when there are subadmins bound to the parent.");
        } catch (Exception e) {
            // Expected
            assertTrue("Unexpected error", e instanceof OXResellerException);
            String expected = String.format(ERROR_MESSAGE, admin.getId());
            String actual = String.format(OXResellerException.Code.UNABLE_TO_DELETE_OWNS_SUBADMINS.getText(), admin.getId());
            assertEquals("Wrong message", expected, actual);
        }

        getResellerManager().delete(subAdmin);
        getResellerManager().delete(admin);
    }
}
