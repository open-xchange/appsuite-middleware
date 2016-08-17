
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
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.json.helpers.UnsynchronizedStringReader;
import org.json.helpers.UnsynchronizedStringWriter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * A JSONObject is an unordered collection of name/value pairs. Its external form is a string wrapped in curly braces with colons between
 * the names and values, and commas between the values and names. The internal form is an object having <code>get</code> and
 * <code>opt</code> methods for accessing the values by name, and <code>put</code> methods for adding or replacing values by name. The
 * values can be any of these types: <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>, <code>Number</code>,
 * <code>String</code>, or the <code>JSONObject.NULL</code> object. A JSONObject constructor can be used to convert an external form JSON
 * text into an internal form whose values can be retrieved with the <code>get</code> and <code>opt</code> methods, or to convert values
 * into a JSON text using the <code>put</code> and <code>toString</code> methods. A <code>get</code> method returns a value if one can be
 * found, and throws an exception if one cannot be found. An <code>opt</code> method returns a default value instead of throwing an
 * exception, and so is useful for obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an object, which you can cast or query for type. There are also
 * typed <code>get</code> and <code>opt</code> methods that do type checking and type coersion for you.
 * <p>
 * The <code>put</code> methods adds values to an object. For example,
 *
 * <pre>
 * myString = new JSONObject().put(&quot;JSON&quot;, &quot;Hello, World!&quot;).toString();
 * </pre>
 *
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to the JSON sysntax rules. The constructors are more forgiving
 * in the texts they will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote or single quote, and if they do not contain leading or
 * trailing spaces, and if they do not contain any of these characters: <code>{ } [ ] / \ : , = ; #</code> and if they do not look like
 * numbers and if they are not the reserved words <code>true</code>, <code>false</code>, or <code>null</code>.</li>
 * <li>Keys can be followed by <code>=</code> or <code>=></code> as well as by <code>:</code>.</li>
 * <li>Values can be followed by <code>;</code> <small>(semicolon)</small> as well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0-</code> <small>(octal)</small> or <code>0x-</code> <small>(hex)</small> prefix.</li>
 * <li>Comments written in the slashshlash, slashstar, and hash conventions will be ignored.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2
 */
public class JSONObject extends AbstractJSONValue {

    private static final long serialVersionUID = 3666538885751033341L;

    /**
     * The logger reference.
     */
    protected static final AtomicReference<Logger> LOGGER = new AtomicReference<Logger>();

    /**
     * Sets the logger.
     *
     * @param logger The logger
     */
    public static void setLogger(final Logger logger) {
        LOGGER.set(logger);
    }

    private static final AtomicInteger MAX_SIZE = new AtomicInteger(0);

    /**
     * Sets the max. allowed size of a JSON object.
     *
     * @param maxSize The max. allowed size or a value less than/equal to zero if unlimited
     */
    public static void setMaxSize(final int maxSize) {
        MAX_SIZE.set(maxSize <= 0 ? 0 : maxSize);
    }

    private static final String STR_TRUE = "true".intern();
    private static final String STR_FALSE = "false".intern();
    private static final String STR_NULL = "null".intern();

    /**
     * JSONObject.NULL is equivalent to the value that JavaScript calls null, whilst Java's null is equivalent to the value that JavaScript
     * calls undefined.
     */
    private static final class Null implements Cloneable {

        public Null() {
            super();
        }

        /**
         * There is only intended to be a single instance of the NULL object, so the clone method returns itself.
         *
         * @return NULL.
         */
        @Override
        protected Object clone() {
            return this;
        }

        /**
         * A Null object is equal to the null value and to itself.
         *
         * @param object An object to test for nullness.
         * @return true if the object parameter is the JSONObject.NULL object or null.
         */
        @Override
        public boolean equals(final Object object) {
            return object == null || object == this;
        }

        /**
         * Get the "null" string value.
         *
         * @return The string "null".
         */
        @Override
        public String toString() {
            return "null".intern();
        }
    }

    /**
     * The hash map where the JSONObject's properties are kept.
     */
    private final Map<String, Object> myHashMap;

    /**
     * It is sometimes more convenient and less ambiguous to have a <code>NULL</code> object than to use Java's <code>null</code> value.
     * <code>JSONObject.NULL.equals(null)</code> returns <code>true</code>. <code>JSONObject.NULL.toString()</code> returns
     * <code>"null"</code>.
     */
    public static final Object NULL = new Null();

    /**
     * Construct an empty JSONObject.
     */
    public JSONObject() {
        super();
        this.myHashMap = new LinkedHashMap<String, Object>();
    }

    /**
     * Construct an empty JSONObject.
     */
    public JSONObject(final int initialCapacity) {
        super();
        this.myHashMap = new LinkedHashMap<String, Object>(initialCapacity);
    }

    /**
     * Internal constructor.
     *
     * @param myHashMap The map to use
     */
    JSONObject(Map<String, Object> myHashMap, boolean internal) {
        super();
        this.myHashMap = myHashMap;
    }

    /**
     * Construct a JSONObject from a subset of another JSONObject. An array of strings is used to identify the keys that should be copied.
     * Missing keys are ignored.
     *
     * @param jo A JSONObject.
     * @param sa An array of strings.
     * @exception JSONException If a value is a non-finite number.
     */
    public JSONObject(final JSONObject jo, final String[] sa) throws JSONException {
        this();
        for (int i = 0; i < sa.length; i += 1) {
            putOpt(sa[i], jo.opt(sa[i]));
        }
    }

