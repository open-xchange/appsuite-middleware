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

package com.openexchange.ajax.mail.filter.api.dao.comparison;

import com.openexchange.ajax.mail.filter.api.dao.MatchType;
import com.openexchange.ajax.mail.filter.api.dao.comparison.argument.ComparisonArgument;

/**
 * {@link Comparison}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface Comparison<A extends ComparisonArgument> {

    /**
     * Returns the {@link MatchType}
     * 
     * @return the {@link MatchType}
     */
    MatchType getMatchType();

    /**
     * Sets the value for the specified {@link ComparisonArgument}
     * 
     * @param argument The {@link ComparisonArgument} for which to set the value
     * @param value The value of the {@link ComparisonArgument} to set
     */
    void setArgument(A argument, Object value);

    /**
     * Returns the value of the specified {@link ComparisonArgument}
     * 
     * @param argument The {@link ComparisonArgument} to return
     * @return The value of the {@link ComparisonArgument}
     */
    Object getArgument(A argument);
}
