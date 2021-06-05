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

package com.openexchange.ajax.chronos.schedjoules;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.sql.Date;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.FindFactory;
import com.openexchange.testing.httpclient.models.FindQueryBody;
import com.openexchange.testing.httpclient.models.FindQueryResponse;
import com.openexchange.testing.httpclient.models.FindQueryResponseData;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.modules.FindApi;

/**
 * {@link BasicSchedJoulesSearchAwareTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BasicSchedJoulesSearchAwareTest extends AbstractSchedJoulesProviderTest {

    private static final String DEFAULT_COLUMNS = "1,2,20,101,200,201,202,206,207,209,212,213,214,215,216,220,221,222,224,227,400,401,402";

    /**
     * Initialises a new {@link BasicSchedJoulesSearchAwareTest}.
     *
     * @param providerId
     */
    public BasicSchedJoulesSearchAwareTest() {
        super();
    }

    /**
     * Tests a simple search with default fields
     */
    @Test
    public void testSimpleSearch() throws Exception {
        FolderData folderData = createFolder(CALENDAR_ONE, "testSimpleSearch");
        eventManager.getAllEvents(new Date(0), new Date(1516370400), false, folderData.getId());

        FindQueryBody queryBody = FindFactory.createFindBody("Full", folderData.getId());

        FindApi findApi = defaultUserApi.getFindApi();
        FindQueryResponse response = findApi.doQuery("calendar", queryBody, DEFAULT_COLUMNS, null);
        assertNull(response.getErrorDesc(), response.getError());
        FindQueryResponseData responseData = response.getData();
        assertNotNull(responseData);
        assertTrue("No results found", responseData.getSize().intValue() > 0);
    }

    /**
     * Tests a simple search with defined fields
     */
    @Test
    public void testSearchWithFields() throws Exception {
        FolderData folderData = createFolder(CALENDAR_ONE, "testSearchWithFields");
        eventManager.getAllEvents(new Date(0), new Date(1516370400), false, folderData.getId());

        FindQueryBody queryBody = FindFactory.createFindBody("Full", folderData.getId());

        FindApi findApi = defaultUserApi.getFindApi();
        FindQueryResponse response = findApi.doQuery("calendar", queryBody, "400", null);
        assertNull(response.getErrorDesc(), response.getError());
        FindQueryResponseData responseData = response.getData();
        assertNotNull(responseData);
        assertTrue("No results found", responseData.getSize().intValue() > 0);
    }
}