    /**
     * Construct a JSONObject from a JSONTokener.
     *
     * @param x A JSONTokener object containing the source string.
     * @throws JSONException If there is a syntax error in the source string.
     */
    public JSONObject(final JSONTokener x) throws JSONException {
        this();
        parseJSONTokener(x);
    }

    /**
     * Parses a JSONTokener and fills its values into this JSONObject
     *
     * @param tokener A JSONTokener object containing the source string
     * @throws JSONException
     */
    private final void parseJSONTokener(final JSONTokener tokener) throws JSONException {
        char c;
        String key;
        if (tokener.nextClean() != '{') {
            throw tokener.syntaxError("A JSONObject text must begin with '{'");
        }
        for (;;) {
            c = tokener.nextClean();
            switch (c) {
            case 0:
                throw tokener.syntaxError("A JSONObject text must end with '}'");
            case '}':
                return;
            default:
                tokener.back();
                key = tokener.nextValue().toString();
            }
            /*
             * The key is followed by ':'. We will also tolerate '=' or '=>'.
             */
            c = tokener.nextClean();
            if (c == '=') {
                if (tokener.next() != '>') {
                    tokener.back();
                }
            } else if (c != ':') {
                throw tokener.syntaxError("Expected a ':' after a key");
            }
            put(key, tokener.nextValue());
            /*
             * Pairs are separated by ','. We will also tolerate ';'.
             */
            switch (tokener.nextClean()) {
            case ';':
            case ',':
                if (tokener.nextClean() == '}') {
                    return;
                }
                tokener.back();
                break;
            case '}':
                return;
            default:
                throw tokener.syntaxError("Expected a ',' or '}'");
            }
        }
    }

    /**
     * Construct a JSONObject from a JSONObject.
     *
     * @param other A JSONObject to initialize the contents of the JSONObject.
     */
    public JSONObject(final JSONObject other) {
        this(null == other ? null : other.myHashMap);
    }

    /**
     * Construct a JSONObject from a Map.
     *
     * @param map A map object that can be used to initialize the contents of the JSONObject.
     */
    public JSONObject(final Map<String, ? extends Object> map) {
        super();
        if (null == map || map.isEmpty()) {
            this.myHashMap = new LinkedHashMap<String, Object>();
        } else {
            final int max = MAX_SIZE.get();
            final int size = map.size();
            if (max > 0 && size > max) {
                throw new IllegalStateException("Max. size (" + max + ") for JSON object exceeded");
            }
            this.myHashMap = new LinkedHashMap<String, Object>(map.size());
            for (final Map.Entry<String, ? extends Object> entry : map.entrySet()) {
                final Object value = entry.getValue();
                if (value instanceof JSONValue) {
                    final JSONValue jsonValue = (JSONValue) value;
                    if (jsonValue.isArray()) {
                        myHashMap.put(entry.getKey(), new JSONArray(jsonValue.toArray()));
                    } else {
                        myHashMap.put(entry.getKey(), new JSONObject(jsonValue.toObject()));
                    }
                } else if (value instanceof Collection) {
                    myHashMap.put(entry.getKey(), new JSONArray((Collection<Object>) value));
                } else if (value instanceof Map) {
                    myHashMap.put(entry.getKey(), new JSONObject((Map<String, Object>) value));
                } else {
                    myHashMap.put(entry.getKey(), value);
                }
            }
        }
    }

    /**
     * Construct a JSONObject from an Object, using reflection to find the public members. The resulting JSONObject's keys will be the
     * strings from the names array, and the values will be the field values associated with those keys in the object. If a key is not found
     * or not visible, then it will not be copied into the new JSONObject.
     *
     * @param object An object that has fields that should be used to make a JSONObject.
     * @param names An array of strings, the names of the fields to be used from the object.
     */
    public JSONObject(final Object object, final String names[]) {
        this();
        final Class<?> c = object.getClass();
        for (int i = 0; i < names.length; i += 1) {
            try {
                final String name = names[i];
                final Field field = c.getField(name);
                final Object value = field.get(object);
                this.put(name, value);
            } catch (final Exception e) {
                /* forget about it */
            }
        }
    }

    /**
     * Construct a JSONObject from a reader.
     *
     * @param reader A reader beginning with <code>{</code>&nbsp;<small>(left brace)</small> and ending with <code>}</code>
     *            &nbsp;<small>(right brace)</small>.
     * @exception JSONException If there is a syntax error in reader's content.
     */
    public JSONObject(final Reader reader) throws JSONException {
        this();
        if (null == reader) {
            throw new JSONException("Reader must not be null.");
        }
        parse(reader, this);
    }

    /**
     * Construct a JSONObject from a string. This is the most commonly used JSONObject constructor.
     *
     * @param string A string beginning with <code>{</code>&nbsp;<small>(left brace)</small> and ending with <code>}</code>
     *            &nbsp;<small>(right brace)</small>.
     * @exception JSONException If there is a syntax error in the source string.
     */
    public JSONObject(final String string) throws JSONException {
        this();
        if (null == string) {
            throw new JSONException("String must not be null.");
        }
        if (!"{}".equals(string)) {
            parse(new UnsynchronizedStringReader(string), this);
        }
    }

