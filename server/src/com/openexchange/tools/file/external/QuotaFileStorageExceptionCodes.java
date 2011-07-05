package com.openexchange.tools.file.external;

import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionStrings;

/**
 * Error codes for the file storage exception.
 * 
 * @author Steffen Templin
 */
public enum QuotaFileStorageExceptionCodes implements OXExceptionCode {
    /** Couldn't reach the filestore */
    INSTANTIATIONERROR("Couldn't reach the filestore", Category.CATEGORY_SERVICE_DOWN, 21),
    /** Database Query could not be realized */
    SQLSTATEMENTERROR("Database Query could not be realized", Category.CATEGORY_ERROR, 23),
    /** The allowed Quota is reached. */
    STORE_FULL("The allowed Quota is reached.", Category.CATEGORY_USER_INPUT, 24),
    /** Quota seems to be inconsistent. Please use consistency tool on context %1$d. */
    QUOTA_UNDERRUN("Quota seems to be inconsistent. Please use consistency tool on context %1$d.", Category.CATEGORY_TRUNCATED, 25),
    /** Quota usage is missing for context %1$d. */
    NO_USAGE("Quota usage is missing for context %1$d.", Category.CATEGORY_ERROR, 26),
    /** Update of quota usage for context %1$d failed. */
    UPDATE_FAILED("Update of quota usage for context %1$d failed.", Category.CATEGORY_ERROR, 27);

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
    
    private final boolean display;

    /**
     * Default constructor.
     * 
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private QuotaFileStorageExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.number = detailNumber;
        display = category.getLogLevel().implies(LogLevel.DEBUG);
    }
    
    public String getPrefix() {
        return "FLS";
    }

    public Category getCategory() {
        return category;
    }

    public int getNumber() {
        return number;
    }

    public String getMessage() {
        return message;
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

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * 
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        final OXException ret;
        if (display) {
            ret = new OXException(number, message, cause, args);
        } else {
            ret =
                new OXException(
                    number,
                    Category.EnumType.TRY_AGAIN.equals(category.getType()) ? OXExceptionStrings.MESSAGE_RETRY : OXExceptionStrings.MESSAGE,
                    new Object[0]).setLogMessage(message, args);
        }
        return ret.addCategory(category).setPrefix(getPrefix());
    }
}