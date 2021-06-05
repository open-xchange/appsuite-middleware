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
 * This exception is thrown by {@link ValidationStrategy} in case the validation of a response failed.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ValidationException extends Exception {

    private static final long serialVersionUID = -2030486914619353014L;

    private final ValidationFailedReason reason;

    public ValidationException(ValidationFailedReason reason, String detailMessage) {
        super(detailMessage);
        this.reason = reason;
    }

    public ValidationException(ValidationFailedReason reason, String detailMessage, Throwable cause) {
        super(detailMessage, cause);
        this.reason = reason;
    }

    public ValidationFailedReason getReason() {
        return reason;
    }

}
