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

package com.openexchange.mail.compose;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailPath;
import com.openexchange.mailaccount.UnifiedInboxManagement;
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
     * Destroys the composition space denoted by given identifier
     *
     * @param csid The composition space identifier
     * @param session The associated session
     */
    public static void destroyCompositionSpace(String csid, boolean apply, Session session) {
        if (apply) {
            try {
                CompositionSpaces.applyCompositionSpace(csid, session);
            } catch (Exception e) {
                org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CompositionSpace.class);
                logger.warn("Failed to apply composition space", e);
            }
        }
        CompositionSpaces.destroy(csid, session);
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
     * Optionally gets the composition space for given identifier
     *
     * @param csid The composition space identifier
     * @param session The associated session
     * @return The composition space or <code>null</code>
     */
    public static CompositionSpace optCompositionSpace(String csid, Session session) {
        CompositionSpaceRegistry registry = (CompositionSpaceRegistry) session.getParameter(PARAM_REGISTRY);
        if (null == registry) {
            return null;
        }

        return registry.optCompositionSpace(csid);
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
    private final Queue<MailPath> forwardsFor;
    private final Queue<MailPath> draftEditsFor;
    private final Queue<MailPath> cleanUps;
    volatile long lastAccessed;
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
        forwardsFor = new ConcurrentLinkedQueue<MailPath>();
        draftEditsFor = new ConcurrentLinkedQueue<MailPath>();
        idleTime = TimeUnit.MINUTES.toMillis(15); // 15 minutes idle time
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

    private boolean isMarkedAsReplyOrForward0(MailPath toCheck, boolean considerReply, boolean considerForward) {
        if (considerReply && areEqual(toCheck, this.replyFor)) {
            return true;
        }

        if (considerForward) {
            for (MailPath forwardFor : forwardsFor) {
                if (areEqual(toCheck, forwardFor)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if specified mail path is already referenced by either as <code>replyFor</code> or <code>forwardsFor</code>.
     *
     * @param toCheck The mail path to check
     * @return <code>true</code> if mail path is referenced by <code>replyFor</code>/<code>forwardsFor</code>; otherwise <code>false</code>
     */
    public boolean isMarkedAsReplyOrForward(MailPath toCheck) {
        if (null == toCheck) {
            return false;
        }

        return isMarkedAsReplyOrForward0(toCheck, true, true);
    }

    /**
     * Checks if specified mail path is already referenced as <code>replyFor</code>.
     *
     * @param toCheck The mail path to check
     * @return <code>true</code> if mail path is referenced by <code>replyFor</code>; otherwise <code>false</code>
     */
    public boolean isMarkedAsReply(MailPath toCheck) {
        if (null == toCheck) {
            return false;
        }

        return isMarkedAsReplyOrForward0(toCheck, true, false);
    }

    /**
     * Checks if specified mail path is already referenced as <code>forwardsFor</code>.
     *
     * @param toCheck The mail path to check
     * @return <code>true</code> if mail path is referenced as <code>replyFor</code>/<code>forwardsFor</code>; otherwise <code>false</code>
     */
    public boolean isMarkedAsForward(MailPath toCheck) {
        if (null == toCheck) {
            return false;
        }

        return isMarkedAsReplyOrForward0(toCheck, false, true);
    }

    /**
     * Gets the <code>draftEditFor</code> references
     *
     * @return The <code>draftEditFor</code> references
     */
    public Queue<MailPath> getDraftEditsFor() {
        return draftEditsFor;
    }

    /**
     * Sets the <code>draftEditFor</code> reference
     *
     * @param draftEditFor The <code>draftEditFor</code> reference to set
     */
    public void addDraftEditFor(MailPath draftEditFor) {
        this.draftEditsFor.offer(draftEditFor);
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
     * Gets the <code>forwardFor</code> references
     *
     * @return The <code>forwardFor</code> references
     */
    public Queue<MailPath> getForwardsFor() {
        return forwardsFor;
    }

    /**
     * Sets the <code>forwardFor</code> reference
     *
     * @param forwardFor The <code>forwardFor</code> reference to set
     */
    public void addForwardFor(MailPath forwardFor) {
        this.forwardsFor.offer(forwardFor);
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

    /**
     * Adds given mail path to clean-ups.
     *
     * @param mailPath The mail path to add
     */
    public void addCleanUp(MailPath mailPath) {
        cleanUps.offer(mailPath);
        lastAccessed = System.currentTimeMillis();
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private boolean areEqual(MailPath mailPath, MailPath other) {
        if (null == mailPath) {
            return false;
        }

        if (mailPath.equals(other)) {
            return true;
        }

        int unifiedMailId = getUnifiedMailId();

        MailPath extractedPath = CompositionSpaces.optUnifiedInboxUID(mailPath, unifiedMailId);
        if (null != extractedPath && extractedPath.equals(other)) {
            return true;
        }

        MailPath extractedOther = null == other ? null : CompositionSpaces.optUnifiedInboxUID(other, unifiedMailId);
        if (null != extractedOther) {
            if (mailPath.equals(extractedOther) || (null != extractedPath && extractedPath.equals(extractedOther))) {
                return true;
            }
        }

        return false;
    }

    private int getUnifiedMailId() {
        int unifiedMailId = -1;
        {
            UnifiedInboxManagement uim = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);
            if (null != uim) {
                try {
                    unifiedMailId = uim.getUnifiedINBOXAccountID(session);
                } catch (OXException e) {
                    // Failed...
                    unifiedMailId = -1;
                }
            }
        }
        return unifiedMailId;
    }

}
