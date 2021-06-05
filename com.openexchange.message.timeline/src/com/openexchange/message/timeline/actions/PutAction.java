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

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import org.json.JSONException;
import org.json.JSONValue;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.message.timeline.Message;
import com.openexchange.message.timeline.MessageTimelineExceptionCodes;
import com.openexchange.message.timeline.MessageTimelineManagement;
import com.openexchange.message.timeline.MessageTimelineRequest;
import com.openexchange.server.ServiceLookup;


/**
 * {@link PutAction} - The 'put' action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PutAction extends AbstractMessageTimelineAction {

    /**
     * Initializes a new {@link PutAction}.
     */
    public PutAction(final ServiceLookup services, final Map<String, AbstractMessageTimelineAction> actions) {
        super(services, actions);
    }

    @Override
    protected AJAXRequestResult perform(final MessageTimelineRequest msgTimelineRequest) throws OXException, JSONException {
        // Check client identifier
        final String client = checkClient(msgTimelineRequest.getSession());

        // Get JSON object to store
        final JSONValue toStore = (JSONValue) msgTimelineRequest.getRequestData().getData();

        // Get appropriate queue
        final BlockingQueue<Message> queue = MessageTimelineManagement.getInstance().getQueueFor(msgTimelineRequest.getSession(), client);

        // Put to queue (if possible)
        final boolean offered = queue.offer(new Message(toStore));

        if (!offered) {
            // Boundary exceeded
            throw MessageTimelineExceptionCodes.NO_MORE_MSGS.create(client);
        }

        // Signal positive result
        return new AJAXRequestResult(Boolean.TRUE, "native");
    }

    @Override
    public String getAction() {
        return "put";
    }

}
