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

package com.openexchange.ajax.framework;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface AJAXRequest<T extends AbstractAJAXResponse> {

    static final Header[] NO_HEADER = new Header[0];

    enum Method {
        GET,
        POST,
        UPLOAD,
        PUT,
        DELETE
    }

    Method getMethod();

    String getServletPath();

    public class Parameter {

        private final String name;
        private final String value;

        public Parameter(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        public Parameter(final String name, final String[] values) {
            this(name, convert(values));
        }

        public Parameter(final String name, final int[] values) {
            this(name, convert(values));
        }

        public Parameter(final String name, final int identifier) {
            this(name, String.valueOf(identifier));
        }

        public Parameter(final String name, final long time) {
            this(name, String.valueOf(time));
        }

        public Parameter(final String name, final Date time) {
            this(name, time.getTime());
        }

        public Parameter(final String name, final Date time, final TimeZone tz) {
            this(name, time.getTime() + tz.getOffset(time.getTime()));
        }

        public Parameter(final String name, final boolean schalter) {
            this(name, String.valueOf(schalter));
        }

        public static String convert(final int[] values) {
            final StringBuilder columnSB = new StringBuilder();
            for (final int i : values) {
                columnSB.append(i);
                columnSB.append(',');
            }
            columnSB.delete(columnSB.length() - 1, columnSB.length());
            return columnSB.toString();
        }

        public static String convert(final String[] values) {
            final StringBuilder columnSB = new StringBuilder();
            for (final String i : values) {
                columnSB.append(i);
                columnSB.append(',');
            }
            columnSB.delete(columnSB.length() - 1, columnSB.length());
            return columnSB.toString();
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }

    class URLParameter extends Parameter {

        public URLParameter(final String name, final String value) {
            super(name, value);
        }

        public URLParameter(final String name, final int value) {
            this(name, Integer.toString(value));
        }

        public URLParameter(String name, int[] values) {
            this(name, convert(values));
        }

        public URLParameter(String name, boolean value) {
            this(name, Boolean.toString(value));
        }
    }

    class FileParameter extends Parameter {

        private final InputStream inputStream;
        private final String mimeType;

        public FileParameter(final String name, final String fileName, final InputStream inputStream, final String mimeType) {
            super(name, fileName);
            this.inputStream = inputStream;
            this.mimeType = mimeType;
        }

        /**
         * @return the fileName
         */
        public String getFileName() {
            return super.getValue();
        }

        /**
         * @return the inputStream
         */
        public InputStream getInputStream() {
            return inputStream;
        }

        /**
         * @return the mimeType
         */
        public String getMimeType() {
            return mimeType;
        }

        @Override
        public String toString() {
            return super.toString() + ',' + mimeType;
        }
    }

    class FieldParameter extends Parameter {

        /**
         * Initializes a new {@link FieldParameter}
         * 
         * @param fieldName
         * @param fieldContent
         */
        public FieldParameter(final String fieldName, final String fieldContent) {
            super(fieldName, fieldContent);
        }

        /**
         * Gets the fieldName
         *
         * @return the fieldName
         */
        public String getFieldName() {
            return super.getName();
        }

        /**
         * Gets the fieldContent
         *
         * @return the fieldContent
         */
        public String getFieldContent() {
            return super.getValue();
        }
    }

    /**
     * This method has to return all parameters that are necessary for the
     * request. The session request parameter is added by the {@link Executor}.
     * 
     * @return all request parameters except the session identifier.
     */
    Parameter[] getParameters() throws IOException, JSONException;

    AbstractAJAXParser<? extends T> getParser();

    Object getBody() throws IOException, JSONException;

    Header[] getHeaders();

}
