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

package com.openexchange.admin.exceptions;

/**
 * Class containing generic Exceptions
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a> ,
 * <a href="mailto:sebastian.kotyrba@open-xchange.com">Sebastian Kotyrba</a> ,
 * <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 */
public class OXGenericException extends Exception {

    private static final long serialVersionUID = 8013716815582743724L;

    /**
     * Value is null or empty
     */
    public static final String NULL_EMPTY   = "Value is null or empty";

    /**
     * General ERROR
     */
    public static final String GENERAL_ERROR   = "ERROR";

    /**
     * Key is missing
     */
    public static final String KEY_MISSING   = "Key is missing";

    /**
     * OX generic exceptions with various messages
     *
     * @see #NULL_EMPTY
     * @see #KEY_MISSING
     */
    public OXGenericException(String s) {
        super(s);
    }

    public OXGenericException(String message, Throwable cause) {
        super(message, cause);
    }
}
