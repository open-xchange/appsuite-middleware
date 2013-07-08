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

package com.openexchange.ajp13.servlet.http;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;
import com.openexchange.ajp13.Services;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.HashKeyMap;

/**
 * {@link HttpSessionWrapper} - A wrapper class for {@link HttpSession}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HttpSessionWrapper implements HttpSession {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(HttpSessionWrapper.class));

    private static volatile int cookieTTL = -1;

    /**
     * Gets the cookieTTL
     *
     * @return The cookieTTL
     */
    public static int getCookieTTL() {
        int tmp = cookieTTL;
        if (tmp < 0) {
            synchronized (HttpSessionWrapper.class) {
                tmp = cookieTTL;
                if (tmp < 0) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        tmp = ConfigTools.parseTimespanSecs("1W");
                    } else {
                        tmp = ConfigTools.parseTimespanSecs(service.getProperty("com.openexchange.cookie.ttl", "1W"));
                    }
                    cookieTTL = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Resets the cookieTTL
     */
    public static void resetCookieTTL() {
        cookieTTL = -1;
    }

    /*-
     * -------------------- Member stuff ---------------------
     */

    private final Map<String, Object> attributes;

    private final Map<String, Object> values;

    private final long creationTime;

    private long lastAccessedTime;

    private final String id;

    private int maxInactiveIntervall;

    private ServletContext servletContext;

    private HttpSessionContext sessionContext;

    /**
     * Indicates if the client does not yet know about the session or if the client chooses not to join the session
     */
    private boolean newSession;

    /**
     * Initializes a new {@link HttpSessionWrapper}.
     *
     * @param id The HTTP session identifier
     */
    public HttpSessionWrapper(final String id) {
        super();
        newSession = true;
        /*
         * Max. inactive interval
         */
        final ConfigurationService service = Services.getService(ConfigurationService.class);
        maxInactiveIntervall = null == service ? 1800 : service.getIntProperty("com.openexchange.servlet.maxInactiveIntervall", 1800); // 30 Minutes
        /*
         * Initialize other stuff
         */
        attributes = new HashKeyMap<Object>();
        values = new HashKeyMap<Object>();
        creationTime = lastAccessedTime = System.currentTimeMillis();
        this.id = id;
    }

    /**
     * Touches this session's last-accessed time stamp.
     *
     * @return This session with last-accessed time stamp updated
     */
    public HttpSessionWrapper touch() {
        lastAccessedTime = System.currentTimeMillis();
        return this;
    }

    @Override
    public Object getAttribute(final String attributeName) {
        return attributes.get(attributeName);
    }

    @Override
    public Enumeration<?> getAttributeNames() {
        return new IteratorEnumeration(attributes.keySet().iterator());
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveIntervall;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(final ServletContext context) {
        servletContext = context;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return sessionContext;
    }

    @Override
    public Object getValue(final String valueName) {
        return values.get(valueName);
    }

    @Override
    public String[] getValueNames() {
        return values.keySet().toArray(new String[values.size()]);
    }

    @Override
    public void invalidate() {
        /*
         * Remove attributes
         */
        final List<String> toRemove = new ArrayList<String>(attributes.keySet());
        for (final String attributeName : toRemove) {
            removeAttribute(attributeName);
        }
        /*
         * Remove values
         */
        toRemove.clear();
        toRemove.addAll(values.keySet());
        for (final String valueName : toRemove) {
            removeValue(valueName);
        }
        servletContext = null;
        sessionContext = null;
        /*
         * Remove from management
         */
        HttpSessionManagement.removeHttpSession(id);
    }

    @Override
    public boolean isNew() {
        return newSession;
    }

    public void setNew(final boolean newSession) {
        this.newSession = newSession;
    }

    @Override
    public void putValue(final String valueName, final Object value) {
        values.put(valueName, value);
        if (value instanceof HttpSessionBindingListener) {
            final HttpSessionBindingListener listener = (HttpSessionBindingListener) value;
            listener.valueBound(new HttpSessionBindingEvent(this, valueName));
        }
    }

    @Override
    public void removeAttribute(final String attributeName) {
        final Object removedObj = attributes.remove(attributeName);
        if (removedObj instanceof HttpSessionBindingListener) {
            final HttpSessionBindingListener listener = (HttpSessionBindingListener) removedObj;
            listener.valueUnbound(new HttpSessionBindingEvent(this, attributeName));
        }
    }

    @Override
    public void removeValue(final String valueName) {
        final Object removedObj = values.remove(valueName);
        if (removedObj instanceof HttpSessionBindingListener) {
            final HttpSessionBindingListener listener = (HttpSessionBindingListener) removedObj;
            listener.valueUnbound(new HttpSessionBindingEvent(this, valueName));
        }
    }

    @Override
    public void setAttribute(final String attributeName, final Object attributeValue) {
        if (attributeValue != null) {
            attributes.put(attributeName, attributeValue);
        }
        if (attributeValue instanceof HttpSessionBindingListener) {
            final HttpSessionBindingListener listener = (HttpSessionBindingListener) attributeValue;
            listener.valueBound(new HttpSessionBindingEvent(this, attributeName));
        }
    }

    @Override
    public void setMaxInactiveInterval(final int maxInactiveIntervall) {
        final int cookieTTL = getCookieTTL();
        if (maxInactiveIntervall < 0 || maxInactiveIntervall > cookieTTL) {
            LOG.warn("Specified maxInactiveIntervall is negative or exceeds max. cookie time-to-live. Using max. cookie time-to-live: " + cookieTTL + "seconds");
            this.maxInactiveIntervall = cookieTTL;
        } else {
            this.maxInactiveIntervall = maxInactiveIntervall;
        }
    }

    private static class IteratorEnumeration implements Enumeration<Object> {

        private final Iterator<?> iter;

        public IteratorEnumeration(final Iterator<?> iter) {
            this.iter = iter;
        }

        @Override
        public boolean hasMoreElements() {
            return iter.hasNext();
        }

        @Override
        public Object nextElement() {
            return iter.next();
        }
    }

}
