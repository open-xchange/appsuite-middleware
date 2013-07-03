/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.push.ms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
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
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DelayPushQueue.class));

    /** The special poison object */
    private static final DelayedPushMsObject POISON = new DelayedPushMsObject(null, 0, 0);

    private final int delayDuration;
    private final int maxDelayDuration;
    private final PushMSDelayQueue delayQueue;
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
        delayQueue = new PushMSDelayQueue();
        this.publishTopic = publishTopic;
        this.delayDuration = delayDuration;
        this.maxDelayDuration = maxDelayDuration;
        isRunning = new AtomicBoolean(true);
        pollThread = new Thread(this, "DelayPushQueuePoller");
        pollThread.setName(this.getClass().getName());
        pollThread.start();
    }

    /**
     * Add a pushMsObject into the DealyQueue. If the pushMsObject is already contained within this queue its delay will be refreshed.
     *
     * @param pushMsObject the pushMsObject to add
     */
    public void add(final PushMsObject pushMsObject) {
        delayQueue.offerIfAbsentElseReschedule(new DelayedPushMsObject(pushMsObject, delayDuration, maxDelayDuration));
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
        final PushMSDelayQueue delayQueue = this.delayQueue;
        final List<DelayedPushMsObject> objects = new ArrayList<DelayedPushMsObject>(16);
        while (isRunning.get()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Awaiting push objects from DelayQueue with current size: " + delayQueue.size());
            }
            try {
                objects.clear();
                // Blocking wait for at least 1 DelayedPushMsObject to expire.
                final DelayedPushMsObject object = delayQueue.take();
                if (POISON == object) {
                    return;
                }
                objects.add(object);
                // Drain more if available
                delayQueue.drainTo(objects);
                for (final DelayedPushMsObject delayedPushMsObject : objects) {
                    if (POISON == delayedPushMsObject) {
                        // Reached poison element
                        return;
                    }
                    if (delayedPushMsObject != null) {
                        // Publish
                        publishTopic.publish(delayedPushMsObject.getPushObject().writePojo());
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Published delayed PushMsObject: " + delayedPushMsObject.getPushObject());
                        }
                    }
                }
            } catch (final Exception exc) {
                LOG.error(exc.getMessage(), exc);
            }
        }
    }

}
