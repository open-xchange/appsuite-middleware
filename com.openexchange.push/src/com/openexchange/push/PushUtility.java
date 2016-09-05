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

package com.openexchange.push;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.event.CommonEvent;
import com.openexchange.event.EventFactoryService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.push.osgi.Services;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushUtility.class);

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
     *
     * @throws OXException If posting event fails
     */
    public static void triggerOSGiEvent(String folder, Session session) throws OXException {
        triggerOSGiEvent(folder, session, null, true, false);
    }

    /**
     * Triggers the OSGi event system and posts a new event for new mails in given folder.
     *
     * @param folder The folder identifier; including account information
     * @param session The session providing needed user data
     * @param props The optional additional properties to put into OSGi event
     * @param includeCommonEvent <code>true</code> to add {@link CommonEvent} properties for remote distribution, <code>false</code>, otherwise
     * @throws OXException If posting event fails
     */
    public static void triggerOSGiEvent(String folder, Session session, boolean includeCommonEvent) throws OXException {
        triggerOSGiEvent(folder, session, null, includeCommonEvent, false);
    }

    /**
     * Triggers the OSGi event system and posts a new event for new mails in given folder.
     *
     * @param folder The folder identifier; including account information
     * @param session The session providing needed user data
     * @param props The optional additional properties to put into OSGi event
     * @param includeCommonEvent <code>true</code> to add {@link CommonEvent} properties for remote distribution, <code>false</code>, otherwise
     * @param publishMarker <code>true</code> to include publish marker; otherwise <code>false</code>
     * @throws OXException If posting event fails
     */
    public static void triggerOSGiEvent(String folder, Session session, Map<String, Object> props, boolean includeCommonEvent, boolean publishMarker) throws OXException {
        if (null == folder || null == session) {
            return;
        }
        try {
            EventAdmin eventAdmin = Services.requireService(EventAdmin.class);
            EventFactoryService eventFactoryService = includeCommonEvent ? Services.requireService(EventFactoryService.class) : null;
            int contextId = session.getContextId();
            int userId = session.getUserId();
            /*
             * Create event's properties
             */
            Dictionary<String, Object> properties = null == props ? new Hashtable<String, Object>(4) : new Hashtable<String, Object>(props);
            properties.put(PushEventConstants.PROPERTY_CONTEXT, Integer.valueOf(contextId));
            properties.put(PushEventConstants.PROPERTY_USER, Integer.valueOf(userId));
            properties.put(PushEventConstants.PROPERTY_SESSION, session);
            properties.put(PushEventConstants.PROPERTY_FOLDER, folder);
            /*-
             * Add common event to properties for remote distribution via push-UDP/push-MS
             *
             * Push-UDP/push-MS listens to OSGi events with "com/openexchange/*" topic, but
             * an instance of CommonEvent needs to be associated with key CommonEvent.EVENT_KEY
             * in order to remotely distribute that event.
             *
             * (see com.openexchange.push.ms.osgi.PushMsActivator.startBundle() /
             *      com.openexchange.push.ms.PushMsHandler.handleEvent(Event) )
             */
            if (null != eventFactoryService) {
                Map<Integer, Set<Integer>> emptyMap = Collections.<Integer, Set<Integer>> emptyMap();
                CommonEvent commonEvent = eventFactoryService.newCommonEvent(contextId, userId, emptyMap, CommonEvent.INSERT, Types.EMAIL, null, null, null, null, session);
                properties.put(CommonEvent.EVENT_KEY, commonEvent);
            }
            if (publishMarker) {
                // Add this property (with any or without a value) to distribute to remote nodes in the cluster
                properties.put(CommonEvent.PUBLISH_MARKER, Boolean.TRUE);
            }
            /*
             * Create event with push topic
             */
            Event event = new Event(PushEventConstants.TOPIC, properties);
            /*
             * Finally post it
             */
            eventAdmin.postEvent(event);
            LOG.debug("Notified new mails in folder \"{}\" for user {} in context {}", folder, I(userId), I(contextId));
        } catch (final OXException e) {
            throw e;
        }
    }

    private static final AtomicReference<ServiceListing<PushClientChecker>> CHECKERS_REF = new AtomicReference<ServiceListing<PushClientChecker>>(null);

    /**
     * Sets the {@link PushClientChecker} listing
     *
     * @param listing The listing to set
     */
    public static void setPushClientCheckerListing(ServiceListing<PushClientChecker> listing) {
        CHECKERS_REF.set(listing);
    }

    private static final String PARAM_PUSH_ALLOWED = "ox:push:allowed";

    /**
     * Checks if specified client identifier is allowed according to white-list filter or any {@link PushClientChecker checker}.
     *
     * @param client The client identifier
     * @param session The optional client-associated session
     * @param onAdd Whether this method is called for adding or removing a listener
     * @return <code>true</code> if client identifier is allowed; otherwise <code>false</code>
     */
    public static final boolean allowedClient(String client, Session session, boolean onAdd) {
        PushClientWhitelist clientWhitelist = PushClientWhitelist.getInstance();
        if (clientWhitelist.isEmpty() || clientWhitelist.isAllowed(client)) {
            // Allowed per client white-list
            return true;
        }

        if (false == onAdd && null != session) {
            // Called for possible listener removal
            Boolean wasAllowed = (Boolean) session.getParameter(PARAM_PUSH_ALLOWED);
            if (null != wasAllowed) {
                return wasAllowed.booleanValue();
            }
        }

        // Test if permitted by any checker (only permitted for adding a listener)
        if (onAdd) {
            ServiceListing<PushClientChecker> checkers = CHECKERS_REF.get();
            if (null != checkers) {
                for (PushClientChecker checker : checkers) {
                    try {
                        if (checker.isAllowed(client, session)) {
                            // Allowed by checker
                            if (onAdd && null != session) {
                                session.setParameter(PARAM_PUSH_ALLOWED, Boolean.TRUE);
                            }
                            return true;
                        }
                    } catch (OXException e) {
                        LOG.error("Checker '{}' failed to test if client {} is allowed to start a push listener", checker.getClass().getName(), client, e);
                    }
                }
            }
        }

        // Not allowed...
        if (onAdd && null != session) {
            session.setParameter(PARAM_PUSH_ALLOWED, Boolean.FALSE);
        }
        return false;
    }
}
