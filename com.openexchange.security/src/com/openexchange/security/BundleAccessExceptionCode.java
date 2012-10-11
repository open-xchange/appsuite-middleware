package com.openexchange.security;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

public enum BundleAccessExceptionCode implements OXExceptionCode {

	/**
	 * Access to bundle %1$s is not permitted
	 */
	ACCESS_DENIED(BundleAccessExceptionMessage.ACCESS_DENIED, CATEGORY_PERMISSION_DENIED, 1);

	private final String message;

	private final int detailNumber;

	private final Category category;

	private BundleAccessExceptionCode(final String message, final Category category, final int detailNumber) {
		this.message = message;
		this.detailNumber = detailNumber;
		this.category = category;
	}

	@Override
    public String getPrefix() {
	    return "SECURITY";
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
