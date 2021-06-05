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

package com.openexchange.data.conversion.ical;

import com.openexchange.exception.OXException;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ConversionError extends ConversionWarning {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -8257376167243573600L;

    /**
     * @deprecated use {@link #ConversionError(int, Code, Object...)}.
     */
    @Deprecated
    public ConversionError(final int index, final String message, final Object... args) {
        super(index, message, args);
    }

    public ConversionError(final int index, final OXException cause) {
        super(index, cause);
    }

    public ConversionError(final int index, final Code code, final Object... args) {
        this(index, code, null, args);
    }

    public ConversionError(final int index, final Code code, final Throwable cause, final Object... args) {
        super(index, code, cause, args);
    }

}
