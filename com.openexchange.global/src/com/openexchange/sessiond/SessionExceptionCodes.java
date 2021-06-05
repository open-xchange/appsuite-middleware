/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.sessiond;

import static com.openexchange.exception.OXExceptionStrings.MESSAGE;
import static com.openexchange.exception.OXExceptionStrings.MESSAGE_RETRY;
import static com.openexchange.sessiond.SessionExceptionMessages.CONTEXT_LOCKED_MSG;
import static com.openexchange.sessiond.SessionExceptionMessages.KERBEROS_TICKET_MISSING_MSG;
import static com.openexchange.sessiond.SessionExceptionMessages.MAX_SESSION_EXCEPTION_MSG;
import static com.openexchange.sessiond.SessionExceptionMessages.MAX_SESSION_PER_CLIENT_EXCEPTION_MSG;
import static com.openexchange.sessiond.SessionExceptionMessages.MAX_SESSION_PER_USER_EXCEPTION_MSG;
import static com.openexchange.sessiond.SessionExceptionMessages.NO_SESSION_FOR_TOKENS_MSG;
import static com.openexchange.sessiond.SessionExceptionMessages.PASSWORD_UPDATE_FAILED_MSG;
import static com.openexchange.sessiond.SessionExceptionMessages.SESSION_EXPIRED_MSG;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.java.Strings;

