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

import com.openexchange.share.AuthenticationMode;

/**
 * A notification to send a link to confirm a password reset to a guest user who made use of the password reset mechanism. Such notifications must only be used
 * for shares with {@link AuthenticationMode#GUEST_PASSWORD}.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8
 */
public interface PasswordResetConfirmNotification<T> extends ShareNotification<T> {

    /**
     * Get the ID of the according guest user
     *
     * @return The user ID
     */
    int getGuestID();

    /**
     * Gets the URL to the according share.
     *
     * @return The URL
     */
    String getShareUrl();

    /**
     * Gets the URL to be visited to reset the password.
     *
     * @return The URL
     */
    String getConfirmPasswordResetUrl();

}
