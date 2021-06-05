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

import java.util.Date;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;

/**
 * A notification to inform arbitrary recipients about created links.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface LinkCreatedNotification<T> extends ShareNotification<T> {

    /**
     * Gets the session of the new shares creator.
     *
     * @return The session
     */
    Session getSession();

    /**
     * Gets the share target to notify the recipient about.
     *
     * @return The share target, never <code>null</code>
     */
    ShareTarget getShareTarget();

    /**
     * Gets an optional message that will be shown to the recipient if appropriate. Whether a message is shown or not depends on the
     * {@link NotificationType}.
     *
     * @return The message or <code>null</code>, if nothing was provided
     */
    String getMessage();

    /**
     * Get the id of the user something is shared to
     *
     * @return The user ID
     */
    int getTargetUserID();

    /**
     * Gets the URL to the according share.
     *
     * @return The URL
     */
    String getShareUrl();

    /**
     * If defined, gets the date when this share expires, i.e. it should be no longer accessible.
     *
     * @return The expiry date of the share, or <code>null</code> if not defined
     */
    Date getExpiryDate();

    /**
     * Gets the password that is necessary to access the link.
     *
     * @return The password or <code>null</code> if none was set.
     */
    String getPassword();

}
