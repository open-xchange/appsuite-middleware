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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mail.compose;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import com.openexchange.mail.MailPath;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.PutIfAbsent;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link CompositionSpace} - Represents a composition space.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CompositionSpace {

    private static final String PARAM_REGISTRY = "__comp.registry";

    /**
     * Gets the composition space registry
     *
     * @param session The associated session
     * @return The composition space registry
     */
    static CompositionSpaceRegistry getRegistry(Session session) {
        CompositionSpaceRegistry registry = (CompositionSpaceRegistry) session.getParameter(PARAM_REGISTRY);
        if (null == registry) {
            CompositionSpaceRegistry newRegistry = new CompositionSpaceRegistry();
            registry = (CompositionSpaceRegistry) ((PutIfAbsent) session).setParameterIfAbsent(PARAM_REGISTRY, newRegistry);
            if (null == registry) {
                registry = newRegistry;
            }
        }
        return registry;
    }

    /**
     * Gets the composition space for given identifier
     *
     * @param csid The composition space identifier
     * @param session The associated session
     * @return The composition space
     */
    public static CompositionSpace getCompositionSpace(String csid, Session session) {
        return getRegistry(session).getCompositionSpace(csid, session);
    }

    /**
     * Drops composition spaces from given session
     *
     * @param session The associated session
     */
    public static void dropCompositionSpaces(Session session) {
        CompositionSpaceRegistry registry = (CompositionSpaceRegistry) session.getParameter(PARAM_REGISTRY);
        if (null != registry) {
            try {
                CompositionSpaces.destroy(registry, session);
            } finally {
                session.setParameter(PARAM_REGISTRY, null);
            }
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------------

    final String id;
    private volatile MailPath replyFor;
    private volatile MailPath forwardFor;
    private volatile MailPath draftEditFor;
    volatile long lastAccessed;
    private final Queue<MailPath> cleanUps;
    final long idleTime;
    final Session session;
    private volatile ScheduledTimerTask scheduledTimerTask;

    /**
     * Initializes a new {@link CompositionSpace}.
     *
     * @param id The composition space identifier
     * @param session The associated session
     */
    CompositionSpace(String id, Session session) {
        super();
        this.session = session;
        this.id = id;
        cleanUps = new ConcurrentLinkedQueue<MailPath>();
        idleTime = TimeUnit.MINUTES.toMillis(10); // 10 minutes idle time
        lastAccessed = System.currentTimeMillis();
    }

    /**
     * Marks this composition space as active
     */
    void markActive() {
        Runnable task = new Runnable() {

            @Override
            public void run() {
                if (System.currentTimeMillis() - lastAccessed > idleTime) {
                    CompositionSpaces.destroy(id, session);
                }
            }
        };

        TimerService timerService = ServerServiceRegistry.getInstance().getService(TimerService.class);
        scheduledTimerTask = timerService.scheduleAtFixedRate(task, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * Marks this composition space as inactive
     */
    void markInactive() {
        ScheduledTimerTask scheduledTimerTask = this.scheduledTimerTask;
        if (null != scheduledTimerTask) {
            scheduledTimerTask.cancel();
            this.scheduledTimerTask = null;
        }
    }

    /**
     * Gets the last-accessed time stamp
     *
     * @return The last-accessed time stamp
     */
    public long getLastAccessed() {
        return lastAccessed;
    }

    /**
     * Touches this composition space.
     *
     * @return This composition space with updated last-accessed time stamp
     */
    public CompositionSpace touch() {
        lastAccessed = System.currentTimeMillis();
        return this;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the <code>draftEditFor</code> reference
     *
     * @return The <code>draftEditFor</code> reference
     */
    public MailPath getDraftEditFor() {
        return draftEditFor;
    }

    /**
     * Sets the <code>draftEditFor</code> reference
     *
     * @param draftEditFor The <code>draftEditFor</code> reference to set
     */
    public void setDraftEditFor(MailPath draftEditFor) {
        this.draftEditFor = draftEditFor;
        lastAccessed = System.currentTimeMillis();
    }

    /**
     * Gets the <code>replyFor</code> reference
     *
     * @return The <code>replyFor</code> reference
     */
    public MailPath getReplyFor() {
        return replyFor;
    }

    /**
     * Sets the <code>replyFor</code> reference
     *
     * @param replyFor The <code>replyFor</code> reference to set
     */
    public void setReplyFor(MailPath replyFor) {
        this.replyFor = replyFor;
        lastAccessed = System.currentTimeMillis();
    }

    /**
     * Gets the <code>forwardFor</code> reference
     *
     * @return The <code>forwardFor</code> reference
     */
    public MailPath getForwardFor() {
        return forwardFor;
    }

    /**
     * Sets the <code>forwardFor</code> reference
     *
     * @param forwardFor The <code>forwardFor</code> reference to set
     */
    public void setForwardFor(MailPath forwardFor) {
        this.forwardFor = forwardFor;
        lastAccessed = System.currentTimeMillis();
    }

    /**
     * Adds given mail path to clean-ups.
     *
     * @param mailPath The mail path to add
     */
    public void addCleanUp(MailPath mailPath) {
        cleanUps.offer(mailPath);
        lastAccessed = System.currentTimeMillis();
    }

    /**
     * Gets the clean-ups
     *
     * @return The clean-ups
     */
    public Queue<MailPath> getCleanUps() {
        return cleanUps;
    }

}
