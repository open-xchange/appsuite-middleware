package com.openexchange.ajax.helper;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 *
 * The error code enumeration for missing or invalid request parameters.
 */
public enum ParamContainerExceptionCode implements OXExceptionCode {

	/**
	 * Bad value %1$s in parameter %2$s
	 */
	BAD_PARAM_VALUE(ParamContainerExceptionMessage.BAD_PARAM_VALUE_MSG, CATEGORY_USER_INPUT, 1),
	/**
	 * Missing parameter %1$s
	 */
	MISSING_PARAMETER(ParamContainerExceptionMessage.MISSING_PARAMETER_MSG, CATEGORY_ERROR, 2);

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
	 * @param message
	 *            message.
	 * @param category
	 *            category.
	 * @param detailNumber
	 *            detail number.
	 */
	private ParamContainerExceptionCode(final String message, final Category category, final int detailNumber) {
		this.message = message;
		this.category = category;
		this.detailNumber = detailNumber;
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
    public Category getCategory() {
		return category;
	}

	@Override
    public String getPrefix() {
	    return "REQ_PARAM";
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
