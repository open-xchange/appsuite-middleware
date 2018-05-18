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

package com.openexchange.chronos.storage.rdb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.github.mangstadt.vinnie.SyntaxStyle;
import com.github.mangstadt.vinnie.VObjectParameters;
import com.github.mangstadt.vinnie.VObjectProperty;
import com.github.mangstadt.vinnie.io.Context;
import com.github.mangstadt.vinnie.io.SyntaxRules;
import com.github.mangstadt.vinnie.io.VObjectDataAdapter;
import com.github.mangstadt.vinnie.io.VObjectReader;
import com.github.mangstadt.vinnie.io.VObjectWriter;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * {@link ExtendedPropertiesCodec}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ExtendedPropertiesCodec {

    private static final byte TYPE_JSON_DEFLATE = 5;
    private static final byte TYPE_VOBJECT = 12;

    /**
     * Encodes the supplied extended properties prior passing it to the storage.
     *
     * @param extendedProperties The extended properties to encode
     * @return The encoded properties, or <code>null</code> if passed are <code>null</code> or empty
     */
    public static byte[] encode(ExtendedProperties extendedProperties) throws IOException {
        return encode(extendedProperties, TYPE_JSON_DEFLATE);
    }

    /**
     * Encodes the supplied extended property parameters prior passing it to the storage.
     *
     * @param parameters The extended property parameters to encode
     * @return The encoded parameters, or <code>null</code> if passed are <code>null</code> or empty
     */
    public static byte[] encodeParameters(List<ExtendedPropertyParameter> parameters) throws IOException {
        return encode(parameters, TYPE_JSON_DEFLATE);
    }

    /**
     * Decodes extended properties from the supplied input stream fetched from the storage.
     *
     * @param inputStream The input stream carrying the encoded extended properties
     * @return The decoded properties, or <code>null</code> if passed no data was read
     * @throws IOException
     */
    public static ExtendedProperties decode(InputStream inputStream) throws IOException {
        if (null == inputStream) {
            return null;
        }
        int type = inputStream.read();
        if (-1 == type) {
            return null; // eol
        }
        switch (type) {
            case TYPE_JSON_DEFLATE:
                return decodeDeflatedJson(inputStream);
            case TYPE_VOBJECT:
                return decodeVObjectProperties(inputStream);
            default:
                throw new IOException(new UnsupportedEncodingException(String.valueOf(type)));
        }
    }

    /**
     * Decodes extended property parameters from the supplied input stream fetched from the storage.
     *
     * @param inputStream The input stream carrying the encoded parameters
     * @return The decoded parameters, or <code>null</code> if passed no data was read
     */
    public static List<ExtendedPropertyParameter> decodeParameters(InputStream inputStream) throws IOException {
        if (null == inputStream) {
            return null;
        }
        int type = inputStream.read();
        if (-1 == type) {
            return null; // eol
        }
        switch (type) {
            case TYPE_JSON_DEFLATE:
                return decodeDeflatedJsonParameters(inputStream);
            case TYPE_VOBJECT:
                return decodeVObjectParameters(inputStream);
            default:
                throw new IOException(new UnsupportedEncodingException(String.valueOf(type)));
        }
    }

    private static byte[] encode(ExtendedProperties extendedProperties, byte type) throws IOException {
        if (null == extendedProperties || extendedProperties.isEmpty()) {
            return null;
        }
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = Streams.newByteArrayOutputStream();
            encode(extendedProperties, type, outputStream);
            return outputStream.toByteArray();
        } finally {
            Streams.close(outputStream);
        }
    }

    private static byte[] encode(List<ExtendedPropertyParameter> parameters, byte type) throws IOException {
        if (null == parameters || parameters.isEmpty()) {
            return null;
        }
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = Streams.newByteArrayOutputStream();
            encode(parameters, type, outputStream);
            return outputStream.toByteArray();
        } finally {
            Streams.close(outputStream);
        }
    }

    private static void encode(ExtendedProperties extendedProperties, byte type, ByteArrayOutputStream outputStream) throws IOException {
        outputStream.write(type);
        switch (type) {
            case TYPE_JSON_DEFLATE:
                encodeDeflatedJson(extendedProperties, outputStream);
                break;
            case TYPE_VOBJECT:
                encodeVObjectProperties(extendedProperties, outputStream);
                break;
            default:
                throw new IOException(new UnsupportedEncodingException(String.valueOf(type)));
        }
    }

    private static void encode(List<ExtendedPropertyParameter> parameters, byte type, ByteArrayOutputStream outputStream) throws IOException {
        outputStream.write(type);
        switch (type) {
            case TYPE_JSON_DEFLATE:
                encodeDeflatedJson(parameters, outputStream);
                break;
            case TYPE_VOBJECT:
                encodeVObjectParameters(parameters, outputStream);
                break;
            default:
                throw new IOException(new UnsupportedEncodingException(String.valueOf(type)));
        }
    }

    private static void encodeDeflatedJson(ExtendedProperties extendedProperties, ByteArrayOutputStream outputStream) throws IOException {
        try {
            encodeDeflatedJson(encodeJsonProperties(extendedProperties), outputStream);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private static void encodeDeflatedJson(List<ExtendedPropertyParameter> parameters, ByteArrayOutputStream outputStream) throws IOException {
        try {
            encodeDeflatedJson(encodeJsonParameters(parameters), outputStream);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private static void encodeDeflatedJson(JSONValue json, ByteArrayOutputStream outputStream) throws IOException {
        try (DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream)) {
            byte[] jsonBytes = json.toString().getBytes(Charsets.US_ASCII);
            deflaterOutputStream.write(jsonBytes);
            deflaterOutputStream.finish();
        }
    }

    private static JSONArray encodeJsonProperties(ExtendedProperties extendedProperties) throws JSONException {
        if (null == extendedProperties || extendedProperties.isEmpty()) {
            return new JSONArray();
        }
        JSONArray jsonExtendedProperties = new JSONArray(extendedProperties.size());
        for (int i = 0; i < extendedProperties.size(); i++) {
            jsonExtendedProperties.add(i, encodeJsonProperty(extendedProperties.get(i)));
        }
        return jsonExtendedProperties;
    }

    private static void encodeVObjectProperties(ExtendedProperties extendedProperties, ByteArrayOutputStream outputStream) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, Charsets.UTF_8);
            VObjectWriter vObjectWriter = new VObjectWriter(writer, SyntaxStyle.NEW)) {
            for (ExtendedProperty extendedProperty : extendedProperties) {
                vObjectWriter.writeProperty(encodeVObjectProperty(extendedProperty));
            }
            vObjectWriter.flush();
        }
    }

    private static void encodeVObjectParameters(List<ExtendedPropertyParameter> parameters, ByteArrayOutputStream outputStream) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, Charsets.UTF_8); VObjectWriter vObjectWriter = new VObjectWriter(writer, SyntaxStyle.NEW)) {
            ExtendedProperty extendedProperty = new ExtendedProperty("X", null, parameters);
            vObjectWriter.writeProperty(encodeVObjectProperty(extendedProperty));
            vObjectWriter.flush();
        }
    }

    private static VObjectProperty encodeVObjectProperty(ExtendedProperty extendedProperty) throws IOException {
        Object value = extendedProperty.getValue();
        if (null != value && false == String.class.isInstance(value)) {
            throw new IOException("Can't encode " + value.getClass());
        }
        VObjectProperty vObjectProperty = new VObjectProperty(extendedProperty.getName(), (String) extendedProperty.getValue());
        List<ExtendedPropertyParameter> parameters = extendedProperty.getParameters();
        if (null != parameters) {
            vObjectProperty.setParameters(encodeVObjectParameters(parameters));
        }
        return vObjectProperty;
    }

    private static VObjectParameters encodeVObjectParameters(List<ExtendedPropertyParameter> parameters) {
        VObjectParameters vObjectParameters = new VObjectParameters();
        for (ExtendedPropertyParameter parameter : parameters) {
            vObjectParameters.put(parameter.getName(), parameter.getValue());
        }
        return vObjectParameters;
    }

    private static JSONObject encodeJsonProperty(ExtendedProperty extendedProperty) throws JSONException {
        JSONObject jsonExtendedProperty = new JSONObject();
        jsonExtendedProperty.put("name", extendedProperty.getName());
        jsonExtendedProperty.put("value", extendedProperty.getValue());
        List<ExtendedPropertyParameter> parameters = extendedProperty.getParameters();
        if (null == parameters || parameters.isEmpty()) {
            return jsonExtendedProperty;
        }
        jsonExtendedProperty.put("parameters", encodeJsonParameters(parameters));
        return jsonExtendedProperty;
    }

    private static JSONArray encodeJsonParameters(List<ExtendedPropertyParameter> parameters) throws JSONException {
        JSONArray jsonParameters = new JSONArray(parameters.size());
        for (int i = 0; i < parameters.size(); i++) {
            ExtendedPropertyParameter parameter = parameters.get(i);
            jsonParameters.add(i, new JSONObject().putOpt("name", parameter.getName()).putOpt("value", parameter.getValue()));
        }
        return jsonParameters;
    }

    private static ExtendedProperties decodeDeflatedJson(InputStream inputStream) throws IOException {
        try (InflaterInputStream inflaterStream = new InflaterInputStream(inputStream);
            AsciiReader reader = new AsciiReader(inflaterStream)) {
            return decodeJsonProperties(new JSONArray(reader));
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private static List<ExtendedPropertyParameter> decodeDeflatedJsonParameters(InputStream inputStream) throws IOException {
        try (InflaterInputStream inflaterStream = new InflaterInputStream(inputStream);
            AsciiReader reader = new AsciiReader(inflaterStream)) {
            return decodeJsonParameters(new JSONArray(reader));
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private static ExtendedProperties decodeJsonProperties(JSONArray jsonExtendedProperties) throws JSONException {
        if (null == jsonExtendedProperties || jsonExtendedProperties.isEmpty()) {
            return null;
        }
        List<ExtendedProperty> extendedProperties = new ArrayList<ExtendedProperty>(jsonExtendedProperties.length());
        for (int i = 0; i < jsonExtendedProperties.length(); i++) {
            extendedProperties.add(decodeJsonProperty(jsonExtendedProperties.getJSONObject(i)));
        }
        return extendedProperties.isEmpty() ? null : new ExtendedProperties(extendedProperties);
    }

    private static ExtendedProperty decodeJsonProperty(JSONObject jsonExtendedProperty) throws JSONException {
        String name = jsonExtendedProperty.optString("name", null);
        Object value = jsonExtendedProperty.opt("value");
        JSONArray jsonParameters = jsonExtendedProperty.optJSONArray("parameters");
        if (null == jsonParameters || jsonParameters.isEmpty()) {
            return new ExtendedProperty(name, value);
        }
        return new ExtendedProperty(name, value, decodeJsonParameters(jsonParameters));
    }

    private static List<ExtendedPropertyParameter> decodeJsonParameters(JSONArray jsonParameters) throws JSONException {
        List<ExtendedPropertyParameter> parameters = new ArrayList<ExtendedPropertyParameter>(jsonParameters.length());
        for (int i = 0; i < jsonParameters.length(); i++) {
            JSONObject jsonParameter = jsonParameters.getJSONObject(i);
            parameters.add(new ExtendedPropertyParameter(jsonParameter.optString("name", null), jsonParameter.optString("value", null)));
        }
        return parameters;
    }

    private static ExtendedProperties decodeVObjectProperties(InputStream inputStream) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(inputStream, Charsets.UTF_8);
            VObjectReader vObjectReader = new VObjectReader(reader, SyntaxRules.iCalendar())) {
            return decodeVObjectProperties(vObjectReader);
        }
    }

    private static List<ExtendedPropertyParameter> decodeVObjectParameters(InputStream inputStream) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(inputStream, Charsets.UTF_8);
            VObjectReader vObjectReader = new VObjectReader(reader, SyntaxRules.iCalendar())) {
            ExtendedProperties properties = decodeVObjectProperties(vObjectReader);
            return null != properties && 0 < properties.size() ? properties.get(0).getParameters() : null;
        }
    }

    private static ExtendedProperties decodeVObjectProperties(VObjectReader vObjectReader) throws IOException {
        if (null == vObjectReader) {
            return null;
        }
        final List<ExtendedProperty> extendedProperties = new ArrayList<ExtendedProperty>();
        vObjectReader.parse(new VObjectDataAdapter() {

            @Override
            public void onProperty(VObjectProperty property, Context context) {
                extendedProperties.add(decodeVObjectProperty(property));
            }
        });
        return extendedProperties.isEmpty() ? null : new ExtendedProperties(extendedProperties);
    }

    static ExtendedProperty decodeVObjectProperty(VObjectProperty vObjectProperty) {
        String name = vObjectProperty.getName();
        String value = vObjectProperty.getValue();
        VObjectParameters vObjectParameters = vObjectProperty.getParameters();
        if (null == vObjectParameters) {
            return new ExtendedProperty(name, value);
        }
        return new ExtendedProperty(name, value, decodeVObjectParameters(vObjectParameters));
    }

    private static List<ExtendedPropertyParameter> decodeVObjectParameters(VObjectParameters vObjectParameters) {
        List<ExtendedPropertyParameter> parameters = new ArrayList<ExtendedPropertyParameter>();
        for (Entry<String, List<String>> entry : vObjectParameters) {
            for (String parameterValue : entry.getValue()) {
                parameters.add(new ExtendedPropertyParameter(entry.getKey(), parameterValue));
            }
        }
        return parameters;
    }

}
