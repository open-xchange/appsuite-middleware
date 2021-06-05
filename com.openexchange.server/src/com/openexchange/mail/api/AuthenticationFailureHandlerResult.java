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

package com.openexchange.mail.api;

import com.openexchange.exception.OXException;

/**
 * {@link AuthenticationFailureHandlerResult} - The result for a processed authentication failure.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class AuthenticationFailureHandlerResult {

    private static final AuthenticationFailureHandlerResult RESULT_RETRY = new AuthenticationFailureHandlerResult(Type.RETRY);
    private static final AuthenticationFailureHandlerResult RESULT_CONTINUE = new AuthenticationFailureHandlerResult(Type.CONTINUE);

    /**
     * The result's type for a processed authentication failure.
     */
    public static enum Type {
        /**
         * Simply go ahead with original processing (that is to throw originating authentication error).
         */
        CONTINUE,
        /**
         * Throw a certain exception (as given through {@link AuthenticationFailureHandlerResult#getError()}
         */
        EXCEPTION,
        /**
         * Authentication is supposed to be retried.
         */
        RETRY;
    }

    /**
     * Create the result signaling specified error.
     *
     * @return The result with given error contained
     */
    public static AuthenticationFailureHandlerResult createErrorResult(OXException e) {
        return new AuthenticationFailureHandlerResult(e);
    }

    /**
     * Create the result signaling to continue.
     *
     * @return The result
     */
    public static AuthenticationFailureHandlerResult createContinueResult() {
        return RESULT_CONTINUE;
    }

    /**
     * Create the result signaling to retry.
     *
     * @return The result
     */
    public static AuthenticationFailureHandlerResult createRetryResult() {
        return RESULT_RETRY;
    }

    // --------------------------------------------------------------------------------------------

    private final OXException exception;
    private final Type type;

    private AuthenticationFailureHandlerResult(Type type) {
        super();
        exception = null;
        this.type = type;
    }

    private AuthenticationFailureHandlerResult(OXException exception) {
        super();
        this.type = Type.EXCEPTION;
        this.exception = exception;
    }

    /**
     * Gets the error associated with this result.
     *
     * @return The error or <code>null</code> (if type is different from {@link Type#EXCEPTION})
     */
    public OXException getError() {
        return exception;
    }

    /**
     * Gets this result's type, which indicates how to proceed.
     *
     * @return The type
     */
    public Type getType() {
        return type;
    }

}