    /**
     * Gets the reference to the internal map.
     *
     * @return The internal map
     */
    Map<String, Object> getMyHashMap() {
        return myHashMap;
    }

    @Override
    public boolean isEqualTo(final JSONValue jsonValue) {
        if (jsonValue == this) {
            return true;
        }
        if ((null == jsonValue) || !jsonValue.isObject()) {
            return false;
        }
        final Map<String, Object> m = jsonValue.toObject().myHashMap;
        if (myHashMap.size() != m.size()) {
            return false;
        }
        try {
            final Iterator<Entry<String, Object>> i = myHashMap.entrySet().iterator();
            while (i.hasNext()) {
                final Entry<String, Object> e = i.next();
                final String key = e.getKey();
                final Object value = e.getValue();
                if (isNull(value)) {
                    if (!m.containsKey(key)) {
                        return false;
                    }
                    if (!isNull(m.get(key))) {
                        return false;
                    }
                } else {
                    if (value instanceof JSONValue) {
                        final Object object = m.get(key);
                        if (!(object instanceof JSONValue)) {
                            return false;
                        } else if (!((JSONValue) value).isEqualTo((JSONValue) object)) {
                            return false;
                        }
                    } else if (!value.equals(m.get(key))) {
                        return false;
                    }
                }
            }
        } catch (final ClassCastException unused) {
            return false;
        } catch (final NullPointerException unused) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(final Object object) {
        if (object instanceof JSONObject) {
            return isEqualTo((JSONObject) object);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return myHashMap.hashCode();
    }

    /**
     * Gets the {@link Map map} view for this JSON object.
     *
     * @return The map
     */
    public Map<String, Object> asMap() {
        final Map<String, Object> retval = new LinkedHashMap<String, Object>(myHashMap.size());
        for (final Map.Entry<String, ? extends Object> entry : myHashMap.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof JSONValue) {
                final JSONValue jsonValue = (JSONValue) value;
                if (jsonValue.isArray()) {
                    retval.put(entry.getKey(), jsonValue.toArray().asList());
                } else {
                    retval.put(entry.getKey(), jsonValue.toObject().asMap());
                }
            } else {
                retval.put(entry.getKey(), value);
            }
        }
        return retval;
    }

    /**
     * Fills JSONObject with the given source string. This method is dedicated for <b>re-using</b> a JSONObject in combination with the
     * <code>reset()</code> method, since it gives the same possibility as the common-used <code>JSONObject(String string)</code> constructor
     * to create a JSONObject from a string.
     *
     * @param string A string beginning with <code>{</code>&nbsp;<small>(left brace)</small> and ending with <code>}</code>
     *            &nbsp;<small>(right brace)</small>.
     * @return this.
     * @exception JSONException If there is a syntax error in the source string.
     * @see #reset()
     */
    public JSONObject parseJSONString(final String string) throws JSONException {
        parse(new UnsynchronizedStringReader(string), this);
        return this;
    }

    /**
     * Accumulate values under a key. It is similar to the put method except that if there is already an object stored under the key then a
     * JSONArray is stored under the key to hold all of the accumulated values. If there is already a JSONArray, then the new value is
     * appended to it. In contrast, the put method replaces the previous value.
     *
     * @param key A key string.
     * @param value An object to be accumulated under the key.
     * @return this.
     * @throws JSONException If the value is an invalid number or if the key is null.
     */
    public JSONObject accumulate(final String key, final Object value) throws JSONException {
        final Object o = opt(key);
        if (o == null) {
            put(key, value instanceof JSONArray ? new JSONArray().put(value) : value);
        } else if (o instanceof JSONArray) {
            ((JSONArray) o).put(value);
        } else {
            put(key, new JSONArray().put(o).put(value));
        }
        return this;
    }

    /**
     * Accumulate values under a key. It is similar to the put method except that if there is already an object stored under the key then a
     * JSONArray is stored under the key to hold all of the accumulated values. If there is already a JSONArray, then the new value is
     * appended to it. In contrast, the put method replaces the previous value.
     *
     * @param key A key string.
     * @param value An object to be accumulated under the key.
     * @param forceArray <code>true</code> to enforce to accumulate under a JSON array; otherwise <code>false</code>
     * @return this.
     * @throws JSONException If the value is an invalid number or if the key is null.
     */
    public JSONObject accumulate(final String key, final Object value, final boolean forceArray) throws JSONException {
        final Object o = opt(key);
        if (o == null) {
            if (value instanceof JSONArray) {
                put(key, new JSONArray().put(value));
            } else {
                put(key, forceArray ? new JSONArray().put(value) : value);
            }
        } else if (o instanceof JSONArray) {
            ((JSONArray) o).put(value);
        } else {
            put(key, new JSONArray().put(o).put(value));
        }
        return this;
    }

    /**
     * Append values to the array under a key. If the key does not exist in the JSONObject, then the key is put in the JSONObject with its
     * value being a JSONArray containing the value parameter. If the key was already associated with a JSONArray, then the value parameter
     * is appended to it.
     *
     * @param key A key string.
     * @param value An object to be accumulated under the key.
     * @return this.
     * @throws JSONException If the key is null or if the current value associated with the key is not a JSONArray.
     */
    public JSONObject append(final String key, final Object value) throws JSONException {
        final Object o = opt(key);
        if (o == null) {
            put(key, new JSONArray().put(value));
        } else if (o instanceof JSONArray) {
            put(key, ((JSONArray) o).put(value));
        } else {
            throw new JSONException("JSONObject[" + key + "] is not a JSONArray.");
        }
        return this;
    }

    /**
     * Resets this JSONObject for re-use
     */
    @Override
    public void reset() {
        myHashMap.clear();
    }

    /**
     * Produce a string from a double. The string "null" will be returned if the number is not finite.
     *
     * @param d A double.
     * @return A String.
     */
    static public final String doubleToString(final double d) {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            return STR_NULL;
        }

        // Shave off trailing zeros and decimal point, if possible.

        String s = Double.toString(d);
        if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }

