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

package com.openexchange.subscribe.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.context.ContextService;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionStorage;


/**
 * {@link FolderCleanUpEventHandler}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class FolderCleanUpEventHandler implements EventHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderCleanUpEventHandler.class);
    private final ContextService contexts;
    private final SubscriptionStorage storage;
    private ServiceRegistration<EventHandler> registration;

    public FolderCleanUpEventHandler(final BundleContext context, final SubscriptionStorage storage, final ContextService contexts) {
        this.contexts = contexts;
        this.storage = storage;
        register(context);
    }

    private void register(final BundleContext context) {
        final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(EventConstants.EVENT_TOPIC, new String[] { "com/openexchange/groupware/folder/delete" });
        registration = context.registerService(EventHandler.class, this, serviceProperties);
    }

    public void close() {
        registration.unregister();
    }

    @Override
    public void handleEvent(final Event event) {
        final CommonEvent commonEvent = (CommonEvent) event.getProperty(CommonEvent.EVENT_KEY);
        final FolderObject actionObj = (FolderObject) commonEvent.getActionObj();

        // TODO: Special Handling for mail?
        final String folderId = String.valueOf(actionObj.getObjectID());
        Context context;
        try {
            context = contexts.getContext(commonEvent.getContextId());
        } catch (OXException e) {
            LOG.error("Could not delete all dependent subscriptions", e);
            return;
        }

        try {
            final List<Subscription> subscriptions = storage.getSubscriptions(context, folderId);
            for (final Subscription subscription : subscriptions) {
                storage.forgetSubscription(subscription);
            }
        } catch (OXException e) {
            LOG.error("Could not delete all dependent subscriptions", e);
        }


    }

}
