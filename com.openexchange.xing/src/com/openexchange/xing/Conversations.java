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

package com.openexchange.xing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.xing.exception.XingException;

/**
 * {@link Conversations} - Represents a XING conversations response.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Conversations {

    private final int total;
    private int unreadCount;
    private final List<Conversation> items;

    /**
     * Initializes a new {@link Conversations}.
     */
    public Conversations(int total, List<Conversation> items) {
        super();
        this.total = total;
        this.items = items;
    }

    /**
     * Initializes a new {@link Conversations}.
     *
     * @throws XingException If initialization fails
     */
    public Conversations(final JSONObject conversationsInformation) throws XingException {
        super();
        this.total = conversationsInformation.optInt("total", 0);
        this.unreadCount = conversationsInformation.optInt("unread_count", 0);
        if (conversationsInformation.hasAndNotNull("items")) {
            final JSONArray itemsInformation = conversationsInformation.optJSONArray("items");
            final int length = itemsInformation.length();
            this.items = new ArrayList<Conversation>(length);
            for (int i = 0; i < length; i++) {
                items.add(new Conversation(itemsInformation.optJSONObject(i)));
            }
        } else {
            items = Collections.emptyList();
        }
    }

    /**
     * Gets the total
     *
     * @return The total
     */
    public int getTotal() {
        return total;
    }

    /**
     * Gets the unread count
     *
     * @return The unread count
     */
    public int getUnreadCount() {
        return unreadCount;
    }

    /**
     * Sets the unread count.
     *
     * @param unreadCount The unread count
     */
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    /**
     * Gets the items
     *
     * @return The items
     */
    public List<Conversation> getItems() {
        return items;
    }

}
