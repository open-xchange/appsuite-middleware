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

import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;

/**
 * {@link ShareInfo}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public interface ShareInfo {

    /**
     * Gets the share target from the sharing users point of view.
     *
     * @return The share target
     */
    ShareTarget getTarget();

    /**
     * Gets the share target from the recipients point of view.
     *
     * @return The share target
     */
    ShareTarget getDestinationTarget();

    /**
     * Gets additional information about the guest user the share is associated with.
     *
     * @return The guest information
     */
    GuestInfo getGuest();

    /**
     * Gets the (base) share URL to access the share as guest user and jump to the underlying target directly.
     *
     * @param hostData The host data of the current HTTP request to determine protocol, hostname and servlet prefix
     * @return The share URL as used to access the share as guest
     * @throws OXException in case the url cannot be generated.
     */
    String getShareURL(HostData hostData) throws OXException;

}
