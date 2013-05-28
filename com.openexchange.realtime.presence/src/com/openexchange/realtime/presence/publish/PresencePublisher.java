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

package com.openexchange.realtime.presence.publish;

import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.directory.ChangeListener;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.presence.osgi.Services;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;

/**
 * {@link PresencePublisher} - Listen for Presence changes in the ResourceDirectory and publish these.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class PresencePublisher implements ChangeListener {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(PresencePublisher.class);

    private ResourceDirectory resourceDirectory;

    private enum PresenceChangeType {
        COMING_ONLINE, ONLINE_CHANGE, GOING_OFFLINE
    }

    /**
     * Initializes a new {@link PresencePublisher}.
     * 
     * @param resourceDirectory
     */
    public PresencePublisher(ResourceDirectory resourceDirectory) {
        super();
        this.resourceDirectory = resourceDirectory;
        resourceDirectory.addListener(this);
    }

    /**
     * Sets the ResourceDirectory to given value/reference.
     * 
     * @param resourceDirectory The ResourceDirectory
     * @throws OXException
     */
    public void setResourceDirectory(ResourceDirectory resourceDirectory) {
        this.resourceDirectory = resourceDirectory;
        if (resourceDirectory != null) {
            resourceDirectory.addListener(this);
        }
    }

    /*
     * This is called when the client connects for the first time. Nevertheless check if Resource contains Presence, get subscribers, send
     * to each of them
     */
    @Override
    public void added(ID id, Resource resource) {
        if(LOG.isDebugEnabled()) {
            LOG.debug(String.format("Added: %1$s - %2$s", id, resource.getPresence()));
        }
        if (hasPresence(resource)) {
            // TODO: restore from expirer list if present
            Presence presence = resource.getPresence();
            try {
                PresenceSubscriptionService presenceSubscriptionService = getPresenceSubscriptionService();
                List<ID> subscribers = presenceSubscriptionService.getSubscribers(presence.getFrom());
                if (!subscribers.isEmpty()) {
                    MessageDispatcher messageDispatcher = getMessageDispatcher();
                    for (ID subscriber : subscribers) {
                        Presence directedPresence = new Presence(presence);
                        directedPresence.setTo(subscriber);
                        @SuppressWarnings("unused")// TODO: Can Presence safely be discarded
                        Map<ID, OXException> failures = messageDispatcher.send(directedPresence);
                    }
                }
            } catch (OXException e) {
                LOG.error("Couldn't send Presence", e);
            }
        }
    }

    /*
     * Check if Resource contains Presence and differs from last Presence. If yes, get subscribers publish to each of them. This can be
     * initial, normal or final presence.
     */
    @Override
    public void updated(ID id, Resource currentResource, Resource previousResource) {
        if (hasPresence(currentResource)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resource has Presence");
            }
            Presence currentPresence = currentResource.getPresence();
            if (!currentPresence.equals(previousResource.getPresence())) {
                try {
                    PresenceSubscriptionService presenceSubscriptionService = getPresenceSubscriptionService();
                    List<ID> subscribers = presenceSubscriptionService.getSubscribers(currentPresence.getFrom());
                    if (!subscribers.isEmpty()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Pushing Presence to " + subscribers.size() + "subscriber(s)");
                        }
                        MessageDispatcher messageDispatcher = getMessageDispatcher();
                        for (ID subscriber : subscribers) {
                            Presence directedPresence = new Presence(currentPresence);
                            directedPresence.setTo(subscriber);
                            Map<ID, OXException> failures = messageDispatcher.send(directedPresence);
                            for (OXException oxe : failures.values()) {
                                LOG.error(oxe);
                            }
                        }
                    }
                } catch (OXException e) {
                    LOG.error("Couldn't send Presence", e);
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Resource's Presence didn't differ from previous");
                }
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resource didn't carry Presence");
            }
        }
    }

    @Override
    public void removed(ID id, Resource resource) {
        if(LOG.isDebugEnabled()) {
            LOG.debug(String.format("Removed: %1$s - %2$s", id, resource.getPresence()));
        }

        /*
         * Check if Resource contains Presence. Check if id is still registered in directory. If no longer registered - if Presence was
         * unavailable -> do nothing - if Presence wasn't unavailable -> add to expirer list Needs cluster wide sorted list + cluster wide
         * executor that checks for expiration candidates that are older than a minute and didn't send unavailable presence. If time is
         * over send unavailable presence
         */

    }

    private PresenceSubscriptionService getPresenceSubscriptionService() throws OXException {
        PresenceSubscriptionService presenceSubscriptionService = Services.getService(PresenceSubscriptionService.class);
        if (presenceSubscriptionService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(PresenceSubscriptionService.class.getName());
        }
        return presenceSubscriptionService;
    }

    private MessageDispatcher getMessageDispatcher() throws OXException {
        MessageDispatcher messageDispatcher = Services.getService(MessageDispatcher.class);
        if (messageDispatcher == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(PresenceSubscriptionService.class.getName());
        }
        return messageDispatcher;
    }

    private boolean hasPresence(Resource resource) {
        return resource.getPresence() != null;
    }

}
