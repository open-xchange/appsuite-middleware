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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.test.external;

import java.util.Optional;
import java.util.Set;
import org.apache.jsieve.SieveException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.IdAwareParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.AbstractTestCommandParser;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ExecuteTestParser}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class ExecuteTestParser extends AbstractTestCommandParser implements IdAwareParser {

    private final ExecuteTestRegistry registry;

    /**
     * Initializes a new {@link ExecuteTestParser}.
     *
     * @param services The {@link ServiceLookup}
     * @param registry The {@link ExecuteTestRegistry}
     */
    public ExecuteTestParser(ServiceLookup services, ExecuteTestRegistry registry) {
        super(services, registry);
        this.registry = registry;
    }

    @Override
    public boolean isCommandSupported(Set<String> capabilities, String id) throws OXException {
       return registry.isCommandSupported(capabilities, id);
    }

    @Override
    public void parse(JSONObject jsonObject, TestCommand command, boolean transformToNotMatcher) throws JSONException, OXException {
        Optional<SieveExecuteTest> parser = registry.getApplicableParser(command);
        if(parser.isPresent()) {
            parser.get().parse(jsonObject, command, transformToNotMatcher);
        }
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        Optional<SieveExecuteTest> parser = registry.getApplicableParser(jsonObject);
        if(parser.isPresent()) {
            return parser.get().parse(jsonObject, session);
        }
        throw OXException.general("No suitable parser found for this sieve execute action");
    }

}
