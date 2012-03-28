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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.service.messaging.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.service.messaging.Message;
import com.openexchange.service.messaging.MessageHandler;

/**
 * {@link DelegateMessageHandler} - A {@link ServiceTracker} for event admin service and a {@link MessageHandler} which delegates incoming
 * messages as events to event admin service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DelegateMessageHandler extends ServiceTracker implements MessageHandler {

    private final AtomicReference<EventAdmin> eventAdminRef;

    /**
     * Initializes a new {@link DelegateMessageHandler}.
     *
     * @param context The bundle context
     */
    public DelegateMessageHandler(final BundleContext context) {
        super(context, EventAdmin.class.getName(), null);
        eventAdminRef = new AtomicReference<EventAdmin>();
    }

    @Override
    public void handleMessage(final Message message) {
        final EventAdmin eventAdmin = eventAdminRef.get();
        if (null == eventAdmin) {
            return;
        }
        final Map<String, Serializable> properties = message.getProperties();
        final Map<String, Object> dict = new HashMap<String, Object>(properties.size());
        for (final Entry<String, Serializable> entry : properties.entrySet()) {
            dict.put(entry.getKey(), entry.getValue());
        }
        dict.put(Constants.PROPERTY_EVENT_ORIGIN, Constants.ORIGIN_REMOTE);
        eventAdmin.postEvent(new Event(message.getTopic(), dict));
    }

    @Override
    public Object addingService(final ServiceReference reference) {
        final EventAdmin eventAdmin = (EventAdmin) context.getService(reference);
        if (eventAdminRef.compareAndSet(null, eventAdmin)) {
            return eventAdmin;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference reference, final Object service) {
        // Nope
    }

    @Override
    public void removedService(final ServiceReference reference, final Object service) {
        if (null == service) {
            return;
        }
        eventAdminRef.compareAndSet((EventAdmin) service, null);
        context.ungetService(reference);
    }
}
