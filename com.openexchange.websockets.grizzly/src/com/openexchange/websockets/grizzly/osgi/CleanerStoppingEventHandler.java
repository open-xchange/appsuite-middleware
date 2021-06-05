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

package com.openexchange.websockets.grizzly.osgi;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.websockets.grizzly.remote.RemoteWebSocketDistributor;


/**
 * {@link CleanerStoppingEventHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class CleanerStoppingEventHandler implements EventHandler {

    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CleanerStoppingEventHandler.class);

    private final RemoteWebSocketDistributor distributor;

    /**
     * Initializes a new {@link CleanerStoppingEventHandler}.
     */
    public CleanerStoppingEventHandler(RemoteWebSocketDistributor distributor) {
        super();
        this.distributor = distributor;
    }

    @Override
    public void handleEvent(final Event event) {
        if (false == SessiondEventConstants.TOPIC_LAST_SESSION.equals(event.getTopic())) {
            return;
        }

        ThreadPoolService threadPool = ThreadPools.getThreadPool();
        if (null == threadPool) {
            doHandleEvent(event);
        } else {
            AbstractTask<Void> t = new AbstractTask<Void>() {

                @Override
                public Void call() throws Exception {
                    try {
                        doHandleEvent(event);
                    } catch (Exception e) {
                        LOG.warn("Handling event {} failed.", event.getTopic(), e);
                    }
                    return null;
                }
            };
            threadPool.submit(t, CallerRunsBehavior.<Void> getInstance());
        }
    }

    /**
     * Handles given event.
     *
     * @param lastSessionEvent The event
     */
    protected void doHandleEvent(Event lastSessionEvent) {
        Integer contextId = (Integer) lastSessionEvent.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
        if (null != contextId) {
            Integer userId = (Integer) lastSessionEvent.getProperty(SessiondEventConstants.PROP_USER_ID);
            if (null != userId) {
                distributor.stopCleanerTaskFor(userId.intValue(), contextId.intValue());
            }
        }
    }

}
