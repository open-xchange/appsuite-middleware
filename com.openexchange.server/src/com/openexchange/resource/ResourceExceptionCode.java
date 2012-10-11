
package com.openexchange.resource;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Error codes for the resource exception.
 */
public enum ResourceExceptionCode implements OXExceptionCode {
    /**
     * A database connection Cannot be obtained.
     */
    NO_CONNECTION(ResourceExceptionMessage.NO_CONNECTION_MSG, Category.CATEGORY_SERVICE_DOWN, 1),
    /**
     * SQL Problem: "%1$s".
     */
    SQL_ERROR(ResourceExceptionMessage.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 2),
    /**
     * Cannot find resource group with identifier %1$d.
     */
    RESOURCEGROUP_NOT_FOUND(ResourceExceptionMessage.RESOURCEGROUP_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 3),
    /**
     * Found resource groups with same identifier %1$d.
     */
    RESOURCEGROUP_CONFLICT(ResourceExceptionMessage.RESOURCEGROUP_CONFLICT_MSG, Category.CATEGORY_USER_INPUT, 4),
    /**
     * Cannot find resource with identifier %1$d.
     */
    RESOURCE_NOT_FOUND(ResourceExceptionMessage.RESOURCE_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 5),
    /**
     * Found resource(s) with same identifier %1$s.
     */
    RESOURCE_CONFLICT(ResourceExceptionMessage.RESOURCE_CONFLICT_MSG, Category.CATEGORY_USER_INPUT, 6),
    /**
     * No resource given.
     */
    NULL(ResourceExceptionMessage.NULL_MSG, Category.CATEGORY_USER_INPUT, 7),
    /**
     * Missing mandatory field(s) in given resource.
     */
    MANDATORY_FIELD(ResourceExceptionMessage.MANDATORY_FIELD_MSG, Category.CATEGORY_ERROR, 8),
    /**
     * No permission to modify resources in context %1$s
     */
    PERMISSION(ResourceExceptionMessage.PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 9),
    /**
     * Found resource(s) with same email address %1$s.
     */
    RESOURCE_CONFLICT_MAIL(ResourceExceptionMessage.RESOURCE_CONFLICT_MAIL_MSG, Category.CATEGORY_ERROR, 10),
    /**
     * Invalid resource identifier: %1$s
     */
    INVALID_RESOURCE_IDENTIFIER(ResourceExceptionMessage.INVALID_RESOURCE_IDENTIFIER_MSG, Category.CATEGORY_USER_INPUT, 11),
    /**
     * Invalid resource email address: %1$s
     */
    INVALID_RESOURCE_MAIL(ResourceExceptionMessage.INVALID_RESOURCE_MAIL_MSG, Category.CATEGORY_USER_INPUT, 12),
    /**
     * The resource has been changed in the meantime
     */
    CONCURRENT_MODIFICATION(ResourceExceptionMessage.CONCURRENT_MODIFICATION_MSG, Category.CATEGORY_CONFLICT, 13);

    /**
     * Message of the exception.
     */
    final String message;

    /**
     * Category of the exception.
     */
    final Category category;

    /**
     * Detail number of the exception.
     */
    final int detailNumber;

    /**
     * Default constructor.
     * 
     * @param message message.
     * @param category category.
     * @param detail detailed information for the exception.
     * @param detailNumber detail number.
     */
    private ResourceExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getPrefix() {
        return "RES";
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
