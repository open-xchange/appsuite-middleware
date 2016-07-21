
package org.json;

/*
 Copyright (c) 2002 JSON.org

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 The Software shall be used for Good, not Evil.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.helpers.UnsynchronizedStringReader;
import org.json.helpers.UnsynchronizedStringWriter;

/**
 * A JSONArray is an ordered sequence of values. Its external text form is a string wrapped in square brackets with commas separating the
 * values. The internal form is an object having <code>get</code> and <code>opt</code> methods for accessing the values by index, and
 * <code>put</code> methods for adding or replacing values. The values can be any of these types: <code>Boolean</code>,
 * <code>JSONArray</code>, <code>JSONObject</code>, <code>Number</code>, <code>String</code>, or the <code>JSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a JSON text into a Java object. The <code>toString</code> method converts to JSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an exception if one cannot be found. An <code>opt</code> method
 * returns a default value instead of throwing an exception, and so is useful for obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type coersion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to JSON syntax rules. The constructors are more forgiving in the
 * texts they will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there is <code>,</code>&nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote or single quote, and if they do not contain leading or
 * trailing spaces, and if they do not contain any of these characters: <code>{ } [ ] / \ : , = ; #</code> and if they do not look like
 * numbers and if they are not the reserved words <code>true</code>, <code>false</code>, or <code>null</code>.</li>
 * <li>Values can be separated by <code>;</code> <small>(semicolon)</small> as well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0-</code> <small>(octal)</small> or <code>0x-</code> <small>(hex)</small> prefix.</li>
 * <li>Comments written in the slashshlash, slashstar, and hash conventions will be ignored.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2
 */
public class JSONArray extends AbstractJSONValue implements Iterable<Object> {

    private static final long serialVersionUID = -3408431864592339725L;

    /**
     * The arrayList where the JSONArray's properties are kept.
     */
    private final List<Object> myArrayList;

    /**
     * Construct an empty JSONArray.
     */
    public JSONArray() {
        super();
        this.myArrayList = new ArrayList<Object>();
    }

    /**
     * Construct an empty JSONArray.
     *
     * @throws IllegalArgumentException If the specified initial capacity is negative
     */
    public JSONArray(final int initialCapacity) {
        super();
        this.myArrayList = new ArrayList<Object>(initialCapacity);
    }

    /**
     * Construct a JSONArray from a JSONTokener.
     *
     * @param x A JSONTokener
     * @throws JSONException If there is a syntax error.
     */
    public JSONArray(final JSONTokener x) throws JSONException {
        this();
        if (x.nextClean() != '[') {
            throw x.syntaxError("A JSONArray text must start with '['");
        }
        if (x.nextClean() == ']') {
            return;
        }
        x.back();
        for (;;) {
            if (x.nextClean() == ',') {
                x.back();
                this.myArrayList.add(null);
            } else {
                x.back();
                this.myArrayList.add(x.nextValue());
            }
            switch (x.nextClean()) {
            case ';':
            case ',':
                if (x.nextClean() == ']') {
                    return;
                }
                x.back();
                break;
            case ']':
                return;
            default:
                throw x.syntaxError("Expected a ',' or ']'");
            }
        }
    }

    /**
     * Construct a JSONArray from a reader.
     *
     * @param reader A reader that begins with <code>[</code>&nbsp;<small>(left bracket)</small> and ends with <code>]</code>
     *            &nbsp;<small>(right bracket)</small>.
     * @throws JSONException If there is a syntax error reader's content..
     */
    public JSONArray(final Reader reader) throws JSONException {
        this();
        if (null == reader) {
            throw new JSONException("Reader must not be null.");
        }
        parse(reader, this);
    }

    /**
     * Construct a JSONArray from a source sJSON text.
     *
     * @param string A string that begins with <code>[</code>&nbsp;<small>(left bracket)</small> and ends with <code>]</code>
     *            &nbsp;<small>(right bracket)</small>.
     * @throws JSONException If there is a syntax error.
     */
    public JSONArray(final String string) throws JSONException {
        this();
        if (null == string) {
            throw new JSONException("String must not be null.");
        }
        if (!"[]".equals(string)) {
            parse(new UnsynchronizedStringReader(string), this);
        }
    }

