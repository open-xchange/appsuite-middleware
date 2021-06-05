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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.action.external;

import java.util.Hashtable;
import java.util.Set;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SieveFilterAction} Interface for an exterenal sieve plugin filter action
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public interface SieveFilterAction {

    /**
     * Return the name for this action
     *
     * @return The json name of this action
     */
    String getJsonName();

    /**
     * Check that the command is supported. Check against capabilities
     *
     * @param capabilities List of Sieve capabilities
     * @return <code>true</code> if supported, <code>false</code> otherwise
     * @throws OXException
     */
    boolean isCommandSupported(Set<String> capabilities) throws OXException;

    /**
     * Checks whether the given filter {@link TestCommand} is applicable or not
     *
     * @param actionCommand to check
     * @return <code>true</code> if applicable, <code>false</code> otherwise
     * @throws OXException
     */
    boolean isApplicable(ActionCommand actionCommand) throws OXException;

    /**
     * Parse the actionCommand and return the jsonObject with proper name.
     * Should only call if isApplicable returned <code>true</code> for the action command
     *
     * @param jsonObject to populate
     * @param actionCommand the {@link ActionCommand} to parse
     * @throws OXException
     */
    void parse(JSONObject jsonObject, ActionCommand actionCommand) throws OXException;

    /**
     * Checks whether the jsonObject refers to this action
     *
     * @param jsonObject to parse
     * @return <code>true</code> if applicable, <code>false</code> otherwise
     * @throws OXException
     */
    boolean isApplicable(JSONObject jsonObject) throws OXException;

    /**
     * Parses the ActionCommand from the given jsonObject
     *
     * @param jsonObject containing ID and other parameters
     * @param session current session
     * @return The action command
     * @throws OXException
     */
    ActionCommand parse(JSONObject jsonObject, ServerSession session) throws OXException;

    /**
     * Gets the tag arguments for the action
     *
     * @return HashTable of tag arguments
     */
    Hashtable<String, Integer> getTagArgs();

}
