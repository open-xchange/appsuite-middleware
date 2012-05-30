package com.openexchange.http.client.exceptions;

import static com.openexchange.http.client.exceptions.OxHttpClientExceptionMessages.*;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.groupware.EnumComponent;

public enum OxHttpClientExceptionCodes implements OXExceptionCode {
	APACHE_CLIENT_ERROR(APACHE_CLIENT_ERROR_MSG, Category.CATEGORY_ERROR, 1),
	JSON_ERROR(JSON_ERROR_MSG, Category.CATEGORY_ERROR, 2), 
	SAX_ERROR(SAX_ERROR_MSG, Category.CATEGORY_ERROR, 3), 
	CATCH_ALL(CATCH_ALL_MSG, Category.CATEGORY_ERROR, 4), 
	IO_ERROR(IO_ERROR_MSG, Category.CATEGORY_ERROR, 5),
	
	;
	
    private String message;
    private Category category;
    private int number;

    private OxHttpClientExceptionCodes(final String message, final Category category, final int number) {
        this.message = message;
        this.category = category;
        this.number = number;
    }

    public String getPrefix() {
        return EnumComponent.IMPORT_EXPORT.getAbbreviation();
    }

    public int getNumber() {
        return number;
    }

    public String getMessage() {
        return message;
    }


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

	public Category getCategory() {
		// TODO Auto-generated method stub
		return null;
	}

}
