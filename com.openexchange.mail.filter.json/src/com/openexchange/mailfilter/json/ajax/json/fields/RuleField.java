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

package com.openexchange.mailfilter.json.ajax.json.fields;

import java.util.List;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.TestCommand;

/**
 * {@link RuleField}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum RuleField {
    id(Integer.class),
    position(Integer.class),
    rulename(String.class),
    active(Boolean.class),
    flags(List.class),
    test(TestCommand.class),
    actioncmds(ActionCommand.class),
    text(String.class),
    errormsg(String.class);

    private final Class<?> jsonType;

    /**
     * Initialises a new {@link RuleField}.
     */
    private RuleField(Class<?> jsonType) {
        this.jsonType = jsonType;
    }

    /**
     * Returns the jsonType
     * 
     * @return the jsonType
     */
    public Class<?> getJSONType() {
        return jsonType;
    }
}
