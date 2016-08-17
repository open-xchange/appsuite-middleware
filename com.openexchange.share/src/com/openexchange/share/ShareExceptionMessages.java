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

    /**
     * Prevent instantiation.
     */
    private ShareExceptionMessages() {
        super();
    }
}
