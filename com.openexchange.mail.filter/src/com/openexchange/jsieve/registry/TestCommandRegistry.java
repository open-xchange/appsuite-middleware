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

package com.openexchange.jsieve.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.test.ITestCommand;
import com.openexchange.jsieve.registry.exception.CommandParserExceptionCodes;

/**
 * {@link TestCommandRegistry}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TestCommandRegistry implements CommandRegistry<ITestCommand> {

    private final Map<String, ITestCommand> testCommands;

    /**
     * Initialises a new {@link TestCommandRegistry}.
     */
    public TestCommandRegistry() {
        super();
        testCommands = new HashMap<>();
    }

    @Override
    public void register(String key, ITestCommand command) {
        testCommands.put(key, command);
    }

    @Override
    public void unregister(String key) {
        testCommands.remove(key);
    }

    @Override
    public ITestCommand get(String key) throws OXException {
        ITestCommand testCommand = testCommands.get(key);
        if (testCommand == null) {
            throw CommandParserExceptionCodes.UNKNOWN_PARSER.create(key);
        }
        return testCommand;
    }

    @Override
    public Collection<ITestCommand> getCommands() {
        return testCommands.values();
    }

    @Override
    public void purge() {
        testCommands.clear();
    }
}
