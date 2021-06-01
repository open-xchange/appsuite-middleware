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

package com.openexchange.ajax.subscribe.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.subscribe.actions.ListSubscriptionsResponse;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.Subscription;

/**
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ListSubscriptionsTest extends AbstractSubscriptionTest {

    public ListSubscriptionsTest() {
        super();
    }

    @Test
    public void testShouldSurviveBasicOXMFSubscription() throws OXException, IOException, JSONException {
        FolderObject folder = createDefaultContactFolder();

        DynamicFormDescription formDescription = generateFormDescription();
        Subscription subscription = generateOXMFSubscription(formDescription);
        subscription.setFolderId(String.valueOf(folder.getObjectID()));

        subMgr.newAction(subscription);
        assertFalse("Precondition: Creation of subscription should work", subMgr.getLastResponse().hasError());

        List<String> columns = Arrays.asList("id", "folder", "source");
        List<Integer> ids = Arrays.asList(Integer.valueOf(subscription.getId()));
        JSONArray list = subMgr.listAction(ids, columns);

        ListSubscriptionsResponse listResp = (ListSubscriptionsResponse) subMgr.getLastResponse();
        assertFalse("List request should have worked flawlessly", listResp.hasError());

        assertEquals("Should only have one result", 1, list.length());
        JSONArray elements = list.getJSONArray(0);
        assertEquals("Should have three elements", 3, elements.length());
        assertEquals("Should return the same ID", subscription.getId(), elements.getInt(0));
        assertEquals("Should return the same folder", subscription.getFolderId(), elements.getString(1));
        assertEquals("Should return the same source ID", subscription.getSource().getId(), elements.getString(2));
    }

}
