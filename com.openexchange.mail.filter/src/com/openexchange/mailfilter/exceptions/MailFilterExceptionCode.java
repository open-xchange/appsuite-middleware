/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.mailfilter.exceptions;

import static com.openexchange.mailfilter.exceptions.MailFilterExceptionMessages.INVALID_REDIRECT_ADDRESS_MSG;
import static com.openexchange.mailfilter.exceptions.MailFilterExceptionMessages.INVALID_SIEVE_RULE2_MSG;
import static com.openexchange.mailfilter.exceptions.MailFilterExceptionMessages.INVALID_SIEVE_RULE_MSG;
import static com.openexchange.mailfilter.exceptions.MailFilterExceptionMessages.MAILFILTER_NOT_AVAILABLE_MSG;
import static com.openexchange.mailfilter.exceptions.MailFilterExceptionMessages.REJECTED_REDIRECT_ADDRESS_MSG;
import java.util.regex.Pattern;
import org.apache.jsieve.SieveException;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.java.Strings;
import com.openexchange.jsieve.export.SieveResponse;
import com.openexchange.jsieve.export.exceptions.OXSieveHandlerException;
import com.openexchange.mailfilter.Credentials;

/**
 * Mail filter error codes.
 */
public enum MailFilterExceptionCode implements DisplayableOXExceptionCode {

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
     * Bad rule position: %d
     */
    BAD_POSITION("Bad rule position: %d", CATEGORY_ERROR, 7),
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
    /**
     * No active sieve script found
     */
    NO_ACTIVE_SCRIPT("No active sieve script found.", CATEGORY_ERROR, 22),
    /**
     * The redirect address \"%1$s\" is not valid.
     */
    INVALID_REDIRECT_ADDRESS(INVALID_REDIRECT_ADDRESS_MSG, INVALID_REDIRECT_ADDRESS_MSG, CATEGORY_USER_INPUT, 23),
    /**
     * The redirect address \"%1$s\" has been rejected.
     */
    REJECTED_REDIRECT_ADDRESS(REJECTED_REDIRECT_ADDRESS_MSG, REJECTED_REDIRECT_ADDRESS_MSG, CATEGORY_USER_INPUT, 24),
    /**
     * Invalid SIEVE rule specified. JSON request body contains an empty JSON array: %1$s
     */
    INVALID_SIEVE_RULE("Invalid SIEVE rule specified. JSON request body contains an empty JSON array: %1$s", INVALID_SIEVE_RULE_MSG, CATEGORY_USER_INPUT, 25),
    /**
     * Invalid SIEVE rule specified. Server response: %1$s
     */
    INVALID_SIEVE_RULE2("Invalid SIEVE rule specified. Server response: %1$s", INVALID_SIEVE_RULE2_MSG, CATEGORY_USER_INPUT, 25), // Yapp,
                                                                                                                                  // the
                                                                                                                                  // same
                                                                                                                                  // error
                                                                                                                                  // code
    /**
     * Invalid credentials
     */
    INVALID_FILTER_TYPE_FLAG("Invalid filter type flag: %1$s", CATEGORY_ERROR, 26),
    /**
     * Mailfilter not available for user %1$d in context %1$d.
     */
    MAILFILTER_NOT_AVAILABLE("Mailfilter not available for user %1$d in context %1$d.", MAILFILTER_NOT_AVAILABLE_MSG, CATEGORY_PERMISSION_DENIED, 27),
    /**
     * The specified 'reorder' array is invalid as the size (%1$d) exceeds the amount (%2$d) of rules for user '%3$d' in context '%4$d'. 
     */
    INVALID_REORDER_ARRAY("The specified 'reorder' array is invalid as the size (%1$d) exceeds the amount (%2$d) of rules for user '%3$d' in context '%4$d'.", CATEGORY_ERROR, 28),
    ;

    private final String message;

    private final String displayMessage;

    private final int detailNumber;

    private final Category category;

    /**
     * Initializes a new {@link MailFilterExceptionCode}.
     *
     * @param message The (technical) error message
     * @param category The category
     * @param detailNumber The detail number
     */
    private MailFilterExceptionCode(final String message, final Category category, final int detailNumber) {
        this(message, OXExceptionStrings.MESSAGE, category, detailNumber);
    }

