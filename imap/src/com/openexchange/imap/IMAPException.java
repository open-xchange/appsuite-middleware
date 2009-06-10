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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.EnumMap;
import java.util.Map;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.mail.MailException;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.session.Session;

/**
 * {@link IMAPException}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPException extends MIMEMailException {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -8226676160145457046L;

    public static enum Code {

        /**
         * Missing parameter in mail connection: %1$s
         */
        MISSING_CONNECT_PARAM(IMAPCode.MISSING_CONNECT_PARAM),
        /**
         * No connection available to access mailbox
         */
        NOT_CONNECTED(IMAPCode.NOT_CONNECTED),
        /**
         * Missing parameter %1$s
         */
        MISSING_PARAMETER(IMAPCode.MISSING_PARAMETER),
        /**
         * A JSON error occurred: %1$s
         */
        JSON_ERROR(IMAPCode.JSON_ERROR),
        /**
         * Invalid permission values: fp=%d orp=%d owp=%d odp=%d
         */
        INVALID_PERMISSION(IMAPCode.INVALID_PERMISSION),
        /**
         * User %1$s has no mail module access due to user configuration
         */
        NO_MAIL_MODULE_ACCESS(IMAPCode.NO_MAIL_MODULE_ACCESS),
        /**
         * No access to mail folder %1$s
         */
        NO_ACCESS(IMAPCode.NO_ACCESS),
        /**
         * No lookup access to mail folder %1$s
         */
        NO_LOOKUP_ACCESS(IMAPCode.NO_LOOKUP_ACCESS),
        /**
         * No read access on IMAP-Folder %1$s
         */
        NO_READ_ACCESS(IMAPCode.NO_READ_ACCESS),
        /**
         * No delete access on IMAP-Folder %1$s
         */
        NO_DELETE_ACCESS(IMAPCode.NO_DELETE_ACCESS),
        /**
         * No insert access on IMAP-Folder %1$s
         */
        NO_INSERT_ACCESS(IMAPCode.NO_INSERT_ACCESS),
        /**
         * No create access on IMAP-Folder %1$s
         */
        NO_CREATE_ACCESS(IMAPCode.NO_CREATE_ACCESS),
        /**
         * No administer access on IMAP-Folder %1$s
         */
        NO_ADMINISTER_ACCESS(IMAPCode.NO_ADMINISTER_ACCESS),
        /**
         * No write access to IMAP folder %1$s
         */
        NO_WRITE_ACCESS(IMAPCode.NO_WRITE_ACCESS),
        /**
         * No keep-seen access on IMAP-Folder %1$s
         */
        NO_KEEP_SEEN_ACCESS(IMAPCode.NO_KEEP_SEEN_ACCESS),
        /**
         * Folder %1$s does not allow subfolders.
         */
        FOLDER_DOES_NOT_HOLD_FOLDERS(IMAPCode.FOLDER_DOES_NOT_HOLD_FOLDERS),
        /**
         * Mail folder cannot be created/rename. Name must not contain character '%1$s'
         */
        INVALID_FOLDER_NAME(IMAPCode.INVALID_FOLDER_NAME),
        /**
         * A folder named %1$s already exists
         */
        DUPLICATE_FOLDER(IMAPCode.DUPLICATE_FOLDER),
        /**
         * Mail folder "%1$s" could not be created (maybe due to insufficient permission on parent folder %2$s or due to an invalid folder
         * name)
         */
        FOLDER_CREATION_FAILED(IMAPCode.FOLDER_CREATION_FAILED),
        /**
         * The composed rights could not be applied to new folder %1$s due to missing administer right in its initial rights specified by
         * IMAP server. However, the folder has been created.
         */
        NO_ADMINISTER_ACCESS_ON_INITIAL(IMAPCode.NO_ADMINISTER_ACCESS_ON_INITIAL),
        /**
         * No admin permission specified for folder %1$s
         */
        NO_ADMIN_ACL(IMAPCode.NO_ADMIN_ACL),
        /**
         * Default folder %1$s must not be updated
         */
        NO_DEFAULT_FOLDER_UPDATE(IMAPCode.NO_DEFAULT_FOLDER_UPDATE),
        /**
         * Deletion of folder %1$s failed
         */
        DELETE_FAILED(IMAPCode.DELETE_FAILED),
        /**
         * IMAP default folder %1$s could not be created
         */
        NO_DEFAULT_FOLDER_CREATION(IMAPCode.NO_DEFAULT_FOLDER_CREATION),
        /**
         * Missing default %1$s folder
         */
        MISSING_DEFAULT_FOLDER_NAME(IMAPCode.MISSING_DEFAULT_FOLDER_NAME),
        /**
         * Update of folder %1$s failed
         */
        UPDATE_FAILED(IMAPCode.UPDATE_FAILED),
        /**
         * Folder %1$s must not be deleted
         */
        NO_FOLDER_DELETE(IMAPCode.NO_FOLDER_DELETE),
        /**
         * Default folder %1$s must not be deleted
         */
        NO_DEFAULT_FOLDER_DELETE(IMAPCode.NO_DEFAULT_FOLDER_DELETE),
        /**
         * An I/O error occurred: %1$s
         */
        IO_ERROR(IMAPCode.IO_ERROR),
        /**
         * Flag %1$s could not be changed due to reason "%2$s"
         */
        FLAG_FAILED(IMAPCode.FLAG_FAILED),
        /**
         * Folder %1$s does not hold messages and is therefore not selectable
         */
        FOLDER_DOES_NOT_HOLD_MESSAGES(IMAPCode.FOLDER_DOES_NOT_HOLD_MESSAGES),
        /**
         * Number of search fields (IMAPCode.) do not match number of search patterns (IMAPCode.)
         */
        INVALID_SEARCH_PARAMS(IMAPCode.INVALID_SEARCH_PARAMS),
        /**
         * IMAP search failed due to reason "%1$s". Switching to application-based search
         */
        IMAP_SEARCH_FAILED(IMAPCode.IMAP_SEARCH_FAILED),
        /**
         * IMAP sort failed due to reason "%1$s". Switching to application-based sorting.
         */
        IMAP_SORT_FAILED(IMAPCode.IMAP_SORT_FAILED),
        /**
         * Unknown search field: %1$s
         */
        UNKNOWN_SEARCH_FIELD(IMAPCode.UNKNOWN_SEARCH_FIELD),
        /**
         * Message field %1$s cannot be handled
         */
        INVALID_FIELD(IMAPCode.INVALID_FIELD),
        /**
         * Mail folder %1$s must not be moved to subsequent folder %2$s
         */
        NO_MOVE_TO_SUBFLD(IMAPCode.NO_MOVE_TO_SUBFLD),
        /**
         * Message could not be moved to trash folder
         */
        MOVE_ON_DELETE_FAILED(IMAPCode.MOVE_ON_DELETE_FAILED),
        /**
         * Missing %1$s folder in mail move operation
         */
        MISSING_SOURCE_TARGET_FOLDER_ON_MOVE(IMAPCode.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE),
        /**
         * Message move aborted for user %1$s. Source and destination folder are equal to "%2$s"
         */
        NO_EQUAL_MOVE(IMAPCode.NO_EQUAL_MOVE),
        /**
         * Folder read-only check failed
         */
        FAILED_READ_ONLY_CHECK(IMAPCode.FAILED_READ_ONLY_CHECK),
        /**
         * Unknown folder open mode %1$s
         */
        UNKNOWN_FOLDER_MODE(IMAPCode.UNKNOWN_FOLDER_MODE),
        /**
         * Message(IMAPCode.Message) %1$s in folder %2$s could not be deleted due to error "%3$s"
         */
        UID_EXPUNGE_FAILED(IMAPCode.UID_EXPUNGE_FAILED),
        /**
         * Not allowed to open folder %1$s due to missing read access
         */
        NO_FOLDER_OPEN(IMAPCode.NO_FOLDER_OPEN),
        /**
         * The raw content's input stream of message %1$s in folder %2$s cannot be read
         */
        MESSAGE_CONTENT_ERROR(IMAPCode.MESSAGE_CONTENT_ERROR),
        /**
         * No attachment was found with id %1$s in message
         */
        NO_ATTACHMENT_FOUND(IMAPCode.NO_ATTACHMENT_FOUND),
        /**
         * Versit attachment could not be saved due to an unsupported MIME type: %1$s
         */
        UNSUPPORTED_VERSIT_ATTACHMENT(IMAPCode.UNSUPPORTED_VERSIT_ATTACHMENT),
        /**
         * Versit object %1$s could not be saved
         */
        FAILED_VERSIT_SAVE(IMAPCode.FAILED_VERSIT_SAVE),
        /**
         * No support of capability "THREAD=REFERENCES"
         */
        THREAD_SORT_NOT_SUPPORTED(IMAPCode.THREAD_SORT_NOT_SUPPORTED),
        /**
         * Unsupported charset-encoding: %1$s
         */
        ENCODING_ERROR(IMAPCode.ENCODING_ERROR),
        /**
         * A protocol exception occurred during execution of IMAP request "%1$s".<br>
         * Error message: %2$s
         */
        PROTOCOL_ERROR(IMAPCode.PROTOCOL_ERROR),
        /**
         * Mail folder could not be found: %1$s.
         */
        FOLDER_NOT_FOUND(IMAPCode.FOLDER_NOT_FOUND),
        /**
         * An attempt was made to open a read-only folder with read-write "%1$s"
         */
        READ_ONLY_FOLDER(IMAPCode.READ_ONLY_FOLDER),
        /**
         * Connect error: Connection was refused or timed out while attempting to connect to remote mail server %1$s for user %2$s.
         */
        CONNECT_ERROR(IMAPCode.CONNECT_ERROR),
        /**
         * Mailbox' root folder must not be source or the destination fullname of a move operation.
         */
        NO_ROOT_MOVE(IMAPCode.NO_ROOT_MOVE),
        /**
         * Sort field %1$s is not supported via IMAP SORT command
         */
        UNSUPPORTED_SORT_FIELD(IMAPCode.UNSUPPORTED_SORT_FIELD),
        /**
         * Missing personal namespace
         */
        MISSING_PERSONAL_NAMESPACE(IMAPCode.MISSING_PERSONAL_NAMESPACE),
        /**
         * Parsing thread-sort string failed: %1$s.
         */
        THREAD_SORT_PARSING_ERROR(IMAPCode.THREAD_SORT_PARSING_ERROR);

        private final IMAPCode imapCode;

        private Code(final IMAPCode imapCode) {
            this.imapCode = imapCode;
        }

        IMAPCode getImapCode() {
            return imapCode;
        }

        public int getNumber() {
            return imapCode.getNumber();
        }

    }

    private static enum IMAPCode {

        /**
         * Missing parameter in mail connection: %1$s
         */
        MISSING_CONNECT_PARAM(MailException.Code.MISSING_CONNECT_PARAM, null),
        /**
         * No connection available to access mailbox
         */
        NOT_CONNECTED("No connection available to access mailbox", Category.CODE_ERROR, 2001),
        /**
         * No connection available to access mailbox on server %1$s with login %2$s (user=%3$s, context=%4$s)
         */
        NOT_CONNECTED_EXT("No connection available to access mailbox on server %1$s with login %2$s (user=%3$s, context=%4$s)", NOT_CONNECTED),
        /**
         * Missing parameter %1$s
         */
        MISSING_PARAMETER(MailException.Code.MISSING_PARAMETER, null),
        /**
         * A JSON error occurred: %1$s
         */
        JSON_ERROR(MailException.Code.JSON_ERROR, null),
        /**
         * Invalid permission values: fp=%d orp=%d owp=%d odp=%d
         */
        INVALID_PERMISSION(MailException.Code.INVALID_PERMISSION, null),
        /**
         * User %1$s has no mail module access due to user configuration
         */
        NO_MAIL_MODULE_ACCESS("User %1$s has no mail module access due to user configuration", Category.USER_CONFIGURATION, 2003),
        /**
         * No access to mail folder %1$s
         */
        NO_ACCESS("No access to mail folder %1$s", Category.PERMISSION, 2003),
        /**
         * No access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_ACCESS_EXT("No access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_ACCESS),
        /**
         * No lookup access to mail folder %1$s
         */
        NO_LOOKUP_ACCESS("No lookup access to mail folder %1$s", Category.PERMISSION, 2004),
        /**
         * No lookup access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_LOOKUP_ACCESS_EXT("No lookup access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_LOOKUP_ACCESS),
        /**
         * No read access on IMAP-Folder %1$s
         */
        NO_READ_ACCESS("No read access to mail folder %1$s", Category.PERMISSION, 2005),
        /**
         * No read access on IMAP-Folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_READ_ACCESS_EXT("No read access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_READ_ACCESS),
        /**
         * No delete access on IMAP-Folder %1$s
         */
        NO_DELETE_ACCESS("No delete access to mail folder %1$s", Category.PERMISSION, 2006),
        /**
         * No delete access on IMAP-Folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_DELETE_ACCESS_EXT("No delete access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_DELETE_ACCESS),
        /**
         * No insert access on IMAP-Folder %1$s
         */
        NO_INSERT_ACCESS("No insert access to mail folder %1$s", Category.PERMISSION, 2007),
        /**
         * No insert access on IMAP-Folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_INSERT_ACCESS_EXT("No insert access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_INSERT_ACCESS),
        /**
         * No create access on IMAP-Folder %1$s
         */
        NO_CREATE_ACCESS(MailException.Code.NO_CREATE_ACCESS, null),
        /**
         * No create access on IMAP-Folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_CREATE_ACCESS_EXT(MailException.Code.NO_CREATE_ACCESS_EXT, NO_CREATE_ACCESS),
        /**
         * No administer access on IMAP-Folder %1$s
         */
        NO_ADMINISTER_ACCESS("No administer access to mail folder %1$s", Category.PERMISSION, 2009),
        /**
         * No administer access on IMAP-Folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_ADMINISTER_ACCESS_EXT("No administer access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_ADMINISTER_ACCESS),
        /**
         * No write access to IMAP folder %1$s
         */
        NO_WRITE_ACCESS("No write access to IMAP folder %1$s", Category.PERMISSION, 2010),
        /**
         * No write access to IMAP folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_WRITE_ACCESS_EXT("No write access to IMAP folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_WRITE_ACCESS),
        /**
         * No keep-seen access on IMAP-Folder %1$s
         */
        NO_KEEP_SEEN_ACCESS("No keep-seen access to mail folder %1$s", Category.PERMISSION, 2011),
        /**
         * No keep-seen access on IMAP-Folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_KEEP_SEEN_ACCESS_EXT("No keep-seen access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_KEEP_SEEN_ACCESS),
        /**
         * Folder %1$s does not allow subfolders.
         */
        FOLDER_DOES_NOT_HOLD_FOLDERS("Folder %1$s does not allow subfolders.", Category.PERMISSION, 2012),
        /**
         * Folder %1$s does not allow subfolders on server %2$s with login %3$s (user=%4$s, context=%5$s).
         */
        FOLDER_DOES_NOT_HOLD_FOLDERS_EXT("Folder %1$s does not allow subfolders on server %2$s with login %3$s (user=%4$s, context=%5$s).", FOLDER_DOES_NOT_HOLD_FOLDERS),
        /**
         * Mail folder cannot be created/rename. Name must not contain character '%1$s'
         */
        INVALID_FOLDER_NAME(MailException.Code.INVALID_FOLDER_NAME, null),
        /**
         * A folder named %1$s already exists
         */
        DUPLICATE_FOLDER(MailException.Code.DUPLICATE_FOLDER, null),
        /**
         * A folder named %1$s already exists on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        DUPLICATE_FOLDER_EXT(MailException.Code.DUPLICATE_FOLDER_EXT, DUPLICATE_FOLDER),
        /**
         * Mail folder "%1$s" could not be created (maybe due to insufficient permission on parent folder %2$s or due to an invalid folder
         * name)
         */
        FOLDER_CREATION_FAILED("Mail folder \"%1$s\" could not be created (maybe due to insufficient permission on parent folder %2$s or due to an invalid folder name)", Category.CODE_ERROR, 2015),
        /**
         * Mail folder "%1$s" could not be created (maybe due to insufficient permission on parent folder %2$s or due to an invalid folder
         * name) on server %3$s with login %4$s (user=%5$s, context=%6$s)
         */
        FOLDER_CREATION_FAILED_EXT("Mail folder \"%1$s\" could not be created (maybe due to insufficient permission on parent folder %2$s or due to an invalid folder name) on server %3$s with login %4$s (user=%5$s, context=%6$s)", FOLDER_CREATION_FAILED),
        /**
         * The composed rights could not be applied to new folder %1$s due to missing administer right in its initial rights specified by
         * IMAP server. However, the folder has been created.
         */
        NO_ADMINISTER_ACCESS_ON_INITIAL("The composed rights could not be applied to new folder %1$s due to missing administer right in its initial rights specified by IMAP server. However, the folder has been created.", Category.PERMISSION, 2016),
        /**
         * The composed rights could not be applied to new folder %1$s due to missing administer right in its initial rights specified by
         * IMAP server. However, the folder has been created on server %2$s with login %3$s (user=%4$s, context=%5$s).
         */
        NO_ADMINISTER_ACCESS_ON_INITIAL_EXT("The composed rights could not be applied to new folder %1$s due to missing administer right in its initial rights specified by IMAP server. However, the folder has been created on server %2$s with login %3$s (user=%4$s, context=%5$s).", NO_ADMINISTER_ACCESS_ON_INITIAL),
        /**
         * No admin permission specified for folder %1$s
         */
        NO_ADMIN_ACL("No administer permission specified for folder %1$s", Category.USER_INPUT, 2017),
        /**
         * No admin permission specified for folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_ADMIN_ACL_EXT("No administer permission specified for folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_ADMIN_ACL),
        /**
         * Default folder %1$s must not be updated
         */
        NO_DEFAULT_FOLDER_UPDATE("Default folder %1$s cannot be updated", Category.PERMISSION, 2018),
        /**
         * Default folder %1$s must not be updated on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_DEFAULT_FOLDER_UPDATE_EXT("Default folder %1$s cannot be updated on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_DEFAULT_FOLDER_UPDATE),
        /**
         * Deletion of folder %1$s failed
         */
        DELETE_FAILED("Deletion of folder %1$s failed", Category.CODE_ERROR, 2019),
        /**
         * Deletion of folder %1$s failed on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        DELETE_FAILED_EXT("Deletion of folder %1$s failed on server %2$s with login %3$s (user=%4$s, context=%5$s)", DELETE_FAILED),
        /**
         * IMAP default folder %1$s could not be created
         */
        NO_DEFAULT_FOLDER_CREATION("IMAP default folder %1$s could not be created", Category.CODE_ERROR, 2020),
        /**
         * IMAP default folder %1$s could not be created on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_DEFAULT_FOLDER_CREATION_EXT("IMAP default folder %1$s could not be created on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_DEFAULT_FOLDER_CREATION),
        /**
         * Missing default %1$s folder
         */
        MISSING_DEFAULT_FOLDER_NAME("Missing default %1$s folder", Category.CODE_ERROR, 2021),
        /**
         * Missing default %1$s folder on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        MISSING_DEFAULT_FOLDER_NAME_EXT("Missing default %1$s folder on server %2$s with login %3$s (user=%4$s, context=%5$s)", MISSING_DEFAULT_FOLDER_NAME),
        /**
         * Update of folder %1$s failed
         */
        UPDATE_FAILED("Update of folder %1$s failed", Category.CODE_ERROR, 2022),
        /**
         * Update of folder %1$s failed on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        UPDATE_FAILED_EXT("Update of folder %1$s failed on server %2$s with login %3$s (user=%4$s, context=%5$s)", UPDATE_FAILED),
        /**
         * Folder %1$s must not be deleted
         */
        NO_FOLDER_DELETE("Folder %1$s cannot be deleted", Category.PERMISSION, 2023),
        /**
         * Folder %1$s must not be deleted on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_FOLDER_DELETE_EXT("Folder %1$s cannot be deleted on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_FOLDER_DELETE),
        /**
         * Default folder %1$s must not be deleted
         */
        NO_DEFAULT_FOLDER_DELETE("Default folder %1$s cannot be deleted", Category.PERMISSION, 2024),
        /**
         * Default folder %1$s must not be deleted on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_DEFAULT_FOLDER_DELETE_EXT("Default folder %1$s cannot be deleted on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_DEFAULT_FOLDER_DELETE),
        /**
         * An I/O error occurred: %1$s
         */
        IO_ERROR(MailException.Code.IO_ERROR, null),
        /**
         * Flag %1$s could not be changed due to reason "%2$s"
         */
        FLAG_FAILED("Flag %1$s could not be changed due to following reason \"%2$s\"", Category.INTERNAL_ERROR, 2025),
        /**
         * Flag %1$s could not be changed due to reason "%2$s" on server %3$s with login %4$s (user=%5$s, context=%6$s)
         */
        FLAG_FAILED_EXT("Flag %1$s could not be changed due to following reason \"%2$s\" on server %3$s with login %4$s (user=%5$s, context=%6$s)", FLAG_FAILED),
        /**
         * Folder %1$s does not hold messages and is therefore not selectable
         */
        FOLDER_DOES_NOT_HOLD_MESSAGES(MailException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, null),
        /**
         * Folder %1$s does not hold messages and is therefore not selectable
         */
        FOLDER_DOES_NOT_HOLD_MESSAGES_EXT(MailException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES_EXT, FOLDER_DOES_NOT_HOLD_MESSAGES),
        /**
         * Number of search fields (%d) do not match number of search patterns (%d)
         */
        INVALID_SEARCH_PARAMS("Number of search fields (%d) do not match number of search patterns (%d)", Category.CODE_ERROR, 2028),
        /**
         * IMAP search failed due to reason "%1$s". Switching to application-based search
         */
        IMAP_SEARCH_FAILED("IMAP search failed due to reason \"%1$s\". Switching to application-based search", Category.SUBSYSTEM_OR_SERVICE_DOWN, 2029),
        /**
         * IMAP search failed due to reason "%1$s" on server %2$s with login %3$s (user=%4$s, context=%5$s). Switching to application-based
         * search.
         */
        IMAP_SEARCH_FAILED_EXT("IMAP search failed due to reason \"%1$s\" on server %2$s with login %3$s (user=%4$s, context=%5$s). Switching to application-based search.", IMAP_SEARCH_FAILED),
        /**
         * IMAP sort failed due to reason "%1$s". Switching to application-based sorting.
         */
        IMAP_SORT_FAILED("IMAP sort failed due to reason \"%1$s\". Switching to application-based sorting.", Category.SUBSYSTEM_OR_SERVICE_DOWN, 2030),
        /**
         * IMAP sort failed due to reason "%1$s" on server %2$s with login %3$s (user=%4$s, context=%5$s). Switching to application-based
         * sorting.
         */
        IMAP_SORT_FAILED_EXT("IMAP sort failed due to reason \"%1$s\" on server %2$s with login %3$s (user=%4$s, context=%5$s). Switching to application-based sorting.", IMAP_SORT_FAILED),
        /**
         * Unknown search field: %1$s
         */
        UNKNOWN_SEARCH_FIELD("Unknown search field: %1$s", Category.CODE_ERROR, 2031),
        /**
         * Unknown search field: %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        UNKNOWN_SEARCH_FIELD_EXT("Unknown search field: %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", UNKNOWN_SEARCH_FIELD),
        /**
         * Message field %1$s cannot be handled
         */
        INVALID_FIELD(MailException.Code.INVALID_FIELD, null),
        /**
         * Message field %1$s cannot be handled on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        INVALID_FIELD_EXT(MailException.Code.INVALID_FIELD_EXT, INVALID_FIELD),
        /**
         * Mail folder %1$s must not be moved to subsequent folder %2$s
         */
        NO_MOVE_TO_SUBFLD("Mail folder %1$s must not be moved to subsequent folder %2$s", Category.PERMISSION, 2032),
        /**
         * Mail folder %1$s must not be moved to subsequent folder %2$s on server %3$s with login %4$s (user=%5$s, context=%6$s)
         */
        NO_MOVE_TO_SUBFLD_EXT("Mail folder %1$s must not be moved to subsequent folder %2$s on server %3$s with login %4$s (user=%5$s, context=%6$s)", NO_MOVE_TO_SUBFLD),
        /**
         * Message could not be moved to trash folder
         */
        MOVE_ON_DELETE_FAILED("Message could not be moved to trash folder", Category.EXTERNAL_RESOURCE_FULL, 2034),
        /**
         * Message could not be moved to trash folder on server %1$s with login %2$s (user=%3$s, context=%4$s)
         */
        MOVE_ON_DELETE_FAILED_EXT("Message could not be moved to trash folder on server %1$s with login %2$s (user=%3$s, context=%4$s)", MOVE_ON_DELETE_FAILED),
        /**
         * Missing %1$s folder in mail move operation
         */
        MISSING_SOURCE_TARGET_FOLDER_ON_MOVE("Missing %1$s folder in mail move operation", Category.CODE_ERROR, 2035),
        /**
         * Missing %1$s folder in mail move operation on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        MISSING_SOURCE_TARGET_FOLDER_ON_MOVE_EXT("Missing %1$s folder in mail move operation on server %2$s with login %3$s (user=%4$s, context=%5$s)", MISSING_SOURCE_TARGET_FOLDER_ON_MOVE),
        /**
         * Message move aborted for user %1$s. Source and destination folder are equal to "%2$s"
         */
        NO_EQUAL_MOVE("Message move aborted for user %1$s. Source and destination folder are equal to \"%2$s\"", Category.USER_INPUT, 2036),
        /**
         * Message move aborted for user %1$s. Source and destination folder are equal to "%2$s" on server %3$s with login %4$s (user=%5$s,
         * context=%6$s)
         */
        NO_EQUAL_MOVE_EXT("Message move aborted for user %1$s. Source and destination folder are equal to \"%2$s\" on server %3$s with login %4$s (user=%5$s, context=%6$s)", NO_EQUAL_MOVE),
        /**
         * Folder read-only check failed
         */
        FAILED_READ_ONLY_CHECK("IMAP folder read-only check failed", Category.CODE_ERROR, 2037),
        /**
         * Folder read-only check failed on server %1$s with login %2$s (user=%3$s, context=%4$s)
         */
        FAILED_READ_ONLY_CHECK_EXT("Folder read-only check failed on server %1$s with login %2$s (user=%3$s, context=%4$s)", FAILED_READ_ONLY_CHECK),
        /**
         * Unknown folder open mode %1$s
         */
        UNKNOWN_FOLDER_MODE("Unknown folder open mode %1$s", Category.CODE_ERROR, 2038),
        /**
         * Unknown folder open mode %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        UNKNOWN_FOLDER_MODE_EXT("Unknown folder open mode %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", UNKNOWN_FOLDER_MODE),
        /**
         * Message(s) %1$s in folder %2$s could not be deleted due to error "%3$s"
         */
        UID_EXPUNGE_FAILED("Message(s) %1$s in folder %2$s could not be deleted due to error \"%3$s\"", Category.INTERNAL_ERROR, 2039),
        /**
         * Message(s) %1$s in folder %2$s could not be deleted due to error "%3$s" on server %4$s with login %5$s (user=%6$s, context=%7$s)
         */
        UID_EXPUNGE_FAILED_EXT("Message(s) %1$s in folder %2$s could not be deleted due to error \"%3$s\" on server %4$s with login %5$s (user=%6$s, context=%7$s)", UID_EXPUNGE_FAILED),
        /**
         * Not allowed to open folder %1$s due to missing read access
         */
        NO_FOLDER_OPEN("Not allowed to open folder %1$s due to missing read access", Category.PERMISSION, 2041),
        /**
         * Not allowed to open folder %1$s due to missing read access on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_FOLDER_OPEN_EXT("Not allowed to open folder %1$s due to missing read access on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_FOLDER_OPEN),
        /**
         * The raw content's input stream of message %1$s in folder %2$s cannot be read
         */
        MESSAGE_CONTENT_ERROR("The raw content's input stream of message %1$s in folder %2$s cannot be read", Category.CODE_ERROR, 2042),
        /**
         * The raw content's input stream of message %1$s in folder %2$s cannot be read on server %3$s with login %4$s (user=%5$s,
         * context=%6$s)
         */
        MESSAGE_CONTENT_ERROR_EXT("The raw content's input stream of message %1$s in folder %2$s cannot be read on server %3$s with login %4$s (user=%5$s, context=%6$s)", MESSAGE_CONTENT_ERROR),
        /**
         * No attachment was found with id %1$s in message
         */
        NO_ATTACHMENT_FOUND("No attachment was found with id %1$s in message", Category.USER_INPUT, 2043),
        /**
         * No attachment was found with id %1$s in message on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_ATTACHMENT_FOUND_EXT("No attachment was found with id %1$s in message on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_ATTACHMENT_FOUND),
        /**
         * Versit attachment could not be saved due to an unsupported MIME type: %1$s
         */
        UNSUPPORTED_VERSIT_ATTACHMENT(MailException.Code.UNSUPPORTED_VERSIT_ATTACHMENT, null),
        /**
         * Versit object %1$s could not be saved
         */
        FAILED_VERSIT_SAVE("Versit object could not be saved", Category.CODE_ERROR, 2045),
        /**
         * No support of capability "THREAD=REFERENCES"
         */
        THREAD_SORT_NOT_SUPPORTED("No support of capability \"THREAD=REFERENCES\"", Category.CODE_ERROR, 2046),
        /**
         * No support of capability "THREAD=REFERENCES" on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        THREAD_SORT_NOT_SUPPORTED_EXT("No support of capability \"THREAD=REFERENCES\" on server %2$s with login %3$s (user=%4$s, context=%5$s)", THREAD_SORT_NOT_SUPPORTED),
        /**
         * Unsupported charset-encoding: %1$s
         */
        ENCODING_ERROR(MailException.Code.ENCODING_ERROR, null),
        /**
         * A protocol exception occurred during execution of IMAP request "%1$s".<br>
         * Error message: %2$s
         */
        PROTOCOL_ERROR("A protocol exception occurred during execution of IMAP request \"%1$s\".\nError message: %2$s", Category.INTERNAL_ERROR, 2047),
        /**
         * Mail folder could not be found: %1$s.
         */
        FOLDER_NOT_FOUND(MIMEMailException.Code.FOLDER_NOT_FOUND, null),
        /**
         * Mail folder could not be found: %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s).
         */
        FOLDER_NOT_FOUND_EXT(MIMEMailException.Code.FOLDER_NOT_FOUND_EXT, FOLDER_NOT_FOUND),
        /**
         * An attempt was made to open a read-only folder with read-write "%1$s"
         */
        READ_ONLY_FOLDER(MIMEMailException.Code.READ_ONLY_FOLDER, null),
        /**
         * An attempt was made to open a read-only folder with read-write "%1$s" on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        READ_ONLY_FOLDER_EXT(MIMEMailException.Code.READ_ONLY_FOLDER_EXT, READ_ONLY_FOLDER),
        /**
         * Connect error: Connection was refused or timed out while attempting to connect to remote mail server %1$s for user %2$s.
         */
        CONNECT_ERROR(MIMEMailException.Code.CONNECT_ERROR, null),
        /**
         * Mailbox' root folder must not be source or the destination fullname of a move operation.
         */
        NO_ROOT_MOVE("Mailbox' root folder must not be source or the destination fullname of a move operation.", Category.CODE_ERROR, 2048),
        /**
         * Sort field %1$s is not supported via IMAP SORT command
         */
        UNSUPPORTED_SORT_FIELD("Sort field %1$s is not supported via IMAP SORT command", Category.CODE_ERROR, 2049),
        /**
         * Sort field %1$s is not supported via IMAP SORT command on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        UNSUPPORTED_SORT_FIELD_EXT("Sort field %1$s is not supported via IMAP SORT command on server %2$s with login %3$s (user=%4$s, context=%5$s)", UNSUPPORTED_SORT_FIELD),
        /**
         * Missing personal namespace
         */
        MISSING_PERSONAL_NAMESPACE("Missing personal namespace", Category.CODE_ERROR, 2050),
        /**
         * Parsing thread-sort string failed: %1$s.
         */
        THREAD_SORT_PARSING_ERROR("Parsing thread-sort string failed: %1$s.", Category.CODE_ERROR, 2051);

        private final String message;

        private final IMAPCode extend;

        private final int detailNumber;

        private final Category category;

        private IMAPCode(final String message, final Category category, final int detailNumber) {
            this.message = message;
            this.extend = null;
            this.detailNumber = extend.detailNumber;
            this.category = extend.category;
        }

        private IMAPCode(final String message, final IMAPCode extend) {
            this.message = message;
            this.extend = extend;
            this.detailNumber = extend.detailNumber;
            this.category = extend.category;
        }

        private IMAPCode(final MailException.Code code, final IMAPCode extend) {
            message = code.getMessage();
            this.extend = extend;
            detailNumber = code.getNumber();
            category = code.getCategory();
        }

        private IMAPCode(final MIMEMailException.Code code, final IMAPCode extend) {
            message = code.getMessage();
            this.extend = extend;
            detailNumber = code.getNumber();
            category = code.getCategory();
        }

        public Category getCategory() {
            return category;
        }

        public int getNumber() {
            return detailNumber;
        }

        public String getMessage() {
            return message;
        }

        private static final Map<IMAPCode, IMAPCode> EXT_MAP;

        static {
            final IMAPCode[] codes = IMAPCode.values();
            EXT_MAP = new EnumMap<IMAPCode, IMAPCode>(IMAPCode.class);
            for (int i = 0; i < codes.length; i++) {
                final IMAPCode code = codes[i];
                if (null != code.extend) {
                    EXT_MAP.put(code.extend, code);
                }
            }
        }

        /**
         * Gets the extended code for specified code.
         * 
         * @param code The code whose extended version shall be returned
         * @return The extended code for specified code or <code>null</code>
         */
        public static IMAPCode getExtendedCode(final IMAPCode code) {
            return EXT_MAP.get(code);
        }
    }

    /**
     * Throws a new IMAP exception for specified error code.
     * 
     * @param code The error code
     * @param imapConfig The IMAP configuration providing account information
     * @param session The session providing user information
     * @param messageArgs The message arguments
     * @return The new IMAP exception
     */
    public static IMAPException create(final Code code, final Object... messageArgs) {
        return create(code, null, null, null, messageArgs);
    }

    /**
     * Throws a new IMAP exception for specified error code.
     * 
     * @param code The error code
     * @param imapConfig The IMAP configuration providing account information
     * @param session The session providing user information
     * @param messageArgs The message arguments
     * @return The new IMAP exception
     */
    public static IMAPException create(final Code code, final Throwable cause, final Object... messageArgs) {
        return create(code, null, null, cause, messageArgs);
    }

    /**
     * Throws a new IMAP exception for specified error code.
     * 
     * @param code The error code
     * @param imapConfig The IMAP configuration providing account information
     * @param session The session providing user information
     * @param messageArgs The message arguments
     * @return The new IMAP exception
     */
    public static IMAPException create(final Code code, final IMAPConfig imapConfig, final Session session, final Object... messageArgs) {
        return create(code, imapConfig, session, null, messageArgs);
    }

    /**
     * Creates a new IMAP exception for specified error code.
     * 
     * @param code The error code
     * @param imapConfig The IMAP configuration providing account information
     * @param session The session providing user information
     * @param cause The initial cause
     * @param messageArgs The message arguments
     * @return The new IMAP exception
     */
    public static IMAPException create(final Code code, final IMAPConfig imapConfig, final Session session, final Throwable cause, final Object... messageArgs) {
        final IMAPCode imapCode = code.getImapCode();
        final IMAPCode extendedCode = IMAPCode.getExtendedCode(imapCode);
        if (null != imapConfig && null != session && null != extendedCode) {
            final Object[] newArgs;
            int k;
            if (null == messageArgs) {
                newArgs = new Object[4];
                k = 0;
            } else {
                newArgs = new Object[messageArgs.length + 4];
                System.arraycopy(messageArgs, 0, newArgs, 0, messageArgs.length);
                k = messageArgs.length;
            }
            newArgs[k++] = imapConfig.getServer();
            newArgs[k++] = imapConfig.getLogin();
            newArgs[k++] = Integer.valueOf(session.getUserId());
            newArgs[k++] = Integer.valueOf(session.getContextId());
            return new IMAPException(extendedCode.getCategory(), extendedCode.getNumber(), extendedCode.getMessage(), cause, newArgs);
        }
        return new IMAPException(imapCode.getCategory(), imapCode.getNumber(), imapCode.getMessage(), cause, messageArgs);
    }

    /**
     * Gets the message corresponding to specified error code with given message arguments applied.
     * 
     * @param code The code
     * @param msgArgs The message arguments
     * @return The message corresponding to specified error code with given message arguments applied
     */
    public static String getFormattedMessage(final Code code, final Object... msgArgs) {
        final IMAPCode imapCode = code.getImapCode();
        return String.format(imapCode.getMessage(), msgArgs);
    }

    private static final transient Object[] EMPTY_ARGS = new Object[0];

    /**
     * Initializes a new {@link IMAPException}
     * 
     * @param cause The cause
     */
    public IMAPException(final AbstractOXException cause) {
        super(cause);
    }

    /**
     * Initializes a new {@link IMAPException}
     * 
     * @param code The code
     */
    private IMAPException(final Code code) {
        this(code, EMPTY_ARGS);
    }

    /**
     * Initializes a new {@link IMAPException}
     * 
     * @param code The code
     * @param messageArgs The message arguments
     */
    private IMAPException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * Initializes a new {@link IMAPException}
     * 
     * @param code The code
     * @param cause The cause
     * @param messageArgs The message arguments
     */
    private IMAPException(final Code code, final Throwable cause, final Object... messageArgs) {
        this(code.getImapCode().getCategory(), code.getImapCode().getNumber(), code.getImapCode().getMessage(), cause, messageArgs);
    }

    private IMAPException(final Category category, final int number, final String message, final Throwable cause, final Object... messageArgs) {
        super(IMAPProvider.PROTOCOL_IMAP, category, number, message, cause);
        super.setMessageArgs(messageArgs);
    }

}
