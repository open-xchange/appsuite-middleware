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

package com.openexchange.share.notification;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareTargetPath;

/**
 * A service to notify arbitrary recipients about available shares.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface ShareNotificationService {

    /**
     * The transport mechanism to use for notification messages.
     */
    public enum Transport {
        MAIL("mail");

        private final String id;
        private Transport(String id) {
            this.id = id;
        }

        /**
         * Gets the transports unique identifier
         *
         * @return The identifier
         */
        public String getID() {
            return id;
        }

        /**
         * Gets the transport by its unique identifier
         *
         * @param id The identifier
         * @return The transport or <code>null</code> if the identifier is invalid
         */
        public static Transport forID(String id) {
            for (Transport t : values()) {
                if (t.getID().equals(id)) {
                    return t;
                }
            }

            return null;
        }
    }

    /**
     * Sends notifications about one or more created shares to multiple recipients.
     *
     * @param transport The type of {@link Transport} to use when sending notifications
     * @param entities The entities to notify
     * @param message The (optional) additional message for the notification. Can be <code>null</code>.
     * @param targetPath The path to the share target
     * @param session The session of the notifying user
     * @param hostData The host data to generate share links
     * @return Any exceptions occurred during notification, or an empty list if all was fine
     */
    List<OXException> sendShareCreatedNotifications(Transport transport, Entities entities, String message, ShareTargetPath targetPath, Session session, HostData hostData);

    /**
     * (Re-)Sends notifications about one or more shares to multiple recipients.
     *
     * @param transport The type of {@link Transport} to use when sending notifications
     * @param entities The entities to notify
     * @param message The (optional) additional message for the notification. Can be <code>null</code>.
     * @param targetPath The path to the share target
     * @param session The session of the notifying user
     * @param hostData The host data to generate share links
     * @return Any exceptions occurred during notification, or an empty list if all was fine
     */
    List<OXException> sendShareNotifications(Transport transport, Entities entities, String message, ShareTargetPath targetPath, Session session, HostData hostData);

    /**
     * Sends out notifications about a link to multiple recipients,
     *
     * @param transport The type of {@link Transport} to use when sending notifications
     * @param transportInfos The transport information for each recipient. The type must be chosen in accordance to the transport.
     * @param The (optional) additional message for the notification. Can be <code>null</code>.
     * @param link Share info of to the link to notify about
     * @param session The session of the notifying user
     * @param hostData The host data to generate share links
     * @return Any exceptions occurred during notification, or an empty list if all was fine
     */
    List<OXException> sendLinkNotifications(Transport transport, List<Object> transportInfos, String message, ShareInfo link, Session session, HostData hostData);

    /**
     * Send a notification mail that requests a confirmation for a requested password reset from the user.
     *
     * @param transport The type of {@link Transport} to use when sending notifications
     * @param guestInfo The guest info
     * @param confirmToken The confirm token to be part of the resulting link
     * @param hostData The host data to generate share links
     * @throws OXException
     */
    void sendPasswordResetConfirmationNotification(Transport transport, GuestInfo guestInfo, String confirmToken, HostData hostData) throws OXException;

}
