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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.pop3;

import static com.openexchange.i18n.TextPattern.LINE_SEPARATOR;
import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link POP3ExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class POP3ExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link POP3ExceptionMessage}.
     */
    private POP3ExceptionMessage() {
        super();
    }

    /**
     * No connection available to access mailbox
     */
    public final static String NOT_CONNECTED_MSG = "No connection available to access mailbox";
    /**
     * User %1$s has no mail module access due to user configuration
     */
    public final static String NO_MAIL_MODULE_ACCESS_MSG = "User %1$s has no mail module access due to user configuration";
    /**
     * No access to mail folder %1$s
     */
    public final static String NO_ACCESS_MSG = "No access to mail folder %1$s";
    /**
     * No lookup access to mail folder %1$s
     */
    public final static String NO_LOOKUP_ACCESS_MSG = "No lookup access to mail folder %1$s";
    /**
     * No read access on mail folder %1$s
     */
    public final static String NO_READ_ACCESS_MSG = "No read access to mail folder %1$s";
    /**
     * No delete access on mail folder %1$s
     */
    public final static String NO_DELETE_ACCESS_MSG = "No delete access to mail folder %1$s";
    /**
     * No insert access on mail folder %1$s
     */
    public final static String NO_INSERT_ACCESS_MSG = "No insert access to mail folder %1$s";
    /**
     * No administer access on mail folder %1$s
     */
    public final static String NO_ADMINISTER_ACCESS_MSG = "No administer access to mail folder %1$s";
    /**
     * No write access to POP3 folder %1$s
     */
    public final static String NO_WRITE_ACCESS_MSG = "No write access to POP3 folder %1$s";
    /**
     * No keep-seen access on mail folder %1$s
     */
    public final static String NO_KEEP_SEEN_ACCESS_MSG = "No keep-seen access to mail folder %1$s";
    /**
     * Folder %1$s does not allow subfolders.
     */
    public final static String FOLDER_DOES_NOT_HOLD_FOLDERS_MSG = "Folder %1$s does not allow subfolders.";
    /**
     * POP3 does not support mail folder creation
     */
    public final static String FOLDER_CREATION_FAILED_MSG = "POP3 does not support mail folder creation";
    /**
     * The permissions set could not be applied to the new folder %1$s.
     * Its initial permissions specified by the POP3 server do not include administer permissions. The folder has been created, though.
     */
    public final static String NO_ADMINISTER_ACCESS_ON_INITIAL_MSG = "The permissions set could not be applied to the new folder %1$s. Its initial permissions specified by the POP3 server do not include administer permissions. The folder has been created, though.";
    /**
     * No admin permission specified for folder %1$s
     */
    public final static String NO_ADMIN_ACL_MSG = "No administer permission specified for folder %1$s";
    /**
     * Default folder %1$s must not be updated
     */
    public final static String NO_DEFAULT_FOLDER_UPDATE_MSG = "Default folder %1$s cannot be updated";
    /**
     * Deletion of folder %1$s failed
     */
    public final static String DELETE_FAILED_MSG = "Deletion of folder %1$s failed";
    /**
     * POP3 default folder %1$s could not be created
     */
    public final static String NO_DEFAULT_FOLDER_CREATION_MSG = "POP3 default folder %1$s could not be created";
    /**
     * Missing default %1$s folder in user mail settings
     */
    public final static String MISSING_DEFAULT_FOLDER_NAME_MSG = "Missing default %1$s folder in user mail settings";
    /**
     * Update of folder %1$s failed
     */
    public final static String UPDATE_FAILED_MSG = "Update of folder %1$s failed";
    /**
     * Folder %1$s must not be deleted
     */
    public final static String NO_FOLDER_DELETE_MSG = "Folder %1$s cannot be deleted";
    /**
     * Default folder %1$s must not be deleted
     */
    public final static String NO_DEFAULT_FOLDER_DELETE_MSG = "Default folder %1$s cannot be deleted";
    /**
     * Flag %1$s could not be changed due to following reason: %2$s
     */
    public final static String FLAG_FAILED_MSG = "Flag %1$s could not be changed due to the following reason: %2$s";
    /**
     * Number of search fields (%d) do not match number of search patterns (%d)
     */
    public final static String INVALID_SEARCH_PARAMS_MSG = "Number of search fields (%d) do not match number of search patterns (%d)";
    /**
     * POP3 search failed due to following reason: %1$s. Switching to application-based search
     */
    public final static String POP3_SEARCH_FAILED_MSG = "POP3 search failed due to the following reason: %1$s. Switching to application-based search";
    /**
     * POP3 sort failed due to following reason: %1$s Switching to application-based sorting
     */
    public final static String POP3_SORT_FAILED_MSG = "POP3 sort failed due to the following reason: %1$s Switching to application-based sorting";
    /**
     * Unknown search field: %1$s
     */
    public final static String UNKNOWN_SEARCH_FIELD_MSG = "Unknown search field: %1$s";
    /**
     * Mail folder %1$s must not be moved to subsequent folder %2$s
     */
    public final static String NO_MOVE_TO_SUBFLD_MSG = "Mail folder %1$s must not be moved to subsequent folder %2$s";
    /**
     * This message could not be moved to trash folder, possibly because your mailbox is nearly full.<br>
     * In that case, please try to empty your deleted items first, or delete smaller messages first.
     */
    public final static String MOVE_ON_DELETE_FAILED_MSG = "This message could not be moved to trash folder, possibly because your mailbox is nearly full. In that case, please try to empty your deleted items first, or delete smaller messages first.";
    /**
     * Missing %1$s folder in mail move operation
     */
    public final static String MISSING_SOURCE_TARGET_FOLDER_ON_MOVE_MSG = "Missing %1$s folder in mail move operation";
    /**
     * Message move aborted for user %1$s. Source and destination folder are equal: %2$s
     */
    public final static String NO_EQUAL_MOVE_MSG = "Message move aborted for user %1$s. Source and destination folder are equal: %2$s";
    /**
     * Folder read-only check failed
     */
    public final static String FAILED_READ_ONLY_CHECK_MSG = "POP3 folder read-only check failed";
    /**
     * Unknown folder open mode %d
     */
    public final static String UNKNOWN_FOLDER_MODE_MSG = "Unknown folder open mode %d";
    /**
     * Message(s) %1$s in folder %2$s could not be deleted due to following error: %3$s
     */
    public final static String UID_EXPUNGE_FAILED_MSG = "Message(s) %1$s in folder %2$s could not be deleted due to the following error: %3$s";
    /**
     * Not allowed to open folder %1$s due to missing read access
     */
    public final static String NO_FOLDER_OPEN_MSG = "Not allowed to open folder %1$s due to missing read access";
    /**
     * The raw content's input stream of message %1$s in folder %2$s cannot be read
     */
    public final static String MESSAGE_CONTENT_ERROR_MSG = "The raw content's input stream of message %1$s in folder %2$s cannot be read";
    /**
     * No attachment was found with id %1$s in message
     */
    public final static String NO_ATTACHMENT_FOUND_MSG = "No attachment was found with id %1$s in message";
    /**
     * Versit object %1$s could not be saved
     */
    public final static String FAILED_VERSIT_SAVE_MSG = "Versit object could not be saved";
    /**
     * POP3 server does not support capability "THREAD=REFERENCES"
     */
    public final static String THREAD_SORT_NOT_SUPPORTED_MSG = "POP3 server does not support capability \"THREAD=REFERENCES\"";
    /**
     * A protocol exception occurred during execution of an POP3 request: %1$s
     */
    public final static String PROTOCOL_ERROR_MSG = "A protocol exception occurred during execution of a POP3 request: %1$s";
    /**
     * POP3 does not support to move folders.
     */
    public final static String MOVE_DENIED_MSG = "POP3 does not support to move folders.";
    /**
     * Sort field %1$s is not supported via POP3 SORT command
     */
    public final static String UNSUPPORTED_SORT_FIELD_MSG = "Sort field %1$s is not supported via POP3 SORT command";
    /**
     * Missing personal namespace
     */
    public final static String MISSING_PERSONAL_NAMESPACE_MSG = "Missing personal namespace";
    /**
     * Parsing thread-sort string failed: %1$s.
     */
    public final static String THREAD_SORT_PARSING_ERROR_MSG = "Parsing thread-sort string failed: %1$s.";
    /**
     * POP3 does not support to create folders.
     */
    public final static String CREATE_DENIED_MSG = "POP3 does not support to create folders.";
    /**
     * POP3 does not support to delete folders.
     */
    public final static String DELETE_DENIED_MSG = "POP3 does not support to delete folders.";
    /**
     * POP3 does not support to update folders.
     */
    public final static String UPDATE_DENIED_MSG = "POP3 does not support to update folders.";
    /**
     * A SQL error occurred: %1$s.
     */
    public final static String SQL_ERROR_MSG = "A SQL error occurred: %1$s.";
    /**
     * POP3 does not support to move messages.
     */
    public final static String MOVE_MSGS_DENIED_MSG = "POP3 does not support to move messages.";
    /**
     * POP3 does not support to copy messages.
     */
    public final static String COPY_MSGS_DENIED_MSG = "POP3 does not support to copy messages.";
    /**
     * POP3 does not support to append messages.
     */
    public final static String APPEND_MSGS_DENIED_MSG = "POP3 does not support to append messages.";
    /**
     * POP3 does not support draft messages.
     */
    public final static String DRAFTS_NOT_SUPPORTED_MSG = "POP3 does not support draft messages.";
    /**
     * Missing POP3 storage name for user %1$s in context %2$s.
     */
    public final static String MISSING_POP3_STORAGE_NAME_MSG = "Missing POP3 storage name for user %1$s in context %2$s.";
    /**
     * Missing POP3 storage for user %1$s in context %2$s.
     */
    public final static String MISSING_POP3_STORAGE_MSG = "Missing POP3 storage for user %1$s in context %2$s.";
    /**
     * POP3 default folder %1$s must not be moved.
     */
    public final static String NO_DEFAULT_FOLDER_MOVE_MSG = "POP3 default folder %1$s must not be moved.";
    /**
     * POP3 default folder %1$s must not be renamed.
     */
    public final static String NO_DEFAULT_FOLDER_RENAME_MSG = "POP3 default folder %1$s must not be renamed.";
    /**
     * Inconsistency detected in UIDL map.
     */
    public final static String UIDL_INCONSISTENCY_MSG = "Inconsistency detected in UIDL map.";
    /**
     * Missing POP3 storage path for user %1$s in context %2$s.
     */
    public final static String MISSING_PATH_MSG = "Missing POP3 storage path for user %1$s in context %2$s.";
    /**
     * Illegal move operation.
     */
    public final static String MOVE_ILLEGAL_MSG = "Illegal move operation.";
    /**
     * Login delay denies connecting to server %1$s with login %2$s (user=%3$s, context=%4$s).<br>
     * Error message from server: %5$s
     */
    public final static String LOGIN_DELAY_MSG = "Login delay denies connecting to server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from server: %5$s";
    /**
     * Login delay denies connecting to server %1$s with login %2$s (user=%3$s, context=%4$s). Try again in %5$s seconds.<br>
     * Error message from server: %6$s
     */
    public final static String LOGIN_DELAY2_MSG = "Login delay denies connecting to server %1$s with login %2$s (user=%3$s, context=%4$s). Try again in %5$s seconds. Error message from server: %6$s";
    /**
     * Missing required capability %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s).
     */
    public final static String MISSING_REQUIRED_CAPABILITY_MSG = "Missing required capability %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s).";
    /**
     * POP3 storage path "%1$s" cannot be created for user %2$s in context %3$s.
     */
    public final static String ILLEGAL_PATH_MSG = "POP3 storage path \"%1$s\" cannot be created for user %2$s in context %3$s.";
    /**
     * Due to missing required capability %1$s POP3 messages are fetched and removed (expunge-on-quit) from server %2$s with login %3$s
     * (user=%4$s, context=%5$s).
     */
    public final static String EXPUNGE_MODE_ONLY_MSG = "Due to missing required capability %1$s POP3 messages are fetched and removed (expunge-on-quit) from server %2$s with login %3$s (user=%4$s, context=%5$s).";
    /**
     * Validation of POP3 credentials is disabled due to possible login restrictions by provider. Otherwise subsequent login attempt might not work.
     */
    public final static String VALIDATE_DENIED_MSG = "Validation of POP3 credentials is disabled due to possible login restrictions by provider. Otherwise subsequent login attempt might not work.";

    // POP3 messages cannot be imported because of existing quota constraints on primary mail account. Please free some space.
    public static final String QUOTA_CONSTRAINT_MSG = "POP3 messages cannot be imported because of existing quota constraints on primary mail account. Please free some space.";

}