    /**
     * Get the value object associated with a key.
     *
     * @param key A key string.
     * @return The object associated with the key.
     * @throws JSONException if the key is not found.
     */
    public Object get(final String key) throws JSONException {
        final Object o = opt(key);
        if (o == null) {
            throw new JSONException("JSONObject[" + quote(key) + "] not found.");
        }
        return o;
    }

    /**
     * Get the boolean value associated with a key.
     *
     * @param key A key string.
     * @return The truth.
     * @throws JSONException if the value is not a Boolean or the String "true" or "false".
     */
    public boolean getBoolean(final String key) throws JSONException {
        final Object o = get(key);
        if (o.equals(Boolean.FALSE) || (o instanceof String && ((String) o).equalsIgnoreCase(STR_FALSE))) {
            return false;
        } else if (o.equals(Boolean.TRUE) || (o instanceof String && ((String) o).equalsIgnoreCase(STR_TRUE))) {
            return true;
        }
        throw new JSONException("JSONObject[" + quote(key) + "] is not a Boolean.");
    }

    /**
     * Get the double value associated with a key.
     *
     * @param key A key string.
     * @return The numeric value.
     * @throws JSONException if the key is not found or if the value is not a Number object and cannot be converted to a number.
     */
    public double getDouble(final String key) throws JSONException {
        final Object o = get(key);
        try {
            return o instanceof Number ? ((Number) o).doubleValue() : Double.parseDouble((String) o);
        } catch (final Exception e) {
            throw new JSONException("JSONObject[" + quote(key) + "] is not a number.", e);
        }
    }

    /**
     * Get the int value associated with a key. If the number value is too large for an int, it will be clipped.
     *
     * @param key A key string.
     * @return The integer value.
     * @throws JSONException if the key is not found or if the value cannot be converted to an integer.
     */
    public int getInt(final String key) throws JSONException {
        final Object o = get(key);
        return o instanceof Number ? ((Number) o).intValue() : (int) getDouble(key);
    }

    /**
     * Get the JSONArray value associated with a key.
     *
     * @param key A key string.
     * @return A JSONArray which is the value.
     * @throws JSONException if the key is not found or if the value is not a JSONArray.
     */
    public JSONArray getJSONArray(final String key) throws JSONException {
        final Object o = get(key);
        if (o instanceof JSONArray) {
            return (JSONArray) o;
        }
        throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONArray, but " + o.getClass().getName());
    }

    /**
     * Get the JSONObject value associated with a key.
     *
     * @param key A key string.
     * @return A JSONObject which is the value.
     * @throws JSONException if the key is not found or if the value is not a JSONObject.
     */
    public JSONObject getJSONObject(final String key) throws JSONException {
        final Object o = get(key);
        if (o instanceof JSONObject) {
            return (JSONObject) o;
        }
        throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONObject, but " + o.getClass().getName());
    }

    /**
     * Get the long value associated with a key. If the number value is too long for a long, it will be clipped.
     *
     * @param key A key string.
     * @return The long value.
     * @throws JSONException if the key is not found or if the value cannot be converted to a long.
     */
    public long getLong(final String key) throws JSONException {
        final Object o = get(key);
        return o instanceof Number ? ((Number) o).longValue() : (long) getDouble(key);
    }

    /**
     * Get the string associated with a key.
     *
     * @param key A key string.
     * @return A string which is the value.
     * @throws JSONException if the key is not found.
     */
    public String getString(final String key) throws JSONException {
        return get(key).toString();
    }

    /**
     * Determine if the JSONObject contains a specific key.
     *
     * @param key A key string.
     * @return true if the key exists in the JSONObject.
     */
    public boolean has(final String key) {
        return this.myHashMap.containsKey(key);
    }

    /**
     * Determine if the JSONObject contains a specific key AND if the value associated with the key is not null.
     *
     * @param key A key string.
     * @return true if the JSONObject contains a specific key AND if the value associated with the key is not null.
     * @see #isNull(String)
     */
    public boolean hasAndNotNull(final String key) {
        final Object opt = key == null ? null : this.myHashMap.get(key);
        return null != opt && !JSONObject.NULL.equals(opt);
    }

    /**
     * Determine if the value associated with the key is null or if there is no value.
     *
     * @param key A key string.
     * @return true if there is no value associated with the key or if the value is the JSONObject.NULL object.
     */
    public boolean isNull(final String key) {
        final Object opt = opt(key);
        return null == opt || JSONObject.NULL.equals(opt);
    }

