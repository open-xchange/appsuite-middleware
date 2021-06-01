/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.data.conversion.ical;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
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

    /**
     * Creates a conversion warning in case number of elements has been truncated
     *
     * @param The item's index position
     * @param limit The configured limit
     * @return The conversion warning
     */
    public static ConversionWarning truncatedConversionWarningFor(int index, int limit) {
        return new ConversionWarning(index, Code.TRUNCATED_ITEMS, Integer.valueOf(limit));
    }

    // ----------------------------------------------------------------------------------------------------

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

    public static enum Code implements DisplayableOXExceptionCode {
        /**
         * Unable to convert task status "%1$s".
         */
        INVALID_STATUS("Unable to convert task status \"%1$s\".", CATEGORY_USER_INPUT, 1, ConversionWarningMessage.INVALID_STATUS_MSG),
        /**
         * Unable to convert task priority %1$s.
         */
        INVALID_PRIORITY("Unable to convert task priority %1$s.", CATEGORY_USER_INPUT, 2, ConversionWarningMessage.INVALID_PRIORITY_MSG),
        /**
         * Can not create recurrence rule: %1$s
         */
        CANT_CREATE_RRULE("Can not create recurrence rule: %1$s", CATEGORY_ERROR, 3, null),
        /**
         * Invalid session given to implementation "%1$s".
         */
        INVALID_SESSION("Invalid session given to implementation \"%1$s\".", CATEGORY_ERROR, 4, null),
        /**
         * Can't generate uid.
         */
        CANT_GENERATE_UID("Can not generate uid.", CATEGORY_ERROR, 5, null),
        /**
         * Problem writing to stream.
         */
        WRITE_PROBLEM("Problem writing to stream.", CATEGORY_CONNECTIVITY, 6, null),
        /**
         * Validation of calendar failed.
         */
        VALIDATION("Validation of calendar failed.", CATEGORY_ERROR, 7, null),
        /**
         * Cannot resolve user: %1$s
         */
        CANT_RESOLVE_USER("Cannot resolve user: %1$s", CATEGORY_ERROR, 8, null),
        /**
         * Parsing error parsing ical: %1$s
         */
        PARSE_EXCEPTION("Parsing error parsing ical: %1$s", CATEGORY_USER_INPUT, 9, ConversionWarningMessage.PARSE_EXCEPTION_MSG),
        /**
         * Unknown Class: %1$s
         */
        UNKNOWN_CLASS("Unknown Class: %1$s", CATEGORY_USER_INPUT, 10, ConversionWarningMessage.UNKNOWN_CLASS_MSG),
        /**
         * Cowardly refusing to convert confidential classified objects.
         */
        CLASS_CONFIDENTIAL("Cowardly refusing to convert confidential classified objects.", CATEGORY_USER_INPUT, 11,
            ConversionWarningMessage.CLASS_CONFIDENTIAL_MSG),
        /**
         * Missing DTStart in appointment
         */
        MISSING_DTSTART("Missing DTSTART", CATEGORY_USER_INPUT, 12, ConversionWarningMessage.MISSING_DTSTART_MSG),
        /**
         * Cannot resolve resource: %1$s
         */
        CANT_RESOLVE_RESOURCE("Cannot resolve resource: %1$s", CATEGORY_ERROR, 13, null),
        /**
         * Private Appointments can not have attendees. Removing attendees and accepting appointment anyway.
         */
        PRIVATE_APPOINTMENTS_HAVE_NO_PARTICIPANTS("Private appointments can not have attendees. Removing attendees and accepting "
            + "appointment anyway.", CATEGORY_USER_INPUT, 14, ConversionWarningMessage.PRIVATE_APPOINTMENTS_HAVE_NO_PARTICIPANTS_MSG),
        /**
         * Not supported recurrence pattern: BYMONTH
         */
        BYMONTH_NOT_SUPPORTED("Not supported recurrence pattern: BYMONTH", CATEGORY_USER_INPUT, 15,
            ConversionWarningMessage.BYMONTH_NOT_SUPPORTED_MSG),
        /**
         * This does not look like an iCal file. Please check the file.
         */
        DOES_NOT_LOOK_LIKE_ICAL_FILE("This does not look like an iCal file. Please check the file.", CATEGORY_USER_INPUT, 16,
            ConversionWarningMessage.DOES_NOT_LOOK_LIKE_ICAL_FILE_MSG),
        /**
         * Empty "CLASS" element.
         */
        EMPTY_CLASS("Empty \"CLASS\" element.", CATEGORY_USER_INPUT, 17, ConversionWarningMessage.EMPTY_CLASS_MSG),
        /**
         * Insufficient information for parsing/writing this element.
         */
        INSUFFICIENT_INFORMATION("Insufficient information for parsing/writing this element.", CATEGORY_ERROR, 18, null),
        /**
         * An error occurred: %1$s
         */
        UNEXPECTED_ERROR("An error occurred: %1$s", CATEGORY_ERROR, 19, null),
        /**
         * Element automatically truncated: %1$s
         */
        TRUNCATION_WARNING("Element truncated: %1$s", CATEGORY_TRUNCATED, 20, ConversionWarningMessage.TRUNCATION_WARNING_MSG),
        /**
         * Cannot resolve group: %1$s
         */
        CANT_RESOLVE_GROUP("Cannot resolve group: %1$s", CATEGORY_ERROR, 21, null),
        /**
         * Invalid mail address for external participant: %1$s
         */
        INVALID_MAIL_ADDRESS("Invalid mail address for external participant: %1$s", CATEGORY_USER_INPUT, 22, ConversionWarningMessage.INVALID_MAIL_ADDRESS_MSG),
        /**
         * The conversion yields some objects which could not be stored due to missing folder for %1$s
         */
        NO_FOLDER_FOR_APPOINTMENTS("The conversion yields some objects which could not be stored due to missing folder for appointments.", CATEGORY_WARNING, 23, ConversionWarningMessage.NO_FOLDER_FOR_APPOINTMENTS),
        /**
         * The conversion yields some objects which could not be stored due to missing folder for %1$s
         */
        NO_FOLDER_FOR_TASKS("The conversion yields some objects which could not be stored due to missing folder for tasks.", CATEGORY_WARNING, 23, ConversionWarningMessage.NO_FOLDER_FOR_TASKS),
        /**
         * The object could not be stored due to a configured limit of %1$s
         */
        TRUNCATED_ITEMS("The object could not be stored due to a configured limit of %1$s", CATEGORY_WARNING, 24, ConversionWarningMessage.TRUNCATED_ITEMS)

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

        private String displayMessage;

        /**
         * Default constructor.
         *
         * @param message message.
         * @param category category.
         * @param number detail number.
         */
        private Code(final String message, final Category category, final int number, String displayMessage) {
            this.message = message;
            this.category = category;
            this.number = number;
            this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
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

        @Override
        public String getDisplayMessage() {
            return displayMessage;
        }
    }
}
