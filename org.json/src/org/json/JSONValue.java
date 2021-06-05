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

package org.json;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.io.Writer;

/**
 * {@link JSONValue} - The base class for all JSON representations.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface JSONValue extends Serializable {

    /**
     * Writes this JSON value to specified file
     *
     * @param file The file to write to
     * @throws JSONException If writing the JSON object fails (e.g. I/O error)
     */
    void writeTo(File file) throws JSONException;

    /**
     * Pretty-prints this JSON value to specified file
     *
     * @param file The file to write to
     * @throws JSONException If writing the JSON object fails (e.g. I/O error)
     */
    void prettyPrintTo(File file) throws JSONException;

    /**
     * Write the contents of this JSON value as JSON text to a writer. For compactness, no whitespace is added.<br>
     * Invokes {@link #write(Writer, boolean)} with latter parameter set to <code>false</code>.
     * <p>
     * <b>Warning</b>: This method assumes that the data structure is acyclically.
     *
     * @param The writer to write to
     * @return The specified writer for chained invocations
     * @throws JSONException If writing the JSON object fails (e.g. I/O error)
     */
    Writer write(Writer writer) throws JSONException;

    /**
     * Write the contents of this JSON value as JSON text to a writer. For compactness, no whitespace is added.
     * <p>
     * <b>Warning</b>: This method assumes that the data structure is acyclically.
     *
     * @param The writer to write to
     * @param asciiOnly <code>true</code> to only write ASCII characters; otherwise <code>false</code>
     * @return The specified writer for chained invocations
     * @throws JSONException If writing the JSON object fails (e.g. I/O error)
     */
    Writer write(Writer writer, boolean asciiOnly) throws JSONException;

    /**
     * Gets the stream containing this JSON value's raw data.
     *
     * @param asciiOnly <code>true</code> to only write ASCII characters; otherwise <code>false</code>
     * @return The stream
     * @throws JSONException If serializing the JSON object fails (e.g. I/O error)
     */
    InputStream getStream(boolean asciiOnly) throws JSONException;

    /**
     * Resets this JSON value for re-use.
     */
    void reset();

    /**
     * Get the number of elements stored in this JSON value.
     *
     * @return The number of elements stored in this JSON value.
     */
    int length();

    /**
     * Checks if this JSON value contains no elements.
     *
     * @return <tt>true</tt> if this JSON value contains no elements
     */
    boolean isEmpty();

    /**
     * Checks if this JSON value is equal to specified JSON value.
     *
     * @param jsonValue The JSON value
     * @return <code>true</code> if equals; otherwise <code>false</code>
     */
    boolean isEqualTo(JSONValue jsonValue);

    /**
     * Make a pretty-printed JSON text of this JSON value.
     * <p>
     * Warning: This method assumes that the data structure is acyclically.
     *
     * @param indentFactor The number of spaces to add to each level of indentation.
     * @param indent The indention of the top level.
     * @return A printable, displayable, and transmittable representation of the JSON value.
     * @throws JSONException If JSON value cannot be pretty-printed
     */
    String toString(final int indentFactor, final int indent) throws JSONException;

    /**
     * Check if this value represents a JSON array.
     *
     * @return <code>true</code> if this value represents a JSON array; otherwise <code>false</code>
     */
    boolean isArray();

    /**
     * Check if this value represents a JSON object.
     *
     * @return <code>true</code> if this value represents a JSON object; otherwise <code>false</code>
     */
    boolean isObject();

    /**
     * Gets the {@link JSONValue}'s {@link JSONObject} representation (if appropriate).
     *
     * @return The associated {@link JSONObject} or <code>null</code>
     */
    JSONObject toObject();

    /**
     * Gets the {@link JSONValue}'s {@link JSONArray} representation (if appropriate).
     *
     * @return The associated {@link JSONArray} or <code>null</code>
     */
    JSONArray toArray();

}
