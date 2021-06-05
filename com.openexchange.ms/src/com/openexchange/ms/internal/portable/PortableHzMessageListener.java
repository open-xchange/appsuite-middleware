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

package com.openexchange.ms.internal.portable;

import java.util.List;
import com.hazelcast.nio.serialization.Portable;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageListener;

/**
 * {@link PortableHzMessageListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PortableHzMessageListener<P extends Portable> implements com.hazelcast.topic.MessageListener<PortableMessage<P>> {

    private final MessageListener<P> listener;
    private final String senderID;

    /**
     * Initializes a new {@link PortableHzMessageListener}.
     *
     * @param listener The parent message listener
     * @param senderID The listener's sender ID
     */
    protected PortableHzMessageListener(MessageListener<P> listener, String senderID) {
        super();
        this.listener = listener;
        this.senderID = senderID;
    }

    @Override
    public void onMessage(com.hazelcast.topic.Message<PortableMessage<P>> message) {
        PortableMessage<P> messageData = message.getMessageObject();
        if (null != messageData) {
            List<P> messagePayload = messageData.getMessagePayload();
            if (null != messagePayload && 0 < messagePayload.size()) {
                String name = message.getSource().toString();
                String senderID = messageData.getSenderID();
                for (P payload : messagePayload) {
                    listener.onMessage(new Message<P>(name, senderID, payload, !this.senderID.equals(senderID)));
                }
            }
        }
    }

}
