package com.openexchange.groupware.settings;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * The error codes for settings.
 */
public enum SettingExceptionCodes implements OXExceptionCode {
    /** Cannot get connection to database. */
    NO_CONNECTION("Cannot get connection to database.", Category.CATEGORY_SERVICE_DOWN, 1),
    /** An SQL problem occures while reading information from the config database. */
    SQL_ERROR(null, Category.CATEGORY_ERROR, 2),
    /** Writing the setting %1$s is not permitted. */
    NO_WRITE("Writing the setting %1$s is not permitted.", Category.CATEGORY_PERMISSION_DENIED, 3),
    /** Unknown setting path %1$s. */
    UNKNOWN_PATH("Unknown setting path %1$s.", Category.CATEGORY_ERROR, 4),
    /** Setting "%1$s" is not a leaf one. */
    NOT_LEAF("Setting \"%1$s\" is not a leaf one.", Category.CATEGORY_ERROR, 5),
    /** Exception while parsing JSON. */
    JSON_READ_ERROR("Exception while parsing JSON.", Category.CATEGORY_ERROR, 6),
    /** Problem while initialising configuration tree. */
    INIT("Problem while initialising configuration tree.", Category.CATEGORY_ERROR, 8),
    /** Invalid value %s written to setting %s. */
    INVALID_VALUE("Invalid value %s written to setting %s.", Category.CATEGORY_USER_INPUT, 9),
    /** Found duplicate database identifier %d. Not adding preferences item. */
    DUPLICATE_ID("Found duplicate database identifier %d. Not adding preferences item.", Category.CATEGORY_ERROR, 10),
    /** Found duplicate path %s. */
    DUPLICATE_PATH("Found duplicate path %s.", Category.CATEGORY_ERROR, 12),
    /** Subsystem error. */
    SUBSYSTEM("Error during use of a subsystem", Category.CATEGORY_SERVICE_DOWN, 13);

    private final String message;

    private final Category category;

    private final int detailNumber;

    private SettingExceptionCodes(final String message, final Category category,
        final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    @Override
    public String getPrefix() {
        return "USS";
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