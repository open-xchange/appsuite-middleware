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

package com.openexchange.share;

import java.util.Date;
import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.share.recipient.RecipientType;

/**
 * Handles the user specific parts of a share
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public interface GuestInfo {

    /**
     * Gets the authentication mode used for the guest user.
     *
     * @return The authentication mode
     */
    AuthenticationMode getAuthentication();

    /**
     * Gets the base token associated with the guest user.
     *
     * @return The base token
     */
    String getBaseToken();

    /**
     * Gets the e-mail address of the guest user if it denotes a named recipient.
     *
     * @return The e-mail address of the named share recipient, or <code>null</code> if the guest user is anonymous
     */
    String getEmailAddress();

    /**
     * Gets the password of the guest user in case the share recipient is anonymous and a password is required to access the share.
     *
     * @return The password of the anonymous share recipient, or <code>null</code> if no password is set or the guest user is not anonymous
     */
    String getPassword();

    /**
     * Gets the expiry date of the guest user in case the share recipient is anonymous and an expiry date is set for the share.
     *
     * @return The expiry date of the anonymous share recipient, or <code>null</code> if no expiry date is set or the guest user is not anonymous
     */
    Date getExpiryDate();

    /**
     * Gets the recipient type of the guest user.
     *
     * @return The recipient type
     */
    RecipientType getRecipientType();

    /**
     * Gets the user identifier of the guest.
     *
     * @return The guest user identifier
     */
    int getGuestID();

    /**
     * Gets the identifier of the context this guest user belongs to.
     *
     * @return The context identifier
     */
    int getContextID();

    /**
     * Gets the identifier of the user who initially created the guest user.
     *
     * @return The identifier of the user who initially created the guest
     */
    int getCreatedBy();

    /**
     * Gets the display name of the underlying guest user.
     *
     * @return The display name or <code>null</code> if the guest is anonymous or none is set.
     */
    String getDisplayName();

    /**
     * Gets the guest user's configured locale.
     *
     * @return The locale
     */
    Locale getLocale();

    /**
     * Returns the share target for an anonymous guest.
     *
     * @return The target or <code>null</code> if the guest is not anonymous
     */
    ShareTarget getLinkTarget();

    /**
     * Generates a share link to a specific target appropriate for the guest.
     *
     * @param hostData Host data
     * @param targetPath The share target path to create the link for, or <code>null</code> to generate a "base" link only
     * @return The share link
     * @throws OXException in case the link cannot be generated.
     */
    String generateLink(HostData hostData, ShareTargetPath targetPath) throws OXException;

}
