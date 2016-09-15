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
        } catch (final IndexOutOfBoundsException e) {
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
                    } catch (final JSONException e) {
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
                    } catch (final JSONException e) {
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
            if (retval instanceof JSONValue) {
                // Not a leaf
                return null;
            }
            return retval;
        } catch (final JSONException e) {
            return null;
        }
    }

    private static final class Entry {
    	private Entry parent = null;
    	private final JSONObject object;
    	private final JSONPathElement name;

    	public Entry(JSONPathElement name, JSONObject object, Entry parent) {
    		this.name = name;
    		this.object = object;
    		this.parent = parent;
    	}

		public void removeIfEmpty() {
			if (parent == null) {
				return;
			}
			try {
				boolean removed = false;
				if (isInstance(name.name, JSONArray.class, parent.object) && parent.object.getJSONArray(name.name).getJSONObject(name.index).length() == 0) {
					removeOne(parent.object.getJSONArray(name.name), name.index);
					if (parent.object.getJSONArray(name.name).length() == 0) {
						parent.object.remove(name.name);
					}
					removed = true;
				} else if (parent.object.getJSONObject(name.name).length() == 0){
					parent.object.remove(name.name);
					removed = true;
				}
				if (!removed) {
					return;
				}
			} catch (JSONException x) {

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
                    } catch (final JSONException e) {
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
                    } catch (final JSONException e) {
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
        } catch (final JSONException e) {
            return null;
        }
	}


    private static void removeOne(JSONArray jsonArray, int theIndex) {
    	List<Object> purged = new ArrayList<Object>(jsonArray.length());
    	for(int i = 0, size = jsonArray.length(); i < size; i++) {
    		if (i != theIndex) {
    			try {
					purged.add(jsonArray.get(i));
				} catch (JSONException e) {
				}
    		}
    	}

    	jsonArray.reset();
    	for(Object p: purged) {
    		jsonArray.put(p);
    	}

	}

	private static boolean isInstance(final String name, final Class<? extends JSONValue> clazz, final JSONObject jsonObject) {
        if (!jsonObject.hasAndNotNull(name)) {
            return false;
        }
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