    /**
     * Get an enumeration of the keys of the JSONObject.
     *
     * @return An iterator of the keys.
     */
    public Iterator<String> keys() {
        return this.myHashMap.keySet().iterator();
    }

    /**
     * Get the key set of the JSONObject.
     *
     * @return A key set
     */
    public Set<String> keySet() {
        return this.myHashMap.keySet();
    }

    /**
     * Get the entry set of the JSONObject
     *
     * @return A entry set
     */
    public Set<Map.Entry<String, Object>> entrySet() {
        return this.myHashMap.entrySet();
    }

    @Override
    public boolean isEmpty() {
        return myHashMap.isEmpty();
    }

    /**
     * Get the number of keys stored in the JSONObject.
     *
     * @return The number of keys in the JSONObject.
     */
    @Override
    public int length() {
        return this.myHashMap.size();
    }

    /**
     * Produce a JSONArray containing the names of the elements of this JSONObject.
     *
     * @return A JSONArray containing the key strings, or null if the JSONObject is empty.
     */
    public JSONArray names() {
        final JSONArray ja = new JSONArray();
        final Set<String> keys = keySet();
        for (final String name : keys) {
            ja.put(name);
        }
        return ja.length() == 0 ? null : ja;
    }

    /**
     * Produce a string from a Number.
     *
     * @param n A Number
     * @return A String.
     * @throws JSONException If n is a non-finite number.
     */
    static public final String numberToString(final Number n) throws JSONException {
        if (n == null) {
            throw new JSONException("Null pointer");
        }

        // Shave off trailing zeros and decimal point, if possible.

        String s = n.toString();
        if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }

    /**
     * Get an optional value associated with a key.
     *
     * @param key A key string.
     * @return An object which is the value, or null if there is no value.
     */
    public Object opt(final String key) {
        return key == null ? null : this.myHashMap.get(key);
    }

    /**
     * Get an optional boolean associated with a key. It returns false if there is no such key, or if the value is not Boolean.TRUE or the
     * String "true".
     *
     * @param key A key string.
     * @return The truth.
     */
    public boolean optBoolean(final String key) {
        return optBoolean(key, false);
    }

