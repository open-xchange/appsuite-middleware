
package com.openexchange.share.subscription;
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

/**
 * {@link ShareLinkState} - States to indicate a possible usage of the link
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public enum ShareLinkState {

    /**
     * State to indicate that the link belongs to a known share and is accessible.
     */
    SUBSCRIBED,

    /**
     * State to indicate that the link belongs to a known share but is not accessible at the moment because the remote
     * server indicates that credentials have been updated meanwhile.
     */
    CREDENTIALS_REFRESH,

    /**
     * State to indicate that the link is valid and belongs to a share that is not yet subscribed an can be added.
     */
    ADDABLE,

    /**
     * Similar to {@link #ADDABLE} but in addition the user needs to enter a password to add the share.
     */
    ADDABLE_WITH_PASSWORD,

    /**
     * State to indicate that the link belongs to a known share but is inaccessible at the moment.
     */
    INACCESSIBLE,

    /**
     * State to indicate that the link belongs to a known share but can no longer be accessed.
     */
    REMOVED,

    /**
     * State to indicate that the share link can't be resolved at all and thus can't be subscribed.
     */
    UNRESOLVABLE,

    /**
     * State to indicate that the subscription of the share is not supported,
     * i.e. a single file in an unknown folder is shared or the share belongs to
     * an anonymous guest
     * <p>
     * This state describes technical limitations
     */
    UNSUPPORTED,

    /**
     * State to indicate that the subscription of the link is not allowed,
     * i.e. when the share belongs not to the current user.
     * <p>
     * This state describes permissions limitations
     */
    FORBIDDEN,

    /**
     * State to indicate that the link belongs to a known share but is not subscribed at the moment.
     */
    UNSUBSCRIBED;

}
