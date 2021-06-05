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

package com.openexchange.chronos.common.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.EventUpdates;
import com.openexchange.groupware.tools.mappings.common.AbstractSimpleCollectionUpdate;

/**
 * {@link AbstractEventUpdates}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractEventUpdates extends AbstractSimpleCollectionUpdate<Event> implements EventUpdates {

    private final List<EventUpdate> updatedItems;

    /**
     * Initializes a new event updates collection based on the supplied original and updated event lists.
     * <p/>
     * Event matching is performed on one or more event fields.
     *
     * @param originalItems The original collection of events, or <code>null</code> if there is none
     * @param newItems The new collection of events, or <code>null</code> if there is none
     * @param considerUnset <code>true</code> to also consider comparison with not <i>set</i> fields of the original, <code>false</code>, otherwise
     * @param ignoredFields Fields to ignore when determining the differences between updated items
     * @return The event updates
     * @see EventMapper#equalsByFields(Event, Event, EventField...)
     */
    public AbstractEventUpdates(List<Event> originalItems, List<Event> newItems, boolean considerUnset, EventField[] ignoredFields) {
        super(originalItems, newItems);
        if (null != originalItems && null != newItems) {
            updatedItems = new ArrayList<EventUpdate>();
            for (Event newItem : newItems) {
                Event originalItem = find(originalItems, newItem);
                if (null != originalItem) {
                    EventUpdate eventUpdate = new DefaultEventUpdate(originalItem, newItem, considerUnset, ignoredFields);
                    if (0 < eventUpdate.getUpdatedFields().size()) {
                        updatedItems.add(eventUpdate);
                    }
                }
            }
        } else {
            updatedItems = Collections.emptyList();
        }
    }

    @Override
    public List<EventUpdate> getUpdatedItems() {
        return updatedItems;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && updatedItems.isEmpty();
    }

    @Override
    public String toString() {
        return "EventUpdates [" + removedItems.size() + " removed, " + addedItems.size() + " added, " + updatedItems.size() + " updated]";
    }

}
