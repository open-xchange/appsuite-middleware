
package com.openexchange.groupware.reminder;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

public enum ReminderExceptionCode implements OXExceptionCode {
    /**
     * User is missing for the reminder.
     */
    MANDATORY_FIELD_USER(ReminderExceptionMessage.MANDATORY_FIELD_USER_MSG, 1, CATEGORY_ERROR),
    /**
     * Identifier of the object is missing.
     */
    MANDATORY_FIELD_TARGET_ID(ReminderExceptionMessage.MANDATORY_FIELD_TARGET_ID_MSG, 2, CATEGORY_ERROR),
    /**
     * Alarm date for the reminder is missing.
     */
    MANDATORY_FIELD_ALARM(ReminderExceptionMessage.MANDATORY_FIELD_ALARM_MSG, 3, CATEGORY_ERROR),
    INSERT_EXCEPTION(ReminderExceptionMessage.INSERT_EXCEPTION_MSG, 4, CATEGORY_ERROR),
    UPDATE_EXCEPTION(ReminderExceptionMessage.UPDATE_EXCEPTION_MSG, 5, CATEGORY_ERROR),
    DELETE_EXCEPTION(ReminderExceptionMessage.DELETE_EXCEPTION_MSG, 6, CATEGORY_ERROR),
    LOAD_EXCEPTION(ReminderExceptionMessage.LOAD_EXCEPTION_MSG, 7, CATEGORY_ERROR),
    LIST_EXCEPTION(ReminderExceptionMessage.LIST_EXCEPTION_MSG, 8, CATEGORY_ERROR),
    /** Can not find reminder with identifier %1$d in context %2$d. */
    NOT_FOUND(ReminderExceptionMessage.NOT_FOUND_MSG, 9, CATEGORY_ERROR),
    /**
     * Folder of the object is missing.
     */
    MANDATORY_FIELD_FOLDER(ReminderExceptionMessage.MANDATORY_FIELD_FOLDER_MSG, 10, CATEGORY_ERROR),
    /**
     * Module type of the object is missing.
     */
    MANDATORY_FIELD_MODULE(ReminderExceptionMessage.MANDATORY_FIELD_MODULE_MSG, 11, CATEGORY_ERROR),
    /**
     * Updated too many reminders.
     */
    TOO_MANY(ReminderExceptionMessage.TOO_MANY_MSG, 12, CATEGORY_ERROR),
    /** SQL Problem: %1$s. */
    SQL_ERROR(ReminderExceptionMessage.SQL_ERROR_MSG, 13, CATEGORY_ERROR),
    /** No target service is registered for module %1$d. */
    NO_TARGET_SERVICE(ReminderExceptionMessage.NO_TARGET_SERVICE_MSG, 14, CATEGORY_ERROR),
    /**
     * Unexpected error: %1$s
     */
    UNEXPECTED_ERROR(ReminderExceptionMessage.UNEXPECTED_ERROR_MSG, 15, CATEGORY_ERROR),
    /**
     * Reminder identifier is missing.
     */
    MANDATORY_FIELD_ID(ReminderExceptionMessage.MANDATORY_FIELD_ID_MSG, 16, CATEGORY_ERROR);

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
    private final int detailNumber;

    /**
     * Default constructor.
     * 
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private ReminderExceptionCode(final String message, final int detailNumber, final Category category) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    @Override
    public String getPrefix() {
        return "REM";
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
}
