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

package com.openexchange.groupware.delete.contextgroup;

import java.util.EventObject;

/**
 * {@link DeleteContextGroupEvent}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DeleteContextGroupEvent extends EventObject {

    private static final long serialVersionUID = -2426655951674309859L;

    private String contextGroupId;

    /**
     * 
     * Initialises a new {@link DeleteContextGroupEvent}.
     * 
     * @param source The object on which the Event initially occurred
     * @param contextGroupId The context group identifier
     */
    public DeleteContextGroupEvent(Object source, String contextGroupId) {
        super(source);
        this.contextGroupId = contextGroupId;

    }

    /**
     * Returns the context group identifier
     * 
     * @return The context group identifier
     */
    public String getContextGroupId() {
        return contextGroupId;
    }
}
