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

package com.openexchange.chronos.scheduling.impl;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.scheduling.ChangeNotification;
import com.openexchange.chronos.scheduling.ScheduleStatus;
import com.openexchange.chronos.scheduling.SchedulingBroker;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.TransportProvider;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link SchedulingBrokerImpl}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
@SingletonService
public class SchedulingBrokerImpl extends RankingAwareNearRegistryServiceTracker<TransportProvider> implements SchedulingBroker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulingBrokerImpl.class);

    /**
     * Initializes a new {@link SchedulingBrokerImpl}.
     * 
     * @param context The {@link BundleContext}
     */
    public SchedulingBrokerImpl(BundleContext context) {
        super(context, TransportProvider.class);
    }

    @Override
    public List<ScheduleStatus> handleScheduling(Session session, List<SchedulingMessage> messages) {
        List<ScheduleStatus> result = new LinkedList<>();
        for (SchedulingMessage message : messages) {
            try {
                ScheduleStatus status = handle(session, message);
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("An message for {} sent by {} has been handeled with the status {}.", message.getRecipient(), message.getOriginator(), status);
                }
                result.add(status);
            } catch (Throwable t) {
                LOGGER.debug("Unable to send message", t);
            }
        }
        return result;
    }

    /**
     * Defined {@link ScheduleStatus} in which sending can be considered a success.
     */
    private static final EnumSet<ScheduleStatus> SUCCESS = EnumSet.of(ScheduleStatus.SENT, ScheduleStatus.DELIVERED);

    private ScheduleStatus handle(Session session, SchedulingMessage message) {
        if (null != message && null != session) {
            /*
             * Try to send message. Stop once the message is send successfully
             */
            for (Iterator<TransportProvider> iterator = iterator(); iterator.hasNext();) {
                TransportProvider transportProvider = iterator.next();
                ScheduleStatus status = transportProvider.send(session, message);
                if (SUCCESS.contains(status)) {
                    return status;
                }
            }
        }
        return ScheduleStatus.NO_TRANSPORT;
    }

    @Override
    public List<ScheduleStatus> handleNotifications(Session session, List<ChangeNotification> notifications) {
        List<ScheduleStatus> result = new LinkedList<>();
        for (ChangeNotification message : notifications) {
            try {
                ScheduleStatus status = handleNotification(session, message);
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("An message for {} sent by {} has been handeled with the status {}.", message.getRecipient(), message.getOriginator(), status);
                }
                result.add(status);
            } catch (Throwable t) {
                LOGGER.debug("Unable to send message", t);
            }
        }
        return result;
    }

    private ScheduleStatus handleNotification(Session session, ChangeNotification notification) {
        if (null != notification && null != session) {
            /*
             * Try to send message. Stop once the message is send successfully
             */
            for (Iterator<TransportProvider> iterator = iterator(); iterator.hasNext();) {
                TransportProvider transportProvider = iterator.next();
                ScheduleStatus status = transportProvider.send(session, notification);
                if (SUCCESS.contains(status)) {
                    return status;
                }
            }
        }
        return ScheduleStatus.NO_TRANSPORT;
    }

}
