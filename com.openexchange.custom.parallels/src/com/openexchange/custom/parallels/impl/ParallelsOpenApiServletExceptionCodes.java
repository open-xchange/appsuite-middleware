

package com.openexchange.custom.parallels.impl;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link ParallelsOpenApiServletExceptionCodes}
 */
public enum ParallelsOpenApiServletExceptionCodes implements OXExceptionCode {

    /**
     * A openapi  interface error occurred. action: \"%1$s\" ,response: \"%2$s\"
     */
    OPENAPI_COMMUNICATION_ERROR("An OpenAPI interface error occurred. action: \"%1$s\" ,response: \"%2$s\" " , Category.CATEGORY_ERROR, 1),
    
    HTTP_COMMUNICATION_ERROR("OpenAPI communication error detected. Details: \"%1$s\"" , Category.CATEGORY_ERROR, 1);

    private final Category category;

    private final int detailNumber;

    private final String message;

    private ParallelsOpenApiServletExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    private static final String PREFIX = "CUSTOM_STRATO_SMS_MESSAGING";
    
    @Override
    public String getPrefix() {
        return PREFIX;
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
