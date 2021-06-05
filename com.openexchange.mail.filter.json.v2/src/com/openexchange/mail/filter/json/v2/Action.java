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

package com.openexchange.mail.filter.json.v2;

import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 * Enumeration of all possible mail filter module actions.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum Action {
    /**
     * Get config object
     */
    CONFIG("config"),
    /**
     * Uploads a new object to the server.
     */
    NEW("new"),
    /**
     * Reorders the rules on the server side
     */
    REORDER("reorder"),
    /**
     * Updates a single object.
     */
    UPDATE("update"),
    /**
     * Delete a single object.
     */
    DELETE("delete"),
    /**
     * Returns a list of a list of requested attributes. Request must contain
     * a list of identifier of objects that attributes should be returned.
     */
    LIST("list"),
    /**
     * Deletes the whole script
     */
    DELETESCRIPT("deletescript"),
    /**
     * Gets the whole script as text
     */
    GETSCRIPT("getscript"),
    /**
     * Applies a given script to a mail folder
     */
    APPLY("apply");

    private final String ajaxName;

    private Action(final String name) {
        this.ajaxName = name;
    }

    /**
     * Gets the AJAX name for this action.
     *
     * @return The name
     */
    public String getAjaxName() {
        return ajaxName;
    }

    // -----------------------------------------------------------------------------------

    private static final Map<String, Action> name2Action;

    /**
     * Gets the action by specified name.
     *
     * @param ajaxName The name to look-up
     * @return The associated action or <code>null</code>
     */
    public static Action byName(final String ajaxName) {
        return name2Action.get(ajaxName);
    }

    static {
        final ImmutableMap.Builder<String, Action> tmp = ImmutableMap.builder();
        for (final Action action : values()) {
            tmp.put(action.getAjaxName(), action);
        }
        name2Action = tmp.build();
    }
}
