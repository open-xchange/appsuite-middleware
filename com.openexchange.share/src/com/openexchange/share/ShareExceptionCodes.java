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

import static com.openexchange.exception.OXExceptionStrings.SQL_ERROR_MSG;
import static com.openexchange.share.ShareExceptionMessages.INVALID_LINK_MSG;
import static com.openexchange.share.ShareExceptionMessages.INVALID_LINK_PERMISSIONS_MSG;
import static com.openexchange.share.ShareExceptionMessages.INVALID_MAIL_ADDRESS_MSG;
import static com.openexchange.share.ShareExceptionMessages.UNKNOWN_SHARE_MSG;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link ShareExceptionCodes}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public enum ShareExceptionCodes implements DisplayableOXExceptionCode {

    /** Unexpected database error: \"%1$s\" */
    DB_ERROR("Unexpected database error: \"%1$s\"", SQL_ERROR_MSG, Category.CATEGORY_WARNING, 1),

    /** The requested share does not exist. || A share with token \"%1$s\" does not exist. */
    UNKNOWN_SHARE("A share with token \"%1$s\" does not exist.", UNKNOWN_SHARE_MSG, Category.CATEGORY_USER_INPUT, 2),

    /** The link \"%1$s\" is invalid. */
    INVALID_LINK("A share with token \"%1$s\" does not exist.", INVALID_LINK_MSG, Category.CATEGORY_USER_INPUT, 3),

    /** An unexpected error occurred: %1$s */
    UNEXPECTED_ERROR("An unexpected error occurred: %1$s", null, Category.CATEGORY_ERROR, 4),

    /** \"%1$s\" is not a valid email address. */
    INVALID_MAIL_ADDRESS("\"%1$s\" is not a valid email address.", INVALID_MAIL_ADDRESS_MSG, Category.CATEGORY_USER_INPUT, 5),

    /** The share has been modified in the meantime. Please refresh or synchronize and try again. || The share \"%1$s\" has been modified in the meantime. */
    CONCURRENT_MODIFICATION("The share \"%1$s\" has been modified in the meantime.", ShareExceptionMessages.CONCURRENT_MODIFICATION_MSG, Category.CATEGORY_CONFLICT, 6),

    /** You don't have sufficient permissions to delete the share. || User %1$d has no delete permissions for share \"%2$s\" in context %3$d. */
    NO_DELETE_PERMISSIONS("User %1$d has no delete permissions for share \"%2$s\" in context %3$d.", ShareExceptionMessages.NO_DELETE_PERMISSIONS_MSG, Category.CATEGORY_PERMISSION_DENIED, 7),

    /** You don't have sufficient permissions to edit the share. || User %1$d has no edit permissions for share \"%2$s\" in context %3$d. */
    NO_EDIT_PERMISSIONS("User %1$d has no edit permissions for share \"%2$s\" in context %3$d.", ShareExceptionMessages.NO_EDIT_PERMISSIONS_MSG, Category.CATEGORY_PERMISSION_DENIED, 8),

    /** You don't have sufficient permissions to share folder or item \"%2$s\". || User %1$d has no share permissions for folder or item \"%2$s\" in context %3$d. */
    NO_SHARE_PERMISSIONS(" User %1$d has no share permissions for folder or item \"%2$s\" in context %3$d.", ShareExceptionMessages.NO_SHARE_PERMISSIONS_MSG, Category.CATEGORY_PERMISSION_DENIED, 9),

    /** An I/O error occurred: %1$s */
    IO_ERROR("An I/O error occurred: %1$s", null, Category.CATEGORY_ERROR, 10),

    /**
     * An SQL error occurred: %1$s
     */
    SQL_ERROR("An SQL error occurred: %1$s", null, Category.CATEGORY_ERROR, 11),

    /**
     * Module %1$s does not support sharing.
     */
    SHARING_NOT_SUPPORTED(ShareExceptionMessages.SHARING_NOT_SUPPORTED, ShareExceptionMessages.SHARING_NOT_SUPPORTED, Category.CATEGORY_USER_INPUT, 12),

    /**
     * Module %1$s does not support sharing of items.
     */
    SHARING_ITEMS_NOT_SUPPORTED(ShareExceptionMessages.SHARING_ITEMS_NOT_SUPPORTED, ShareExceptionMessages.SHARING_ITEMS_NOT_SUPPORTED, Category.CATEGORY_USER_INPUT, 13),

    /**
     * Module %1$s does not support sharing of folders.
     */
    SHARING_FOLDERS_NOT_SUPPORTED(ShareExceptionMessages.SHARING_FOLDERS_NOT_SUPPORTED, ShareExceptionMessages.SHARING_FOLDERS_NOT_SUPPORTED, Category.CATEGORY_USER_INPUT, 14),

    /**
     * User %1$d is not a guest user.
     */
    UNKNOWN_GUEST("User %1$d is not a guest user.", null, CATEGORY_ERROR, 15),

    /** The token \"%1$s\" is invalid. */
    INVALID_TOKEN("The token \"%1$s\" is invalid.", ShareExceptionMessages.INVALID_TOKEN_MSG, Category.CATEGORY_USER_INPUT, 16),

    /** You can't share with yourself. */
    NO_SHARING_WITH_YOURSELF(ShareExceptionMessages.NO_SHARING_WITH_YOURSELF, ShareExceptionMessages.NO_SHARING_WITH_YOURSELF, Category.CATEGORY_USER_INPUT, 17),

    /** You don't have sufficient permissions to share a link. **/
    NO_SHARE_LINK_PERMISSION(ShareExceptionMessages.NO_SHARE_LINK_PERMISSION_MSG, ShareExceptionMessages.NO_SHARE_LINK_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 18),

    /** You don't have sufficient permissions to invite guests. **/
    NO_INVITE_GUEST_PERMISSION(ShareExceptionMessages.NO_INVITE_GUEST_PERMISSION_MSG, ShareExceptionMessages.NO_INVITE_GUEST_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 19),

    /** Anonymous guests cannot be created via invite! **/
    NO_INVITE_ANONYMOUS("Anonymous guests cannot be created via invite!", null, Category.CATEGORY_ERROR, 20),

    /** You can't create links for multiple targets. **/
    NO_MULTIPLE_TARGETS_LINK(ShareExceptionMessages.NO_MULTIPLE_TARGETS_LINK_MSG, ShareExceptionMessages.NO_MULTIPLE_TARGETS_LINK_MSG, Category.CATEGORY_PERMISSION_DENIED, 21),

    /** You cannot create more than one link per folder or item. **/
    LINK_ALREADY_EXISTS(ShareExceptionMessages.LINK_ALREADY_EXISTS_MSG, ShareExceptionMessages.LINK_ALREADY_EXISTS_MSG, Category.CATEGORY_PERMISSION_DENIED, 22),

    /** A link for target [%1$s - %2$s - %2$s] does not exist. **/
    INVALID_LINK_TARGET("A link for target [%1$s - %2$s - %2$s] does not exist.", null, Category.CATEGORY_ERROR, 23),

    /** The set permissions for the link are invalid. */
    INVALID_LINK_PERMISSION(INVALID_LINK_PERMISSIONS_MSG, INVALID_LINK_PERMISSIONS_MSG, Category.CATEGORY_USER_INPUT, 24),

    /** You don't have sufficient permissions to subscribe to a share. */
    NO_SUBSCRIBE_SHARE_PERMISSION(ShareExceptionMessages.NO_SUBSCRIBE_SHARE_PERMISSION_MSG, ShareExceptionMessages.NO_SUBSCRIBE_SHARE_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 25),

    /** Subscribing to a single file is not supported */
    NO_FILE_SUBSCRIBE(ShareExceptionMessages.NO_FILE_SUBSCRIBE_MSG, ShareExceptionMessages.NO_FILE_SUBSCRIBE_MSG, Category.CATEGORY_ERROR, 26),

    /** You are not allowed to subscribe to a share for %1$s */
    NO_SUBSCRIBE_PERMISSION(ShareExceptionMessages.NO_SUBSCRIBE_PERMISSION_MSG, ShareExceptionMessages.NO_SUBSCRIBE_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 27),

    /** You are not allowed to subscribe to an anonymous share */
    NO_SUBSCRIBE_SHARE_ANONYMOUS(ShareExceptionMessages.NO_SUBSCRIBE_SHARE_ANONYMOUS_MSG, ShareExceptionMessages.NO_SUBSCRIBE_SHARE_ANONYMOUS_MSG, Category.CATEGORY_ERROR, 28),
    
    /** You are not allowed to unsubscribe folder %1$s */
    NO_UNSUBSCRIBE_FOLDER(ShareExceptionMessages.NO_UNSUBSCRIBE_FOLDER_MSG, ShareExceptionMessages.NO_UNSUBSCRIBE_FOLDER_MSG, Category.CATEGORY_ERROR, 29),

    ;

    private static final String PREFIX = "SHR";

    private final Category category;
    private final int number;
    private final String message;
    private final String displayMessage;

    private ShareExceptionCodes(String message, String displayMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
        this.number = detailNumber;
        this.category = category;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return null != displayMessage ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Throwable cause, Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }

}
