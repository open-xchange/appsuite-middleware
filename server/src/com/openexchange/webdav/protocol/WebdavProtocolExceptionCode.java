package com.openexchange.webdav.protocol;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.groupware.EnumComponent;

public enum WebdavProtocolExceptionCode implements OXExceptionCode {

    /**
     * A WebDAV error occurred.
     */
    GENERAL_ERROR("A WebDAV error occurred.", CATEGORY_ERROR, 1000),
    /**
     * The folder %s doesn't exist.
     */
    FOLDER_NOT_FOUND("The folder %s doesn't exist.", CATEGORY_ERROR, 1001),
    /**
     * The directory already exists.
     */
    DIRECTORY_ALREADY_EXISTS("The directory already exists.", CATEGORY_ERROR, 1002),
    /**
     * No write permission.
     */
    NO_WRITE_PERMISSION("No write permission.", CATEGORY_PERMISSION_DENIED, 1003),
    /**
     * File &quot;%1$s&quot; already exists
     */
    FILE_ALREADY_EXISTS("File \"%1$s\" already exists.", CATEGORY_ERROR, 1004),
    /**
     * Collections must not have bodies.
     */
    NO_BODIES_ALLOWED("Collections must not have bodies.", CATEGORY_ERROR, 1005),
    /**
     * File "%1$s" does not exist.
     */
    FILE_NOT_FOUND("File \"%1$s\" does not exist.", CATEGORY_ERROR, 1006),
    /**
     * "%1$s" is a directory.
     */
    FILE_IS_DIRECTORY("\"%1$s\" is a directory.", CATEGORY_ERROR, 1007);

    private final String message;

    private final int detailNumber;

    private final Category category;

    private WebdavProtocolExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }
    
    public String getPrefix() {
        return EnumComponent.WEBDAV.getAbbreviation();
    }

    public Category getCategory() {
        return category;
    }

    public int getNumber() {
        return detailNumber;
    }

    public String getMessage() {
        return message;
    }
    
    public boolean equals(final OXException e) {
        return getPrefix().equals(e.getPrefix()) && e.getCode() == getNumber();
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