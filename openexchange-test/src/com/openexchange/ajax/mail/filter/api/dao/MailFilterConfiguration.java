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
 * {@link MailFilterConfiguration}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailFilterConfiguration {

    private final List<TestCondition> tests;
    private final List<ActionCommand> actionCommands;

    /**
     * Initialises a new {@link MailFilterConfiguration}.
     */
    public MailFilterConfiguration(List<TestCondition> tests, List<ActionCommand> actionCommands) {
        super();
        this.tests = tests;
        this.actionCommands = actionCommands;
    }

    /**
     * Gets the tests
     *
     * @return The tests
     */
    public List<TestCondition> getTests() {
        return Collections.unmodifiableList(tests);
    }

    /**
     * Gets the actionCommands
     *
     * @return The actionCommands
     */
    public List<ActionCommand> getActionCommands() {
        return Collections.unmodifiableList(actionCommands);
    }
}
