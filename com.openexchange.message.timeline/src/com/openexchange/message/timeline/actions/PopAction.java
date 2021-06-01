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

package com.openexchange.message.timeline.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.message.timeline.Message;
import com.openexchange.message.timeline.MessageTimelineManagement;
import com.openexchange.message.timeline.MessageTimelineRequest;
import com.openexchange.server.ServiceLookup;


/**
 * {@link PopAction} - The 'pop' action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PopAction extends AbstractMessageTimelineAction {

    /**
     * Initializes a new {@link PopAction}.
     */
    public PopAction(final ServiceLookup services, final Map<String, AbstractMessageTimelineAction> actions) {
        super(services, actions);
    }

    @Override
    protected AJAXRequestResult perform(final MessageTimelineRequest msgTimelineRequest) throws OXException, JSONException {
        // Check client identifier
        final String client = checkClient(msgTimelineRequest);

        // Get appropriate queue(s)
        final List<Message> messages = new ArrayList<Message>(16);
        if ("*".equals(client) || "all".equalsIgnoreCase(client)) {
            final List<BlockingQueue<Message>> queues = MessageTimelineManagement.getInstance().getQueuesFor(msgTimelineRequest.getSession());
            for (final BlockingQueue<Message> queue : queues) {
                queue.drainTo(messages);
            }
        } else {
            final BlockingQueue<Message> queue = MessageTimelineManagement.getInstance().getQueueFor(msgTimelineRequest.getSession(), client);
            queue.drainTo(messages);
        }

        // Sort according to time stamp
        Collections.sort(messages);

        // Output as JSON array
        final JSONArray jArray = new JSONArray(messages.size());
        for (final Message m : messages) {
            jArray.put(m.jsonValue);
        }
        return new AJAXRequestResult(jArray, "json");
    }

    @Override
    public String getAction() {
        return "pop";
    }

}
