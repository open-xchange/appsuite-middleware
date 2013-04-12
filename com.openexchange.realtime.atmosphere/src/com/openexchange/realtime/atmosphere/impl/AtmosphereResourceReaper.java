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

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.exception.OXException;
import com.openexchange.log.Log;
import com.openexchange.realtime.atmosphere.osgi.AtmosphereServiceRegistry;
import com.openexchange.realtime.packet.ID;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link AtmosphereResourceReaper} - Moribund clients/resources that just disconnected can be added to the AtmosphereResourceReaper. The
 * reaper waits for a limited amount of time before he finally removes the clients/resources. The only way to prevent the reaper from
 * removing the clients/resources is when the Moribund is removed again from the Reaper.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class AtmosphereResourceReaper {

    private static final org.apache.commons.logging.Log LOG = Log.loggerFor(AtmosphereResourceReaper.class);

    /* HashMap mapping Moribund.ID(concrete ID of a client) to Moribund for easy concurrent access */
    private final ConcurrentHashMap<ID, Moribund> moribundRegistry;

    /* Fair Reentrant Lock to synchronize removal of Moribunds */
    private final ReentrantLock moribundRemoveLock;

    /* Define how long a moribund may linger before being reaped */
    private static final int MORIBUND_MAX_LINGER = 5000;

    private volatile ScheduledTimerTask scheduledReaper;

    public AtmosphereResourceReaper() {
        this.moribundRegistry = new ConcurrentHashMap<ID, Moribund>();
        this.moribundRemoveLock = new ReentrantLock(true);
        final TimerService timerService = AtmosphereServiceRegistry.getInstance().getService(TimerService.class);

        final ScheduledTimerTask scheduledReaper = timerService.scheduleAtFixedRate(new Runnable() {

            /*
             * Start at the end of the navigable Set to get the oldest moribund first. Then proceed to the younger ones. Stop at the first
             * moribund that still has some time left to linger.
             */
            @Override
            public void run() {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Next Reaper run.");
                }
                moribundRemoveLock.lock();
                try {
                    NavigableSet<Moribund> sortedMoribunds = new TreeSet<Moribund>(moribundRegistry.values());
                    final Iterator<Moribund> descendingMoribundIterator = sortedMoribunds.descendingIterator();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Reaper found " + sortedMoribunds.size() + " Moribunds");
                    }
                    boolean ripeMoribundsLeft = true;
                    while (ripeMoribundsLeft && descendingMoribundIterator.hasNext()) {
                        final Moribund moribund = descendingMoribundIterator.next();
                        if (moribund.getLinger() > MORIBUND_MAX_LINGER) {
                            try {
                                moribund.die();
                                moribundRegistry.remove(moribund.getConcreteID());
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Reaped Moribund: " + moribund);
                                }
                            } catch (final OXException oxe) {
                                LOG.error("Couldn't reap Moribund: " + moribund, oxe);
                            }
                        } else {
                            ripeMoribundsLeft = false;
                        }
                    }
                } catch(Exception e) {
                    LOG.error("Error during AtmosphereResourceReaper run.", e);
                } finally {
                    moribundRemoveLock.unlock();
                }
            }
        }, 1000, 10000, TimeUnit.MILLISECONDS);
        this.scheduledReaper = scheduledReaper;
    }

    /**
     * Add a moribund to the AtmosphereResourceReaper.
     * 
     * @param moribund The moribund to add to the AtmosphereResourceReaper
     * @return The
     */
    public void add(final Moribund moribund) {
        Moribund oldMoribund = moribundRegistry.put(moribund.getConcreteID(), moribund);
        if (oldMoribund != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found a previous Moribund, this shouldn't happen!: " + oldMoribund);
            }
            try {
                oldMoribund.die();
            } catch (final OXException oxe) {
                LOG.error("Couldn't reap previous Moribund: " + oldMoribund, oxe);
            }
        }
    }

    /**
     * Remove a Moribund from the AtmosphereResourceReaper
     * 
     * @param id the ID of the Moribund to save from being reaped.
     * @return the saved Moribund or null if the Moribund was already reaped.
     */
    public Moribund remove(final ID id) {
        moribundRemoveLock.lock();
        Moribund saved = null;
        try {
            saved = moribundRegistry.remove(id);
            if (saved != null && LOG.isDebugEnabled()) {
                LOG.debug("Saved Moribund: " + saved.getConcreteID());
            }
        } finally {
            moribundRemoveLock.unlock();
        }
        return saved;
    }

}