    private static String checkString(final String string) throws JSONException {
        if (null == string) {
            throw new JSONException("String must not be null.");
        }
        if (isEmpty(string)) {
            throw new JSONException("A JSONArray text must start with '['");
        }
        if (string.indexOf(']') <= 0) {
            throw new JSONException("A JSONArray text must start with '[' and must end with ']'");
        }
        return string;
    }

    private static boolean isEmpty(final String string) {
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            switch (string.charAt(i)) {
            case 9: // 'unicode: 0009
            case 10: // 'unicode: 000A'
            case 11: // 'unicode: 000B'
            case 12: // 'unicode: 000C'
            case 13: // 'unicode: 000D'
            case 28: // 'unicode: 001C'
            case 29: // 'unicode: 001D'
            case 30: // 'unicode: 001E'
            case 31: // 'unicode: 001F'
            case ' ': // Space
                // case Character.SPACE_SEPARATOR:
                // case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
                isWhitespace = true;
                break;
            default:
                isWhitespace = false;
            }
        }
        return isWhitespace;
    }

    /**
     * Construct a JSONArray from a JSONArray.
     *
     * @param jsonArray A JSONArray.
     */
    public JSONArray(final JSONArray jsonArray) {
        this(jsonArray.myArrayList);
    }

    /**
     * Construct a JSONArray from a Collection.
     *
     * @param collection A Collection.
     */
    public JSONArray(final Collection<? extends Object> collection) {
        super();
        if (collection == null || collection.isEmpty()) {
            this.myArrayList = new ArrayList<Object>();
        } else {
            this.myArrayList = new ArrayList<Object>(collection.size());
            for (final Object value : collection) {
                if (value instanceof JSONValue) {
                    final JSONValue jsonValue = (JSONValue) value;
                    if (jsonValue.isArray()) {
                        myArrayList.add(new JSONArray(jsonValue.toArray()));
                    } else {
                        myArrayList.add(new JSONObject(jsonValue.toObject()));
                    }
                } else if (value instanceof Collection) {
                    myArrayList.add(new JSONArray((Collection<Object>) value));
                } else if (value instanceof Map) {
                    myArrayList.add(new JSONObject((Map<String, Object>) value));
                } else {
                    myArrayList.add(value);
                }
            }
        }
    }

    @Override
    public boolean isEqualTo(final JSONValue jsonValue) {
        if (jsonValue == this) {
            return true;
        }
        if ((null == jsonValue) || !jsonValue.isArray()) {
            return false;
        }
        final List<Object> l = jsonValue.toArray().myArrayList;
        final ListIterator<Object> e1 = myArrayList.listIterator();
        final ListIterator<Object> e2 = l.listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            final Object o1 = e1.next();
            final Object o2 = e2.next();
            if (o1 instanceof JSONValue) {
                if (o2 instanceof JSONValue) {
                    if (!((JSONValue) o1).isEqualTo((JSONValue) o2)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else if (!(isNull(o1) ? isNull(o2) : o1.equals(o2))) {
                return false;
            }
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    /**
     * Gets the {@link List list} view for this JSON array.
     *
     * @return The list
     */
    public List<Object> asList() {
        final List<Object> retval = new ArrayList<Object>(myArrayList.size());
        for (final Object value : myArrayList) {
            if (value instanceof JSONValue) {
                final JSONValue jsonValue = (JSONValue) value;
                if (jsonValue.isArray()) {
                    retval.add(jsonValue.toArray().asList());
                } else {
                    retval.add(jsonValue.toObject().asMap());
                }
            } else {
                retval.add(value);
            }
        }
        return retval;
    }

    /**
     * Resets this JSONArray for re-use
     */
    @Override
    public void reset() {
        myArrayList.clear();
    }

    /**
     * Gets this JSON array's iterator.
     *
     * @return The iterator
     */
    @Override
    public Iterator<Object> iterator() {
        return myArrayList.iterator();
    }

    /**
     * Get the object value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return An object value.
     * @throws JSONException If there is no value for the index.
     */
    public Object get(final int index) throws JSONException {
        final Object o = opt(index);
        if (o == null) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        return o;
    }

    /**
     * Get the boolean value associated with an index. The string values "true" and "false" are converted to boolean.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The truth.
     * @throws JSONException If there is no value for the index or if the value is not convertable to boolean.
     */
    public boolean getBoolean(final int index) throws JSONException {
        final Object o = get(index);
        if (o.equals(Boolean.FALSE) || (o instanceof String && "false".equalsIgnoreCase((String) o))) {
            return false;
        } else if (o.equals(Boolean.TRUE) || (o instanceof String && "true".equalsIgnoreCase((String) o))) {
            return true;
        }
        throw new JSONException("JSONArray[" + index + "] is not a Boolean.");
    }

    /**
     * Get the double value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     * @throws JSONException If the key is not found or if the value cannot be converted to a number.
     */
    public double getDouble(final int index) throws JSONException {
        final Object o = get(index);
        try {
            return o instanceof Number ? ((Number) o).doubleValue() : Double.parseDouble((String) o);
        } catch (final Exception e) {
            throw new JSONException("JSONArray[" + index + "] is not a number.", e);
        }
    }

    /**
     * Get the int value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     * @throws JSONException If the key is not found or if the value cannot be converted to a number. if the value cannot be converted to a
     *             number.
     */
    public int getInt(final int index) throws JSONException {
        final Object o = get(index);
        return o instanceof Number ? ((Number) o).intValue() : (int) getDouble(index);
    }

    /**
     * Get the JSONArray associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return A JSONArray value.
     * @throws JSONException If there is no value for the index. or if the value is not a JSONArray
     */
    public JSONArray getJSONArray(final int index) throws JSONException {
        final Object o = get(index);
        if (o instanceof JSONArray) {
            return (JSONArray) o;
        }
        throw new JSONException("JSONArray[" + index + "] is not a JSONArray, but " + o.getClass().getName());
    }

    /**
     * Get the JSONObject associated with an index.
     *
     * @param index subscript
     * @return A JSONObject value.
     * @throws JSONException If there is no value for the index or if the value is not a JSONObject
     */
    public JSONObject getJSONObject(final int index) throws JSONException {
        final Object o = get(index);
        if (o instanceof JSONObject) {
            return (JSONObject) o;
        }
        throw new JSONException("JSONArray[" + index + "] is not a JSONObject, but " + o.getClass().getName());
    }

    /**
     * Get the long value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     * @throws JSONException If the key is not found or if the value cannot be converted to a number.
     */
    public long getLong(final int index) throws JSONException {
        final Object o = get(index);
        return o instanceof Number ? ((Number) o).longValue() : (long) getDouble(index);
    }

    /**
     * Get the string associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return A string value.
     * @throws JSONException If there is no value for the index.
     */
    public String getString(final int index) throws JSONException {
        return get(index).toString();
    }

    /**
     * Determine if the value is null.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return true if the value at the index is null, or if there is no value.
     */
    public boolean isNull(final int index) {
        return JSONObject.NULL.equals(opt(index));
    }

    /**
     * Make a string from the contents of this JSONArray. The <code>separator</code> string is inserted between each element. Warning: This
     * method assumes that the data structure is acyclical.
     *
     * @param separator A string that will be inserted between the elements.
     * @return a string.
     * @throws JSONException If the array contains an invalid number.
     */
    public String join(final String separator) throws JSONException {
        final int len = length();
        final StringBuilder sb = new StringBuilder(len << 4);
        for (int i = 0; i < len; i += 1) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(JSONObject.valueToString(myArrayList.get(i)));
        }
        return sb.toString();
    }

    @Override
    public boolean isEmpty() {
        return myArrayList.isEmpty();
    }

    /**
     * Get the number of elements in the JSONArray, included nulls.
     *
     * @return The length (or size).
     */
    @Override
    public int length() {
        return myArrayList.size();
    }

    /**
     * Get the optional object value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return An object value, or null if there is no object at that index.
     */
    public Object opt(final int index) {
        return (index < 0 || index >= length()) ? null : myArrayList.get(index);
    }

    /**
     * Get the optional boolean value associated with an index. It returns false if there is no value at that index, or if the value is not
     * Boolean.TRUE or the String "true".
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The truth.
     */
    public boolean optBoolean(final int index) {
        return optBoolean(index, false);
    }

    /**
     * Get the optional boolean value associated with an index. It returns the defaultValue if there is no value at that index or if it is
     * not a Boolean or the String "true" or "false" (case insensitive).
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue A boolean default.
     * @return The truth.
     */
    public boolean optBoolean(final int index, final boolean defaultValue) {
        try {
            return getBoolean(index);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional double value associated with an index. NaN is returned if there is no value for the index, or if the value is not a
     * number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     */
    public double optDouble(final int index) {
        return optDouble(index, Double.NaN);
    }

    /**
     * Get the optional double value associated with an index. The defaultValue is returned if there is no value for the index, or if the
     * value is not a number and cannot be converted to a number.
     *
     * @param index subscript
     * @param defaultValue The default value.
     * @return The value.
     */
    public double optDouble(final int index, final double defaultValue) {
        try {
            return getDouble(index);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional int value associated with an index. Zero is returned if there is no value for the index, or if the value is not a
     * number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     */
    public int optInt(final int index) {
        return optInt(index, 0);
    }

    /**
     * Get the optional int value associated with an index. The defaultValue is returned if there is no value for the index, or if the value
     * is not a number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue The default value.
     * @return The value.
     */
    public int optInt(final int index, final int defaultValue) {
        try {
            return getInt(index);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional JSONArray associated with an index.
     *
     * @param index subscript
     * @return A JSONArray value, or null if the index has no value, or if the value is not a JSONArray.
     */
    public JSONArray optJSONArray(final int index) {
        final Object o = opt(index);
        return o instanceof JSONArray ? (JSONArray) o : null;
    }

    /**
     * Get the optional JSONObject associated with an index. Null is returned if the key is not found, or null if the index has no value, or
     * if the value is not a JSONObject.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return A JSONObject value.
     */
    public JSONObject optJSONObject(final int index) {
        final Object o = opt(index);
        return o instanceof JSONObject ? (JSONObject) o : null;
    }

    /**
     * Get the optional long value associated with an index. Zero is returned if there is no value for the index, or if the value is not a
     * number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     */
    public long optLong(final int index) {
        return optLong(index, 0);
    }

    /**
     * Get the optional long value associated with an index. The defaultValue is returned if there is no value for the index, or if the
     * value is not a number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue The default value.
     * @return The value.
     */
    public long optLong(final int index, final long defaultValue) {
        try {
            return getLong(index);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional string value associated with an index. It returns an empty string if there is no value at that index. If the value
     * is not a string and is not null, then it is coverted to a string.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return A String value.
     */
    public String optString(final int index) {
        return optString(index, "");
    }

    /**
     * Get the optional string associated with an index. The defaultValue is returned if the key is not found.
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue The default value.
     * @return A String value.
     */
    public String optString(final int index, final String defaultValue) {
        final Object o = opt(index);
        return o != null ? o.toString() : defaultValue;
    }

    /**
     * Append a boolean value. This increases the array's length by one.
     *
     * @param value A boolean value.
     * @return this.
     */
    public JSONArray put(final boolean value) {
        put(value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    /**
     * Put a value in the JSONArray, where the value will be a JSONArray which is produced from a Collection.
     *
     * @param value A Collection value.
     * @return this.
     */
    public JSONArray put(final Collection<? extends Object> value) {
        put(new JSONArray(value));
        return this;
    }

    /**
     * Append a double value. This increases the array's length by one.
     *
     * @param value A double value.
     * @throws JSONException if the value is not finite.
     * @return this.
     */
    public JSONArray put(final double value) throws JSONException {
        final Double d = Double.valueOf(value);
        //JSONObject.testValidity(d);
        put(d);
        return this;
    }

    /**
     * Append an int value. This increases the array's length by one.
     *
     * @param value An int value.
     * @return this.
     */
    public JSONArray put(final int value) {
        put(Integer.valueOf(value));
        return this;
    }

    /**
     * Append an long value. This increases the array's length by one.
     *
     * @param value A long value.
     * @return this.
     */
    public JSONArray put(final long value) {
        put(Long.valueOf(value));
        return this;
    }

    /**
     * Put a value in the JSONArray, where the value will be a JSONObject which is produced from a Map.
     *
     * @param value A Map value.
     * @return this.
     */
    public JSONArray put(final Map<String, ? extends Object> value) {
        put(new JSONObject(value));
        return this;
    }

    /**
     * Append an object value. This increases the array's length by one.
     *
     * @param value An object value. The value should be a Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
     *            JSONObject.NULL object.
     * @return this.
     */
    public JSONArray put(final Object value) {
        this.myArrayList.add(value);
        return this;
    }

    /**
     * Put or replace a boolean value in the JSONArray. If the index is greater than the length of the JSONArray, then null elements will be
     * added as necessary to pad it out.
     *
     * @param index The subscript.
     * @param value A boolean value.
     * @return this.
     * @throws JSONException If the index is negative.
     */
    public JSONArray put(final int index, final boolean value) throws JSONException {
        put(index, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    /**
     * Put a value in the JSONArray, where the value will be a JSONArray which is produced from a Collection.
     *
     * @param index The subscript.
     * @param value A Collection value.
     * @return this.
     * @throws JSONException If the index is negative or if the value is not finite.
     */
    public JSONArray put(final int index, final Collection<? extends Object> value) throws JSONException {
        put(index, new JSONArray(value));
        return this;
    }

    /**
     * Put or replace a double value. If the index is greater than the length of the JSONArray, then null elements will be added as
     * necessary to pad it out.
     *
     * @param index The subscript.
     * @param value A double value.
     * @return this.
     * @throws JSONException If the index is negative or if the value is not finite.
     */
    public JSONArray put(final int index, final double value) throws JSONException {
        put(index, Double.valueOf(value));
        return this;
    }

    /**
     * Put or replace an int value. If the index is greater than the length of the JSONArray, then null elements will be added as necessary
     * to pad it out.
     *
     * @param index The subscript.
     * @param value An int value.
     * @return this.
     * @throws JSONException If the index is negative.
     */
    public JSONArray put(final int index, final int value) throws JSONException {
        put(index, Integer.valueOf(value));
        return this;
    }

    /**
     * Put or replace a long value. If the index is greater than the length of the JSONArray, then null elements will be added as necessary
     * to pad it out.
     *
     * @param index The subscript.
     * @param value A long value.
     * @return this.
     * @throws JSONException If the index is negative.
     */
    public JSONArray put(final int index, final long value) throws JSONException {
        put(index, Long.valueOf(value));
        return this;
    }

    /**
     * Put a value in the JSONArray, where the value will be a JSONObject which is produced from a Map.
     *
     * @param index The subscript.
     * @param value The Map value.
     * @return this.
     * @throws JSONException If the index is negative or if the the value is an invalid number.
     */
    public JSONArray put(final int index, final Map<String, ? extends Object> value) throws JSONException {
        put(index, new JSONObject(value));
        return this;
    }

    /**
     * Put or replace an object value in the JSONArray. If the index is greater than the length of the JSONArray, then null elements will be
     * added as necessary to pad it out.
     *
     * @param index The subscript.
     * @param value The value to put into the array. The value should be a Boolean, Double, Integer, JSONArray, JSONObject, Long, or String,
     *            or the JSONObject.NULL object.
     * @return this.
     * @throws JSONException If the index is negative or if the the value is an invalid number.
     */
    public JSONArray put(final int index, final Object value) throws JSONException {
        if (index < 0) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        if (index < length()) {
            this.myArrayList.set(index, value);
        } else {
            while (index != length()) {
                put(JSONObject.NULL);
            }
            put(value);
        }
        return this;
    }

    /**
     * Inserts an object value at the specified position in this JSONArray. Shifts the object value currently at that position (if any) and
     * any subsequent object values to the right (adds one to their indices).
     * <p>
     * If the index is greater than the length of the JSONArray, then null elements will be added as necessary to pad it out.
     *
     * @param index The index at which the specified object value is to be inserted
     * @param value The value to put into the array. The value should be a Boolean, Double, Integer, JSONArray, JSONObject, Long, or String,
     *            or the JSONObject.NULL object.
     * @return this.
     * @throws JSONException If the index is negative or if the the value is an invalid number.
     */
    public JSONArray add(final int index, final Object value) throws JSONException {
        if (index < 0) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        if (index < length()) {
            this.myArrayList.add(index, value);
        } else {
            while (index != length()) {
                put(JSONObject.NULL);
            }
            put(value);
        }
        return this;
    }

    /**
     * Removes the element at the specified position in this JSONArray.
     * <p>
     * Shifts any subsequent elements to the left (subtracts one from their indices).
     *
     * @param index The index position
     * @return this.
     * @throws JSONException  If the index is negative or if the remove operation fails.
     */
    public JSONArray remove(final int index) throws JSONException {
        if (index < 0) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        if (index < length()) {
            try {
                this.myArrayList.remove(index);
            } catch (final RuntimeException e) {
                throw new JSONException("JSONArray[" + index + "] could not be removed.", e);
            }
        }
        return this;
    }

    /**
     * Produce a JSONObject by combining a JSONArray of names with the values of this JSONArray.
     *
     * @param names A JSONArray containing a list of key strings. These will be paired with the values.
     * @return A JSONObject, or null if there are no names or if this JSONArray has no values.
     * @throws JSONException If any of the names are null.
     */
    public JSONObject toJSONObject(final JSONArray names) throws JSONException {
        if (names == null || names.length() == 0 || length() == 0) {
            return null;
        }
        final JSONObject jo = new JSONObject();
        for (int i = 0; i < names.length(); i += 1) {
            jo.put(names.getString(i), this.opt(i));
        }
        return jo;
    }

    private static final String EMPTY = "[]".intern();

    /**
     * Make a JSON text of this JSONArray. For compactness, no unnecessary whitespace is added. If it is not possible to produce a
     * syntactically correct JSON text then null will be returned instead. This could occur if the array contains an invalid number.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a printable, displayable, transmittable representation of the array.
     */
    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Make a JSON text of this JSONArray. For compactness, no unnecessary whitespace is added. If it is not possible to produce a
     * syntactically correct JSON text then null will be returned instead. This could occur if the array contains an invalid number.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param asciiOnly Whether to use only ASCII characters
     * @return a printable, displayable, transmittable representation of the array.
     */
    public String toString(final boolean asciiOnly) {
        try {
            final int n = length();
            if (n <= 0) {
                return EMPTY;
            }

            final UnsynchronizedStringWriter writer = new UnsynchronizedStringWriter(n << 4);
            write(writer, asciiOnly);
            return writer.toString();
        } catch (final Exception e) {
            final Logger logger = JSONObject.LOGGER.get();
            if (null != logger) {
                logger.logp(Level.SEVERE, JSONArray.class.getName(), "toString()", e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Make a prettyprinted JSON text of this JSONArray. Warning: This method assumes that the data structure is acyclical.
     *
     * @param indentFactor The number of spaces to add to each level of indentation.
     * @return a printable, displayable, transmittable representation of the object, beginning with <code>[</code>&nbsp;<small>(left
     *         bracket)</small> and ending with <code>]</code>&nbsp;<small>(right bracket)</small>.
     * @throws JSONException
     */
    public String toString(final int indentFactor) throws JSONException {
        final int n = length();
        if (n <= 0) {
            return EMPTY;
        }

        return toString(indentFactor, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(final int indentFactor, final int indent) throws JSONException {
        final int len = length();
        if (len == 0) {
            return EMPTY;
        }

        JsonGenerator jGenerator = null;
        try {
            final UnsynchronizedStringWriter writer = new UnsynchronizedStringWriter(len << 4);
            jGenerator = createGenerator(writer, false);
            jGenerator.setPrettyPrinter(STANDARD_DEFAULT_PRETTY_PRINTER);
            write(this, jGenerator);
            return writer.toString();
        } catch (final IOException e) {
            throw new JSONException(e);
        } finally {
            close(jGenerator);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Writer write(final Writer writer) throws JSONException {
        return write(writer, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Writer write(final Writer writer, final boolean asciiOnly) throws JSONException {
        JsonGenerator jGenerator = null;
        try {
            jGenerator = createGenerator(writer, asciiOnly);
            jGenerator.setPrettyPrinter(STANDARD_MINIMAL_PRETTY_PRINTER);
            write(this, jGenerator);
            return writer;
        } catch (final IOException e) {
            throw new JSONException(e);
        } finally {
            close(jGenerator);
        }
    }

    @Override
    protected void writeTo(final JsonGenerator jGenerator) throws IOException, JSONException {
        write(this, jGenerator);
    }

    /**
     * Writes specified JSON array to given generator.
     *
     * @param ja The JSON array
     * @param jGenerator The generator
     * @throws IOException If an I/O error occurs
     * @throws JSONException IOf a JSON error occurs
     */
    protected static void write(final JSONArray ja, final JsonGenerator jGenerator) throws IOException, JSONException {
        jGenerator.writeStartArray();
        try {
            final List<Object> myArrayList = ja.myArrayList;
            final int len = myArrayList.size();
            if (len > 0) {
                for (int i = 0; i < len; i++) {
                    write(myArrayList.get(i), jGenerator);
                }
            }
        } finally {
            writeEndAndFlush(jGenerator, false);
        }
    }

    /**
     * Parses specified reader's content to a JSON array.
     *
     * @param reader The reader to read from
     * @return The parsed JSON array
     * @throws JSONException If an error occurs
     */
    protected static JSONArray parse(final Reader reader, final JSONArray optArray) throws JSONException {
        JsonParser jParser = null;
        try {
            jParser = createParser(reader);
            // Check start
            {
                final JsonToken token = jParser.nextToken();
                if (token != JsonToken.START_ARRAY) {
                    final String content = readFrom(reader, 0x2000);
                    final String sep = System.getProperty("line.separator");
                    throw new JSONException("A JSONArray text must begin with '[', but got \"" + (null == token ? "null" : token.toString()) + "\" parse event." + sep + "Rest:" + sep + content);
                }
            }
            return parse(jParser, optArray);
        } catch (final IOException e) {
            throw new JSONException(e);
        } finally {
            close(jParser);
        }
    }

    /**
     * Parses specified JSON array from given parser.
     *
     * @param jParser The JSON parser with {@link JsonToken#START_ARRAY} already consumed
     * @return The JSON array
     * @throws JSONException If an error occurs
     */
    protected static JSONArray parse(final JsonParser jParser, final JSONArray optArray) throws JSONException {
        try {
            final JSONArray ja = null == optArray ? new JSONArray() : optArray;
            JsonToken current = jParser.nextToken();
            while (current != JsonToken.END_ARRAY) {
                if (current == JsonToken.FIELD_NAME) {
                    throw new JSONException("JSON parse error: Got a field name inside a JSONArray.");
                }
                switch (current) {
                case START_OBJECT:
                    ja.put(JSONObject.parse(jParser, null));
                    break;
                case START_ARRAY:
                    ja.put(parse(jParser, null));
                    break;
                case VALUE_FALSE:
                    ja.put(false);
                    break;
                case VALUE_NULL:
                    ja.put(JSONObject.NULL);
                    break;
                case VALUE_NUMBER_FLOAT:
                    ja.put(jParser.getDecimalValue());
                    break;
                case VALUE_NUMBER_INT:
                    try {
                        ja.put(jParser.getIntValue());
                    } catch (final JsonParseException e) {
                        // Outside of range of Java int
                        ja.put(jParser.getLongValue());
                    }
                    break;
                case VALUE_TRUE:
                    ja.put(true);
                    break;
                case VALUE_STRING:
                    ja.put(jParser.getText());
                    break;
                default:
                    // Ignore
                    break;
                }
                current = jParser.nextToken();
            }
            return ja;
        } catch (final IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isArray() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isObject() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONArray toArray() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toObject() {
        return null;
    }
}
