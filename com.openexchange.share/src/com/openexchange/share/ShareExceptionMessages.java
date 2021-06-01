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

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ShareExceptionMessages}
 *
 * Translatable messages for {@link ShareExceptionCodes}.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public final class ShareExceptionMessages implements LocalizableStrings {

    public static final String UNKNOWN_SHARE_MSG = "The requested share does not exist.";

    public static final String INVALID_TOKEN_MSG = "The token \"%1$s\" is invalid.";

    public static final String INVALID_LINK_MSG = "The link \"%1$s\" is invalid.";

    public static final String INVALID_MAIL_ADDRESS_MSG = "\"%1$s\" is not a valid email address.";

    public static final String CONCURRENT_MODIFICATION_MSG = "The share has been modified in the meantime. Please refresh or synchronize and try again.";

    public static final String NO_DELETE_PERMISSIONS_MSG = "You don't have sufficient permissions to delete the share.";

    public static final String NO_EDIT_PERMISSIONS_MSG = "You don't have sufficient permissions to edit the share.";

    public static final String NO_SHARE_PERMISSIONS_MSG = "You don't have sufficient permissions to share folder or item \"%2$s\".";

    public static final String SHARING_NOT_SUPPORTED = "Module %1$s does not support sharing.";

    public static final String SHARING_ITEMS_NOT_SUPPORTED = "Module %1$s does not support sharing of items.";

    public static final String SHARING_FOLDERS_NOT_SUPPORTED = "Module %1$s does not support sharing of folders.";

    public static final String NO_SHARING_WITH_YOURSELF = "You can't share with yourself.";

    public static final String NO_SHARE_LINK_PERMISSION_MSG = "You don't have sufficient permissions to share a link.";

    public static final String NO_INVITE_GUEST_PERMISSION_MSG = "You don't have sufficient permissions to invite guests.";

    public static final String NO_MULTIPLE_TARGETS_LINK_MSG = "You can't create links for multiple targets.";

    public static final String LINK_ALREADY_EXISTS_MSG = "You cannot create more than one link per folder or item.";

    public static final String INVALID_LINK_PERMISSIONS_MSG = "The set permissions for the link are invalid.";

    public static final String SHARE_NOT_AVAILABLE_MSG = "The share you are looking for is currently not available. Please try again later.";

    public static final String NO_SUBSCRIBE_SHARE_PERMISSION_MSG = "You don't have sufficient permissions to subscribe to a share.";
    
    public static final String NO_FILE_SUBSCRIBE_MSG = "Subscribing to a single file is not supported";
    
    public static final String NO_SUBSCRIBE_PERMISSION_MSG = "You are not allowed to subscribe to a share for %1$s";
    
    public static final String NO_SUBSCRIBE_SHARE_ANONYMOUS_MSG = "You are not allowed to subscribe to an anonymous share";

    public static final String NO_UNSUBSCRIBE_FOLDER_MSG = "You are not allowed to unsubscribe folder %1$s";

    /**
     * Prevent instantiation.
     */
    private ShareExceptionMessages() {
        super();
    }
}
