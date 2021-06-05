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
import com.openexchange.jsieve.commands.test.IActionCommand;
import com.openexchange.jsieve.registry.exception.CommandParserExceptionCodes;

/**
 * {@link ActionCommandRegistry}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class ActionCommandRegistry implements CommandRegistry<IActionCommand> {

    private final Map<String, IActionCommand> actionCommands;

    /**
     * Initializes a new {@link ActionCommandRegistry}.
     */
    public ActionCommandRegistry() {
        super();
        actionCommands = new HashMap<>();
    }

    @Override
    public void register(String key, IActionCommand command) {
        actionCommands.put(key, command);
    }

    @Override
    public void unregister(String key) {
        actionCommands.remove(key);
    }

    @Override
    public IActionCommand get(String key) throws OXException {
        IActionCommand actionCommand = actionCommands.get(key);
        if (actionCommand == null) {
            throw CommandParserExceptionCodes.UNKNOWN_PARSER.create(key);
        }
        return actionCommand;
    }

    @Override
    public Collection<IActionCommand> getCommands() {
        return actionCommands.values();
    }

    @Override
    public void purge() {
        actionCommands.clear();
    }
}
