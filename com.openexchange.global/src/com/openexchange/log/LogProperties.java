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

package com.openexchange.log;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link LogProperties} - Provides thread-local log properties.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LogProperties {

    /**
     * Enumeration of log properties' names.
     */
    public static enum Name {
        /**
         * com.openexchange.ajax.requestNumber
         */
        AJAX_REQUEST_NUMBER("com.openexchange.ajax.requestNumber"),
        /**
         * com.openexchange.ajax.action
         */
        AJAX_ACTION("com.openexchange.ajax.action"),
        /**
         * com.openexchange.ajax.module
         */
        AJAX_MODULE("com.openexchange.ajax.module"),
        /**
         * threadId
         */
        THREAD_ID("__threadId"),
        /**
         * com.openexchange.session.authId
         */
        SESSION_AUTH_ID("com.openexchange.session.authId"),
        /**
         * com.openexchange.session.sessionId
         */
        SESSION_SESSION_ID("com.openexchange.session.sessionId"),
        /**
         * com.openexchange.session.userId
         */
        SESSION_USER_ID("com.openexchange.session.userId"),
        /**
         * com.openexchange.session.userName
         * <p>
         * The session's full login information; e.g. <code>"user1@foobar.org"</code>
         */
        SESSION_USER_NAME("com.openexchange.session.userName"),
        /**
         * com.openexchange.session.contextId
         */
        SESSION_CONTEXT_ID("com.openexchange.session.contextId"),
        /**
         * com.openexchange.session.clientId
         */
        SESSION_CLIENT_ID("com.openexchange.session.clientId"),
        /**
         * com.openexchange.session.session
         */
        SESSION_SESSION("com.openexchange.session.session"),
        /**
         * com.openexchange.session.loginName
         * <p>
         * The session's login name (<i>not</i> the full login string)
         */
        SESSION_LOGIN_NAME("com.openexchange.session.loginName"),
        /**
         * com.openexchange.grizzly.requestURI
         * <p>
         * The part of this request's URL from the protocol name up to the query string in the first line of the HTTP request.
         */
        GRIZZLY_REQUEST_URI("com.openexchange.grizzly.requestURI"),
        /**
         * com.openexchange.grizzly.queryString
         * <p>
         * The query string that is contained in the request URL after the path
         */
        GRIZZLY_QUERY_STRING("com.openexchange.grizzly.queryString"),
        /**
         * com.openexchange.grizzly.method
         */
        GRIZZLY_METHOD("com.openexchange.grizzly.method"),
        /**
         * com.openexchange.grizzly.servletPath
         * <p>
         * The part of this request's URL that calls the servlet. This path starts with a "/" character and includes either the servlet name
         * or a path to the servlet, but does not include any extra path information or a query string.
         */
        GRIZZLY_SERVLET_PATH("com.openexchange.grizzly.servletPath"),
        /**
         * com.openexchange.grizzly.pathInfo
         * <p>
         * The extra path information associated with the URL the client sent when it made this request. The extra path information follows
         * the servlet path but precedes the query string and will start with a "/" character.
         */
        GRIZZLY_PATH_INFO("com.openexchange.grizzly.pathInfo"),
        /**
         * com.openexchange.grizzly.serverName
         * <p>
         * The host name of the server to which the request was sent. It is the value of the part before ":" in the <code>Host</code> header
         * value, if any, or the resolved server name, or the server IP address.
         */
        GRIZZLY_SERVER_NAME("com.openexchange.grizzly.serverName"),
        /**
         * com.openexchange.grizzly.threadName
         */
        GRIZZLY_THREAD_NAME("com.openexchange.grizzly.threadName"),
        /**
         * com.openexchange.grizzly.remotePort
         * <p>
         * The Internet Protocol (IP) address of the client or last proxy that sent the request
         */
        GRIZZLY_REMOTE_PORT("com.openexchange.grizzly.remotePort"),
        /**
         * com.openexchange.grizzly.remoteAddres
         * <p>
         * The Internet Protocol (IP) address of the client or last proxy that sent the request
         */
        GRIZZLY_REMOTE_ADDRESS("com.openexchange.grizzly.remoteAddress"),
        /**
         * com.openexchange.grizzly.session
         */
        GRIZZLY_HTTP_SESSION("com.openexchange.grizzly.session"),
        /**
         * com.openexchange.grizzly.userAgent
         * <p>
         * The content of the <code>User-Agent</code> header.
         */
        GRIZZLY_USER_AGENT("com.openexchange.grizzly.userAgent"),
        /**
         * com.openexchange.request.trackingId
         */
        REQUEST_TRACKING_ID("com.openexchange.request.trackingId"),
        /**
         * javax.servlet.servletPath
         */
        SERVLET_SERVLET_PATH("javax.servlet.servletPath"),
        /**
         * javax.servlet.pathInfo
         */
        SERVLET_PATH_INFO("javax.servlet.pathInfo"),
        /**
         * javax.servlet.queryString
         */
        SERVLET_QUERY_STRING("javax.servlet.queryString"),
        /**
         * com.openexchange.file.storage.accountId
         */
        FILE_STORAGE_ACCOUNT_ID("com.openexchange.file.storage.accountId"),
        /**
         * com.openexchange.file.storage.configuration
         */
        FILE_STORAGE_CONFIGURATION("com.openexchange.file.storage.configuration"),
        /**
         * com.openexchange.file.storage.serviceId
         */
        FILE_STORAGE_SERVICE_ID("com.openexchange.file.storage.serviceId"),
        /**
         * com.openexchange.mail.host
         */
        MAIL_HOST("com.openexchange.mail.host"),
        /**
         * com.openexchange.mail.fullName
         */
        MAIL_FULL_NAME("com.openexchange.mail.fullName"),
        /**
         * com.openexchange.mail.mailId
         */
        MAIL_MAIL_ID("com.openexchange.mail.mailId"),
        /**
         * com.openexchange.mail.accountId
         */
        MAIL_ACCOUNT_ID("com.openexchange.mail.accountId"),
        /**
         * com.openexchange.mail.login
         */
        MAIL_LOGIN("com.openexchange.mail.login"),
        /**
         * com.openexchange.mail.command
         */
        MAIL_COMMAND("com.openexchange.mail.command"),
        /**
         * com.openexchange.mail.session
         */
        MAIL_SESSION("com.openexchange.mail.session"),
        /**
         * com.openexchange.database.schema
         */
        DATABASE_SCHEMA("com.openexchange.database.schema"),
        /**
         * com.openexchange.login.login
         */
        LOGIN_LOGIN("com.openexchange.login.login"),
        /**
         * com.openexchange.login.clientIp
         */
        LOGIN_CLIENT_IP("com.openexchange.login.clientIp"),
        /**
         * com.openexchange.login.userAgent
         */
        LOGIN_USER_AGENT("com.openexchange.login.userAgent"),
        /**
         * com.openexchange.login.authId
         */
        LOGIN_AUTH_ID("com.openexchange.login.authId"),
        /**
         * com.openexchange.login.client
         */
        LOGIN_CLIENT("com.openexchange.login.client"),
        /**
         * com.openexchange.login.version
         */
        LOGIN_VERSION("com.openexchange.login.version"),
        /**
         * com.openexchange.hostname
         */
        HOSTNAME("com.openexchange.hostname"),

        ;

        private final String name;

        private Name(final String name) {
            this.name = name;
        }

        /**
         * Gets the name
         *
         * @return The name
         */
        public String getName() {
            return name;
        }

        private static final Map<String, Name> STRING2NAME;
        static {
            final Name[] values = Name.values();
            final Map<String, Name> m = new HashMap<String, Name>(values.length);
            for (final Name name : values) {
                m.put(name.getName(), name);
            }
            STRING2NAME = m;
        }

        /**
         * Gets the associated {@code Name} enum.
         *
         * @param sName The name string
         * @return The {@code Name} enum or <code>null</code>
         */
        public static Name nameFor(final String sName) {
            return null == sName ? null : STRING2NAME.get(sName);
        }
    }

    /**
     * Initializes a new {@link LogProperties}.
     */
    private LogProperties() {
        super();
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private static final ThreadLocal<Map<String, Object>> TMP_FILES = new ThreadLocal<Map<String, Object>>();

    private static final Object PRESENT = new Object();

    /**
     * Gets the temporary file property.
     *
     * @return The removed temporary file property or <code>null</code>
     */
    public static String[] getTempFiles() {
        Map<String, Object> map = TMP_FILES.get();
        if (map == null) {
            return null;
        }
        int size = map.size();
        if (size <= 0) {
            return null;
        }
        return map.keySet().toArray(new String[size]);
    }

    /**
     * Gets (and removes) the temporary file property.
     *
     * @return The removed temporary file property or <code>null</code>
     */
    public static String[] getAndRemoveTempFiles() {
        Map<String, Object> map = TMP_FILES.get();
        if (map == null) {
            return null;
        }
        int size = map.size();
        if (size <= 0) {
            return null;
        }
        String[] pathNames = map.keySet().toArray(new String[size]);
        map.clear();
        return pathNames;
    }

    /**
     * Adds given temporary file.
     *
     * @param file The temporary file
     */
    public static void addTempFile(File file) {
        if (null != file) {
            addTempFile(file.getPath());
        }
    }

    /**
     * Adds given denoted temporary file.
     *
     * @param pathName The denoted temporary file
     */
    public static void addTempFile(String pathName) {
        if (Strings.isEmpty(pathName)) {
            return;
        }
        Map<String, Object> oldMap = TMP_FILES.get();
        if (oldMap == null) {
            Map<String, Object> newMap = new HashMap<String, Object>(4, 0.9F);
            TMP_FILES.set(newMap);
            newMap.put(pathName, PRESENT);
        } else {
            oldMap.put(pathName, PRESENT);
        }
    }

    /**
     * Removes given temporary file.
     *
     * @param file The temporary file
     */
    public static void removeTempFile(File file) {
        if (null != file) {
            removeTempFile(file.getPath());
        }
    }

    /**
     * Removes given denoted temporary file.
     *
     * @param pathName The denoted temporary file
     */
    public static void removeTempFile(String pathName) {
        if (Strings.isEmpty(pathName)) {
            return;
        }
        Map<String, Object> map = TMP_FILES.get();
        if (map != null) {
            map.remove(pathName);
        }
    }

    /**
     * Removes all temporary files.
     */
    public static void removeAllTempFiles() {
        Map<String, Object> map = TMP_FILES.get();
        if (map != null) {
            map.clear();
        }
    }

    /**
     * Drops temporary files association.
     */
    public static void dropTempFiles() {
        TMP_FILES.remove();
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private static final ConcurrentMap<Class<? extends MDCAdapter>, Field> FIELD_CACHE = new ConcurrentHashMap<Class<? extends MDCAdapter>, Field>(4, 0.9f, 1);

    @SuppressWarnings("unchecked")
    private static InheritableThreadLocal<Map<String, String>> getPropertiesMap(final MDCAdapter mdcAdapter) throws OXException {
        try {
            final Class<? extends MDCAdapter> clazz = mdcAdapter.getClass();
            Field field = FIELD_CACHE.get(clazz);
            if (null == field) {
                for (final Field f : clazz.getDeclaredFields()) {
                    final Class<?> fieldClazz = f.getType();
                    if (InheritableThreadLocal.class.isAssignableFrom(fieldClazz)) {
                        field = f;
                        break;
                    }
                }
                if (null == field) {
                    throw new NoSuchFieldException("InheritableThreadLocal");
                }
                field.setAccessible(true);
                FIELD_CACHE.put(clazz, field);
            }
            return (InheritableThreadLocal<Map<String, String>>) field.get(mdcAdapter);
        } catch (SecurityException e) {
            throw OXException.general(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw OXException.general(e.getMessage(), e);
        } catch (NoSuchFieldException e) {
            throw OXException.general(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw OXException.general(e.getMessage(), e);
        }
    }

    /**
     * Gets the properties for current thread.
     * <p>
     * <b>Be careful!</b> Returned map is a read-only reference, not a copy.
     *
     * @return The unmodifiable properties
     */
    public static Map<String, String> getPropertyMap() {
        try {
            final MDCAdapter mdcAdapter = MDC.getMDCAdapter();
            if (mdcAdapter instanceof LogbackMDCAdapter) {
                return ((LogbackMDCAdapter) mdcAdapter).getPropertyMap();
            }
            final org.slf4j.Logger logger = LoggerFactory.getLogger(LogProperties.class);
            logger.warn("Unexpected MDC adapter: {}", mdcAdapter.getClass().getName());
            return Collections.unmodifiableMap(getPropertiesMap(mdcAdapter).get());
        } catch (Exception e) {
            final Logger logger = org.slf4j.LoggerFactory.getLogger(LogProperties.class);
            logger.warn("Failed to retrieve MDC map.", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Removes all log properties associated with current thread.
     */
    public static void removeLogProperties() {
        MDC.clear();
    }

    /**
     * Puts given (<code>LogProperties.Name</code> + value) pairs to properties.
     *
     * @param args The arguments
     */
    public static void putProperties(final Object... args) {
        if (null == args) {
            return;
        }
        final int length = args.length;
        if ((length % 2) != 0) {
            return;
        }
        for (int i = 0; i < length; i+=2) {
            final LogProperties.Name name = (LogProperties.Name) args[i];
            final Object arg = args[i + 1];
            if (null != arg) {
                MDC.put(name.getName(), arg.toString());
            }
        }
    }

    /**
     * Puts session properties.
     *
     * @param session The session
     */
    public static void putSessionProperties(final Session session) {
        if (null == session) {
            return;
        }
        MDC.put(LogProperties.Name.SESSION_SESSION_ID.getName(), getLoggableString(session.getSessionID()));
        MDC.put(LogProperties.Name.SESSION_AUTH_ID.getName(), getLoggableString(session.getAuthId()));
        MDC.put(LogProperties.Name.SESSION_USER_ID.getName(), Integer.toString(session.getUserId()));
        MDC.put(LogProperties.Name.SESSION_USER_NAME.getName(), getLoggableString(session.getLogin()));
        MDC.put(LogProperties.Name.SESSION_LOGIN_NAME.getName(), getLoggableString(session.getLoginName()));
        MDC.put(LogProperties.Name.SESSION_CONTEXT_ID.getName(), Integer.toString(session.getContextId()));
        final String client  = session.getClient();
        MDC.put(LogProperties.Name.SESSION_CLIENT_ID.getName(), client == null ? "unknown" : client);
        // MDC.put(LogProperties.Name.SESSION_SESSION.getName(), session.toString());
    }

    /**
     * Removes session properties.
     */
    public static void removeSessionProperties() {
        MDC.remove(LogProperties.Name.SESSION_SESSION_ID.getName());
        MDC.remove(LogProperties.Name.SESSION_AUTH_ID.getName());
        MDC.remove(LogProperties.Name.SESSION_USER_ID.getName());
        MDC.remove(LogProperties.Name.SESSION_USER_NAME.getName());
        MDC.remove(LogProperties.Name.SESSION_LOGIN_NAME.getName());
        MDC.remove(LogProperties.Name.SESSION_CONTEXT_ID.getName());
        MDC.remove(LogProperties.Name.SESSION_CLIENT_ID.getName());
        // MDC.remove(LogProperties.Name.SESSION_SESSION.getName());
    }

    private static String getLoggableString(String str) {
        return null == str ? "" : str;
    }

    /**
     * Gets the thread-local log property associated with specified name.
     *
     * @param name The property name
     * @return The log property or <code>null</code> if absent
     */
    public static String get(final LogProperties.Name name) {
        return getLogProperty(name);
    }

    /**
     * Gets the thread-local log property associated with specified name.
     *
     * @param name The property name
     * @return The log property or <code>null</code> if absent
     */
    public static String getLogProperty(final LogProperties.Name name) {
        if (null == name) {
            return null;
        }
        return MDC.get(name.getName());
    }

    /**
     * Appends denoted property
     *
     * @param name The name
     */
    public static void append(LogProperties.Name name, String value) {
        if (null == name || null == value) {
            return;
        }
        String sName = name.getName();
        String prev = MDC.get(sName);
        MDC.put(sName, null == prev ? value : new StringBuilder(prev).append(',').append(value).toString());
    }

    /**
     * Removes denoted property
     *
     * @param name The name
     */
    public static void remove(final LogProperties.Name name) {
        removeProperty(name);
    }

    /**
     * Removes denoted property
     *
     * @param name The name
     */
    public static void removeProperty(final LogProperties.Name name) {
        if (null != name) {
            MDC.remove(name.getName());
        }
    }

    /**
     * Removes denoted properties
     *
     * @param names The names
     */
    public static void removeProperties(final LogProperties.Name... names) {
        if (null != names) {
            for (final Name name : names) {
                MDC.remove(name.getName());
            }
        }
    }

    /**
     * Removes denoted properties
     *
     * @param names The names
     */
    public static void removeProperties(final Collection<LogProperties.Name> names) {
        if (null != names) {
            for (final Name name : names) {
                MDC.remove(name.getName());
            }
        }
    }

    /**
     * Puts specified log property. A <code>null</code> value removes the property.
     *
     * @param name The property name
     * @param value The property value
     */
    public static void put(final LogProperties.Name name, final Object value) {
        putProperty(name, value);
    }

    /**
     * Puts specified log property. A <code>null</code> value removes the property.
     *
     * @param name The property name
     * @param value The property value
     */
    public static void putProperty(final LogProperties.Name name, final Object value) {
        if (null == name) {
            return;
        }
        if (null == value) {
            MDC.remove(name.getName());
        } else {
            MDC.put(name.getName(), value.toString());
        }
    }

    /**
     * Get the thread local LogProperties and pretty-prints them into a Sting.
     * The String will contain one ore more lines formatted like:
     * <pre>
     * "propertyName1=propertyValue1"
     * "propertyName2=propertyValue2"
     * "propertyName3=propertyValue3"
     * </pre>
     * where the properties are sorted alphabetically.
     */
    public static String getAndPrettyPrint() {
        return getAndPrettyPrint(Collections.<LogProperties.Name> emptySet());
    }

    /**
     * Get the thread local LogProperties and pretty-prints them into a Sting.
     * The String will contain one ore more lines formatted like:
     * <pre>
     * "propertyName1=propertyValue1"
     * "propertyName2=propertyValue2"
     * "propertyName3=propertyValue3"
     * </pre>
     * where the properties are sorted alphabetically.
     *
     * @param nonMatching The property name to ignore
     */
    public static String getAndPrettyPrint(final LogProperties.Name nonMatching) {
        return getAndPrettyPrint(EnumSet.<LogProperties.Name> of(nonMatching));
    }

    /**
     * Get the thread local LogProperties and pretty-prints them into a Sting.
     * The String will contain one ore more lines formatted like:
     * <pre>
     * "propertyName1=propertyValue1"
     * "propertyName2=propertyValue2"
     * "propertyName3=propertyValue3"
     * </pre>
     * where the properties are sorted alphabetically.
     *
     * @param nonMatching The property names to ignore
     */
    public static String getAndPrettyPrint(final LogProperties.Name... nonMatching) {
        return getAndPrettyPrint(EnumSet.<LogProperties.Name> copyOf(Arrays.asList(nonMatching)));
    }

    /**
     * Get the thread local LogProperties and pretty-prints them into a Sting.
     * The String will contain one ore more lines formatted like:
     * <pre>
     * "propertyName1=propertyValue1"
     * "propertyName2=propertyValue2"
     * "propertyName3=propertyValue3"
     * </pre>
     * where the properties are sorted alphabetically.
     *
     * @param nonMatching The property names to ignore
     */
    public static String getAndPrettyPrint(final Collection<LogProperties.Name> nonMatching) {
        final Set<String> nonMatchingNames;
        if (null == nonMatching) {
            nonMatchingNames = null;
        } else {
            nonMatchingNames = new HashSet<String>(nonMatching.size());
            for (final LogProperties.Name name : nonMatching) {
                nonMatchingNames.add(name.getName());
            }
        }
        // If we have additional log properties from the ThreadLocal add it to the logBuilder
        final StringBuilder logBuilder = new StringBuilder(1024);
        // Sort the properties for readability
        final Map<String, String> sorted = new TreeMap<String, String>();
        final String sep = System.getProperty("line.separator");
        for (final Entry<String, String> propertyEntry : getPropertyMap().entrySet()) {
            final String propertyName = propertyEntry.getKey();
            if (null == nonMatchingNames || !nonMatchingNames.contains(propertyName)) {
                final String value = propertyEntry.getValue();
                if (null != value) {
                    sorted.put(propertyName, value);
                }
            }
        }
        // And add them to the logBuilder
        for (final Map.Entry<String, String> propertyEntry : sorted.entrySet()) {
            logBuilder.append(propertyEntry.getKey()).append('=').append(propertyEntry.getValue()).append(sep);
        }
        return logBuilder.toString();
    }

    /**
     * For convenience.
     *
     * @return Always <code>true</code>
     * @deprecated Use slf4j logging
     */
    @Deprecated
    public static boolean isEnabled() {
        return true;
    }

    /**
     * For convenience.
     *
     * @return The log properties
     * @deprecated Please use slf4j MDC to manage log properties
     */
    @Deprecated
    public static Props getLogProperties() {
        return new Props();
    }

    private static final Set<String> SANITIZE = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("password", "epassword")));
    private static final String REPLACEMENT = "xxx";

    /**
     * Gets the sanitized (URL) parameter value.
     *
     * @param name The name
     * @param value The value
     * @return The sanitized value
     */
    public static String getSanitizedValue(String name, String value) {
        return SANITIZE.contains(Strings.asciiLowerCase(name)) ? REPLACEMENT : value;
    }

    /**
     * Gets the sanitized query-string
     *
     * @param queryString The query-string to sanitize
     * @return The sanitized query-string
     */
    public static String getSanitizedQueryString(String queryString) {
        if (Strings.isEmpty(queryString)) {
            return queryString;
        }
        String[] pairs = Strings.splitByAmps(queryString);
        StringBuilder sb = new StringBuilder(queryString.length());
        boolean first = true;
        for (String pair : pairs) {
            if (first) {
                first = false;
            } else {
                sb.append('&');
            }

            int idx = pair.indexOf('=');
            if (idx <= 0) {
                sb.append(pair);
            } else {
                String key = pair.substring(0, idx);
                sb.append(key);
                if (SANITIZE.contains(Strings.asciiLowerCase(key))) {
                    sb.append('=').append(REPLACEMENT);
                } else {
                    sb.append(pair.substring(idx));
                }
            }
        }
        return sb.toString();
    }

}
