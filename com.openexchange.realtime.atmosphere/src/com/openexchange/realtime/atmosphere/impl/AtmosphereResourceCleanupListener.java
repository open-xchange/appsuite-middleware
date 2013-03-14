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

import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListener;
import org.atmosphere.cpr.Broadcaster;
import com.openexchange.log.Log;
import com.openexchange.log.LogFactory;


/**
 * {@link AtmosphereResourceCleanupListener} - Properly disposes Broadcasters after the last resource was disconnected.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class AtmosphereResourceCleanupListener implements AtmosphereResourceEventListener {
    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(RTAtmosphereHandler.class));
    private final Broadcaster[] associatedBroadcasters;
    private final RTAtmosphereState atmosphereState;
    private final RTAtmosphereHandler handler;
    /**
     * Initializes a new {@link AtmosphereResourceCleanupListener}.
     * @param associatedBroadcasters the associated Broadcasters
     */
    public AtmosphereResourceCleanupListener(RTAtmosphereHandler handler, RTAtmosphereState atmosphereState, Broadcaster... associatedBroadcasters) {
        this.handler = handler;
        this.associatedBroadcasters = associatedBroadcasters;
        this.atmosphereState = atmosphereState;
    }

    @Override
    public void onSuspend(AtmosphereResourceEvent event) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Suspending: "+event.broadcaster().getID());
        }
    }

    @Override
    public void onResume(AtmosphereResourceEvent event) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Resuming: "+event.broadcaster().getID());
        }
    }

    @Override
    public void onDisconnect(AtmosphereResourceEvent event) {
        for (Broadcaster broadcaster : associatedBroadcasters) {
            int size = broadcaster.getAtmosphereResources().size();
            if(size==1) {
                if(LOG.isDebugEnabled()) {
                    LOG.debug("Destroying broadcaster: " + broadcaster.getID());
                }
              broadcaster.destroy();
            }
        }
        
        handler.onDisconnect(atmosphereState);
    }

    @Override
    public void onBroadcast(AtmosphereResourceEvent event) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Broadcasting: "+event.broadcaster().getID());
        }
    }

    @Override
    public void onThrowable(AtmosphereResourceEvent event) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Throwing: "+event.broadcaster().getID());
        }
    }

    @Override
    public void onPreSuspend(AtmosphereResourceEvent event) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Pre Suspending: "+event.broadcaster().getID());
        }
    }

}
