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

package com.openexchange.ajax.mail.filter.api.dao.test;

import com.openexchange.ajax.mail.filter.api.dao.TestCommand;
import com.openexchange.ajax.mail.filter.api.dao.comparison.Comparison;
import com.openexchange.ajax.mail.filter.api.dao.comparison.argument.ComparisonArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.argument.TestArgument;

/**
 * {@link Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface Test<T extends TestArgument> {

    /**
     * Returns the {@link TestCommand}
     * 
     * @return the {@link TestCommand}
     */
    TestCommand getTestCommand();

    /**
     * Sets the specified {@link Comparison} to the {@link Test}
     * 
     * @param comparison The {@link Comparison} to set
     */
    void setComparison(Comparison<? extends ComparisonArgument> comparison);

    /**
     * Returns the {@link Comparison}
     * 
     * @return the {@link Comparison}
     */
    Comparison<? extends ComparisonArgument> getComparison();

    /**
     * Sets the value for the specified {@link TestArgument}
     * 
     * @param argument The {@link TestArgument} for which to set the value
     * @param value The value of the {@link TestArgument} to set
     */
    void setTestArgument(T argument, Object value);

    /**
     * Returns the value of the specified {@link TestArgument}
     * 
     * @param argument The {@link TestArgument} to return
     * @return The value of the {@link TestArgument}
     */
    Object getTestArgument(T argument);
}
