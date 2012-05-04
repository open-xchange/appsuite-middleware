
package com.openexchange.server;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * The error code enumeration for service-related issues.
 */
public enum ServiceExceptionCodes implements OXExceptionCode {

    /**
     * The required service %1$s is temporary not available. Please try again later.
     */
    SERVICE_UNAVAILABLE(ServiceExceptionMessage.SERVICE_UNAVAILABLE_MSG, Category.CATEGORY_TRY_AGAIN, 1),
    /**
     * An I/O error occurred
     */
    IO_ERROR(ServiceExceptionMessage.IO_ERROR_MSG, Category.CATEGORY_ERROR, 2),
    /**
     * Service initialization failed
     */
    SERVICE_INITIALIZATION_FAILED(ServiceExceptionMessage.SERVICE_INITIALIZATION_FAILED_MSG, Category.CATEGORY_ERROR, 3);

    private final String message;

    private final int detailNumber;

    private final Category category;

    private ServiceExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    private static final String PREFIX = "SRV";

    @Override
    public String getPrefix() {
        return PREFIX;
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
        return OXExceptionFactory.getInstance().create(this, cause, arguments);
    }

}
