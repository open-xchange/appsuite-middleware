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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
