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

package com.openexchange.unifiedinbox;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link UnifiedInboxExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class UnifiedInboxExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link UnifiedInboxExceptionMessage}.
     */
    private UnifiedInboxExceptionMessage() {
        super();
    }

    // Unified Mail does not support to create folders.
    public final static String CREATE_DENIED_MSG = "Unified Mail does not support folder creation.";

    // Unified Mail does not support to delete folders.
    public final static String DELETE_DENIED_MSG = "Unified Mail does not support folder deletion.";

    // Unified Mail does not support to update folders.
    public final static String UPDATE_DENIED_MSG = "Unified Mail does not support folder update.";

    // Unified Mail does not support to move messages.
    public final static String MOVE_MSGS_DENIED_MSG = "Unified Mail does not support to move messages.";

    // Unified Mail does not support to copy messages.
    public final static String COPY_MSGS_DENIED_MSG = "Unified Mail does not support to copy messages.";

    // Append messages failed.
    public final static String APPEND_MSGS_DENIED_MSG = "Append messages failed.";

    // Unified Mail does not support draft messages.
    public final static String DRAFTS_NOT_SUPPORTED_MSG = "Unified Mail does not support draft messages.";

    // Unified Mail does not support to move folders.
    public final static String MOVE_DENIED_MSG = "Unified Mail does not support to move folders.";

    // Unified Mail does not support mail folder creation
    public final static String FOLDER_CREATION_FAILED_MSG = "Unified Mail does not support mail folder creation";

    // Unified Mail does not support to clear INBOX folder.
    public final static String CLEAR_NOT_SUPPORTED_MSG = "Unified Mail does not support to clear INBOX folder.";

    // No connection available to access mailbox
    public final static String NOT_CONNECTED_MSG = "No connection available to access mailbox.";

    // Unknown default folder fullname: %1$s.
    public final static String UNKNOWN_DEFAULT_FOLDER_INDEX_MSG = "Unknown default folder full name: %1$s.";

    // Move operation aborted. Source and destination folder are equal.
    public final static String NO_EQUAL_MOVE_MSG = "Move operation aborted. Source and destination folder are the same.";

    // Request aborted due to timeout of %1$s %2$s.
    public final static String TIMEOUT_MSG = "Request aborted due to timeout of %1$s %2$s.";

    // Invalid destination folder. Don't know where to append the mails.
    public final static String INVALID_DESTINATION_FOLDER_MSG = "Invalid destination folder. E-Mails cannot be appended.";

}
