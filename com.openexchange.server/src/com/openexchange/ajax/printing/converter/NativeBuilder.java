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

package com.openexchange.ajax.printing.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * {@link NativeBuilder}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class NativeBuilder {
    private final Stack<Object> stack = new Stack<>();

    private Map<String, Object> currentMap;
    private List<Object> currentList;

    private Object current;
    private Object initial;

    private String key;

    /**
     * Initializes a new {@link NativeBuilder}.
     */
    public NativeBuilder() {
        super();
    }

    public NativeBuilder list() {
        if (current != null) {
            stack.push(current);
        }
        List<Object> newList = new ArrayList<Object>();

        if ( !isNil()) {
            value(newList);
        }
        current = currentList = newList;
        currentMap = null;

        if (initial == null) {
            initial = current;
        }
        return this;
    }

    public NativeBuilder map() {
        if (current != null) {
            stack.push(current);
        }
        Map<String, Object> newMap = new HashMap<String, Object>();

        if ( !isNil() ) {
            value(newMap);
        }

        current = currentMap = newMap;
        currentList = null;

        if (initial == null) {
            initial = current;
        }

        return this;
    }

    public NativeBuilder key(String key) {
        if ( isNil() ) {
            throw new IllegalStateException("Please start with either #map() or #list()");
        }

        if ( isList() ) {
            throw new IllegalStateException("Lists can only have values");
        }

        this.key = key;

        return this;
    }

    public NativeBuilder value(Object o) {
        if ( isNil() ) {
            throw new IllegalStateException("Please start with either #map() or #list()");
        }

        if ( isList() ) {
            currentList.add(o);
        } else {
            if (! hasKey()) {
                throw new IllegalStateException("Please provide a key for every value");
            }
            currentMap.put(key, o);
            key = null;
        }
        return this;
    }

    public NativeBuilder end() {
        if (stack.isEmpty()) {
            return this;
        }
        Object previous = stack.pop();
        current = null;
        if (Map.class.isInstance(previous)) {
            current = currentMap = (Map<String, Object>) previous;
            currentList = null;
        }

        if (List.class.isInstance(previous)) {
            current = currentList = (List<Object>) previous;
            currentMap = null;
        }
        return this;
    }

    public Map<String, Object> getMap() {

        return (Map<String, Object>) initial;
    }

    public List getList() {

        return (List<Object>) initial;
    }

    private boolean isNil() {
        return current == null;
    }

    private boolean isList() {
        return currentList == null;
    }

    private boolean isMap() {
        return currentMap == null;
    }

    private boolean hasKey() {
        return key != null;
    }



}
