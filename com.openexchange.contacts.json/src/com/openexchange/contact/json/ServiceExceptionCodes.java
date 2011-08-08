package com.openexchange.contact.json;

import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionStrings;

/**
 * The error code enumeration for service-related issues.
 */
public enum ServiceExceptionCodes {

    /**
     * The required service %1$s is temporary not available. Please try again later.
     */
    SERVICE_UNAVAILABLE("The required service %1$s is temporary not available. Please try again later.", Category.CATEGORY_TRY_AGAIN, 1);;

    private final String message;

    private final int detailNumber;

    private final Category category;

    private final boolean display;

    private ServiceExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        display = LogLevel.DEBUG.equals(category.getLogLevel());
    }

    private static final String PREFIX = "SRV";

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
     * @param logArguments The arguments for log message.
     * @return The newly created {@link OXException} instance.
     */
    public OXException create(final Object... logArguments) {
        return create(null, logArguments);
    }

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