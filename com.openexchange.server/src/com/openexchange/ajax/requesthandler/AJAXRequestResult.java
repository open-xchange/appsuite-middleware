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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONValue;
import com.openexchange.exception.OXException;

/**
 * {@link AJAXRequestResult} - Simple container for a {@link JSONValue result}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AJAXRequestResult {

    private static final String JSON = "json".intern();

    public static final long SECOND_IN_MILLIS = 1000;

    public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;

    public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;

    public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;

    public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;

    /**
     * This constant is actually the length of 364 days, not of a year!
     */
    public static final long YEAR_IN_MILLIS = WEEK_IN_MILLIS * 52;

    /**
     * The constant representing an empty, unmodifiable AJAX request result.
     * <p>
     * Both data and time stamp are set to <code>null</code>.
     */
    public static final AJAXRequestResult EMPTY_REQUEST_RESULT = new AJAXRequestResult() {

        @Override
        public void setResultObject(final Object resultObject) {
            throw new UnsupportedOperationException("Method not allowed for empty AJAX request result.");
        }

        @Override
        public void setResultObject(final Object object, final String format) {
            throw new UnsupportedOperationException("Method not allowed for empty AJAX request result.");
        }

        @Override
        public void setFormat(final String format) {
            throw new UnsupportedOperationException("Method not allowed for empty AJAX request result.");
        }

        @Override
        public void setTimestamp(final Date timestamp) {
            throw new UnsupportedOperationException("Method not allowed for empty AJAX request result.");
        }

        @Override
        public void setHeader(final String header, final String value) {
            throw new UnsupportedOperationException("Method not allowed for empty AJAX request result.");
        }

        @Override
        public void setDeferred(final boolean deferred) {
            throw new UnsupportedOperationException("Method not allowed for empty AJAX request result.");
        }

        @Override
        public void setExpires(final long expires) {
            throw new UnsupportedOperationException("Method not allowed for empty AJAX request result.");
        }

        @Override
        public AJAXRequestResult setType(final ResultType resultType) {
            throw new UnsupportedOperationException("Method not allowed for empty AJAX request result.");
        }

        @Override
        public AJAXRequestResult addWarnings(final java.util.Collection<OXException> warnings) {
            throw new UnsupportedOperationException("Method not allowed for empty AJAX request result.");
        }

        @Override
        public void setParameter(final String name, final Object value) {
            throw new UnsupportedOperationException("Method not allowed for empty AJAX request result.");
        };

        @Override
        public void setResponseProperty(final String name, final Object value) {
            throw new UnsupportedOperationException("Method not allowed for empty AJAX request result.");
        };

        @Override
        public void removeHeader(final String header) {
            throw new UnsupportedOperationException("Method not allowed for empty AJAX request result.");
        };

        @Override
        public void removeParameter(final String name) {
            throw new UnsupportedOperationException("Method not allowed for empty AJAX request result.");
        };

        @Override
        public void removeResponseProperty(final String name) {
            throw new UnsupportedOperationException("Method not allowed for empty AJAX request result.");
        };

    };

    /**
     * The request result type.
     */
    public static enum ResultType {
        /**
         * A common request result which should be further processed.
         */
        COMMON,
        /**
         * An <i>ETag</i> request result.
         */
        ETAG,
        /**
         * The special result directly responded to client.
         */
        DIRECT;
    }

    /**
     * The special response object signaling a direct response has been performed.
     */
    public static final Object DIRECT_OBJECT = new Object();

    private ResultType resultType;

    private Object resultObject;

    private Date timestamp;

    private Collection<OXException> warnings;

    private boolean deferred;

    private final Map<String, String> headers;

    private final Map<String, Object> parameters;

    private final Map<String, Object> responseProperties;

    private String format;

    private long expires;

    private OXException exception;

    private long duration;

    /**
     * Initializes a new {@link AJAXRequestResult} with data and time stamp set to <code>null</code>.
     *
     * @see #EMPTY_REQUEST_RESULT
     */
    public AJAXRequestResult() {
        this(null, null, null);
    }

    /**
     * Initializes a new {@link AJAXRequestResult} with time stamp set to <code>null</code>.
     *
     * @param resultObject The result object
     */
    public AJAXRequestResult(final Object resultObject) {
        this(resultObject, null, null);
    }

    /**
     * Initializes a new {@link AJAXRequestResult}.
     *
     * @param resultObject The result object
     * @param timestamp The server's last-modified time stamp (corresponding to either a GET, ALL, or LIST request)
     */
    public AJAXRequestResult(final Object resultObject, final Date timestamp) {
        this(resultObject, timestamp, null);
    }

    /**
     * Initializes a new {@link AJAXRequestResult} with time stamp set to <code>null</code>.
     *
     * @param resultObject The result object
     * @param format The format of the result object
     */
    public AJAXRequestResult(final Object resultObject, final String format) {
        this(resultObject, null, format);
    }

    /**
     * Initializes a new {@link AJAXRequestResult}.
     *
     * @param resultObject The result object
     * @param timestamp The server's last-modified time stamp (corresponding to either a GET, ALL, or LIST request)
     * @param format The format of the result object
     */
    public AJAXRequestResult(final Object resultObject, final Date timestamp, final String format) {
        super();
        duration = -1L;
        headers = new LinkedHashMap<String, String>(8);
        parameters = new HashMap<String, Object>(8);
        responseProperties = new HashMap<String, Object>(4);
        this.timestamp = null == timestamp ? null : new Date(timestamp.getTime());
        this.format = null == format ? JSON : format;
        if ("direct".equals(format)) {
            resultType = ResultType.DIRECT;
            this.resultObject = DIRECT_OBJECT;
        } else {
            resultType = ResultType.COMMON;
            this.resultObject = resultObject;
        }
        expires = -1;
    }

    /**
     * Gets the duration.
     *
     * @return The duration or <code>-1</code> if not set
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Sets the duration
     *
     * @param duration The duration to set
     * @return This AJAX request result with duration applied
     */
    public AJAXRequestResult setDuration(final long duration) {
        this.duration = duration < 0 ? -1L : duration;
        return this;
    }

    /**
     * Sets the duration by given processing start time stamp.
     *
     * <pre>
     * System.currentTimeMillis() - start;
     * </pre>
     *
     * @param start The start time stamp
     * @return This AJAX request result with duration applied
     */
    public AJAXRequestResult setDurationByStart(final long start) {
        return setDuration(System.currentTimeMillis() - start);
    }

    /**
     * Adds given duration.
     *
     * @param duration The duration to add
     * @return This AJAX request result with duration applied
     */
    public AJAXRequestResult addDuration(final long duration) {
        if (this.duration < 0) {
            return setDuration(duration);
        }
        this.duration += duration;
        return this;
    }

    /**
     * Adds given duration by given start time stamp.
     *
     * <pre>
     * System.currentTimeMillis() - start;
     * </pre>
     *
     * @param start The start time stamp
     * @return This AJAX request result with duration applied
     */
    public AJAXRequestResult addDurationByStart(final long start) {
        return addDuration(System.currentTimeMillis() - start);
    }

    /**
     * Gets the result type
     *
     * @return The result type
     */
    public ResultType getType() {
        return resultType;
    }

    /**
     * Sets the result type
     *
     * @param resultType The result type to set
     * @return This result with type applied
     */
    public AJAXRequestResult setType(final ResultType resultType) {
        this.resultType = resultType;
        return this;
    }

    /**
     * Gets the expiry time.
     * <p>
     * Have a notion of a time-to-live value.
     *
     * @return The expiry time or <code>-1</code> for no expiry
     */
    public long getExpires() {
        return expires;
    }

    /**
     * Sets the expires time
     *
     * @param expires The expires time or <code>-1</code> for no expiry
     */
    public void setExpires(final long expires) {
        this.expires = expires;
    }

    /**
     * Gets the deferred flag
     *
     * @return The deferred flag
     */
    public boolean isDeferred() {
        return deferred;
    }

    /**
     * Sets the deferred flag
     *
     * @param deferred The deferred flag to set
     */
    public void setDeferred(final boolean deferred) {
        this.deferred = deferred;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((resultObject == null) ? 0 : resultObject.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AJAXRequestResult)) {
            return false;
        }
        final AJAXRequestResult other = (AJAXRequestResult) obj;
        if (resultObject == null) {
            if (other.resultObject != null) {
                return false;
            }
        } else if (!resultObject.equals(other.resultObject)) {
            return false;
        }
        if (timestamp == null) {
            if (other.timestamp != null) {
                return false;
            }
        } else if (!timestamp.equals(other.timestamp)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the result object.
     *
     * @return The result object
     */
    public Object getResultObject() {
        return resultObject;
    }

    /**
     * Sets the result object
     *
     * @param resultObject The result object to set
     */
    public void setResultObject(final Object resultObject) {
        this.resultObject = resultObject;
    }

    /**
     * Gets the result's format.
     *
     * @return The format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets this result's format.
     *
     * @param format The format
     */
    public void setFormat(final String format) {
        this.format = format;
    }

    /**
     * Gets the time stamp.
     *
     * @return The time stamp
     */
    public Date getTimestamp() {
        return null == timestamp ? null : new Date(timestamp.getTime());
    }

    /**
     * Sets the time stamp.
     *
     * @param timestamp The time stamp.
     */
    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the warnings.
     *
     * @return The warnings
     */
    public Collection<OXException> getWarnings() {
        return null == warnings ? Collections.<OXException> emptySet() : Collections.unmodifiableCollection(warnings);
    }

    /**
     * Sets the warnings.
     *
     * @param warnings The warnings to set
     * @return This request result with specified warnings added
     */
    public AJAXRequestResult addWarnings(final Collection<OXException> warnings) {
        if (null == warnings || warnings.isEmpty()) {
            return this;
        }
        if (null == this.warnings) {
            this.warnings = new HashSet<OXException>(warnings);
        } else {
            this.warnings.addAll(warnings);
        }
        return this;
    }

    /**
     * Sets a header value
     */
    public void setHeader(final String header, final String value) {
        if (null == value) {
            headers.remove(header);
        } else {
            headers.put(header, value);
        }
    }

    /**
     * Removes a header value
     */
    public void removeHeader(final String header) {
        headers.remove(header);
    }

    /**
     * Gets a header value
     */
    public String getHeader(final String header) {
        return headers.get(header);
    }

    /**
     * Gets the headers
     *
     * @return The headers
     */
    public Map<String, String> getHeaders() {
        return new HashMap<String, String>(headers);
    }

    /**
     * Sets a parameter.
     *
     * @param name The parameter name
     * @param value The value; if <code>null</code> a remove is performed
     */
    public void setParameter(final String name, final Object value) {
        if (null == value) {
            parameters.remove(name);
        } else {
            parameters.put(name, value);
        }
    }

    /**
     * Removes a parameter.
     *
     * @param name The parameter name
     */
    public void removeParameter(final String name) {
        parameters.remove(name);
    }

    /**
     * Gets the associated parameter value.
     *
     * @param name The parameter name
     * @return The associated value or <code>null</code> if there is no such parameter
     */
    public Object getParameter(final String name) {
        return parameters.get(name);
    }

    /**
     * Gets the parameters
     *
     * @return The parameters
     */
    public Map<String, Object> getParameters() {
        return new HashMap<String, Object>(parameters);
    }

    /**
     * Sets a response property.
     *
     * @param name The property name
     * @param value The value; if <code>null</code> a remove is performed
     */
    public void setResponseProperty(final String name, final Object value) {
        if (null == value) {
            responseProperties.remove(name);
        } else {
            responseProperties.put(name, value);
        }
    }

    /**
     * Removes a response property.
     *
     * @param name The property name
     */
    public void removeResponseProperty(final String name) {
        responseProperties.remove(name);
    }

    /**
     * Gets the associated response property.
     *
     * @param name The parameter name
     * @return The associated response property or <code>null</code> if there is no such parameter
     */
    public Object getResponseProperty(final String name) {
        return responseProperties.get(name);
    }

    /**
     * Gets the response properties.
     *
     * @return The response properties
     */
    public Map<String, Object> getResponseProperties() {
        return responseProperties;
    }

    @Override
    public String toString() {
        return new com.openexchange.java.StringAllocator(34).append(super.toString()).append(" resultObject=").append(resultObject).append(", timestamp=").append(
            timestamp).append(" warnings=").append(null == warnings ? "<none>" : warnings.toString()).toString();
    }

    /**
     * Sets the result object and its format.
     *
     * @param object The result format
     * @param format The format
     */
    public void setResultObject(final Object object, final String format) {
        setResultObject(object);
        setFormat(format);
    }

    public void setException(final OXException exception) {
        this.exception = exception;
    }

    public OXException getException() {
        return exception;
    }

}
