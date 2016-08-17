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

package com.openexchange.drive.events.ms;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.events.internal.DriveEventServiceImpl;
import com.openexchange.exception.OXException;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageListener;
import com.openexchange.ms.MsService;
import com.openexchange.ms.PortableMsService;
import com.openexchange.ms.Topic;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link MsDriveEventHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class MsDriveEventHandler implements DriveEventPublisher, MessageListener<PortableDriveEvent> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MsDriveEventHandler.class);
    private static final String TOPIC_NAME = "driveEvents-0";
    private static final AtomicReference<PortableMsService> MS_REFERENCE = new AtomicReference<PortableMsService>();

    private final DriveEventServiceImpl driveEventService;
    private final String senderId;

    /**
     * Sets the specified {@link MsService}.
     *
     * @param service The {@link MsService}
     */
    public static void setMsService(final PortableMsService service) {
        MS_REFERENCE.set(service);
    }

    /**
     * Initializes a new {@link MsDriveEventHandler}.
     *
     * @throws OXException
     */
    public MsDriveEventHandler(DriveEventServiceImpl driveEventService) throws OXException {
        super();
        this.driveEventService = driveEventService;
        driveEventService.registerPublisher(this);
        Topic<PortableDriveEvent> topic = getTopic();
        this.senderId = topic.getSenderId();
        topic.addMessageListener(this);
    }

    public void stop() {
        driveEventService.unregisterPublisher(this);
        try {
            getTopic().removeMessageListener(this);
        } catch (OXException e) {
            LOG.warn("Error removing message listener", e);
        }
    }

    @Override
    public void publish(DriveEvent event) {
        if (null != event && false == event.isRemote()) {
            LOG.debug("publishing drive event: {} [{}]", event, senderId);
            try {
                getTopic().publish(PortableDriveEvent.wrap(event));
            } catch (OXException e) {
                LOG.warn("Error publishing drive event", e);
            }
        }
    }

    @Override
    public void onMessage(Message<PortableDriveEvent> message) {
        if (null != message && message.isRemote()) {
            PortableDriveEvent driveEvent = message.getMessageObject();
            if (null != driveEvent) {
                LOG.debug("onMessage: {} [{}]", message.getMessageObject(), message.getSenderId());
                driveEventService.notifyPublishers(PortableDriveEvent.unwrap(driveEvent));
            } else {
                LOG.warn("Discarding empty drive event message.");
            }
        }
    }

    @Override
    public boolean isLocalOnly() {
        return true;
    }

    private Topic<PortableDriveEvent> getTopic() throws OXException {
        PortableMsService msService = MS_REFERENCE.get();
        if (null == msService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(PortableMsService.class.getName());
        }
        return msService.getTopic(TOPIC_NAME);
    }

}
