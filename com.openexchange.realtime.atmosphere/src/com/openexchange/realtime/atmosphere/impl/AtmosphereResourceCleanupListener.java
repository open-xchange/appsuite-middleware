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

package com.openexchange.realtime.atmosphere.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListener;
import com.openexchange.exception.OXException;
import com.openexchange.log.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.realtime.atmosphere.osgi.AtmosphereServiceRegistry;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.IDMap;

/**
 * {@link AtmosphereResourceCleanupListener} - Properly disposes Broadcasters after the last resource was disconnected.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class AtmosphereResourceCleanupListener implements AtmosphereResourceEventListener {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(RTAtmosphereHandler.class));

    private final AtmosphereResource atmosphereResource;

    private final ID fullID;

    /*
     * Map general ids (user@context) to concrete ids (ox://user@context/resource.browserx.taby) This is used for lookups via isConnected
     */
    private final IDMap<Set<ID>> generalToConcreteIDMap;

    /*
     * Map concrete client IDs to the AtmosphereResource that represents their connection to the server
     */
    private final IDMap<AtmosphereResource> fullIDToResourceMap;

    /*
     * Stored Stanzas for a concreteID
     */
    private ConcurrentHashMap<ID, List<EnqueuedStanza>> outboxes;

    /*
     * Resource Reaper
     */
    private AtmosphereResourceReaper atmosphereResourceReaper;

    private ConcurrentHashMap<ID, SortedSet<EnqueuedStanza>> resendBuffers;

    private ConcurrentHashMap<ID, Long> sequenceNumbers;

    /**
     * Initializes a new {@link AtmosphereResourceCleanupListener}.
     * 
     * @param atmosphereResource The AtmosphereResource this AtmosphereResourceEventListener was added to
     * @param fullID The full ID of the client connected via atmosphereResource
     * @param generalToFullIDMap Reference to the map of the RTAtmosphereHandler that tracks full client IDs to the AtmosphereResource that
     *            represents their connection to the server
     * @param fullIDToResourceMap Reference to the map of the RTAtmosphereHandler that tracks general ids to full ids.
     * @param outboxes
     */
    public AtmosphereResourceCleanupListener(AtmosphereResource atmosphereResource, ID fullID, IDMap<Set<ID>> generalToFullIDMap, IDMap<AtmosphereResource> fullIDToResourceMap, ConcurrentHashMap<ID, List<EnqueuedStanza>> outboxes, ConcurrentHashMap<ID, SortedSet<EnqueuedStanza>> resendBuffers, ConcurrentHashMap<ID, Long> sequenceNumbers, AtmosphereResourceReaper atmosphereResourceReaper) {
        this.atmosphereResource = atmosphereResource;
        this.fullID = fullID;
        this.generalToConcreteIDMap = generalToFullIDMap;
        this.fullIDToResourceMap = fullIDToResourceMap;
        this.outboxes = outboxes;
        this.resendBuffers = resendBuffers;
        this.sequenceNumbers = sequenceNumbers;
        this.atmosphereResourceReaper = atmosphereResourceReaper;
    }

    @Override
    public void onSuspend(AtmosphereResourceEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Suspending: " + fullID + " and AtmosphereResource " + atmosphereResource.uuid());
        }
    }

    @Override
    public void onResume(AtmosphereResourceEvent event) {
        // if (LOG.isDebugEnabled()) {
        // LOG.debug("Resuming: " + fullID + " and AtmosphereResource " + atmosphereResource.uuid());
        // }
    }

    @Override
    public void onDisconnect(AtmosphereResourceEvent event) {
        atmosphereResource.removeEventListener(this);
        AtmosphereResource activeResource = fullIDToResourceMap.get(fullID);
        if (!activeResource.equals(atmosphereResource)) {
            return; // Other resource is active here. No need to clean up just yet.
        } else {
            atmosphereResourceReaper.add(new Moribund(fullID, atmosphereResource, generalToConcreteIDMap, fullIDToResourceMap, outboxes, resendBuffers, sequenceNumbers));
        }
    }

    @Override
    public void onBroadcast(AtmosphereResourceEvent event) {
        // if (LOG.isDebugEnabled()) {
        // LOG.debug("Broadcasting: " + fullID + " and AtmosphereResource " + atmosphereResource.uuid());
        // }
    }

    @Override
    public void onThrowable(AtmosphereResourceEvent event) {
        // if (LOG.isDebugEnabled()) {
        // LOG.debug("Throwing: " + fullID + " and AtmosphereResource " + atmosphereResource.uuid(), event.throwable());
        // }
    }

    @Override
    public void onPreSuspend(AtmosphereResourceEvent event) {
        // if (LOG.isDebugEnabled()) {
        // LOG.debug("Pre Suspending: " + fullID + " and AtmosphereResource " + atmosphereResource.uuid());
        // }
    }

}
