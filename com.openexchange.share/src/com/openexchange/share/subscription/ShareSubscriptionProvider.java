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

package com.openexchange.share.subscription;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.Ranked;
import com.openexchange.session.Session;

/**
 * {@link ShareSubscriptionProvider} - A provider that handles CRUD operations for a certain kind of share in a specific module
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public interface ShareSubscriptionProvider extends Ranked {

    /**
     * Gets a value indicating whether e.g. the given combination of module,
     * folder and item extracted from the share link is supported by this provider.
     *
     * @param session The session of the user
     * @param shareLink The share link to support
     * @return <code>true</code> in case this provider can be used for subsequent calls, <code>false</code> if not
     */
    boolean isSupported(Session session, String shareLink);

    /**
     * Analyzes the given share link
     *
     * @param session The session representing the acting user
     * @param shareLink The share link to access
     * @return A result indicating the action that can be performed, or <code>null</code> if not applicable
     * @throws OXException In case of an error
     */
    ShareLinkAnalyzeResult analyze(Session session, String shareLink) throws OXException;

    /**
     * Mounts a share represented by the link efficiently subscribing the share
     *
     * @param session The user session to bind the share to
     * @param shareLink The share link to mount
     * @param shareName The name to set for the share to mount, or <code>null</code> to use a default name
     * @param password The optional password for the share
     * @return The information about the mount
     * @throws OXException In case of error
     */
    ShareSubscriptionInformation subscribe(Session session, String shareLink, String shareName, String password) throws OXException;

    /**
     * Updates a mounted object
     *
     * @param session The user session
     * @param shareLink The share link to identify the mounted object
     * @param shareName The optional name to set for the share, or <code>null</code> to keep the existing one
     * @param password The password to set for the object
     * @return The information about the mount
     * @throws OXException In case of error
     */
    ShareSubscriptionInformation resubscribe(Session session, String shareLink, String shareName, String password) throws OXException;

    /**
     * Unmouts a share or rather deactivates the subscription
     * <p>
     * Implementations should always search for subscription for the given share link
     * nevertheless the link might not fit the expectations of {@link #isSupported(Session, String)}
     *
     * @param session The user session
     * @param shareLink The share link to identify the mounted object
     * @return <code>true</code> if the resource identified by the link was unmount, <code>false</code> otherwise
     * @throws OXException In case of missing service.
     */
    boolean unsubscribe(Session session, String shareLink) throws OXException;

}
