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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.http.grizzly.service.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link ServletFilterRegistration}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class ServletFilterRegistration {

    private static volatile ServletFilterRegistration instance;

    /**
     * Initializes the instance using given configuration service instance
     *
     * @param configService The configuration service instance
     * @throws IOException If initialization fails
     */
    public static void initInstance(ConfigurationService configService) throws IOException {
        List<String> filterNames = null;
        {
            File file = configService.getFileByName("Service.properties");
            if (null != file) {
                BufferedReader reader = new BufferedReader(new FileReader(file), 65536);
                try {
                    for (String line; (line = reader.readLine()) != null;) {
                        line = line.trim();
                        if (!Strings.isEmpty(line) && !line.startsWith("#") && !line.startsWith("!")) {
                            if (null == filterNames) {
                                filterNames = new LinkedList<String>();
                            }
                            filterNames.add(line);
                        }
                    }
                } finally {
                    Streams.close(reader);
                }
            }
        }
        instance = new ServletFilterRegistration(filterNames);
    }

    /**
     * Drops the instance.
     */
    public static void dropInstance() {
        instance = null;
    }

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static ServletFilterRegistration getInstance() {
        return instance;
    }

    // ------------------------------------------------------------------------------------------------------- //

    private volatile List<FilterProxy> matchingAllPaths;
    private volatile Map<String, List<FilterProxy>> matchingPath;
    private volatile Map<String, List<FilterProxy>> matchingPrefixPath;
    private final Comparator<FilterProxy> comparator;
    private volatile OSGiMainHandler handler;

    /**
     * Initializes a new {@link ServletFilterRegistration}.
     */
    private ServletFilterRegistration(List<String> filterNames) {
        super();
        // we start with empty collections
        this.matchingAllPaths = new LinkedList<FilterProxy>();
        this.matchingPath = new HashMap<String, List<FilterProxy>>();
        this.matchingPrefixPath = new HashMap<String, List<FilterProxy>>();

        if (null == filterNames) {
            this.comparator = null;
        } else {
            final Map<String, Integer> m = new HashMap<String, Integer>(filterNames.size());
            {
                int count = 0;
                for (String name : filterNames) {
                    m.put(name, Integer.valueOf(++count));
                }
            }
            this.comparator = new Comparator<FilterProxy>() {

                @Override
                public int compare(FilterProxy proxy1, FilterProxy proxy2) {
                    Integer pos1 = m.get(proxy1.getClass().getName());
                    Integer pos2 = m.get(proxy2.getClass().getName());

                    if (null == pos1) {
                        return null == pos2 ? proxy1.getClass().getName().compareTo(proxy2.getClass().getName()) : 1;
                    }

                    return null == pos2 ? -1 : pos1.compareTo(pos2);
                }
            };
        }
    }

    /**
     * Gets all filters for given path
     *
     * @param path The Servlet path
     * @return All sorted filters for the path
     */
    public List<Filter> getFilters(String path) {
        List<FilterProxy> proxies = getFilterProxies(path);
        List<Filter> filters = new LinkedList<Filter>();
        for (FilterProxy proxy : proxies) {
            filters.add(proxy.getFilter());
        }
        return filters;
    }

    /**
     * Gets all filters for given path
     *
     * @param path The Servlet path
     * @return All sorted filters for the path
     */
    public List<FilterProxy> getFilterProxies(String path) {
        List<FilterProxy> filters = new LinkedList<FilterProxy>();

        // Add all handlers matching everything
        filters.addAll(this.matchingAllPaths);

        // Now check for prefix matches
        {
            Map<String, List<FilterProxy>> thisMatchingPrefixPath = this.matchingPrefixPath;
            if (!thisMatchingPrefixPath.isEmpty()) {
                int pos = path.lastIndexOf('/');
                while (pos != -1) {
                    final String prefix = path.substring(0, pos);
                    List<FilterProxy> proxies = thisMatchingPrefixPath.get(prefix);
                    if (proxies != null) {
                        filters.addAll(proxies);
                    }

                    pos = prefix.lastIndexOf('/');
                }
            }
        }

        // Add the handlers for matching topic names
        List<FilterProxy> proxies = this.matchingPath.get(path);
        if (proxies != null) {
            filters.addAll(proxies);
        }

        // Now order them according to 'Service.properties' file
        if (null != comparator) {
            Collections.sort(filters, comparator);
        }

        return filters;
    }

    /**
     * Gets all filters
     *
     * @return All filters
     */
    public List<FilterProxy> getFilters() {
        List<FilterProxy> filters = new LinkedList<FilterProxy>();

        filters.addAll(this.matchingAllPaths);

        {
            Map<String, List<FilterProxy>> thisMatchingPrefixPath = this.matchingPrefixPath;
            for (List<FilterProxy> proxies : thisMatchingPrefixPath.values()) {
                filters.addAll(proxies);
            }
        }

        {
            Map<String, List<FilterProxy>> thisMatchingPath = this.matchingPath;
            for (List<FilterProxy> proxies : thisMatchingPath.values()) {
                filters.addAll(proxies);
            }
        }

        return filters;
    }

    /**
     * Puts given filter proxy
     *
     * @param proxy The filter proxy
     */
    public synchronized void put(final FilterProxy proxy) {
        String[] paths = proxy.getPaths();
        if (paths == null) {
            final List<FilterProxy> newMatchingAllPaths = new ArrayList<FilterProxy>(this.matchingAllPaths);
            newMatchingAllPaths.add(proxy);
            this.matchingAllPaths = newMatchingAllPaths;
        } else {
            Map<String, List<FilterProxy>> newMatchingPath = null;
            Map<String, List<FilterProxy>> newMatchingPrefixPath = null;
            for (int i = 0; i < paths.length; i++) {
                final String topic = paths[i];

                if (topic.endsWith("/*")) {
                    // prefix topic: we remove the /*
                    if (newMatchingPrefixPath == null) {
                        newMatchingPrefixPath = new HashMap<String, List<FilterProxy>>(this.matchingPrefixPath);
                    }

                    final String prefix = topic.substring(0, topic.length() - 2);
                    this.updateMap(newMatchingPrefixPath, prefix, proxy, true);
                } else {
                    // exact match
                    if (newMatchingPath == null) {
                        newMatchingPath = new HashMap<String, List<FilterProxy>>(this.matchingPath);
                    }

                    this.updateMap(newMatchingPath, topic, proxy, true);
                }
            }
            if (newMatchingPath != null) {
                this.matchingPath = newMatchingPath;
            }
            if (newMatchingPrefixPath != null) {
                this.matchingPrefixPath = newMatchingPrefixPath;
            }
        }

        handler.updateTrackedServletFilters(this);
    }

    /**
     * Removes given filter proxy.
     *
     * @param proxy The filter proxy to remove
     */
    public synchronized void remove(final FilterProxy proxy) {
        final String[] paths = proxy.getPaths();
        if (paths == null) {
            final List<FilterProxy> newMatchingAllPaths = new ArrayList<FilterProxy>(this.matchingAllPaths);
            newMatchingAllPaths.remove(proxy);
            this.matchingAllPaths = newMatchingAllPaths;
        } else {
            Map<String, List<FilterProxy>> newMatchingPath = null;
            Map<String, List<FilterProxy>> newMatchingPrefixPath = null;
            for (int i = 0; i < paths.length; i++) {
                final String topic = paths[i];

                if (topic.endsWith("/*")) {
                    // prefix topic: we remove the /*
                    if (newMatchingPrefixPath == null) {
                        newMatchingPrefixPath = new HashMap<String, List<FilterProxy>>(this.matchingPrefixPath);
                    }

                    final String prefix = topic.substring(0, topic.length() - 2);
                    this.updateMap(newMatchingPrefixPath, prefix, proxy, false);
                } else {
                    // exact match
                    if (newMatchingPath == null) {
                        newMatchingPath = new HashMap<String, List<FilterProxy>>(this.matchingPath);
                    }

                    this.updateMap(newMatchingPath, topic, proxy, false);
                }
            }
            if (newMatchingPath != null) {
                this.matchingPath = newMatchingPath;
            }
            if (newMatchingPrefixPath != null) {
                this.matchingPrefixPath = newMatchingPrefixPath;
            }
        }

        handler.removeTrackedServletFilter(proxy);
    }

    private void updateMap(final Map<String, List<FilterProxy>> proxyListMap, final String key, final FilterProxy proxy, final boolean add) {
        List<FilterProxy> proxies = proxyListMap.get(key);
        if (proxies == null) {
            if (!add) {
                return;
            }
            proxies = new LinkedList<FilterProxy>();
        } else {
            proxies = new LinkedList<FilterProxy>(proxies);
        }
        if (add) {
            proxies.add(proxy);
        } else {
            proxies.remove(proxy);
        }
        if (proxies.size() == 0) {
            proxyListMap.remove(key);
        } else {
            proxyListMap.put(key, proxies);
        }
    }

    /**
     * Sets the {@link OSGiMainHandler} instance.
     *
     * @param handler The instance to set
     */
    public void setOSGiMainHandler(OSGiMainHandler handler) {
        this.handler = handler;
    }

}
