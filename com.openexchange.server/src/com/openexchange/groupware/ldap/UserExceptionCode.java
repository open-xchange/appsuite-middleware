package com.openexchange.groupware.ldap;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link UserExceptionCode} - The user error codes.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum UserExceptionCode implements OXExceptionCode {
    /**
     * A property from the ldap.properties file is missing.
     */
    PROPERTY_MISSING("Cannot find property %s.", Category.CATEGORY_CONFIGURATION,
        1),
    /**
     * A problem with distinguished names occurred.
     */
    DN_PROBLEM("Cannot build distinguished name from %s.",
        Category.CATEGORY_ERROR, 2),
    /**
     * Class can not be found.
     */
    CLASS_NOT_FOUND("Class %s can not be loaded.", Category.CATEGORY_CONFIGURATION,
        3),
    /**
     * An implementation can not be instantiated.
     */
    INSTANTIATION_PROBLEM("Cannot instantiate class %s.",
        Category.CATEGORY_CONFIGURATION, 4),
    /**
     * A database connection cannot be obtained.
     */
    NO_CONNECTION("Cannot get database connection.",
        Category.CATEGORY_SERVICE_DOWN, 5),
    /**
     * Cannot clone object %1$s.
     */
    NOT_CLONEABLE("Cannot clone object %1$s.", Category.CATEGORY_ERROR,
        6),
    /**
     * SQL Problem: \"%s\".
     */
    SQL_ERROR("SQL Problem: \"%s\".", Category.CATEGORY_ERROR,
        7),
    /**
     * Hash algorithm %s isn't found.
     */
    HASHING("Hash algorithm %s isn't found.", Category.CATEGORY_ERROR, 8),
    /**
     * Encoding %s cannot be used.
     */
    UNSUPPORTED_ENCODING("Encoding %s cannot be used.", Category.CATEGORY_ERROR,
        9),
    /**
     * Cannot find user with identifier %1$s in context %2$d.
     */
    USER_NOT_FOUND("Cannot find user with identifier %1$s in context %2$d.",
        Category.CATEGORY_ERROR, 10),
    /**
     * Found two user with same identifier %1$s in context %2$d.
     */
    USER_CONFLICT("Found two user with same identifier %1$s in context "
        + "%2$d.", Category.CATEGORY_ERROR, 11),
    /**
     * Problem putting an object into the cache.
     */
    CACHE_PROBLEM("Problem putting/removing an object into/from the cache.",
        Category.CATEGORY_ERROR, 12),
    /**
     * No CATEGORY_PERMISSION_DENIED to modify resources in context %1$s
     */
     PERMISSION("No CATEGORY_PERMISSION_DENIED to modify resources in context %1$s",
        Category.CATEGORY_PERMISSION_DENIED, 13),
     /**
      * Missing or unknown password mechanism %1$s
      */
     MISSING_PASSWORD_MECH("Missing or unknown password mechanism %1$s", Category.CATEGORY_ERROR, 14),
     /**
      * New password contains invalid characters
      */
     INVALID_PASSWORD("New password contains invalid characters", Category.CATEGORY_USER_INPUT, 15),
     /**
      * Attributes of user %1$d in context %2$d have been erased.
      */
     ERASED_ATTRIBUTES("Attributes of user %1$d in context %2$d have been erased.", Category.CATEGORY_WARNING, 16),
     /**
      * Loading one or more users failed.
      */
     LOAD_FAILED("Loading one or more users failed.", Category.CATEGORY_ERROR, 17),
     /** Alias entries are missing for user %1$d in context %2$d. */
     ALIASES_MISSING("Alias entries are missing for user %1$d in context %2$d.", Category.CATEGORY_CONFIGURATION, 18),
     /** Updating attributes failed in context %1$d for user %2$d. */
     UPDATE_ATTRIBUTES_FAILED("Updating attributes failed in context %1$d for user %2$d.", Category.CATEGORY_ERROR, 19);

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
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private UserExceptionCode(final String message, final Category category,
        final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
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
    public String getPrefix() {
        return "USR";
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