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

package com.openexchange.file.storage.search;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;


/**
 * {@link AbstractNumberSearchTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractNumberSearchTerm implements SearchTerm<ComparablePattern<Number>> {

    /** The pattern */
    protected final ComparablePattern<Number> pattern;

    /**
     * Initializes a new {@link AbstractNumberSearchTerm}.
     */
    protected AbstractNumberSearchTerm(final ComparablePattern<Number> pattern) {
        super();
        this.pattern = pattern;
    }

    @Override
    public ComparablePattern<Number> getPattern() {
        return pattern;
    }

    @Override
    public boolean matches(final File file) throws OXException {
        final Number number = getNumber(file);
        if (null == number) {
            return false;
        }

        if (compareLongValues()) {
            switch (pattern.getComparisonType()) {
            case EQUALS:
                return number.longValue() == pattern.getPattern().longValue();
            case LESS_THAN:
                return number.longValue() < pattern.getPattern().longValue();
            case GREATER_THAN:
                return number.longValue() > pattern.getPattern().longValue();
            default:
                return false;
            }
        }

        switch (pattern.getComparisonType()) {
        case EQUALS:
            return number.intValue() == pattern.getPattern().intValue();
        case LESS_THAN:
            return number.intValue() < pattern.getPattern().intValue();
        case GREATER_THAN:
            return number.intValue() > pattern.getPattern().intValue();
        default:
            return false;
        }
    }

    /**
     * Gets the number to compare with.
     *
     * @param file The file to retreive the number from
     * @return The number
     */
    protected abstract Number getNumber(File file);

    /**
     * Signals whether to compare <code>long</code> value (default is <code>int</code>)
     *
     * @return <code>true</code> for <code>long</code> values; otherwise <code>false</code> for <code>int</code> ones
     */
    protected abstract boolean compareLongValues();

}
