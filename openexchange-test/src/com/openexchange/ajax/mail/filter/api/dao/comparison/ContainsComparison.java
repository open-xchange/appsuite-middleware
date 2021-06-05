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
import com.openexchange.ajax.mail.filter.api.dao.comparison.argument.ContainsComparisonArgument;

/**
 * {@link ContainsComparison}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ContainsComparison extends AbstractComparison implements Comparison<ContainsComparisonArgument> {

    /**
     * Initialises a new {@link ContainsComparison}.
     */
    public ContainsComparison() {
        super(MatchType.contains);
    }

    @Override
    public MatchType getMatchType() {
        return MatchType.contains;
    }

    @Override
    public void setArgument(ContainsComparisonArgument argument, Object value) {
        throw new UnsupportedOperationException("The '" + MatchType.contains + "' comparison type can not have any arguments");
    }

    @Override
    public Object getArgument(ContainsComparisonArgument argument) {
        throw new UnsupportedOperationException("The '" + MatchType.contains + "' comparison type does not have any arguments");
    }
}
