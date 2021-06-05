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

package com.openexchange.ajax.fields;

import static com.openexchange.tools.Collections.newHashMap;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import com.openexchange.groupware.search.Order;

/**
 * Class for converting AJAX GUI order strings into an Order object and vice
 * versa.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class OrderFields {

    private static final Map<Order, String> WRITE_MAP;

    private static final Map<String, Order> PARSE_MAP;

    /**
     * Prevent instantiation.
     */
    private OrderFields() {
        super();
    }

    /**
     * Converts the order enum into the corresponding GUI string.
     * @param order order enum value.
     * @return the corresponding GUI string.
     */
    public static String write(final Order order) {
        final String retval;
        switch (order) {
        case NO_ORDER:
            retval = null;
            break;
        default:
            retval = WRITE_MAP.get(order);
        }
        return retval;
    }

    /**
     * Parses the order string of the GUI.
     * @param order order string sent by GUI.
     * @return parsed {@link Order} or <code>null</code> if the order string
     * can't be parsed.
     */
    public static Order parse(final String order) {
        final Order retval;
        if (null == order || !PARSE_MAP.containsKey(order)) {
            retval = Order.NO_ORDER;
        } else {
            retval = PARSE_MAP.get(order);
        }
        return retval;
    }

    static {
        WRITE_MAP = new EnumMap<Order, String>(Order.class);
        WRITE_MAP.put(Order.ASCENDING, "asc");
        WRITE_MAP.put(Order.DESCENDING, "desc");
        final Order[] values = Order.values();
        final Map<String, Order> tmp = newHashMap(values.length);
        for (final Order order : values) {
            tmp.put(WRITE_MAP.get(order), order);
        }
        PARSE_MAP = Collections.unmodifiableMap(tmp);
    }
}
