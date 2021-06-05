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

package com.openexchange.tools.exceptions;

import com.openexchange.exception.OXException;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class SimpleTruncatedAttribute implements OXException.Truncated {

    private final int length;

    private final int maxSize;

    private final int id;

    private final String value;

    public SimpleTruncatedAttribute(final int id, final int maxSize, final int length) {
        this(id, maxSize, length, null);
    }

    public SimpleTruncatedAttribute(final int id, final int maxSize, final int length, final String value) {
        this.id = id;
        this.maxSize = maxSize;
        this.length = length;
        this.value = value;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public int getLength() {
        return length;
    }

    public String getValue() {
    	return value;
    }
}
