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

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractSimplifiedMatcherAwareCommandParser}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public abstract class AbstractSimplifiedMatcherAwareCommandParser extends AbstractTestCommandParser {

    /**
     * Initializes a new {@link AbstractSimplifiedMatcherAwareCommandParser}.
     * @param services
     * @param testCommand
     */
    protected AbstractSimplifiedMatcherAwareCommandParser(ServiceLookup services, Commands testCommand) {
        super(services, testCommand);
    }

    /**
     * Handles simplified matchers like "startswith" and "endwith"
     *
     * @param matcher The matcher name
     * @param argList The argument list
     * @param data The input data
     * @throws JSONException
     * @throws OXException
     */
    abstract void handleSimplifiedMatcher(String matcher, List<Object> argList, JSONObject data) throws JSONException, OXException;

}
