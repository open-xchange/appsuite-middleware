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

package com.openexchange.config.cascade.user.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.config.cascade.user.UserConfigProvider;
import com.openexchange.config.cascade.user.cache.CacheInvalidator;
import com.openexchange.context.ContextService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
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
        // Register config provider for "user" scope
        Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
        properties.put("scope", ConfigViewScope.USER.getScopeName());
        registerService(ConfigProviderService.class, new UserConfigProvider(this), properties);

        // Register event handler alongside with a cache invalidator
        final CacheInvalidator invalidator = new CacheInvalidator(context);
        {
            EventHandler eventHandler = new EventHandler() {

                @Override
                public void handleEvent(final Event event) {
                    String topic = event.getTopic();
                    if (SessiondEventConstants.TOPIC_LAST_SESSION.equals(topic)) {
                        Integer contextId = (Integer) event.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
                        if (null != contextId) {
                            Integer userId = (Integer) event.getProperty(SessiondEventConstants.PROP_USER_ID);
                            if (null != userId) {
                                invalidator.invalidateUser(userId.intValue(), contextId.intValue());
                            }
                        }
                    } if (SessiondEventConstants.TOPIC_LAST_SESSION_CONTEXT.equals(topic)) {
                        Integer contextId = (Integer) event.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
                        if (null != contextId) {
                            invalidator.invalidateContext(contextId.intValue());
                        }
                    }
                }
            };

            properties = new Hashtable<String, Object>(2);
            properties.put(EventConstants.EVENT_TOPIC, new String[] { SessiondEventConstants.TOPIC_LAST_SESSION, SessiondEventConstants.TOPIC_LAST_SESSION_CONTEXT });
            registerService(EventHandler.class, eventHandler, properties);
        }

        track(CacheEventService.class, invalidator);
        openTrackers();
    }

}
