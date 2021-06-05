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

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractSubscriptionTest extends AbstractPubSubTest {

    protected SubscriptionTestManager subMgr;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        subMgr = new SubscriptionTestManager(getClient());
    }

    @Override
    protected Subscription generateOXMFSubscription(DynamicFormDescription formDescription, String folderID) {
        Subscription sub = generateOXMFSubscription(formDescription);
        sub.setFolderId(folderID);
        return sub;
    }

    @Override
    protected Subscription generateOXMFSubscription(DynamicFormDescription formDescription) {
        Subscription subscription = new Subscription();

        subscription.setDisplayName("mySubscription");

        SubscriptionSource source = new SubscriptionSource();
        source.setId("com.openexchange.subscribe.microformats.contacts.http");
        source.setFormDescription(formDescription);
        subscription.setSource(source);

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("url", "https://ox6.open-xchange.com");
        subscription.setConfiguration(config);

        return subscription;
    }

    public DynamicFormDescription generateFormDescription() {
        DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("url", "URL", true, null));
        return form;
    }

}
