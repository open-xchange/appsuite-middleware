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

package com.openexchange.chronos.provider.caching.internal.handler.utils;

import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.EventUpdates;

/**
 * {@link EmptyUidUpdates}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class EmptyUidUpdates implements EventUpdates {

    private List<Event> addedItems;
    private List<Event> removedItems;

    public EmptyUidUpdates(List<Event> removedItems, List<Event> addedItems) {
        this.addedItems = addedItems;
        this.removedItems = removedItems;
    }

    @Override
    public List<Event> getAddedItems() {
        return this.addedItems;
    }

    @Override
    public List<Event> getRemovedItems() {
        return removedItems;
    }

    @Override
    public boolean isEmpty() {
        return this.addedItems.isEmpty() && this.removedItems.isEmpty();
    }

    @Override
    public List<EventUpdate> getUpdatedItems() {
        return Collections.emptyList();
    }

}
