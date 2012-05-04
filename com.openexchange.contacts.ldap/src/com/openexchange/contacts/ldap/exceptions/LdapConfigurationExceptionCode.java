package com.openexchange.contacts.ldap.exceptions;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Error codes for permission exceptions.
 * @author <a href="mailto:dennis.sieben@open-xchange.org">Dennis Sieben</a>
 */
public enum LdapConfigurationExceptionCode implements OXExceptionCode {
    /**
     * The given value for authtype "%s" is not a possible one
     */
    AUTH_TYPE_WRONG(LdapConfigurationExceptionMessage.AUTH_TYPE_WRONG_MSG, Category.CATEGORY_CONFIGURATION, 1),

    /**
     * The given value for authtype "%s" is not a possible one
     */
    SORTING_WRONG(LdapConfigurationExceptionMessage.SORTING_WRONG_MSG, Category.CATEGORY_CONFIGURATION, 2),

    /**
     * The given value for searchScope "%s" is not a possible one
     */
    SEARCH_SCOPE_WRONG(LdapConfigurationExceptionMessage.SEARCH_SCOPE_WRONG_MSG, Category.CATEGORY_CONFIGURATION, 3),

    /**
     * The parameter "%s" is not set in the property file %s
     */
    PARAMETER_NOT_SET(LdapConfigurationExceptionMessage.PARAMETER_NOT_SET_MSG, Category.CATEGORY_CONFIGURATION, 4),

    /**
     * Value for context "%s" is no integer value
     */
    NO_INTEGER_VALUE(LdapConfigurationExceptionMessage.NO_INTEGER_VALUE_MSG, Category.CATEGORY_CONFIGURATION, 5),

    /**
     * Mapping file %s not valid
     */
    INVALID_MAPPING_FILE(LdapConfigurationExceptionMessage.INVALID_MAPPING_FILE_MSG, Category.CATEGORY_CONFIGURATION, 6),

    /**
     * The given value for pagesize "%s" is no integer value
     */
    INVALID_PAGESIZE(LdapConfigurationExceptionMessage.INVALID_PAGESIZE_MSG, Category.CATEGORY_CONFIGURATION, 7),

    /**
     * The given value for userauthtype "%s" is not a possible one
     */
    USER_AUTH_TYPE_WRONG(LdapConfigurationExceptionMessage.USER_AUTH_TYPE_WRONG_MSG, Category.CATEGORY_CONFIGURATION, 8),

    /**
     * The given value for userSearchScope "%s" is not a possible one
     */
    USER_SEARCH_SCOPE_WRONG(LdapConfigurationExceptionMessage.USER_SEARCH_SCOPE_WRONG_MSG, Category.CATEGORY_CONFIGURATION, 9),

    /**
     * The given value for userLoginSource "%s" is not a possible one
     */
    USER_LOGIN_SOURCE_WRONG(LdapConfigurationExceptionMessage.USER_LOGIN_SOURCE_WRONG_MSG, Category.CATEGORY_CONFIGURATION, 10),

    /**
     * The given value for contactTypes "%s" is not a possible one
     */
    CONTACT_TYPES_WRONG(LdapConfigurationExceptionMessage.CONTACT_TYPES_WRONG_MSG, Category.CATEGORY_CONFIGURATION, 11),

    /**
     * The given value for searchScope_distributionlist "%s" is not a possible one
     */
    SEARCH_SCOPE_DISTRI_WRONG(LdapConfigurationExceptionMessage.SEARCH_SCOPE_DISTRI_WRONG_MSG, Category.CATEGORY_CONFIGURATION, 12),

    /**
     * Abstract pathname "%1$s" does not denote a directory.
     */
    NOT_DIRECTORY(LdapConfigurationExceptionMessage.NOT_DIRECTORY_MSG, Category.CATEGORY_CONFIGURATION, 13),

    /**
     * The directory "%1$s" is not a context identifier.
     */
    DIRECTORY_IS_NOT_A_CONTEXT_ID(LdapConfigurationExceptionMessage.DIRECTORY_IS_NOT_A_CONTEXT_ID_MSG, Category.CATEGORY_CONFIGURATION, 14),

    /**
     * The given value for referrals "%s" is not a possible one
     */
    REFERRALS_WRONG(LdapConfigurationExceptionMessage.REFERRALS_WRONG_MSG, Category.CATEGORY_CONFIGURATION, 15),

    /**
     * The given value for refreshinterval "%s" is no integer value
     */
    INVALID_REFRESHINTERVAL(LdapConfigurationExceptionMessage.INVALID_REFRESHINTERVAL_MSG, Category.CATEGORY_CONFIGURATION, 16),

    /**
     * The given value for pooltimeout "%s" in file "%s" is no integer value
     */
    INVALID_POOLTIMEOUT(LdapConfigurationExceptionMessage.INVALID_POOLTIMEOUT_MSG, Category.CATEGORY_CONFIGURATION, 17),

    /**
     * The given value for derefAliases "%s" in file "%s" is not a possible one
     */
    DEREF_ALIASES_WRONG(LdapConfigurationExceptionMessage.DEREF_ALIASES_WRONG_MSG, Category.CATEGORY_CONFIGURATION, 18);


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
    final int number;

    /**
     * Default constructor.
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private LdapConfigurationExceptionCode(final String message, final Category category,
        final int detailNumber) {
        this.message = message;
        this.category = category;
        this.number = detailNumber;
    }

    @Override
    public String getPrefix() {
        return "PERMISSION";
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
        return number;
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
