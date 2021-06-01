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

package com.openexchange.http.requestwatcher.osgi.services;

import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * {@link HttpServletRequestRegistryEntry} keeps track of the incoming Request and its associated thread. The Date of instantiation is saved
 * to be able to calculate the age of the entry
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RequestRegistryEntry implements Comparable<RequestRegistryEntry> {

    private final long number;
    private final Thread thread;
    private final Map<String, String> propertyMap;
    private final HttpServletRequest request;
    private final long birthTime;
    private final int hash;

    /**
     * Initializes a new {@link RequestRegistryEntry}.
     *
     * @param number The entry's unique number
     * @param request the incoming request to register
     * @param thread the thread associated with the request
     * @param propertyMap
     */
    public RequestRegistryEntry(long number, HttpServletRequest request, Thread thread, Map<String, String> propertyMap) {
        super();
        this.number = number;
        this.thread = thread;
        this.propertyMap = null == propertyMap ? Collections.<String, String> emptyMap() : propertyMap;
        this.request = request;
        this.birthTime = System.currentTimeMillis();

        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (number ^ (number >>> 32));
        hash = result;
    }

    /**
     * Get the age of this entry.
     *
     * @return the age of the entry in milliseconds
     */
    public long getAge() {
        return System.currentTimeMillis() - birthTime;
    }

    /**
     * Get the request url.
     *
     * @return the request url as String
     */
    public String getRequestUrl() {
        return request.getRequestURL().toString();
    }

    /**
     * Get the request parameters in the form of name=value&name=value.
     *
     * @return the request parameters as String in the form of name=value&name=value
     */
    public String getRequestParameters() {
        final StringBuilder sa = new StringBuilder();
        final Map<String, String[]> parameterMap = request.getParameterMap();
        final String[] parameterNames = parameterMap.keySet().toArray(new String[0]);
        for (int i = 0; i < parameterNames.length; i++) {
            final String[] parameterValues = parameterMap.get(parameterNames[i]);
            for (int j = 0; j < parameterValues.length; j++) {
                sa.append(parameterNames[i]).append("=").append(parameterValues[j]);
                if (j != parameterValues.length - 1) {
                    sa.append("&");
                }
            }
            if (i != parameterNames.length - 1) {
                sa.append("&");
            }
        }
        return sa.toString();
    }

    /**
     * Get the StackTrace of the Thread processing this Request.
     *
     * @see java.lang.Thread#getStackTrace()
     */
    public StackTraceElement[] getStackTrace() {
        return thread.getStackTrace();
    }

    /**
     * Return thread infos in the form of "id=threadId, name=threadName"
     *
     * @return thread infos in the form of "id=threadId, name=threadName"
     */
    public String getThreadInfo() {
        return new StringBuilder("id=").append(thread.getId()).append(", name=").append(thread.getName()).toString();
    }

    /**
     * Gets the thread
     *
     * @return The thread
     */
    public Thread getThread() {
        return thread;
    }

    /**
     * Gets the log properties
     *
     * @return The log properties; never <code>null</code>
     */
    public Map<String, String> getPropertyMap() {
        return propertyMap;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (null == obj || !(obj instanceof RequestRegistryEntry)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        return (number == ((RequestRegistryEntry) obj).number);
    }

    @Override
    public int compareTo(final RequestRegistryEntry otherEntry) {
        final long thisNumber = this.number;
        final long otherNumber = otherEntry.number;
        return thisNumber < otherNumber ? 1 : thisNumber > otherNumber ? -1 : 0;
    }

}
