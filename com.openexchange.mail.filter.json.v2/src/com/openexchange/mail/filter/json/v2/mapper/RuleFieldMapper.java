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

package com.openexchange.mail.filter.json.v2.mapper;

import org.apache.jsieve.SieveException;
import org.json.JSONException;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.mail.filter.json.v2.json.fields.RuleField;
import com.openexchange.tools.session.ServerSession;

/**
 *
 * {@link RuleFieldMapper}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface RuleFieldMapper {

    /**
     * Returns the attribute name
     *
     * @return the attribute name
     */
    RuleField getAttributeName();

    /**
     * Verifies whether the specified rule is <code>null</code>
     *
     * @param rule The rule to verify
     * @return true if the object {@link T} is <code>null</code>; false otherwise
     */
    boolean isNull(Rule rule);

    /**
     * Gets the attribute of the specified {@link T} object
     *
     * @param rule The rule to get the attribute from
     * @return The attribute
     * @throws JSONException If a JSON error occurs
     * @throws OXException If a semantic error occurs
     */
    Object getAttribute(Rule rule) throws JSONException, OXException;

    /**
     * Sets the specified attribute to the specified {@link T} object
     *
     * @param rule The rule to set the attribute to
     * @param attribute The attribute to set to the rule
     * @param session The session
     * @throws JSONException If a JSON error occurs
     * @throws SieveException If a Sieve parsing error occurs
     * @throws OXException If an error occurs
     */
    void setAttribute(Rule rule, Object attribute, ServerSession session) throws JSONException, SieveException, OXException;
}
