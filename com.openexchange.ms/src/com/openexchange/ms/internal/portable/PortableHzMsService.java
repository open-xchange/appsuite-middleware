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

import java.util.concurrent.ConcurrentMap;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.nio.serialization.Portable;
import com.openexchange.ms.PortableMsService;
import com.openexchange.ms.Topic;
import com.openexchange.ms.internal.AbstractHzResource;

/**
 * {@link PortableHzMsService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class PortableHzMsService extends AbstractHzResource implements PortableMsService {

    private final HazelcastInstance hz;
    private final ConcurrentMap<String, Topic<?>> topics;

    /**
     * Initializes a new {@link PortableHzMsService}.
     *
     * @param hz The underlying hazelcast instance
     */
    public PortableHzMsService(HazelcastInstance hz) {
        super();
        this.hz = hz;
        topics = new NonBlockingHashMap<String, Topic<?>>(16);
    }

    @Override
    public <P extends Portable> Topic<P> getTopic(final String name) {
        Topic<P> topic = (Topic<P>) topics.get(name);
        if (null == topic) {
            try {
                PortableHzTopic<P> hzTopic = new PortableHzTopic<P>(name, hz);
                topic = (Topic<P>) topics.putIfAbsent(name, hzTopic);
                if (null == topic) {
                    topic = hzTopic;
                }
            } catch (HazelcastInstanceNotActiveException e) {
                throw handleNotActiveException(e);
            }
        }
        return topic;
    }

}
