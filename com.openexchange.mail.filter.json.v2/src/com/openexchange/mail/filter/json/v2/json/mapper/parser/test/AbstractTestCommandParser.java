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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.test;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.test.ITestCommand;
import com.openexchange.jsieve.registry.CommandRegistry;
import com.openexchange.jsieve.registry.TestCommandRegistry;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.AbstractCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParser;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractTestCommandParser}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.4
 */
public abstract class AbstractTestCommandParser extends AbstractCommandParser<ITestCommand> implements TestCommandParser<TestCommand> {

    /**
     * Initializes a new {@link AbstractTestCommandParser}.
     *
     * @param services The service lookup
     * @param testCommand The {@link ITestCommand}
     */
    protected AbstractTestCommandParser(ServiceLookup services, ITestCommand testCommand) {
        super(services, testCommand);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> CommandRegistry<T> getCommandRegistry() {
        return (CommandRegistry<T>) services.getService(TestCommandRegistry.class);
    }

    @Override
    public void parse(JSONObject jsonObject, TestCommand command) throws JSONException, OXException {
        this.parse(jsonObject, command, false);
    }

}
