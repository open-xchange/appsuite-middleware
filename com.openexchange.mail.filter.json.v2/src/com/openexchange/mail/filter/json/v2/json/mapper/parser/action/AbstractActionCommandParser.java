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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.action;

import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.test.IActionCommand;
import com.openexchange.jsieve.registry.ActionCommandRegistry;
import com.openexchange.jsieve.registry.CommandRegistry;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.AbstractCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.ActionCommandParser;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractActionCommandParser}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.4
 */
public abstract class AbstractActionCommandParser extends AbstractCommandParser<IActionCommand> implements ActionCommandParser<ActionCommand> {

    /**
     * Initializes a new {@link AbstractActionCommandParser}.
     */
    protected AbstractActionCommandParser(ServiceLookup services, IActionCommand actionCommand) {
        super(services, actionCommand);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> CommandRegistry<T> getCommandRegistry() {
        return (CommandRegistry<T>) services.getService(ActionCommandRegistry.class);
    }
}
