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

package com.openexchange.http.grizzly.service.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ServletFilterRegistration} - Keeps the path -> {@link FilterProxy} mappings and updates the central {@link OSGiMainHandler} when
 * mappings change so they are respected for new incoming requests.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.6.1
 */
public class ServletFilterRegistration {

    private static final Logger LOG = LoggerFactory.getLogger(ServletFilterRegistration.class);
    private static volatile ServletFilterRegistration instance;

    /**
     * Initializes the instance using given configuration service instance
     *
     * @param configService The configuration service instance
     */
    public static void initInstance() {
        instance = new ServletFilterRegistration();
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
    private volatile OSGiMainHandler handler;
    private final Comparator<FilterProxy> comparator;

    /**
     * Initializes a new {@link ServletFilterRegistration}.
     */
    private ServletFilterRegistration() {
        super();
        // we start with empty collections
        this.matchingAllPaths = new LinkedList<FilterProxy>();
        this.matchingPath = new HashMap<String, List<FilterProxy>>();
        this.matchingPrefixPath = new HashMap<String, List<FilterProxy>>();

        comparator = new Comparator<FilterProxy>() {

            @Override
            public int compare(FilterProxy o1, FilterProxy o2) {
                int rank1 = o1.getRanking();
                int rank2 = o2.getRanking();
                return (rank1 < rank2 ? -1 : (rank1 == rank2 ? 0 : 1));
            }
        };
    }

    /**
     * Gets all filters for given path. The filters are ordered by their service ranking. If multiple filters have the same service ranking
     * the are ordered as follows:
     * 
     * <ol>
     *   <li>filters that match all paths e.g. were registered without a path or with the /* wildcard</li>
     *   <li>filters that match a prefix of a path e.g. filters that are registered for /a/b should be used for requests to /a/b/c, too</li>
     *   <li>filters that match a path e.g. filters that are registered for /a/b should be used for requests to /a/b</li>
     * </ol> 
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
     * Gets all filters for given path. The filters are ordered by their service ranking. If multiple filters have the same service ranking
     * the are ordered as follows:
     * 
     * <ol>
     *   <li>filters that match all paths e.g. were registered without a path or with the /* wildcard</li>
     *   <li>filters that match a prefix of a path e.g. filters that are registered for /a/b should be used for requests to /a/b/c, too</li>
     *   <li>filters that match a path e.g. filters that are registered for /a/b should be used for requests to /a/b</li>
     * </ol> 
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

        // Add the handlers for matching 
        List<FilterProxy> proxies = this.matchingPath.get(path);
        if (proxies != null) {
            filters.addAll(proxies);
        }

        // Now order them according to service ranking
        if (null != comparator) {
            Collections.sort(filters, comparator);
        } else {
            LOG.error("Missing comparator, unable to sort servlet filters properly");
        }

        return filters;
    }

    /**
     * Gets all filters. The filters are ordered by their service ranking. If multiple filters have the same service ranking
     * the are ordered as follows:
     * 
     * <ol>
     *   <li>filters that match all paths e.g. were registered without a path or with the /* wildcard</li>
     *   <li>filters that match a prefix of a path e.g. filters that are registered for /a/b should be used for requests to /a/b/c, too</li>
     *   <li>filters that match a path e.g. filters that are registered for /a/b should be used for requests to /a/b</li>
     * </ol> 
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

        // Now order them according to service ranking
        if (null != comparator) {
            Collections.sort(filters, comparator);
        } else {
            LOG.error("Missing comparator, unable to sort servlet filters properly");
        }

        return filters;
    }

    /**
     * Puts given servlet filter proxy into this registration. Depending on the paths specified in the proxy the servlet filters will be
     * registered like follows:
     * <ul>
     *   <li>
     *     There is no path specified for the filter or the path equals <strong>/*</strong>: This filter will be applied to all request
     *   </li>
     *   <li>
     *     The path ends with <strong>/*</strong> wildcard but doesn't equal <strong>/*</strong> e.g. <strong>/a/b/*</strong>: This filter
     *     will be used for requests to all URLs starting with <strong>/a/b</strong> e.g <strong>/a/b/c</strong>, <strong>/a/b/c/d</strong>
     *     and so on
     *   </li>
     *   <li>
     *     The path doesn't end with the <strong>/*</strong> wildcard: This filter will only be used for requests that match this path
     *     exactly
     *   </li>
     * </ul>
     * 
     * @param proxy The filter proxy to register
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
                final String servletPath = paths[i];
                if (servletPath.endsWith("/*")) {
                    // prefix servletPath: we remove the /*
                    final String prefix = servletPath.substring(0, servletPath.length() - 2);
                    //corner case: a filterproxy with path '/*' should be applied to all requests
                    if(prefix.isEmpty()) {
                        final List<FilterProxy> newMatchingAllPaths = new ArrayList<FilterProxy>(this.matchingAllPaths);
                        newMatchingAllPaths.add(proxy);
                        this.matchingAllPaths = newMatchingAllPaths;
                    } else {
                        if (newMatchingPrefixPath == null) {
                            newMatchingPrefixPath = new HashMap<String, List<FilterProxy>>(this.matchingPrefixPath);
                        }
                        this.updateMap(newMatchingPrefixPath, prefix, proxy, true);
                    }
                } else {
                    // exact match
                    if (newMatchingPath == null) {
                        newMatchingPath = new HashMap<String, List<FilterProxy>>(this.matchingPath);
                    }
                    this.updateMap(newMatchingPath, servletPath, proxy, true);
                }
            }
            if (newMatchingPath != null) {
                this.matchingPath = newMatchingPath;
            }
            if (newMatchingPrefixPath != null) {
                this.matchingPrefixPath = newMatchingPrefixPath;
            }
        }
        if (handler != null) {
            handler.updateTrackedServletFilters(this);
        } else {
            LOG.error("OSGIMainHandler not set, unable to update servlet filters");
        }
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
                final String servletPath = paths[i];
                if (servletPath.endsWith("/*")) {
                    // prefix servlet path: we remove the /*
                    final String prefix = servletPath.substring(0, servletPath.length() - 2);
                    //corner case: a filterproxy with path '/*' should be applied to all requests
                    if(prefix.isEmpty()) {
                        final List<FilterProxy> newMatchingAllPaths = new ArrayList<FilterProxy>(this.matchingAllPaths);
                        if(!newMatchingAllPaths.remove(proxy)) {
                            LOG.error("Failed to remove FilterProxy: {}", proxy);
                        }
                        this.matchingAllPaths = newMatchingAllPaths;
                    } else {
                        if (newMatchingPrefixPath == null) {
                            newMatchingPrefixPath = new HashMap<String, List<FilterProxy>>(this.matchingPrefixPath);
                        }
                        this.updateMap(newMatchingPrefixPath, prefix, proxy, false);
                    }
                } else {
                    // exact match
                    if (newMatchingPath == null) {
                        newMatchingPath = new HashMap<String, List<FilterProxy>>(this.matchingPath);
                    }
                    this.updateMap(newMatchingPath, servletPath, proxy, false);
                }
            }
            if (newMatchingPath != null) {
                this.matchingPath = newMatchingPath;
            }
            if (newMatchingPrefixPath != null) {
                this.matchingPrefixPath = newMatchingPrefixPath;
            }
        }
        if (handler != null) {
            handler.removeTrackedServletFilter(proxy);
        } else {
            LOG.error("OSGIMainHandler not set, unable to update servlet filters");
        }
    }

    /**
     * Update the map keeping the path -> FilterProxy associations by adding or removing a path, {@link FilterProxy} pair.
     * 
     * @param proxyListMap The current mapping that has to be updated
     * @param path The path that should be modified
     * @param proxy The FilterProxy that should be updated for the given path
     * @param add if true add the {@link FilterProxy} to the given path, else remove it from the path.
     */
    private void updateMap(final Map<String, List<FilterProxy>> proxyListMap, final String path, final FilterProxy proxy, final boolean add) {
        List<FilterProxy> proxies = proxyListMap.get(path);
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
            proxyListMap.remove(path);
        } else {
            proxyListMap.put(path, proxies);
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
