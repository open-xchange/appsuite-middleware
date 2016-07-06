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

package com.openexchange.mail.cache;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.event.impl.osgi.EventHandlerRegistration;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.session.Session;
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
    public void handleEvent(final Event event) {
        final String topic = event.getTopic();
        if (SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic) || SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic)) {
            final @SuppressWarnings("unchecked") Map<String, Session> sessions = (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
            IMailAccessCache mac = MailAccess.optMailAccessCache();
            if (null != mac) {
                for (final Session session : sessions.values()) {
                    if (!session.isTransient()) {
                        try {
                            mac.clearUserEntries(session);
                            // AttachmentTokenRegistry.getInstance().dropFor(session);
                        } catch (final OXException e) {
                            LOG.error("Unable to clear cached mail access for session: {}", session.getSessionID(), e);
                        }
                    }
                }
            }
        } else if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
            final Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
            if (session.isTransient()) {
                return;
            }
            IMailAccessCache mac = MailAccess.optMailAccessCache();
            if (null != mac) {
                try {
                    mac.clearUserEntries(session);
                    // AttachmentTokenRegistry.getInstance().dropFor(session);
                } catch (final OXException e) {
                    LOG.error("Unable to clear cached mail access for session: {}", session.getSessionID(), e);
                }
            }
        }
    }

    @Override
    public void registerService(final BundleContext context) {
        final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(EventConstants.EVENT_TOPIC, new String[] {
            SessiondEventConstants.TOPIC_REMOVE_DATA, SessiondEventConstants.TOPIC_REMOVE_CONTAINER,
            SessiondEventConstants.TOPIC_REMOVE_SESSION });
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
