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

package com.openexchange.share.notification.impl;

import java.util.Locale;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.share.notification.ShareNotificationService.Transport;


/**
 * A {@link ShareNotification} encapsulates all information necessary to notify
 * the according recipient about a share and provide her a link to access that share.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface ShareNotification<T> {

    /**
     * Gets the transport that shall be used to deliver this notification.
     *
     * @return The {@link Transport}, never <code>null</code>
     */
    Transport getTransport();

    /**
     * Gets the type of this notification (e.g. "a share has been created").
     *
     * @return The {@link NotificationType}, never <code>null</code>
     */
    NotificationType getType();

    /**
     * Gets the transport information used to notify the recipient.
     *
     * @return The transport information, never <code>null</code>
     */
    T getTransportInfo();

    /**
     * Gets the ID of the context where the share is located.
     *
     * @return The context ID
     */
    int getContextID();

    /**
     * Gets the locale used to translate the notification message before it is sent out.
     *
     * @return The locale
     */
    Locale getLocale();

    /**
     * Gets host information of the HTTP request that led to the generation of this
     * notification.
     *
     * @return The host data
     */
    HostData getHostData();

}
