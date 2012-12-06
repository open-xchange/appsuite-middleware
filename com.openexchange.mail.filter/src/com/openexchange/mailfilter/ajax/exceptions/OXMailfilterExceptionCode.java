package com.openexchange.mailfilter.ajax.exceptions;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 *
 * Mail filter error codes.
 */
public enum OXMailfilterExceptionCode implements OXExceptionCode {

	/**
	 * %s
	 */
	PROBLEM("%s", CATEGORY_ERROR, 1),
    /**
     * %s
     */
    SESSION_EXPIRED("%s", CATEGORY_PERMISSION_DENIED, 200),
    /**
     * Missing parameter %s
     */
    MISSING_PARAMETER("Missing parameter %s", CATEGORY_ERROR, 1),
    /**
     * Invalid credentials
     */
    INVALID_CREDENTIALS("Invalid sieve credentials", CATEGORY_PERMISSION_DENIED, 2),
    /**
     * A JSON error occurred: %s
     */
    JSON_ERROR("A JSON error occurred: %s", CATEGORY_ERROR, 3),
    /**
     * Property error: %s
     */
    PROPERTY_ERROR("Property error: %s", CATEGORY_CONFIGURATION, 4),
    /**
     * Sieve error: %1$s
     */
    SIEVE_ERROR("Sieve error: %1$s", CATEGORY_ERROR, 5),
    /**
     * mail filter servlet cannot be registered: %s
     */
    SERVLET_REGISTRATION_FAILED("mail filter servlet cannot be registered: %s", CATEGORY_ERROR, 6),
    /**
     * The position where the rule should be added is too big
     */
    POSITION_TOO_BIG("The position where the rule should be added is too big", CATEGORY_ERROR, 7),
    /**
     * A rule with the id %1$s does not exist for user %2$s in context %3$s
     */
    NO_SUCH_ID("A rule with the id %1$s does not exist for user %2$s in context %3$s", CATEGORY_ERROR, 8),
    /**
     * The id is missing inside the update request
     */
    ID_MISSING("The id is missing inside the update request or has a non integer type", CATEGORY_ERROR, 9),
    /**
     * A server name cannot be found in the server URL "%1$s".
     */
    NO_SERVERNAME_IN_SERVERURL("A server name cannot be found in the server URL \"%1$s\".", CATEGORY_ERROR, 10),
    /**
     * The login type given in the config file is not a valid one
     */
    NO_VALID_LOGIN_TYPE("The login type given in the config file is not a valid one", CATEGORY_ERROR, 11),
    /**
     * The credsrc given in the config file is not a valid one
     */
    NO_VALID_CREDSRC("The credsrc given in the config file is not a valid one", CATEGORY_ERROR, 12),
    /**
     * The encoding given is not supported by Java
     */
    UNSUPPORTED_ENCODING("The encoding given is not supported by Java", CATEGORY_ERROR, 13),
    /**
     * Error in low level connection to sieve server
     */
    IO_CONNECTION_ERROR("Error in low level connection to sieve server %1$s at port %2$s", CATEGORY_ERROR, 14),
    /**
     * Error while communicating with the sieve server %1$s at port %2$s for user %3$s in context %4$s
     */
    SIEVE_COMMUNICATION_ERROR("Error while communicating with the sieve server %1$s at port %2$s for user %3$s in context %4$s", CATEGORY_ERROR, 15),
    /**
     * Lexical error: %1$s
     */
    LEXICAL_ERROR("Lexical error: %1$s", CATEGORY_ERROR, 16),
    /**
     * Input string "%1$s" is not a number.
     */
    NAN("Input string \"%1$s\" is not a number.", CATEGORY_USER_INPUT, 17),
    /**
     * The field \"%1$s\" must have a value, but is not set.
     */
    EMPTY_MANDATORY_FIELD("The field \"%1$s\" must have a value, but is not set", CATEGORY_USER_INPUT, 18),
    /**
     * The configuration requests a master password but none is given in the configuration file
     */
    NO_MASTERPASSWORD_SET("The configuration requests a master password but none is given in the configuration file", CATEGORY_ERROR, 19),
    /**
     * The passwordSource given in the config file is not a valid one
     */
    NO_VALID_PASSWORDSOURCE("The passwordSource given in the config file is not a valid one", CATEGORY_ERROR, 20),
    /**
     * Another vacation rule already exists. Please remove that first and try again.
     */
    DUPLICATE_VACATION_RULE("Another vacation rule already exists. Please remove that first and try again.", CATEGORY_TRY_AGAIN, 21),
    
    ;

	public static final String ERR_PREFIX_INVALID_ADDRESS = "The parameter for redirect must be a valid Internet email address";
    public static final String ERR_PREFIX_REJECTED_ADDRESS = "The Internet email address used for redirect is not allowed: ";

    private final String message;

    private final int detailNumber;

    private final Category category;

    private OXMailfilterExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    @Override
    public String getPrefix() {
        return "MAIL_FILTER";
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
