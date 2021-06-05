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

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.json.FormDescriptionWriter;
import com.openexchange.i18n.Translator;
import com.openexchange.messaging.MessagingAction;
import com.openexchange.messaging.MessagingService;

/**
 * Renders a given MessagingService as its JSON representation. This also contains a dynamic form description.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingServiceWriter {

    private static final String FORM_DESCRIPTION = "formDescription";
    private static final String MESSAGE_ACTIONS = "messagingActions";
    private static final String DISPLAY_NAME = "displayName";
    private static final String ID = "id";

    private final Translator translator;

    public MessagingServiceWriter(final Translator translator) {
        this.translator = translator;
    }

    public JSONObject write(final MessagingService messagingService) throws JSONException {
        final JSONObject object = new JSONObject();
        object.put(ID, messagingService.getId());
        object.put(DISPLAY_NAME, messagingService.getDisplayName());
        object.put(MESSAGE_ACTIONS, writeCapabilities(messagingService.getMessageActions()));
        if (null != messagingService.getFormDescription()) {
            object.put(FORM_DESCRIPTION, new FormDescriptionWriter(translator).write(messagingService.getFormDescription()));
        }
        return object;
    }

    private JSONArray writeCapabilities(final List<MessagingAction> capabilities) {
        final JSONArray array = new JSONArray();
        if (capabilities == null) {
            return array;
        }
        for (final MessagingAction action : capabilities) {
            array.put(action.getName());
        }
        return array;
    }

}
