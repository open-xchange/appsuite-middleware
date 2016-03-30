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
