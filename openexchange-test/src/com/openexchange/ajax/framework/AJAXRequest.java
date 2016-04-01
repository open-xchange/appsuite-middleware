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
        public FileParameter(final String name, final String fileName,
            final InputStream inputStream, final String mimeType) {
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
     * @return all request parameters except the session identifier.
     */
    Parameter[] getParameters() throws IOException, JSONException;

    AbstractAJAXParser<? extends T> getParser();

    Object getBody() throws IOException, JSONException;

    Header[] getHeaders();

}
