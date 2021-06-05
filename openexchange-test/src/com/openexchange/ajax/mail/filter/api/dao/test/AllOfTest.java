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
import com.openexchange.ajax.mail.filter.api.dao.test.argument.AllOfTestArgument;

/**
 * {@link AllOfTest}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AllOfTest extends AbstractTest implements Test<AllOfTestArgument> {

    /**
     * Initialises a new {@link AllOfTest}.
     * 
     * @param testCommand
     */
    public AllOfTest(Test<?>[] tests) {
        super(TestCommand.ALLOF);
        setTestArgument(AllOfTestArgument.tests, tests);
    }

    @Override
    public TestCommand getTestCommand() {
        return TestCommand.ALLOF;
    }

    @Override
    public void setComparison(Comparison<? extends ComparisonArgument> comparison) {
        this.comparison = comparison;
    }

    @Override
    public Comparison<? extends ComparisonArgument> getComparison() {
        return comparison;
    }

    @Override
    public void setTestArgument(AllOfTestArgument argument, Object value) {
        addArgument(argument, value);
    }

    @Override
    public Object getTestArgument(AllOfTestArgument argument) {
        return getArguments().get(argument);
    }
}
