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

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.mime.MimeMailExceptionCode;

/**
 * {@link UnifiedInboxException} - Indicates a Unified Mail error.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxException extends OXException {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -8226676160145457046L;

    public static enum Code implements DisplayableOXExceptionCode {

        /**
         * Unified Mail does not support to create folders.
         */
        CREATE_DENIED("Unified Mail does not support to create folders.", UnifiedInboxExceptionMessage.CREATE_DENIED_MSG, Category.CATEGORY_ERROR, 2001),
        /**
         * Unified Mail does not support to delete folders.
         */
        DELETE_DENIED("Unified Mail does not support to delete folders.", UnifiedInboxExceptionMessage.DELETE_DENIED_MSG, Category.CATEGORY_ERROR, 2002),
        /**
         * Unified Mail does not support to update folders.
         */
        UPDATE_DENIED("Unified Mail does not support to update folders.", UnifiedInboxExceptionMessage.UPDATE_DENIED_MSG, Category.CATEGORY_ERROR, 2003),
        /**
         * Unified Mail does not support to move messages.
         */
        MOVE_MSGS_DENIED("Unified Mail does not support to move messages.", UnifiedInboxExceptionMessage.MOVE_MSGS_DENIED_MSG, Category.CATEGORY_ERROR, 2004),
        /**
         * Unified Mail does not support to copy messages.
         */
        COPY_MSGS_DENIED("Unified Mail does not support to copy messages.", UnifiedInboxExceptionMessage.COPY_MSGS_DENIED_MSG, Category.CATEGORY_ERROR, 2005),
        /**
         * Append messages failed.
         */
        APPEND_MSGS_DENIED("Append messages failed.", UnifiedInboxExceptionMessage.APPEND_MSGS_DENIED_MSG, Category.CATEGORY_ERROR, 2006),
        /**
         * Unified Mail does not support draft messages.
         */
        DRAFTS_NOT_SUPPORTED("Unified Mail does not support draft messages.", UnifiedInboxExceptionMessage.DRAFTS_NOT_SUPPORTED_MSG, Category.CATEGORY_ERROR, 2007),
        /**
         * Unified Mail does not support to move folders.
         */
        MOVE_DENIED("Unified Mail does not support to move folders.", UnifiedInboxExceptionMessage.MOVE_DENIED_MSG, Category.CATEGORY_ERROR, 2008),
        /**
         * Unified Mail does not support mail folder creation
         */
        FOLDER_CREATION_FAILED("Unified Mail does not support mail folder creation", UnifiedInboxExceptionMessage.FOLDER_CREATION_FAILED_MSG, Category.CATEGORY_ERROR, 2009),
        /**
         * Unified Mail does not support to clear INBOX folder.
         */
        CLEAR_NOT_SUPPORTED("Unified Mail does not support to clear INBOX folder.", UnifiedInboxExceptionMessage.CLEAR_NOT_SUPPORTED_MSG, Category.CATEGORY_ERROR, 2010),
        /**
         * No connection available to access mailbox
         */
        NOT_CONNECTED("No connection available to access mailbox", UnifiedInboxExceptionMessage.NOT_CONNECTED_MSG, Category.CATEGORY_ERROR, 2011),
        /**
         * Folder %1$s does not hold messages and is therefore not selectable.
         */
        FOLDER_DOES_NOT_HOLD_MESSAGES(MailExceptionCode.FOLDER_DOES_NOT_HOLD_MESSAGES),
        /**
         * Mail folder could not be found: %1$s.
         */
        FOLDER_NOT_FOUND(MimeMailExceptionCode.FOLDER_NOT_FOUND),
        /**
         * Unknown default folder fullname: %1$s.
         */
        UNKNOWN_DEFAULT_FOLDER_INDEX("Unknown default folder fullname: %1$s.", UnifiedInboxExceptionMessage.UNKNOWN_DEFAULT_FOLDER_INDEX_MSG, Category.CATEGORY_ERROR, 2012),
        /**
         * Move operation aborted. Source and destination folder are equal.
         */
        NO_EQUAL_MOVE("Move operation aborted. Source and destination folder are equal.", UnifiedInboxExceptionMessage.NO_EQUAL_MOVE_MSG, Category.CATEGORY_ERROR, 2013),
        /**
         * Request aborted due to timeout of %1$s %2$s.
         */
        TIMEOUT("Request aborted due to timeout of %1$s %2$s.", UnifiedInboxExceptionMessage.TIMEOUT_MSG, Category.CATEGORY_ERROR, 2014),
        /**
         * Invalid destination folder. Don't know where to append the mails.
         */
        INVALID_DESTINATION_FOLDER("Invalid destination folder. Don't know where to append the mails.", UnifiedInboxExceptionMessage.INVALID_DESTINATION_FOLDER_MSG, Category.CATEGORY_USER_INPUT, 2015),
        ;

        private final String message;

        private final int detailNumber;

        private final Category category;
        
        private final String displayMessage;

        private final String prefix;

        private Code(final String message, final String displayMessage, final Category category, final int detailNumber) {
            this.message = message;
            this.detailNumber = detailNumber;
            this.category = category;
            this.displayMessage = displayMessage;
            prefix = UnifiedInboxProvider.PROTOCOL_UNIFIED_INBOX.getName();
        }

        private Code(final MailExceptionCode code) {
            message = code.getMessage();
            detailNumber = code.getNumber();
            category = code.getCategory();
            prefix = code.getPrefix();
            displayMessage = code.getDisplayMessage();
        }

        private Code(final MimeMailExceptionCode code) {
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

    /**
     * Gets the message corresponding to specified error code with given message arguments applied.
     *
     * @param code The code
     * @param msgArgs The message arguments
     * @return The message corresponding to specified error code with given message arguments applied
     */
    public static String getFormattedMessage(final Code code, final Object... msgArgs) {
        return String.format(code.getMessage(), msgArgs);
    }

    /**
     * Initializes a new {@link UnifiedInboxException}
     */
    private UnifiedInboxException() {
        super();
    }

}
