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

package com.openexchange.http.requestwatcher.osgi.services;

import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    private final HttpServletResponse response;
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
    public RequestRegistryEntry(long number, HttpServletRequest request, HttpServletResponse response, Thread thread,  Map<String, String> propertyMap) {
        super();
        this.number = number;
        this.thread = thread;
        this.propertyMap = null == propertyMap ? Collections.<String, String> emptyMap() : propertyMap;;
        this.request = request;
        this.response = response;
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
        @SuppressWarnings("unchecked") final Map<String, String[]> parameterMap = request.getParameterMap();
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
        if (null == obj) {
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
