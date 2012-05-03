package com.openexchange.conversion.servlet;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

public enum ConversionServletExceptionCode implements OXExceptionCode {

    /**
     * A JSON error occurred: %1$s
     */
    JSON_ERROR(ConversionServletExceptionMessage.JSON_ERROR_MSG, Category.CATEGORY_ERROR, 1),
    /**
     * Missing parameter %1$s
     */
    MISSING_PARAM(ConversionServletExceptionMessage.MISSING_PARAM_MSG, Category.CATEGORY_ERROR, 2),
    /**
     * Unsupported value in parameter %1$s: %2$s
     */
    UNSUPPORTED_PARAM(ConversionServletExceptionMessage.UNSUPPORTED_PARAM_MSG, Category.CATEGORY_ERROR, 3),
    /**
     * Unsupported method %1$s
     */
    UNSUPPORTED_METHOD(ConversionServletExceptionMessage.UNSUPPORTED_METHOD_MSG, Category.CATEGORY_ERROR, 4);

    private final String message;

    private final int detailNumber;

    private final Category category;

    private ConversionServletExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    @Override
    public String getPrefix() {
        return "SRV";
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
