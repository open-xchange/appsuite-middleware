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

import java.util.List;
import com.openexchange.group.Group;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;

/**
 * A notification to inform users about created shares.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface ShareCreatedNotification<T> extends ShareNotification<T> {

    /**
     * Gets the session of the new shares creator.
     *
     * @return The session
     */
    Session getSession();

    /**
     * Gets the share targets to notify the recipient about.
     *
     * @return The share targets, never <code>null</code>
     */
    List<ShareTarget> getShareTargets();

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
     * Gets the group of the recipient user in cases where the group was added as permission entity and not
     * the user itself.
     *
     * @return group The group or <code>null</code> if the target was shared directly to the user
     */
    Group getTargetGroup();

}