/**
 * {@link SessionExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum SessionExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * Sessiond Exception
     */
    SESSIOND_EXCEPTION("Sessiond exception", MESSAGE, Category.CATEGORY_ERROR, 1),
    /**
     * Maximum number of sessions elapsed
     */
    MAX_SESSION_EXCEPTION("Maximum number of sessions elapsed", MAX_SESSION_EXCEPTION_MSG, Category.CATEGORY_ERROR, 2),
    /**
     * Sessiond Config Exception
     */
    SESSIOND_CONFIG_EXCEPTION("Sessiond Config Exception", MESSAGE, Category.CATEGORY_ERROR, 3),
    /**
     * Missing property '%s'
     */
    MISSING_PROPERTY("Missing property '%s'", MESSAGE, Category.CATEGORY_CONFIGURATION, 4),
    /**
     * Unknown event topic %s
     */
    UNKNOWN_EVENT_TOPIC("Unknown event topic %s", MESSAGE, Category.CATEGORY_ERROR, 5),
    /**
     * Password could not be changed
     */
    PASSWORD_UPDATE_FAILED("Password could not be changed", PASSWORD_UPDATE_FAILED_MSG, Category.CATEGORY_ERROR, 6),
    /**
     * Max. number of sessions exceeded for user %1$s in context %2$s
     */
    MAX_SESSION_PER_USER_EXCEPTION("Max. number of sessions exceeded for user %1$s in context %2$s", MAX_SESSION_PER_USER_EXCEPTION_MSG, Category.CATEGORY_USER_INPUT, 7),
    /**
     * Authentication identifier duplicate found. Existing session login: %1$s. Current denied login request: %2$s.
     */
    DUPLICATE_AUTHID("Authentication identifier duplicate found. Existing session login: %1$s. Current denied login request: %2$s.", MESSAGE_RETRY, Category.CATEGORY_ERROR, 8),
    /**
     * SessionD returned wrong session with identifier %1$s for given session identifier %2$s.
     */
    WRONG_SESSION("SessionD returned wrong session with identifier %1$s for given session identifier %2$s.", MESSAGE, Category.CATEGORY_ERROR, 9),
    /**
     * Got a collision while adding a new session to the session container. Colliding session has login %1$s and new session has login %2$s.
     */
    SESSIONID_COLLISION("Got a collision while adding a new session to the session container. Colliding session has login %1$s and new session has login %2$s.", MESSAGE_RETRY, Category.CATEGORY_ERROR, 10),
    /**
     * Received wrong session %1$s having random %2$s when looking for random %3$s and session %4$s.
     */
    WRONG_BY_RANDOM("Received wrong session %1$s having random %2$s when looking for random %3$s and session %4$s.", MESSAGE, Category.CATEGORY_ERROR, 11),
    /**
     * The session parameter is missing.
     */
    SESSION_PARAMETER_MISSING("The session parameter is missing.", MESSAGE,Category.CATEGORY_ERROR, 201),
    /**
     * Your session %s expired. Please start a new browser session.
     */
    SESSION_EXPIRED("Your session %s expired. Please start a new browser session.", SESSION_EXPIRED_MSG, Category.CATEGORY_TRY_AGAIN, 203),
    /**
     * Context %1$d (%2$s) is currently not enabled.
     */
    CONTEXT_LOCKED("Context %1$d (%2$s) is currently not enabled.", CONTEXT_LOCKED_MSG, Category.CATEGORY_TRY_AGAIN, 204),
    /**
     * Max. number of sessions exceeded for client %1$s of user %2$s in context %3$s
     */
    MAX_SESSION_PER_CLIENT_EXCEPTION("Max. number of sessions exceeded for client %1$s of user %2$s in context %3$s", MAX_SESSION_PER_CLIENT_EXCEPTION_MSG, Category.CATEGORY_ERROR, 207),
    /**
     * Session daemon is not initialized yet.
     * TODO Refactoring of the session daemon should make the service public only then, when it is completely initialized.
     */
    NOT_INITIALIZED("Session daemon is not initialized yet.", MESSAGE_RETRY, Category.CATEGORY_ERROR, 208),
    /**
     * Method not implemented.
     * Use this only internally for testing purposes.
     */
    NOT_IMPLEMENTED("Method not implemented.", MESSAGE, Category.CATEGORY_ERROR, 209),
    /**
     * Can not find a session for server token %1$s and client token %2$s.
     */
    NO_SESSION_FOR_SERVER_TOKEN("Can not find a session for server token %1$s and client token %2$s.", NO_SESSION_FOR_TOKENS_MSG, Category.CATEGORY_USER_INPUT, 210),
    /**
     * Can not find a session for server token %1$s and client token %2$s.
     */
    NO_SESSION_FOR_CLIENT_TOKEN("Can not find a session for server token %1$s and client token %2$s.", NO_SESSION_FOR_TOKENS_MSG, Category.CATEGORY_USER_INPUT, 211),
    /**
     * Kerberos ticket is missing in session %1$s.
     */
    KERBEROS_TICKET_MISSING("Kerberos ticket is missing in session %1$s.", KERBEROS_TICKET_MISSING_MSG, Category.CATEGORY_TRY_AGAIN, 212),
    /**
    /**
     * Failed to issue remote session removal for user %1$s in contexts %2$s on remote node %3$s.
     */
    REMOTE_SESSION_REMOVAL_FAILED("Failed to issue remote session removal for user %1$s in contexts %2$s on remote node %3$s.", MESSAGE, Category.CATEGORY_ERROR, 213);

    private static final String PREFIX = "SES";

    /**
     * Checks if specified {@code OXException}'s prefix is equal to this {@code OXExceptionCode} enumeration.
     *
     * @param e The {@code OXException} to check
     * @return <code>true</code> if prefix is equal; otherwise <code>false</code>
     */
    public static boolean hasPrefix(final OXException e) {
        if (null == e) {
            return false;
        }
        return PREFIX.equals(e.getPrefix());
    }

    /**
     * Gets the error prefix for these error codes.
     *
     * @return The error prefix
     */
    public static String getErrorPrefix() {
        return PREFIX;
    }

    /**
     * The special name of the OXExceotion property signaling the session expiration reason.
     */
    public static final String OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON = "com.openexchange.session.expiration.reason";

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String message;
    private final String displayMessage;
    private final Category category;
    private final int number;

    private SessionExceptionCodes(final String message, String displayMessage, final Category category, final int number) {
        this.message = message;
        this.displayMessage = displayMessage;
        this.category = category;
        this.number = number;
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
    public int getNumber() {
        return number;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public Category getCategory() {
        return category;
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
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, sanitize(args));
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, sanitize(args));
    }

    /**
     * Sanitizes the supplied exception message arguments.
     *
     * @param args The exception arguments to sanitize
     * @return The sanitized exception arguments
     */
    private Object[] sanitize(Object[] args) {
        if (null == args || 0 == args.length) {
            return args;
        }
        switch (this) {
            case SESSION_EXPIRED:
            case KERBEROS_TICKET_MISSING:
                args[0] = sanitizeSessionIdParamter(args[0]);
                break;
            case WRONG_SESSION:
                args[0] = sanitizeSessionIdParamter(args[0]);
                if (1 < args.length) {
                    args[1] = sanitizeSessionIdParamter(args[1]);
                }
                break;
            case WRONG_BY_RANDOM:
                args[0] = sanitizeSessionIdParamter(args[0]);
                if (3 < args.length) {
                    args[3] = sanitizeSessionIdParamter(args[3]);
                }
                break;
            default:
                break;
        }
        return args;
    }

    /**
     * Sanitizes the session identifier parameter to ensure it is at least in the expected format.
     *
     * @param sessionIdParameter The parameter to sanitize
     * @return The parameter, or a sane string if not in expected format
     */
    private static Object sanitizeSessionIdParamter(Object sessionIdParameter) {
        if (null != sessionIdParameter && String.class.isInstance(sessionIdParameter) && isNoValidSessionId((String) sessionIdParameter)) {
            return "<invalid_sessionid>";
        }
        return sessionIdParameter;
    }

    private static boolean isNoValidSessionId(String sessionId) {
        return isValidSessionId(sessionId) == false;
    }

    private static boolean isValidSessionId(String sessionId) {
        if (Strings.isEmpty(sessionId)) {
            return false;
        }

        int length = sessionId.length();
        if (length != 32) {
            return false;
        }

        for (int i = length; i-- > 0;) {
            char ch = sessionId.charAt(i);
            if (Strings.isHex(ch, false) == false) {
                return false;
            }
        }
        return true;
    }

}
