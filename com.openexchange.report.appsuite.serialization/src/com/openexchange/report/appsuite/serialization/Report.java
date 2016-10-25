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

package com.openexchange.report.appsuite.serialization;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.report.appsuite.serialization.osgi.StringParserServiceRegistry;
import com.openexchange.tools.strings.StringParser;

/**
 * A {@link Report} contains the analysis of a context ( in a {@link ContextReport}), a User ( in a {@link UserReport} ) or the system ( in
 * a regular {@link Report} ). It also keeps track of runtime statistics (when was it started, when was it done, how many tasks must be
 * performed, how many are still open).
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Report implements Serializable {

    private static final long serialVersionUID = 6998213011280390705L;

    private final String uuid;

    private final String type;

    private final Map<String, Map<String, Object>> namespaces = new HashMap<String, Map<String, Object>>();

    private final long startTime;

    private long stopTime;

    private int numberOfTasks;

    private int pendingTasks;

    /**
     * Initializes a new {@link Report}.
     *
     * @param uuid The uuid of this report run
     * @param type The type of this report run to determine which analyzers and cumulators are asked for contributions
     * @param startTime When this report was started in milliseconds.
     */
    public Report(String uuid, String type, long startTime) {
        this.uuid = uuid;
        this.type = type;
        this.startTime = startTime;
    }

    /**
     * Save a value in the report
     * 
     * @param ns a namespace, to keep the data of different analyzers and cumulators separate from one another
     * @param key the name to save the value under
     * @param value the value. The value must be serializable, and a List (composed of these legal values) or Map (again, composed of these legal values) or a primitive Java Type or a String
     * @return this report, so that multiple set commands can be chained
     */
    public Report set(String ns, String key, Serializable value) {
        checkValue(value);
        getNamespace(ns).put(key, value);

        return this;
    }

    private static Class[] allowedTypes = new Class[] { Integer.class, Long.class, Float.class, Short.class, Double.class, Byte.class, Boolean.class, String.class };

    private void checkValue(Object value) {

        if (value == null) {
            throw new NullPointerException("value may not be null");
        }

        if (!(value instanceof Serializable)) {
            throw new IllegalArgumentException("Illegal type! Use only serializable types! " + value.getClass() + ": " + value);
        }
        if (value instanceof Map) {
            Map map = (Map) value;
            for (Object oEntry : map.entrySet()) {
                Map.Entry entry = (Map.Entry) oEntry;
                checkValue(entry.getKey());
                checkValue(entry.getValue());
            }
            return;
        } else if (value instanceof Collection) {
            Collection col = (Collection) value;
            for (Object o : col) {
                checkValue(o);
            }
            return;
        } else {
            for (Class candidate : allowedTypes) {
                if (candidate.isInstance(value)) {
                    return;
                }
            }
        }

        throw new IllegalArgumentException("Illegal type! Use only native java types! Was " + value.getClass());

    }

    /**
     * Retrieve a value and try to turn it into an Object of the given class
     * 
     * @param ns The namespace, as it was used in {@link #set(String, String, Serializable)}
     * @param key The key
     * @param klass The klass to try and turn the value into
     * @return The value or null, if the value wasn't previously set.
     */
    public <T> T get(String ns, String key, Class<T> klass) {
        return get(ns, key, null, klass);
    }

    /**
     * Retrieve a value and try to turn it into an Object of the given class
     * 
     * @param ns The namespace, as used by {@link #set(String, String, Serializable)}
     * @param key The key
     * @param defaultValue The value to return, if no value was set in this report
     * @param klass The klass to try and turn the value into
     * @return
     */
    public <T> T get(String ns, String key, T defaultValue, Class<T> klass) {
        Object value = getNamespace(ns).get(key);
        if (value == null) {
            return defaultValue;
        }

        if (klass.isAssignableFrom(klass)) {
            return (T) value;
        }

        if (klass == String.class) {
            return (T) value.toString();
        }
        return StringParserServiceRegistry.getServiceRegistry().getService(StringParser.class).parse(value.toString(), klass);
    }

    /**
     * Remove a value from the report
     */
    public void remove(String ns, String key) {
        Map<String, Object> namespace = getNamespace(ns);
        namespace.remove(key);
        if (namespace.isEmpty()) {
            namespaces.remove(ns);
        }
    }

    /**
     * Remove all values in the given namespace
     */
    public void clearNamespace(String ns) {
        namespaces.remove(ns);
    }

    /**
     * Retrieve the Namespace -> ( Key -> Value ) mappings. Note that this is the internal Object, so
     * whatever you do with it, it will also change the state in the report. Though it's better to use {@link #set(String, String, Serializable)}, {@link #get(String, String, Class)}, {@link #remove(String, String)} and
     * {@link #clearNamespace(String)} for modifying this Report
     */
    public Map<String, Map<String, Object>> getData() {
        return namespaces;
    }

    public String getUUID() {
        return uuid;
    }

    public String getType() {
        if (type == null) {
            return "default";
        }
        return type;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    /**
     * Retrieve a namespace. Useful for e.g. iterating of all keys in a namespace. Note this is also the internal Object, so modifying it
     * will have side-effects in the state of the report
     */
    public Map<String, Object> getNamespace(String ns) {
        Map<String, Object> candidate = namespaces.get(ns);
        if (candidate == null) {
            candidate = new HashMap<String, Object>();
            namespaces.put(ns, candidate);
        }
        return candidate;
    }

    /**
     * A report tracks the number of tasks that had to be, or still have to be, looked at for the report to complete. This is usually the number of contexts
     * that have to be analyzed
     */
    public void setNumberOfTasks(int numberOfTasks) {
        this.numberOfTasks = numberOfTasks;
        this.pendingTasks = numberOfTasks;
    }

    public void setTaskState(int numberOfTasks, int pendingTasks) {
        this.numberOfTasks = numberOfTasks;
        this.pendingTasks = pendingTasks;
    }

    /**
     * Retrieve the number of tasks that remain to be done. A report is complete when this number reaches 0.
     */
    public int getNumberOfPendingTasks() {
        return pendingTasks;
    }

    /**
     * A report tracks the number of tasks that had to be, or still have to be, looked at for the report to complete. This is usually the
     * number of contexts that have to be analyzed
     */
    public int getNumberOfTasks() {
        return numberOfTasks;
    }

    @Override
    public String toString() {
        return "Report [UUID=" + uuid + ", type=" + type + ", tasks=" + numberOfTasks + ", tasksToDo=" + pendingTasks + "]";
    }
}
