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

package com.openexchange.admin.rmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Database;

/**
 * {@link Bug}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug16865Test extends AbstractRMITest {

    @Test
    public void testDefaultInitial() throws Throwable {
        Database db = new Database();
        db.setName("test" + System.currentTimeMillis());
        db.setPassword("secret");
        db.setMaster(Boolean.TRUE);
        Database created = getDatabaseManager().register(db, Boolean.FALSE, Integer.valueOf(0));
        try {
            Database test = null;
            for (Database tmpDB : getDatabaseManager().listAll()) {
                if (tmpDB.getName().equals(db.getName())) {
                    test = tmpDB;
                }
            }
            assertNotNull("Just registered database not found.", test);
            assertEquals("Initial number of database connections must be zero by default.", Integer.valueOf(0), test.getPoolInitial());
        } finally {
            getDatabaseManager().unregister(created);
        }
    }
}
