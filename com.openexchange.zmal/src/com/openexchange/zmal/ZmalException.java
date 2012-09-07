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

package com.openexchange.zmal;

import java.util.EnumMap;
import java.util.Map;
import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.zmal.config.ZmalConfig;

/**
 * {@link ZmalException} - Indicates an IMAP error.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ZmalException extends OXException {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -8226676160145457046L;

    /**
     * The IMAP error code enumeration.
     */
    public static enum Code implements OXExceptionCode {

        /**
         * Missing parameter in mail connection: %1$s
         */
        MISSING_CONNECT_PARAM(ZmalCode.MISSING_CONNECT_PARAM),
        /**
         * No connection available to access mailbox
         */
        NOT_CONNECTED(ZmalCode.NOT_CONNECTED),
        /**
         * Missing parameter %1$s
         */
        MISSING_PARAMETER(ZmalCode.MISSING_PARAMETER),
        /**
         * A JSON error occurred: %1$s
         */
        JSON_ERROR(ZmalCode.JSON_ERROR),
        /**
         * Invalid CATEGORY_PERMISSION_DENIED values: fp=%d orp=%d owp=%d odp=%d
         */
        INVALID_PERMISSION(ZmalCode.INVALID_PERMISSION),
        /**
         * User %1$s has no mail module access due to user configuration
         */
        NO_MAIL_MODULE_ACCESS(ZmalCode.NO_MAIL_MODULE_ACCESS),
        /**
         * No access to mail folder %1$s
         */
        NO_ACCESS(ZmalCode.NO_ACCESS),
        /**
         * No lookup access to mail folder %1$s
         */
        NO_LOOKUP_ACCESS(ZmalCode.NO_LOOKUP_ACCESS),
        /**
         * No read access on mail folder %1$s
         */
        NO_READ_ACCESS(ZmalCode.NO_READ_ACCESS),
        /**
         * No delete access on mail folder %1$s
         */
        NO_DELETE_ACCESS(ZmalCode.NO_DELETE_ACCESS),
        /**
         * No insert access on mail folder %1$s
         */
        NO_INSERT_ACCESS(ZmalCode.NO_INSERT_ACCESS),
        /**
         * No create access on mail folder %1$s
         */
        NO_CREATE_ACCESS(ZmalCode.NO_CREATE_ACCESS),
        /**
         * No administer access on mail folder %1$s
         */
        NO_ADMINISTER_ACCESS(ZmalCode.NO_ADMINISTER_ACCESS),
        /**
         * No write access to IMAP folder %1$s
         */
        NO_WRITE_ACCESS(ZmalCode.NO_WRITE_ACCESS),
        /**
         * No keep-seen access on mail folder %1$s
         */
        NO_KEEP_SEEN_ACCESS(ZmalCode.NO_KEEP_SEEN_ACCESS),
        /**
         * Folder %1$s does not allow subfolders.
         */
        FOLDER_DOES_NOT_HOLD_FOLDERS(ZmalCode.FOLDER_DOES_NOT_HOLD_FOLDERS),
        /**
         * Mail folder cannot be created/rename. Name must not contain character '%1$s'
         */
        INVALID_FOLDER_NAME(ZmalCode.INVALID_FOLDER_NAME),
        /**
         * A folder named %1$s already exists
         */
        DUPLICATE_FOLDER(ZmalCode.DUPLICATE_FOLDER),
        /**
         * Mail folder "%1$s" could not be created (maybe due to insufficient CATEGORY_PERMISSION_DENIED on parent folder %2$s or due to an invalid folder
         * name)
         */
        FOLDER_CREATION_FAILED(ZmalCode.FOLDER_CREATION_FAILED),
        /**
         * The composed rights could not be applied to new folder %1$s due to missing administer right in its initial rights specified by
         * IMAP server. However, the folder has been created.
         */
        NO_ADMINISTER_ACCESS_ON_INITIAL(ZmalCode.NO_ADMINISTER_ACCESS_ON_INITIAL),
        /**
         * No admin CATEGORY_PERMISSION_DENIED specified for folder %1$s
         */
        NO_ADMIN_ACL(ZmalCode.NO_ADMIN_ACL),
        /**
         * Default folder %1$s must not be updated
         */
        NO_DEFAULT_FOLDER_UPDATE(ZmalCode.NO_DEFAULT_FOLDER_UPDATE),
        /**
         * Deletion of folder %1$s failed
         */
        DELETE_FAILED(ZmalCode.DELETE_FAILED),
        /**
         * IMAP default folder %1$s could not be created
         */
        NO_DEFAULT_FOLDER_CREATION(ZmalCode.NO_DEFAULT_FOLDER_CREATION),
        /**
         * Missing default %1$s folder
         */
        MISSING_DEFAULT_FOLDER_NAME(ZmalCode.MISSING_DEFAULT_FOLDER_NAME),
        /**
         * Update of folder %1$s failed
         */
        UPDATE_FAILED(ZmalCode.UPDATE_FAILED),
        /**
         * Rename of folder "%1$s" to "%2$s" failed with "%3$s".
         */
        RENAME_FAILED(ZmalCode.RENAME_FAILED),
        /**
         * Folder %1$s must not be deleted
         */
        NO_FOLDER_DELETE(ZmalCode.NO_FOLDER_DELETE),
        /**
         * Default folder %1$s must not be deleted
         */
        NO_DEFAULT_FOLDER_DELETE(ZmalCode.NO_DEFAULT_FOLDER_DELETE),
        /**
         * An I/O error occurred: %1$s
         */
        IO_ERROR(ZmalCode.IO_ERROR),
        /**
         * Flag %1$s could not be changed due to reason "%2$s"
         */
        FLAG_FAILED(ZmalCode.FLAG_FAILED),
        /**
         * Folder %1$s does not hold messages and is therefore not selectable
         */
        FOLDER_DOES_NOT_HOLD_MESSAGES(ZmalCode.FOLDER_DOES_NOT_HOLD_MESSAGES),
        /**
         * Number of search fields (IMAPCode.) do not match number of search patterns (IMAPCode.)
         */
        INVALID_SEARCH_PARAMS(ZmalCode.INVALID_SEARCH_PARAMS),
        /**
         * IMAP search failed due to reason "%1$s". Switching to application-based search
         */
        IMAP_SEARCH_FAILED(ZmalCode.IMAP_SEARCH_FAILED),
        /**
         * IMAP sort failed due to reason "%1$s". Switching to application-based sorting.
         */
        IMAP_SORT_FAILED(ZmalCode.IMAP_SORT_FAILED),
        /**
         * Unknown search field: %1$s
         */
        UNKNOWN_SEARCH_FIELD(ZmalCode.UNKNOWN_SEARCH_FIELD),
        /**
         * Message field %1$s cannot be handled
         */
        INVALID_FIELD(ZmalCode.INVALID_FIELD),
        /**
         * Mail folder %1$s must not be moved to subsequent folder %2$s
         */
        NO_MOVE_TO_SUBFLD(ZmalCode.NO_MOVE_TO_SUBFLD),
        /**
         * Message could not be moved to trash folder
         */
        MOVE_ON_DELETE_FAILED(ZmalCode.MOVE_ON_DELETE_FAILED),
        /**
         * Missing %1$s folder in mail move operation
         */
        MISSING_SOURCE_TARGET_FOLDER_ON_MOVE(ZmalCode.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE),
        /**
         * Message move aborted for user %1$s. Source and destination folder are equal to "%2$s"
         */
        NO_EQUAL_MOVE(ZmalCode.NO_EQUAL_MOVE),
        /**
         * Folder read-only check failed
         */
        FAILED_READ_ONLY_CHECK(ZmalCode.FAILED_READ_ONLY_CHECK),
        /**
         * Unknown folder open mode %1$s
         */
        UNKNOWN_FOLDER_MODE(ZmalCode.UNKNOWN_FOLDER_MODE),
        /**
         * Message(IMAPCode.Message) %1$s in folder %2$s could not be deleted due to error "%3$s"
         */
        UID_EXPUNGE_FAILED(ZmalCode.UID_EXPUNGE_FAILED),
        /**
         * Not allowed to open folder %1$s due to missing read access
         */
        NO_FOLDER_OPEN(ZmalCode.NO_FOLDER_OPEN),
        /**
         * The raw content's input stream of message %1$s in folder %2$s cannot be read
         */
        MESSAGE_CONTENT_ERROR(ZmalCode.MESSAGE_CONTENT_ERROR),
        /**
         * No attachment was found with id %1$s in message
         */
        NO_ATTACHMENT_FOUND(ZmalCode.NO_ATTACHMENT_FOUND),
        /**
         * Versit attachment could not be saved due to an unsupported MIME type: %1$s
         */
        UNSUPPORTED_VERSIT_ATTACHMENT(ZmalCode.UNSUPPORTED_VERSIT_ATTACHMENT),
        /**
         * Versit object %1$s could not be saved
         */
        FAILED_VERSIT_SAVE(ZmalCode.FAILED_VERSIT_SAVE),
        /**
         * No support of capability "THREAD=REFERENCES"
         */
        THREAD_SORT_NOT_SUPPORTED(ZmalCode.THREAD_SORT_NOT_SUPPORTED),
        /**
         * Unsupported charset-encoding: %1$s
         */
        ENCODING_ERROR(ZmalCode.ENCODING_ERROR),
        /**
         * A protocol exception occurred during execution of IMAP request "%1$s".<br>
         * Error message: %2$s
         */
        PROTOCOL_ERROR(ZmalCode.PROTOCOL_ERROR),
        /**
         * Mail folder "%1$s" could not be found.
         */
        FOLDER_NOT_FOUND(ZmalCode.FOLDER_NOT_FOUND),
        /**
         * An attempt was made to open a read-only folder with read-write "%1$s"
         */
        READ_ONLY_FOLDER(ZmalCode.READ_ONLY_FOLDER),
        /**
         * Connect error: Connection was refused or timed out while attempting to connect to remote mail server %1$s for user %2$s.
         */
        CONNECT_ERROR(ZmalCode.CONNECT_ERROR),
        /**
         * Mailbox' root folder must not be source or the destination full name of a move operation.
         */
        NO_ROOT_MOVE(ZmalCode.NO_ROOT_MOVE),
        /**
         * Sort field %1$s is not supported via IMAP SORT command
         */
        UNSUPPORTED_SORT_FIELD(ZmalCode.UNSUPPORTED_SORT_FIELD),
        /**
         * Missing personal namespace
         */
        MISSING_PERSONAL_NAMESPACE(ZmalCode.MISSING_PERSONAL_NAMESPACE),
        /**
         * Parsing thread-sort string failed: %1$s.
         */
        THREAD_SORT_PARSING_ERROR(ZmalCode.THREAD_SORT_PARSING_ERROR),
        /**
         * A SQL error occurred: %1$s
         */
        SQL_ERROR(ZmalCode.SQL_ERROR),
        /**
         * No rename access to mail folder %1$s
         */
        NO_RENAME_ACCESS(ZmalCode.NO_RENAME_ACCESS),
        /**
         * Unable to parse IMAP server URI "%1$s".
         */
        URI_PARSE_FAILED(ZmalCode.URI_PARSE_FAILED),
        /**
         * Default folder %1$s must not be unsubscribed.
         */
        NO_DEFAULT_FOLDER_UNSUBSCRIBE(ZmalCode.NO_DEFAULT_FOLDER_UNSUBSCRIBE),
        /**
         * IMAP server refuses to import one or more E-Mails.
         */
        INVALID_MESSAGE(ZmalCode.INVALID_MESSAGE),
        /**
         * Currently not possible to establish a new connection to server %1$s with login %2$s. Please try again.
         */
        CONNECTION_UNAVAILABLE(ZmalCode.CONNECTION_UNAVAILABLE),
        /**
         * A service error occurred: %1$s
         */
        SERVICE_ERROR(ZmalCode.SERVICE_ERROR),

        ;

        private final ZmalCode imapCode;

        private Code(final ZmalCode imapCode) {
            this.imapCode = imapCode;
        }

        ZmalCode getImapCode() {
            return imapCode;
        }

        @Override
        public int getNumber() {
            return imapCode.getNumber();
        }

        @Override
        public String getPrefix() {
            return imapCode.getPrefix();
        }

        @Override
        public Category getCategory() {
            return imapCode.getCategory();
        }

        @Override
        public String getMessage() {
            return imapCode.getMessage();
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
        public OXException create(final Throwable cause, final Object... args) {
            return OXExceptionFactory.getInstance().create(this, cause, args);
        }

    }

    public static enum ZmalCode {

        /**
         * Missing parameter in mail connection: %1$s
         */
        MISSING_CONNECT_PARAM(MailExceptionCode.MISSING_CONNECT_PARAM, null),
        /**
         * No connection available to access mailbox
         */
        NOT_CONNECTED("No connection available to access mailbox", Category.CATEGORY_ERROR, 2001),
        /**
         * No connection available to access mailbox on server %1$s with login %2$s (user=%3$s, context=%4$s)
         */
        NOT_CONNECTED_EXT("No connection available to access mailbox on server %1$s with login %2$s (user=%3$s, context=%4$s)", NOT_CONNECTED),
        /**
         * Missing parameter %1$s
         */
        MISSING_PARAMETER(MailExceptionCode.MISSING_PARAMETER, null),
        /**
         * A JSON error occurred: %1$s
         */
        JSON_ERROR(MailExceptionCode.JSON_ERROR, null),
        /**
         * Invalid CATEGORY_PERMISSION_DENIED values: fp=%d orp=%d owp=%d odp=%d
         */
        INVALID_PERMISSION(MailExceptionCode.INVALID_PERMISSION, null),
        /**
         * User %1$s has no mail module access due to user configuration
         */
        NO_MAIL_MODULE_ACCESS("User %1$s has no mail module access due to user configuration", Category.CATEGORY_PERMISSION_DENIED, 2003),
        /**
         * No access to mail folder %1$s
         */
        NO_ACCESS("No access to mail folder %1$s", Category.CATEGORY_PERMISSION_DENIED, 2003),
        /**
         * No access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_ACCESS_EXT("No access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_ACCESS),
        /**
         * No lookup access to mail folder %1$s
         */
        NO_LOOKUP_ACCESS("No lookup access to mail folder %1$s", Category.CATEGORY_PERMISSION_DENIED, 2004),
        /**
         * No lookup access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_LOOKUP_ACCESS_EXT("No lookup access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_LOOKUP_ACCESS),
        /**
         * No read access on mail folder %1$s
         */
        NO_READ_ACCESS("No read access to mail folder %1$s", Category.CATEGORY_PERMISSION_DENIED, 2005),
        /**
         * No read access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_READ_ACCESS_EXT("No read access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_READ_ACCESS),
        /**
         * No delete access on mail folder %1$s
         */
        NO_DELETE_ACCESS("No delete access to mail folder %1$s", Category.CATEGORY_PERMISSION_DENIED, 2006),
        /**
         * No delete access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_DELETE_ACCESS_EXT("No delete access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_DELETE_ACCESS),
        /**
         * No insert access on mail folder %1$s
         */
        NO_INSERT_ACCESS("No insert access to mail folder %1$s", Category.CATEGORY_PERMISSION_DENIED, 2007),
        /**
         * No insert access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_INSERT_ACCESS_EXT("No insert access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_INSERT_ACCESS),
        /**
         * No create access on mail folder %1$s
         */
        NO_CREATE_ACCESS(MailExceptionCode.NO_CREATE_ACCESS, null),
        /**
         * No create access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_CREATE_ACCESS_EXT(MailExceptionCode.NO_CREATE_ACCESS_EXT, NO_CREATE_ACCESS),
        /**
         * No administer access on mail folder %1$s
         */
        NO_ADMINISTER_ACCESS("No administer access to mail folder %1$s", Category.CATEGORY_PERMISSION_DENIED, 2009),
        /**
         * No administer access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_ADMINISTER_ACCESS_EXT("No administer access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_ADMINISTER_ACCESS),
        /**
         * No write access to IMAP folder %1$s
         */
        NO_WRITE_ACCESS("No write access to IMAP folder %1$s", Category.CATEGORY_PERMISSION_DENIED, 2010),
        /**
         * No write access to IMAP folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_WRITE_ACCESS_EXT("No write access to IMAP folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_WRITE_ACCESS),
        /**
         * No keep-seen access on mail folder %1$s
         */
        NO_KEEP_SEEN_ACCESS("No keep-seen access to mail folder %1$s", Category.CATEGORY_PERMISSION_DENIED, 2011),
        /**
         * No keep-seen access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_KEEP_SEEN_ACCESS_EXT("No keep-seen access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_KEEP_SEEN_ACCESS),
        /**
         * Folder %1$s does not allow subfolders.
         */
        FOLDER_DOES_NOT_HOLD_FOLDERS("Folder %1$s does not allow subfolders.", Category.CATEGORY_PERMISSION_DENIED, 2012),
        /**
         * Folder %1$s does not allow subfolders on server %2$s with login %3$s (user=%4$s, context=%5$s).
         */
        FOLDER_DOES_NOT_HOLD_FOLDERS_EXT("Folder %1$s does not allow subfolders on server %2$s with login %3$s (user=%4$s, context=%5$s).", FOLDER_DOES_NOT_HOLD_FOLDERS),
        /**
         * Mail folder cannot be created/rename. Name must not contain character '%1$s'
         */
        INVALID_FOLDER_NAME(MailExceptionCode.INVALID_FOLDER_NAME, null),
        /**
         * A folder named %1$s already exists
         */
        DUPLICATE_FOLDER(MailExceptionCode.DUPLICATE_FOLDER, null),
        /**
         * A folder named %1$s already exists on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        DUPLICATE_FOLDER_EXT(MailExceptionCode.DUPLICATE_FOLDER_EXT, DUPLICATE_FOLDER),
        /**
         * Mail folder "%1$s" could not be created (maybe due to insufficient CATEGORY_PERMISSION_DENIED on parent folder %2$s or due to an invalid folder
         * name)
         */
        FOLDER_CREATION_FAILED("Mail folder \"%1$s\" could not be created (maybe due to insufficient CATEGORY_PERMISSION_DENIED on parent folder %2$s or due to an invalid folder name)", Category.CATEGORY_USER_INPUT, 2015),
        /**
         * Mail folder "%1$s" could not be created (maybe due to insufficient CATEGORY_PERMISSION_DENIED on parent folder %2$s or due to an invalid folder
         * name) on server %3$s with login %4$s (user=%5$s, context=%6$s)
         */
        FOLDER_CREATION_FAILED_EXT("Mail folder \"%1$s\" could not be created (maybe due to insufficient CATEGORY_PERMISSION_DENIED on parent folder %2$s or due to an invalid folder name) on server %3$s with login %4$s (user=%5$s, context=%6$s)", FOLDER_CREATION_FAILED),
        /**
         * The composed rights could not be applied to new folder %1$s due to missing administer right in its initial rights specified by
         * IMAP server. However, the folder has been created.
         */
        NO_ADMINISTER_ACCESS_ON_INITIAL("The composed rights could not be applied to new folder %1$s due to missing administer right in its initial rights specified by IMAP server. However, the folder has been created.", Category.CATEGORY_PERMISSION_DENIED, 2016),
        /**
         * The composed rights could not be applied to new folder %1$s due to missing administer right in its initial rights specified by
         * IMAP server. However, the folder has been created on server %2$s with login %3$s (user=%4$s, context=%5$s).
         */
        NO_ADMINISTER_ACCESS_ON_INITIAL_EXT("The composed rights could not be applied to new folder %1$s due to missing administer right in its initial rights specified by IMAP server. However, the folder has been created on server %2$s with login %3$s (user=%4$s, context=%5$s).", NO_ADMINISTER_ACCESS_ON_INITIAL),
        /**
         * No admin CATEGORY_PERMISSION_DENIED specified for folder %1$s
         */
        NO_ADMIN_ACL("No administer CATEGORY_PERMISSION_DENIED specified for folder %1$s", Category.CATEGORY_USER_INPUT, 2017),
        /**
         * No admin CATEGORY_PERMISSION_DENIED specified for folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_ADMIN_ACL_EXT("No administer CATEGORY_PERMISSION_DENIED specified for folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_ADMIN_ACL),
        /**
         * Default folder %1$s must not be updated
         */
        NO_DEFAULT_FOLDER_UPDATE("Default folder %1$s must not be updated", Category.CATEGORY_PERMISSION_DENIED, 2018),
        /**
         * Default folder %1$s must not be updated on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_DEFAULT_FOLDER_UPDATE_EXT("Default folder %1$s must not be updated on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_DEFAULT_FOLDER_UPDATE),
        /**
         * Deletion of folder %1$s failed
         */
        DELETE_FAILED("Deletion of folder %1$s failed", Category.CATEGORY_ERROR, 2019),
        /**
         * Deletion of folder %1$s failed on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        DELETE_FAILED_EXT("Deletion of folder %1$s failed on server %2$s with login %3$s (user=%4$s, context=%5$s)", DELETE_FAILED),
        /**
         * IMAP default folder %1$s could not be created
         */
        NO_DEFAULT_FOLDER_CREATION("IMAP default folder %1$s could not be created", Category.CATEGORY_ERROR, 2020),
        /**
         * IMAP default folder %1$s could not be created on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_DEFAULT_FOLDER_CREATION_EXT("IMAP default folder %1$s could not be created on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_DEFAULT_FOLDER_CREATION),
        /**
         * Missing default %1$s folder
         */
        MISSING_DEFAULT_FOLDER_NAME("Missing default %1$s folder", Category.CATEGORY_ERROR, 2021),
        /**
         * Missing default %1$s folder on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        MISSING_DEFAULT_FOLDER_NAME_EXT("Missing default %1$s folder on server %2$s with login %3$s (user=%4$s, context=%5$s)", MISSING_DEFAULT_FOLDER_NAME),
        /**
         * Update of folder %1$s failed
         */
        UPDATE_FAILED("Update of folder %1$s failed", Category.CATEGORY_ERROR, 2022),
        /**
         * Update of folder %1$s failed on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        UPDATE_FAILED_EXT("Update of folder %1$s failed on server %2$s with login %3$s (user=%4$s, context=%5$s)", UPDATE_FAILED),
        /**
         * Folder %1$s must not be deleted
         */
        NO_FOLDER_DELETE("Folder %1$s cannot be deleted", Category.CATEGORY_PERMISSION_DENIED, 2023),
        /**
         * Folder %1$s must not be deleted on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_FOLDER_DELETE_EXT("Folder %1$s cannot be deleted on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_FOLDER_DELETE),
        /**
         * Default folder %1$s must not be deleted
         */
        NO_DEFAULT_FOLDER_DELETE("Default folder %1$s cannot be deleted", Category.CATEGORY_PERMISSION_DENIED, 2024),
        /**
         * Default folder %1$s must not be deleted on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_DEFAULT_FOLDER_DELETE_EXT("Default folder %1$s cannot be deleted on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_DEFAULT_FOLDER_DELETE),
        /**
         * An I/O error occurred: %1$s
         */
        IO_ERROR(MailExceptionCode.IO_ERROR, null),
        /**
         * Flag %1$s could not be changed due to reason "%2$s"
         */
        FLAG_FAILED("Flag %1$s could not be changed due to following reason \"%2$s\"", Category.CATEGORY_ERROR, 2025),
        /**
         * Flag %1$s could not be changed due to reason "%2$s" on server %3$s with login %4$s (user=%5$s, context=%6$s)
         */
        FLAG_FAILED_EXT("Flag %1$s could not be changed due to following reason \"%2$s\" on server %3$s with login %4$s (user=%5$s, context=%6$s)", FLAG_FAILED),
        /**
         * Folder %1$s does not hold messages and is therefore not selectable
         */
        FOLDER_DOES_NOT_HOLD_MESSAGES(MailExceptionCode.FOLDER_DOES_NOT_HOLD_MESSAGES, null),
        /**
         * Folder %1$s does not hold messages and is therefore not selectable
         */
        FOLDER_DOES_NOT_HOLD_MESSAGES_EXT(MailExceptionCode.FOLDER_DOES_NOT_HOLD_MESSAGES_EXT, FOLDER_DOES_NOT_HOLD_MESSAGES),
        /**
         * Number of search fields (%d) do not match number of search patterns (%d)
         */
        INVALID_SEARCH_PARAMS("Number of search fields (%d) do not match number of search patterns (%d)", Category.CATEGORY_ERROR, 2028),
        /**
         * IMAP search failed due to reason "%1$s". Switching to application-based search
         */
        IMAP_SEARCH_FAILED("IMAP search failed due to reason \"%1$s\". Switching to application-based search", Category.CATEGORY_SERVICE_DOWN, 2029),
        /**
         * IMAP search failed due to reason "%1$s" on server %2$s with login %3$s (user=%4$s, context=%5$s). Switching to application-based
         * search.
         */
        IMAP_SEARCH_FAILED_EXT("IMAP search failed due to reason \"%1$s\" on server %2$s with login %3$s (user=%4$s, context=%5$s). Switching to application-based search.", IMAP_SEARCH_FAILED),
        /**
         * IMAP sort failed due to reason "%1$s". Switching to application-based sorting.
         */
        IMAP_SORT_FAILED("IMAP sort failed due to reason \"%1$s\". Switching to application-based sorting.", Category.CATEGORY_SERVICE_DOWN, 2030),
        /**
         * IMAP sort failed due to reason "%1$s" on server %2$s with login %3$s (user=%4$s, context=%5$s). Switching to application-based
         * sorting.
         */
        IMAP_SORT_FAILED_EXT("IMAP sort failed due to reason \"%1$s\" on server %2$s with login %3$s (user=%4$s, context=%5$s). Switching to application-based sorting.", IMAP_SORT_FAILED),
        /**
         * Unknown search field: %1$s
         */
        UNKNOWN_SEARCH_FIELD("Unknown search field: %1$s", Category.CATEGORY_ERROR, 2031),
        /**
         * Unknown search field: %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        UNKNOWN_SEARCH_FIELD_EXT("Unknown search field: %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", UNKNOWN_SEARCH_FIELD),
        /**
         * Message field %1$s cannot be handled
         */
        INVALID_FIELD(MailExceptionCode.INVALID_FIELD, null),
        /**
         * Message field %1$s cannot be handled on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        INVALID_FIELD_EXT(MailExceptionCode.INVALID_FIELD_EXT, INVALID_FIELD),
        /**
         * Mail folder %1$s must not be moved to subsequent folder %2$s
         */
        NO_MOVE_TO_SUBFLD("Mail folder %1$s must not be moved to subsequent folder %2$s", Category.CATEGORY_PERMISSION_DENIED, 2032),
        /**
         * Mail folder %1$s must not be moved to subsequent folder %2$s on server %3$s with login %4$s (user=%5$s, context=%6$s)
         */
        NO_MOVE_TO_SUBFLD_EXT("Mail folder %1$s must not be moved to subsequent folder %2$s on server %3$s with login %4$s (user=%5$s, context=%6$s)", NO_MOVE_TO_SUBFLD),
        /**
         * Message could not be moved to trash folder
         */
        MOVE_ON_DELETE_FAILED("Message could not be moved to trash folder", Category.CATEGORY_CAPACITY, 2034),
        /**
         * Message could not be moved to trash folder on server %1$s with login %2$s (user=%3$s, context=%4$s)
         */
        MOVE_ON_DELETE_FAILED_EXT("Message could not be moved to trash folder on server %1$s with login %2$s (user=%3$s, context=%4$s)", MOVE_ON_DELETE_FAILED),
        /**
         * Missing %1$s folder in mail move operation
         */
        MISSING_SOURCE_TARGET_FOLDER_ON_MOVE("Missing %1$s folder in mail move operation", Category.CATEGORY_ERROR, 2035),
        /**
         * Missing %1$s folder in mail move operation on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        MISSING_SOURCE_TARGET_FOLDER_ON_MOVE_EXT("Missing %1$s folder in mail move operation on server %2$s with login %3$s (user=%4$s, context=%5$s)", MISSING_SOURCE_TARGET_FOLDER_ON_MOVE),
        /**
         * Message move aborted for user %1$s. Source and destination folder are equal to "%2$s"
         */
        NO_EQUAL_MOVE("Message move aborted for user %1$s. Source and destination folder are equal to \"%2$s\"", Category.CATEGORY_USER_INPUT, 2036),
        /**
         * Message move aborted for user %1$s. Source and destination folder are equal to "%2$s" on server %3$s with login %4$s (user=%5$s,
         * context=%6$s)
         */
        NO_EQUAL_MOVE_EXT("Message move aborted for user %1$s. Source and destination folder are equal to \"%2$s\" on server %3$s with login %4$s (user=%5$s, context=%6$s)", NO_EQUAL_MOVE),
        /**
         * Folder read-only check failed
         */
        FAILED_READ_ONLY_CHECK("IMAP folder read-only check failed", Category.CATEGORY_ERROR, 2037),
        /**
         * Folder read-only check failed on server %1$s with login %2$s (user=%3$s, context=%4$s)
         */
        FAILED_READ_ONLY_CHECK_EXT("Folder read-only check failed on server %1$s with login %2$s (user=%3$s, context=%4$s)", FAILED_READ_ONLY_CHECK),
        /**
         * Unknown folder open mode %1$s
         */
        UNKNOWN_FOLDER_MODE("Unknown folder open mode %1$s", Category.CATEGORY_ERROR, 2038),
        /**
         * Unknown folder open mode %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        UNKNOWN_FOLDER_MODE_EXT("Unknown folder open mode %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", UNKNOWN_FOLDER_MODE),
        /**
         * Message(s) %1$s in folder %2$s could not be deleted due to error "%3$s"
         */
        UID_EXPUNGE_FAILED("Message(s) %1$s in folder %2$s could not be deleted due to error \"%3$s\"", Category.CATEGORY_ERROR, 2039),
        /**
         * Message(s) %1$s in folder %2$s could not be deleted due to error "%3$s" on server %4$s with login %5$s (user=%6$s, context=%7$s)
         */
        UID_EXPUNGE_FAILED_EXT("Message(s) %1$s in folder %2$s could not be deleted due to error \"%3$s\" on server %4$s with login %5$s (user=%6$s, context=%7$s)", UID_EXPUNGE_FAILED),
        /**
         * Not allowed to open folder %1$s due to missing read access
         */
        NO_FOLDER_OPEN("Not allowed to open folder %1$s due to missing read access", Category.CATEGORY_PERMISSION_DENIED, 2041),
        /**
         * Not allowed to open folder %1$s due to missing read access on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_FOLDER_OPEN_EXT("Not allowed to open folder %1$s due to missing read access on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_FOLDER_OPEN),
        /**
         * The raw content's input stream of message %1$s in folder %2$s cannot be read
         */
        MESSAGE_CONTENT_ERROR("The raw content's input stream of message %1$s in folder %2$s cannot be read", Category.CATEGORY_ERROR, 2042),
        /**
         * The raw content's input stream of message %1$s in folder %2$s cannot be read on server %3$s with login %4$s (user=%5$s,
         * context=%6$s)
         */
        MESSAGE_CONTENT_ERROR_EXT("The raw content's input stream of message %1$s in folder %2$s cannot be read on server %3$s with login %4$s (user=%5$s, context=%6$s)", MESSAGE_CONTENT_ERROR),
        /**
         * No attachment was found with id %1$s in message
         */
        NO_ATTACHMENT_FOUND("No attachment was found with id %1$s in message", Category.CATEGORY_USER_INPUT, 2043),
        /**
         * No attachment was found with id %1$s in message on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_ATTACHMENT_FOUND_EXT("No attachment was found with id %1$s in message on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_ATTACHMENT_FOUND),
        /**
         * Versit attachment could not be saved due to an unsupported MIME type: %1$s
         */
        UNSUPPORTED_VERSIT_ATTACHMENT(MailExceptionCode.UNSUPPORTED_VERSIT_ATTACHMENT, null),
        /**
         * Versit object %1$s could not be saved
         */
        FAILED_VERSIT_SAVE("Versit object could not be saved", Category.CATEGORY_ERROR, 2045),
        /**
         * No support of capability "THREAD=REFERENCES"
         */
        THREAD_SORT_NOT_SUPPORTED("No support of capability \"THREAD=REFERENCES\"", Category.CATEGORY_ERROR, 2046),
        /**
         * No support of capability "THREAD=REFERENCES" on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        THREAD_SORT_NOT_SUPPORTED_EXT("No support of capability \"THREAD=REFERENCES\" on server %2$s with login %3$s (user=%4$s, context=%5$s)", THREAD_SORT_NOT_SUPPORTED),
        /**
         * Unsupported charset-encoding: %1$s
         */
        ENCODING_ERROR(MailExceptionCode.ENCODING_ERROR, null),
        /**
         * A protocol exception occurred during execution of IMAP request "%1$s".<br>
         * Error message: %2$s
         */
        PROTOCOL_ERROR("A protocol exception occurred during execution of IMAP request \"%1$s\".\nError message: %2$s", Category.CATEGORY_ERROR, 2047),
        /**
         * Mail folder "%1$s" could not be found.
         */
        FOLDER_NOT_FOUND(MimeMailExceptionCode.FOLDER_NOT_FOUND, null),
        /**
         * Mail folder could not be found: %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s).
         */
        FOLDER_NOT_FOUND_EXT(MimeMailExceptionCode.FOLDER_NOT_FOUND_EXT, FOLDER_NOT_FOUND),
        /**
         * An attempt was made to open a read-only folder with read-write "%1$s"
         */
        READ_ONLY_FOLDER(MimeMailExceptionCode.READ_ONLY_FOLDER, null),
        /**
         * An attempt was made to open a read-only folder with read-write "%1$s" on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        READ_ONLY_FOLDER_EXT(MimeMailExceptionCode.READ_ONLY_FOLDER_EXT, READ_ONLY_FOLDER),
        /**
         * Connect error: Connection was refused or timed out while attempting to connect to remote mail server %1$s for user %2$s.
         */
        CONNECT_ERROR(MimeMailExceptionCode.CONNECT_ERROR, null),
        /**
         * Mailbox' root folder must not be source or the destination full name of a move operation.
         */
        NO_ROOT_MOVE("Mailbox' root folder must not be source or the destination full name of a move operation.", Category.CATEGORY_ERROR, 2048),
        /**
         * Sort field %1$s is not supported via IMAP SORT command
         */
        UNSUPPORTED_SORT_FIELD("Sort field %1$s is not supported via IMAP SORT command", Category.CATEGORY_ERROR, 2049),
        /**
         * Sort field %1$s is not supported via IMAP SORT command on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        UNSUPPORTED_SORT_FIELD_EXT("Sort field %1$s is not supported via IMAP SORT command on server %2$s with login %3$s (user=%4$s, context=%5$s)", UNSUPPORTED_SORT_FIELD),
        /**
         * Missing personal namespace
         */
        MISSING_PERSONAL_NAMESPACE("Missing personal namespace", Category.CATEGORY_ERROR, 2050),
        /**
         * Parsing thread-sort string failed: %1$s.
         */
        THREAD_SORT_PARSING_ERROR("Parsing thread-sort string failed: %1$s.", Category.CATEGORY_ERROR, 2051),
        /**
         * A SQL error occurred: %1$s
         */
        SQL_ERROR("A SQL error occurred: %1$s", Category.CATEGORY_ERROR, 2052),
        /**
         * Rename of folder "%1$s" to "%2$s" failed with "%3$s".
         */
        RENAME_FAILED("Rename of folder \"%1$s\" to \"%2$s\" failed with \"%3$s\".", Category.CATEGORY_ERROR, 2053),
        /**
         * Rename of folder "%1$s" to "%2$s" failed on server %3$s with login %4$s (user=%5$s, context=%6$s).
         */
        RENAME_FAILED_EXT("Rename of folder \"%1$s\" to \"%2$s\" failed with \"%3$s\" on server %4$s with login %5$s (user=%6$s, context=%7$s).", RENAME_FAILED),
        /**
         * No rename access to mail folder %1$s
         */
        NO_RENAME_ACCESS("No rename access to mail folder %1$s", Category.CATEGORY_PERMISSION_DENIED, 2054),
        /**
         * No rename access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_RENAME_ACCESS_EXT("No rename access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_RENAME_ACCESS),
        /**
         * Unable to parse IMAP server URI "%1$s".
         */
        URI_PARSE_FAILED("Unable to parse IMAP server URI \"%1$s\".", Category.CATEGORY_CONFIGURATION, 2055),
        /**
         * Default folder %1$s must not be unsubscribed.
         */
        NO_DEFAULT_FOLDER_UNSUBSCRIBE("Default folder %1$s must not be unsubscribed.", Category.CATEGORY_USER_INPUT, 2056),
        /**
         * Default folder %1$s must not be unsubscribed on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_DEFAULT_FOLDER_UNSUBSCRIBE_EXT("Default folder %1$s must not be unsubscribed on server %2$s with login %3$s (user=%4$s, context=%5$s)", NO_DEFAULT_FOLDER_UNSUBSCRIBE),
        /**
         * IMAP server refuses to import one or more E-Mails.
         */
        INVALID_MESSAGE("IMAP server refuses to import one or more E-Mails.", Category.CATEGORY_USER_INPUT, 2057),
        /**
         * IMAP server %1$s refuses to import one or more E-Mails with login %2$s (user=%3$s, context=%3$s)
         */
        INVALID_MESSAGE_EXT("IMAP server %1$s refuses to import one or more E-Mails with login %2$s (user=%3$s, context=%4$s)", INVALID_MESSAGE),
        /**
         * Currently not possible to establish a new connection to server %1$s with login %2$s. Please try again.
         */
        CONNECTION_UNAVAILABLE("Currently not possible to establish a new connection to server %1$s with login %2$s. Please try again.", Category.CATEGORY_TRY_AGAIN, 2058),
        /**
         * A service error occurred: %1$s
         */
        SERVICE_ERROR("A service error occurred: %1$s", Category.CATEGORY_USER_INPUT, 2059),

        ;

        private final String message;

        /*
         * The IMAPCode this IMAPCode extends the base code.
         */
        private final ZmalCode extend;

        private final int detailNumber;

        private static final String PREFIX = ZmalProvider.PROTOCOL_ZMAL.getName().toUpperCase();

        private final Category category;
        
        private ZmalCode(final String message, final Category category, final int detailNumber) {
            this.message = message;
            extend = null;
            this.detailNumber = detailNumber;
            this.category = category;
        }

        private ZmalCode(final String message, final ZmalCode extend) {
            this.message = message;
            this.extend = extend;
            detailNumber = extend.detailNumber;
            category = extend.category;
        }

        private ZmalCode(final MailExceptionCode code, final ZmalCode extend) {
            message = code.getMessage();
            this.extend = extend;
            detailNumber = code.getNumber();
            category = code.getCategory();
        }

        private ZmalCode(final MimeMailExceptionCode code, final ZmalCode extend) {
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

        public String getPrefix() {
            return PREFIX;
        }

        private static final Map<ZmalCode, ZmalCode> EXT_MAP;

        static {
            final ZmalCode[] codes = ZmalCode.values();
            EXT_MAP = new EnumMap<ZmalCode, ZmalCode>(ZmalCode.class);
            for (int i = 0; i < codes.length; i++) {
                final ZmalCode code = codes[i];
                if (null != code.extend) {
                    //code.extend is actually the base code that is extended
                    //e.g. NOT_CONNECTED(code.extend) -> NOT_CONNECTED_EXT(code)
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
        static ZmalCode getExtendedCode(final ZmalCode code) {
            return EXT_MAP.get(code);
        }

        /**
         * Creates a new {@link OXException} instance pre-filled with this code's attributes.
         *
         * @return The newly created {@link OXException} instance
         */
        public OXException create() {
            return create(new Object[0]);
        }

        /**
         * Creates a new {@link OXException} instance pre-filled with this code's attributes.
         *
         * @param args The message arguments in case of printf-style message
         * @return The newly created {@link OXException} instance
         */
        public OXException create(final Object... args) {
            return create((Throwable) null, args);
        }

        /**
         * Creates a new {@link OXException} instance pre-filled with this code's attributes.
         *
         * @param cause The optional initial cause
         * @param args The message arguments in case of printf-style message
         * @return The newly created {@link OXException} instance
         */
        public OXException create(final Throwable cause, final Object... args) {
            final OXException ret;
            if (category.getLogLevel().implies(LogLevel.DEBUG)) {
                ret = new OXException(detailNumber, message, cause, args);
            } else {
                ret = new OXException(detailNumber, Category.EnumType.TRY_AGAIN.equals(category.getType()) ? OXExceptionStrings.MESSAGE_RETRY : OXExceptionStrings.MESSAGE, new Object[0]).setLogMessage(message, args);
            }
            return ret.addCategory(category).setPrefix(PREFIX);
        }
    }

    /**
     * Throws a new OXException for specified error code.
     *
     * @param code The error code
     * @param messageArgs The message arguments
     * @return The new OXException
     */
    public static OXException create(final Code code, final Object... messageArgs) {
        return create(code, null, null, null, messageArgs);
    }

    /**
     * Throws a new OXException for specified error code.
     *
     * @param code The error code
     * @param cause The initial cause
     * @param messageArgs The message arguments
     * @return The new OXException
     */
    public static OXException create(final Code code, final Throwable cause, final Object... messageArgs) {
        return create(code, null, null, cause, messageArgs);
    }

    /**
     * Throws a new OXException for specified error code.
     *
     * @param code The error code
     * @param imapConfig The IMAP configuration providing account information
     * @param session The session providing user information
     * @param messageArgs The message arguments
     * @return The new OXException
     */
    public static OXException create(final Code code, final ZmalConfig imapConfig, final Session session, final Object... messageArgs) {
        return create(code, imapConfig, session, null, messageArgs);
    }

    private static final int EXT_LENGTH = 4;

    /**
     * Creates a new OXException for specified error code.
     *
     * @param code The error code
     * @param imapConfig The IMAP configuration providing account information
     * @param session The session providing user information
     * @param cause The initial cause
     * @param messageArgs The message arguments
     * @return The new OXException
     */
    public static OXException create(final Code code, final ZmalConfig imapConfig, final Session session, final Throwable cause, final Object... messageArgs) {
        final ZmalCode imapCode = code.getImapCode();
        if (null != imapConfig && null != session) {
            final ZmalCode extendedCode = ZmalCode.getExtendedCode(imapCode);
            if (null == extendedCode) {
                return code.create(cause, messageArgs);
            }
            final Object[] newArgs;
            int k;
            if (null == messageArgs) {
                newArgs = new Object[EXT_LENGTH];
                k = 0;
            } else {
                newArgs = new Object[messageArgs.length + EXT_LENGTH];
                System.arraycopy(messageArgs, 0, newArgs, 0, messageArgs.length);
                k = messageArgs.length;
            }
            newArgs[k++] = imapConfig.getServer();
            newArgs[k++] = imapConfig.getLogin();
            newArgs[k++] = Integer.valueOf(session.getUserId());
            newArgs[k++] = Integer.valueOf(session.getContextId());
            return extendedCode.create(cause, newArgs);
        }
        return code.create(cause, messageArgs);
    }

    /**
     * Gets the message corresponding to specified error code with given message arguments applied.
     *
     * @param code The code
     * @param msgArgs The message arguments
     * @return The message corresponding to specified error code with given message arguments applied
     */
    public static String getFormattedMessage(final Code code, final Object... msgArgs) {
        final ZmalCode imapCode = code.getImapCode();
        return String.format(imapCode.getMessage(), msgArgs);
    }

    private ZmalException() {
        super();
    }

}
