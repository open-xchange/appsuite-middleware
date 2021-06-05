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

package com.openexchange.groupware.infostore.search;

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;


/**
 * {@link AbstractDateSearchTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractDateSearchTerm implements SearchTerm<ComparablePattern<Date>> {

    /** The pattern */
    protected final ComparablePattern<Date> pattern;

    /**
     * Initializes a new {@link AbstractDateSearchTerm}.
     */
    protected AbstractDateSearchTerm(final ComparablePattern<Date> pattern) {
        super();
        this.pattern = pattern;
    }

    @Override
    public ComparablePattern<Date> getPattern() {
        return pattern;
    }

    @Override
    public boolean matches(final DocumentMetadata file) throws OXException {
        final Date date = getDate(file);
        if (null == date) {
            return false;
        }

        switch (pattern.getComparisonType()) {
        case EQUALS:
            return date.getTime() == pattern.getPattern().getTime();
        case LESS_THAN:
            return date.getTime() < pattern.getPattern().getTime();
        case GREATER_THAN:
            return date.getTime() > pattern.getPattern().getTime();
        default:
            return false;
        }

    }

    /**
     * Gets the date to compare with.
     *
     * @param file The file to retrieve the date from
     * @return The date
     */
    protected abstract Date getDate(DocumentMetadata file);

//    protected Date addTimeZoneOffset(final long date, final TimeZone timeZone) {
//        return new Date(date + timeZone.getOffset(date));
//    }

}
