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

package com.openexchange.chronos.itip.generators;

import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;

/**
 * 
 * {@link ITipMailGeneratorFactory}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public interface ITipMailGeneratorFactory {

    /**
     * Creates an {@link ITipMailGenerator} for given events
     * 
     * @param original The original {@link Event}. Can be <code>null</code>
     * @param event The updated or new {@link Event}
     * @param session The current {@link CalendarSession}
     * @param onBehalfOfId The ID of the user to act on its behalf
     * @param principal The {@link CalendarUser}
     * @return An {@link ITipMailGenerator}
     * @throws OXException If generator or diff can't be build
     */
    ITipMailGenerator create(Event original, Event event, CalendarSession session, int onBehalfOfId, CalendarUser principal) throws OXException;

    /**
     * Creates an {@link ITipMailGenerator} for given events
     * 
     * @param original The original {@link Event}. Can be <code>null</code>
     * @param event The updated or new {@link Event}
     * @param session The current {@link CalendarSession}
     * @param onBehalfOfId The ID of the user to act on its behalf
     * @param principal The {@link CalendarUser}
     * @param comment An optional comment by the acting user
     * @return An {@link ITipMailGenerator}
     * @throws OXException If generator or diff can't be build
     */
    ITipMailGenerator create(Event original, Event event, CalendarSession session, int onBehalfOfId, CalendarUser principal, String comment) throws OXException;

}
