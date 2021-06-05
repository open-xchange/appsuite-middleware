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
import java.util.UUID;
import org.slf4j.Logger;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.nio.serialization.HazelcastSerializationException;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.topic.ITopic;
import com.openexchange.ms.MessageListener;
import com.openexchange.ms.internal.AbstractHzTopic;

/**
 * {@link PortableHzTopic}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class PortableHzTopic<P extends Portable> extends AbstractHzTopic<P> {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PortableHzTopic.class);

    private final ITopic<PortableMessage<P>> hzTopic;

    /**
     * Initializes a new {@link PortableHzTopic}.
     *
     * @param name The topic's name
     * @param hz The hazelcast instance
     */
    public PortableHzTopic(String name, HazelcastInstance hz) {
        super(name, hz);
        this.hzTopic = hz.getTopic(name);
    }

    @Override
    protected UUID registerListener(MessageListener<P> listener, String senderID) {
        try {
            return hzTopic.addMessageListener(new PortableHzMessageListener<P>(listener, senderID));
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    protected boolean unregisterListener(UUID registrationID) {
        try {
            return hzTopic.removeMessageListener(registrationID);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    protected void publish(String senderId, P message) {
        try {
            hzTopic.publish(new PortableMessage<P>(senderId, message));
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (HazelcastSerializationException e) {
            // Could not create a PortableMessage object
            LOGGER.warn("Could no create a {} instance from message of type {}. Please ensure proper start levels for active OSGi bundles.", PortableMessage.class.getSimpleName(), message.getClass().getName(), e);
        }
    }

    @Override
    protected void publish(String senderId, List<P> messages) {
        if (null != messages && !messages.isEmpty()) {
            try {
                hzTopic.publish(new PortableMessage<P>(senderId, messages));
            } catch (HazelcastInstanceNotActiveException e) {
                throw handleNotActiveException(e);
            } catch (HazelcastSerializationException e) {
                // Could not create a PortableMessage object
                LOGGER.warn("Could no create a {} instance from message of type {}", PortableMessage.class.getSimpleName(), messages.get(0).getClass().getName(), e);
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (null != hzTopic) {
            hzTopic.destroy();
        }
    }

}
