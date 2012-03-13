package com.openexchange.groupware.reminder;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

public enum ReminderExceptionCode implements OXExceptionCode {
    /**
     * User is missing for the reminder.
     */
    MANDATORY_FIELD_USER("User is missing for the reminder.", 1, CATEGORY_ERROR),
    /**
     * Identifier of the object is missing.
     */
    MANDATORY_FIELD_TARGET_ID("Identifier of the object is missing.", 2, CATEGORY_ERROR),
    /**
     * Alarm date for the reminder is missing.
     */
    MANDATORY_FIELD_ALARM("Alarm date for the reminder is missing.", 3, CATEGORY_ERROR),
    INSERT_EXCEPTION("Unable to insert reminder", 4, CATEGORY_ERROR),
    UPDATE_EXCEPTION("Unable to update reminder.", 5, CATEGORY_ERROR),
    DELETE_EXCEPTION("Unable to delete reminder", 6, CATEGORY_ERROR),
    LOAD_EXCEPTION("Unable to load reminder", 7, CATEGORY_ERROR),
    LIST_EXCEPTION("Unable to list reminder", 8, CATEGORY_ERROR),
    /** Can not find reminder with identifier %1$d in context %2$d. */
    NOT_FOUND("Can not find reminder with identifier %1$d in context %2$d.", 9, CATEGORY_ERROR),
    /**
     * Folder of the object is missing.
     */
    MANDATORY_FIELD_FOLDER("Folder of the object is missing.", 10, CATEGORY_ERROR),
    /**
     * Module type of the object is missing.
     */
    MANDATORY_FIELD_MODULE("Module type of the object is missing.", 11, CATEGORY_ERROR),
    /**
     * Updated too many reminders.
     */
    TOO_MANY("Updated too many reminders.", 12, CATEGORY_ERROR),
    /** SQL Problem: %1$s. */
    SQL_ERROR("SQL Problem: \"%1$s\"", 13, CATEGORY_ERROR),
    /** No target service is registered for module %1$d. */
    NO_TARGET_SERVICE("No target service is registered for module %1$d.", 14, CATEGORY_ERROR),
    /**
     * Unexpected error: %1$s
     */
    UNEXPECTED_ERROR("Unexpected error: %1$s", 15, CATEGORY_ERROR),
    /**
     * Reminder identifier is missing.
     */
    MANDATORY_FIELD_ID("Reminder identifier is missing.", 16, CATEGORY_ERROR);

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
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private ReminderExceptionCode(final String message, final int detailNumber, final Category category)  {
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
