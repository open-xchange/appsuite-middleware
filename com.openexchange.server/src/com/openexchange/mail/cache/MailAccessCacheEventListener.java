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

package com.openexchange.mail.cache;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.event.impl.osgi.EventHandlerRegistration;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.sessiond.SessiondEventConstants;

/**
 * {@link MailAccessCacheEventListener} - Listens for removed session containers to dispose its cached mail access instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccessCacheEventListener implements EventHandlerRegistration {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailAccessCacheEventListener.class);

    private volatile ServiceRegistration<EventHandler> serviceRegistration;

    /**
     * Initializes a new {@link MailAccessCacheEventListener}.
     */
    public MailAccessCacheEventListener() {
        super();
    }

    @Override
    public void handleEvent(Event event) {
        final String topic = event.getTopic();
        if (SessiondEventConstants.TOPIC_LAST_SESSION.equals(topic)) {
            Integer contextId = (Integer) event.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
            if (null != contextId) {
                Integer userId = (Integer) event.getProperty(SessiondEventConstants.PROP_USER_ID);
                if (null != userId) {
                    IMailAccessCache mac = MailAccess.optMailAccessCache();
                    if (null != mac) {
                        try {
                            mac.clearUserEntries(userId.intValue(), contextId.intValue());
                            // AttachmentTokenRegistry.getInstance().dropFor(session);
                        } catch (OXException e) {
                            LOG.error("Unable to clear cached mail access for user {} in context {}", userId, contextId, e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void registerService(BundleContext context) {
        final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(EventConstants.EVENT_TOPIC, new String[] { SessiondEventConstants.TOPIC_LAST_SESSION });
        serviceRegistration = context.registerService(EventHandler.class, this, serviceProperties);
    }

    @Override
    public void unregisterService() {
        final ServiceRegistration<EventHandler> serviceRegistration = this.serviceRegistration;
        if (null != serviceRegistration) {
            serviceRegistration.unregister();
            this.serviceRegistration = null;
        }
    }
}
