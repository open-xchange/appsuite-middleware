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

package com.openexchange.gdpr.dataexport.impl.notification;

import static com.openexchange.gdpr.dataexport.impl.DataExportUtility.stringFor;
import static com.openexchange.java.Autoboxing.I;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportStorageService;
import com.openexchange.gdpr.dataexport.HostInfo;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.notification.mail.MailData;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DataExportNotificationSender} - Utility class for sending notification messages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportNotificationSender {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DataExportNotificationSender.class);
    }

    /**
     * Initializes a new {@link DataExportNotificationSender}.
     */
    private DataExportNotificationSender() {
        super();
    }

    /**
     * Sends the notification message to specified user.
     *
     * @param reason The notification reason
     * @param creationDate The date when the data export has been created/requested
     * @param expiryDate The expiration date (only expected if reason is set to {@link Reason#SUCCESS})
     * @param hostInfo The basic host information (only expected if reason is set to {@link Reason#SUCCESS})
     * @param taskId The task identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param markNotificationSent Whether to set the notification-sent marker
     * @param services The service look-up
     * @throws OXException If operation fails to set/unset notification-sent marker
     */
    public static void sendNotificationAndSetMarker(Reason reason, Date creationDate, Date expiryDate, HostInfo hostInfo, UUID taskId, int userId, int contextId, boolean markNotificationSent, ServiceLookup services) throws OXException {
        if (!markNotificationSent) {
            // Only send notification message w/o any storage operation
            sendNotification(reason, creationDate, expiryDate, hostInfo, taskId, userId, contextId, services);
            return;
        }

        // Acquire needed storage service
        DataExportStorageService storageService = services.getServiceSafe(DataExportStorageService.class);

        // Try to mark task that notification has been sent. Revert it laster on if actual transport fails.
        boolean markerSet = storageService.setNotificationSent(taskId, userId, contextId);
        if (!markerSet) {
            // Assume that notification has already been sent
            return;
        }

        // Marker set
        boolean error = true;
        try {
            // Send notification message
            sendNotification(reason, creationDate, expiryDate, hostInfo, taskId, userId, contextId, services);
            error = false;
        } finally {
            if (error) {
                try {
                    storageService.unsetNotificationSent(taskId, userId, contextId);
                } catch (Exception e) {
                    LoggerHolder.LOG.warn("Cannot unset notification-sent marker for data export task {} of user {} in context {}", stringFor(taskId), I(userId), I(contextId), e);
                }
            }
        }
    }

    private static void sendNotification(Reason reason, Date creationDate, Date expiryDate, HostInfo hostInfo, UUID taskId, int userId, int contextId, ServiceLookup services) {
        try {
            TransportProvider transportProvider = TransportProviderRegistry.getTransportProvider("smtp");
            MailTransport transport = transportProvider.createNewNoReplyTransport(contextId);
            try {
                NotificationMailFactory notify = services.getServiceSafe(NotificationMailFactory.class);
                MailData mailData = DataExportNotificationMail.createNotificationMail(reason, creationDate, expiryDate, hostInfo, userId, contextId);
                ComposedMailMessage message = notify.createMail(mailData);
                transport.sendMailMessage(message, ComposeType.NEW);
            } finally {
                transport.close();
            }
        } catch (Exception e) {
            LoggerHolder.LOG.warn("Failed to send {} notification message for data export task {} of user {} in context {}", reason.toString(), stringFor(taskId), I(userId), I(contextId), e);
        }
    }

}
