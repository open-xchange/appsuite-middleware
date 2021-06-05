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

package com.openexchange.exception.interception;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;

/**
 * {@link OXExceptionArguments} - Arguments to yield an appropriate {@link OXException}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXExceptionArguments {

    private final OXExceptionCode code;
    private final Category category;
    private final Throwable cause;
    private final Object[] args;

    /**
     * Initializes a new {@link OXExceptionArguments}.
     *
     * @param code The exception code
     * @param category The optional category to use
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     */
    public OXExceptionArguments(OXExceptionCode code, Category category, Throwable cause, Object... args) {
        super();
        this.code = code;
        this.category = category;
        this.cause = cause;
        this.args = args;
    }

    /**
     * Gets the code
     *
     * @return The code
     */
    public OXExceptionCode getCode() {
        return code;
    }

    /**
     * Gets the category
     *
     * @return The category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Gets the cause
     *
     * @return The cause
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Gets the arguments
     *
     * @return The arguments
     */
    public Object[] getArgs() {
        return args;
    }

}
