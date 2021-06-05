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

package com.openexchange.ajax.group;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.group.actions.ListRequest;
import com.openexchange.group.Group;

/**
 * Checks if group 0 lacks its identifier in JSON.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug11659Test extends AbstractAJAXSession {

    /**
     * Default constructor.
     * 
     * @param name name of the test.
     */
    public Bug11659Test() {
        super();
    }

    /**
     * Lists group 0 and checks if returned group contains its identifier.
     */
    @Test
    public void testForMissingIdentifier() throws Throwable {
        final Group[] groups = Executor.execute(getClient(), new ListRequest(new int[] { 0 })).getGroups();
        assertTrue("Identifier for group 0 is missing.", groups[0].getIdentifier() == 0);
    }
}
