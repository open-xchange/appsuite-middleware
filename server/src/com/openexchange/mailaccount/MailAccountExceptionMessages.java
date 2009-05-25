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

package com.openexchange.mailaccount;

import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * {@link MailAccountExceptionMessages} - The error messages for mail account exceptions.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum MailAccountExceptionMessages implements OXErrorMessage {

    /**
     * Unknown operation: %1$s.
     */
    UNKNOWN_OPERATION(MailAccountExceptionStrings.UNKNOWN_OPERATION_MSG, Category.CODE_ERROR, 1),
    /**
     * Cannot find mail account with identifier %1$s for user %2$s in context %3$s.
     */
    NOT_FOUND(MailAccountExceptionStrings.NOT_FOUND_MSG, Category.CODE_ERROR, 2),
    /**
     * Found two mail accounts with same identifier %1$s for user %2$s in context %3$s.
     */
    CONFLICT(MailAccountExceptionStrings.CONFLICT_MSG, Category.CODE_ERROR, 3),
    /**
     * A SQL error occurred: %1$s.
     */
    SQL_ERROR(MailAccountExceptionStrings.SQL_ERROR_MSG, Category.CODE_ERROR, 4),
    /**
     * A host could not be resolved: %1$s.
     */
    UNKNOWN_HOST_ERROR(MailAccountExceptionStrings.UNKNOWN_HOST_ERROR_MSG, Category.CODE_ERROR, 5),
    /**
     * Denied deletion of default mail account of user %1$s in context %2$s.
     */
    NO_DEFAULT_DELETE(MailAccountExceptionStrings.NO_DEFAULT_DELETE_MSG, Category.CODE_ERROR, 6),
    /**
     * Denied update of default mail account of user %1$s in context %2$s.
     */
    NO_DEFAULT_UPDATE(MailAccountExceptionStrings.NO_DEFAULT_UPDATE_MSG, Category.CODE_ERROR, 7),
    /**
     * No duplicate default account allowed.
     */
    NO_DUPLICATE_DEFAULT(MailAccountExceptionStrings.NO_DUPLICATE_DEFAULT_MSG, Category.CODE_ERROR, 8),
    /**
     * Password encryption failed.
     */
    PASSWORD_ENCRYPTION_FAILED(MailAccountExceptionStrings.PASSWORD_ENCRYPTION_FAILED_MSG, Category.CODE_ERROR, 9),
    /**
     * Password decryption failed.
     */
    PASSWORD_DECRYPTION_FAILED(MailAccountExceptionStrings.PASSWORD_DECRYPTION_FAILED_MSG, Category.CODE_ERROR, 10),
    /**
     * The Unified INBOX account already exists for user %1$s in context %2$s.
     */
    DUPLICATE_UNIFIED_INBOX_ACCOUNT(MailAccountExceptionStrings.DUPLICATE_UNIFIED_INBOX_ACCOUNT_MSG, Category.CODE_ERROR, 11),
    /**
     * Mail account creation failed.
     */
    CREATION_FAILED(MailAccountExceptionStrings.CREATION_FAILED_MSG, Category.CODE_ERROR, 12),
    /**
     * Mail account validation failed.
     */
    VALIDATION_FAILED(MailAccountExceptionStrings.VALIDATION_FAILED_MSG, Category.CODE_ERROR, 13),
    /**
     * Multiple mail accounts not enabled for user %1$s in context %2$s.
     */
    NOT_ENABLED(MailAccountExceptionStrings.NOT_ENABLED_MSG, Category.USER_CONFIGURATION, 14),
    /**
     * Found two mail accounts with same email address %1$s for user %2$s in context %3$s.
     */
    CONFLICT_ADDR(MailAccountExceptionStrings.CONFLICT_ADDR_MSG, Category.CODE_ERROR, 15);

    /**
     * Message of the exception.
     */
    private final String message;

    /**
     * Category of the exception.
     */
    private final Category category;

    /**
     * Detail number of the exception.
     */
    private final int number;

    /**
     * Default constructor.
     * 
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private MailAccountExceptionMessages(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        number = detailNumber;
    }

    public Category getCategory() {
        return category;
    }

    public int getErrorCode() {
        return number;
    }

    public String getHelp() {
        return null;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Creates a new mail account exception instance with specified message arguments.
     * 
     * @param messageArgs The message arguments.
     * @return A new mail account exception instance with specified message arguments.
     */
    public MailAccountException create(final Object... messageArgs) {
        return MailAccountExceptionFactory.getInstance().create(this, messageArgs);
    }

    /**
     * Creates a new mail account exception instance with specified message arguments. <br>
     * Exception's init cause is set to provided {@link Throwable} instance.
     * 
     * @param cause The init cause.
     * @param messageArgs The message arguments.
     * @return A new mail account exception instance with specified message arguments and init cause.
     */
    public MailAccountException create(final Throwable cause, final Object... messageArgs) {
        return MailAccountExceptionFactory.getInstance().create(this, cause, messageArgs);
    }
}
