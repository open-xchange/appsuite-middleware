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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.gdpr.dataexport.impl.notification;

import static com.openexchange.gdpr.dataexport.impl.DataExportUtility.stringFor;
import static com.openexchange.java.Autoboxing.I;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportStorageService;
import com.openexchange.gdpr.dataexport.HostInfo;
import com.openexchange.gdpr.dataexport.impl.osgi.Services;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.mail.transport.config.NoReplyConfig;
import com.openexchange.notification.mail.MailData;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.User;

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
                // Set personal for no-reply address
                {
                    TranslatorFactory factory = Services.optService(TranslatorFactory.class);
                    if (null == factory) {
                        throw ServiceExceptionCode.absentService(TranslatorFactory.class);
                    }
                    User user = DataExportNotificationMail.getUser(userId, contextId);
                    Translator translator = factory.translatorFor(user.getLocale());
                    message.setHeader(NoReplyConfig.HEADER_NO_REPLY_PERSONAL, translator.translate(DataExportNotificationStrings.NO_REPLY_PERSONAL));
                }
                transport.sendMailMessage(message, ComposeType.NEW);
            } finally {
                transport.close();
            }
        } catch (Exception e) {
            LoggerHolder.LOG.warn("Failed to send {} notification message for data export task {} of user {} in context {}", reason.toString(), stringFor(taskId), I(userId), I(contextId), e);
        }
    }

}
