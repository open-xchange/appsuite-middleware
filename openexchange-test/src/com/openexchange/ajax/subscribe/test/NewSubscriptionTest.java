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
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.subscribe.actions.GetSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.GetSubscriptionResponse;
import com.openexchange.ajax.subscribe.actions.NewSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.NewSubscriptionResponse;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.SimSubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.Subscription;

/**
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class NewSubscriptionTest extends AbstractSubscriptionTest {

    @Test
    public void testShouldSurviveBasicOXMFSubscriptionCreation() throws OXException, IOException, JSONException {
        //setup
        FolderObject folder = ftm.generatePublicFolder("subscriptionTest", FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId());
        ftm.insertFolderOnServer(folder);

        DynamicFormDescription form = generateFormDescription();
        Subscription expected = generateOXMFSubscription(form);
        expected.setFolderId(String.valueOf(folder.getObjectID()));

        //new request
        NewSubscriptionRequest newReq = new NewSubscriptionRequest(expected, form);
        NewSubscriptionResponse newResp = getClient().execute(newReq);

        assertFalse("Should succeed creating the subscription: " + newResp.getException(), newResp.hasError());
        expected.setId(newResp.getId());

        //verify via get request
        SimSubscriptionSourceDiscoveryService discovery = new SimSubscriptionSourceDiscoveryService();
        discovery.addSource(expected.getSource());

        GetSubscriptionRequest getReq = new GetSubscriptionRequest(newResp.getId());
        GetSubscriptionResponse getResp = getClient().execute(getReq);

        Subscription actual = getResp.getSubscription(discovery);

        assertEquals("Should have same source ID", expected.getSource().getId(), actual.getSource().getId());
        assertEquals("Should have same ID", expected.getId(), actual.getId());
        assertEquals("Should have same user ID", expected.getUserId(), actual.getUserId());
        assertEquals("Should have the same URL", expected.getConfiguration().get("url"), actual.getConfiguration().get("url"));
        assertNotNull("Should still have an url configured", actual.getConfiguration().get("url"));
    }
}
