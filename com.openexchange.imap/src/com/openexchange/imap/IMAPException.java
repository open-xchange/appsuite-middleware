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

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.mail.Folder;
import javax.mail.MessagingException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.imap.cache.ListLsubCache;
import com.openexchange.imap.cache.RightsCache;
import com.openexchange.imap.cache.UserFlagsCache;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.services.Services;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.push.PushEventConstants;
import com.openexchange.session.Session;
import com.sun.mail.iap.ConnectQuotaExceededException;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPException} - Indicates an IMAP error.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPException extends OXException {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -8226676160145457046L;

    /**
     * The IMAP error code enumeration.
     */
    public static enum Code implements DisplayableOXExceptionCode {

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
         * Invalid CATEGORY_PERMISSION_DENIED values: fp=%d orp=%d owp=%d odp=%d
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
         * No read access on mail folder %1$s
         */
        NO_READ_ACCESS(IMAPCode.NO_READ_ACCESS),
        /**
         * No delete access on mail folder %1$s
         */
        NO_DELETE_ACCESS(IMAPCode.NO_DELETE_ACCESS),
        /**
         * No insert access on mail folder %1$s
         */
        NO_INSERT_ACCESS(IMAPCode.NO_INSERT_ACCESS),
        /**
         * No create access on mail folder %1$s
         */
        NO_CREATE_ACCESS(IMAPCode.NO_CREATE_ACCESS),
        /**
         * No administer access on mail folder %1$s
         */
        NO_ADMINISTER_ACCESS(IMAPCode.NO_ADMINISTER_ACCESS),
        /**
         * No write access to IMAP folder %1$s
         */
        NO_WRITE_ACCESS(IMAPCode.NO_WRITE_ACCESS),
        /**
         * No keep-seen access on mail folder %1$s
         */
        NO_KEEP_SEEN_ACCESS(IMAPCode.NO_KEEP_SEEN_ACCESS),
        /**
         * Folder %1$s does not allow subfolders.
         */
        FOLDER_DOES_NOT_HOLD_FOLDERS(IMAPCode.FOLDER_DOES_NOT_HOLD_FOLDERS),
        /**
         * Invalid folder name: "%1$s". Please avoid the following characters: %2$s
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
         * No admin CATEGORY_PERMISSION_DENIED specified for folder %1$s
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
         * Rename of folder "%1$s" to "%2$s" failed with "%3$s".
         */
        RENAME_FAILED(IMAPCode.RENAME_FAILED),
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
         * This message could not be moved to trash folder, possibly because your mailbox is nearly full.<br>
         * In that case, please try to empty your deleted items first, or delete smaller messages first.
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
         * Message %1$s in folder %2$s could not be deleted due to error "%3$s"
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
         * Mail folder "%1$s" could not be found.
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
         * Mailbox' root folder must not be source or the destination full name of a move operation.
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
        THREAD_SORT_PARSING_ERROR(IMAPCode.THREAD_SORT_PARSING_ERROR),
        /**
         * A SQL error occurred: %1$s
         */
        SQL_ERROR(IMAPCode.SQL_ERROR),
        /**
         * No rename access to mail folder %1$s
         */
        NO_RENAME_ACCESS(IMAPCode.NO_RENAME_ACCESS),
        /**
         * Unable to parse IMAP server URI "%1$s".
         */
        URI_PARSE_FAILED(IMAPCode.URI_PARSE_FAILED),
        /**
         * Default folder %1$s must not be unsubscribed.
         */
        NO_DEFAULT_FOLDER_UNSUBSCRIBE(IMAPCode.NO_DEFAULT_FOLDER_UNSUBSCRIBE),
        /**
         * IMAP server refuses to import one or more E-Mails.
         */
        INVALID_MESSAGE(IMAPCode.INVALID_MESSAGE),
        /**
         * Currently not possible to establish a new connection to server %1$s with login %2$s. Please try again.
         */
        CONNECTION_UNAVAILABLE(IMAPCode.CONNECTION_UNAVAILABLE),

        ;

        private final IMAPCode imapCode;

        private Code(final IMAPCode imapCode) {
            this.imapCode = imapCode;
        }

        IMAPCode getImapCode() {
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

        @Override
        public String getDisplayMessage() {
            return imapCode.getDisplayMessage();
        }

    }

    // ----------------------------------------------------------------------------------------------------- //

    private static final String NOT_CONNECTED_MSG = "No connection available to access mailbox";

    private static final String NOT_CONNECTED_EXT_MSG = "No connection available to access mailbox on server %1$s with login %2$s (user=%3$s, context=%4$s)";

    private static final String NO_MAIL_MODULE_ACCESS_MSG = "User %1$s has no mail module access due to user configuration";

    private static final String NO_ACCESS_MSG = "No access to mail folder %1$s";

    private static final String NO_ACCESS_EXT_MSG = "No access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String NO_LOOKUP_ACCESS_MSG = "No lookup access to mail folder %1$s";

    private static final String NO_LOOKUP_ACCESS_EXT_MSG = "No lookup access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String NO_READ_ACCESS_MSG = "No read access to mail folder %1$s";

    private static final String NO_READ_ACCESS_EXT_MSG = "No read access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String NO_DELETE_ACCESS_MSG = "No delete access to mail folder %1$s";

    private static final String NO_DELETE_ACCESS_EXT_MSG = "No delete access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String NO_INSERT_ACCESS_MSG = "No insert access to mail folder %1$s";

    private static final String NO_INSERT_ACCESS_EXT_MSG = "No insert access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String NO_ADMINISTER_ACCESS_MSG = "No administer access to mail folder %1$s";

    private static final String NO_ADMINISTER_ACCESS_EXT_MSG = "No administer access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String NO_WRITE_ACCESS_MSG = "No write access to IMAP folder %1$s";

    private static final String NO_WRITE_ACCESS_EXT_MSG = "No write access to IMAP folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String NO_KEEP_SEEN_ACCESS_MSG = "No keep-seen access to mail folder %1$s";

    private static final String NO_KEEP_SEEN_ACCESS_EXT_MSG = "No keep-seen access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String FOLDER_DOES_NOT_HOLD_FOLDERS_MSG = "Folder %1$s does not allow subfolders.";

    private static final String FOLDER_DOES_NOT_HOLD_FOLDERS_EXT_MSG = "Folder %1$s does not allow subfolders on server %2$s with login %3$s (user=%4$s, context=%5$s).";

    private static final String FOLDER_CREATION_FAILED_MSG = "Mail folder \"%1$s\" could not be created (maybe due to insufficient permission on parent folder %2$s or due to an invalid folder name)";

    private static final String FOLDER_CREATION_FAILED_EXT_MSG = "Mail folder \"%1$s\" could not be created (maybe due to insufficient permission on parent folder %2$s or due to an invalid folder name) on server %3$s with login %4$s (user=%5$s, context=%6$s)";

    private static final String NO_ADMINISTER_ACCESS_ON_INITIAL_MSG = "The composed rights could not be applied to new folder %1$s due to missing administer right in its initial rights specified by IMAP server. However, the folder has been created.";

    private static final String NO_ADMINISTER_ACCESS_ON_INITIAL_EXT_MSG = "The composed rights could not be applied to new folder %1$s due to missing administer right in its initial rights specified by IMAP server. However, the folder has been created on server %2$s with login %3$s (user=%4$s, context=%5$s).";

    private static final String NO_ADMIN_ACL_MSG = "No administer permission specified for folder %1$s";

    private static final String NO_ADMIN_ACL_EXT_MSG = "No administer permission specified for folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String NO_DEFAULT_FOLDER_UPDATE_MSG = "Default folder %1$s must not be updated";

    private static final String NO_DEFAULT_FOLDER_UPDATE_EXT_MSG = "Default folder %1$s must not be updated on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String DELETE_FAILED_MSG = "Deletion of folder %1$s failed";

    private static final String DELETE_FAILED_EXT_MSG = "Deletion of folder %1$s failed on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String NO_DEFAULT_FOLDER_CREATION_MSG = "IMAP default folder %1$s could not be created";

    private static final String NO_DEFAULT_FOLDER_CREATION_EXT_MSG = "IMAP default folder %1$s could not be created on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String MISSING_DEFAULT_FOLDER_NAME_MSG = "Missing default %1$s folder";

    private static final String MISSING_DEFAULT_FOLDER_NAME_EXT_MSG = "Missing default %1$s folder on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String UPDATE_FAILED_MSG = "Update of folder %1$s failed";

    private static final String UPDATE_FAILED_EXT_MSG = "Update of folder %1$s failed on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String NO_FOLDER_DELETE_MSG = "Folder %1$s cannot be deleted";

    private static final String NO_FOLDER_DELETE_EXT_MSG = "Folder %1$s cannot be deleted on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String NO_DEFAULT_FOLDER_DELETE_MSG = "Default folder %1$s cannot be deleted";

    private static final String NO_DEFAULT_FOLDER_DELETE_EXT_MSG = "Default folder %1$s cannot be deleted on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String FLAG_FAILED_MSG = "Flag %1$s could not be changed due to following reason \"%2$s\"";

    private static final String FLAG_FAILED_EXT_MSG = "Flag %1$s could not be changed due to following reason \"%2$s\" on server %3$s with login %4$s (user=%5$s, context=%6$s)";

    private static final String INVALID_SEARCH_PARAMS_MSG = "Number of search fields (%d) do not match number of search patterns (%d)";

    private static final String IMAP_SEARCH_FAILED_MSG = "IMAP search failed due to reason \"%1$s\". Switching to application-based search";

    private static final String IMAP_SEARCH_FAILED_EXT_MSG = "IMAP search failed due to reason \"%1$s\" on server %2$s with login %3$s (user=%4$s, context=%5$s). Switching to application-based search.";

    private static final String IMAP_SORT_FAILED_MSG = "IMAP sort failed due to reason \"%1$s\". Switching to application-based sorting.";

    private static final String IMAP_SORT_FAILED_EXT_MSG = "IMAP sort failed due to reason \"%1$s\" on server %2$s with login %3$s (user=%4$s, context=%5$s). Switching to application-based sorting.";

    private static final String UNKNOWN_SEARCH_FIELD_MSG = "Unknown search field: %1$s";

    private static final String UNKNOWN_SEARCH_FIELD_EXT_MSG = "Unknown search field: %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String NO_MOVE_TO_SUBFLD_MSG = "Mail folder %1$s must not be moved to subsequent folder %2$s";

    private static final String NO_MOVE_TO_SUBFLD_EXT_MSG = "Mail folder %1$s must not be moved to subsequent folder %2$s on server %3$s with login %4$s (user=%5$s, context=%6$s)";

    private static final String MOVE_ON_DELETE_FAILED_MSG = "This message could not be moved to trash folder, possibly because your mailbox is nearly full.\nIn that case, please try to empty your deleted items first, or delete smaller messages first.";

    private static final String MOVE_ON_DELETE_FAILED_EXT_MSG = "This message could not be moved to trash folder on server %1$s with login %2$s (user=%3$s, context=%4$s), possibly because your mailbox is nearly full.\nIn that case, please try to empty your deleted items first, or delete smaller messages first.";

    private static final String MISSING_SOURCE_TARGET_FOLDER_ON_MOVE_MSG = "Missing %1$s folder in mail move operation";

    private static final String MISSING_SOURCE_TARGET_FOLDER_ON_MOVE_EXT_MSG = "Missing %1$s folder in mail move operation on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String NO_EQUAL_MOVE_MSG = "Message move aborted for user %1$s. Source and destination folder are equal to \"%2$s\"";

    private static final String NO_EQUAL_MOVE_EXT_MSG = "Message move aborted for user %1$s. Source and destination folder are equal to \"%2$s\" on server %3$s with login %4$s (user=%5$s, context=%6$s)";

    private static final String FAILED_READ_ONLY_CHECK_MSG = "IMAP folder read-only check failed";

    private static final String FAILED_READ_ONLY_CHECK_EXT_MSG = "Folder read-only check failed on server %1$s with login %2$s (user=%3$s, context=%4$s)";

    private static final String UNKNOWN_FOLDER_MODE_MSG = "Unknown folder open mode %1$s";

    private static final String UNKNOWN_FOLDER_MODE_EXT_MSG = "Unknown folder open mode %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String UID_EXPUNGE_FAILED_MSG = "Message(s) %1$s in folder %2$s could not be deleted due to error \"%3$s\"";

    private static final String UID_EXPUNGE_FAILED_EXT_MSG = "Message(s) %1$s in folder %2$s could not be deleted due to error \"%3$s\" on server %4$s with login %5$s (user=%6$s, context=%7$s)";

    private static final String NO_FOLDER_OPEN_MSG = "Not allowed to open folder %1$s due to missing read access";

    private static final String NO_FOLDER_OPEN_EXT_MSG = "Not allowed to open folder %1$s due to missing read access on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String MESSAGE_CONTENT_ERROR_MSG = "The raw content's input stream of message %1$s in folder %2$s cannot be read";

    private static final String MESSAGE_CONTENT_ERROR_EXT_MSG = "The raw content's input stream of message %1$s in folder %2$s cannot be read on server %3$s with login %4$s (user=%5$s, context=%6$s)";

    private static final String NO_ATTACHMENT_FOUND_MSG = "No attachment was found with id %1$s in message";

    private static final String NO_ATTACHMENT_FOUND_EXT_MSG = "No attachment was found with id %1$s in message on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String FAILED_VERSIT_SAVE_MSG = "Versit object could not be saved";

    private static final String THREAD_SORT_NOT_SUPPORTED_MSG = "No support of capability \"THREAD=REFERENCES\"";

    private static final String THREAD_SORT_NOT_SUPPORTED_EXT_MSG = "No support of capability \"THREAD=REFERENCES\" on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String PROTOCOL_ERROR_MSG = "A protocol exception occurred during execution of IMAP request \"%1$s\".\nError message: %2$s";

    private static final String NO_ROOT_MOVE_MSG = "Mailbox' root folder must not be source or the destination full name of a move operation.";

    private static final String UNSUPPORTED_SORT_FIELD_MSG = "Sort field %1$s is not supported via IMAP SORT command";

    private static final String UNSUPPORTED_SORT_FIELD_EXT_MSG = "Sort field %1$s is not supported via IMAP SORT command on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String MISSING_PERSONAL_NAMESPACE_MSG = "Missing personal namespace";

    private static final String THREAD_SORT_PARSING_ERROR_MSG = "Parsing thread-sort string failed: %1$s.";

    private static final String SQL_ERROR_MSG = "A SQL error occurred: %1$s";

    private static final String RENAME_FAILED_MSG = "Rename of folder \"%1$s\" to \"%2$s\" failed with \"%3$s\".";

    private static final String RENAME_FAILED_EXT_MSG = "Rename of folder \"%1$s\" to \"%2$s\" failed with \"%3$s\" on server %4$s with login %5$s (user=%6$s, context=%7$s).";

    private static final String NO_RENAME_ACCESS_MSG = "No rename access to mail folder %1$s";

    private static final String NO_RENAME_ACCESS_EXT_MSG = "No rename access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String URI_PARSE_FAILED_MSG = "Unable to parse IMAP server URI \"%1$s\".";

    private static final String NO_DEFAULT_FOLDER_UNSUBSCRIBE_MSG = "Default folder %1$s must not be unsubscribed.";

    private static final String NO_DEFAULT_FOLDER_UNSUBSCRIBE_EXT_MSG = "Default folder %1$s must not be unsubscribed on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    private static final String INVALID_MESSAGE_MSG = "IMAP server refuses to import one or more E-Mails.";

    private static final String INVALID_MESSAGE_EXT_MSG = "IMAP server %1$s refuses to import one or more E-Mails with login %2$s (user=%3$s, context=%4$s)";

    private static final String CONNECTION_UNAVAILABLE_MSG = "Currently not possible to establish a new connection to server %1$s with login %2$s. Please try again.";

    // ---------------------------------------------------------------------------------- //

    public static enum IMAPCode {

        /**
         * Missing parameter in mail connection: %1$s
         */
        MISSING_CONNECT_PARAM(MailExceptionCode.MISSING_CONNECT_PARAM, null),
        /**
         * No connection available to access mailbox
         */
        NOT_CONNECTED(NOT_CONNECTED_MSG, Category.CATEGORY_ERROR, 2001, IMAPExceptionMessages.NOT_CONNECTED_MSG),
        /**
         * No connection available to access mailbox on server %1$s with login %2$s (user=%3$s, context=%4$s)
         */
        NOT_CONNECTED_EXT(NOT_CONNECTED_EXT_MSG, NOT_CONNECTED),
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
        NO_MAIL_MODULE_ACCESS(NO_MAIL_MODULE_ACCESS_MSG, Category.CATEGORY_PERMISSION_DENIED, 2003, IMAPExceptionMessages.NO_MAIL_MODULE_ACCESS_MSG),
        /**
         * No access to mail folder %1$s
         */
        NO_ACCESS(NO_ACCESS_MSG, Category.CATEGORY_PERMISSION_DENIED, 2003, IMAPExceptionMessages.NO_ACCESS_MSG),
        /**
         * No access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_ACCESS_EXT(NO_ACCESS_EXT_MSG, NO_ACCESS),
        /**
         * No lookup access to mail folder %1$s
         */
        NO_LOOKUP_ACCESS(NO_LOOKUP_ACCESS_MSG, Category.CATEGORY_PERMISSION_DENIED, 2004, IMAPExceptionMessages.NO_LOOKUP_ACCESS_MSG),
        /**
         * No lookup access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_LOOKUP_ACCESS_EXT(NO_LOOKUP_ACCESS_EXT_MSG, NO_LOOKUP_ACCESS),
        /**
         * No read access on mail folder %1$s
         */
        NO_READ_ACCESS(NO_READ_ACCESS_MSG, Category.CATEGORY_PERMISSION_DENIED, 2005, IMAPExceptionMessages.NO_READ_ACCESS_MSG),
        /**
         * No read access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_READ_ACCESS_EXT(NO_READ_ACCESS_EXT_MSG, NO_READ_ACCESS),
        /**
         * No delete access on mail folder %1$s
         */
        NO_DELETE_ACCESS(NO_DELETE_ACCESS_MSG, Category.CATEGORY_PERMISSION_DENIED, 2006, IMAPExceptionMessages.NO_DELETE_ACCESS_MSG),
        /**
         * No delete access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_DELETE_ACCESS_EXT(NO_DELETE_ACCESS_EXT_MSG, NO_DELETE_ACCESS),
        /**
         * No insert access on mail folder %1$s
         */
        NO_INSERT_ACCESS(NO_INSERT_ACCESS_MSG, Category.CATEGORY_PERMISSION_DENIED, 2007, IMAPExceptionMessages.NO_INSERT_ACCESS_MSG),
        /**
         * No insert access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_INSERT_ACCESS_EXT(NO_INSERT_ACCESS_EXT_MSG, NO_INSERT_ACCESS),
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
        NO_ADMINISTER_ACCESS(NO_ADMINISTER_ACCESS_MSG, Category.CATEGORY_PERMISSION_DENIED, 2009, IMAPExceptionMessages.NO_ADMINISTER_ACCESS_MSG),
        /**
         * No administer access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_ADMINISTER_ACCESS_EXT(NO_ADMINISTER_ACCESS_EXT_MSG, NO_ADMINISTER_ACCESS),
        /**
         * No write access to IMAP folder %1$s
         */
        NO_WRITE_ACCESS(NO_WRITE_ACCESS_MSG, Category.CATEGORY_PERMISSION_DENIED, 2010, IMAPExceptionMessages.NO_WRITE_ACCESS_MSG),
        /**
         * No write access to IMAP folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_WRITE_ACCESS_EXT(NO_WRITE_ACCESS_EXT_MSG, NO_WRITE_ACCESS),
        /**
         * No keep-seen access on mail folder %1$s
         */
        NO_KEEP_SEEN_ACCESS(NO_KEEP_SEEN_ACCESS_MSG, Category.CATEGORY_PERMISSION_DENIED, 2011, IMAPExceptionMessages.NO_KEEP_SEEN_ACCESS_MSG),
        /**
         * No keep-seen access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_KEEP_SEEN_ACCESS_EXT(NO_KEEP_SEEN_ACCESS_EXT_MSG, NO_KEEP_SEEN_ACCESS),
        /**
         * Folder %1$s does not allow subfolders.
         */
        FOLDER_DOES_NOT_HOLD_FOLDERS(FOLDER_DOES_NOT_HOLD_FOLDERS_MSG, Category.CATEGORY_PERMISSION_DENIED, 2012, IMAPExceptionMessages.FOLDER_DOES_NOT_HOLD_FOLDERS_MSG),
        /**
         * Folder %1$s does not allow subfolders on server %2$s with login %3$s (user=%4$s, context=%5$s).
         */
        FOLDER_DOES_NOT_HOLD_FOLDERS_EXT(FOLDER_DOES_NOT_HOLD_FOLDERS_EXT_MSG, FOLDER_DOES_NOT_HOLD_FOLDERS),
        /**
         * Invalid folder name: "%1$s". Please avoid the following characters: %2$s
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
         * Mail folder "%1$s" could not be created (maybe due to insufficient permission on parent folder %2$s or due to an invalid folder
         * name)
         */
        FOLDER_CREATION_FAILED(FOLDER_CREATION_FAILED_MSG, Category.CATEGORY_USER_INPUT, 2015, IMAPExceptionMessages.FOLDER_CREATION_FAILED_MSG),
        /**
         * Mail folder "%1$s" could not be created (maybe due to insufficient permission on parent folder %2$s or due to an invalid folder
         * name) on server %3$s with login %4$s (user=%5$s, context=%6$s)
         */
        FOLDER_CREATION_FAILED_EXT(FOLDER_CREATION_FAILED_EXT_MSG, FOLDER_CREATION_FAILED),
        /**
         * The composed rights could not be applied to new folder %1$s due to missing administer right in its initial rights specified by
         * IMAP server. However, the folder has been created.
         */
        NO_ADMINISTER_ACCESS_ON_INITIAL(NO_ADMINISTER_ACCESS_ON_INITIAL_MSG, Category.CATEGORY_PERMISSION_DENIED, 2016, IMAPExceptionMessages.NO_ADMINISTER_ACCESS_ON_INITIAL_MSG),
        /**
         * The composed rights could not be applied to new folder %1$s due to missing administer right in its initial rights specified by
         * IMAP server. However, the folder has been created on server %2$s with login %3$s (user=%4$s, context=%5$s).
         */
        NO_ADMINISTER_ACCESS_ON_INITIAL_EXT(NO_ADMINISTER_ACCESS_ON_INITIAL_EXT_MSG, NO_ADMINISTER_ACCESS_ON_INITIAL),
        /**
         * No admin permission specified for folder %1$s
         */
        NO_ADMIN_ACL(NO_ADMIN_ACL_MSG, Category.CATEGORY_USER_INPUT, 2017, IMAPExceptionMessages.NO_ADMIN_ACL_MSG),
        /**
         * No admin permission specified for folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_ADMIN_ACL_EXT(NO_ADMIN_ACL_EXT_MSG, NO_ADMIN_ACL),
        /**
         * Default folder %1$s must not be updated
         */
        NO_DEFAULT_FOLDER_UPDATE(NO_DEFAULT_FOLDER_UPDATE_MSG, Category.CATEGORY_PERMISSION_DENIED, 2018, IMAPExceptionMessages.NO_DEFAULT_FOLDER_UPDATE_MSG),
        /**
         * Default folder %1$s must not be updated on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_DEFAULT_FOLDER_UPDATE_EXT(NO_DEFAULT_FOLDER_UPDATE_EXT_MSG, NO_DEFAULT_FOLDER_UPDATE),
        /**
         * Deletion of folder %1$s failed
         */
        DELETE_FAILED(DELETE_FAILED_MSG, Category.CATEGORY_ERROR, 2019, IMAPExceptionMessages.DELETE_FAILED_MSG),
        /**
         * Deletion of folder %1$s failed on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        DELETE_FAILED_EXT(DELETE_FAILED_EXT_MSG, DELETE_FAILED),
        /**
         * IMAP default folder %1$s could not be created
         */
        NO_DEFAULT_FOLDER_CREATION(NO_DEFAULT_FOLDER_CREATION_MSG, Category.CATEGORY_ERROR, 2020, IMAPExceptionMessages.NO_DEFAULT_FOLDER_CREATION_MSG),
        /**
         * IMAP default folder %1$s could not be created on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_DEFAULT_FOLDER_CREATION_EXT(NO_DEFAULT_FOLDER_CREATION_EXT_MSG, NO_DEFAULT_FOLDER_CREATION),
        /**
         * Missing default %1$s folder
         */
        MISSING_DEFAULT_FOLDER_NAME(MISSING_DEFAULT_FOLDER_NAME_MSG, Category.CATEGORY_ERROR, 2021, IMAPExceptionMessages.MISSING_DEFAULT_FOLDER_NAME_MSG),
        /**
         * Missing default %1$s folder on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        MISSING_DEFAULT_FOLDER_NAME_EXT(MISSING_DEFAULT_FOLDER_NAME_EXT_MSG, MISSING_DEFAULT_FOLDER_NAME),
        /**
         * Update of folder %1$s failed
         */
        UPDATE_FAILED(UPDATE_FAILED_MSG, Category.CATEGORY_ERROR, 2022, IMAPExceptionMessages.UPDATE_FAILED_MSG),
        /**
         * Update of folder %1$s failed on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        UPDATE_FAILED_EXT(UPDATE_FAILED_EXT_MSG, UPDATE_FAILED),
        /**
         * Folder %1$s must not be deleted
         */
        NO_FOLDER_DELETE(NO_FOLDER_DELETE_MSG, Category.CATEGORY_PERMISSION_DENIED, 2023, IMAPExceptionMessages.NO_FOLDER_DELETE_MSG),
        /**
         * Folder %1$s must not be deleted on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_FOLDER_DELETE_EXT(NO_FOLDER_DELETE_EXT_MSG, NO_FOLDER_DELETE),
        /**
         * Default folder %1$s must not be deleted
         */
        NO_DEFAULT_FOLDER_DELETE(NO_DEFAULT_FOLDER_DELETE_MSG, Category.CATEGORY_PERMISSION_DENIED, 2024, IMAPExceptionMessages.NO_DEFAULT_FOLDER_DELETE_MSG),
        /**
         * Default folder %1$s must not be deleted on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_DEFAULT_FOLDER_DELETE_EXT(NO_DEFAULT_FOLDER_DELETE_EXT_MSG, NO_DEFAULT_FOLDER_DELETE),
        /**
         * An I/O error occurred: %1$s
         */
        IO_ERROR(MailExceptionCode.IO_ERROR, null),
        /**
         * Flag %1$s could not be changed due to reason "%2$s"
         */
        FLAG_FAILED(FLAG_FAILED_MSG, Category.CATEGORY_ERROR, 2025, IMAPExceptionMessages.FLAG_FAILED_MSG),
        /**
         * Flag %1$s could not be changed due to reason "%2$s" on server %3$s with login %4$s (user=%5$s, context=%6$s)
         */
        FLAG_FAILED_EXT(FLAG_FAILED_EXT_MSG, FLAG_FAILED),
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
        INVALID_SEARCH_PARAMS(INVALID_SEARCH_PARAMS_MSG, Category.CATEGORY_ERROR, 2028, IMAPExceptionMessages.INVALID_SEARCH_PARAMS_MSG),
        /**
         * IMAP search failed due to reason "%1$s". Switching to application-based search
         */
        IMAP_SEARCH_FAILED(IMAP_SEARCH_FAILED_MSG, Category.CATEGORY_SERVICE_DOWN, 2029, IMAPExceptionMessages.IMAP_SEARCH_FAILED_MSG),
        /**
         * IMAP search failed due to reason "%1$s" on server %2$s with login %3$s (user=%4$s, context=%5$s). Switching to application-based
         * search.
         */
        IMAP_SEARCH_FAILED_EXT(IMAP_SEARCH_FAILED_EXT_MSG, IMAP_SEARCH_FAILED),
        /**
         * IMAP sort failed due to reason "%1$s". Switching to application-based sorting.
         */
        IMAP_SORT_FAILED(IMAP_SORT_FAILED_MSG, Category.CATEGORY_SERVICE_DOWN, 2030, IMAPExceptionMessages.IMAP_SORT_FAILED_MSG),
        /**
         * IMAP sort failed due to reason "%1$s" on server %2$s with login %3$s (user=%4$s, context=%5$s). Switching to application-based
         * sorting.
         */
        IMAP_SORT_FAILED_EXT(IMAP_SORT_FAILED_EXT_MSG, IMAP_SORT_FAILED),
        /**
         * Unknown search field: %1$s
         */
        UNKNOWN_SEARCH_FIELD(UNKNOWN_SEARCH_FIELD_MSG, Category.CATEGORY_ERROR, 2031, IMAPExceptionMessages.UNKNOWN_SEARCH_FIELD_MSG),
        /**
         * Unknown search field: %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        UNKNOWN_SEARCH_FIELD_EXT(UNKNOWN_SEARCH_FIELD_EXT_MSG, UNKNOWN_SEARCH_FIELD),
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
        NO_MOVE_TO_SUBFLD(NO_MOVE_TO_SUBFLD_MSG, Category.CATEGORY_PERMISSION_DENIED, 2032, IMAPExceptionMessages.NO_MOVE_TO_SUBFLD_MSG),
        /**
         * Mail folder %1$s must not be moved to subsequent folder %2$s on server %3$s with login %4$s (user=%5$s, context=%6$s)
         */
        NO_MOVE_TO_SUBFLD_EXT(NO_MOVE_TO_SUBFLD_EXT_MSG, NO_MOVE_TO_SUBFLD),
        /**
         * This message could not be moved to trash folder, possibly because your mailbox is nearly full.<br>
         * In that case, please try to empty your deleted items first, or delete smaller messages first.
         */
        MOVE_ON_DELETE_FAILED(MOVE_ON_DELETE_FAILED_MSG, Category.CATEGORY_CAPACITY, 2034, IMAPExceptionMessages.MOVE_ON_DELETE_FAILED_MSG),
        /**
         * This message could not be moved to trash folder on server %1$s with login %2$s (user=%3$s, context=%4$s), possibly because your mailbox is nearly full.<br>
         * In that case, please try to empty your deleted items first, or delete smaller messages first.
         */
        MOVE_ON_DELETE_FAILED_EXT(MOVE_ON_DELETE_FAILED_EXT_MSG, MOVE_ON_DELETE_FAILED),
        /**
         * Missing %1$s folder in mail move operation
         */
        MISSING_SOURCE_TARGET_FOLDER_ON_MOVE(MISSING_SOURCE_TARGET_FOLDER_ON_MOVE_MSG, Category.CATEGORY_ERROR, 2035, IMAPExceptionMessages.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE_MSG),
        /**
         * Missing %1$s folder in mail move operation on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        MISSING_SOURCE_TARGET_FOLDER_ON_MOVE_EXT(MISSING_SOURCE_TARGET_FOLDER_ON_MOVE_EXT_MSG, MISSING_SOURCE_TARGET_FOLDER_ON_MOVE),
        /**
         * Message move aborted for user %1$s. Source and destination folder are equal to "%2$s"
         */
        NO_EQUAL_MOVE(NO_EQUAL_MOVE_MSG, Category.CATEGORY_USER_INPUT, 2036, IMAPExceptionMessages.NO_EQUAL_MOVE_MSG),
        /**
         * Message move aborted for user %1$s. Source and destination folder are equal to "%2$s" on server %3$s with login %4$s (user=%5$s,
         * context=%6$s)
         */
        NO_EQUAL_MOVE_EXT(NO_EQUAL_MOVE_EXT_MSG, NO_EQUAL_MOVE),
        /**
         * Folder read-only check failed
         */
        FAILED_READ_ONLY_CHECK(FAILED_READ_ONLY_CHECK_MSG, Category.CATEGORY_ERROR, 2037, IMAPExceptionMessages.FAILED_READ_ONLY_CHECK_MSG),
        /**
         * Folder read-only check failed on server %1$s with login %2$s (user=%3$s, context=%4$s)
         */
        FAILED_READ_ONLY_CHECK_EXT(FAILED_READ_ONLY_CHECK_EXT_MSG, FAILED_READ_ONLY_CHECK),
        /**
         * Unknown folder open mode %1$s
         */
        UNKNOWN_FOLDER_MODE(UNKNOWN_FOLDER_MODE_MSG, Category.CATEGORY_ERROR, 2038, IMAPExceptionMessages.UNKNOWN_FOLDER_MODE_MSG),
        /**
         * Unknown folder open mode %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        UNKNOWN_FOLDER_MODE_EXT(UNKNOWN_FOLDER_MODE_EXT_MSG, UNKNOWN_FOLDER_MODE),
        /**
         * Message(s) %1$s in folder %2$s could not be deleted due to error "%3$s"
         */
        UID_EXPUNGE_FAILED(UID_EXPUNGE_FAILED_MSG, Category.CATEGORY_ERROR, 2039, IMAPExceptionMessages.UID_EXPUNGE_FAILED_MSG),
        /**
         * Message(s) %1$s in folder %2$s could not be deleted due to error "%3$s" on server %4$s with login %5$s (user=%6$s, context=%7$s)
         */
        UID_EXPUNGE_FAILED_EXT(UID_EXPUNGE_FAILED_EXT_MSG, UID_EXPUNGE_FAILED),
        /**
         * Not allowed to open folder %1$s due to missing read access
         */
        NO_FOLDER_OPEN(NO_FOLDER_OPEN_MSG, Category.CATEGORY_PERMISSION_DENIED, 2041, IMAPExceptionMessages.NO_FOLDER_OPEN_MSG),
        /**
         * Not allowed to open folder %1$s due to missing read access on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_FOLDER_OPEN_EXT(NO_FOLDER_OPEN_EXT_MSG, NO_FOLDER_OPEN),
        /**
         * The raw content's input stream of message %1$s in folder %2$s cannot be read
         */
        MESSAGE_CONTENT_ERROR(MESSAGE_CONTENT_ERROR_MSG, Category.CATEGORY_ERROR, 2042, IMAPExceptionMessages.MESSAGE_CONTENT_ERROR_MSG),
        /**
         * The raw content's input stream of message %1$s in folder %2$s cannot be read on server %3$s with login %4$s (user=%5$s,
         * context=%6$s)
         */
        MESSAGE_CONTENT_ERROR_EXT(MESSAGE_CONTENT_ERROR_EXT_MSG, MESSAGE_CONTENT_ERROR),
        /**
         * No attachment was found with id %1$s in message
         */
        NO_ATTACHMENT_FOUND(NO_ATTACHMENT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 2043, IMAPExceptionMessages.NO_ATTACHMENT_FOUND_MSG),
        /**
         * No attachment was found with id %1$s in message on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_ATTACHMENT_FOUND_EXT(NO_ATTACHMENT_FOUND_EXT_MSG, NO_ATTACHMENT_FOUND),
        /**
         * Versit attachment could not be saved due to an unsupported MIME type: %1$s
         */
        UNSUPPORTED_VERSIT_ATTACHMENT(MailExceptionCode.UNSUPPORTED_VERSIT_ATTACHMENT, null),
        /**
         * Versit object %1$s could not be saved
         */
        FAILED_VERSIT_SAVE(FAILED_VERSIT_SAVE_MSG, Category.CATEGORY_ERROR, 2045, IMAPExceptionMessages.FAILED_VERSIT_SAVE_MSG),
        /**
         * No support of capability "THREAD=REFERENCES"
         */
        THREAD_SORT_NOT_SUPPORTED(THREAD_SORT_NOT_SUPPORTED_MSG, Category.CATEGORY_ERROR, 2046, IMAPExceptionMessages.THREAD_SORT_NOT_SUPPORTED_MSG),
        /**
         * No support of capability "THREAD=REFERENCES" on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        THREAD_SORT_NOT_SUPPORTED_EXT(THREAD_SORT_NOT_SUPPORTED_EXT_MSG, THREAD_SORT_NOT_SUPPORTED),
        /**
         * Unsupported charset-encoding: %1$s
         */
        ENCODING_ERROR(MailExceptionCode.ENCODING_ERROR, null),
        /**
         * A protocol exception occurred during execution of IMAP request "%1$s".<br>
         * Error message: %2$s
         */
        PROTOCOL_ERROR(PROTOCOL_ERROR_MSG, Category.CATEGORY_ERROR, 2047, IMAPExceptionMessages.PROTOCOL_ERROR_MSG),
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
        NO_ROOT_MOVE(NO_ROOT_MOVE_MSG, Category.CATEGORY_ERROR, 2048, IMAPExceptionMessages.NO_ROOT_MOVE_MSG),
        /**
         * Sort field %1$s is not supported via IMAP SORT command
         */
        UNSUPPORTED_SORT_FIELD(UNSUPPORTED_SORT_FIELD_MSG, Category.CATEGORY_ERROR, 2049, IMAPExceptionMessages.UNSUPPORTED_SORT_FIELD_MSG),
        /**
         * Sort field %1$s is not supported via IMAP SORT command on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        UNSUPPORTED_SORT_FIELD_EXT(UNSUPPORTED_SORT_FIELD_EXT_MSG, UNSUPPORTED_SORT_FIELD),
        /**
         * Missing personal namespace
         */
        MISSING_PERSONAL_NAMESPACE(MISSING_PERSONAL_NAMESPACE_MSG, Category.CATEGORY_ERROR, 2050, IMAPExceptionMessages.MISSING_PERSONAL_NAMESPACE_MSG),
        /**
         * Parsing thread-sort string failed: %1$s.
         */
        THREAD_SORT_PARSING_ERROR(THREAD_SORT_PARSING_ERROR_MSG, Category.CATEGORY_ERROR, 2051, IMAPExceptionMessages.THREAD_SORT_PARSING_ERROR_MSG),
        /**
         * A SQL error occurred: %1$s
         */
        SQL_ERROR(SQL_ERROR_MSG, Category.CATEGORY_ERROR, 2052, IMAPExceptionMessages.SQL_ERROR_MSG),
        /**
         * Rename of folder "%1$s" to "%2$s" failed with "%3$s".
         */
        RENAME_FAILED(RENAME_FAILED_MSG, Category.CATEGORY_ERROR, 2053, IMAPExceptionMessages.RENAME_FAILED_MSG),
        /**
         * Rename of folder "%1$s" to "%2$s" failed on server %3$s with login %4$s (user=%5$s, context=%6$s).
         */
        RENAME_FAILED_EXT(RENAME_FAILED_EXT_MSG, RENAME_FAILED),
        /**
         * No rename access to mail folder %1$s
         */
        NO_RENAME_ACCESS(NO_RENAME_ACCESS_MSG, Category.CATEGORY_PERMISSION_DENIED, 2054, IMAPExceptionMessages.NO_RENAME_ACCESS_MSG),
        /**
         * No rename access to mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_RENAME_ACCESS_EXT(NO_RENAME_ACCESS_EXT_MSG, NO_RENAME_ACCESS),
        /**
         * Unable to parse IMAP server URI "%1$s".
         */
        URI_PARSE_FAILED(URI_PARSE_FAILED_MSG, Category.CATEGORY_CONFIGURATION, 2055, IMAPExceptionMessages.URI_PARSE_FAILED_MSG),
        /**
         * Default folder %1$s must not be unsubscribed.
         */
        NO_DEFAULT_FOLDER_UNSUBSCRIBE(NO_DEFAULT_FOLDER_UNSUBSCRIBE_MSG, Category.CATEGORY_USER_INPUT, 2056, IMAPExceptionMessages.NO_DEFAULT_FOLDER_UNSUBSCRIBE_MSG),
        /**
         * Default folder %1$s must not be unsubscribed on server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        NO_DEFAULT_FOLDER_UNSUBSCRIBE_EXT(NO_DEFAULT_FOLDER_UNSUBSCRIBE_EXT_MSG, NO_DEFAULT_FOLDER_UNSUBSCRIBE),
        /**
         * IMAP server refuses to import one or more E-Mails.
         */
        INVALID_MESSAGE(INVALID_MESSAGE_MSG, Category.CATEGORY_USER_INPUT, 2057, IMAPExceptionMessages.INVALID_MESSAGE_MSG),
        /**
         * IMAP server %1$s refuses to import one or more E-Mails with login %2$s (user=%3$s, context=%3$s)
         */
        INVALID_MESSAGE_EXT(INVALID_MESSAGE_EXT_MSG, INVALID_MESSAGE),
        /**
         * Currently not possible to establish a new connection to server %1$s with login %2$s. Please try again.
         */
        CONNECTION_UNAVAILABLE(CONNECTION_UNAVAILABLE_MSG, Category.CATEGORY_TRY_AGAIN, 2058, IMAPExceptionMessages.CONNECTION_UNAVAILABLE_MSG),
        ;

        private final String message;
        private String displayMessage;
        private final IMAPCode extend;
        private final int detailNumber;
        private final Category category;

        private static final String PREFIX = IMAPProvider.PROTOCOL_IMAP.getName().toUpperCase();

        /**
         * Gets the <code>"IMAP"</code> prefix.
         *
         * @return The prefix
         */
        public static String prefix() {
            return PREFIX;
        }

        private IMAPCode(final String message, final Category category, final int detailNumber, final String displayMessage) {
            this.message = message;
            this.displayMessage = displayMessage;
            extend = null;
            this.detailNumber = detailNumber;
            this.category = category;
        }

        private IMAPCode(final String message, final IMAPCode extend) {
            this.message = message;
            this.displayMessage = extend.getDisplayMessage();
            this.extend = extend;
            detailNumber = extend.detailNumber;
            category = extend.category;
        }

        private IMAPCode(final MailExceptionCode code, final IMAPCode extend) {
            message = code.getMessage();
            displayMessage = code.getDisplayMessage();
            this.extend = extend;
            detailNumber = code.getNumber();
            category = code.getCategory();
        }

        private IMAPCode(final MimeMailExceptionCode code, final IMAPCode extend) {
            message = code.getMessage();
            displayMessage = code.getDisplayMessage();
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

        public String getDisplayMessage() {
            return displayMessage;
        }

        private static final Map<IMAPCode, IMAPCode> EXT_MAP;

        static {
            final IMAPCode[] codes = IMAPCode.values();
            EXT_MAP = new EnumMap<IMAPCode, IMAPCode>(IMAPCode.class);
            for (int i = 0; i < codes.length; i++) {
                final IMAPCode code = codes[i];
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
        static IMAPCode getExtendedCode(final IMAPCode code) {
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

        private static final Set<Category.EnumType> DISPLAYABLE = OXExceptionFactory.DISPLAYABLE;

        /**
         * Creates a new {@link OXException} instance pre-filled with specified code's attributes.
         *
         * @param code The exception code
         * @param category The optional category to use
         * @param cause The optional initial cause
         * @param args The message arguments in case of printf-style message
         * @return The newly created {@link OXException} instance
         */
        public OXException create(final Throwable cause, final Object... args) {
            final OXException ret;
            String displayMessage = this.displayMessage;
            if (null != displayMessage) {
                ret = new OXException(getNumber(), displayMessage, cause, args).setLogMessage(getMessage(), args);
            } else {
                if (category.getLogLevel().implies(LogLevel.DEBUG)) {
                    ret = new OXException(getNumber(), getMessage(), cause, args);
                } else {
                    if (DISPLAYABLE.contains(category.getType())) {
                        // Displayed message is equal to logged one
                        ret = new OXException(detailNumber, message, cause, args).setLogMessage(message, args);
                    } else {
                        displayMessage = Category.EnumType.TRY_AGAIN.equals(category.getType()) ? OXExceptionStrings.MESSAGE_RETRY : OXExceptionStrings.MESSAGE;
                        ret = new OXException(detailNumber, displayMessage, cause, new Object[0]).setLogMessage(message, args);
                    }
                }
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
    public static OXException create(final Code code, final IMAPConfig imapConfig, final Session session, final Object... messageArgs) {
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
    public static OXException create(final Code code, final IMAPConfig imapConfig, final Session session, final Throwable cause, final Object... messageArgs) {
        if (IMAPException.Code.NO_ACCESS.equals(code) && messageArgs[0] != null) {
            final String fullName = messageArgs[0].toString();
            if (!MailFolder.DEFAULT_FOLDER_ID.equals(fullName)) {
                ListLsubCache.removeCachedEntry(fullName, imapConfig.getAccountId(), session);
                final IMAPStore imapStore = imapConfig.optImapStore();
                if (null != imapStore) {
                    IMAPCommandsCollection.forceSetSubscribed(imapStore, fullName, false);
                }
            }
        }
        if (null == imapConfig || null == session) {
            return code.create(cause, messageArgs);
        }
        final IMAPCode imapCode = code.getImapCode();
        final IMAPCode extendedCode = IMAPCode.getExtendedCode(imapCode);
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

    /**
     * Handles given instance of {@link MessagingException} derived from IMAP communication and creates an appropriate instance of
     * {@link OXException}
     *
     * @param e The messaging exception
     * @param mailConfig The corresponding mail configuration used to add information like mail server etc.
     * @param session The session providing user information
     * @param accountId The account identifier or <code>-1</code> if unknown
     * @param optProps The optional properties
     * @return An appropriate instance of {@link OXException}
     */
    public static OXException handleMessagingException(final MessagingException e, final MailConfig mailConfig, final Session session, final int accountId, final Map<String, Object> optProps) {
        return handleMessagingException(e, mailConfig, session, null, accountId, optProps);
    }

    /**
     * Handles given instance of {@link MessagingException} derived from IMAP communication and creates an appropriate instance of
     * {@link OXException}
     *
     * @param e The messaging exception
     * @param mailConfig The corresponding mail configuration used to add information like mail server etc.
     * @param session The session providing user information
     * @param folder The optional folder
     * @param accountId The account identifier or <code>-1</code> if unknown
     * @param optProps The optional properties
     * @return An appropriate instance of {@link OXException}
     */
    public static OXException handleMessagingException(final MessagingException e, final MailConfig mailConfig, final Session session, final Folder folder, final int accountId, final Map<String, Object> optProps) {
        // Check for com.sun.mail.iap.ConnectQuotaExceededException
        if (e.getNextException() instanceof ConnectQuotaExceededException) {
            final String server = null == mailConfig ? "<unknown>" : mailConfig.getServer();
            final String login = null == mailConfig ? "<unknown>" : mailConfig.getLogin();
            return IMAPException.create(IMAPException.Code.CONNECTION_UNAVAILABLE, e.getNextException(), server, login);
        }
        // Check for session
        if (null == session) {
            // Delegate to MIME handling
            return MimeMailException.handleMessagingException(e, mailConfig, session, folder);
        }
        final String message = Strings.toLowerCase(e.getMessage());
        if (null != message) {
            // Check for absent folder
            if (message.indexOf("not found") >= 0 || (message.indexOf("mailbox") >= 0 && (message.indexOf("doesn't exist") >= 0 || message.indexOf("does not exist") >= 0))) {
                // Folder not found
                String fullName = getProperty("fullName", optProps);
                if (null == fullName) {
                    fullName = null == folder ? null : folder.getFullName();
                }
                if (null == fullName) {
                    ListLsubCache.clearCache(accountId, session);
                    return MailExceptionCode.FOLDER_NOT_FOUND_SIMPLE.create(e, new Object[0]);
                }
                ListLsubCache.removeCachedEntry(fullName, accountId, session);
                RightsCache.removeCachedRights(fullName, session, accountId);
                UserFlagsCache.removeUserFlags(fullName, session, accountId);
                final EventAdmin eventAdmin = Services.getService(EventAdmin.class);
                if (null != eventAdmin) {
                    final Map<String, Object> ep = new LinkedHashMap<String, Object>(6);
                    ep.put(PushEventConstants.PROPERTY_CONTENT_RELATED, Boolean.FALSE);
                    ep.put(PushEventConstants.PROPERTY_FOLDER, MailFolderUtility.prepareFullname(accountId, fullName));
                    ep.put(PushEventConstants.PROPERTY_CONTEXT, Integer.valueOf(session.getContextId()));
                    ep.put(PushEventConstants.PROPERTY_USER, Integer.valueOf(session.getUserId()));
                    ep.put(PushEventConstants.PROPERTY_SESSION, session);
                    eventAdmin.postEvent(new Event(PushEventConstants.TOPIC, ep));
                }
                return MailExceptionCode.FOLDER_NOT_FOUND.create(e, fullName);
            }
        }
        // Delegate to MIME handling
        return MimeMailException.handleMessagingException(e, mailConfig, session, folder);
    }

    private static <V> V getProperty(final String name, final Map<String, Object> props) {
        if (Strings.isEmpty(name) || null == props) {
            return null;
        }
        try {
            return (V) props.get(name);
        } catch (final ClassCastException e) {
            return null;
        }
    }

    private IMAPException() {
        super();
    }

}
