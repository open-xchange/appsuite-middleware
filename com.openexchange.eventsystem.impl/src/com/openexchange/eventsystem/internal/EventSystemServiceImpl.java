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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.eventsystem.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.eventsystem.Event;
import com.openexchange.eventsystem.EventPublicationClaimer;
import com.openexchange.eventsystem.EventSystemExceptionCodes;
import com.openexchange.eventsystem.EventSystemService;
import com.openexchange.exception.OXException;
import com.openexchange.ms.MessageListener;
import com.openexchange.ms.MsService;
import com.openexchange.ms.Queue;
import com.openexchange.ms.Topic;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link EventSystemServiceImpl} - An event service using {@link MsService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class EventSystemServiceImpl implements EventSystemService {

    private static final String NAME_TOPIC = EventSystemConstants.NAME_TOPIC;
    private static final String NAME_QUEUE = EventSystemConstants.NAME_QUEUE;

    protected final ServiceLookup services;
    protected final EventPublicationClaimerImpl publicationClaimer;
    private volatile ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link EventSystemServiceImpl}.
     *
     * @throws OXException If initialization fails
     */
    public EventSystemServiceImpl(final ServiceLookup services, final EventHandlerTracker handlers) throws OXException {
        super();
        this.services = services;

        final MsService msService = getMsService();
        final Topic<Map<String, Object>> topic = msService.getTopic(NAME_TOPIC);
        final Queue<Map<String, Object>> queue = msService.getQueue(NAME_QUEUE);

        final MessageListener<Map<String, Object>> messageListener = new EventDistributingMessageListener(handlers, true);
        topic.addMessageListener(messageListener);
        queue.addMessageListener(messageListener);

        publicationClaimer = new EventPublicationClaimerImpl(services, this);
    }

    /**
     * Starts the timer.
     */
    public void startTimer() {
        if (null == timerTask) {
            synchronized (this) {
                if (null == timerTask) {
                    final TimerService service = services.getService(TimerService.class);
                    if (service != null) {
                        final Runnable task = new Runnable() {

                            @Override
                            public void run() {
                                final DatabaseService databaseService = services.getService(DatabaseService.class);
                                if (null != databaseService) {
                                    final Set<Integer> contextIds = publicationClaimer.getContextIds();
                                    if (false == contextIds.isEmpty()) {
                                        final long minStamp = System.currentTimeMillis() - 300000L; // Older than 5 minutes
                                        for (final Integer contextId : contextIds) {
                                            try {
                                                forContextId(contextId.intValue(), minStamp, databaseService);
                                            } catch (final Exception e) {
                                                // Ignore
                                            }
                                        }
                                    }
                                }
                            }

                            private boolean forContextId(final int contextId, final long minStamp, final DatabaseService databaseService) throws OXException {
                                final Connection con = databaseService.getWritable(contextId);
                                PreparedStatement stmt = null;
                                try {
                                    stmt = con.prepareStatement("DELETE FROM eventSystemClaim WHERE cid=? AND lastModified < ?");
                                    stmt.setInt(1, contextId);
                                    stmt.setLong(2, minStamp);
                                    return stmt.executeUpdate() > 0;
                                } catch (final SQLException e) {
                                    throw EventSystemExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                                } finally {
                                    Databases.closeSQLStuff(stmt);
                                    databaseService.backWritable(contextId, con);
                                }
                            }
                        };
                        timerTask = service.scheduleWithFixedDelay(task, 5, 5, TimeUnit.MINUTES);
                    }
                }
            }
        }
    }

    /**
     * Shuts-down this event system.
     */
    public void shutdown() {
        final MsService service = services.getOptionalService(MsService.class);
        if (null != service) {
            service.getTopic(NAME_TOPIC).cancel();
        }

        final ScheduledTimerTask timerTask = this.timerTask;
        if (null != timerTask) {
            timerTask.cancel();
            this.timerTask = null;
        }
    }

    private MsService getMsService() throws OXException {
        final MsService service = services.getService(MsService.class);
        if (null == service) {
            throw ServiceExceptionCode.serviceUnavailable(MsService.class);
        }
        return service;
    }

    @Override
    public void publish(final Event event) throws OXException {
        if (null == event) {
            return;
        }
        final MsService msService = getMsService();
        final Topic<Map<String, Object>> topic = msService.getTopic(NAME_TOPIC);
        topic.publish(EventUtility.wrap(event));
    }

    @Override
    public void deliver(final Event event) throws OXException {
        if (null == event) {
            return;
        }
        final MsService msService = getMsService();
        final Queue<Map<String, Object>> queue = msService.getQueue(NAME_QUEUE);
        queue.offer(EventUtility.wrap(event));
    }

    @Override
    public EventPublicationClaimer getClaimer() throws OXException {
        return publicationClaimer;
    }

}
