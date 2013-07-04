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

package com.openexchange.messaging.facebook.session;

import java.text.MessageFormat;
import java.util.Map;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;

/**
 * {@link FacebookEventHandler} - The {@link EventHandler event handler} for mail push bundle to track newly created and removed sessions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FacebookEventHandler implements EventHandler {

    /**
     * The logger constant.
     */
    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FacebookEventHandler.class));

    /**
     * Whether logger allows debug.
     */
    private static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * Initializes a new {@link FacebookEventHandler}.
     */
    public FacebookEventHandler() {
        super();
    }

    @Override
    public void handleEvent(final Event event) {
        final String topic = event.getTopic();
        try {
            if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic) || SessiondEventConstants.TOPIC_STORED_SESSION.equals(topic)) {
                // A single session was removed
                final Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                if (!session.isTransient() && FacebookOAuthAccessRegistry.getInstance().removeSessionIfLast(session.getContextId(), session.getUserId()) && DEBUG) {
                    LOG.debug(new StringBuilder("Facebook session removed for user ").append(session.getUserId()).append(" in context ").append(
                        session.getContextId()).toString());
                }
            } else if (SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic) || SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic)) {
                // A session container was removed
                @SuppressWarnings("unchecked") final Map<String, Session> sessionContainer =
                    (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                // For each session
                final FacebookOAuthAccessRegistry sessionRegistry = FacebookOAuthAccessRegistry.getInstance();
                for (final Session session : sessionContainer.values()) {
                    if (!session.isTransient() && sessionRegistry.removeSessionIfLast(session.getContextId(), session.getUserId()) && DEBUG) {
                        LOG.debug(new StringBuilder("Facebook session removed for user ").append(session.getUserId()).append(" in context ").append(
                            session.getContextId()).toString());
                    }
                }
            } else if (SessiondEventConstants.TOPIC_ADD_SESSION.equals(topic)) {
                // final Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                // Nothing to do for an added session
            }
        } catch (final Exception e) {
            LOG.error(MessageFormat.format("Error while handling SessionD event \"{0}\": {1}", topic, e.getMessage()), e);
        }
    }
}
