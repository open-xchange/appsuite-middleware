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

package com.openexchange.ajax.chronos.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.chronos.EventField;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.FindActiveFacet;
import com.openexchange.testing.httpclient.models.FindActiveFacetFilter;
import com.openexchange.testing.httpclient.models.FindQueryBody;
import com.openexchange.testing.httpclient.models.FindQueryResponse;
import com.openexchange.testing.httpclient.models.FindQueryResponseData;

/**
 *
 * {@link Bug13625Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class Bug13625Test extends AbstractChronosTest {

    public Bug13625Test() {
        super();
    }

    @Test
    public void testBug13625() throws Exception {
        EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "testBug13625", folderId);
        event.setCategories(Arrays.asList("eins", "zwei"));
        EventData createEvent = eventManager.createEvent(event, true);
        FindQueryBody body = new FindQueryBody();

        FindActiveFacet folderFacet = new FindActiveFacet();
        folderFacet.facet("folder");
        folderFacet.value(folderId);
        body.addFacetsItem(folderFacet);

        FindActiveFacet globalFacet = new FindActiveFacet();
        globalFacet.facet("global");
        globalFacet.value("global:eins");
        FindActiveFacetFilter filter = new FindActiveFacetFilter();
        filter.addFieldsItem("global");
        filter.setQueries(java.util.Collections.singletonList("eins"));
        globalFacet.filter(filter);
        body.addFacetsItem(globalFacet);

        FindQueryResponse response = defaultUserApi.getFindApi().doQuery("calendar", body, null, EventField.ID.name().toLowerCase());
        assertNull(response.getErrorDesc(), response.getError());
        assertNotNull(response.getData());

        FindQueryResponseData data = response.getData();
        assertEquals(1, data.getNumFound().intValue());

        @SuppressWarnings("unchecked") ArrayList<HashMap<String, Object>> results = (ArrayList<HashMap<String, Object>>) data.getResults();
        assertEquals(createEvent.getId(), results.get(0).get(EventField.ID.name().toLowerCase()));

    }

}
