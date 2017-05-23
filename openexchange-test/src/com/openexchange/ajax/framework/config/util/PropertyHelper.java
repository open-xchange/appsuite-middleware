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

package com.openexchange.ajax.framework.config.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * {@link PropertyHelper}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class PropertyHelper {

    /**
     * Filter the properties by the given name.
     * 
     * @param name a part of the key
     * @param prefix if true, property key must begin with the given name. Otherwise, the key merely contains the
     *            name to qualify.
     *
     * @return key-value pairs that match.
     */
    public static Map<String, Object> filter(Map<String, Object> props, String name, boolean prefix, boolean includeArrays) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (String key : props.keySet()) {
            if (key == null)
                continue;
            boolean match = prefix ? key.startsWith(name) : (key.indexOf(name) != -1);
            if (match && !isArray(key)) {
                result.put(key, props.get(key));
            }
        }
        if (includeArrays) {
            Map<String, List<Object>> arrayProperties = filterArrayKeys(props, name, prefix);
            result.putAll(arrayProperties);
        }
        return result;
    }

    /**
     * Select only those property keys which ends with an array marker such as <code>openjpa.DataCache[1]</code>.
     * The multiple values of the property is inserted into the resultant map as a List of object against the
     * original key.
     * <br>
     * For example, if the original map had three key-value pairs as
     * <LI><code>openjpa.DataCache[1]=true</code>
     * <LI><code>openjpa.DataCache[2]=false</code>
     * <LI><code>openjpa.DataCache[3]=default</code>
     * <br>
     * Then that will result into a single entry in the resultant Map under the key <code>openjpa.DataCache</code>
     * with a value as a List of three elements namely <code>{true, false, default}</code>. The array index values
     * are not significant other than they must all be different for the same base key.
     * 
     * @param name part of the property key
     * @param prefix does the name must appear as a prefix?
     * 
     * @return key-value pairs that match.
     */
    public static Map<String, List<Object>> filterArrayKeys(Map<String, Object> props, String name, boolean prefix) {
        Map<String, List<Object>> result = new HashMap<String, List<Object>>();
        for (String key : props.keySet()) {
            boolean match = prefix ? key.startsWith(name) : (key.indexOf(name) != -1);
            if (match && isArray(key)) {
                String realKey = removeArray(key);
                List<Object> values = result.get(realKey);
                if (values == null) {
                    values = new ArrayList<Object>();
                    result.put(realKey, values);
                }
                values.add(props.get(key));
            }
        }
        return result;
    }

    /**
     * Load properties from the given name resource.
     * The given named resource is first looked up as resource on the current thread's context
     * and if not found as a file input.
     * 
     * @param resource name a of resource.
     * 
     * @return empty properties if no resource found.
     */
    public static Map<String, Object> load(String resource) {
        Properties p = new Properties();
        try {
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            if (stream == null) {
                stream = new FileInputStream(resource);
            }
            p.load(stream);
        } catch (Exception e) {
            System.err.println("Error reading " + resource + " due to " + e);
        }
        return toMap(p);
    }

    /**
     * Affirm if the given resource is available either as a resource in the current thread's context classpath
     * or as a file.
     * 
     */
    public static boolean canFind(String resource) {
        if (resource == null)
            return false;
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (stream != null)
            return true;
        return new File(resource).exists();
    }

    public static Map<String, Object> toMap(Properties p) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (Object k : p.keySet()) {
            result.put(k.toString(), p.get(k));
        }
        return result;
    }

    /**
     * Overwrites any key-value pair in the given map for which a System property is available
     * 
     * @param original properties to be overwritten
     * @return the original property overwritten with System properties
     */
    public static Map<String, Object> overwriteWithSystemProperties(Map<String, Object> original) {
        Properties properties = System.getProperties();
        for (Object syskey : properties.keySet()) {
            if (original.containsKey(syskey)) {
                original.put(syskey.toString(), properties.get(syskey));
            }
        }
        return original;
    }

    public static int getInteger(Map<String, Object> props, String key, int def) {
        int result = def;
        try {
            Object value = props.get(key);
            if (value != null)
                result = Integer.parseInt(value.toString());
        } catch (NumberFormatException nfe) {

        }
        return result;
    }

    public static double getDouble(Map<String, Object> props, String key, double def) {
        double result = def;
        try {
            Object value = props.get(key);
            if (value != null)
                result = Double.parseDouble(value.toString());
        } catch (NumberFormatException nfe) {

        }
        return result;
    }

    public static String getString(Map<String, Object> props, String key, String def) {
        Object value = props.get(key);
        if (value != null)
            return value.toString();
        return def;
    }

    public static List<String> getStringList(Map<String, Object> props, String key) {
        return getStringList(props, key, Collections.EMPTY_LIST);
    }

    public static List<String> getStringList(Map<String, Object> props, String key, List<String> def) {
        Object value = props.get(key);
        if (value != null)
            return Arrays.asList(value.toString().split("\\,"));
        return def;
    }

    public static Map<String, String> getMap(Map<String, Object> props, String key) {
        return getMap(props, key, Collections.EMPTY_MAP);
    }

    public static Map<String, String> getMap(Map<String, Object> props, String key, Map<String, String> def) {
        List<String> pairs = getStringList(props, key);
        if (pairs == null || pairs.isEmpty())
            return def;
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (String pair : pairs) {
            int index = pair.indexOf("->");
            if (index != -1) {
                String name = pair.substring(0, index).trim();
                String value = pair.substring(index + 2).trim();
                result.put(name, value);
            }
        }
        return result;
    }

    /**
     * Affirms if the given string using array [] symbol at the end.
     * 
     * @param key a string to check for array symbol.
     */
    private static boolean isArray(String key) {
        if (key == null || key.length() < 3 || !key.endsWith("]"))
            return false;
        int i = key.indexOf("[");
        if (i == -1 || i != key.lastIndexOf("["))
            return false;
        String index = key.substring(i + 1, key.length() - 1);
        try {
            Integer.parseInt(index);
        } catch (NumberFormatException e) {
            System.err.println("Bad index " + index + " in " + key);
            return false;
        }
        return true;
    }

    private static String removeArray(String key) {
        int i = key.indexOf("[");
        return key.substring(0, i);
    }

    public static Set<String> getSubsectionKeys(Set<String> keys, String section) {
        String prefix = asPrefix(section);
        Set<String> subsections = new HashSet<String>();
        for (String key : keys) {
            if (key.startsWith(prefix)) {
                subsections.add(prefix + getPrefix(key.substring(prefix.length())));
            }
        }
        return subsections;
    }

    private static final String DOT = ".";

    private static String asPrefix(String s) {
        if (s.endsWith(DOT))
            return s;
        return s + DOT;
    }

    public static String getPrefix(String s) {
        int i = s.indexOf(DOT);
        return (i == -1) ? s : s.substring(0, i);
    }

    /**
     * Get the portion of the given map whose key has the given section at prefix.
     * 
     * @param props a set of name-value pair
     * @param section a string representing section of a key
     * 
     * @return a new map with only the keys that starts with the given section.
     */
    public static Map<String, Object> getSection(Map<String, Object> props, String section) {
        return getSection(props, section, false);
    }

    /**
     * Get the portion of the given map whose key has the given section at prefix.
     * 
     * @param props a set of name-value pair
     * @param section a string representing section of a key
     * @param retain if true the key of resultant map is same as the original map. Otherwise
     *            the resultant map keys are without the section prefix.
     * 
     * @return the map with only the keys that starts with the given section.
     */
    public static Map<String, Object> getSection(Map<String, Object> props, String section, boolean retain) {
        Map<String, Object> result = new HashMap<String, Object>(props);
        Set<String> keys = props.keySet();
        String prefix = asPrefix(section);
        for (String key : keys) {
            if (key.startsWith(prefix)) {
                String newKey = retain ? key : key.substring(prefix.length());
                result.put(newKey, props.get(key));
            }
        }
        return result;
    }
}
