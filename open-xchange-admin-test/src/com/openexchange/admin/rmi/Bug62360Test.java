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

import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.factory.ContextFactory;
import com.openexchange.admin.rmi.factory.UserFactory;

/**
 * 
 * {@link Bug62360Test}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public final class Bug62360Test extends AbstractRMITest {

    private Context context;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        context = createContext("bug62360context.com", ContextFactory.getRandomContextId());
    }

    @Test
    public void test() throws Throwable {
        getContextManager().disable(context);
        Context data = getContextManager().getData(context);
        assertFalse(data.isEnabled());

        getContextManager().disable(context);
        assertFalse(data.isEnabled());
    }

    private Context createContext(String name, int cid) throws Exception {
        Context newContext = ContextFactory.createContext(cid, name);
        User newAdmin = UserFactory.createUser("oxadmin", "secret", "New Admin", "New", "Admin", "newadmin@ox.invalid");
        return getContextManager().create(newContext, newAdmin);
    }
}
