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

package com.openexchange.java;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Lists}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class Lists {

    /**
     * Initializes a new {@link Lists}.
     */
    private Lists() {
        super();
    }

    /**
     * Converts an array to an {@link ArrayList}
     *
     * @param <T> The Type of the elements
     * @param elemets The elements to convert
     * @return A list
     */
    public static <T> List<T> toList(T... elemets) {
        if (null == elemets || elemets.length <= 0) {
            return new ArrayList<>(0);
        }
        ArrayList<T> list = new ArrayList<>(elemets.length);
        for (T t : elemets) {
            list.add(t);
        }
        return list;
    }

    /**
     * Ensures that each element added element is unique in the list
     *
     * @param <T> The type of element
     * @param list The list to add items to
     * @param itemsToAdd The items to add
     */
    public static <T> void addIfAbsent(List<T> list, T... itemsToAdd) {
        addIfAbsent(list, toList(itemsToAdd));
    }

    /**
     * Ensures that each element added element is unique in the list
     * 
     * @param <T> The type of element
     * @param list The list to add items to
     * @param itemsToAdd The items to add
     */
    public static <T> void addIfAbsent(List<T> list, List<T> itemsToAdd) {
        if (null == itemsToAdd || null == list) {
            return;
        }
        for (T object : itemsToAdd) {
            if (null != object && false == list.contains(object)) {
                list.add(object);
            }
        }
    }

}
