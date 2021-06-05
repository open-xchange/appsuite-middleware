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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.test.simplified;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.exception.OXException;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.exceptions.CommandParserExceptionCodes;

/**
 * {@link SimplifiedHeaderTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.4
 */
public enum SimplifiedHeaderTest {
    Subject("subject", "Subject"),
    From("from", "From"),
    To("to", "To"),
    Cc("cc", "Cc"),
    AnyRecipient("anyrecipient", "to", "cc"),
    MailingList("mailinglist", "List-Id", "X-BeenThere", "X-Mailinglist", "X-Mailing-List");

    private static final Map<String, SimplifiedHeaderTest> map;
    static {
        Map<String, SimplifiedHeaderTest> m = new HashMap<>(8);
        for (SimplifiedHeaderTest sht : SimplifiedHeaderTest.values()) {
            m.put(sht.getCommandName(), sht);
        }
        map = ImmutableMap.copyOf(m);
    }

    private String commandName;
    private List<String> headerNames;

    SimplifiedHeaderTest(String commandName, String... headerNames) {
        this.commandName = commandName;
        this.headerNames = Arrays.asList(headerNames);
    }

    public String getCommandName() {
        return commandName;
    }

    /**
     * Retrieves the {@link SimplifiedHeaderTest} by name
     *
     * @param name The name
     * @return The {@link SimplifiedHeaderTest}
     * @throws OXException if no {@link SimplifiedHeaderTest} with this name exists
     */
    public static SimplifiedHeaderTest getTestByName(String name) throws OXException {
        SimplifiedHeaderTest simplifiedHeaderTest = map.get(name);
        if (simplifiedHeaderTest == null) {
            throw CommandParserExceptionCodes.UNKOWN_SIMPLIFIED_RULE.create(name);
        }
        return simplifiedHeaderTest;
    }

    /**
     * Retrieves the header names of this {@link SimplifiedHeaderTest}
     *
     * @return A list of header names
     */
    public List<String> getHeaderNames() {
        return this.headerNames;
    }
}
