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

package com.openexchange.jslob;

import static com.openexchange.java.util.Tools.getUnsignedInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.exception.OXException;

/**
 * {@link JSONPathElement} - A JSON path element.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONPathElement {

    private static final Pattern SPLIT = Pattern.compile("/"); // Pattern.compile("\\.");

    /**
     * Parses specified path to a list of path elements.
     *
     * @param path The path to parse; e.g. <code>"ui/my/setting"</code>
     * @return The parsed path
     * @throws OXException If parsing path fails
     */
    public static List<JSONPathElement> parsePath(final String path) throws OXException {
        try {
            final String[] fields = SPLIT.split(path, 0);
            final List<JSONPathElement> list = new ArrayList<JSONPathElement>(fields.length);
            StringBuilder composite = null;
            for (int i = 0; i < fields.length; i++) {
                String field = fields[i];
                if (field.isEmpty()) {
                    continue;
                }
                if (field.endsWith("\\")) {
                    if (composite == null) {
                        composite = new StringBuilder();
                    }
                    composite.append(field.substring(0, field.length() - 1)).append('/');
                } else {
                    field = (composite != null) ? composite.toString() + field : field;
                    composite = null;
                    final int pos = field.indexOf('[');
                    if (pos >= 0) {
                        final int index = getUnsignedInteger(field.substring(pos + 1, field.indexOf(']', pos + 1)));
                        final String name = field.substring(0, pos);
                        list.add(new JSONPathElement(0 == name.length() ? null : name, index));
                    } else {
                        list.add(new JSONPathElement(field));
                    }
                }
            }
            return list;
        } catch (IndexOutOfBoundsException e) {
            throw JSlobExceptionCodes.INVALID_PATH.create(path);
        }
    }

    /**
     * Gets the value associated with specified path in given JSlob.
     *
     * @param jPath The path
     * @param jslob The JSlob
     * @return The associated value or <code>null</code> if not present
     */
    public static Object getPathFrom(final List<JSONPathElement> jPath, final JSlob jslob) {
        return getPathFrom(jPath, jslob.getJsonObject());
    }

    /**
     * Gets the value associated with specified path in given JSON object.
     *
     * @param jPath The path
     * @param jslob The JSON object
     * @return The associated value or <code>null</code> if not present
     */
    public static Object getPathFrom(final List<JSONPathElement> jPath, final JSONObject jObject) {
        JSONObject jCurrent = jObject;
        final int msize = jPath.size() - 1;
        for (int i = 0; i < msize; i++) {
            final JSONPathElement jPathElement = jPath.get(i);
            final int index = jPathElement.getIndex();
            final String name = jPathElement.getName();
            if (index >= 0) {
                /*
                 * Denotes an index within a JSON array
                 */
                if (isInstance(name, JSONArray.class, jCurrent)) {
                    try {
                        final JSONArray jsonArray = jCurrent.getJSONArray(name);
                        jCurrent = jsonArray.getJSONObject(index);
                    } catch (JSONException e) {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                /*
                 * Denotes an element within a JSON object
                 */
                if (isInstance(name, JSONObject.class, jCurrent)) {
                    try {
                        jCurrent = jCurrent.getJSONObject(name);
                    } catch (JSONException e) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
        try {
            final JSONPathElement leaf = jPath.get(msize);
            final int index = leaf.getIndex();
            final String name = leaf.getName();
            final Object retval;
            if (index >= 0) {
                retval = jCurrent.getJSONArray(name).get(index);
            } else {
                retval = jCurrent.get(name);
            }
            return retval;
        } catch (JSONException e) {
            return null;
        }
    }

    private static final class Entry {

        private Entry parent = null;
        private final JSONObject object;
        private final JSONPathElement pathElem;

        public Entry(JSONPathElement pathElem, JSONObject object, Entry parent) {
            this.pathElem = pathElem;
            this.object = object;
            this.parent = parent;
        }

        public void removeIfEmpty() {
            if (parent == null) {
                return;
            }
            try {
                boolean removed = false;
                String name = pathElem.getName();
                if (isInstance(name, JSONArray.class, parent.object) && parent.object.getJSONArray(name).getJSONObject(pathElem.getIndex()).length() == 0) {
                    removeOne(parent.object.getJSONArray(name), pathElem.getIndex());
                    if (parent.object.getJSONArray(name).length() == 0) {
                        parent.object.remove(name);
                    }
                    removed = true;
                } else if (parent.object.getJSONObject(name).length() == 0) {
                    parent.object.remove(name);
                    removed = true;
                }
                if (!removed) {
                    return;
                }
            } catch (JSONException x) {
                // Ignore
            }
            if (parent != null) {
                parent.removeIfEmpty();
            }
        }
    }

    public static Object remove(List<JSONPathElement> jPath, JSONObject jObject) {
        JSONObject jCurrent = jObject;
        Entry entry = new Entry(null, jObject, null);

        final int msize = jPath.size() - 1;
        for (int i = 0; i < msize; i++) {
            final JSONPathElement jPathElement = jPath.get(i);
            final int index = jPathElement.getIndex();
            final String name = jPathElement.getName();
            if (index >= 0) {
                /*
                 * Denotes an index within a JSON array
                 */
                if (isInstance(name, JSONArray.class, jCurrent)) {
                    try {
                        final JSONArray jsonArray = jCurrent.getJSONArray(name);
                        jCurrent = jsonArray.getJSONObject(index);
                        entry = new Entry(jPathElement, jCurrent, entry);
                    } catch (JSONException e) {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                /*
                 * Denotes an element within a JSON object
                 */
                if (isInstance(name, JSONObject.class, jCurrent)) {
                    try {
                        jCurrent = jCurrent.getJSONObject(name);
                        entry = new Entry(jPathElement, jCurrent, entry);
                    } catch (JSONException e) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
        try {
            final JSONPathElement leaf = jPath.get(msize);
            final int index = leaf.getIndex();
            final String name = leaf.getName();
            final Object retval;
            if (index >= 0) {
                retval = jCurrent.getJSONArray(name).get(index);
                removeOne(jCurrent.getJSONArray(name), index);
            } else {
                retval = jCurrent.get(name);
                jCurrent.remove(name);
            }
            if (retval instanceof JSONValue) {
                // Not a leaf
                return null;
            }
            entry.removeIfEmpty();
            return retval;
        } catch (JSONException e) {
            return null;
        }
    }

    static void removeOne(JSONArray jsonArray, int theIndex) {
        try {
            jsonArray.remove(theIndex);
        } catch (JSONException e) {
            // Ignore
        }
    }

    static boolean isInstance(final String name, final Class<? extends JSONValue> clazz, final JSONObject jsonObject) {
        return clazz.isInstance(jsonObject.opt(name));
    }

    /*-
     * ----------------------------- Member stuff ---------------------------------
     */

    private final String name;

    private final int index;

    /**
     * Initializes a new {@link JSONPathElement}.
     *
     * @param name The field name
     */
    public JSONPathElement(final String name) {
        this(name, -1);
    }

    /**
     * Initializes a new {@link JSONPathElement}.
     *
     * @param name The field name
     * @param index The index in the JSON array denoted by field name
     */
    public JSONPathElement(final String name, final int index) {
        super();
        this.name = name;
        this.index = index;
    }

    /**
     * Checks if this JSON field denotes a certain index in a JSON array.
     *
     * @return <code>true</code> if this JSON field has an index; otherwise <code>false</code>
     */
    public boolean hasIndex() {
        return index >= 0;
    }

    @Override
    public String toString() {
        if (index >= 0) {
            new StringBuilder(name).append('[').append(index).append(']').toString();
        }
        return name;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the index
     *
     * @return The index
     */
    public int getIndex() {
        return index;
    }
}
