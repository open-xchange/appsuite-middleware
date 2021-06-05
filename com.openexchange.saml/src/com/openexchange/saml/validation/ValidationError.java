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

package com.openexchange.saml.validation;


/**
 * Indicates validation errors of {@link ResponseValidator}s and {@link AssertionValidator}s.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ValidationError {

    private final ValidationFailedReason reason;

    private final String message;

    private final Throwable throwable;

    /**
     * Initializes a new {@link ValidationError}.
     * @param reason
     * @param message
     */
    public ValidationError(ValidationFailedReason reason, String message) {
        super();
        this.reason = reason;
        this.message = message;
        throwable = null;
    }

    /**
     * Initializes a new {@link ValidationError}.
     * @param reason
     * @param message
     */
    public ValidationError(ValidationFailedReason reason, String message, Throwable throwable) {
        super();
        this.reason = reason;
        this.message = message;
        this.throwable = throwable;
    }

    public ValidationFailedReason getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public ValidationException toValidationException() {
        return new ValidationException(reason, message, throwable);
    }

}
