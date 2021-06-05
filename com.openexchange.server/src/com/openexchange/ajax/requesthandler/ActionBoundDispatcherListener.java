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

package com.openexchange.ajax.requesthandler;

import java.util.Set;
import com.openexchange.java.Strings;

/**
 * {@link ActionBoundDispatcherListener} - A dispatcher listener bound to certain actions and/or module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public abstract class ActionBoundDispatcherListener implements DispatcherListener {

    /**
     * Initializes a new {@link ActionBoundDispatcherListener}.
     */
    protected ActionBoundDispatcherListener() {
        super();
    }
    
    @Override
    public boolean applicable(AJAXRequestData requestData) {
        String module = getModule();
        if (false == DispatcherListeners.equalsModule(module, requestData)) {
            return false;
        }

        String action = requestData.getAction();
        if (Strings.isEmpty(action)) {
            // No further chance to test if applicable; at least module matches
            return true;
        }

        Set<String> actions = getActions();
        return null == actions || actions.isEmpty() ? true : actions.contains(action);
    }

    /**
     * Gets the actions of interest.
     *
     * @return The action wrapped by a {@link Set set} or <code>null</code> if there are no specific action this listener is interested in
     */
    public abstract Set<String> getActions();

    /**
     * Gets the module of interest.
     *
     * @return The module; must not be <code>null</code>!
     */
    public abstract String getModule();
}
