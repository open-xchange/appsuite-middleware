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
