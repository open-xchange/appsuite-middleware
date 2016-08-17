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

package com.openexchange.imap;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link IMAPExceptionMessages} - Error messages for IMAP bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPExceptionMessages implements LocalizableStrings {

    private IMAPExceptionMessages() {
        super();
    }

    public static final String NOT_CONNECTED_MSG = "No connection available to access mailbox";

    public static final String NO_MAIL_MODULE_ACCESS_MSG = "No mail module access due to user configuration";

    public static final String NO_ACCESS_MSG = "Access to mail folder is not permitted";

    public static final String NO_LOOKUP_ACCESS_MSG = "No lookup access to mail folder";

    public static final String NO_READ_ACCESS_MSG = "No read access to mail folder";

    public static final String NO_DELETE_ACCESS_MSG = "No delete access to mail folder";

    public static final String NO_INSERT_ACCESS_MSG = "No insert access to mail folder";

    public static final String NO_ADMINISTER_ACCESS_MSG = "No administer access to mail folder";

    public static final String NO_WRITE_ACCESS_MSG = "No write access to IMAP folder";

    public static final String NO_KEEP_SEEN_ACCESS_MSG = "No keep-seen access to mail folder";

    public static final String FOLDER_DOES_NOT_HOLD_FOLDERS_MSG = "Folder does not allow subfolders.";

    public static final String FOLDER_CREATION_FAILED_MSG = "Mail folder \"%1$s\" could not be created (maybe due to insufficient permission on parent folder %2$s or due to an invalid folder name)";

    public static final String NO_ADMINISTER_ACCESS_ON_INITIAL_MSG = "The composed rights could not be applied to new folder due to missing administer right in its initial rights specified by IMAP server. However, the folder has been created.";

    public static final String NO_ADMIN_ACL_MSG = "No administer permission specified for folder";

    public static final String NO_DEFAULT_FOLDER_UPDATE_MSG = "Default folder must not be updated";

    public static final String DELETE_FAILED_MSG = "Folder could not be deleted";

    public static final String NO_DEFAULT_FOLDER_CREATION_MSG = "IMAP default folder %1$s could not be created";

    public static final String MISSING_DEFAULT_FOLDER_NAME_MSG = "Missing default %1$s folder";

    public static final String UPDATE_FAILED_MSG = "Update of folder failed";

    public static final String NO_FOLDER_DELETE_MSG = "Folder could not be deleted";

    public static final String NO_DEFAULT_FOLDER_DELETE_MSG = "Default folder %1$s cannot be deleted";

    public static final String FLAG_FAILED_MSG = "Flag %1$s could not be changed due to following reason \"%2$s\"";

    public static final String INVALID_SEARCH_PARAMS_MSG = "Number of search fields (%d) do not match number of search patterns (%d)";

    public static final String IMAP_SEARCH_FAILED_MSG = "IMAP search failed due to reason \"%1$s\". Switching to application-based search";

    public static final String IMAP_SORT_FAILED_MSG = "IMAP sort failed due to reason \"%1$s\". Switching to application-based sorting.";

    public static final String UNKNOWN_SEARCH_FIELD_MSG = "Unknown search field: %1$s";

    public static final String NO_MOVE_TO_SUBFLD_MSG = "Mail folder must not be moved to subsequent folder";

    public static final String MOVE_ON_DELETE_FAILED_MSG = "This message could not be moved to trash folder, possibly because your mailbox is nearly full.\nIn that case, please try to empty your deleted items first, or delete smaller messages first.";

    public static final String MISSING_SOURCE_TARGET_FOLDER_ON_MOVE_MSG = "Missing folder in mail move operation";

    public static final String NO_EQUAL_MOVE_MSG = "Message move aborted for user %1$s. Source and destination folder are equal to \"%2$s\"";

    public static final String FAILED_READ_ONLY_CHECK_MSG = "IMAP folder read-only check failed";

    public static final String UNKNOWN_FOLDER_MODE_MSG = "Unknown folder open mode %1$s";

    public static final String UID_EXPUNGE_FAILED_MSG = "Message(s) %1$s in folder %2$s could not be deleted due to error \"%3$s\"";

    public static final String NO_FOLDER_OPEN_MSG = "Not allowed to open folder %1$s due to missing read access";

    public static final String MESSAGE_CONTENT_ERROR_MSG = "The raw content's input stream cannot be read";

    public static final String NO_ATTACHMENT_FOUND_MSG = "No attachment was found with id %1$s in message";

    public static final String FAILED_VERSIT_SAVE_MSG = "Versit object could not be saved";

    public static final String THREAD_SORT_NOT_SUPPORTED_MSG = "No support of capability \"THREAD=REFERENCES\"";

    public static final String PROTOCOL_ERROR_MSG = "A protocol exception occurred during execution of IMAP request \"%1$s\".\nError message: %2$s";

    public static final String NO_ROOT_MOVE_MSG = "Mailbox' root folder must not be source or the destination full name of a move operation.";

    public static final String UNSUPPORTED_SORT_FIELD_MSG = "Sort field %1$s is not supported via IMAP SORT command";

    public static final String MISSING_PERSONAL_NAMESPACE_MSG = "Missing personal namespace";

    public static final String THREAD_SORT_PARSING_ERROR_MSG = "Parsing thread-sort string failed: %1$s.";

    public static final String SQL_ERROR_MSG = "A SQL error occurred: %1$s";

    public static final String RENAME_FAILED_MSG = "Rename of folder \"%1$s\" to \"%2$s\" failed with \"%3$s\".";

    public static final String NO_RENAME_ACCESS_MSG = "No rename access to mail folder %1$s";

    public static final String URI_PARSE_FAILED_MSG = "Unable to parse IMAP server URI \"%1$s\".";

    public static final String NO_DEFAULT_FOLDER_UNSUBSCRIBE_MSG = "Default folder %1$s must not be unsubscribed.";

    public static final String INVALID_MESSAGE_MSG = "IMAP server refuses to import one or more E-Mails.";

    public static final String CONNECTION_UNAVAILABLE_MSG = "Currently not possible to establish a new connection to server %1$s with login %2$s. Please try again.";

}
