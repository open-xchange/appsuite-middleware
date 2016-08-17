/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
    CONCURRENT_MODIFICATION("The share \"%1$s\" has been modified in the meantime.",
        ShareExceptionMessages.CONCURRENT_MODIFICATION_MSG, Category.CATEGORY_CONFLICT, 6),

    /** You don't have sufficient permissions to delete the share. || User %1$d has no delete permissions for share \"%2$s\" in context %3$d. */
    NO_DELETE_PERMISSIONS("User %1$d has no delete permissions for share \"%2$s\" in context %3$d.",
        ShareExceptionMessages.NO_DELETE_PERMISSIONS_MSG, Category.CATEGORY_PERMISSION_DENIED, 7),

    /** You don't have sufficient permissions to edit the share. || User %1$d has no edit permissions for share \"%2$s\" in context %3$d. */
    NO_EDIT_PERMISSIONS("User %1$d has no edit permissions for share \"%2$s\" in context %3$d.",
        ShareExceptionMessages.NO_EDIT_PERMISSIONS_MSG, Category.CATEGORY_PERMISSION_DENIED, 8),

    /** You don't have sufficient permissions to share folder or item \"%2$s\". || User %1$d has no share permissions for folder or item \"%2$s\" in context %3$d. */
    NO_SHARE_PERMISSIONS(" User %1$d has no share permissions for folder or item \"%2$s\" in context %3$d.",
        ShareExceptionMessages.NO_SHARE_PERMISSIONS_MSG, Category.CATEGORY_PERMISSION_DENIED, 9),

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
