/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
