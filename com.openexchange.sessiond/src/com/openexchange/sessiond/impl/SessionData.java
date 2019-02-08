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

package com.openexchange.sessiond.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * Object handling the multi threaded access to session container. Excessive locking is used to secure container data structures.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class SessionData {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionData.class);

    private final int maxSessions;
    private final long randomTokenTimeout;
    private final boolean autoLogin;
    private final Map<String, String> randoms;
    private final MetricHandler metrics;
    

    /** Plain array+direct indexing is the fastest technique of iterating. So, use CopyOnWriteArrayList since 'sessionList' is seldom modified (see rotateShort()) */
    private final RotatableCopyOnWriteArrayList<SessionContainer> sessionList;

    /**
     * The LongTermUserGuardian contains an entry for a given UserKey if the longTermList contains a session for the user
     * <p>
     * This is used to guard against potentially slow serial searches of the long term sessions
     */
    private final UserRefCounter longTermUserGuardian = new UserRefCounter();

    /** Plain array+direct indexing is the fastest technique of iterating. So, use CopyOnWriteArrayList since 'longTermList' is seldom modified (see rotateLongTerm()) */
    private final RotatableCopyOnWriteArrayList<SessionMap> longTermList;

    /**
     * Map to remember if there is already a task that should move the session to the first container.
     */
    private final ConcurrentMap<String, Move2FirstContainerTask> tasks = new ConcurrentHashMap<String, Move2FirstContainerTask>();

    private final AtomicReference<ThreadPoolService> threadPoolService;
    private final AtomicReference<TimerService> timerService;

    protected final Map<String, ScheduledTimerTask> removers = new ConcurrentHashMap<String, ScheduledTimerTask>();

    /**
     * Initializes a new {@link SessionData}.
     *
     * @param containerCount The container count for short-term sessions
     * @param maxSessions The max. number of total sessions
     * @param randomTokenTimeout The timeout for random tokens
     * @param longTermContainerCount The container count for long-term sessions
     * @param autoLogin Whether auto-login is enabled or not
     * @param An optional {@link MetricHandler}
     */
    SessionData(int containerCount, int maxSessions, long randomTokenTimeout, int longTermContainerCount, boolean autoLogin, MetricHandler metrics) {
        super();
        threadPoolService = new AtomicReference<ThreadPoolService>();
        timerService = new AtomicReference<TimerService>();
        this.maxSessions = maxSessions;
        this.randomTokenTimeout = randomTokenTimeout;
        this.autoLogin = autoLogin;

        randoms = new ConcurrentHashMap<String, String>(1024, 0.75f, 1);

        SessionContainer[] shortTermInit = new SessionContainer[containerCount];
        for (int i = containerCount; i-- > 0;) {
            shortTermInit[i] = new SessionContainer();
        }
        sessionList = new RotatableCopyOnWriteArrayList<SessionContainer>(shortTermInit);

        SessionMap[] longTermInit = new SessionMap[longTermContainerCount];
        for (int i = longTermContainerCount; i-- > 0;) {
            longTermInit[i] = new SessionMap(256);
        }
        longTermList = new RotatableCopyOnWriteArrayList<SessionMap>(longTermInit);
        this.metrics = metrics;
    }
    
    private boolean gatherMetrics() {
        return metrics != null;
    }

    private void metricsDecreaseAll(int num) {
        if (gatherMetrics()) {
            metrics.decreaseSessionCount(num);
        }
    }

    private void metricsDecreaseShort(int num) {
        if (gatherMetrics()) {
            metrics.decreaseShortTermSessionCount(num);
        }
    }

    private void metricsDecreaseLong(int num) {
        if (gatherMetrics()) {
            metrics.decreaseLongTermSessionCount(num);
        }
    }

    private void metricsIncreaseAll(int num) {
        if (gatherMetrics()) {
            metrics.increaseSessionCount(num);
        }
    }

    private void metricsIncreaseShort(int num) {
        if (gatherMetrics()) {
            metrics.increaseShortTermSessionCount(num);
        }
    }

    private void metricsIncreaseLong(int num) {
        if (gatherMetrics()) {
            metrics.increaseLongTermSessionCount(num);
        }
    }

    void clear() {
        int shortTerm = sessionList.size();
        sessionList.clear();
        metricsDecreaseShort(shortTerm);
        randoms.clear();
        int longterm =  longTermList.size();
        longTermUserGuardian.clear();
        longTermList.clear();
        metricsDecreaseLong(longterm);
        metricsDecreaseAll(longterm + shortTerm);
    }

    /**
     * Rotates the session containers. A new slot is added to head of each queue, while the last one is removed.
     *
     * @return The removed sessions
     */
    List<SessionControl> rotateShort() {
        // This is the only location which alters 'sessionList' during runtime
        List<SessionControl> removedSessions = new ArrayList<SessionControl>(sessionList.rotate(new SessionContainer()).getSessionControls());

        if (autoLogin && false == removedSessions.isEmpty()) {
            List<SessionControl> transientSessions = null;

            try {
                SessionMap first = longTermList.get(0);
                int numLongTermIncrease = 0;
                for (Iterator<SessionControl> it = removedSessions.iterator(); it.hasNext();) {
                    final SessionControl control = it.next();
                    final SessionImpl session = control.getSession();
                    if (false == session.isTransient()) {
                        // A regular, non-transient session
                        first.putBySessionId(session.getSessionID(), control);
                        numLongTermIncrease++;
                        longTermUserGuardian.add(session.getUserId(), session.getContextId());
                    } else {
                        // A transient session -- do not move to long-term container
                        it.remove();
                        if (null == transientSessions) {
                            transientSessions = new LinkedList<SessionControl>();
                        }
                        transientSessions.add(control);
                    }
                }
                metricsIncreaseLong(numLongTermIncrease);
                metricsDecreaseShort(removedSessions.size());
                metricsDecreaseAll(removedSessions.size() - numLongTermIncrease);
            } catch (IndexOutOfBoundsException e) {
                // About to shut-down
                LOG.error("First long-term session container does not exist. Likely SessionD is shutting down...", e);
            }

            if (null != transientSessions) {
                SessionHandler.postContainerRemoval(transientSessions, true);
            }
        }

        return removedSessions;
    }

    List<SessionControl> rotateLongTerm() {
        // This is the only location which alters 'longTermList' during runtime
        List<SessionControl> removedSessions = new ArrayList<SessionControl>(longTermList.rotate(new SessionMap(256)).values());
        for (SessionControl sessionControl : removedSessions) {
            SessionImpl session = sessionControl.getSession();
            longTermUserGuardian.remove(session.getUserId(), session.getContextId());
        }
        metricsDecreaseLong(removedSessions.size());
        metricsDecreaseAll(removedSessions.size());
        return removedSessions;
    }

    /**
     * Checks if given user in specified context has an active session kept in session container(s)
     *
     * @param userId The user ID
     * @param contextId The user's context ID
     * @param includeLongTerm <code>true</code> to also lookup the long term sessions, <code>false</code>, otherwise
     * @return <code>true</code> if given user in specified context has an active session; otherwise <code>false</code>
     */
    boolean isUserActive(int userId, int contextId, boolean includeLongTerm) {
        // A read-only access to session list
        for (final SessionContainer container : sessionList) {
            if (container.containsUser(userId, contextId)) {
                return true;
            }
        }

        // No need to check long-term container
        if (!includeLongTerm) {
            return false;
        }

        // Check long-term container, too
        return hasLongTermSession(userId, contextId);
    }

    private final boolean hasLongTermSession(final int userId, final int contextId) {
        return this.longTermUserGuardian.contains(userId, contextId);
    }

    private final boolean hasLongTermSession(final int contextId) {
        return this.longTermUserGuardian.contains(contextId);
    }

    SessionControl[] removeUserSessions(final int userId, final int contextId) {
        // Removing sessions is a write operation.
        final List<SessionControl> retval = new LinkedList<SessionControl>();
        for (final SessionContainer container : sessionList) {
            retval.addAll(container.removeSessionsByUser(userId, contextId));
        }
        for (final SessionControl control : retval) {
            unscheduleTask2MoveSession2FirstContainer(control.getSession().getSessionID(), true);
        }
        metricsDecreaseShort(retval.size());
        if (!hasLongTermSession(userId, contextId)) {
            metricsDecreaseAll(retval.size());
            return retval.toArray(new SessionControl[retval.size()]);
        }
        int numShortTerm = retval.size();
        int numLongTerm = 0;
        for (SessionMap longTerm : longTermList) {
            for (SessionControl control : longTerm.values()) {
                if (control.equalsUserAndContext(userId, contextId)) {
                    Session session = control.getSession();
                    SessionControl removeBySessionId = longTerm.removeBySessionId(session.getSessionID());
                    if(removeBySessionId != null) {
                        numLongTerm++;
                    }
                    longTermUserGuardian.remove(userId, contextId);
                    retval.add(control);
                }
            }
        }
        
        metricsDecreaseLong(numLongTerm);
        metricsDecreaseAll(numShortTerm + numLongTerm);
        return retval.toArray(new SessionControl[retval.size()]);
    }

    List<SessionControl> removeContextSessions(final int contextId) {
        // Removing sessions is a write operation.
        final List<SessionControl> list = new LinkedList<SessionControl>();
        for (final SessionContainer container : sessionList) {
            list.addAll(container.removeSessionsByContext(contextId));
        }
        for (final SessionControl control : list) {
            unscheduleTask2MoveSession2FirstContainer(control.getSession().getSessionID(), true);
        }

        if (!hasLongTermSession(contextId)) {
            metricsDecreaseShort(list.size());
            metricsDecreaseAll(list.size());
            return list;
        }
        int numOfShortTerm = list.size();
        metricsDecreaseShort(numOfShortTerm);
        int numOfLongTerm = 0;
        for (SessionMap longTerm : longTermList) {
            for (SessionControl control : longTerm.values()) {
                if (control.equalsContext(contextId)) {
                    Session session = control.getSession();
                    SessionControl removed = longTerm.removeBySessionId(session.getSessionID());
                    if(removed != null) {
                       numOfLongTerm++;
                    }
                    longTermUserGuardian.remove(session.getUserId(), contextId);
                    list.add(control);
                }
            }
        }
        
        metricsDecreaseLong(numOfLongTerm);
        metricsDecreaseAll(numOfShortTerm + numOfLongTerm);
        return list;
    }

    /**
     * Removes all sessions belonging to given contexts out of long- and short-term container.
     *
     * @param contextIds - Set with the context identifiers to remove sessions for
     * @return List of {@link SessionControl} objects for each handled session
     */
    List<SessionControl> removeContextSessions(final Set<Integer> contextIds) {
        // Removing sessions is a write operation.
        final List<SessionControl> list = new ArrayList<SessionControl>();
        for (final SessionContainer container : sessionList) {
            list.addAll(container.removeSessionsByContexts(contextIds));
        }
        for (final SessionControl control : list) {
            unscheduleTask2MoveSession2FirstContainer(control.getSession().getSessionID(), true);
        }
        int numShortTerm=list.size();
        TIntSet contextIdsToCheck = new TIntHashSet(contextIds.size());
        for (int contextId : contextIds) {
            if (hasLongTermSession(contextId)) {
                contextIdsToCheck.add(contextId);
            }
        }
        int numLongTerm=0;
        for (final SessionMap longTerm : longTermList) {
            for (SessionControl control : longTerm.values()) {
                Session session = control.getSession();
                int contextId = session.getContextId();
                if (contextIdsToCheck.contains(contextId)) {
                    SessionControl removed = longTerm.removeBySessionId(session.getSessionID());
                    if(removed != null) {
                        numLongTerm++;
                    }
                    longTermUserGuardian.remove(session.getUserId(), contextId);
                    list.add(control);
                }
            }
        }
        
        metricsDecreaseShort(numShortTerm);
        metricsDecreaseLong(numLongTerm);
        metricsDecreaseAll(numShortTerm + numLongTerm);
        return list;
    }

    boolean hasForContext(final int contextId) {
        for (final SessionContainer container : sessionList) {
            if (container.hasForContext(contextId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the first session for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param includeLongTerm Whether long-term container should be considered or not
     * @return The first matching session or <code>null</code>
     */
    public SessionControl getAnyActiveSessionForUser(final int userId, final int contextId, final boolean includeLongTerm) {
        for (final SessionContainer container : sessionList) {
            final SessionControl control = container.getAnySessionByUser(userId, contextId);
            if (control != null) {
                return control;
            }
        }

        if (includeLongTerm) {
            if (!hasLongTermSession(userId, contextId)) {
                return null;
            }
            for (SessionMap longTermMap : longTermList) {
                for (SessionControl control : longTermMap.values()) {
                    if (control.equalsUserAndContext(userId, contextId)) {
                        return control;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds the first session for given user that satisfies given matcher.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param matcher The matcher to satisfy
     * @param ignoreLongTerm Whether long-term container should be considered or not
     * @return The first matching session or <code>null</code>
     */
    public Session findFirstSessionForUser(int userId, int contextId, SessionMatcher matcher, boolean ignoreLongTerm) {
        for (SessionContainer container : sessionList) {
            SessionControl control = container.getAnySessionByUser(userId, contextId);
            if ((control != null) && matcher.accepts(control.getSession())) {
                return control.getSession();
            }
        }

        if (false == ignoreLongTerm) {
            if (!hasLongTermSession(userId, contextId)) {
                return null;
            }
            for (SessionMap longTermMap : longTermList) {
                for (SessionControl control : longTermMap.values()) {
                    if (control.equalsUserAndContext(userId, contextId) && matcher.accepts(control.getSession())) {
                        return control.getSession();
                    }
                }
            }
        }
        return null;
    }

    public List<Session> filterSessions(SessionFilter filter) {
        List<Session> sessions = new LinkedList<Session>();
        for (final SessionContainer container : sessionList) {
            collectSessions(filter, container.getSessionControls(), sessions);
        }

        for (final SessionMap longTermMap : longTermList) {
            collectSessions(filter, longTermMap.values(), sessions);
        }
        return sessions;
    }

    private static void collectSessions(SessionFilter filter, Collection<SessionControl> sessionControls, List<Session> sessions) {
        for (SessionControl sessionControl : sessionControls) {
            SessionImpl session = sessionControl.getSession();
            if (filter.apply(session)) {
                sessions.add(session);
            }
        }
    }

    /**
     * Gets the <b>local-only</b> sessions associated with specified user in given context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The <b>local-only</b> sessions or an empty list
     */
    List<SessionControl> getUserSessions(int userId, int contextId) {
        // A read-only access to session list
        List<SessionControl> retval = new LinkedList<SessionControl>();

        // Short term ones
        for (SessionContainer container : sessionList) {
            retval.addAll(container.getSessionsByUser(userId, contextId));
        }

        // Long term ones
        if (!hasLongTermSession(userId, contextId)) {
            return retval;
        }

        for (SessionMap longTermMap : longTermList) {
            for (SessionControl control : longTermMap.values()) {
                if (control.equalsUserAndContext(userId, contextId)) {
                    retval.add(control);
                }
            }
        }
        return retval;
    }

    /**
     * Gets the number of <b>local-only</b> sessions associated with specified user in given context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param considerLongTerm <code>true</code> to also consider long-term sessions; otherwise <code>false</code>
     * @return The number of sessions
     */
    int getNumOfUserSessions(int userId, int contextId, boolean considerLongTerm) {
        // A read-only access to session list
        int count = 0;
        for (SessionContainer container : sessionList) {
            count += container.numOfUserSessions(userId, contextId);
        }

        if (considerLongTerm) {
            if (!hasLongTermSession(userId, contextId)) {
                return count;
            }
            for (SessionMap longTermMap : longTermList) {
                for (SessionControl control : longTermMap.values()) {
                    if (control.equalsUserAndContext(userId, contextId)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Checks validity/uniqueness of specified authentication identifier for given login
     *
     * @param login The login
     * @param authId The authentication identifier
     * @throws OXException If authentication identifier is invalid/non-unique
     */
    void checkAuthId(String login, String authId) throws OXException {
        if (null != authId) {
            for (SessionContainer container : sessionList) {
                for (SessionControl sc : container.getSessionControls()) {
                    if (authId.equals(sc.getSession().getAuthId())) {
                        throw SessionExceptionCodes.DUPLICATE_AUTHID.create(sc.getSession().getLogin(), login);
                    }
                }
            }

            for (SessionMap longTermMap : longTermList) {
                for (SessionControl control : longTermMap.values()) {
                    if (authId.equals(control.getSession().getAuthId())) {
                        throw SessionExceptionCodes.DUPLICATE_AUTHID.create(control.getSession().getLogin(), login);
                    }
                }
            }
        }
    }

    /**
     * Adds specified session.
     *
     * @param session The session to add
     * @param noLimit <code>true</code> to add without respect to limitation; otherwise <code>false</code> to honor limitation
     * @return The associated {@link SessionControl} instance
     * @throws OXException If add operation fails
     */
    protected SessionControl addSession(final SessionImpl session, final boolean noLimit) throws OXException {
        return addSession(session, noLimit, false);
    }

    /**
     * Adds specified session.
     *
     * @param session The session to add
     * @param noLimit <code>true</code> to add without respect to limitation; otherwise <code>false</code> to honor limitation
     * @param addIfAbsent <code>true</code> to perform an add-if-absent operation; otherwise <code>false</code> to fail on duplicate session
     * @return The associated {@link SessionControl} instance
     * @throws OXException If add operation fails
     */
    protected SessionControl addSession(final SessionImpl session, final boolean noLimit, final boolean addIfAbsent) throws OXException {
        if (!noLimit && countSessions() > maxSessions) {
            throw SessionExceptionCodes.MAX_SESSION_EXCEPTION.create();
        }

        // Add session
        try {
            SessionControl control = sessionList.get(0).put(session, addIfAbsent);
            randoms.put(session.getRandomToken(), session.getSessionID());

            scheduleRandomTokenRemover(session.getRandomToken());
            metricsIncreaseShort(1);
            metricsIncreaseAll(1);
            return control;
        } catch (IndexOutOfBoundsException e) {
            // About to shut-down
            throw SessionExceptionCodes.NOT_INITIALIZED.create();
        }
    }

    int countSessions() {
        // A read-only access to session list
        int count = 0;
        for (final SessionContainer container : sessionList) {
            count += container.size();
        }

        for (final SessionMap longTermMap : longTermList) {
            count += longTermMap.size();
        }
        return count;
    }

    int[] getShortTermSessionsPerContainer() {
        // read-only access to short term sessions.
        TIntList counts = new TIntArrayList(10);
        for (final SessionContainer container : sessionList) {
            counts.add(container.size());
        }
        return counts.toArray();
    }

    int[] getLongTermSessionsPerContainer() {
        // read-only access to long term sessions.
        TIntList counts = new TIntArrayList(10);
        for (final SessionMap longTermMap : longTermList) {
            counts.add(longTermMap.size());
        }
        return counts.toArray();
    }

    SessionControl getSessionByAlternativeId(final String altId) {
        SessionControl control = null;

        boolean first = true;
        for (SessionContainer container : sessionList) {
            control = container.getSessionByAlternativeId(altId);
            if (null != control) {
                if (false == first) {
                    // Schedule task to put session into first container and remove from latter one.
                    scheduleTask2MoveSession2FirstContainer(control.getSession().getSessionID(), false);
                }
                return control;
            }
            first = false;
        }

        for (Iterator<SessionMap> iterator = longTermList.iterator(); null == control && iterator.hasNext();) {
            control = iterator.next().getByAlternativeId(altId);
            if (null != control) {
                scheduleTask2MoveSession2FirstContainer(control.getSession().getSessionID(), true);
            }
        }
        return control;
    }

    SessionControl getSession(final String sessionId) {
        SessionControl control = null;
        boolean first = true;
        for (SessionContainer container : sessionList) {
            control = container.getSessionById(sessionId);
            if (control != null) {
                if (false == first) {
                    // Schedule task to put session into first container and remove from latter one.
                    scheduleTask2MoveSession2FirstContainer(sessionId, false);
                }
                return control;
            }
            first = false;
        }

        for (Iterator<SessionMap> iterator = longTermList.iterator(); null == control && iterator.hasNext();) {
            control = iterator.next().getBySessionId(sessionId);
            if (null != control) {
                scheduleTask2MoveSession2FirstContainer(sessionId, true);
            }
        }
        return control;
    }

    SessionControl optShortTermSession(final String sessionId) {
        SessionControl control = null;
        for (SessionContainer container : sessionList) {
            if ((control = container.getSessionById(sessionId)) != null) {
                return control;
            }
        }

        return control;
    }

    SessionControl getSessionByRandomToken(final String randomToken) {
        // A read-only access to session and a write access to random list
        final String sessionId = randoms.remove(randomToken);
        if (null == sessionId) {
            return null;
        }

        final SessionControl sessionControl = getSession(sessionId);
        if (null == sessionControl) {
            LOG.error("Unable to get session for sessionId: {}.", sessionId);
            SessionHandler.clearSession(sessionId, true);
            return null;
        }
        final SessionImpl session = sessionControl.getSession();
        if (!randomToken.equals(session.getRandomToken())) {
            final OXException e = SessionExceptionCodes.WRONG_BY_RANDOM.create(session.getSessionID(), session.getRandomToken(), randomToken, sessionId);
            LOG.error("", e);
            SessionHandler.clearSession(sessionId, true);
            return null;
        }
        session.removeRandomToken();
        if (sessionControl.getCreationTime() + randomTokenTimeout < System.currentTimeMillis()) {
            SessionHandler.clearSession(sessionId, true);
            return null;
        }
        return sessionControl;
    }

    SessionControl clearSession(final String sessionId) {
        // Look-up in short-term list
        for (SessionContainer container : sessionList) {
            SessionControl sessionControl = container.removeSessionById(sessionId);
            if (null != sessionControl) {
                Session session = sessionControl.getSession();

                String random = session.getRandomToken();
                if (null != random) {
                    // If session is accessed through random token, random token is removed in the session.
                    randoms.remove(random);
                }

                unscheduleTask2MoveSession2FirstContainer(sessionId, true);
                metricsDecreaseShort(1);
                metricsDecreaseAll(1);
                return sessionControl;
            }
        }

        // Look-up in long-term list
        for (SessionMap longTermMap : longTermList) {
            SessionControl sessionControl = longTermMap.removeBySessionId(sessionId);
            if (null != sessionControl) {
                Session session = sessionControl.getSession();

                String random = session.getRandomToken();
                if (null != random) {
                    // If session is accessed through random token, random token is removed in the session.
                    randoms.remove(random);
                }

                unscheduleTask2MoveSession2FirstContainer(sessionId, true);
                metricsDecreaseLong(1);
                metricsDecreaseAll(1);
                return sessionControl;
            }
        }

        // No such session...
        return null;
    }

    List<SessionControl> getShortTermSessions() {
        // A read-only access
        final List<SessionControl> retval = new LinkedList<SessionControl>();
        for (final SessionContainer container : sessionList) {
            retval.addAll(container.getSessionControls());
        }
        return retval;
    }

    List<String> getShortTermSessionIDs() {
        // A read-only access
        List<String> retval = new LinkedList<String>();
        for (SessionContainer container : sessionList) {
            retval.addAll(container.getSessionIDs());
        }
        return retval;
    }

    List<SessionControl> getLongTermSessions() {
        // A read-only access
        List<SessionControl> retval = new LinkedList<SessionControl>();
        for (final SessionMap longTermMap : longTermList) {
            retval.addAll(longTermMap.values());
        }
        return retval;
    }

    void move2FirstContainer(final String sessionId) {
        Iterator<SessionContainer> iterator = sessionList.iterator();
        SessionContainer firstContainer = iterator.next(); // Skip first container

        // Look for associated session in successor containers
        SessionControl control = null;
        try {
            while (null == control && iterator.hasNext()) {
                // Remove from current container & put into first one
                control = iterator.next().removeSessionById(sessionId);
                if (null != control) {
                    firstContainer.putSessionControl(control);
                }
            }

            if (null == control) {
                if (firstContainer.containsSessionId(sessionId)) {
                    LOG.warn("Somebody else moved session to most up-to-date container.");
                } else {
                    LOG.debug("Was not able to move the session {} into the most up-to-date container since it has already been removed in the meantime", sessionId);
                }
            }
        } catch (OXException e) {
            LOG.error("", e);
        } catch (IndexOutOfBoundsException e) {
            // About to shut-down
            LOG.error("First session container does not exist. Likely SessionD is shutting down...", e);
        }

        unscheduleTask2MoveSession2FirstContainer(sessionId, false);
        if (null != control) {
            SessionHandler.postSessionTouched(control.getSession());
        }
    }

    void move2FirstContainerLongTerm(final String sessionId) {
        SessionControl control = null;
        try {
            SessionContainer firstContainer = sessionList.get(0);
            boolean movedSession = false;
            int moved=0;
            for (Iterator<SessionMap> iterator = longTermList.iterator(); !movedSession && iterator.hasNext();) {
                SessionMap longTermMap = iterator.next();
                control = longTermMap.removeBySessionId(sessionId);
                if (null != control) {
                    firstContainer.putSessionControl(control);
                    final SessionImpl session = control.getSession();
                    longTermUserGuardian.remove(session.getUserId(), session.getContextId());
                    LOG.trace("Moved from long term container to first one.");
                    movedSession = true;
                    moved++;
                }
            }
            if (!movedSession) {
                if (firstContainer.containsSessionId(sessionId)) {
                    LOG.warn("Somebody else moved session to most actual container.");
                } else {
                    LOG.warn("Was not able to move the session into the most actual container.");
                }
                metricsDecreaseLong(moved);
                metricsIncreaseShort(moved);
            }
        } catch (final OXException e) {
            LOG.error("", e);
        } catch (IndexOutOfBoundsException e) {
            // About to shut-down
            LOG.error("First session container does not exist. Likely SessionD is shutting down...", e);
        }

        unscheduleTask2MoveSession2FirstContainer(sessionId, false);
        if (null != control) {
            SessionHandler.postSessionReactivation(control.getSession());
        }
    }

    void removeRandomToken(final String randomToken) {
        randoms.remove(randomToken);
    }

    public void addThreadPoolService(final ThreadPoolService service) {
        threadPoolService.set(service);
    }

    public void removeThreadPoolService() {
        threadPoolService.set(null);
    }

    private void scheduleTask2MoveSession2FirstContainer(final String sessionId, final boolean longTerm) {
        Move2FirstContainerTask task = tasks.get(sessionId);
        if (null != task) {
            LOG.trace("Found an already existing task to move session to first container.");
            return;
        }
        {
            final Move2FirstContainerTask ntask = new Move2FirstContainerTask(sessionId, longTerm);
            task = tasks.putIfAbsent(sessionId, ntask);
            if (null != task) {
                LOG.trace("Found an already existing task to move session to first container.");
                return;
            }
            task = ntask;
        }
        final ThreadPoolService threadPoolService = this.threadPoolService.get();
        if (null == threadPoolService) {
            final Move2FirstContainerTask tmp = task;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        tmp.call();
                    } catch (final Exception e) {
                        LOG.error("Moving session to first container failed.", e);
                    }
                }
            }, "Move2FirstContainer").start();
        } else {
            threadPoolService.submit(task);
        }
    }

    private void unscheduleTask2MoveSession2FirstContainer(String sessionId, boolean deactivateIfPresent) {
        final Move2FirstContainerTask task = tasks.remove(sessionId);
        if (deactivateIfPresent && null != task) {
            task.deactivate();
        }
    }

    private class Move2FirstContainerTask extends AbstractTask<Void> {

        private final String sessionId;
        private final boolean longTerm;
        private volatile boolean deactivated = false;

        Move2FirstContainerTask(final String sessionId, final boolean longTerm) {
            super();
            this.sessionId = sessionId;
            this.longTerm = longTerm;
        }

        public void deactivate() {
            deactivated = true;
        }

        @Override
        public Void call() {
            if (deactivated) {
                return null;
            }
            if (longTerm) {
                move2FirstContainerLongTerm(sessionId);
            } else {
                move2FirstContainer(sessionId);
            }
            return null;
        }
    }

    /**
     * Adds the specified timer service.
     *
     * @param service The timer service
     */
    public void addTimerService(final TimerService service) {
        timerService.set(service);
    }

    /**
     * Removes the timer service
     */
    public void removeTimerService() {
        for (final ScheduledTimerTask timerTask : removers.values()) {
            timerTask.cancel();
        }
        timerService.set(null);
    }

    private void scheduleRandomTokenRemover(final String randomToken) {
        final RandomTokenRemover remover = new RandomTokenRemover(randomToken);
        final TimerService timerService = this.timerService.get();
        if (null == timerService) {
            remover.run();
        } else {
            final ScheduledTimerTask timerTask = timerService.schedule(remover, randomTokenTimeout, TimeUnit.MILLISECONDS);
            removers.put(randomToken, timerTask);
        }
    }

    private class RandomTokenRemover implements Runnable {

        private final String randomToken;

        RandomTokenRemover(final String randomToken) {
            super();
            this.randomToken = randomToken;
        }

        @Override
        public void run() {
            try {
                removers.remove(randomToken);
                removeRandomToken(randomToken);
            } catch (final Throwable t) {
                LOG.error("", t);
            }
        }
    }

}
