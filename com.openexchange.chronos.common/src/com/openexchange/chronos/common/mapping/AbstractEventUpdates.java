/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import com.openexchange.exception.OXException;

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
    public AbstractEventUpdates(List<Event> originalItems, List<Event> newItems, boolean considerUnset, EventField[] ignoredFields) throws OXException {
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
