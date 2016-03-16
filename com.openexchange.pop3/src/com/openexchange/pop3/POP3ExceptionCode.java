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

package com.openexchange.pop3;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.mime.MimeMailExceptionCode;

/**
 * {@link DisplayableOXExceptionCode} - The POP3 error codes.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum POP3ExceptionCode implements DisplayableOXExceptionCode {

    /**
     * Missing parameter in mail connection: %1$s
     */
    MISSING_CONNECT_PARAM(MailExceptionCode.MISSING_CONNECT_PARAM),
    /**
     * No connection available to access mailbox
     */
    NOT_CONNECTED("No connection available to access mailbox", POP3ExceptionMessage.NOT_CONNECTED_MSG, CATEGORY_ERROR, 2001),
    /**
     * Missing parameter %1$s
     */
    MISSING_PARAMETER(MailExceptionCode.MISSING_PARAMETER),
    /**
     * A JSON error occurred: %1$s
     */
    JSON_ERROR(MailExceptionCode.JSON_ERROR),
    /**
     * Invalid permission values: fp=%d orp=%d owp=%d odp=%d
     */
    INVALID_PERMISSION(MailExceptionCode.INVALID_PERMISSION),
    /**
     * User %1$s has no mail module access due to user configuration
     */
    NO_MAIL_MODULE_ACCESS("User %1$s has no mail module access due to user configuration", POP3ExceptionMessage.NO_MAIL_MODULE_ACCESS_MSG, CATEGORY_PERMISSION_DENIED, 2003),
    /**
     * No access to mail folder %1$s
     */
    NO_ACCESS("No access to mail folder %1$s", POP3ExceptionMessage.NO_ACCESS_MSG, CATEGORY_PERMISSION_DENIED, 2003),
    /**
     * No read access on mail folder %1$s
     */
    NO_READ_ACCESS("No read access on mail folder %1$s", POP3ExceptionMessage.NO_READ_ACCESS_MSG, CATEGORY_PERMISSION_DENIED, 2005),
    /**
     * No delete access on mail folder %1$s
     */
    NO_DELETE_ACCESS("No delete access on mail folder %1$s", POP3ExceptionMessage.NO_DELETE_ACCESS_MSG, CATEGORY_PERMISSION_DENIED, 2006),
    /**
     * No insert access on mail folder %1$s
     */
    NO_INSERT_ACCESS("No insert access on mail folder %1$s", POP3ExceptionMessage.NO_INSERT_ACCESS_MSG, CATEGORY_PERMISSION_DENIED, 2007),
    /**
     * No create access on mail folder %1$s
     */
    NO_CREATE_ACCESS(MailExceptionCode.NO_CREATE_ACCESS),
    /**
     * No administer access on mail folder %1$s
     */
    NO_ADMINISTER_ACCESS("No administer access on mail folder %1$s", POP3ExceptionMessage.NO_ADMINISTER_ACCESS_MSG, CATEGORY_PERMISSION_DENIED, 2009),
    /**
     * No write access to POP3 folder %1$s
     */
    NO_WRITE_ACCESS("No write access to POP3 folder %1$s", POP3ExceptionMessage.NO_WRITE_ACCESS_MSG, CATEGORY_PERMISSION_DENIED, 2010),
    /**
     * No keep-seen access on mail folder %1$s
     */
    NO_KEEP_SEEN_ACCESS("No keep-seen access on mail folder %1$s", POP3ExceptionMessage.NO_KEEP_SEEN_ACCESS_MSG, CATEGORY_PERMISSION_DENIED, 2011),
    /**
     * Folder %1$s does not allow subfolders.
     */
    FOLDER_DOES_NOT_HOLD_FOLDERS("Folder %1$s does not allow subfolders.", POP3ExceptionMessage.FOLDER_DOES_NOT_HOLD_FOLDERS_MSG, CATEGORY_PERMISSION_DENIED, 2012),
    /**
     * Invalid folder name: "%1$s". Please avoid the following characters: %2$s
     */
    INVALID_FOLDER_NAME(MailExceptionCode.INVALID_FOLDER_NAME),
    /**
     * A folder named %1$s already exists
     */
    DUPLICATE_FOLDER(MailExceptionCode.DUPLICATE_FOLDER),
    /**
     * POP3 does not support mail folder creation
     */
    FOLDER_CREATION_FAILED("POP3 does not support mail folder creation", POP3ExceptionMessage.FOLDER_CREATION_FAILED_MSG, CATEGORY_ERROR, 2015),
    /**
     * The composed rights could not be applied to new folder %1$s due to missing administer right in its initial rights specified by
     * POP3 server. However, the folder has been created.
     */
    NO_ADMINISTER_ACCESS_ON_INITIAL("Missing initial administer right therefore the composed rights could not be applied. However, the folder has been created.", POP3ExceptionMessage.NO_ADMINISTER_ACCESS_ON_INITIAL_MSG, CATEGORY_PERMISSION_DENIED, 2016),
    /**
     * No admin permission specified for folder %1$s
     */
    NO_ADMIN_ACL("No admin permission specified for folder %1$s", POP3ExceptionMessage.NO_ADMIN_ACL_MSG, CATEGORY_USER_INPUT, 2017),
    /**
     * Default folder %1$s can not be updated
     */
    NO_DEFAULT_FOLDER_UPDATE("Default folder %1$s can not be updated", POP3ExceptionMessage.NO_DEFAULT_FOLDER_UPDATE_MSG, CATEGORY_PERMISSION_DENIED, 2018),
    /**
     * Deletion of folder %1$s failed
     */
    DELETE_FAILED("Deletion of folder %1$s failed", POP3ExceptionMessage.DELETE_FAILED_MSG, CATEGORY_ERROR, 2019),
    /**
     * POP3 default folder %1$s could not be created
     */
    NO_DEFAULT_FOLDER_CREATION("POP3 default folder %1$s could not be created", POP3ExceptionMessage.NO_DEFAULT_FOLDER_CREATION_MSG, CATEGORY_ERROR, 2020),
    /**
     * Missing default %1$s folder in user mail settings
     */
    MISSING_DEFAULT_FOLDER_NAME("Missing default %1$s folder in user mail settings", POP3ExceptionMessage.MISSING_DEFAULT_FOLDER_NAME_MSG, CATEGORY_ERROR, 2021),
    /**
     * Update of folder %1$s failed
     */
    UPDATE_FAILED("Update of folder %1$s failed", POP3ExceptionMessage.UPDATE_FAILED_MSG, CATEGORY_ERROR, 2022),
    /**
     * Folder %1$s cannot not be deleted
     */
    NO_FOLDER_DELETE("Folder %1$s cannot not be deleted", POP3ExceptionMessage.NO_FOLDER_DELETE_MSG, CATEGORY_PERMISSION_DENIED, 2023),
    /**
     * Default folder %1$s cannot not be deleted
     */
    NO_DEFAULT_FOLDER_DELETE("Default folder %1$s cannot not be deleted", POP3ExceptionMessage.NO_DEFAULT_FOLDER_DELETE_MSG, CATEGORY_PERMISSION_DENIED, 2024),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR(MailExceptionCode.IO_ERROR),
    /**
     * Flag %1$s could not be changed due to following reason: %2$s
     */
    FLAG_FAILED("Flag %1$s could not be changed due to following reason: %2$s", POP3ExceptionMessage.FLAG_FAILED_MSG, CATEGORY_ERROR, 2025),
    /**
     * Folder %1$s does not hold messages and is therefore not selectable
     */
    FOLDER_DOES_NOT_HOLD_MESSAGES(MailExceptionCode.FOLDER_DOES_NOT_HOLD_MESSAGES),
    /**
     * Number of search fields (%d) do not match number of search patterns (%d)
     */
    INVALID_SEARCH_PARAMS("Number of search fields (%d) do not match number of search patterns (%d)", POP3ExceptionMessage.INVALID_SEARCH_PARAMS_MSG, CATEGORY_ERROR, 2028),
    /**
     * POP3 search failed due to following reason: %1$s. Switching to application-based search
     */
    POP3_SEARCH_FAILED("POP3 search failed due to following reason: %1$s. Switching to application-based search", POP3ExceptionMessage.POP3_SEARCH_FAILED_MSG, CATEGORY_SERVICE_DOWN, 2029),
    /**
     * POP3 sort failed due to following reason: %1$s Switching to application-based sorting
     */
    POP3_SORT_FAILED("POP3 sort failed due to following reason: %1$s Switching to application-based sorting", POP3ExceptionMessage.POP3_SORT_FAILED_MSG, CATEGORY_SERVICE_DOWN, 2030),
    /**
     * Unknown search field: %1$s
     */
    UNKNOWN_SEARCH_FIELD("Unknown search field: %1$s", POP3ExceptionMessage.UNKNOWN_SEARCH_FIELD_MSG, CATEGORY_ERROR, 2031),
    /**
     * Message field %1$s cannot be handled
     */
    INVALID_FIELD(MailExceptionCode.INVALID_FIELD),
    /**
     * Mail folder %1$s must not be moved to subsequent folder %2$s
     */
    NO_MOVE_TO_SUBFLD("Mail folder %1$s must not be moved to subsequent folder %2$s", POP3ExceptionMessage.NO_MOVE_TO_SUBFLD_MSG, CATEGORY_PERMISSION_DENIED, 2032),
    /**
     * This message could not be moved to trash folder, possibly because your mailbox is nearly full.<br>
     * In that case, please try to empty your deleted items first, or delete smaller messages first.
     */
    MOVE_ON_DELETE_FAILED("Cannot move to trash, cause mailbox full. Empty trash or delete messages and then try again.", POP3ExceptionMessage.MOVE_ON_DELETE_FAILED_MSG, CATEGORY_CAPACITY, 2034),
    /**
     * Missing %1$s folder in mail move operation
     */
    MISSING_SOURCE_TARGET_FOLDER_ON_MOVE("Missing %1$s folder in mail move operation", POP3ExceptionMessage.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE_MSG, CATEGORY_ERROR, 2035),
    /**
     * Message move aborted for user %1$s. Source and destination folder are equal: %2$s
     */
    NO_EQUAL_MOVE("Message move aborted for user %1$s. Source and destination folder are equal: %2$s", POP3ExceptionMessage.NO_EQUAL_MOVE_MSG, CATEGORY_USER_INPUT, 2036),
    /**
     * Folder read-only check failed
     */
    FAILED_READ_ONLY_CHECK("Folder read-only check failed", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 2037),
    /**
     * Unknown folder open mode %d
     */
    UNKNOWN_FOLDER_MODE("Unknown folder open mode %d", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 2038),
    /**
     * Message(s) %1$s in folder %2$s could not be deleted due to following error: %3$s
     */
    UID_EXPUNGE_FAILED("Message(s) %1$s in folder %2$s could not be deleted due to following error: %3$s", POP3ExceptionMessage.UID_EXPUNGE_FAILED_MSG, CATEGORY_ERROR, 2039),
    /**
     * Not allowed to open folder %1$s due to missing read access
     */
    NO_FOLDER_OPEN("Not allowed to open folder %1$s due to missing read access", POP3ExceptionMessage.NO_FOLDER_OPEN_MSG, CATEGORY_PERMISSION_DENIED, 2041),
    /**
     * The raw content's input stream of message %1$s in folder %2$s cannot be read
     */
    MESSAGE_CONTENT_ERROR("The raw content's input stream of message %1$s in folder %2$s cannot be read", POP3ExceptionMessage.MESSAGE_CONTENT_ERROR_MSG, CATEGORY_ERROR, 2042),
    /**
     * No attachment was found with id %1$s in message
     */
    NO_ATTACHMENT_FOUND("No attachment was found with id %1$s in message", POP3ExceptionMessage.NO_ATTACHMENT_FOUND_MSG, CATEGORY_USER_INPUT, 2043),
    /**
     * Versit attachment could not be saved due to an unsupported MIME type: %1$s
     */
    UNSUPPORTED_VERSIT_ATTACHMENT(MailExceptionCode.UNSUPPORTED_VERSIT_ATTACHMENT),
    /**
     * Versit object %1$s could not be saved
     */
    FAILED_VERSIT_SAVE("Versit object %1$s could not be saved", POP3ExceptionMessage.FAILED_VERSIT_SAVE_MSG, CATEGORY_ERROR, 2045),
    /**
     * POP3 server does not support capability "THREAD=REFERENCES"
     */
    THREAD_SORT_NOT_SUPPORTED("POP3 server does not support capability \"THREAD=REFERENCES\"", POP3ExceptionMessage.THREAD_SORT_NOT_SUPPORTED_MSG, CATEGORY_ERROR, 2046),
    /**
     * Unsupported charset-encoding: %1$s
     */
    ENCODING_ERROR(MailExceptionCode.ENCODING_ERROR),
    /**
     * A protocol exception occurred during execution of an POP3 request: %1$s
     */
    PROTOCOL_ERROR("A protocol exception occurred during execution of an POP3 request: %1$s", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 2047),
    /**
     * Mail folder could not be found: %1$s.
     */
    FOLDER_NOT_FOUND(MimeMailExceptionCode.FOLDER_NOT_FOUND),
    /**
     * An attempt was made to open a read-only folder with read-write: %1$s
     */
    READ_ONLY_FOLDER(MimeMailExceptionCode.READ_ONLY_FOLDER),
    /**
     * Connection was refused or timed out while attempting to connect to remote server %1$s for user %2$s.
     */
    CONNECT_ERROR(MimeMailExceptionCode.CONNECT_ERROR),
    /**
     * POP3 does not support to move folders.
     */
    MOVE_DENIED("POP3 does not support to move folders.", POP3ExceptionMessage.MOVE_DENIED_MSG, CATEGORY_ERROR, 2048),
    /**
     * Sort field %1$s is not supported via POP3 SORT command
     */
    UNSUPPORTED_SORT_FIELD("Sort field %1$s is not supported via POP3 SORT command", POP3ExceptionMessage.UNSUPPORTED_SORT_FIELD_MSG, CATEGORY_ERROR, 2049),
    /**
     * Missing personal namespace
     */
    MISSING_PERSONAL_NAMESPACE("Missing personal namespace", POP3ExceptionMessage.MISSING_PERSONAL_NAMESPACE_MSG, CATEGORY_ERROR, 2050),
    /**
     * Parsing thread-sort string failed: %1$s.
     */
    THREAD_SORT_PARSING_ERROR("Parsing thread-sort string failed: %1$s.", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 2051),
    /**
     * POP3 does not support to create folders.
     */
    CREATE_DENIED("POP3 does not support to create folders.", POP3ExceptionMessage.CREATE_DENIED_MSG, CATEGORY_ERROR, 2052),
    /**
     * POP3 does not support to delete folders.
     */
    DELETE_DENIED("POP3 does not support to delete folders.", POP3ExceptionMessage.DELETE_DENIED_MSG, CATEGORY_ERROR, 2053),
    /**
     * POP3 does not support to update folders.
     */
    UPDATE_DENIED("POP3 does not support to update folders.", POP3ExceptionMessage.UPDATE_DENIED_MSG, CATEGORY_ERROR, 2054),
    /**
     * A SQL error occurred: %1$s.
     */
    SQL_ERROR("A SQL error occurred: %1$s.", OXExceptionStrings.SQL_ERROR_MSG, CATEGORY_ERROR, 2055),
    /**
     * POP3 does not support to move messages.
     */
    MOVE_MSGS_DENIED("POP3 does not support to move messages.", POP3ExceptionMessage.MOVE_MSGS_DENIED_MSG, CATEGORY_ERROR, 2056),
    /**
     * POP3 does not support to copy messages.
     */
    COPY_MSGS_DENIED("POP3 does not support to copy messages.", POP3ExceptionMessage.COPY_MSGS_DENIED_MSG, CATEGORY_ERROR, 2057),
    /**
     * POP3 does not support draft messages.
     */
    DRAFTS_NOT_SUPPORTED("POP3 does not support draft messages.", POP3ExceptionMessage.DRAFTS_NOT_SUPPORTED_MSG, CATEGORY_ERROR, 2059),
    /**
     * Missing POP3 storage name for user %1$s in context %2$s.
     */
    MISSING_POP3_STORAGE_NAME("Missing POP3 storage name for user %1$s in context %2$s.", POP3ExceptionMessage.MISSING_POP3_STORAGE_NAME_MSG, CATEGORY_ERROR, 2060),
    /**
     * Missing POP3 storage for user %1$s in context %2$s.
     */
    MISSING_POP3_STORAGE("Missing POP3 storage name for user %1$s in context %2$s.", POP3ExceptionMessage.MISSING_POP3_STORAGE_MSG, CATEGORY_ERROR, 2061),
    /**
     * POP3 default folder %1$s must not be moved.
     */
    NO_DEFAULT_FOLDER_MOVE("POP3 default folder %1$s must not be moved.", POP3ExceptionMessage.NO_DEFAULT_FOLDER_MOVE_MSG, CATEGORY_ERROR, 2062),
    /**
     * POP3 default folder %1$s must not be renamed.
     */
    NO_DEFAULT_FOLDER_RENAME("POP3 default folder %1$s must not be renamed.", POP3ExceptionMessage.NO_DEFAULT_FOLDER_RENAME_MSG, CATEGORY_ERROR, 2063),
    /**
     * Inconsistency detected in UIDL map.
     */
    UIDL_INCONSISTENCY("Inconsistency detected in UIDL map.", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 2064),
    /**
     * Missing POP3 storage path for user %1$s in context %2$s.
     */
    MISSING_PATH("Missing POP3 storage path for user %1$s in context %2$s.", POP3ExceptionMessage.MISSING_PATH_MSG, CATEGORY_ERROR, 2065),
    /**
     * Illegal move operation.
     */
    MOVE_ILLEGAL("Illegal move operation.", POP3ExceptionMessage.MOVE_ILLEGAL_MSG, CATEGORY_USER_INPUT, 2066),
    /**
     * Login delay denies connecting to server %1$s with login %2$s (user=%3$s, context=%4$s).<br>
     * Error message from server: %5$s
     */
    LOGIN_DELAY("Login delay denies connecting to server %1$s with login %2$s (user=%3$s, context=%4$s).\nError message from server: %5$s", POP3ExceptionMessage.LOGIN_DELAY_MSG, CATEGORY_SERVICE_DOWN, 2067),
    /**
     * Login delay denies connecting to server %1$s with login %2$s (user=%3$s, context=%4$s). Try again in %5$s seconds.<br>
     * Error message from server: %6$s
     */
    LOGIN_DELAY2("Login delay denies connecting to server %1$s with login %2$s (user=%3$s, context=%4$s). Try again in %5$s seconds.\nError message from server: %6$s", POP3ExceptionMessage.LOGIN_DELAY2_MSG, LOGIN_DELAY.category, LOGIN_DELAY.detailNumber),
    /**
     * Missing required capability %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s).
     */
    MISSING_REQUIRED_CAPABILITY("Missing required capability %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s).", OXExceptionStrings.MESSAGE, CATEGORY_CONFIGURATION, 2068),
    /**
     * POP3 storage path "%1$s" cannot be created for user %2$s in context %3$s.
     */
    ILLEGAL_PATH("POP3 storage path \"%1$s\" cannot be created for user %2$s in context %3$s.", POP3ExceptionMessage.ILLEGAL_PATH_MSG, CATEGORY_ERROR, 2069),
    /**
     * Due to missing required capability %1$s POP3 messages are fetched and removed (expunge-on-quit) from server %2$s with login %3$s
     * (user=%4$s, context=%5$s).
     */
    EXPUNGE_MODE_ONLY("Due to missing required capability %1$s POP3 messages are fetched and removed (expunge-on-quit) from server %2$s with login %3$s (user=%4$s, context=%5$s).", OXExceptionStrings.MESSAGE, CATEGORY_CONFIGURATION, 2070),
    /**
     * Validation of POP3 credentials is disabled due to possible login restrictions by provider. Otherwise subsequent login attempt might not work.
     */
    VALIDATE_DENIED("Validation of POP3 credentials is disabled due to possible login restrictions by provider. Otherwise subsequent login attempt might not work.", POP3ExceptionMessage.VALIDATE_DENIED_MSG, CATEGORY_WARNING, 2071),
    /**
     * POP3 messages cannot be imported because of existing quota constraints on primary mail account. Please free some space.
     */
    QUOTA_CONSTRAINT("POP3 messages cannot be imported because of existing quota constraints on primary mail account. Please free some space.", POP3ExceptionMessage.QUOTA_CONSTRAINT_MSG, CATEGORY_USER_INPUT, 2072),
    ;

    private final String message;

    private final int detailNumber;

    private final Category category;

    private final String prefix;

    private final String displayMessage;

    private POP3ExceptionCode(final String message, final String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        prefix = POP3Provider.PROTOCOL_POP3.getName();
        this.displayMessage = displayMessage;
    }

    private POP3ExceptionCode(final MailExceptionCode code) {
        message = code.getMessage();
        detailNumber = code.getNumber();
        category = code.getCategory();
        prefix = code.getPrefix();
        displayMessage = code.getDisplayMessage();
    }

    private POP3ExceptionCode(final MimeMailExceptionCode code) {
        message = code.getMessage();
        detailNumber = code.getNumber();
        category = code.getCategory();
        prefix = code.getPrefix();
        displayMessage = code.getDisplayMessage();
    }


    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public String getMessage() {
        return message;
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

    /* (non-Javadoc)
     * @see com.openexchange.exception.DisplayableOXExceptionCode#getDisplayMessage()
     */
    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }
}
