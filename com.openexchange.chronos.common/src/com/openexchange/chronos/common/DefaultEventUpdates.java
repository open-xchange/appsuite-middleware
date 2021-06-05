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

package com.openexchange.chronos.common;

import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.EventUpdates;

/**
 * {@link DefaultEventUpdates}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.6
 */
public class DefaultEventUpdates implements EventUpdates {

    private final List<Event> addedItems;
    private final List<Event> removedItems;
    private final List<EventUpdate> updatedItems;

    /**
     * Initializes a new {@link DefaultEventUpdates}.
     * 
     * @param addedItems The added events
     * @param removedItems The removed events
     * @param updatedItems The update events
     */
    public DefaultEventUpdates(List<Event> addedItems, List<Event> removedItems, List<EventUpdate> updatedItems) {
        super();
        this.addedItems = null != addedItems ? addedItems : Collections.emptyList();
        this.removedItems = null != removedItems ? removedItems : Collections.emptyList();
        this.updatedItems = null != updatedItems ? updatedItems : Collections.emptyList();
    }

    @Override
    public List<Event> getAddedItems() {
        return addedItems;
    }

    @Override
    public List<Event> getRemovedItems() {
        return removedItems;
    }

    @Override
    public boolean isEmpty() {
        return addedItems.isEmpty() && removedItems.isEmpty() && updatedItems.isEmpty();
    }

    @Override
    public List<EventUpdate> getUpdatedItems() {
        return updatedItems;
    }

    @Override
    public String toString() {
        return "EventUpdates [" + removedItems.size() + " removed, " + addedItems.size() + " added, " + updatedItems.size() + " updated]";
    }

}
