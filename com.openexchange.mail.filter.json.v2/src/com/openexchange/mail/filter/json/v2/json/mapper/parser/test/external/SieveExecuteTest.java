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

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.JSONMatchType;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SieveExecuteTest}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public interface SieveExecuteTest {

    /**
     * Returns the name of the test
     *
     * @return The name of the test
     */
    public String getJsonName();

    /**
     * Check if the command is supported with the available capabilities
     *
     * @param capabilities Sieve capabilities
     * @return <code>true</code> if supported, <code>false</code> otherwise
     * @throws OXException
     */
    public boolean isCommandSupported(Set<String> capabilities) throws OXException;

    /**
     * Checks whether the given execute {@link TestCommand} is applicable or not
     *
     * @param command The command to be checked
     * @return <code>true</code> if applicable, <code>false</code> otherwise
     * @throws OXException
     */
    public boolean isApplicable(TestCommand command) throws OXException;

    /**
     * Populate the jsonObject based on the command
     * Should only be called if isApplicable was called first and applies
     *
     * @param jsonObject to populate
     * @param command The {@link TestCommand}
     * @param transformToNotMatcher
     * @throws OXException
     */
    public void parse(JSONObject jsonObject, TestCommand command, boolean transformToNotMatcher) throws OXException;

    /**
     * Checks whether the JSONObject refers to this command or not
     *
     * @param jsonObject The {@link JSONObject} to test
     * @return <code>true</code> if applies, <code>false</code> otherwise
     * @throws OXException
     */
    boolean isApplicable(JSONObject jsonObject) throws OXException;

    /**
     * Create a TestCommand from the jsonObject and ServerSession
     *
     * @param jsonObject
     * @param session The active user session
     * @return <code>true</code> if applies, <code>false</code> otherwise
     * @throws OXException
     */
    TestCommand parse(JSONObject jsonObject, ServerSession session) throws OXException;

    /**
     * Gets a map of arguments for this execute test
     *
     * @return The arguments
     */
    Map<String, String> getOtherArguments();

    /**
     * Gets the match types for this execute test
     *
     * @return The match types
     */
    Map<String, String> getMatchTypes();

    /**
     * Gets the json match types for this test
     * getJsonMatchTypes
     *
     * @return The json match types
     */
    List<JSONMatchType> getJsonMatchTypes();

}
