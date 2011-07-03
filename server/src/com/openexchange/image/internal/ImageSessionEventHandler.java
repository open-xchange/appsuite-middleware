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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.image.internal;

import java.text.MessageFormat;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.image.ImageService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;

/**
 * {@link ImageSessionEventHandler} - The {@link EventHandler event handler} for the ImageRegistry to remove ImageId-SessionId mappings after session removal.
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public final class ImageSessionEventHandler implements EventHandler {

    /**
     * The logger constant.
     */
    private static final Log LOG = com.openexchange.exception.Log.valueOf(LogFactory.getLog(ImageSessionEventHandler.class));

    /**
     * Whether logger allows debug.
     */
    private static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * Initializes a new {@link ImageSessionEventHandler}.
     */
    public ImageSessionEventHandler() {
        super();
    }

    public void handleEvent(final Event event) {
        final String topic = event.getTopic();
        try {
            if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
                // A single session was removed
                final Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                final ImageService imageService = ServerServiceRegistry.getInstance().getService(ImageService.class);
                imageService.removeImageData(session);
                
                if (DEBUG) {
                    LOG.debug("Removed image mappings for session " + session.getSessionID() + ".");
                }
            } else if (SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic) || SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic)) {
                // A session container was removed
                @SuppressWarnings("unchecked") final Map<String, Session> sessionContainer =
                    (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                
                final ImageService imageService = ServerServiceRegistry.getInstance().getService(ImageService.class);
                
                // For each session
                for (final Session session : sessionContainer.values()) {
                    imageService.removeImageData(session);
                    
                    if (DEBUG) {
                        LOG.debug("Removed image mappings for session " + session.getSessionID() + ".");
                    }
                }
            } else if (SessiondEventConstants.TOPIC_ADD_SESSION.equals(topic)) {
                // Nothing to do for an added session
            }
        } catch (final Exception e) {
            LOG.error(MessageFormat.format("Error while handling SessionD event \"{0}\": {1}", topic, e.getMessage()), e);
        }
    }
}
