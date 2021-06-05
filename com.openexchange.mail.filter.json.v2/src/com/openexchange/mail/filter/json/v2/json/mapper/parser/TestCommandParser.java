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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.test.ITestCommand;

/**
 * {@link TestCommandParser}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.4
 */
public interface TestCommandParser<T> extends CommandParser<T> {

    /**
     * Retrieves the corresponding {@link ITestCommand}
     *
     * @return The {@link ITestCommand}
     * @throws OXException if an error is occurred
     */
    ITestCommand getCommand() throws OXException;

    /**
     * Parses the specified {@link T} object to the specified {@link JSONObject}
     *
     * @param jsonObject The {@link JSONObject} to parse the {@link T} object into
     * @param command The {@link T} to parse
     * @param transformToNotMatcher Indicates whether the matchers should be parsed to not matchers or not.
     * @throws JSONException if a JSON parsing error occurs
     * @throws OXException if a semantic error occurs
     */
    void parse(JSONObject jsonObject, T command, boolean transformToNotMatcher) throws JSONException, OXException;

}
