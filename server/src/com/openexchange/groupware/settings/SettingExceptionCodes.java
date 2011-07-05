package com.openexchange.groupware.settings;

import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionStrings;

/**
 * The error code enumeration for setting exceptions.
 */
public enum SettingExceptionCodes {
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

    private final boolean display;

    private SettingExceptionCodes(final String message, final Category category,
        final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
        display = LogLevel.DEBUG.equals(category.getLogLevel());
    }

    public Category getCategory() {
        return category;
    }

    public int getDetailNumber() {
        return detailNumber;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Creates an {@link OXException} instance using this error code.
     * 
     * @return The newly created {@link OXException} instance.
     */
    public OXException create() {
        return create(new Object[0]);
    }

    /**
     * Creates an {@link OXException} instance using this error code.
     * 
     * @param arguments The arguments for message.
     * @return The newly created {@link OXException} instance.
     */
    public OXException create(final Object... arguments) {
        return create(null, arguments);
    }

    private static final String PREFIX = "USS";

    /**
     * Creates an {@link OXException} instance using this error code.
     * 
     * @param cause The initial cause for {@link OXException}
     * @param arguments The arguments for message.
     * @return The newly created {@link OXException} instance.
     */
    public OXException create(final Throwable cause, final Object... arguments) {
        final OXException ret;
        if (display) {
            ret = new OXException(detailNumber, message, cause, arguments);
        } else {
            ret = new OXException(detailNumber, OXExceptionStrings.MESSAGE, cause);
            ret.setLogMessage(message, arguments);
        }
        return ret.setPrefix(PREFIX).addCategory(category);
    }
}