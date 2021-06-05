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

package com.openexchange.ajax.contact;

import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.openexchange.ajax.contact.action.AutocompleteRequest;
import com.openexchange.ajax.framework.CommonSearchResponse;

/**
 * {@link Bug42225Test} - Empty query string leads to SQLException
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class Bug42225Test extends AbstractManagedContactTest {

    /**
     * Initializes a new {@link Bug42225Test}.
     * 
     * @param name
     */
    public Bug42225Test() {
        super();
    }

    @Test
    public void testBug42225() throws Exception {
        AutocompleteRequest req = new AutocompleteRequest("", false, String.valueOf(getClient().getValues().getPrivateContactFolder()), new int[] { 500 }, false);
        CommonSearchResponse resp = getClient().execute(req);
        assertFalse("Response has error.", resp.hasError());
    }
}
