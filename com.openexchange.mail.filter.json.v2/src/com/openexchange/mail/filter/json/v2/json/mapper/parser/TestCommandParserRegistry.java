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

package com.openexchange.mail.filter.json.v2.json.mapper.parser;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.exceptions.CommandParserExceptionCodes;

/**
 * {@link TestCommandParserRegistry}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public class TestCommandParserRegistry implements CommandParserRegistry<TestCommand, TestCommandParser<TestCommand>> {

    private final Map<String, TestCommandParser<TestCommand>> parsers;

    /**
     * Initialises a new {@link TestCommandParserRegistry}.
     */
    public TestCommandParserRegistry() {
        super();
        parsers = new HashMap<>();
    }

    @Override
    public void register(String key, TestCommandParser<TestCommand> parser) {
        parsers.put(key, parser);
    }

    @Override
    public void unregister(String key) {
        parsers.remove(key);
    }

    @Override
    public TestCommandParser<TestCommand> get(String key) throws OXException {
        TestCommandParser<TestCommand> parser = parsers.get(key);
        if (parser == null) {
            throw CommandParserExceptionCodes.UNKNOWN_PARSER.create(key);
        }
        return parser;
    }

    @Override
    public void purge() {
        parsers.clear();
    }

    @Override
    public Map<String, TestCommandParser<TestCommand>> getCommandParsers() {
        return parsers;
    }
}
