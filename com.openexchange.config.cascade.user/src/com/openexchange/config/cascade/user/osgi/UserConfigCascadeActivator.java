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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.config.cascade.user.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.user.UserConfigProvider;
import com.openexchange.config.cascade.user.cache.CacheInvalidator;
import com.openexchange.context.ContextService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SessiondServiceExtended;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.user.UserService;

/**
 * {@link UserConfigCascadeActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UserConfigCascadeActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { UserService.class, ContextService.class, CacheService.class, SessiondService.class, ThreadPoolService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final UserConfigProvider provider = new UserConfigProvider(this);
        final CacheInvalidator invalidator = new CacheInvalidator(context);

        final Hashtable<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put("scope", "user");
        registerService(ConfigProviderService.class, provider, properties);

        {
            final EventHandler eventHandler = new EventHandler() {

                @Override
                public void handleEvent(final Event event) {
                    final String topic = event.getTopic();
                    if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
                        handleDroppedSession((Session) event.getProperty(SessiondEventConstants.PROP_SESSION));
                    } else if (SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic) || SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic)) {
                        @SuppressWarnings("unchecked")
                        final Map<String, Session> map = (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                        for (final Session session : map.values()) {
                            handleDroppedSession(session);
                        }
                    }
                }

                private void handleDroppedSession(final Session session) {
                    final SessiondService sessiondService = getService(SessiondService.class);
                    final int contextId = session.getContextId();
                    if (null == sessiondService.getAnyActiveSessionForUser(session.getUserId(), contextId)) {
                        invalidator.invalidateUser(session.getUserId(), session.getContextId());
                    }
                    if ((sessiondService instanceof SessiondServiceExtended) && !((SessiondServiceExtended) sessiondService).hasForContext(contextId)) {
                        invalidator.invalidateContext(contextId);
                    }
                }
            };
            final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
            dict.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
            registerService(EventHandler.class, eventHandler, dict);
        }

        track(CacheEventService.class, invalidator);
        openTrackers();
    }

}
