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

package com.openexchange.push.ms;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.java.BufferingQueue;
import com.openexchange.ms.Topic;

/**
 * {@link DelayPushQueue}- used if the Push of Objects should be delayed by a certain amount of time. Use case for this delay: Unlike E-Mails
 * other PIM Objects shouldn't be pushed immediately because they can be changed within a short timeframe to adjust details or other objects
 * might be created in the same folder which would lead to yet another push event. Introduced to stay compatible with old {c.o}.push.udp
 * implementation.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class DelayPushQueue implements Runnable {

    /** The logger */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DelayPushQueue.class);

    /** The special poison object */
    private static final PushMsObject POISON = new PushMsObject(0, 0, 0, null, false, 0, null);

    private final BufferingQueue<PushMsObject> delayQueue;
    private final Thread pollThread;
    private final AtomicBoolean isRunning;
    private final Topic<Map<String, Object>> publishTopic;

    /**
     * Initializes a new {@link DelayPushQueue}.
     *
     * @param publishTopic the publish topic used to finally publish the pushMsObjects in this DelayQueue.
     * @param delayDuration the default delay time for an object in this DelayQueue, gets refreshed when objects in the same folder are
     *            updated within the delayDuration
     * @param maxDelayDuration the maximum time an object can be in this DelayQueue before being finally published.
     */
    public DelayPushQueue(final Topic<Map<String, Object>> publishTopic, final int delayDuration, final int maxDelayDuration) {
        super();
        delayQueue = new BufferingQueue<PushMsObject>(delayDuration, maxDelayDuration);
        this.publishTopic = publishTopic;
        isRunning = new AtomicBoolean(true);
        pollThread = new Thread(this, "DelayPushQueuePoller");
        pollThread.setName(this.getClass().getName());
    }

    /**
     * Starts the polling thread.
     *
     * @return This instance
     */
    public DelayPushQueue start() {
        pollThread.start();
        return this;
    }

    /**
     * Add a pushMsObject into the DealyQueue. If the pushMsObject is already contained within this queue its delay will be refreshed.
     *
     * @param pushMsObject the pushMsObject to add
     */
    public void add(final PushMsObject pushMsObject, final boolean immediate) {
        if (immediate) {
            delayQueue.offerOrReplaceImmediately(pushMsObject);
        } else {
            delayQueue.offerIfAbsentElseReset(pushMsObject);
        }
    }

    /**
     * Closes this <code>DelayPushQueue</code>.
     */
    public void close() {
        isRunning.set(false);
        // Feed poison element to enforce quit
        delayQueue.put(POISON);
        // Clear rest
        delayQueue.clear();
    }

    @Override
    public void run() {
        final BufferingQueue<PushMsObject> delayQueue = this.delayQueue;
        final List<PushMsObject> objects = new ArrayList<PushMsObject>(16);
        while (isRunning.get()) {
            LOG.debug("Awaiting push objects from DelayQueue with current size: {}", I(delayQueue.size()));
            try {
                objects.clear();
                // Blocking wait for at least 1 DelayedPushMsObject to expire.
                final PushMsObject object = delayQueue.take();
                if (POISON == object) {
                    return;
                }
                objects.add(object);
                // Drain more if available
                delayQueue.drainTo(objects);
                for (PushMsObject delayedPushMsObject : objects) {
                    if (POISON == delayedPushMsObject) {
                        // Reached poison element
                        return;
                    }
                    if (delayedPushMsObject != null) {
                        // Publish
                        publishTopic.publish(delayedPushMsObject.writePojo());
                        LOG.debug("Published delayed PushMsObject: {}", delayedPushMsObject);
                    }
                }
            } catch (Exception exc) {
                LOG.error("", exc);
            }
        }
    }

}
