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

import static com.openexchange.json.JSONAssertion.assertValidates;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.i18n.Translator;
import com.openexchange.json.JSONAssertion;
import com.openexchange.messaging.MessagingAction;
import com.openexchange.messaging.SimMessagingService;

/**
 * {@link MessagingServiceWriterTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingServiceWriterTest {
         @Test
     public void testSimpleWrite() throws JSONException {
        final SimMessagingService messagingService = new SimMessagingService();

        final List<MessagingAction> actions = new LinkedList<MessagingAction>();
        actions.add(new MessagingAction("powerize!", MessagingAction.Type.NONE));
        actions.add(new MessagingAction("send", MessagingAction.Type.MESSAGE));
        actions.add(new MessagingAction("retweet", MessagingAction.Type.STORAGE, "send"));
        actions.add(new MessagingAction("reply", MessagingAction.Type.STORAGE, "send"));


        messagingService.setId("com.openexchange.messaging.twitter");
        messagingService.setDisplayName("Twitter");
        messagingService.setMessageActions(actions);
        messagingService.setFormDescription(new DynamicFormDescription());

        final JSONAssertion assertion = new JSONAssertion().isObject()
            .hasKey("id").withValue("com.openexchange.messaging.twitter")
            .hasKey("displayName").withValue("Twitter")
            .hasKey("messagingActions").withValueArray().withValues("powerize!", "send", "retweet", "reply").objectEnds()
            .hasKey("formDescription").withValueArray()
            .objectEnds();


        final JSONObject messagingServiceJSON = new MessagingServiceWriter(Translator.EMPTY).write(messagingService);

        assertValidates(assertion, messagingServiceJSON);

    }
}
