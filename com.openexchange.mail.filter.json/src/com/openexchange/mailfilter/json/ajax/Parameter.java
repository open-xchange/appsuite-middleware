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

package com.openexchange.mailfilter.json.ajax;

import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum Parameter {

    ACTION("action", true),
    USERNAME("username", false),
    FLAG("flag", false);

    private final String name;

    private final boolean required;

    private Parameter(final String name, final boolean required) {
        this.name = name;
        this.required = required;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the required
     */
    public final boolean isRequired() {
        return required;
    }

    private static final Map<String, Parameter> name2Parameter;

    public static Parameter byName(final String name) {
        return name2Parameter.get(name);
    }

    static {
        Map<String, Parameter> tmp = new HashMap<String, Parameter>(values().length, 1);
        for (Parameter action : values()) {
            tmp.put(action.getName(), action);
        }
        name2Parameter = ImmutableMap.copyOf(tmp);
    }

}
