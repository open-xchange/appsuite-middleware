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

package com.openexchange.push;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.event.CommonEvent;
import com.openexchange.event.EventFactoryService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.push.internal.ServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link PushUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
/**
 * {@link PushUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushUtility {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(PushUtility.class));

    /**
     * Initializes a new {@link PushUtility}.
     */
    private PushUtility() {
        super();
    }

    /**
     * Triggers the OSGi event system and posts a new event for new mails in given folder.
     *
     * @param folder The folder identifier; including account information
     * @param session The session providing needed user data
     * @throws OXException If posting event fails
     */
    public static void triggerOSGiEvent(final String folder, final Session session) throws OXException {
        try {
            final EventAdmin eventAdmin = ServiceRegistry.getInstance().getService(EventAdmin.class, true);
            final int contextId = session.getContextId();
            final int userId = session.getUserId();
            /*
             * Create event's properties
             */
            final Dictionary<String, Object> properties = new Hashtable<String, Object>(4);
            properties.put(PushEventConstants.PROPERTY_CONTEXT, Integer.valueOf(contextId));
            properties.put(PushEventConstants.PROPERTY_USER, Integer.valueOf(userId));
            properties.put(PushEventConstants.PROPERTY_SESSION, session);
            properties.put(PushEventConstants.PROPERTY_FOLDER, folder);
            /*
             * Add common event to properties for remote distribution via UDP-push
             */
            {
                final EventFactoryService eventFactoryService = ServiceRegistry.getInstance().getService(EventFactoryService.class, true);
                final CommonEvent commonEvent =
                    eventFactoryService.newCommonEvent(
                        contextId,
                        userId,
                        Collections.<Integer, Set<Integer>> emptyMap(),
                        CommonEvent.INSERT,
                        Types.EMAIL,
                        null,
                        null,
                        null,
                        null,
                        session);
                properties.put(CommonEvent.EVENT_KEY, commonEvent);
            }
            /*
             * Create event with push topic
             */
            final Event event = new Event(PushEventConstants.TOPIC, properties);
            /*
             * Finally post it
             */
            eventAdmin.postEvent(event);
            if (LOG.isDebugEnabled()) {
                LOG.debug(new StringBuilder(64).append("Notified new mails in folder \"").append(folder).append("\" for user ").append(
                    userId).append(" in context ").append(contextId).toString());
            }
        } catch (final OXException e) {
            throw e;
        }
    }

    /**
     * Checks if specified client identifier is allowed according to white-list filter.
     *
     * @param client The client identifier
     * @return <code>true</code> if client identifier is allowed; otherwise <code>false</code>
     */
    public static final boolean allowedClient(final String client) {
        final PushClientWhitelist clientWhitelist = PushClientWhitelist.getInstance();
        return clientWhitelist.isEmpty() || clientWhitelist.isAllowed(client);
    }
}
