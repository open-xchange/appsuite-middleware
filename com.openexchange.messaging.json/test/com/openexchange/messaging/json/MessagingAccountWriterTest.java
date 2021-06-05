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

package com.openexchange.messaging.json;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.json.JSONAssertion.assertValidates;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.json.JSONAssertion;
import com.openexchange.messaging.SimMessagingAccount;
import com.openexchange.messaging.SimMessagingService;

/**
 * {@link MessagingAccountWriterTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingAccountWriterTest {

    @Test
    public void testWriteAccount() throws JSONException {
        final SimMessagingAccount account = new SimMessagingAccount();
        account.setId(12);
        account.setDisplayName("My Twitter Account");

        final Map<String, Object> configuration = new HashMap<String, Object>();
        configuration.put("inputField", "My Input Value");
        account.setConfiguration(configuration);

        final SimMessagingService messagingService = new SimMessagingService();

        final DynamicFormDescription description = new DynamicFormDescription().add(FormElement.input("inputField", "My cool config option"));
        messagingService.setFormDescription(description);

        messagingService.setId("com.openexchange.twitter");
        account.setMessagingService(messagingService);

        final JSONAssertion assertion = new JSONAssertion()
            .isObject()
                .hasKey("id").withValue(I(12))
                .hasKey("displayName").withValue("My Twitter Account")
                .hasKey("messagingService").withValue("com.openexchange.twitter")
                .hasKey("configuration").withValueObject()
                    .hasKey("inputField").withValue("My Input Value")
                .objectEnds()
            .objectEnds()
        ;

        final MessagingAccountWriter writer = new MessagingAccountWriter();

        final JSONObject object = writer.write(account);

        assertValidates(assertion, object);

    }
}
