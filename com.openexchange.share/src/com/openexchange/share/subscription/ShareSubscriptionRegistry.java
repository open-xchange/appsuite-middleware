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
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link ShareSubscriptionRegistry}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
@SingletonService
public interface ShareSubscriptionRegistry {

    /**
     * Analyzes the given link
     *
     * @param session The session representing the acting user
     * @param shareLink The link to a share or rather subscription
     * @return A result indicating the action that can be performed for the link
     * @throws OXException In case of an error
     */
    ShareLinkAnalyzeResult analyze(Session session, String shareLink) throws OXException;

    /**
     * Subscribes to a share represented by the link. If the share is unknown a new
     * account for the share will be mounted, too.
     *
     * @param session The user session
     * @param shareLink The share link to subscribe
     * @param shareName The name to set for the share, or <code>null</code> to use a default value
     * @param password The optional password for the share, can be <code>null</code>
     * @return The information about the mount
     * @throws OXException In case the share can't be subscribed
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
     * @throws OXException In case the underlying account can't be updated or rather remounted
     */
    ShareSubscriptionInformation resubscribe(Session session, String shareLink, String shareName, String password) throws OXException;

    /**
     * Unsubscribes a share. This however will not delete the underlying account.
     * <p>
     * To delete the underlying account use the account API.
     *
     * @param session The user session
     * @param shareLink The share link to identify the subscription
     * @throws OXException In case the unsubscribe fails
     */
    void unsubscribe(Session session, String shareLink) throws OXException;

}
