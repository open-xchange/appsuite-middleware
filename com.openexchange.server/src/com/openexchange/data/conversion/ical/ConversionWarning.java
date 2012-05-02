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

package com.openexchange.data.conversion.ical;

import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ConversionWarning extends OXException {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -7593693106963732974L;

    private final int index;

    /**
     * @deprecated use {@link #ConversionWarning(Code, Object...)}.
     */
    @Deprecated
    public ConversionWarning(final int index, final String message, final Object... args) {
        super(9999, OXExceptionStrings.MESSAGE, null, new Object[0]);
        addCategory(Category.CATEGORY_ERROR);
        setLogMessage(message, args);
        setPrefix("ICA");
        this.index = index;
    }

    /**
     * @deprecated use {@link #getMessage()}.
     */
    @Deprecated
    public String getFormattedMessage() {
        return getLogMessage();
    }

    public ConversionWarning(final int index, final Code code, final Object... args) {
        this(index, code, null, args);
    }

    public ConversionWarning(final int index, final Code code, final Throwable cause, final Object... args) {
        super(
            code.getNumber(),
            isDisplay(code) ? code.getMessage() : OXExceptionStrings.MESSAGE,
            cause,
            isDisplay(code) ? args : new Object[0]);
        if (!isDisplay(code)) {
            setLogMessage(code.getMessage(), args);
        }
        addCategory(code.getCategory());
        setPrefix(code.getPrefix());
        this.index = index;
    }

    public ConversionWarning(final int index, final OXException cause) {
        super(cause);
        this.index = index;
    }

    private static boolean isDisplay(final Code code) {
        return code.getCategory().getLogLevel().implies(LogLevel.DEBUG);
    }

    public int getIndex() {
        return index;
    }

    public static enum Code implements OXExceptionCode {
        /**
         * Unable to convert task status "%1$s".
         */
        INVALID_STATUS(ConversionWarningMessage.INVALID_STATUS_MSG, Category.CATEGORY_USER_INPUT, 1),
        /**
         * Unable to convert task priority %1$d.
         */
        INVALID_PRIORITY(ConversionWarningMessage.INVALID_PRIORITY_MSG, Category.CATEGORY_USER_INPUT, 2),
        /**
         * Can not create recurrence rule: %s
         */
        CANT_CREATE_RRULE(ConversionWarningMessage.CANT_CREATE_RRULE_MSG, Category.CATEGORY_ERROR, 3),
        /**
         * Invalid session given to implementation "%1$s".
         */
        INVALID_SESSION(ConversionWarningMessage.INVALID_SESSION_MSG, Category.CATEGORY_ERROR, 4),
        /**
         * Can't generate uid.
         */
        CANT_GENERATE_UID(ConversionWarningMessage.CANT_GENERATE_UID_MSG, Category.CATEGORY_ERROR, 5),
        /**
         * Problem writing to stream.
         */
        WRITE_PROBLEM(ConversionWarningMessage.WRITE_PROBLEM_MSG, Category.CATEGORY_CONNECTIVITY, 6),
        /**
         * Validation of calendar failed.
         */
        VALIDATION(ConversionWarningMessage.VALIDATION_MSG, Category.CATEGORY_ERROR, 7),
        /**
         * Can not resolve user: %d
         */
        CANT_RESOLVE_USER(ConversionWarningMessage.CANT_RESOLVE_USER_MSG, Category.CATEGORY_ERROR, 8),
        /**
         * Parsing error parsing ical: %s
         */
        PARSE_EXCEPTION(ConversionWarningMessage.PARSE_EXCEPTION_MSG, Category.CATEGORY_USER_INPUT, 9),
        /**
         * Unknown Class: %1$s
         */
        UNKNOWN_CLASS(ConversionWarningMessage.UNKNOWN_CLASS_MSG, Category.CATEGORY_USER_INPUT, 10),
        /**
         * Cowardly refusing to convert confidential classified objects.
         */
        CLASS_CONFIDENTIAL(ConversionWarningMessage.CLASS_CONFIDENTIAL_MSG, Category.CATEGORY_USER_INPUT, 11),
        /**
         * Missing DTStart in appointment
         */
        MISSING_DTSTART(ConversionWarningMessage.MISSING_DTSTART_MSG, Category.CATEGORY_USER_INPUT, 12),
        /**
         * Can not resolve resource: %d
         */
        CANT_RESOLVE_RESOURCE(ConversionWarningMessage.CANT_RESOLVE_RESOURCE_MSG, Category.CATEGORY_ERROR, 13),
        /**
         * Private Appointments can not have attendees. Removing attendees and accepting appointment anyway.
         */
        PRIVATE_APPOINTMENTS_HAVE_NO_PARTICIPANTS(ConversionWarningMessage.PRIVATE_APPOINTMENTS_HAVE_NO_PARTICIPANTS_MSG, Category.CATEGORY_USER_INPUT, 14),
        /**
         * Not supported recurrence pattern: BYMONTH
         */
        BYMONTH_NOT_SUPPORTED(ConversionWarningMessage.BYMONTH_NOT_SUPPORTED_MSG, Category.CATEGORY_USER_INPUT, 15),
        /**
         * This does not look like an iCal file. Please check the file.
         */
        DOES_NOT_LOOK_LIKE_ICAL_FILE(ConversionWarningMessage.DOES_NOT_LOOK_LIKE_ICAL_FILE_MSG, Category.CATEGORY_USER_INPUT, 16),
        /**
         * Empty "CLASS" element.
         */
        EMPTY_CLASS(ConversionWarningMessage.EMPTY_CLASS_MSG, Category.CATEGORY_USER_INPUT, 17),
        /**
         * Insufficient information for parsing/writing this element.
         */
        INSUFFICIENT_INFORMATION(ConversionWarningMessage.INSUFFICIENT_INFORMATION_MSG, Category.CATEGORY_ERROR, 18),
        /**
         * An error occurred: %1$s
         */
        UNEXPECTED_ERROR(ConversionWarningMessage.UNEXPECTED_ERROR_MSG, Category.CATEGORY_ERROR, 19),

        ;

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
         * @param number detail number.
         */
        private Code(final String message, final Category category, final int number) {
            this.message = message;
            this.category = category;
            this.number = number;
        }

        @Override
        public String getPrefix() {
            return "ICA";
        }

        /**
         * @return the message
         */
        @Override
        public String getMessage() {
            return message;
        }

        /**
         * @return the category
         */
        @Override
        public Category getCategory() {
            return category;
        }

        /**
         * @return the number
         */
        @Override
        public int getNumber() {
            return number;
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
}
