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
import com.openexchange.ajax.subscribe.actions.UpdateSubscriptionResponse;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.SimSubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.Subscription;

/**
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class UpdateSubscriptionTest extends AbstractSubscriptionTest {

    public UpdateSubscriptionTest() {
        super();
    }

    @Test
    public void testUpdatingAnExistingValueWithinAnOMXFSubscription() throws OXException, IOException, JSONException {
        FolderObject folder = createDefaultContactFolder();

        DynamicFormDescription formDescription = generateFormDescription();
        Subscription expected = generateOXMFSubscription(formDescription);
        expected.setFolderId(folder.getObjectID());
        subMgr.setFormDescription(formDescription);
        SimSubscriptionSourceDiscoveryService discovery = new SimSubscriptionSourceDiscoveryService();
        discovery.addSource(expected.getSource());
        subMgr.setSubscriptionSourceDiscoveryService(discovery);

        //create as pre-requisite
        subMgr.newAction(expected);
        assertFalse("Precondition: Creation of subscription should work", subMgr.getLastResponse().hasError());

        //update
        expected.getConfiguration().put("url", "http://ox.open-exchange.com/2");
        subMgr.updateAction(expected);
        assertTrue("Should return 1 if update worked", ((UpdateSubscriptionResponse) subMgr.getLastResponse()).wasSuccessful());

        //verify via get

        Subscription actual = subMgr.getAction(expected.getId());

        assertEquals("Should contain the same url in the configuration", expected.getConfiguration().get("url"), actual.getConfiguration().get("url"));
    }

    @Test
    public void testUpdatingAnOMXFSubscriptionWithANewValue() throws OXException, IOException, JSONException {
        FolderObject folder = createDefaultContactFolder();

        DynamicFormDescription formDescription = generateFormDescription();
        Subscription expected = generateOXMFSubscription(formDescription);
        expected.setFolderId(folder.getObjectID());
        subMgr.setFormDescription(formDescription);
        SimSubscriptionSourceDiscoveryService discovery = new SimSubscriptionSourceDiscoveryService();
        discovery.addSource(expected.getSource());
        subMgr.setSubscriptionSourceDiscoveryService(discovery);

        //create as pre-requisite
        subMgr.newAction(expected);
        assertFalse("Precondition: Creation of subscription should work", subMgr.getLastResponse().hasError());

        //update
        expected.getConfiguration().put("username", "Elvis Aaron Presley");
        subMgr.updateAction(expected);
        assertTrue("Should return 1 if update worked", ((UpdateSubscriptionResponse) subMgr.getLastResponse()).wasSuccessful());

        //verify via get

        Subscription actual = subMgr.getAction(expected.getId());

        assertEquals("Should not take a value that was not defined in the form description", null, actual.getConfiguration().get("username"));
    }

}
