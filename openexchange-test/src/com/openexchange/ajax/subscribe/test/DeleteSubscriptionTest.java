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
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.subscribe.actions.DeleteSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.DeleteSubscriptionResponse;
import com.openexchange.ajax.subscribe.actions.GetSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.GetSubscriptionResponse;
import com.openexchange.ajax.subscribe.actions.NewSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.NewSubscriptionResponse;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Autoboxing;
import com.openexchange.subscribe.Subscription;

/**
 * {@link DeleteSubscriptionTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class DeleteSubscriptionTest extends AbstractSubscriptionTest {

    public DeleteSubscriptionTest() {
        super();
    }

    @Test
    public void testDeleteOMXFSubscriptionShouldAlwaysWork() throws OXException, IOException, JSONException {
        //setup
        FolderObject folder = ftm.generatePublicFolder("subscriptionTest", FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId());
        ftm.insertFolderOnServer(folder);

        DynamicFormDescription form = generateFormDescription();
        Subscription expected = generateOXMFSubscription(form);
        expected.setFolderId(String.valueOf(folder.getObjectID()));

        //new request
        NewSubscriptionRequest newReq = new NewSubscriptionRequest(expected, form);
        NewSubscriptionResponse newResp = getClient().execute(newReq);

        assertFalse("Should succeed creating the subscription", newResp.hasError());
        expected.setId(newResp.getId());

        //delete subcription
        DeleteSubscriptionRequest delReq = new DeleteSubscriptionRequest(expected.getId());
        DeleteSubscriptionResponse delResp = getClient().execute(delReq);
        assertFalse("Should succeed deleting the subscription", delResp.hasError());

        //verify absense via get request
        GetSubscriptionRequest getReq = new GetSubscriptionRequest(newResp.getId());
        GetSubscriptionResponse getResp = getClient().execute(getReq);

        assertTrue("Should fail trying to get subcription afte deletion", getResp.hasError());
        assertEquals("Should return 1 in case of success", Autoboxing.I(1), delResp.getData());
    }
}