    /**
     * Initializes a new {@link MailFilterExceptionCode} containing a display message for the user.
     *
     * @param message The (technical) error message
     * @param displayMessage The display message for the enduser
     * @param category The category
     * @param detailNumber The detail number
     */
    private MailFilterExceptionCode(String message, String displayMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage;
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
    public String getDisplayMessage() {
        return displayMessage;
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

    /**
     * The SIEVE parser is not very expressive when it comes to exceptions. This method analyses an exception message and throws a more
     * detailed one if possible.
     *
     * @param e the OXSieveHandlerException
     * @param credentials the user credentials
     * @param useSIEVEResponseCodes flag to communicate the condition of whether or not to use the sieve response codes
     * @return
     */
    public static OXException handleParsingException(final OXSieveHandlerException e, final Credentials credentials, final boolean useSIEVEResponseCodes) {
        final String message = e.toString();

        if (message.contains("unexpected SUBJECT")) {
            return MailFilterExceptionCode.EMPTY_MANDATORY_FIELD.create(e, "ADDRESS (probably)");
        }
        if (message.contains("address ''")) {
            return MailFilterExceptionCode.EMPTY_MANDATORY_FIELD.create(e, "ADDRESS");
        }

        if (useSIEVEResponseCodes) {
            final SieveResponse.Code code = e.getSieveResponseCode();
            if (null != code) {
                return new OXException(
                    code.getDetailnumber(),
                    code.getMessage(),
                    e.getSieveHost(),
                    Integer.valueOf(e.getSieveHostPort()),
                    credentials.getRightUsername(),
                    credentials.getContextString()).addCategory(sieveResponse2OXCategory(code)).setPrefix("MAIL_FILTER");
            }

            if (e.isParseError()) {
                return MailFilterExceptionCode.INVALID_SIEVE_RULE2.create(e, saneMessage(e.getMessage()));
            }

            return MailFilterExceptionCode.SIEVE_COMMUNICATION_ERROR.create(
                e,
                e.getSieveHost(),
                Integer.valueOf(e.getSieveHostPort()),
                credentials.getRightUsername(),
                credentials.getContextString());
        }

        if (e.isParseError()) {
            return MailFilterExceptionCode.INVALID_SIEVE_RULE2.create(e, saneMessage(e.getMessage()));
        }

        return MailFilterExceptionCode.SIEVE_COMMUNICATION_ERROR.create(
            e,
            e.getSieveHost(),
            Integer.valueOf(e.getSieveHostPort()),
            credentials.getRightUsername(),
            credentials.getContextString());
    }

    private static final Pattern CONTROL = Pattern.compile("[\\x00-\\x1F\\x7F]+");

    /**
     * Sanitize message
     *
     * @param message
     * @return
     */
    private static String saneMessage(final String message) {
        if (Strings.isEmpty(message)) {
            return "";
        }

        return CONTROL.matcher(message).replaceAll(" ");
    }

    /**
     * Match the SieveResponse to an OXCategory
     *
     * @param code
     * @return
     */
    private static Category sieveResponse2OXCategory(final SieveResponse.Code code) {
        switch (code) {
        case ENCRYPT_NEEDED:
        case QUOTA:
        case REFERRAL:
        case SASL:
        case TRANSITION_NEEDED:
        case TRYLATER:
        case ACTIVE:
        case ALREADYEXISTS:
        case NONEXISTENT:
        case TAG:
            break;
        case WARNINGS:
            return Category.CATEGORY_USER_INPUT;

        default:
            break;
        }
        return Category.CATEGORY_ERROR;
    }

    /**
     * Handle a SieveException
     *
     * @param e the SieveException
     * @return an OXException
     */
    public static OXException handleSieveException(final SieveException e) {
        final String msg = e.getMessage();
        return MailFilterExceptionCode.SIEVE_ERROR.create(e, msg);
    }

    public static String getNANString(final NumberFormatException nfe) {
        final String msg = nfe.getMessage();
        if (msg != null && msg.startsWith("For input string: \"")) {
            return msg.substring(19, msg.length() - 1);
        }
        return msg;
    }

}
