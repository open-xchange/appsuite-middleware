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

/**
 * Possible notification types. Meant to be extended for future requirements.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public enum NotificationType {
    /**
     * Notification type for a newly created share.
     * Use to send notifications about a new share to
     * a recipient of any type.
     */
    SHARE_CREATED("share-created"),

    /**
     * Notification type for a newly created link.
     * Use to send notifications about a new link to
     * a recipient of any type.
     */
    LINK_CREATED("link-created"),

    /**
     * Notification type for a password-reset that needs to be confirmed.
     * Use to send a request to confirm the password-reset to the share's recipient.
     */
    CONFIRM_PASSWORD_RESET("confirm-pwreset");

    private final String id;
    private NotificationType(String id) {
        this.id = id;
    }

    /**
     * Gets the type identifier
     *
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the notification for the given identifier
     *
     * @param id The id
     * @return The type or <code>null</code> if the identifier is invalid
     */
    public static NotificationType forId(String id) {
        for (NotificationType type : NotificationType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }

        return null;
    }

}