    /**
     * Get an optional boolean associated with a key. It returns the defaultValue if there is no such key, or if it is not a Boolean or the
     * String "true" or "false" (case insensitive).
     *
     * @param key A key string.
     * @param defaultValue The default.
     * @return The truth.
     */
    public boolean optBoolean(final String key, final boolean defaultValue) {
        try {
            return getBoolean(key);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Put a key/value pair in the JSONObject, where the value will be a JSONArray which is produced from a Collection.
     *
     * @param key A key string.
     * @param value A Collection value.
     * @return this.
     * @throws JSONException
     */
    public JSONObject put(final String key, final Collection<? extends Object> value) throws JSONException {
        put(key, new JSONArray(value));
        return this;
    }

    /**
     * Get an optional double associated with a key, or NaN if there is no such key or if its value is not a number. If the value is a
     * string, an attempt will be made to evaluate it as a number.
     *
     * @param key A string which is the key.
     * @return An object which is the value.
     */
    public double optDouble(final String key) {
        return optDouble(key, Double.NaN);
    }

    /**
     * Get an optional double associated with a key, or the defaultValue if there is no such key or if its value is not a number. If the
     * value is a string, an attempt will be made to evaluate it as a number.
     *
     * @param key A key string.
     * @param defaultValue The default.
     * @return An object which is the value.
     */
    public double optDouble(final String key, final double defaultValue) {
        try {
            final Object o = opt(key);
            return o instanceof Number ? ((Number) o).doubleValue() : new Double((String) o).doubleValue();
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional int value associated with a key, or zero if there is no such key or if the value is not a number. If the value is a
     * string, an attempt will be made to evaluate it as a number.
     *
     * @param key A key string.
     * @return An object which is the value.
     */
    public int optInt(final String key) {
        return optInt(key, 0);
    }

    /**
     * Get an optional int value associated with a key, or the default if there is no such key or if the value is not a number. If the value
     * is a string, an attempt will be made to evaluate it as a number.
     *
     * @param key A key string.
     * @param defaultValue The default.
     * @return An object which is the value.
     */
    public int optInt(final String key, final int defaultValue) {
        try {
            return getInt(key);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional JSONArray associated with a key. It returns null if there is no such key, or if its value is not a JSONArray.
     *
     * @param key A key string.
     * @return A JSONArray which is the value.
     */
    public JSONArray optJSONArray(final String key) {
        final Object o = opt(key);
        return o instanceof JSONArray ? (JSONArray) o : null;
    }

    /**
     * Get an optional JSONObject associated with a key. It returns null if there is no such key, or if its value is not a JSONObject.
     *
     * @param key A key string.
     * @return A JSONObject which is the value.
     */
    public JSONObject optJSONObject(final String key) {
        final Object o = opt(key);
        return o instanceof JSONObject ? (JSONObject) o : null;
    }

    /**
     * Get an optional long value associated with a key, or zero if there is no such key or if the value is not a number. If the value is a
     * string, an attempt will be made to evaluate it as a number.
     *
     * @param key A key string.
     * @return An object which is the value.
     */
    public long optLong(final String key) {
        return optLong(key, 0);
    }

    /**
     * Get an optional long value associated with a key, or the default if there is no such key or if the value is not a number. If the
     * value is a string, an attempt will be made to evaluate it as a number.
     *
     * @param key A key string.
     * @param defaultValue The default.
     * @return An object which is the value.
     */
    public long optLong(final String key, final long defaultValue) {
        try {
            return getLong(key);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional string associated with a key. It returns an empty string if there is no such key. If the value is not a string and is
     * not {@link #NULL}, then it is converted to a string.
     *
     * @param key A key string.
     * @return A string which is the value.
     */
    public String optString(final String key) {
        return optString(key, "");
    }

    /**
     * Get an optional string associated with a key. It returns the <tt>defaultValue</tt> if there is no such key or is {@link #NULL}.
     *
     * @param key A key string.
     * @param defaultValue The default.
     * @return A string which is the value.
     */
    public String optString(final String key, final String defaultValue) {
        final Object o = opt(key);
        if (o == null) {
            return defaultValue;
        }
        return NULL.equals(o) ? defaultValue : o.toString();
    }

    /**
     * Put a key/boolean pair in the JSONObject.
     *
     * @param key A key string.
     * @param value A boolean which is the value.
     * @return this.
     * @throws JSONException If the key is null.
     */
    public JSONObject put(final String key, final boolean value) throws JSONException {
        put(key, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    /**
     * Put a key/double pair in the JSONObject.
     *
     * @param key A key string.
     * @param value A double which is the value.
     * @return this.
     * @throws JSONException If the key is null or if the number is invalid.
     */
    public JSONObject put(final String key, final double value) throws JSONException {
        put(key, Double.valueOf(value));
        return this;
    }

    /**
     * Put a key/int pair in the JSONObject.
     *
     * @param key A key string.
     * @param value An int which is the value.
     * @return this.
     * @throws JSONException If the key is null.
     */
    public JSONObject put(final String key, final int value) throws JSONException {
        put(key, Integer.valueOf(value));
        return this;
    }

    /**
     * Put a key/long pair in the JSONObject.
     *
     * @param key A key string.
     * @param value A long which is the value.
     * @return this.
     * @throws JSONException If the key is null.
     */
    public JSONObject put(final String key, final long value) throws JSONException {
        put(key, Long.valueOf(value));
        return this;
    }

    /**
     * Put a key/value pair in the JSONObject, where the value will be a JSONObject which is produced from a Map.
     *
     * @param key A key string.
     * @param value A Map value.
     * @return this.
     * @throws JSONException
     */
    public JSONObject put(final String key, final Map<String, ? extends Object> value) throws JSONException {
        put(key, new JSONObject(value));
        return this;
    }

    /**
     * Put a key/value pair in the JSONObject. If the value is null, then the key will be removed from the JSONObject if it is present.
     *
     * @param key A key string.
     * @param value An object which is the value. It should be of one of these types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
     *            String, or the JSONObject.NULL object.
     * @return this.
     * @throws JSONException If the value is non-finite number or if the key is null.
     */
    public JSONObject put(final String key, final Object value) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        }
        if (value != null) {
            final int max = MAX_SIZE.get();
            if (max > 0 && this.myHashMap.size() >= max) {
                throw new IllegalStateException("Max. size (" + max + ") for JSON object exceeded");
            }
            this.myHashMap.put(key, value);
        } else {
            remove(key);
        }
        return this;
    }

    /**
     * Put a key/value pair in the JSONObject. If the value is null, then the key will be removed from the JSONObject if it is present.
     *
     * @param key A key string.
     * @param value An object which is the value. It should be of one of these types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
     *            String, or the JSONObject.NULL object.
     * @return this.
     * @throws IllegalArgumentException If the value is non-finite number or if the key is <code>null</code>
     */
    public JSONObject putSafe(final String key, final Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Null key.");
        }
        if (value != null) {
            final int max = MAX_SIZE.get();
            if (max > 0 && this.myHashMap.size() >= max) {
                throw new IllegalStateException("Max. size (" + max + ") for JSON object exceeded");
            }
            this.myHashMap.put(key, value);
        } else {
            remove(key);
        }
        return this;
    }

    /**
     * Put a key/value pair in the JSONObject, but only if the key and the value are both non-null.
     *
     * @param key A key string.
     * @param value An object which is the value. It should be of one of these types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
     *            String, or the JSONObject.NULL object.
     * @return this.
     * @throws JSONException If the value is a non-finite number.
     */
    public JSONObject putOpt(final String key, final Object value) throws JSONException {
        if (key != null && value != null) {
            put(key, value);
        }
        return this;
    }

    /**
     * Produce a string in double quotes with backslash sequences in all the right places. A backslash will be inserted within </, allowing
     * JSON text to be delivered in HTML. In JSON text, a string cannot contain a control character or an unescaped quote or backslash.
     *
     * @param string A String
     * @return A String correctly formatted for insertion in a JSON text.
     */
    public static final String quote(final String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char b;
        char c = 0;
        int i;
        final int len = string.length();

        final StringBuilder sb = new StringBuilder(len + 4);
        String t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
            case '\\':
            case '"':
                sb.append('\\');
                sb.append(c);
                break;
            case '/':
                if (b == '<') {
                    sb.append('\\');
                }
                sb.append(c);
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\t':
                sb.append("\\t");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\r':
                sb.append("\\r");
                break;
            default:
                if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
                    t = "000" + Integer.toHexString(c);
                    sb.append("\\u").append(t.substring(t.length() - 4));
                } else {
                    sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Remove a name and its value, if present.
     *
     * @param key The name to be removed.
     * @return The value that was associated with the name, or null if there was no value.
     */
    public Object remove(final String key) {
        return this.myHashMap.remove(key);
    }

    /**
     * Produce a JSONArray containing the values of the members of this JSONObject.
     *
     * @param names A JSONArray containing a list of key strings. This determines the sequence of the values in the result.
     * @return A JSONArray of values.
     * @throws JSONException If any of the values are non-finite numbers.
     */
    public JSONArray toJSONArray(final JSONArray names) throws JSONException {
        if (names == null || names.length() == 0) {
            return null;
        }
        final JSONArray ja = new JSONArray();
        for (int i = 0; i < names.length(); i += 1) {
            ja.put(this.opt(names.getString(i)));
        }
        return ja;
    }

    private static final String EMPTY = "{}".intern();

    /**
     * Make a JSON text of this JSONObject. For compactness, no whitespace is added. If this would not result in a syntactically correct
     * JSON text, then null will be returned instead.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a printable, displayable, portable, transmittable representation of the object, beginning with <code>{</code>
     *         &nbsp;<small>(left brace)</small> and ending with <code>}</code>&nbsp;<small>(right brace)</small>.
     */
    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Make a JSON text of this JSONObject. For compactness, no whitespace is added. If this would not result in a syntactically correct
     * JSON text, then null will be returned instead.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param asciiOnly Whether to write only ASCII characters
     * @return a printable, displayable, portable, transmittable representation of the object, beginning with <code>{</code>
     *         &nbsp;<small>(left brace)</small> and ending with <code>}</code>&nbsp;<small>(right brace)</small>.
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
                logger.logp(Level.SEVERE, JSONObject.class.getName(), "toString()", e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Make a pretty-printed JSON text of this JSONObject.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param indentFactor The number of spaces to add to each level of indentation.
     * @return a printable, displayable, portable, transmittable representation of the object, beginning with <code>{</code>
     *         &nbsp;<small>(left brace)</small> and ending with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If the object contains an invalid number.
     */
    public String toString(final int indentFactor) throws JSONException {
        return toString(indentFactor, 0);
    }

    /**
     * Make a pretty-printed JSON text of this JSONObject.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param indentFactor The number of spaces to add to each level of indentation.
     * @param indent The indentation of the top level.
     * @return a printable, displayable, transmittable representation of the object, beginning with <code>{</code>&nbsp;<small>(left
     *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If the object contains an invalid number.
     */
    @Override
    public String toString(final int indentFactor, final int indent) throws JSONException {
        final int n = length();
        if (n == 0) {
            return EMPTY;
        }

        JsonGenerator jGenerator = null;
        try {
            final UnsynchronizedStringWriter writer = new UnsynchronizedStringWriter(n << 4);
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
     * Make a JSON text of an Object value. If the object has an value.toJSONString() method, then that method will be used to produce the
     * JSON text. The method is required to produce a strictly conforming text. If the object does not contain a toJSONString method (which
     * is the most common case), then a text will be produced by the rules.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param value The value to be serialized.
     * @return a printable, displayable, transmittable representation of the object, beginning with <code>{</code>&nbsp;<small>(left
     *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If the value is or contains an invalid number.
     */
    static final String valueToString(final Object value) throws JSONException {
        if (value == null || NULL.equals(value)) {
            return STR_NULL;
        }
        if (value instanceof JSONString) {
            Object o;
            try {
                o = ((JSONString) value).toJSONString();
            } catch (final Exception e) {
                throw new JSONException(e);
            }
            if (o instanceof String) {
                return (String) o;
            }
            throw new JSONException("Bad value from toJSONString: " + o);
        }
        if (value instanceof Number) {
            return numberToString((Number) value);
        }
        if (value instanceof Boolean || value instanceof JSONValue) {
            return value.toString();
        }
        return quote(value.toString());
    }

    /**
     * Make a prettyprinted JSON text of an object value.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param value The value to be serialized.
     * @param indentFactor The number of spaces to add to each level of indentation.
     * @param indent The indentation of the top level.
     * @return a printable, displayable, transmittable representation of the object, beginning with <code>{</code>&nbsp;<small>(left
     *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If the object contains an invalid number.
     */
    static final String valueToString(final Object value, final int indentFactor, final int indent) throws JSONException {
        if (value == null || NULL.equals(value)) {
            return STR_NULL;
        }
        try {
            if (value instanceof JSONString) {
                final Object o = ((JSONString) value).toJSONString();
                if (o instanceof String) {
                    return (String) o;
                }
            }
        } catch (final Exception e) {
            /* forget about it */
        }
        if (value instanceof Number) {
            return numberToString((Number) value);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof JSONValue) {
            return ((JSONValue) value).toString(indentFactor, indent);
        }
        return quote(value.toString());
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
     * Writes specified JSON object to given generator.
     *
     * @param jo The JSON object
     * @param asciiOnly Whether to write only ASCII characters
     * @param jGenerator The generator
     * @throws IOException If an I/O error occurs
     * @throws JSONException IOf a JSON error occurs
     */
    protected static void write(final JSONObject jo, final JsonGenerator jGenerator) throws IOException, JSONException {
        jGenerator.writeStartObject(); // {
        try {
            final Map<String, Object> myHashMap = jo.myHashMap;
            final int len = myHashMap.size();
            if (len > 0) {
                final Iterator<Map.Entry<String, Object>> iter = myHashMap.entrySet().iterator();
                for (int i = 0; i < len; i++) {
                    final Entry<String, Object> entry = iter.next();
                    jGenerator.writeFieldName(entry.getKey());
                    write(entry.getValue(), jGenerator);
                }
            }
        } finally {
            writeEndAndFlush(jGenerator, true);
        }
    }

    /**
     * Parses readers' content to a JSON value.
     *
     * @param reader The reader
     * @return The parsed JSON value
     * @throws JSONException If parsing fails
     */
    public static JSONValue parse(final Reader reader) throws JSONException {
        JsonParser jParser = null;
        try {
            jParser = createParser(reader);
            final JsonToken token = jParser.nextToken();
            if (token == JsonToken.START_OBJECT) {
                return parse(jParser, null);
            }
            if (token == JsonToken.START_ARRAY) {
                return JSONArray.parse(jParser, null);
            }
            throw new JSONException("Neither a JSONObject nor a JSONArray");
        } catch (final IOException e) {
            throw new JSONException(e);
        } finally {
            close(jParser);
        }
    }

    /**
     * Parses specified reader's content to a JSON object.
     *
     * @param reader The reader to read from
     * @return The parsed JSON object
     * @throws JSONException If an error occurs
     */
    protected static JSONObject parse(final Reader reader, final JSONObject optObject) throws JSONException {
        JsonParser jParser = null;
        try {
            jParser = createParser(reader);
            // Check start
            {
                final JsonToken token = jParser.nextToken();
                if (token != JsonToken.START_OBJECT) {
                    final String content = readFrom(reader, 0x2000);
                    final String sep = System.getProperty("line.separator");
                    throw new JSONException("A JSONObject text must begin with '{', but got \"" + (null == token ? "null" : token.toString()) + "\" parse event." + sep + "Rest:" + sep + content);
                }
            }
            return parse(jParser, optObject);
        } catch (final IOException e) {
            throw new JSONException(e);
        } finally {
            close(jParser);
        }
    }

    /**
     * Parses specified JSON object from given parser.
     *
     * @param jParser The JSON parser with {@link JsonToken#START_OBJECT} already consumed
     * @return The JSON object
     * @throws JSONException If an error occurs
     */
    protected static JSONObject parse(final JsonParser jParser, final JSONObject optObject) throws JSONException {
        try {
            final JSONObject jo = null == optObject ? new JSONObject() : optObject;
            JsonToken current = jParser.nextToken();
            while (current != JsonToken.END_OBJECT) {
                if (current != JsonToken.FIELD_NAME) {
                    throw new JSONException("JSON parse error: field name expected, but got " + (null == current ? "null" : current.toString()));
                }
                final String fieldName = jParser.getCurrentName();
                // Move from field name to field value
                current = jParser.nextToken();
                switch (current) {
                case START_OBJECT:
                    jo.put(fieldName, parse(jParser, null));
                    break;
                case START_ARRAY:
                    jo.put(fieldName, JSONArray.parse(jParser, null));
                    break;
                case VALUE_FALSE:
                    jo.put(fieldName, false);
                    break;
                case VALUE_NULL:
                    jo.put(fieldName, JSONObject.NULL);
                    break;
                case VALUE_NUMBER_FLOAT:
                    try {
                        jo.put(fieldName, jParser.getDecimalValue());
                    } catch (final RuntimeException e) {
                        final String text = jParser.getText();
                        if (!"NaN".equals(text)) {
                            throw new JSONException("JSON parsing failed. Could not convert \"" + text + "\" to a big decimal number.", e);
                        }
                        // Discard
                    }
                    break;
                case VALUE_NUMBER_INT:
                    try {
                        jo.put(fieldName, jParser.getIntValue());
                        } catch (final JsonParseException e) {
                            // Outside of range of Java int
                            try {
                                jo.put(fieldName, jParser.getLongValue());
                            } catch (final JsonParseException pe) {
                                // Outside of range of Java long
                                // Fallback: Treat number as double, so we don't lose
                                // too much precision (#44850)
                                jo.put(fieldName, jParser.getDoubleValue());
                            }
                        }
                    break;
                case VALUE_TRUE:
                    jo.put(fieldName, true);
                    break;
                case VALUE_STRING:
                    jo.put(fieldName, jParser.getText());
                    break;
                default:
                    // Ignore
                    break;
                }
                current = jParser.nextToken();
            }
            return jo;
        } catch (final IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isArray() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isObject() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONArray toArray() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toObject() {
        return this;
    }

    private static final Pattern PATTERN_CRLF = Pattern.compile("\r?\n");

    private static String prepareJSONString(final String sJson) {
        if (null == sJson) {
            return null;
        }
        return PATTERN_CRLF.matcher(sJson).replaceAll("");
    }

}
