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

package com.openexchange.ajax.mail.filter.api.dao;

import java.util.Collections;
import java.util.List;

/**
 * {@link TestCondition}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TestCondition {

    private final TestCommand testCommand;
    private final List<MatchType> comparisons;

    /**
     * Initialises a new {@link TestCondition}.
     */
    public TestCondition(TestCommand testCommand, List<MatchType> comparisons) {
        super();
        this.testCommand = testCommand;
        this.comparisons = Collections.unmodifiableList(comparisons);
    }

    /**
     * Gets the testCommand
     *
     * @return The testCommand
     */
    public TestCommand getTestCommand() {
        return testCommand;
    }

    /**
     * Gets the comparisons
     *
     * @return The comparisons
     */
    public List<MatchType> getComparisons() {
        return comparisons;
    }
}
