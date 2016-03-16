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
        } catch (final OXException e) {
            LOG.error("Could not delete all dependent subscriptions", e);
            return;
        }

        try {
            final List<Subscription> subscriptions = storage.getSubscriptions(context, folderId);
            for (final Subscription subscription : subscriptions) {
                storage.forgetSubscription(subscription);
            }
        } catch (final OXException e) {
            LOG.error("Could not delete all dependent subscriptions", e);
        }


    }

}
